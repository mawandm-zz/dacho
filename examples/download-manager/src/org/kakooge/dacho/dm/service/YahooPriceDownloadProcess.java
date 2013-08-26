package org.kakooge.dacho.dm.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.dm.model.Security;
import org.kakooge.dacho.dm.process.DownloadTask;


/**
 * Download process for Yahoo finance 
 * @author mawandm
 */
public class YahooPriceDownloadProcess implements DownloadProcess{
   
	public static final String DB_DRIVER = "db.driver";
	public static final String DB_URL = "db.url";
	public static final String DB_USERNAME = "db.username"; // fin_db_user
	public static final String DB_PASSWORD = "db.password"; // 4nDbYza
    
	private final Properties properties;
	private final Logger logger;
	private final ServiceContext serviceContext;
	private final static String CONNECTION_STRING_FORMAT = "jdbc:derby:%s/%s;";
	
	private ExecutorService downloadExecutor;
	private ExecutorService saveExecutor;
	
    private final List<Security> securityList = new ArrayList<Security>();
    
    private PoolingClientConnectionManager connectionManager = null;
    private HttpClient httpClient = null;
    private Connection connection;
    
    public final static String RUN_MARKET="run.market";
    public final static String RUN_SCHEDULE = "run.schedule";
    
    private final static boolean debug = System.getProperty("debug")!=null;
   
   public YahooPriceDownloadProcess(Logger logger, ServiceContext serviceContext, final Properties properties) throws ClassNotFoundException{
       this.properties = properties;
       Class.forName(properties.getProperty(DB_DRIVER));
       this.logger = logger;
       this.serviceContext = serviceContext;
       

   }
   
   @Override
   public void init() throws Exception{
	   
	   if(debug)
		   logger.info("Initializing process " + properties.getProperty(RUN_MARKET));
	   
       downloadExecutor = Executors.newCachedThreadPool();
       saveExecutor = Executors.newCachedThreadPool();	   

       connectionManager = new PoolingClientConnectionManager();
       httpClient = new DefaultHttpClient(connectionManager);
   }
   
   @Override
   public void destroy() throws Exception{  
	   
	   if(debug)
		   logger.info("Destroying process " + properties.getProperty(RUN_MARKET));
	   
	   if(downloadExecutor!=null)
		   downloadExecutor.shutdown();
	   
	   if(saveExecutor!=null)
		   saveExecutor.shutdown();
	   
	   if(httpClient!=null)
		   httpClient.getConnectionManager().shutdown();
	   
	   if(connectionManager!=null)
		   connectionManager.shutdown();
   }
   
   private synchronized void initConnection() throws SQLException{
	   final String url = String.format(CONNECTION_STRING_FORMAT, serviceContext.getInitParameter(ServiceContext.SERVICE_HOME), properties.getProperty(DB_URL));
	   final String username = properties.getProperty(DB_USERNAME);
	   final String password = properties.getProperty(DB_PASSWORD);
       
	   connection = DriverManager.getConnection(url, username, password);
	   
	   if(securityList.size()==0) //- Don't initialize twice
		   initSecurities();	   
   }
   
   private void destroyConnection(){
	   if(connection!=null) {
    	   try{
    		   connection.close();
    	   }catch(SQLException ignore){}
       }
   }
   
   private void initSecurities() throws SQLException{
       Statement statement = null;
       
       //System.out.println("Downloading market: " + properties.getProperty(RUN_MARKET));
       
       statement = connection.createStatement();
       String sql = String.format("select symbol from app.security where type = 'EQTY' and market in (%s)", properties.getProperty(RUN_MARKET));
       final ResultSet resultSet = statement.executeQuery(sql);
       try{
           while(resultSet.next()){
               String symbol = resultSet.getString("symbol");
               Security security = new Security();
               try{
            	   security.symbol = Security.getSymbolLong(symbol);
               }catch(IllegalArgumentException iae){
            	   logger.log(Level.WARNING, "Found error " + iae.getMessage());
            	   continue;
               }
               securityList.add(security);
           }
       }finally{
           if(resultSet!=null) {
        	   try{
        		   resultSet.close();
        	   }catch(SQLException ignore){}
           }
       }
       
       if(debug)
    	   logger.info(String.format("Market: %s has %s instruments", properties.getProperty(RUN_MARKET), securityList.size()));
   }
   
   
   
   /**
    * 
    * @param statement
    * @param security
    * @throws Exception
    */
   private void save(final PreparedStatement statement, final Security security) throws Exception {
   
	   if(security==null)
		   return;
	   
	   String symbolString = Security.getSymbolString(security);
	   
       if(debug)
    	   logger.info("Saving symbol " + symbolString);
	   
	   statement.setString(1, symbolString);
	   statement.setDate(2, new java.sql.Date(security.date));
	   statement.setDouble(3, security.open);
	   statement.setDouble(4, security.low);
	   statement.setDouble(5, security.high);
	   statement.setDouble(6, security.close);
	   statement.setDouble(7, security.volume);
	   
	   try{
		   statement.execute();
	   }catch (SQLException e){
            if(!e.getSQLState().equals("23505")) // Derby has no concept of replace into table values (....) so we just ignore the duplicate record
            {
            	//logger.log(Level.SEVERE, String.format("Could not save record: %s", security), e);
            	throw e;
            }
        }
   }
   
   @Override
   public void run(){
	   
	   CompletionService<Security> downloadCompletionService = new ExecutorCompletionService<Security>(downloadExecutor);
	      
	   PreparedStatement statement = null;
	   try {
		   initConnection();

		   for(final Security security : securityList){
			   DownloadTask downloadTask = new DownloadTask(httpClient, security);
			   downloadCompletionService.submit(downloadTask);
		   }		   
		   
		   statement = connection.prepareStatement("insert into app.price values" +
				   "(?,?,?,?,?,?,?)");
		   
		   //- JDBC Connection isn't guaranteed to be thread safe so we serially save results as they become available
		   for(int idx=0; idx<securityList.size();++idx){
			   Future<Security> future = downloadCompletionService.take();
			   Security security = future.get();
			   save(statement, security);
		   }
		   
	   } catch (InterruptedException e) {
		   
		   logger.info("Shutting down " + properties.getProperty(RUN_MARKET));
		   
		   Thread.currentThread().interrupt();	 
	   } catch (Exception e) {
		   logger.log(Level.SEVERE, "An error occured during the SaveTask execution", e);			   
	   }finally{
		   try {
			   destroyConnection();
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
	   }
   }
}


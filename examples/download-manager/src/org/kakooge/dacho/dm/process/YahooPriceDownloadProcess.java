package org.kakooge.dacho.dm.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.dm.model.Security;
import org.kakooge.dacho.dm.service.ServiceRunnable;


/**
 * Download process for Yahoo finance 
 * @author mawandm
 */
public class YahooPriceDownloadProcess extends AbstractDownloadProcess<String[], List<Security>>{
   
	public static final String DB_DRIVER = "db.driver";
	public static final String DB_URL = "db.url";
	public static final String DB_USERNAME = "db.username"; // fin_db_user
	public static final String DB_PASSWORD = "db.password"; // 4nDbYza
    
	final private Properties properties;
	final private Logger logger;
	final ServiceContext serviceContext;
	final private static String CONNECTION_STRING_FORMAT = "jdbc:derby:%s/%s;";
   
   public YahooPriceDownloadProcess(Logger logger, ServiceContext serviceContext, final Properties properties) throws ClassNotFoundException{
       this.properties = properties;
       Class.forName(properties.getProperty(DB_DRIVER));
       this.logger = logger;
       this.serviceContext = serviceContext;
   }
   
   /**
    * This helps us build a URI for the symbol to send to finance.yahoo.com
    * @param symbol
    * @return
    * @throws Exception
    */
   private URI buildURI(String symbol) throws Exception{
       //http://download.finance.yahoo.com/d/quotes.csv?s=SORL&f=sl1d1t1c1ohgv&e=.csv
       URIBuilder builder = new URIBuilder();
       builder.setScheme("http").setHost("download.finance.yahoo.com").setPath("/d/quotes.csv")
           .setParameter("s", symbol)
           .setParameter("f", "sl1d1t1c1ohgv")
           .setParameter("e", ".csv");
       return builder.build();      
   }
   
   /**
    * Download Yahoo prices
    * @param httpclient the httpClient to connect with
    * @param symbol the symbol to query
    * @return
    * @throws Exception
    */
   private String downloadYahoo(HttpClient httpclient, String symbol) throws Exception{
       URI uri = buildURI(symbol);
       
       HttpGet httpget = new HttpGet(uri);
       HttpResponse response = httpclient.execute(httpget);
       HttpEntity entity = response.getEntity();
       StringBuilder sb = new StringBuilder();
       
       if (entity != null) {
           InputStream instream = entity.getContent();
           try {
               BufferedReader br = new BufferedReader(new InputStreamReader((instream)));

               String output;
               //System.out.println("Output from Server .... \n");
               while ((output = br.readLine()) != null) {
                   sb.append(output);
               }
           } finally {
               instream.close();
           }
       }
       
       return sb.length() > 0 ? sb.toString() : null;
   }
   
   @Override
   public String[] download() throws Exception {        
       Connection connection = null;
       Statement statement = null;
       List<String> symbolList = new LinkedList<String>();
       
       System.out.println("Downloading market: " + properties.getProperty(ServiceRunnable.RUN_MARKET));
       
       try{
    	   final String url = String.format(CONNECTION_STRING_FORMAT, serviceContext.getInitParameter(ServiceContext.SERVICE_HOME), properties.getProperty(DB_URL));
    	   final String username = properties.getProperty(DB_USERNAME);
    	   final String password = properties.getProperty(DB_PASSWORD);
           
    	   connection = DriverManager.getConnection(url, username, password);

           statement = connection.createStatement();
           String sql = String.format("select symbol from app.security where type = 'EQTY' and market in (%s)", properties.getProperty(ServiceRunnable.RUN_MARKET));
           final ResultSet resultSet = statement.executeQuery(sql);
           try{
               while(resultSet.next()){
                   String symbol = resultSet.getString("symbol");
                   symbolList.add(symbol);
               }
           }finally{
               if(resultSet!=null) {
            	   try{
            		   resultSet.close();
            	   }catch(SQLException ignore){}
               }
           }
       }finally{
           if(connection!=null) {
        	   try{
        		   connection.close();
        	   }catch(SQLException ignore){}
           }
       }
       
       HttpClient httpclient = new DefaultHttpClient();
       try{
           List<String> responseList = new LinkedList<String>();
           for(String symbol : symbolList){
               String response = downloadYahoo(httpclient, symbol);
               responseList.add(response);
           }
           return responseList.toArray(new String[0]);
       }finally{
           httpclient.getConnectionManager().shutdown();
       }
   }


   /**
    * This pattern allows us to clean some of that Yahoo data that is always quoted
    */
   private final static Pattern pattern = Pattern.compile("\"*");

   @Override
   public List<Security> clean(String[] data) throws Exception {

       List<Security> recordList = new LinkedList<Security>();
       for(String line : data){
           String[] field = line.split(",");
           Security record = new Security();
           try{

               Matcher m = pattern.matcher(field[0]);
               
               record.symbol = m.replaceAll("");
               record.high = Double.parseDouble(field[6]);

               m = pattern.matcher(field[2]);
               record.date = new SimpleDateFormat("M/dd/yyyy").parse(m.replaceAll(""));

               record.open = Double.parseDouble(field[5]);
               record.close = Double.parseDouble(field[1]);
               record.low = Double.parseDouble(field[7]);
               record.volume = Double.parseDouble(field[8]);
           }catch(NumberFormatException nfe){
               logger.log(Level.WARNING, String.format("Could not parse record '%s'; ignoring with exception '%s'", line, nfe.getMessage()));
               continue;
           }catch(ParseException pe){
        	   logger.log(Level.WARNING, String.format("Could not parse record '%s'; ignoring with exception '%s'", line, pe.getMessage()));
               continue;                
           }
           
           recordList.add(record);
           
       }
       return recordList;
   }

   @Override
   public void save(List<Security> data) throws Exception {
	   
       Connection connection = null;
       PreparedStatement statement = null;

       try{
    	   final String url = String.format(CONNECTION_STRING_FORMAT, serviceContext.getInitParameter(ServiceContext.SERVICE_HOME), properties.getProperty(DB_URL));
    	   final String username = properties.getProperty(DB_USERNAME);
    	   final String password = properties.getProperty(DB_PASSWORD);
           
    	   connection = DriverManager.getConnection(url, username, password);
           statement = connection.prepareStatement("insert into app.price values" +
        		   "(?,?,?,?,?,?,?)");

    	   for(final Security security : data){
    		   statement.setString(1, security.symbol);
    		   statement.setDate(2, new java.sql.Date(security.date.getTime()));
    		   statement.setDouble(3, security.open);
    		   statement.setDouble(4, security.low);
    		   statement.setDouble(5, security.high);
    		   statement.setDouble(6, security.close);
    		   statement.setDouble(7, security.volume);
    		   
    		   try{
    			   statement.execute();
    		   }catch (SQLException e){
    	            if(e.getSQLState().equals("23505")) // Derby has no concept of replace into table values (....) so we just ignore the duplicate record
    	                continue;
    	            else{
    	            	//logger.log(Level.SEVERE, String.format("Could not save record: %s", security), e);
    	            	throw e;
    	            }
    	        }
           }
    	   
       }finally{
    	   
           if(statement!=null) {
        	   try{
        		   statement.close();
        	   }catch(SQLException ignore){}
           }
    	   
           if(connection!=null) {
        	   try{
        		   connection.close();
        	   }catch(SQLException ignore){}
           }
       }	   
   }
   
}


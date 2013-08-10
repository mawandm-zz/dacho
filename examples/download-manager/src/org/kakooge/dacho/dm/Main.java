package org.kakooge.dacho.dm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.dm.model.Security;
import org.kakooge.dacho.dm.service.DataManagerService;


public class Main {
	
	private final static Logger logger = Logger.getLogger(Main.class.getName());
	
	private static Properties properties() throws IOException{
		InputStream is = new FileInputStream(System.getProperty("config"));
		final Properties properties = new Properties();
		properties.load(is);
		return properties;
	}
	
	private static void testConnection(final Properties properties) throws SQLException{

	       Connection connection = null;
	       PreparedStatement statement = null;
	       
	       final String DB_DRIVER = "db.driver";
	       final String DB_URL = "db.url";
	       final String DB_USERNAME = "db.username"; // fin_db_user
	       final String DB_PASSWORD = "db.password"; // 4nDbYza
	       final String SCHEDULE="schedule";

	       try{
	    	   final String url = String.format("jdbc:derby:%s;", properties.getProperty(DB_URL));
	    	   final String username = properties.getProperty(DB_USERNAME);
	    	   final String password = properties.getProperty(DB_PASSWORD);
	           
	    	   connection = DriverManager.getConnection(url, username, password);
	           statement = connection.prepareStatement("insert into app.price values" +
	        		   "(?,?,?,?,?,?,?)");

	           Security security = new Security();
	           security.open = 0d;
	           security.low = 0d;
	           security.high = 0d;
	           security.close = 0d;
	           security.volume = 0d;
	           
	           
    		   statement.setString(1, "WFR");
    		   statement.setDate(2, new java.sql.Date(new Date().getTime()));
    		   statement.setDouble(3, security.open);
    		   statement.setDouble(4, security.low);
    		   statement.setDouble(5, security.high);
    		   statement.setDouble(6, security.close);
    		   statement.setDouble(7, security.volume);
    		   
    		   try{
    			   statement.execute();
    		   }catch (SQLException e){
    	            e.printStackTrace();
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
	
	public static void main(String[] args) throws Exception{
		//final Properties prop = properties();
		//testConnection(prop);
		
		DataManagerService dataManagerService = new DataManagerService();
		dataManagerService.OnStart(logger, new ServiceContext(){

			@Override
			public String getInitParameter(String arg0) {
				if(arg0.equals("config"))
					return "config/dm.properties";
				if(arg0.equals(ServiceContext.SERVICE_HOME))
					return "/host/Users/mawandm/Documents/Projects/kiboel/dacho-sm/dist/services/ddm";
				if(arg0.equals("run.schedule"))
					return "'FTAS','FTAI'|*/5 * * * *\n" +
							"'IXIC','NYA'|*/5 * * * *";
				
				return null;
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
			
		});

		try {
			Thread.sleep(1000L * 60L * 10L);
		} catch (InterruptedException e) {
			;
		}
		
		dataManagerService.OnShutdown();
		
		//DownloadProcess<?, ?> downloadProcess = new YahooPriceDownloadProcess(prop);
		//downloadProcess.execute();
	}
}

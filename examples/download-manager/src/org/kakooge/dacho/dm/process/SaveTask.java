package org.kakooge.dacho.dm.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.kakooge.dacho.dm.model.Security;

/**
 * Save task. To be used with some kinda JDBC Connection pool
 * @author mawandm
 *
 */
public class SaveTask implements Runnable{

	private final PreparedStatement statement;
	private final Security security;
	private final String symbolString;
	
	private static final Logger logger = Logger.getLogger(Logger.class.getName());
	private static final boolean debug = System.getProperty("debug")!=null;
	
	public SaveTask(final PreparedStatement statement, final Security security){
		this.statement = statement;
		this.security = security;
		symbolString = (security!=null) ? Security.getSymbolString(security) : null;
	}
	
	private void save() throws Exception {
	   
	   if(security==null)
		   return;
   
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
	public void run() {
		try {
			save();
		} catch (Exception e) {
			logger.warning("Failed to save security: " + this.symbolString);
		}
	}
}

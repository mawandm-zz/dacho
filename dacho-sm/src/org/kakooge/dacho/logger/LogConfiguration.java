package org.kakooge.dacho.logger;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

import org.kakooge.dacho.loader.ServiceController;

/**
 * This is incomplete 
 * @author mawandm
 *
 */
public class LogConfiguration {
	
	LogManager manager = LogManager.getLogManager();
	
	private static Pattern pathPattern = Pattern.compile("^/|[a-z]?:");
	public void initialize(){
		String dachoHome = properties.getProperty(ServiceController.DACHO_HOME);
		String pattern = manager.getProperty("java.util.logging.FileHandler.pattern");
		if(!pathPattern.matcher(dachoHome).matches()){
			//- Assume relative path
			
		}
	}
	
	public void reinitialize() throws SecurityException, IOException{
		manager.readConfiguration();
	}
	
	public LogConfiguration(final Properties properties){
		this.properties = properties;
	}
	final Properties properties;
}

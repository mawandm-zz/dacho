package org.kakooge.dacho.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import org.kakooge.dacho.loader.ServiceController;

/**
 * Incomplete attempt to create an overriding FileHandler
 * @author mawandm
 *
 */
public class DachoLogFileHandler extends FileHandler{
	
	final private FileHandler handler;
	
	@Override
	public synchronized void flush() {
		handler.flush();
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		return handler.isLoggable(record);
	}

	@Override
	public void setEncoding(String encoding) throws SecurityException,
			UnsupportedEncodingException {
		handler.setEncoding(encoding);
	}

	public DachoLogFileHandler() throws IOException{
		handler = new FileHandler();
	}
    
    public DachoLogFileHandler(String pattern) throws SecurityException, IOException{
    	pattern = generatePattern(pattern);
    	handler = new FileHandler(pattern);
	}
    
    public DachoLogFileHandler(String pattern, boolean append) throws SecurityException, IOException{
    	pattern = generatePattern(pattern);
    	handler = new FileHandler(pattern, append);
	}
    
    public DachoLogFileHandler(String pattern, int limit, int count) throws SecurityException, IOException{
    	pattern = generatePattern(pattern);
    	handler = new FileHandler(pattern, limit, count);
    }
    
    public DachoLogFileHandler(String pattern, int limit, int count, boolean append) throws SecurityException, IOException{
    	pattern = generatePattern(pattern);
    	handler = new FileHandler(pattern, limit, count, append);
		
	}
	
    private String getDachoHome(){
    	/*
    	String className = getClass().getName();
    	URL url = getClass().getClassLoader().getResource(className.replace('.', '/') + ".class");
    	String fileName = url.getFile();
    	String dachoHome = fileName.substring(0, fileName.indexOf(className + ".class"));
    	return dachoHome.intern();
    	*/
    	return System.getProperty(ServiceController.DACHO_HOME);
    }
	
    private static Pattern pathPattern = Pattern.compile("^(/|([a-z]:))+.*", Pattern.CASE_INSENSITIVE);
	public String generatePattern(String pattern){
		String dachoHome = getDachoHome();
		if(!pathPattern.matcher(pattern).matches()){
			//- Assume relative path
			return dachoHome + pattern;
		}
		return pattern;
	}
}

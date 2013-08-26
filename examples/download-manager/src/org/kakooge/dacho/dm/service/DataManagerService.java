package org.kakooge.dacho.dm.service;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import org.kakooge.dacho.api.DSMException;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.dm.util.Utilities;


/**
 *
 * @author mawandm
 */
public class DataManagerService extends ServiceBase{

    private final static String name = DataManagerService.class.getSimpleName();
    private Properties properties = null;
    private volatile boolean initialized = false;
    private final Scheduler scheduler = new Scheduler();
    private Logger logger = null;
    private final static boolean debug = System.getProperty("debug")!=null;
    
    
    @Override
	public void OnPause() throws DSMException {
		// TODO Auto-generated method stub
		super.OnPause();
	}

	@Override
	public void OnResume() throws DSMException {
		// TODO Auto-generated method stub
		super.OnResume();
	}

	@Override
	public void OnShutdown() throws DSMException {
		// TODO Auto-generated method stub
		super.OnShutdown();
	}

	/**
	 * Generate properties from a properties file
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private Properties getProperties(final String fileName) throws IOException{
		final Properties props = new Properties();
		InputStream is = new FileInputStream(fileName);
		props.load(is);
		return props;
	}
	
	/**
	 * Generate a {@link Map} of market schedules
	 * @param schedule
	 * @return
	 */
	private Map<String, String> getSchedules(final String schedule){
		
		if(debug)
			logger.info("Getting schedules...");
		
		final Scanner scanner = new Scanner(schedule);
		final Map<String, String> scheduleMap = new HashMap<String, String>();
		while(scanner.hasNextLine()){
			String line = scanner.nextLine().trim();
			if(debug)
				logger.info(String.format("Getting schedules...%s", line));
			String[] schedule_parts = line.split("\\|");
			if(schedule_parts==null || schedule_parts.length!=2)
				continue;
			String[] market_parts = schedule_parts[0].split(",");
			if(market_parts==null || market_parts.length==0)
				continue;
			
			for(int idx=0; idx<market_parts.length; ++idx){
				if(debug)
					logger.info(String.format("Setting schedules...%s-%s", market_parts[idx], schedule_parts[1]));
				scheduleMap.put(market_parts[idx], schedule_parts[1]);
			}
		}
		return scheduleMap;
	}
	
	/**
	 * Initialize the service
	 * @param serviceContext
	 * @throws Exception
	 */
	private synchronized void init(final Logger logger, final ServiceContext serviceContext) throws Exception{
		if(!initialized){
			String fileName = serviceContext.getInitParameter(ServiceContext.SERVICE_HOME)
					+ File.separator +
					serviceContext.getInitParameter("config");
			
			if(debug)
				logger.info(String.format("Running with config file: %s", fileName));
			
			properties = getProperties(fileName);
			final Map<String, String> scheduleMap = getSchedules(serviceContext.getInitParameter(YahooPriceDownloadProcess.RUN_SCHEDULE));
			for(final Map.Entry<String, String> entry : scheduleMap.entrySet()){
				final Properties props = new Properties();
				
				// Clone the properties and set the run.market. Then pass the cloned properties into the scheduled task
				Utilities.copyProperties(properties, props);
				props.setProperty(YahooPriceDownloadProcess.RUN_MARKET, entry.getKey());
				Runnable serviceRunnable = new YahooPriceDownloadProcess(logger, serviceContext, props);
				
				if(debug)
					logger.info(String.format("Scheduling %s downloads with schedule %s", entry.getKey(), entry.getValue()));
				
				scheduler.schedule(entry.getValue(), serviceRunnable);
			}
			initialized = true;
		}
	}
	
	private Properties testProperties(){
		
		InputStream is = null;
		final String messageFileName = "org/kakooge/dacho/dm/util/test.properties";
		try{
			//Configuration config = new PropertiesConfiguration("usergui.properties");
			is = getClass().getClassLoader().getResourceAsStream(messageFileName);
			Properties MESSAGES = new Properties();
			MESSAGES.load(is);
			return MESSAGES;
		}catch(Exception e){
			throw new Error(String.format("Could not load the '%s' resource", messageFileName), e);
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(Exception e){}
			}
		}
	}
	
	@Override
	public void OnStart(Logger logger, ServiceContext serviceContext) throws DSMException {
		this.logger = logger;
		logger.info(String.format("Executing OnStart in class '%s'", getClass().getName()));
		
		Properties prop = testProperties();
		logger.info("TEST Property" + prop.getProperty("test"));
		
		Enumeration<String> enumm = serviceContext.getInitParameterNames();
		if(enumm!=null){
			while(enumm.hasMoreElements()){
				String name = enumm.nextElement();
				logger.info(String.format("%s=%s", name, serviceContext.getInitParameter(name)));
			}
		}
		
        try {
			init(logger, serviceContext);
		} catch (Exception e) {
			throw new DSMException("Error when executing init method", e);
		}
		scheduler.start();
    }

    @Override
    public void OnStop() throws DSMException{
    	if(debug)
    		logger.info(String.format("Executing OnStop in class '%s'", getClass().getName()));
        scheduler.stop();
        
        if(debug)
        	logger.info(String.format("Service '%s' stoped", getClass().getName()));
    }
    
}

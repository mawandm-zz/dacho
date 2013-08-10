package org.kakooge.dacho.dm.service;


import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.dm.process.DownloadProcess;
import org.kakooge.dacho.dm.process.YahooPriceDownloadProcess;


/**
 *
 * @author mawandm
 */
public class ServiceRunnable implements Runnable{

    private volatile boolean alive;
    private String name;
    private DownloadProcess<?, ?> downloadProcess = null;
    
    /**
     * 
     */
    final public static String RUN_MARKET="run.market";
    
    /**
     * This is the run schedule for each of the services
     */
    final public static String RUN_SCHEDULE = "run.schedule";
    private Logger logger = Logger.getLogger(ServiceRunnable.class.getName());
    
    public ServiceRunnable(final Logger logger, final ServiceContext serviceContext, Properties properties, String name) throws ClassNotFoundException{
        this.name = name;
        alive = true;
        downloadProcess = new YahooPriceDownloadProcess(logger, serviceContext, properties);
    }

    public boolean isAlive() {
        return alive;
    }

    public synchronized void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getName() {
        return name;
    }
    
    private void downloadPrice(DownloadProcess<?, ?> downloadProcess) throws Exception{
        downloadProcess.execute();
    }
    
    @Override
    public void run() {
    	try {
			downloadPrice(downloadProcess);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error when executing download process", e);
		}
    }
    
}


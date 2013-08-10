package org.kakooge.dacho.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapDaemon {

    /**
     * If appMode is set, then we have to join in the initServices method to wait for all threads to stop.
     * If appMode mode isn't set, then we have been started by an external process e.g. a daemon/windows service
     * and it will be responsible to wait for us and signal when are done via destroServices 
     */

    final private static ServiceController serviceManagerBootstrap = new ServiceController(System.getProperties());
    
    final private static Logger logger = Logger.getLogger(BootstrapDaemon.class.getName());
    
    /*
    private static void redirectOutput(){
    	
    }
    */
    /**
     * Add a VM shutdown hook
     */
    
    private static void attachShutdownCleanup(){
    	Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				try {
					destroyServiceManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
    	});
    }
    
    
    /**
     * To be called by external JVM controller which would have been started by say the Windows ServiceManager
     * @throws Exception
     */
    public static void initServiceManager(){
    	if(serviceManagerBootstrap == null){
    		logger.log(Level.WARNING, "Service manager instance not found in initServiceManage");
    		return;
    	}
    	try {
    		attachShutdownCleanup();
			serviceManagerBootstrap.initServices();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not start service manager", e);
		}
    }
    
    /**
     * The opposite of {@link #initServiceManager()}
     * @throws Exception
     */
    public static void destroyServiceManager(){
    	if(serviceManagerBootstrap == null){
    		logger.log(Level.WARNING, "Service manager instance not found in destroyServiceManage");
    		return;
    	}
    	try {
			serviceManagerBootstrap.destroyServices();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not destroy service manager", e);
		}
    }
}

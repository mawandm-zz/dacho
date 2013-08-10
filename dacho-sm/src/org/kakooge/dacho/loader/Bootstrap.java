package org.kakooge.dacho.loader;


/**
 * Bootstrap services
 * @author mawandm
 *
 */
public class Bootstrap {	
	
    /**
     * Main entry point
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
    	
    	BootstrapDaemon.initServiceManager();
    	Thread.sleep(Long.MAX_VALUE);
    	BootstrapDaemon.destroyServiceManager();
    	
    }
}

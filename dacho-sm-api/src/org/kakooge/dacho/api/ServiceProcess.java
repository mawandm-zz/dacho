package org.kakooge.dacho.api;

import java.util.logging.Logger;

/**
 * The main service contract between the service manager and any service to be 
 * contained and managed by the service manager.
 * @author mawandm
 */
interface ServiceProcess{
	
	/**
	 * The service manager executes this method for any service registered. 
	 * This method is expected to perform only initialization of the service 
	 * and return immediately after. Typically you'd be starting a worker 
	 * thread that would handle all service specific tasks
	 * @param logger a service specific {@link Logger} supplied by the services manager
	 * @param serviceContext a {@link ServiceContext} instance with 
	 * information about the service's environment
	 * @throws Exception if any problem is encountered during start up of this method
	 */
    void OnStart(Logger logger, ServiceContext serviceContext) throws DSMException;
    
    /**
     * This method is executed by the service manager to stop the service. 
     * The implementation is expected to tear down the service in this method. 
     * For example if a thread was started in the {@link #OnStart(Logger, ServiceContext)} 
     * method, that thread could be stopped in this method.
     * @throws Exception
     */
    void OnStop() throws DSMException;
    
    /**
     * The framework gives you an opportunity to temporarily 
     * stop the service by executing this method.
     * @throws Exception
     */
    void OnPause() throws DSMException;
    
    /**
     * Use this method to resume the service after it has been paused
     * @throws Exception
     */
    void OnResume() throws DSMException;
    
    /**
     * This method is intended to implement complete tear down of the service
     * @throws Exception
     */
    void OnShutdown() throws DSMException;
}

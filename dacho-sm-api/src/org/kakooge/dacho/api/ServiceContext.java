package org.kakooge.dacho.api;

import java.util.Enumeration;

/**
 * The service context helps a specific service gain access to specific resources of the service containers. For example, 
 * the service, using the {@link #getInitParameter(String)}, the service is able to gain access to the initialization
 * parameters in the {@code service.xml} service descriptor.
 * <p>
 * If you are familiar with the servlet API, this is similar to the {@code ServletContext} and truly this class was
 * inspired by the {@code ServletContext}
 * </p>
 * @author mawandm
 *
 */
public interface ServiceContext {
	
    /**
     * A specific service's home folder
     */
    public final static String SERVICE_HOME = "service.home";
	
	/**
	 * This is the name of the service
	 * @return
	 */
	String getName();
	
	/**
	 * Get a specific initialization parameter
	 * @param name the name of the parameter
	 * @return the value of the parameter
	 */
	String getInitParameter(String name);
	
	/**
	 * Get an enumeration of all parameter names
	 * @return an enumuration of the names
	 */
	Enumeration<String> getInitParameterNames();
}

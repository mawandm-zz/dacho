package org.kakooge.dacho.loader;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.kakooge.dacho.api.DSMException;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;

/**
 * Service bootstrapping service. 
 * @author mawandm
 */
public class ServiceBootstrap extends ServiceBase{

	//private ServiceContext serviceContext;
    private final ClassLoader classLoader;
    private final String serviceClassName;
    private String statusMessage;
    private final Object serviceInstance;
    public final static boolean debug = System.getProperty("debug")!=null;
    
    public enum ServiceMethod{
        ONSTART, ONSTOP, ONPAUSE, ONRESUME, ONSHUTDOWN
    }

    /**
     * Constructs a service bootstrap object
     * @param classLoader           The <code>ServiceClassLoader</code> used to load the service class
     * @param serviceClassName  The name of the service class for this service
     * @param args                      The arguments passed to the service's {@link OnStart} method
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public ServiceBootstrap(ClassLoader classLoader, String serviceClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
        this.classLoader = classLoader;
        this.serviceClassName = serviceClassName;
        serviceInstance = instanciateServiceClass();
    }
    
    private void messagePrint(String message){
        if(debug){                
            System.out.println(message);
        }        
    }

    public String getStatusMessage() {
        return statusMessage;
    }
	
    //@Override
    private void invokeMethod(ServiceMethod invokeMethod, List<?> args) throws DSMException{
        
        String methodName;
        
        switch(invokeMethod){
            case ONSTART:
                methodName = "OnStart";
                break;
            case ONSTOP:
                methodName = "OnStop";
                break;
            case ONPAUSE:
                methodName = "OnPause";
                break;
            case ONRESUME:
                methodName = "OnResume";
                break;
            case ONSHUTDOWN:
                methodName = "OnShutdown";
                break;
            default:
                throw new DSMException(String.format("Invalid method '%s'", invokeMethod.toString()));
        }
        
        
    	statusMessage = String.format("invoking %s()", methodName);
    	messagePrint(statusMessage);
        
        try{
        	boolean executed = false;
            //find the method OnStart() and execute it
            Method[] allMethods = serviceInstance.getClass().getDeclaredMethods();
            for (Method m : allMethods) {
                String mname = m.getName();
                if (!mname.equals(methodName)) {
                    continue;
                }

                try {
                    m.setAccessible(true);
                    
                    Class<?>[] mtype = m.getParameterTypes();
                    if(debug){
                    	for(Class<?> type : mtype)
                    		System.out.println(type.getName());
                    }
                    
                    if(args!=null)
                    	m.invoke(serviceInstance, args.toArray());
                    else
                    	m.invoke(serviceInstance);
                    
                // Handle any exceptions thrown by method to be invoked.
                } catch (InvocationTargetException x) {
                    throw new DSMException(String.format("invocation of %s failed", mname), x);
                }
            }
            
            if(!executed){
            	throw new DSMException("Did not find the requested method to execute");
            }
        }catch(Exception e){
            statusMessage = String.format("Failied to initialize service class '%s' due to exception '%s", serviceClassName, e.getMessage());
            if(debug){
                e.printStackTrace();
            }
            throw new DSMException(statusMessage, e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) //List and ArrayList
    @Override
	public void OnStart(Logger logger, ServiceContext serviceContext) throws DSMException {
        
		List args = new ArrayList();
        args.add(Logger.getLogger(serviceClassName));
        args.add(serviceContext);
        this.invokeMethod(ServiceMethod.ONSTART, args);
	}

	@Override
	public void OnStop() throws DSMException {
		this.invokeMethod(ServiceMethod.ONSTOP, null);
	}

	@Override
	public void OnPause() throws DSMException {
		throw new DSMException("Not implemented");
	}

	@Override
	public void OnResume() throws DSMException {
		throw new DSMException("Not implemented");
	}

	@Override
	public void OnShutdown() throws DSMException {
		throw new DSMException("Not implemented");
	}

    
	private Object instanciateServiceClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		//Create an instance of the service class
        Class<?> clazz = classLoader.loadClass(serviceClassName);

    	statusMessage = String.format("Clazz '%s' classloader is '%s'", clazz.getName(), clazz.getClassLoader().getClass().getName());
    	messagePrint(statusMessage);

        Object instance = clazz.newInstance();
        return instance;
	}
    
}


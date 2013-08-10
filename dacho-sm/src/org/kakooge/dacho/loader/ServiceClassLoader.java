package org.kakooge.dacho.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class loader is responsible for class loading within each service context. A new ServiceClassLoader is created
 * for each service application context.
 * <p>
 * The class loading structure is as follows;
 * <ol>
 * <li>First delegate the class loading to the system class loader. This ensures that if the class requested is a system class then it will be loaded by the system class loader</li>
 * <li>Then if not found, delegate to the base class {@link URLClassLoader} to search for the path from the service class path</li>
 * <li>If not found, lastly delegate to the parent class loader, which search for the class from the dacho service manager classpath</li>
 * <li>If not found, then... unlucky, {@link ClassNotFoundException} is thrown</li>
 * </ol>
 * </p>
 * @author mawandm
 */
public class ServiceClassLoader extends URLClassLoader{
    
    final private boolean debug = false /*System.getProperty("debug")!=null*/;
    final protected ClassLoader systemClassLoader;
    final private static Logger logger = Logger.getLogger(ServiceClassLoader.class.getName());
    
    public ServiceClassLoader(URL[] urls, ClassLoader parent) throws IOException{
        super(urls, parent);
        systemClassLoader = getSystemClassLoader();
        if(debug)
        	for(final URL url : urls)
        		logger.info(String.format("Classpath: %s", url));
    }
    
    /**
     * Inspired by the Apache Tomcat WebappClassloader.java
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException 
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
          // First, check if the class has already been loaded
        Class<?> klass = findLoadedClass(name);
        
        // This ensures that system classes are not overridden
        if (klass == null) {
            try {
                klass = systemClassLoader.loadClass(name);
                if (klass != null) {
                    if(debug){
                        logger.log(Level.INFO, String.format("%s Loaded by SystemCL", getClass().getName()));
                    }
                }
            } catch (ClassNotFoundException e) {
                klass = null;
            }
        }
        
        // If an allowed class, load it locally first
        if (klass == null) {
            try {
                // If still not found, then invoke findClass in order to find the class.
                klass = findClass(name);
            } catch (ClassNotFoundException e) {
                klass = null;
            }
        }
        
        // otherwise, and only if we have a parent, delegate to our parent
        if (klass == null) {
            ClassLoader parent = getParent();
            if (parent != null) {
                klass = parent.loadClass(name);
            } else {
            	logger.log(Level.SEVERE, String.format("Failed to load class %s", name));
                throw new ClassNotFoundException(name);
            }
        }
        
        if (resolve && (klass != null)) {
            resolveClass(klass);
        }
        return klass;
    }

    public InputStream getResourceAsStream(String name) {
        if ((name != null) && name.startsWith("/")) {
            name = name.substring(1);
        }
        return super.getResourceAsStream(name);
    }
}

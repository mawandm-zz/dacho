package org.kakooge.dacho.loader;

import static org.kakooge.dacho.api.ServiceContext.SERVICE_HOME;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.kakooge.dacho.api.DSMException;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.model.Service;
import org.kakooge.dacho.util.IOUtils;
import org.kakooge.dacho.util.StringUtil;
import org.kakooge.dacho.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides a unified route into the service manager. It
 * <ol>
 * <li>Initializes the whole service manager</li>
 * <li>Starts the rudimentary service directory watcher</li>
 * <li>Destroys the service manager</li>
 * </ol>
 * @author mawandm
 */
public class ServiceController {

    /**
     * Map of ServiceName to a list of all URLs belonging to that Service's classpath
     */
    //private final Map<String, List<URL>> serviceURLMap = new Hashtable<String, List<URL>>();
    
    /**
     * Map of Servicename to the ServiceProcess
     */
    private final Map<Service, ServiceBootstrap> serviceMap = new Hashtable<Service, ServiceBootstrap>();

    /**
     * Map of all properties needed to bootstrap service container
     */
    private final Properties properties;
    
    /**
     * This is the folder containing the derby database
     */
    public final static String SERVICE_DBHOME = "service.db.home";
    
    /**
     * If using XML configuration, use this file
     */
    public final static String XML_CONFIG = "xml.config";
    
    /**
     * The dacho application path
     */
    public final static String DACHO_HOME = "dacho.home";
    
    /**
     * The class logger
     */
    private final static Logger logger = Logger.getLogger(ServiceController.class.getName());
    
    /**
     * Thread to watch for new service deployments
     */
    private Thread serviceWatcherThread = null;
    
    private File servicesBaseFolder = null;
    
    private boolean debug = System.getProperty("debug")!=null;
    
    /**
     * Construct a ServiceController object
     * @param properties
     */
    public ServiceController(final Properties properties){
    	if(properties == null)
    		throw new IllegalArgumentException("Invalid argument, properties my be provided");
    	
    	this.properties = properties;

    	/*Is the SERVICE_DBHOME valid?*/
    	if(!StringUtil.empty(properties.getProperty(SERVICE_DBHOME))){
    		final File folder = new File(properties.getProperty(SERVICE_DBHOME));
    		if(!folder.exists() || !folder.isDirectory())
    			throw new IllegalArgumentException(String.format("The specified property %s = %s is missing or is not a valid folder", SERVICE_DBHOME, properties.getProperty(SERVICE_DBHOME)));
    	}
    	
    	/*Dacho home checks*/
    	if(StringUtil.empty(properties.getProperty(DACHO_HOME))){
    		throw new IllegalArgumentException("-Ddacho.home=<installation home> is missing");
    	}else{
    		final File dacho_home = new File(properties.getProperty(DACHO_HOME));
    		if(!dacho_home.exists() || !dacho_home.isDirectory())
    			throw new IllegalArgumentException("-Ddacho.home=<installation home> is not a valid directory");
    		
        	/*Is the XML_CONFIG valid?*/
    		properties.setProperty(XML_CONFIG, String.format("%s/%s", properties.getProperty(DACHO_HOME), "config/dacho.xml"));
    		final File folder = new File(properties.getProperty(XML_CONFIG));
    		if(!folder.exists() && !folder.isDirectory())
    			throw new IllegalArgumentException(String.format("The specified property %s = %s is missing or is not a valid file", XML_CONFIG, properties.getProperty(XML_CONFIG)));
    	}
    }
    
    public Map<Service, ServiceBootstrap> getServiceMap(){
    	return new Hashtable<Service, ServiceBootstrap>(serviceMap);
    }
    
    public File getServicesBaseFolder(){
    	return servicesBaseFolder;
    }
    
    /**
     * Starts teh service watcher thread
     */
    private void startServiceWatcherThread(){
    	if(serviceWatcherThread==null)
    		serviceWatcherThread = new ServiceWatcher(this);
    	serviceWatcherThread.start();
    }
    
    private void stopServiceWatcherThread(){
    	if(serviceWatcherThread!=null){
    		serviceWatcherThread.interrupt();
    		try {
				serviceWatcherThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Initialize all services available
     * @throws Exception
     */
    public void initServices() throws Exception{
        Set<Service> serviceSet = null;
        if(!StringUtil.empty(properties.getProperty(XML_CONFIG)))
        	serviceSet = loadServicesXML();
        else if(!StringUtil.empty(properties.getProperty(SERVICE_DBHOME)))
        	serviceSet = loadServicesDB();
        else
        	throw new Exception(String.format("Invalid configuration setting. Use '%s' or '%s", XML_CONFIG, SERVICE_DBHOME));
        
        startServices(serviceSet);
        
        startServiceWatcherThread();
        //attachShutdownCleanup();
    }
    
    /**
     * Destroy services waiting for all to complete
     * @throws Exception
     */
    public synchronized void destroyServices() throws Exception{
		
		stopServiceWatcherThread();

		final Thread[] shutdownThreads = new Thread[serviceMap.size()];
		int threadIdx = 0;
		/*
		 * Iterate through all threads stopping each of them
		 */
		for(final Service service : serviceMap.keySet()){
			/*
			 * Start a new thread to handle the stopping of each service
			 */
			shutdownThreads[threadIdx] = new Thread(){
				@Override
				public void run() {
					try {
						if(ServiceController.this.debug)
							logger.info(String.format("Stopping service '%s'", service.getServiceClass()));
						ServiceController.this.stop(service);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			shutdownThreads[threadIdx].start();
			++threadIdx;
		}
		
        // Wait on all all services to stop and then sleep the current thread
        for(Thread shutdownThread : shutdownThreads){
        	try {
				shutdownThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        serviceMap.clear();
        
    }
    
    /**
     * Load service data from the data store
     * @return  Map of ServiceName and class of the SericeProcess within that ServiceName's classpath
     * @throws SQLException If reading from the data store fails
     * @throws DachoException If the read classpath is an invalid file location
     * @throws MalformedURLException If a URL object could not be derived from the read classpath
     * @throws XPathExpressionException 
     */
    
    private Set<Service> loadServicesDB() throws SQLException, DSMException, MalformedURLException, XPathExpressionException{
        
        final Set<Service> serviceSet = new HashSet<Service>();
  /*      
        try{
            //Class.forName("org.apache.derby.jdbc.ClientDriver");
        	Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        	
        }catch(ClassNotFoundException e){
            throw new Error("Error loading return service;drivers");
        }
        
        Connection connection = null; 
        ResultSet result = null;
        try{
        	
        	final String url = "jdbc:derby:%s;";
        	//url = "jdbc:derby://localhost:1527/jsc-db";
        	connection = DriverManager.getConnection(String.format(url, properties.getProperty(SERVICE_DBHOME)));
        	result = connection.createStatement().executeQuery("select id, name, description, args, class, filepath from classpath, service where serviceid = id");
        	
            while(result.next()){
                int id = result.getInt("id");
                String name = result.getString("name");
                String clazz = result.getString("class");
                String desc = result.getString("description");
                String args = result.getString("args");

                if(!serviceURLMap.containsKey(name))
                    serviceURLMap.put(name, new ArrayList<URL>());

                String filepath = result.getString("filepath");
                File pathFile = new File(filepath);
                if(!pathFile.exists())
                    throw new DSMException(String.format("File '%s' specified in classpath does not exist for service '%s'", filepath, name));
                
                serviceURLMap.get(name).add(pathFile.toURI().toURL());
                Map<String, String> paramMap = contextParameters(XMLUtil.getXmlDocument(args));
                final ServiceContext serviceContext = getServiceContext(name, paramMap);
                
                //assert serviceContext.getInitParameterNames()!=null;
                
                Service service = new Service(id, name, desc, clazz, serviceContext, null);
                if(!serviceSet.contains(service))
                    serviceSet.add(service);
            }
        }finally{
            try{result.close();}catch(Exception e){} // SQLException && NullPointerException
            try{connection.close();}catch(Exception e){} // SQLException && NullPointerException
        }
*/
       return serviceSet;
       
    }
    
    /**
     * Get the service context
     * @param name
     * @param parameterMap
     * @return
     */
    private ServiceContext getServiceContext(final String name, final Map<String, String> parameterMap){
    	final DachoServiceContext serviceContext = new DachoServiceContext();
    	serviceContext.setName(name);
    	serviceContext.setParameterMap(parameterMap);
    	return serviceContext;
    }
    
    /**
     * Read the context parameters into a map of key=value pairs
     * @param serviceDocument
     * @return
     * @throws XPathExpressionException
     */
    private Map<String, String> contextParameters(final Document serviceDocument) throws XPathExpressionException{
    	final NodeList paramNameNodeList = XMLUtil.evaluateXPath(serviceDocument, "/service/class/params/param/name", XPathConstants.NODESET);
    	final NodeList paramValueNodeList = XMLUtil.evaluateXPath(serviceDocument, "/service/class/params/param/value", XPathConstants.NODESET);
    	Map<String, String> paramMap = new HashMap<String, String>();
    	for(int jdx=0; jdx<paramNameNodeList.getLength(); ++jdx){
    		String pavamName = paramNameNodeList.item(jdx).getTextContent();
    		String paramValue = paramValueNodeList.item(jdx).getTextContent();
    		paramMap.put(pavamName, paramValue);
    	}
    	return paramMap;
    }
    
    /**
     * Deploy any undeployed DARs
     */
    private void deployDAR(){
    	for(final File file : servicesBaseFolder.listFiles()){
			if(!file.isDirectory()){
				if(file.getName().endsWith(".dar")){

					final String name = IOUtils.stripExtension(file);
					final File directory = new File(file.getParentFile(), name);
					if(!directory.exists()){
						try {
							IOUtils.deploy(file);
						} catch (IOException e) {
							//- Log something here
							logger.log(Level.SEVERE, String.format("Failed to deploy %s", name, e));
						}
					}else{
						/*
						 * if the directory.lastModified() < file.lastModified()
						 *   then we redeploy the .dar file by unzipping into the existing folder
						 */
					}
				}
			}
    	}
    }
    
    /**
     * Load service data from the XML data store
     * @return  Map of ServiceName and class of the SericeProcess within that ServiceName's classpath
     * @throws SQLException If reading from the data store fails
     * @throws IOException 
     * @throws XPathExpressionException 
     * @throws DachoException If the read classpath is an invalid file location
     */
    private Set<Service> loadServicesXML() throws SQLException, DSMException, IOException, XPathExpressionException{
        
    	final String xmlConfig = properties.getProperty(XML_CONFIG);
    	
    	final Document configXmlDocument = XMLUtil.getXmlDocument(new File(xmlConfig));
    	
        final Set<Service> serviceSet = new HashSet<Service>();
        
        final Node serviceHomeNode = XMLUtil.evaluateXPath(configXmlDocument, "/dacho/service", XPathConstants.NODE);
        if(serviceHomeNode==null)
        	throw new DSMException("Could not find service home node");
       
        //- Validate the service home provided
        
        final String serviceHome = serviceHomeNode.getTextContent();
        if(StringUtil.empty(serviceHome))
        	throw new DSMException("Invalid service home specified cannot be empty");
        
        //- Check as if a full path given
        servicesBaseFolder = new File(serviceHome.trim());
    	if(!servicesBaseFolder.exists() || !servicesBaseFolder.isDirectory()){
    		//- If not check as if a relative path is given
    		servicesBaseFolder = new File(properties.getProperty(DACHO_HOME), serviceHome.trim());
    		if(!servicesBaseFolder.exists() || !servicesBaseFolder.isDirectory()){	
    			throw new DSMException(String.format("Invalid service home '%s' specified must be a valid directory", serviceHome));
    		}
    	}
    	
    	// Deploy DARs
    	deployDAR();
        
        //- Get the serviceFiles
        final File[] serviceFiles = servicesBaseFolder.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {				
				return (file.isDirectory() && new File(file, "service.xml").exists());
			}
        });
        
        //- Create the service class models
        for(int idx = 0; idx<serviceFiles.length; ++idx){
        	final File serviceFile = new File(serviceFiles[idx], "service.xml");

        	final Service service = makeService(serviceFile);
        	
        	//serviceURLMap.get(name).add(pathFile.toURI().toURL());
        	serviceSet.add(service);
        }
        return serviceSet;
    }
   

    /**
     * Load service data from the XML descriptor
     * @return  Map of ServiceName and class of the SericeProcess within that ServiceName's classpath
     * @throws SQLException If reading from the data store fails
     * @throws IOException 
     * @throws XPathExpressionException 
     * @throws DachoException If the read classpath is an invalid file location
     */
    public Service makeService(final File descriptorFile) throws SQLException, DSMException, IOException, XPathExpressionException{
                
        //- Create the service class models
    	if(!descriptorFile.exists()){
    		//logger.log(Level.SEVERE, String.format("Failed to load service in '%s'. service.xml file not found", serviceFiles[idx].getCanonicalPath()));
    		return null;
    	}
    	
    	logger.info(String.format("Launching service from descriptor '%s'", descriptorFile.getCanonicalPath()));
    	
    	final Document serviceDocument = XMLUtil.getXmlDocument(descriptorFile);
    	//- Servicename
    	Node node = XMLUtil.evaluateXPath(serviceDocument, "/service/name", XPathConstants.NODE);
    	String name = node.getTextContent();
    	
    	//- Class
    	node = XMLUtil.evaluateXPath(serviceDocument, "/service/class/name", XPathConstants.NODE);
    	String clazz = node.getTextContent();
    	
    	//- Get the service context parameters
    	final Map<String, String> paramMap = contextParameters(serviceDocument);

        //- Set the service home
        paramMap.put(SERVICE_HOME, descriptorFile.getParent());
        
        //- Generate a service context
        final ServiceContext serviceContext = getServiceContext(name, paramMap);
        assert serviceContext.getInitParameterNames()!=null;
        
        List<URL> classpathList = new ArrayList<URL>();
        
    	//- Classpath
    	final NodeList nodeList = XMLUtil.evaluateXPath(serviceDocument, "/service/classpath/entry", XPathConstants.NODESET);
    	for(int jdx=0; jdx<nodeList.getLength(); ++jdx){
    		final Node entryNode = nodeList.item(jdx);
    		if(entryNode==null)
    			continue;
    		
    		String entry = entryNode.getTextContent();
    		if(StringUtil.empty(entry))
    			continue;
    		
            File pathFile = new File(descriptorFile.getParentFile(), entry.trim());
            if(!pathFile.exists())
                throw new DSMException(String.format("Classpath entry file '%s' classpath does not exist for service '%s'", pathFile.getCanonicalPath(), name));

            classpathList.add(pathFile.toURI().toURL());
            
    	}
    	    	
        //- Create the service
    	return new Service(0, name, name, clazz, serviceContext, classpathList);

    }
    
    
    /**
     * Start all services
     * @param serviceSet
     * @throws IOException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    private void startServices(Set<Service> serviceSet) throws IOException, InstantiationException, ClassNotFoundException, IllegalAccessException, InterruptedException{
        
    	// 27/07/2013 Start services in sequence
    	
        // Start the services
        for(final Service service : serviceSet){
			try {
				ServiceController.this.start(service);
				
			} catch (Exception e) {
				String message = String.format("Failed to initialize service '%s'", service.getServiceClass());
				logger.log(Level.SEVERE, message, e);
			}
        }
    }

    /**
     * Initialize the supplied service by assigning a collecting the service class path, initializing the service class loader
     * and caching the service bootstrapper
     * @param service the service to initialize
     * @throws Exception
     */
    private void initService(Service service) throws Exception{
   	 
    	if(serviceMap.containsKey(service))
    		return;
    	
        //- Get this service's classpath URLs
        final URL[] urls = service.getClasspath().toArray(new URL[0]);
        
        //- Initialize a ClassLoader for this service
        final ServiceClassLoader classLoader = new ServiceClassLoader(urls, getClass().getClassLoader());
        
        ServiceBootstrap serviceBootStrap = new ServiceBootstrap(classLoader, service.getServiceClass());
        //DSMLogger logger = new DSMLogger(service);
                    
        assert service.getServiceContext()!=null;
        if(service.getServiceContext()==null)
        	throw new InstantiationException("ServiceContext is null");
        
        serviceMap.put(service, serviceBootStrap);
    }

    
    /**
     * Starts the give {@link Service}
     * @param service the service to be started
     * @throws Exception if at any point during service start, an exception is thrown
     */
    public void start(Service service) throws Exception{
    	// Get and run the service
    	if(serviceMap.containsKey(service))
    		return;
    	// Initialize the service
    	initService(service);
    	
    	// Start the service
    	ServiceBootstrap serviceBootstrap = serviceMap.get(service);
    	serviceBootstrap.OnStart(Logger.getLogger(service.getServiceClass()), service.getServiceContext());
    }

    /**
     * Stop the given {@link Service} by calling {@link ServiceBase#OnStop()} of that service class
     * @param service the service to be stopped
     * @throws Exception if at any point the process fails
     */
    public void stop(Service service) throws Exception{
    	//- Get the service bootstrap instance of this service
        final ServiceBootstrap serviceBootstrap = serviceMap.get(service);
        if(serviceBootstrap!=null){
        	serviceBootstrap.OnStop();
	        serviceMap.remove(service);
        }
    }
      
    /**
     * This is just test code
     * @throws Exception
     */
    private static void tests() throws Exception{
       
        URL[] urls = new URL[1];
        File file = new File("/home/mawandm/Documents/Projects/jsc-tests/dist/jsc-tests.jar");
        urls[0] = file.toURI().toURL();
        ClassLoader classLoader = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        
        Class<?> clazz = classLoader.loadClass("org.kiboel.asm.jsc.loader.tests.TestClass");
        System.out.println(String.format("Clazz '%s' classloader is '%s'", clazz.getName(), clazz.getClassLoader().getClass().getName()));
        Object instance = clazz.newInstance();
        
    }
    
}

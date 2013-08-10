package org.kakooge.dacho.loader;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.model.Service;
import org.kakooge.dacho.util.IOUtils;

/**
 * In July 2013
 * Currently only accept folder with a service.xml file in it. Later, we can accept
 * zipped files as well. In which case we'd have to extract them. The zipped files would
 * ideally end with .dar (for Dacho Archive)
 * 
 * In August 2013
 * Changed to only accept files ending in .dar
 * @author mawandm
 */
public final class ServiceWatcher extends Thread{

	/**
	 * This helps us remember when a specific file was previously last modified
	 * @author mawandm
	 *
	 */
	private static class FileProperties <T>{
		public final T size;
		public final T modified;
		public FileProperties(final T size, final T modified){
			this.size = size;
			this.modified = modified;
		}
	}
	
	// Pointer to the ServiceController
	final private ServiceController serviceController;
	
	// Cache for all the previous last modified times
	final private Map<File, FileProperties<Long>> cachedPropertiesMap = new HashMap<File, FileProperties<Long>>();
	
	/**
	 * Create a new instance
	 * @param folder
	 * @param serviceController
	 */
	public ServiceWatcher(ServiceController serviceController){
		this.serviceController = serviceController;
		initialize();
	}
	
	/**
	 * This helps us determine which services have already been started so that by the time the ServiceWatcher
	 * starts there is no conflict otherwise the ServiceWatcher might end up starting services that
	 * are already being started by the ServiceController
	 */
	
	private void initialize(){
		final Map<Service, ServiceBootstrap> serviceMap = serviceController.getServiceMap();
		for(final Service service : serviceMap.keySet()){
			final String serviceHome = service.getServiceContext().getInitParameter(ServiceContext.SERVICE_HOME);
			final File serviceHomeFile = new File(serviceHome);
			if(!serviceHomeFile.exists())
				continue;
			final File serviceDeployFile = new File(serviceHomeFile.getParentFile(), 
					IOUtils.stripExtension(serviceHomeFile) + ".dar");
			if(!serviceDeployFile.exists())
				continue;
			final FileProperties<Long> props = new FileProperties<Long>(serviceDeployFile.length(), serviceDeployFile.lastModified());
			cachedPropertiesMap.put(serviceDeployFile, props);
		}
	}
	
	/**
	 * Start the service in the supplied dar file by
	 * <pre>
	 * 1. Extracting the dar file into a unique folder withing the services folder
	 * 2. Making a service from the the service.xml service descriptor
	 * 3. Starting the service
	 * </pre>
	 * @param darFile
	 * @throws Exception
	 */
	private void startService(File darFile) throws Exception{
		if(!IOUtils.complete(darFile))
			throw new Exception("Could not determine completeness in time");
		
		final File deployFolder = IOUtils.deploy(darFile);
		final Service service = serviceController.makeService(new File(deployFolder, "service.xml"));
		serviceController.start(service);
	}
	
	/**
	 * Watch process
	 */
	private void watch(){
        
		final File servicesFolder = serviceController.getServicesBaseFolder();
		
		//- Get the serviceFiles
        final File[] serviceFiles = servicesFolder.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				return !file.isDirectory() && file.getName().endsWith(".dar");
			}
        });
        
        for(File file : serviceFiles){
        	FileProperties<Long> cachedFileProperties = null;
        	final long modified = file.lastModified();        	
        	if(!cachedPropertiesMap.keySet().contains(file)){
        		// start the service
        		try{
        			startService(file);
        			cachedFileProperties = new FileProperties<Long>(file.length(), modified);
        			cachedPropertiesMap.put(file, cachedFileProperties);
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}else{
        		cachedFileProperties = cachedPropertiesMap.get(file);
        		if(modified > cachedFileProperties.modified || file.length() != cachedFileProperties.size){
        			// restart the service
        			try{
        				startService(file);
        				cachedFileProperties = new FileProperties<Long>(file.length(), modified);
        				cachedPropertiesMap.put(file, cachedFileProperties);
        			}catch(Exception e){
        				e.printStackTrace();
        			}
        		}
        	}
        }
	}
	
	@Override
	public void run() {
		/*
		 * Simple algorithm
		 * for every 10 seconds
		 *  read the contents of the service folder
		 *   if any file (ending with .dar) exists in the cache; then
		 *    if the object's modified date is greater than the cached value; then restart service
		 *     record the current file size, wait for 5 seconds. if the file size remains the same
		 *   if the file didn't exist; then deploy it and start the service
		 */
		long waitTime=10L * 1000; // in seconds
		while(!isInterrupted()){
			watch();
	        try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				interrupted();
				break;
			}	
		}
	}

}

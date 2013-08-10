package org.kakooge.dacho.jetty;

import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kakooge.dacho.api.ServiceContext;

public class JettyRunnable implements Runnable{
    
    private final boolean debug = System.getProperty("debug")!=null;
    private final static Logger logger = Logger.getLogger(JettyRunnable.class.getName());
    private ServiceContext serviceContext;
    private Server server = null;
        
    public void setServiceContext(final ServiceContext sc){
    	assert sc!=null;
    	if(sc==null)
    		throw new IllegalArgumentException("Null service context supplied");
    	this.serviceContext = sc;
    }
    
    public void terminate(){
    	if(server!=null)
			try {
				server.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    @Override
    public void run() {
    	Integer port = Integer.parseInt(serviceContext.getInitParameter("port"));
    	
        server = new Server(port);
        try{
            /*
            XmlConfiguration configuration =  new XmlConfiguration(new FileInputStream("/home/mawandm/Documents/Projects/jetty-ws/jetty.xml"));
            configuration.configure(server);
            */
        	
        	assert serviceContext!=null;
        	
            String home = serviceContext.getInitParameter("contexts");
            
            if(debug)
            	logger.info(String.format("Executing server on...%s with context home %s", port, home));
            /*Enumeration<String> enume = serviceContext.getInitParameterNames();
            while(enume.hasMoreElements()){
            	String name = enume.nextElement();
            	logger.info(String.format("%s=%s", name, serviceContext.getInitParameter(name)));
            }*/

            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath("/");
            webapp.setWar(home+"/examples.war");
            server.setHandler(webapp);     

            if(debug)
                logger.info("Starting service");
            server.start();        
            server.join();
            
        }catch(InterruptedException e){
            try{
                if(debug)
                    logger.info("Stopping service");
                server.stop();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            Thread.currentThread().isInterrupted();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
   
}

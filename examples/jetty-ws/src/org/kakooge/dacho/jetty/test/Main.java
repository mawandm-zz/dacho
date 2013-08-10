package org.kakooge.dacho.jetty.test;

import java.util.Enumeration;
import java.util.logging.Logger;

import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.jetty.JettyService;

public class Main {
	 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        /*Server server = new Server(8983);        
        String home = "/home/mawandm/Documents/Projects/jetty-ws/contexts";
        
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(home+"/examples.war");
        server.setHandler(webapp);     
        
        server.start();        
        server.join();
        */
        
                
        
        /*JettyMain jettyMain = new JettyMain();
        jettyMain.run();
        Thread jettyMainThread = new Thread(jettyMain);
        jettyMainThread.start();
        jettyMainThread.join();*/
        
        final ServiceBase serviceBase = new JettyService();
            
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try{
                    serviceBase.OnStop();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        
        serviceBase.OnStart(Logger.getLogger(JettyService.class.getName()), new ServiceContext(){
			@Override
			public String getInitParameter(String arg0) {
				//return params.get(arg0);
				if(arg0.equals("contexts"))
					return "/home/mawandm/Documents/Projects/jetty-ws/contexts";
				else if(arg0.equals("port"))
					return "8983";
				return null;
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
        	
        });
        
        Thread.sleep(30 * 1000);
        serviceBase.OnStop();
    }
}

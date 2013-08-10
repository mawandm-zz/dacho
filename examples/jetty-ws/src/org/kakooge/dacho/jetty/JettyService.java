package org.kakooge.dacho.jetty;

import java.util.logging.Logger;

import org.kakooge.dacho.api.DSMException;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;


/**
 *
 * @author mawandm
 */
public class JettyService extends ServiceBase{

    private final JettyRunnable jettyMain;
    private final Thread jettyMainThread;
    private final static boolean debug = System.getProperty("debug")!=null;
    private Logger logger;
    
    public JettyService(){
        jettyMain = new JettyRunnable();
        jettyMainThread = new Thread(jettyMain);
    }
    
    @Override
    public void OnStart(Logger logger, ServiceContext serviceContext) throws DSMException {
        assert serviceContext.getInitParameterNames()!=null;
        this.logger = logger;
        if(debug)
        	logger.info("Executing OnStart...");
        jettyMain.setServiceContext(serviceContext);
        jettyMainThread.start();
    }

    @Override
    public void OnStop() throws DSMException{
        if(debug)
        	logger.info("Executing OnStop...");
        jettyMain.terminate();
        try {
			jettyMainThread.join();
		} catch (InterruptedException ignore) {}
        
        if(debug)
        	logger.info("Service stopped...");
    }
   
}


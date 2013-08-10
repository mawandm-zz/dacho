package org.kakooge.dacho.api;

import java.util.logging.Logger;

/**
 * Provides a base for all service implementations. 
 * All service classes extend this class but 
 * must be implement {@link #OnStart(Logger, ServiceContext)} 
 * and {@link #OnStop()} 
 * @author mawandm
 */
public abstract class ServiceBase implements ServiceProcess{
   
    @Override
    public void OnPause() throws DSMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void OnResume() throws DSMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void OnShutdown() throws DSMException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


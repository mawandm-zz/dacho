package org.kakooge.dacho.api;

public class DSMException  extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3789680811251997236L;
	public DSMException(String error){
        super(error);
    }    
    public DSMException(String error, Throwable e){
        super(error, e);
    }
}

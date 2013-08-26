package org.kakooge.dacho.dm.service;

/**
 * 
 * @author mawandm
 *
 */
public interface DownloadProcess extends Runnable{
	void init() throws Exception;
	void destroy() throws Exception;
}

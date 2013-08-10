package org.kakooge.dacho.dm.process;

/**
* Attempts to model a basic download process of data. The process goes through three stages, firstly the {@code download} is done
* then the data is {@code clean}sed to suit local system/business needs and then the data is {@code save}d. The generic template parameters
* define the raw data download type {@code T} and its cleansed version {@code W}
* 
* @version 18.1 June 2013
* @author mawandm
* 
*/
public interface DownloadProcess<T, W> {
	T download() throws Exception;
	W clean(T data) throws Exception;
	void save(W data) throws Exception;
	void init() throws Exception;
	void destroy() throws Exception;
	void execute() throws Exception;
}


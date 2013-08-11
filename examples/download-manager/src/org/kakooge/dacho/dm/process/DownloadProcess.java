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
	
	/**
	 * Download the data
	 * @return the downloaded data in either raw format or with some basic formatting
	 * @throws Exception
	 */
	T download() throws Exception;
	
	/**
	 * Clean the downloaded data
	 * @param data the downloaded data
	 * @return cleaned data ready to be saved
	 * @throws Exception if bad things happen
	 */
	W clean(T data) throws Exception;
	
	/**
	 * Saves the data into some persistent state
	 * @param data the data
	 * @throws Exception when bad things happen
	 */
	void save(W data) throws Exception;
	
	/**
	 * Allows for initialization of the download process
	 * @throws Exception
	 */
	void init() throws Exception;
	
	/**
	 * Allows for clean destruction of this download process object
	 * @throws Exception
	 */
	void destroy() throws Exception;
	
	/**
	 * Performs the choreographed execution of the download process
	 * @throws Exception
	 */
	void execute() throws Exception;
}


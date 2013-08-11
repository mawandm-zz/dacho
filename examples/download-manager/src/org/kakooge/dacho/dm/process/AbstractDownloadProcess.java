package org.kakooge.dacho.dm.process;

/**
 * Generic class for a download process
 * @author mawandm
 */
public abstract class AbstractDownloadProcess<T, W> implements DownloadProcess<T, W>{

    @Override
	public void init() throws Exception {
	}

	@Override
	public void destroy() throws Exception {
	}

	@Override
    public void execute() throws Exception {
        T data = download();
        W result = clean(data);
        save(result);
    }
    
}

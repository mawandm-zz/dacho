package org.kakooge.dacho.loader;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.kakooge.dacho.api.ServiceContext;

public class DachoServiceContext implements ServiceContext {

	private Map<String, String> parameterMap;
	private String name;
	
	public void setName(final String name){
		this.name = name;
	}
	
	public void setParameterMap(final Map<String, String> parameterMap){
		this.parameterMap = parameterMap;
	}
	
	@Override
	public String getInitParameter(final String parameterName) {
		return parameterMap.get(parameterName);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		final Set<String> keySet = parameterMap.keySet();
		return new Enumeration<String>(){
			
			final Iterator<String> it = keySet.iterator();
			
			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}

			@Override
			public String nextElement() {
				return it.next();
			}
			
		};
	}

	@Override
	public String getName() {
		return name;
	}

}

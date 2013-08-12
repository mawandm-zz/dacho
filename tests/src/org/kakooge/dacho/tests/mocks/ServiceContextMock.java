package org.kakooge.dacho.tests.mocks;

import java.util.Enumeration;
import java.util.Properties;

import org.kakooge.dacho.api.ServiceContext;

public class ServiceContextMock implements ServiceContext{

	final String name;
	final Properties properties;
	public ServiceContextMock(final Properties properties, String name){
		this.properties = properties;
		this.name = name;
	}
	
	@Override
	public String getInitParameter(String arg0) {
		return properties.getProperty(arg0);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

}

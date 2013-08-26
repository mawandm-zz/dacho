package org.kakooge.dacho.dm;

import java.util.Enumeration;

import org.kakooge.dacho.api.ServiceContext;

public class TestServiceContext implements ServiceContext {
	@Override
	public String getInitParameter(String arg0) {
		if (arg0.equals("config"))
			return "config/dm.properties";
		if (arg0.equals(ServiceContext.SERVICE_HOME))
			return "/host/Users/mawandm/Documents/Projects/kakooge/dacho/examples/download-manager/dist";
		if (arg0.equals("run.schedule"))
			return "'FTAS','FTAI'|*/5 * * * *\n" + "'IXIC','NYA'|*/5 * * * *";

		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}

package org.kakooge.dacho.tests.mocks;

import java.util.logging.Logger;

import org.kakooge.dacho.api.DSMException;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;

public class TestServiceClass1 extends ServiceBase{
	@Override
	public void OnStart(Logger arg0, ServiceContext arg1)
			throws DSMException {
	}
	@Override
	public void OnStop() throws DSMException {
	}
}
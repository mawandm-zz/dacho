package org.kakooge.dacho.tests;

import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Test;
import org.kakooge.dacho.api.ServiceBase;
import org.kakooge.dacho.api.ServiceContext;
import org.kakooge.dacho.loader.ServiceBootstrap;
import org.kakooge.dacho.tests.mocks.ServiceContextMock;
import org.kakooge.dacho.tests.mocks.TestServiceClass1;
import org.kakooge.dacho.tests.mocks.TestServiceClass2;
import org.kakooge.dacho.tests.mocks.TestServiceClass3;
import org.kakooge.dacho.tests.util.Util;

/**
 * 
 * @author mawandm
 *
 */
public class ServiceBootstrapTest{
		
	/**
	 * Test the loading of a valid class
	 */
	@Test
	public void testValidClass(){
		
		try {
			ServiceBase bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass1.class.getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test the execution of a a valid OnStart
	 */
	@Test
	public void testValidClassOnStart(){
		
		try {
			ServiceBase bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass1.class.getName());
			bootstrapService.OnStart(Logger.getAnonymousLogger(), null);
		} catch (Exception e) {
			//System.out.println(Util.stackTraceString(e));
			fail("Failed to execute the OnStart method successfully");
		}
	}
	
	/**
	 * Test loading of an invalid class name
	 */
	@Test
	public void testInvalidClass1(){
		ServiceBase bootstrapService = null;
		try {
			bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), "");
			fail("Error loaded invalid class ''");
		} catch (Exception e) {
			
		}

	}
	
	/**
	 * Test execution of an invalid service base class
	 */
	@Test
	public void testBootstratpInvalidOnStart(){
		ServiceBase bootstrapService = null;
		try {
			bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass2.class.getName());
			
			Properties p = new Properties();
			p.setProperty(ServiceContext.SERVICE_HOME, "/home");
			
			ServiceContext sc = new ServiceContextMock(p, "testContext");
			Logger logger = Logger.getAnonymousLogger();
			bootstrapService.OnStart(logger, sc);
			fail("Error test did not detect unimplemented OnStart");
		} catch (Exception e) {
			
		}

	}
	
	/**
	 * Test execution of an invalid service base class
	 */
	@Test
	public void testBootstratpInvalidOnStop(){
		ServiceBase bootstrapService = null;
		try {
			bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass2.class.getName());
			bootstrapService.OnStop();
			fail("Error test did not detect unimplemented OnStop");
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Test execution of an invalid service base class
	 */
	@Test
	public void testBootstrapFailedOnStart(){
		ServiceBase bootstrapService = null;
		try {
			bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass3.class.getName());
			bootstrapService.OnStop();
			fail("Error test did not detect failed OnStart");
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Test execution of an invalid service base class
	 */
	@Test
	public void testBootstrapFailedOnStop(){
		ServiceBase bootstrapService = null;
		try {
			bootstrapService = new ServiceBootstrap(getClass().getClassLoader(), TestServiceClass3.class.getName());
			bootstrapService.OnStop();
			fail("Error test did not detect failed OnStop");
		} catch (Exception e) {
			
		}
	}
}

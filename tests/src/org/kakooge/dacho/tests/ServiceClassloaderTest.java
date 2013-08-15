package org.kakooge.dacho.tests;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kakooge.dacho.loader.ServiceClassLoader;
import org.kakooge.dacho.loader.ServiceController;

public class ServiceClassloaderTest {
	
	private ClassLoader classLoader1 = null;
	private ClassLoader classLoader2 = null;
	private Class<?> klass1 = null;
	private Class<?> klass2 = null;
	private Class<?> klass3 = null;
	
	@Before
	public void initObjects() throws Exception{
        File file = new File("lib/classloader-test.jar");
        URL[] urls = new URL[]{file.toURI().toURL()};
        
        classLoader1 = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        classLoader2 = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        
        klass1 = classLoader1.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
        klass2 = classLoader1.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
        klass3 = classLoader2.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
	}
	
    /**
     * The classloader maintains that;
     * <pre>
     * 1. When two bytewise similar classes A & B are loaded by two different classloaders then 
     *    A.class != B.class becuase they'll be loaded into two different memory locations.
     *    If however A & B are loaded by the same classloader, then A.class == B.class since CLs cache any 
     *    new resolutions
     * 2. This same notion applies to resources such as properties files, icons e.t.c.
     * </pre>
     * @throws Exception
     */
	@Test
	public void testClassObjectSimilarityDiffCL() throws Exception{
		Assert.assertFalse(klass2.equals(klass3));
    }
	
	@Test
	public void testClassObjectSimilaritySameCL() throws Exception{
		Assert.assertTrue(klass1.equals(klass2));
    }
	
	public void testResourceSimilarity(){
		
	}
}

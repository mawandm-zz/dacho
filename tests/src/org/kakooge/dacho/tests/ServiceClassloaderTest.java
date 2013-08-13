package org.kakooge.dacho.tests;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.kakooge.dacho.loader.ServiceClassLoader;
import org.kakooge.dacho.loader.ServiceController;

public class ServiceClassloaderTest {
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
    @SuppressWarnings("unused")
	public void testClassObjectSimilarity() throws Exception{
       
        File file = new File("lib/classloader-test.jar");
        URL[] urls = new URL[]{file.toURI().toURL()};
        
        ClassLoader classLoader1 = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        ClassLoader classLoader2 = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        
        Class<?> klass1 = classLoader1.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
        Class<?> klass2 = classLoader1.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
        Class<?> klass3 = classLoader2.loadClass("org.kakooge.dacho.tests.mocks.TestClassLoaderMock");
        
        Assert.assertTrue(klass1.equals(klass2));
        Assert.assertFalse(klass2.equals(klass3));
        Assert.assertFalse(klass1.equals(klass3));
    }
	
	public void testResourceSimilarity(){
		
	}
}

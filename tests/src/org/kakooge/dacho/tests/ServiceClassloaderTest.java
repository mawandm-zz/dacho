package org.kakooge.dacho.tests;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.kakooge.dacho.loader.ServiceClassLoader;
import org.kakooge.dacho.loader.ServiceController;

public class ServiceClassloaderTest {
    /**
     * This is just test code
     * @throws Exception
     */
	//@Test
    private static void tests() throws Exception{
       
        URL[] urls = new URL[1];
        File file = new File("/home/mawandm/Documents/Projects/jsc-tests/dist/jsc-tests.jar");
        urls[0] = file.toURI().toURL();
        ClassLoader classLoader = new ServiceClassLoader(urls, ServiceController.class.getClassLoader());
        
        Class<?> clazz = classLoader.loadClass("org.kiboel.asm.jsc.loader.tests.TestClass");
        System.out.println(String.format("Clazz '%s' classloader is '%s'", clazz.getName(), clazz.getClassLoader().getClass().getName()));
        Object instance = clazz.newInstance();
        
    }
}

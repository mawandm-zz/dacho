package org.kakooge.dacho.tests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import static org.junit.Assert.fail;

import org.kakooge.dacho.tests.util.Util;
import org.kakooge.dacho.util.IOUtils;


public class IOUtilsTest{
	
	private final static CountDownLatch startGate = new CountDownLatch(1);
	private volatile static boolean complete = false;
	
	private static class Thread2 extends Thread{
		private Thread t1;
		private final File tempFile;
		public Thread2(File tempFile){
			this.tempFile = tempFile;
		}
		
		public void setThread1(Thread t1){
			this.t1 = t1;
		}
		
		@Override
		public void run(){
			try {startGate.await();} catch (InterruptedException e1) {} //- Wait for t2 to create the first 4kb of dummy data
			try {
				complete = IOUtils.complete(tempFile);
			} catch (InterruptedException e) {
				t1.interrupt();
			}
		}
	}
	
	private static class Thread1 extends Thread{
		private Thread t2;
		private final File tempFile;
		public Thread1(final File tempFile){
			this.tempFile = tempFile;
		}		
		
		public void setThread2(Thread t2){
			this.t2 = t2;
		}
		
		@Override
		public void run(){
			try {
				RandomAccessFile raf = new RandomAccessFile(tempFile, "rwd");
				raf.setLength(0x1000L); //-Set an initial value
				startGate.countDown();
				raf.setLength(0x80000000L); //2gb
			} catch (Exception e) {
				e.printStackTrace();
				fail("Failed to create random test file: \n" + Util.stackTraceString(e));
				t2.interrupt();
			}
		}
	}
	
	
	/**
	 * This tests IOUtils.complete functionality.
	 * We start a thread to create a temp file of size 2GB + 4kb
	 * We start another thread to start as soon as the first 4kb bytes are written
	 * Test passes if after 5 minutes or less, the file is detected as complete
	 * Test fails otherwise (this could be failure to detect completion or an IOException occured
	 * @throws IOException 
	 */
	@Test
	public static void testComplete() throws IOException{
		
		//- See: http://stackoverflow.com/questions/1755285/creating-a-temp-file-is-incredibly-slow if too slow some JVMs have issues
		final File tempFile = File.createTempFile("dachoCompleteTest", null);
		tempFile.deleteOnExit();
		
		Thread1 t1 = new Thread1(tempFile);
		Thread2 t2 = new Thread2(tempFile);
		
		t1.setDaemon(true);
		t2.setDaemon(true);
		
		t1.setThread2(t2);
		t2.setThread1(t1);
		
		t1.start();
		t2.start();
		
		final long maxWait = 5 * 60 * 1000L;
		
		try {t1.join(maxWait);} catch (InterruptedException e) {}
		try {t2.join(maxWait);} catch (InterruptedException e) {}
		
		if(!complete)
			fail("Failed to determine complete file");
	}
	
}

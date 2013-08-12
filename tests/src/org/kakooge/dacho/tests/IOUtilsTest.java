package org.kakooge.dacho.tests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

import org.kakooge.dacho.util.IOUtils;

public class IOUtilsTest{
	/**
	 * Testing code
	 * @throws IOException 
	 */
	private void testComplete() throws IOException{
		
		final CountDownLatch startGate = new CountDownLatch(1);
		
		//- See: http://stackoverflow.com/questions/1755285/creating-a-temp-file-is-incredibly-slow if too slow some JVMs have issues
		final File tempFile = File.createTempFile("dachoCompleteTest", null);
		tempFile.deleteOnExit();
		
		Thread t1 = new Thread(){
			@Override
			public void run(){
				try {
					startGate.await();
				} catch (InterruptedException e1) {}
				
				while(true){
					try {
						Thread.sleep(10 * 1000);
						System.out.println(IOUtils.complete(tempFile));
					} catch (InterruptedException e) {}
				}
			}
		};
		
		Thread t2 = new Thread(){
			@Override
			public void run(){
				try {
					RandomAccessFile raf = new RandomAccessFile(tempFile, "rwd");
					raf.setLength(0x1000L); //-Set an initial value
					startGate.countDown();
					raf.setLength(0x80000000L); //2gb
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		t1.start();
	}
	
	/*
	public static void main(String[] args){
		testComplete();
	}
	*/
}

package org.kakooge.dacho.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IOUtils {
	/**
	 * Copy from one stream to another
	 * @param is stream to copy from 
	 * @param os stream to copy to
	 * @throws IOException if bad things happen during IO
	 */
	public static void copy(final InputStream is, final OutputStream os) throws IOException{
		if(is==null || os==null)
			throw new IllegalArgumentException("Null input or output stream supplied as argument");
		final byte[] buffer = new byte[4096];
		int len;
		while((len = is.read(buffer))>0)
			os.write(buffer, 0, len);
	}
	
	/**
	 * Rudimentary method to probe that a file is complete.
	 * @param file the file to probe
	 * @return true of the file is complete false otherwise
	 * @throws Exception if interrupted during the probe or if maximum wait time expired without determining completion
	 */
	public static boolean complete(final File file) throws InterruptedException{
		long length = 0;
		//final long maxSleep = ;
		//long totalSleep = 0;
		final int maxTry = 3;
		final long sleepLength = 2L; // in seconds
		
		if(file==null)
			throw new IllegalArgumentException("Null file supplied as argument");
		
		//
		// Basically keep checking and if the file length is moving reset trycount.
		// If the file size stays constant for over maxTry then the file is complete
		//
		for(int trycount = 0; /*totalSleep < maxSleep || */trycount < maxTry; ++trycount){
			Thread.sleep(sleepLength * 1000);
			//totalSleep += sleepLength;
			//System.out.println(file.length());
			if(file.length() > length){
				trycount=0;
				length = file.length();
			}
		}
		
		//- One last check
		if(length!=file.length())
			return false;
		return true;
	}
	
	/**
	 * Strips the extension off a file name
	 * @param file the File to be stripped
	 * @return the stripped name string
	 */
	public static String stripExtension(final File file){
		if(file==null)
			throw new IllegalArgumentException("Null file supplied as argument");

		final String name = file.getName();
		if(file.isDirectory())
			return name;
		final int idx = name.lastIndexOf('.');
		return (idx<0) ? name : name.substring(0, idx);
	}
	
	/**
	 * Unzip/deploys the supplied file. Expects that the file structure is equivalent to a jar file
	 * @param file the file to deploy
	 * @return the deployment folder created which is the same as the result of {@link IOUtils#stripExtension(File)}
	 * @throws  
	 * @throws IOException if bad things happen during IO
	 */
	public static File deploy(File file) throws IOException{
		
		final String deployFileName = file.getName();
		final File deployFolder = new File(file.getParent(), deployFileName.substring(0, deployFileName.lastIndexOf(".dar")));
		
		if(!deployFolder.mkdir())
			throw new IOException(String.format("The directory %s could not be created", deployFolder.getCanonicalFile()));
		
		InputStream fis = null;
		ZipInputStream zis = null;
		
		try{
			fis = new FileInputStream(file);
			zis = new ZipInputStream(fis);
			ZipEntry entry = zis.getNextEntry();
			
			System.out.println("Deploying " + deployFolder.getCanonicalPath());
			
			while(entry!=null){
				final String fileName = entry.getName();
				final File entryFile = new File(deployFolder, fileName);
				entryFile.getParentFile().mkdirs();
				
				if(!entry.isDirectory()){
					FileOutputStream fos = null;
					try{
						fos = new FileOutputStream(entryFile);
						IOUtils.copy(zis, fos);
					}finally{
						try{
							fos.close();
						}catch(Exception ignore){} //IO & Null
					}
				}
				
				entry = zis.getNextEntry();
			}
		}finally{
			try{
				fis.close();
			}catch(Exception ignore){} //- NullPointer & IO excepts
			
			try{
				zis.close();
			}catch(Exception ignore){}
		}
		
		return deployFolder;
	}
	
	/**
	 * Testing code
	 */
	private static void testComplete(){
		Thread t = new Thread(){
			@Override
			public void run(){
				while(true){
					
					//- Create with: dd if=/dev/zero of=/host/temp/file.out bs=1G count=2
					File file = new File("/host/temp/file.out");
					
					try {
						Thread.sleep(10 * 1000);
						System.out.println(IOUtils.complete(file));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		};
		t.start();
	}
	
	/*
	public static void main(String[] args){
		testComplete();
	}
	*/
}

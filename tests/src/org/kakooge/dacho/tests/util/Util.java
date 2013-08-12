package org.kakooge.dacho.tests.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
	public static String stackTraceString(Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
}

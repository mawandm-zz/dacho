package org.kakooge.dacho.util;


public class StringUtil {
	public static boolean empty(final String value){
		return value==null || value.trim().length()==0;
	}
}

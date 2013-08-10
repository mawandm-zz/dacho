package org.kakooge.dacho.dm.util;

import java.util.Enumeration;
import java.util.Properties;

public class Utilities {
	  /**
	   * Copy a set of properties from one Property to another.
	   * <p>
	   *
	   * @param src_prop  Source set of properties to copy from.
	   * @param dest_prop Dest Properties to copy into.
	   *
	   **/
	  public static void copyProperties(Properties source, Properties destination)
	  {
	      for (Enumeration<?> propertyNames = source.propertyNames();
	           propertyNames.hasMoreElements(); )
	      {
	          Object key = propertyNames.nextElement();
	          destination.put(key, source.get(key));
	      }
	  }
}

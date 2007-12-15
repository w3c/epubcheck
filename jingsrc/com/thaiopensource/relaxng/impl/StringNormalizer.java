package com.thaiopensource.relaxng.impl;

import java.util.StringTokenizer;

class StringNormalizer {
  static String normalize(String s) {
    StringBuffer buf = new StringBuffer();
    for (StringTokenizer e = new StringTokenizer(s); e.hasMoreElements();) {
      if (buf.length() > 0)
	buf.append(' ');
      buf.append((String)e.nextElement());
    }
    return buf.toString();
  }
}

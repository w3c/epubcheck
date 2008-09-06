package com.thaiopensource.datatype.xsd;

import com.thaiopensource.xml.util.Naming;

class NmtokenDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    return Naming.isNmtoken(str);
  }
  public int getLength(Object obj) {
    // Surrogates are not possible in an NMTOKEN.
    return ((String)obj).length();
  }
  public boolean alwaysValid() {
    return false;
  }
}

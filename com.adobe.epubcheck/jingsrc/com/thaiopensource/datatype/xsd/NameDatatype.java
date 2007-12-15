package com.thaiopensource.datatype.xsd;

import com.thaiopensource.xml.util.Naming;

class NameDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    return Naming.isName(str);
  }
  public int getLength(Object obj) {
    // Surrogates are not possible in an Name.
    return ((String)obj).length();
  }

  public boolean alwaysValid() {
    return false;
  }
}

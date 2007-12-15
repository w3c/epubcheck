package com.thaiopensource.datatype.xsd;

import com.thaiopensource.xml.util.Naming;

class NCNameDatatype extends NameDatatype {
  public boolean lexicallyAllows(String str) {
    return Naming.isNcname(str);
  }
}

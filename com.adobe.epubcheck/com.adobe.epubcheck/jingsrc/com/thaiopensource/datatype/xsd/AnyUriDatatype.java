package com.thaiopensource.datatype.xsd;

import com.thaiopensource.util.Uri;
import org.relaxng.datatype.ValidationContext;

class AnyUriDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    return Uri.isValid(str);
  }

  public boolean alwaysValid() {
    return false;
  }
}

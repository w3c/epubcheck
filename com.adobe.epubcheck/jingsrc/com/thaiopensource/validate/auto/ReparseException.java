package com.thaiopensource.validate.auto;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.IncorrectSchemaException;

import java.io.IOException;

public abstract class ReparseException extends SAXException {
  public ReparseException() {
    super((Exception)null);
  }

  public abstract Schema reparse(InputSource in) throws IncorrectSchemaException, SAXException, IOException;
}

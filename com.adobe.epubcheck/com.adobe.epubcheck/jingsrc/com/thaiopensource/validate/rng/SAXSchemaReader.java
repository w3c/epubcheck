package com.thaiopensource.validate.rng;

import com.thaiopensource.relaxng.impl.SchemaReaderImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

public class SAXSchemaReader extends SchemaReaderImpl {
  private static final SchemaReader theInstance = new SAXSchemaReader();
  
  private SAXSchemaReader() {
  }
  
  public static SchemaReader getInstance() {
    return theInstance;
  }

  protected Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    return new SAXParseable(xrc, in, eh);
  }
}

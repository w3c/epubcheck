package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.ParsedPatternFuture;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class SAXParseReceiver extends SAXSubParser implements ParseReceiver {
  public SAXParseReceiver(XMLReaderCreator xrc, ErrorHandler eh) {
    super(xrc, eh);
  }

  public ParsedPatternFuture installHandlers(XMLReader xr, SchemaBuilder schemaBuilder, Scope scope)
          throws SAXException {
    return new SchemaParser(xr, eh, schemaBuilder, null, scope);
  }
}

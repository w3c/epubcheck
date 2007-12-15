package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class SAXParseable extends SAXSubParser implements Parseable {
  private final InputSource in;

  public SAXParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    super(xrc, eh);
    this.in = in;
  }

  public ParsedPattern parse(SchemaBuilder schemaBuilder, Scope scope) throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, null, scope);
      xr.parse(in);
      return sp.getParsedPattern();
    }
    catch (SAXException e) {
      throw toBuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

}

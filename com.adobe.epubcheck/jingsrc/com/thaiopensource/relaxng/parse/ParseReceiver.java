package com.thaiopensource.relaxng.parse;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

public interface ParseReceiver extends SubParser {
  ParsedPatternFuture installHandlers(XMLReader xr, SchemaBuilder schemaBuilder, Scope scope) throws SAXException;
}

package com.thaiopensource.validate.auto;

import org.xml.sax.SAXException;

import java.io.IOException;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.IncorrectSchemaException;

public interface SchemaFuture {
  Schema getSchema() throws IncorrectSchemaException, SAXException, IOException;
  RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException;
}

package com.thaiopensource.relaxng.impl;

import org.xml.sax.SAXException;
import com.thaiopensource.validate.IncorrectSchemaException;

import java.io.IOException;

public interface PatternFuture {
  Pattern getPattern(boolean isAttributesPattern) throws IncorrectSchemaException, SAXException, IOException;
}

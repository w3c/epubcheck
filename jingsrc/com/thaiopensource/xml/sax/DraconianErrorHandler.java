package com.thaiopensource.xml.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * An <code>ErrorHandler</code> implementing a brutal error handling policy.
 * Fatal errors and errors are handled by throwing the exception.
 * Warnings are ignored.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class DraconianErrorHandler implements ErrorHandler {
  public void warning(SAXParseException e) throws SAXException {
  }

  public void error(SAXParseException e) throws SAXException {
    throw e;
  }

  public void fatalError(SAXParseException e) throws SAXException {
    throw e;
  }
}

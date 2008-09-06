package com.thaiopensource.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLReaderFactory;

import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * An <code>XMLReaderCreator</code> that creates <code>XMLReader</code>s using the SAX2 <code>XMLReaderFactory</code>.
 * An instance of this class is safe for concurrent access by multiple threads.
 *
 * @see org.xml.sax.helpers.XMLReaderFactory
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Sax2XMLReaderCreator implements XMLReaderCreator {
  private final String className;

  /**
   * Constructs a <code>Sax2XMLReaderCreator</code> that uses system defaults to construct <code>XMLReader</code>s.
   */
  public Sax2XMLReaderCreator() {
    this.className = null;
  }

 /**
  * Constructs a <code>Sax2XMLReaderCreator</code> that constructs <code>XMLReader</code>s with the specified
  * class name.
  *
  * @param className the fully-qualified name of the class implementing <code>XMLReader</code>;
  * if <code>null</code> equivalent to the no-argument constructor
  *
  */
  public Sax2XMLReaderCreator(String className) {
    this.className = className;
  }

  public XMLReader createXMLReader() throws SAXException {
    XMLReader xr;
    if (className == null)
      xr = XMLReaderFactory.createXMLReader();
    else
      xr = XMLReaderFactory.createXMLReader(className);
    xr.setFeature("http://xml.org/sax/features/namespaces", true);
    xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
    try {
      xr.setFeature("http://xml.org/sax/features/validation", false);
    }
    catch (SAXNotRecognizedException e) {
    }
    catch (SAXNotSupportedException e) {
    }
    return xr;
  }
}

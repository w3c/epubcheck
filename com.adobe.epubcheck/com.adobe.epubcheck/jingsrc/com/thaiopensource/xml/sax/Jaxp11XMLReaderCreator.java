package com.thaiopensource.xml.sax;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * An <code>XMLReaderCreator</code> that uses JAXP 1.1 to create <code>XMLReader</code>s.
 * An instance of this class is <em>not</em> safe for concurrent access by multiple threads.
 *
 * @see javax.xml.parsers.SAXParserFactory
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Jaxp11XMLReaderCreator implements XMLReaderCreator {
    
  private final SAXParserFactory factory;

  /**
   * Default constructor.
   */
  public Jaxp11XMLReaderCreator() {
    factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
  }

  public XMLReader createXMLReader() throws SAXException {
    try {
      return factory.newSAXParser().getXMLReader();
    }
    catch (ParserConfigurationException e) {
      throw new SAXException(e);
    }
  }
}

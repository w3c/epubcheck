package com.thaiopensource.datatype.xsd.regex.test;

import com.thaiopensource.datatype.xsd.regex.Regex;
import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;
import com.thaiopensource.util.Service;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Utf16;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Enumeration;

public class TestDriver extends DefaultHandler {
  private final StringBuffer buf = new StringBuffer();
  private Regex regex;
  private int nFail = 0;
  private int nTests = 0;
  private Locator loc;
  private final RegexEngine engine;

  static public void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
    if (args.length != 2) {
      System.err.println("usage: TestDriver class testfile");
      System.exit(2);
    }
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    XMLReader xr = factory.newSAXParser().getXMLReader();

    Enumeration e = new Service(RegexEngine.class).getProviders();
    RegexEngine engine;
    for (;;) {
      if (!e.hasMoreElements()) {
        System.err.println("couldn't find regex engine");
        System.exit(2);
      }
      engine = (RegexEngine)e.nextElement();
      if (engine.getClass().getName().equals(args[0]))
        break;
    }
    TestDriver tester = new TestDriver(engine);
    xr.setContentHandler(tester);
    InputSource in = new InputSource(UriOrFile.fileToUri(args[1]));
    xr.parse(in);
    System.err.println(tester.nTests + " tests performed");
    System.err.println(tester.nFail + " failures");
    if (tester.nFail > 0)
      System.exit(1);
  }

  public TestDriver(RegexEngine engine) {
    this.engine = engine;
  }

  public void setDocumentLocator(Locator locator) {
    this.loc = locator;
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    buf.append(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    buf.append(ch, start, length);
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    buf.setLength(0);
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (localName.equals("valid"))
      valid(buf.toString());
    else if (localName.equals("invalid"))
      invalid(buf.toString());
    else if (localName.equals("correct"))
      correct(buf.toString());
    else if (localName.equals("incorrect"))
      incorrect(buf.toString());
  }

  private void correct(String str) {
    nTests++;
    regex = null;
    try {
      regex = engine.compile(str);
    }
    catch (RegexSyntaxException e) {
      error("unexpected error: " + e.getMessage() + ": " + display(str, e.getPosition()));
    }
  }

  private void incorrect(String str) {
    nTests++;
    regex = null;
    try {
      engine.compile(str);
      error("failed to detect error in regex: " + display(str, -1));
    }
    catch (RegexSyntaxException e) { }
  }

  private void valid(String str) {
    if (regex == null)
      return;
    nTests++;
    if (!regex.matches(str))
      error("match failed for string: " + display(str, -1));
  }

  private void invalid(String str) {
    if (regex == null)
      return;
    nTests++;
    if (regex.matches(str))
      error("match incorrectly succeeded for string: " + display(str, -1));
  }

  private void error(String str) {
    int line = -1;
    if (loc != null)
      line = loc.getLineNumber();
    if (line >= 0)
      System.err.print("Line " + line + ": ");
    System.err.println(str);
    nFail++;
  }

  static final private String ERROR_MARKER = ">>>>";

  static String display(String str, int pos) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0, len = str.length(); i < len; i++) {
      if (i == pos)
        buf.append(ERROR_MARKER);
      char c = str.charAt(i);
      if (Utf16.isSurrogate1(c))
        buf.append("&#x" + Integer.toHexString(Utf16.scalarValue(c, str.charAt(++i))) + ";");
      else if (c < ' ' || c >= 0x7F)
        buf.append("&#x" + Integer.toHexString(c) + ";");
      else if (c == '&')
        buf.append("&amp;");
      else
        buf.append(c);
    }
    if (str.length() == pos)
      buf.append(ERROR_MARKER);
    return buf.toString();
  }

}

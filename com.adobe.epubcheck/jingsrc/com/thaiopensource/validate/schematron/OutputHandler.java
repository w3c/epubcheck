package com.thaiopensource.validate.schematron;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import com.thaiopensource.util.Localizer;

class OutputHandler extends DefaultHandler {
  private final ErrorHandler eh;
  private int lineNumber = -1;
  private String systemId = null;
  private final StringBuffer message = new StringBuffer();
  private boolean inMessage = false;
  private final String lineSeparator;
  private static final String indent = "  ";
  private final Localizer localizer = new Localizer(OutputHandler.class);

  OutputHandler(ErrorHandler eh) {
    this.eh = eh;
    this.lineSeparator = System.getProperty("line.separator");
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    if (inMessage) {
      for (int i = 0; i < length; i++) {
        char c = ch[start + i];
        switch (c) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
          if (message.length() == 0 || message.charAt(message.length() - 1) != ' ')
            message.append(' ');
          break;
        default:
          message.append(c);
          break;
        }
      }
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    characters(ch, start, length);
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (localName.equals("failed-assertion")
        || localName.equals("report")) {
      String value = attributes.getValue("", "line-number");
      if (value == null)
        lineNumber = -1;
      else {
        try {
          lineNumber = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
          lineNumber = -1;
        }
      }
      value = attributes.getValue("", "system-id");
      if (value != null && value.equals(""))
        value = null;
      systemId = value;
      message.append(localizer.message(localName.equals("failed-assertion")
                                       ? "failed_assertion"
                                       : "report"));
    }
    else if (localName.equals("statement") || localName.equals("diagnostic")) {
      inMessage = true;
      message.append(lineSeparator);
      message.append(indent);
    }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (localName.equals("statement") || localName.equals("diagnostic")) {
      if (message.length() > 0 && message.charAt(message.length() - 1) == ' ')
        message.setLength(message.length() - 1);
      inMessage = false;
    }
    else if (localName.equals("failed-assertion")
             || localName.equals("report")) {
      eh.error(new SAXParseException(message.toString(), null, systemId, lineNumber, -1));
      message.setLength(0);
    }
  }
}

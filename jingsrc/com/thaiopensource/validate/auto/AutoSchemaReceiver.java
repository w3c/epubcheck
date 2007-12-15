package com.thaiopensource.validate.auto;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.Vector;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;

public class AutoSchemaReceiver implements SchemaReceiver {
  private final PropertyMap properties;
  private final Rewindable rewindable;

  private class Handler extends DefaultHandler implements SchemaFuture {
    private final XMLReader xr;
    private SchemaFuture sf = null;
    private Locator locator = null;
    private final Vector prefixMappings = new Vector();

    private Handler(XMLReader xr) {
      this.xr = xr;
    }

    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    public void startPrefixMapping(String prefix, String uri) {
      prefixMappings.addElement(prefix);
      prefixMappings.addElement(uri);
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      SchemaReceiverFactory srf = SchemaReceiverFactory.PROPERTY.get(properties);
      SchemaReceiver sr = srf.createSchemaReceiver(uri, properties);
      if (sr == null) {
        Localizer localizer = new Localizer(AutoSchemaReceiver.class);
        String detail = ("".equals(uri)
                         ? localizer.message("no_namespace")
                         : localizer.message("unknown_namespace", uri));
        throw new SAXParseException(detail, locator);
      }
      sf = sr.installHandlers(xr);
      rewindable.willNotRewind();
      ContentHandler contentHandler = xr.getContentHandler();
      if (contentHandler == null)
        return;
      if (locator != null) {
        contentHandler.setDocumentLocator(locator);
        contentHandler = xr.getContentHandler();
      }
      contentHandler.startDocument();
      contentHandler = xr.getContentHandler();
      for (int i = 0, len = prefixMappings.size(); i < len; i += 2) {
        contentHandler.startPrefixMapping((String)prefixMappings.elementAt(i),
                                          (String)prefixMappings.elementAt(i + 1));
        contentHandler = xr.getContentHandler();
      }
      contentHandler.startElement(uri, localName, qName, attributes);
    }

    public Schema getSchema() throws IncorrectSchemaException, SAXException, IOException {
      if (sf == null)
        throw new IncorrectSchemaException();
      return sf.getSchema();
    }

    public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
      if (sf == null)
        return e;
      return sf.unwrapException(e);
    }
  }

  public AutoSchemaReceiver(PropertyMap properties, Rewindable rewindable) {
    this.properties = properties;
    this.rewindable = rewindable;
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    Handler h = new Handler(xr);
    xr.setContentHandler(h);
    return h;
  }
}

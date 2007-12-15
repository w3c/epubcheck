package com.thaiopensource.xml.sax;

import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;

public class ForkDTDHandler implements DTDHandler {
  private final DTDHandler dh1;
  private final DTDHandler dh2;

  public ForkDTDHandler(DTDHandler dh1, DTDHandler dh2) {
    this.dh1 = dh1;
    this.dh2 = dh2;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
    dh1.notationDecl(name, publicId, systemId);
    dh2.notationDecl(name, publicId, systemId);
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
    dh1.unparsedEntityDecl(name, publicId, systemId, notationName);
    dh2.unparsedEntityDecl(name, publicId, systemId, notationName);
  }
}

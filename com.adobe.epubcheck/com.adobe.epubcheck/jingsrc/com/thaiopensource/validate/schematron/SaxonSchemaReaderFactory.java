package com.thaiopensource.validate.schematron;

import com.icl.saxon.TransformerFactoryImpl;

import javax.xml.transform.TransformerFactory;

public class SaxonSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public TransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }
}

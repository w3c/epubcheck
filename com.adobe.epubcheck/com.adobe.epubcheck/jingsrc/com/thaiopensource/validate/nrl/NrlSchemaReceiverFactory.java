package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.xml.sax.XMLReaderCreator;

public class NrlSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!SchemaImpl.NRL_URI.equals(namespaceUri))
      return null;
    // modified for ePubCheck
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    return new SchemaReceiverImpl(properties, xrc);
  }

  public Option getOption(String uri) {
    return null;
  }
}

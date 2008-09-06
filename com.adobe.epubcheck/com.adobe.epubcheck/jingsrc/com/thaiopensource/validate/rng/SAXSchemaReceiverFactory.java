package com.thaiopensource.validate.rng;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.relaxng.impl.SchemaReceiverImpl;
import com.thaiopensource.relaxng.parse.sax.SAXParseReceiver;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Option;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;

public class SAXSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             PropertyMap properties) {
    // XXX allow namespaces with incorrect version
    if (!WellKnownNamespaces.RELAX_NG.equals(namespaceUri))
      return null;
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    return new SchemaReceiverImpl(new SAXParseReceiver(xrc, eh), properties);
  }

  public Option getOption(String uri) {
    return RngProperty.getOption(uri);
  }
}

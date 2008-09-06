package com.thaiopensource.validate.mns;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.Option;

public class MnsSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!SchemaImpl.MNS_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(properties);
  }

  public Option getOption(String uri) {
    return null;
  }
}

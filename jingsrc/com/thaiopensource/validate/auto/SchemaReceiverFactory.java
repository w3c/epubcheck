package com.thaiopensource.validate.auto;

import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.Option;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMapBuilder;

public interface SchemaReceiverFactory {
  public static class SchemaReceiverFactoryPropertyId extends PropertyId {
    public SchemaReceiverFactoryPropertyId(String name) {
      super(name, SchemaReceiverFactory.class);
    }

    public SchemaReceiverFactory get(PropertyMap properties) {
      return (SchemaReceiverFactory)properties.get(this);
    }

    public SchemaReceiverFactory put(PropertyMapBuilder builder, SchemaReceiverFactory value) {
      return (SchemaReceiverFactory)builder.put(this, value);
    }
  }

  static final SchemaReceiverFactoryPropertyId PROPERTY
          = new SchemaReceiverFactoryPropertyId("SCHEMA_RECEIVER_FACTORY");
  SchemaReceiver createSchemaReceiver(String namespaceUri,
                                      PropertyMap properties);
  Option getOption(String uri);

}

package com.thaiopensource.validate.auto;

import com.thaiopensource.validate.SchemaReaderLoader;
import com.thaiopensource.validate.auto.SchemaReaderFactorySchemaReceiverFactory;

public class SchemaReaderLoaderSchemaReceiverFactory extends SchemaReaderFactorySchemaReceiverFactory {
  public SchemaReaderLoaderSchemaReceiverFactory() {
    super(new SchemaReaderLoader());
  }
}

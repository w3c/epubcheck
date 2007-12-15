package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.auto.ReparseException;
import com.thaiopensource.validate.auto.SchemaFuture;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class SchemaReaderSchemaReceiver implements SchemaReceiver {
  private final SchemaReader schemaLanguage;
  private final PropertyMap properties;

  public SchemaReaderSchemaReceiver(SchemaReader schemaLanguage, PropertyMap properties) {
    this.schemaLanguage = schemaLanguage;
    this.properties = properties;
  }

  public SchemaFuture installHandlers(XMLReader xr) throws SAXException {
    throw new ReparseException() {
      public Schema reparse(InputSource in) throws IncorrectSchemaException, SAXException, IOException {
        return schemaLanguage.createSchema(in, properties);
      }
    };
  }
}

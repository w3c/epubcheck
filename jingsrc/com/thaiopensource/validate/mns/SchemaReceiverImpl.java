package com.thaiopensource.validate.mns;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.validate.nrl.NrlSchemaReceiverFactory;
import com.thaiopensource.validate.nrl.NrlProperty;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;

class SchemaReceiverImpl implements SchemaReceiver {
  private static final String MNS_SCHEMA = "mns.rng";
  private static final String RNC_MEDIA_TYPE = "application/x-rnc";
  private final PropertyMap properties;
  private final PropertyMap attributeSchemaProperties;
  private final boolean attributesSchema;
  private final SchemaReader autoSchemaLanguage;
  private Schema mnsSchema = null;

  public SchemaReceiverImpl(PropertyMap properties) {
    this.attributesSchema = properties.contains(NrlProperty.ATTRIBUTES_SCHEMA);
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    if (attributesSchema) {
      attributeSchemaProperties = properties;
      builder.put(NrlProperty.ATTRIBUTES_SCHEMA, null);
      this.properties = builder.toPropertyMap();
    }
    else {
      this.properties = properties;
      NrlProperty.ATTRIBUTES_SCHEMA.add(builder);
      attributeSchemaProperties = builder.toPropertyMap();
    }
    this.autoSchemaLanguage = new AutoSchemaReader(SchemaReceiverFactory.PROPERTY.get(properties));
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    return new SchemaImpl(attributesSchema).installHandlers(xr, this);
  }

  Schema getMnsSchema() throws IOException, IncorrectSchemaException, SAXException {
   if (mnsSchema == null) {
      String className = SchemaReceiverImpl.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + MNS_SCHEMA;
      URL mnsSchemaUrl = getResource(resourceName);
      mnsSchema = SAXSchemaReader.getInstance().createSchema(
              new InputSource(mnsSchemaUrl.toString()),
              properties);
    }
    return mnsSchema;
  }

  private static URL getResource(String resourceName) {
    ClassLoader cl = SchemaReceiverImpl.class.getClassLoader();
    // XXX see if we should borrow 1.2 code from Service
    if (cl == null)
      return ClassLoader.getSystemResource(resourceName);
    else
      return cl.getResource(resourceName);
  }

  PropertyMap getProperties() {
    return properties;
  }

  Schema createChildSchema(InputSource inputSource, String schemaType, boolean isAttributesSchema) throws IOException, IncorrectSchemaException, SAXException {
    SchemaReader lang = isRnc(schemaType) ? CompactSchemaReader.getInstance() : autoSchemaLanguage;
    return lang.createSchema(inputSource,
                             isAttributesSchema ? attributeSchemaProperties : properties);
  }

  private static boolean isRnc(String schemaType) {
    if (schemaType == null)
      return false;
    schemaType = schemaType.trim();
    return schemaType.equals(RNC_MEDIA_TYPE);
  }
}

package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.XMLReaderCreator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;

class SchemaReceiverImpl implements SchemaReceiver {
  private static final String NRL_SCHEMA = "nrl.rng";
  private static final String RNC_MEDIA_TYPE = "application/x-rnc";
  private final PropertyMap properties;
  private final boolean attributesSchema;
  private final SchemaReader autoSchemaReader;
  private Schema nrlSchema = null;
  private final XMLReaderCreator xrc;
  private static final PropertyId subSchemaProperties[] = {
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ENTITY_RESOLVER,
    SchemaReceiverFactory.PROPERTY,
  };

  // modified for ePubCheck
  public SchemaReceiverImpl(PropertyMap properties, XMLReaderCreator xrc) {
	this.xrc = xrc;
    this.attributesSchema = properties.contains(NrlProperty.ATTRIBUTES_SCHEMA);
    PropertyMapBuilder builder = new PropertyMapBuilder();
    for (int i = 0; i < subSchemaProperties.length; i++) {
      Object value = properties.get(subSchemaProperties[i]);
      if (value != null)
        builder.put(subSchemaProperties[i], value);
    }
    this.properties = builder.toPropertyMap();
    this.autoSchemaReader = new AutoSchemaReader(SchemaReceiverFactory.PROPERTY.get(properties));
  }

  // modified for ePubCheck
  public SchemaFuture installHandlers(XMLReader xr) {
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    if (attributesSchema)
      NrlProperty.ATTRIBUTES_SCHEMA.add(builder);
    return new SchemaImpl(builder.toPropertyMap(), xrc).installHandlers(xr, this);
  }

  Schema getNrlSchema() throws IOException, IncorrectSchemaException, SAXException {
   if (nrlSchema == null) {
      String className = SchemaReceiverImpl.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + NRL_SCHEMA;
      URL nrlSchemaUrl = getResource(resourceName);
     nrlSchema = SAXSchemaReader.getInstance().createSchema(new InputSource(nrlSchemaUrl.toString()),
                                                              properties);
    }
    return nrlSchema;
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

  Schema createChildSchema(InputSource inputSource, String schemaType, PropertyMap options, boolean isAttributesSchema) throws IOException, IncorrectSchemaException, SAXException {
    SchemaReader reader = isRnc(schemaType) ? CompactSchemaReader.getInstance() : autoSchemaReader;
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    if (isAttributesSchema)
      NrlProperty.ATTRIBUTES_SCHEMA.add(builder);
    for (int i = 0, len = options.size(); i < len; i++)
      builder.put(options.getKey(i), options.get(options.getKey(i)));
    return reader.createSchema(inputSource, builder.toPropertyMap());
  }

  Option getOption(String uri) {
    Option option = autoSchemaReader.getOption(uri);
    if (option != null)
      return option;
    return CompactSchemaReader.getInstance().getOption(uri);
  }

  private static boolean isRnc(String schemaType) {
    if (schemaType == null)
      return false;
    schemaType = schemaType.trim();
    return schemaType.equals(RNC_MEDIA_TYPE);
  }
}

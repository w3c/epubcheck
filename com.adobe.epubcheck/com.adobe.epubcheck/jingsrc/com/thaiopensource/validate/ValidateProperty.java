package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

/**
 * Provides common properties to control reading schemas and validation.
 *
 * @see Schema#createValidator
 * @see SchemaReader#createSchema
 * @see PropertyMap
 * @see PropertyId
 * @see com.thaiopensource.validate.rng.RngProperty
 * @see com.thaiopensource.validate.schematron.SchematronProperty
 */
public class ValidateProperty {
  /**
   * Property specifying ErrorHandler to be used for reporting errors.  The value
   * to which this PropertyId maps must be an instance of ErrorHandler.
   *
   * @see ErrorHandler
   */
  public static final ErrorHandlerPropertyId ERROR_HANDLER = new ErrorHandlerPropertyId("ERROR_HANDLER");

  /**
   * Property specifying EntityResolver to be used for resolving entities. The value
   * to which this PropertyId maps must be an instance of EntityResolver.
   *
   * @see EntityResolver
   */
  public static final EntityResolverPropertyId ENTITY_RESOLVER = new EntityResolverPropertyId("ENTITY_RESOLVER");

  /**
   * Property specifying XMLReaderCreator used to create XMLReader objects needed for
   * parsing XML documents.  The value to which this PropertyId maps must be an
   * instance of XMLReaderCreator.
   */
  public static final XMLReaderCreatorPropertyId XML_READER_CREATOR
          = new XMLReaderCreatorPropertyId("XML_READER_CREATOR");

  private ValidateProperty() { }

  /**
   * A PropertyId whose value is constrained to be an instance of
   * ErrorHandler.
   *
   * @see ErrorHandler
   */
  public static class ErrorHandlerPropertyId extends PropertyId {
    public ErrorHandlerPropertyId(String name) {
      super(name, ErrorHandler.class);
    }

    /**
     * Returns the value of the property.  This is a typesafe
     * version of <code>PropertyMap.get</code>.
     *
     * @param properties the PropertyMap to be used
     * @return the ErrorHandler to which the PropertyMap maps this PropertyId,
     * or <code>null</code> if this PropertyId is not in the PropertyMap
     * @see PropertyMap#get
     */
    public ErrorHandler get(PropertyMap properties) {
      return (ErrorHandler)properties.get(this);
    }

    /**
     * Sets the value of the property. Modifies the PropertyMapBuilder
     * so that this PropertyId is mapped to the specified value. This
     * is a typesafe version of PropertyMapBuilder.put.
     *
     * @param builder the PropertyMapBuilder to be modified
     * @param value the ErrorHandler to which this PropertyId is to be mapped
     * @return the ErrorHandler to which this PropertyId was mapped before,
     * or <code>null</code> if it was not mapped
     *
     * @see PropertyMapBuilder#put
     */
    public ErrorHandler put(PropertyMapBuilder builder, ErrorHandler value) {
      return (ErrorHandler)builder.put(this, value);
    }
  }

  /**
   * A PropertyId whose value is constrained to be an instance of
   * EntityResolver.
   *
   * @see EntityResolver
   */
  public static class EntityResolverPropertyId extends PropertyId {
    public EntityResolverPropertyId(String name) {
      super(name, EntityResolver.class);
    }

    /**
     * Returns the value of the property.  This is a typesafe
     * version of <code>PropertyMap.get</code>.
     *
     * @param properties the PropertyMap to be used
     * @return the EntityResolver to which the PropertyMap maps this PropertyId,
     * or <code>null</code> if this PropertyId is not in the PropertyMap
     * @see PropertyMap#get
     */
    public EntityResolver get(PropertyMap properties) {
      return (EntityResolver)properties.get(this);
    }

    /**
     * Sets the value of the property. Modifies the PropertyMapBuilder
     * so that this PropertyId is mapped to the specified value. This
     * is a typesafe version of PropertyMapBuilder.put.
     *
     * @param builder the PropertyMapBuilder to be modified
     * @param value the EntityResolver to which this PropertyId is to be mapped
     * @return the EntityResolver to which this PropertyId was mapped before,
     * or <code>null</code> if it was not mapped
     *
     * @see PropertyMapBuilder#put
     */
    public EntityResolver put(PropertyMapBuilder builder, EntityResolver value) {
      return (EntityResolver)builder.put(this, value);
    }
  }

  /**
   * A PropertyId whose value is constrained to be an instance of
   * XMLReaderCreator.
   *
   * @see XMLReaderCreator
   */
  public static class XMLReaderCreatorPropertyId extends PropertyId {
    public XMLReaderCreatorPropertyId(String name) {
      super(name, XMLReaderCreator.class);
    }

    /**
     * Returns the value of the property.  This is a typesafe
     * version of <code>PropertyMap.get</code>.
     *
     * @param properties the PropertyMap to be used
     * @return the XMLReaderCreator to which the PropertyMap maps this PropertyId,
     * or <code>null</code> if this PropertyId is not in the PropertyMap
     * @see PropertyMap#get
     */
    public XMLReaderCreator get(PropertyMap properties) {
      return (XMLReaderCreator)properties.get(this);
    }

    /**
     * Sets the value of the property. Modifies the PropertyMapBuilder
     * so that this PropertyId is mapped to the specified value. This
     * is a typesafe version of PropertyMapBuilder.put.
     *
     * @param builder the PropertyMapBuilder to be modified
     * @param value the XMLReaderCreator to which this PropertyId is to be mapped
     * @return the XMLReaderCreator to which this PropertyId was mapped before,
     * or <code>null</code> if it was not mapped
     *
     * @see PropertyMapBuilder#put
     */
    public XMLReaderCreator put(PropertyMapBuilder builder, XMLReaderCreator value) {
      return (XMLReaderCreator)builder.put(this, value);
    }
  }
}

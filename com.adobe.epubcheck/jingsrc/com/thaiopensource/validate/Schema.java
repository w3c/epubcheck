package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;

/**
 * A schema that can be used to validate an XML document. A single <code>Schema</code> object
 * is safe for concurrent access by multiple threads.
 *
 * @see SchemaReader
 * @see Validator
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Schema {
  /**
   * Creates a new <code>Validator</code> that can be used to validate XML documents with
   * respect to this schema.  The <code>PropertyMap</code> should contain a
   * <code>ValidateProperty.ERROR_HANDLER</code> property, which will be
   * used to report errors.  If it does not, then an <code>ErrorHandler</code>
   * will be used that ignores warnings and throws its argument on errors and fatal errors.
   * Common properties are defined in <code>ValidateProperty</code>.  Implementations
   * may support additional properties.
   *
   * @param properties a <code>PropertyMap</code> specifying the properties of the
   * <code>Validator</code> to be created
   * @return a new <code>Validator</code> that can be used to validate an XML document
   * with respect to this schema; never <code>null</code>
   *
   * @see ValidateProperty#ERROR_HANDLER
   */
  Validator createValidator(PropertyMap properties);
  PropertyMap getProperties();
}

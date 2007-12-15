package com.thaiopensource.validate;

import com.thaiopensource.validate.SchemaReader;

/**
 * A factory for SchemaReader. A class that implements this interface can be
 * automatically discovered by SchemaReaderLoader, if it has
 * a 0-argument constructor and if its name is listed in the file
 * <code>META-INF/services/com.thaiopensource.validate.SchemaReaderFactory</code>.
 *
 * @see SchemaReaderLoader
 * @see SchemaReader
 */
public interface SchemaReaderFactory {
  /**
   * Creates a SchemaReader for a particular schema language.
   *
   * @param namespaceUri a String identifing the schema language; must not be <code>null</code>;
   * for schema languages that use XML, this should be the namespace URI
   * of the root element if the root element has a non-absent namespace URI
   *
   * @return a SchemaReader for the specified schema language, or <code>null</code>,
   * if this SchemaReaderFactory cannot create a SchemaReader for the specified
   * schema language
   */
  public SchemaReader createSchemaReader(String namespaceUri);

  Option getOption(String uri);
}

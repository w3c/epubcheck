package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.IncorrectSchemaException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A SchemaReader object is immutable and can thus be safely accessed
 * concurrently from multiple threads.
 */
public interface SchemaReader {
  public static final String BASE_URI = "http://www.thaiopensource.com/validate/";
  /**
   * Creates a <code>Schema</code> by reading it from an <code>InputSource</code>.
   *
   * @param in the <code>InputSource</code> from which to read the schema;
   * must not be <code>null</code>
   * @param properties a <code>PropertyMap</code> to control the schema creation;
   * must not be <code>null</code>
   * @return a newly created <code>Schema</code>, never <code>null</code>
   * @throws IOException if an I/O error occurs
   * @throws SAXException
   * @throws IncorrectSchemaException
   *
   * @see ValidateProperty
   */
  Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException;

  Option getOption(String uri);
}

package com.thaiopensource.validate;

import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.util.Service;

import java.util.Enumeration;
/**
 * A SchemaReaderFactory that automatically discovers SchemaReader implementations.
 * For a SchemeaReader implementation to be discoverable by this class, it must have
 * a factory class with a no-argument constructor implementing SchemaReaderFactory,
 * and the fully-qualified name of this factory class must be listed in the file
 * <code>META-INF/services/com.thaiopensource.validate.SchemaReaderFactory</code>.
 */
public class SchemaReaderLoader implements SchemaReaderFactory {
  private final Service service = new Service(SchemaReaderFactory.class);
  public SchemaReader createSchemaReader(String namespaceUri) {
    for (Enumeration enumList = service.getProviders(); enumList.hasMoreElements();) {
      SchemaReaderFactory srf = (SchemaReaderFactory)enumList.nextElement();
      SchemaReader sr = srf.createSchemaReader(namespaceUri);
      if (sr != null)
        return sr;
    }
    return null;
  }

  public Option getOption(String uri) {
    for (Enumeration enumList = service.getProviders(); enumList.hasMoreElements();) {
      SchemaReaderFactory srf = (SchemaReaderFactory)enumList.nextElement();
      Option option = srf.getOption(uri);
      if (option != null)
        return option;
    }
    return null;
  }
}

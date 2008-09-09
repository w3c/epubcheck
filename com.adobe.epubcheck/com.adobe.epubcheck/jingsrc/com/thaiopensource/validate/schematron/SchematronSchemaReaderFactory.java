package com.thaiopensource.validate.schematron;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaReaderFactory;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

public abstract class SchematronSchemaReaderFactory implements SchemaReaderFactory {
  public SchemaReader createSchemaReader(String namespaceUri) {
    if (namespaceUri.equals(SchemaReaderImpl.SCHEMATRON_URI)) {
      try {
        return new SchemaReaderImpl(newTransformerFactory());
      }
      catch (TransformerFactoryConfigurationError e) { }
      catch (IncorrectSchemaException e) { }
      catch (TransformerConfigurationException e) { }
    }
    return null;
  }

  public Option getOption(String uri) {
    return SchematronProperty.getOption(uri);
  }

  public abstract TransformerFactory newTransformerFactory();
}

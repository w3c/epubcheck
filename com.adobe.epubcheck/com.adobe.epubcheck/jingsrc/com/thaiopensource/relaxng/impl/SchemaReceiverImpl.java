package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.validate.nrl.NrlSchemaReceiverFactory;
import com.thaiopensource.validate.nrl.NrlProperty;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.util.PropertyMap;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class SchemaReceiverImpl implements SchemaReceiver {
  private final ParseReceiver parser;
  private final PropertyMap properties;

  public SchemaReceiverImpl(ParseReceiver parser, PropertyMap properties) {
    this.parser = parser;
    this.properties = properties;
  }

  public SchemaFuture installHandlers(XMLReader xr) throws SAXException {
    final SchemaPatternBuilder pb = new SchemaPatternBuilder();
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    DatatypeLibraryFactory dlf = RngProperty.DATATYPE_LIBRARY_FACTORY.get(properties);
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    final PatternFuture pf = SchemaBuilderImpl.installHandlers(parser, xr, eh, dlf, pb);
    return new SchemaFuture() {
      public Schema getSchema() throws IncorrectSchemaException, SAXException, IOException {
        return SchemaReaderImpl.wrapPattern(pf.getPattern(properties.contains(NrlProperty.ATTRIBUTES_SCHEMA)),
                                            pb, properties);
      }
      public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
        if (e instanceof BuildException)
          return SchemaBuilderImpl.unwrapBuildException((BuildException)e);
        return e;
      }
    };
  }
}

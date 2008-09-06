package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.nrl.NrlSchemaReceiverFactory;
import com.thaiopensource.validate.nrl.NrlProperty;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class SchemaReaderImpl implements SchemaReader {
  private static final PropertyId[] supportedPropertyIds = {
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ERROR_HANDLER,
    RngProperty.DATATYPE_LIBRARY_FACTORY,
    RngProperty.CHECK_ID_IDREF,
    RngProperty.FEASIBLE,
    NrlProperty.ATTRIBUTES_SCHEMA,
  };

  public Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    DatatypeLibraryFactory dlf = RngProperty.DATATYPE_LIBRARY_FACTORY.get(properties);
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    Pattern start = SchemaBuilderImpl.parse(createParseable(xrc, in, eh), eh, dlf, spb,
                                            properties.contains(NrlProperty.ATTRIBUTES_SCHEMA));
    return wrapPattern(start, spb, properties);
  }

  public Option getOption(String uri) {
    return RngProperty.getOption(uri);
  }

  static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, PropertyMap properties) throws SAXException, IncorrectSchemaException {
    properties = AbstractSchema.filterProperties(properties, supportedPropertyIds);
    if (properties.contains(RngProperty.FEASIBLE))
      start = FeasibleTransform.transform(spb, start);
    Schema schema = new PatternSchema(spb, start, properties);
    if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
      ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
      IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null)
        throw new IncorrectSchemaException();
      Schema idSchema;
      if (properties.contains(RngProperty.FEASIBLE))
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap, properties);
      else
        idSchema = new IdTypeMapSchema(idTypeMap, properties);
      schema = new CombineSchema(schema, idSchema, properties);
    }
    return schema;
  }

  protected abstract Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh);
}

package com.thaiopensource.validate.rng;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.FlagPropertyId;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.FlagOption;
import com.thaiopensource.validate.SchemaReader;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class RngProperty {

  private RngProperty() { }

  public static class DatatypeLibraryFactoryPropertyId extends PropertyId {
    public DatatypeLibraryFactoryPropertyId(String name) {
      super(name, DatatypeLibraryFactory.class);
    }

    public DatatypeLibraryFactory get(PropertyMap properties) {
      return (DatatypeLibraryFactory)properties.get(this);
    }

    public DatatypeLibraryFactory put(PropertyMapBuilder builder, DatatypeLibraryFactory value) {
      return (DatatypeLibraryFactory)builder.put(this, value);
    }
  }

  public static final DatatypeLibraryFactoryPropertyId DATATYPE_LIBRARY_FACTORY
          = new DatatypeLibraryFactoryPropertyId("DATATYPE_LIBRARY_FACTORY");
  public static final FlagPropertyId CHECK_ID_IDREF = new FlagPropertyId("CHECK_ID_IDREF");
  public static final FlagPropertyId FEASIBLE = new FlagPropertyId("FEASIBLE");

  public static Option getOption(String uri) {
    if (!uri.startsWith(SchemaReader.BASE_URI))
      return null;
    uri = uri.substring(SchemaReader.BASE_URI.length());
    if (uri.equals("feasible"))
      return new FlagOption(FEASIBLE);
    if (uri.equals("check-id-idref"))
      return new FlagOption(CHECK_ID_IDREF);
    return null;
  }
}

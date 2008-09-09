package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

class BuiltinDatatypeBuilder implements DatatypeBuilder {
  private final Datatype dt;

  BuiltinDatatypeBuilder(Datatype dt) {
    this.dt = dt;
  }

  public void addParameter(String name,
			   String value,
			   ValidationContext context) throws DatatypeException {
    throw new DatatypeException(SchemaBuilderImpl.localizer.message("builtin_param"));
  }

  public Datatype createDatatype() {
    return dt;
  }
}

package com.thaiopensource.relaxng.impl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;

public class CombineSchema extends AbstractSchema {
  private final Schema schema1;
  private final Schema schema2;

  public CombineSchema(Schema schema1, Schema schema2, PropertyMap properties) {
    super(properties);
    this.schema1 = schema1;
    this.schema2 = schema2;
  }

  public Validator createValidator(PropertyMap properties) {
    return new CombineValidator(schema1.createValidator(properties),
                                schema2.createValidator(properties));
  }
}

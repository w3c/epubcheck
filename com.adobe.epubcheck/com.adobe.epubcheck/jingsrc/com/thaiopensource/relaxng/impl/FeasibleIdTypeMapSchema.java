package com.thaiopensource.relaxng.impl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;

public class FeasibleIdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public FeasibleIdTypeMapSchema(IdTypeMap idTypeMap, PropertyMap properties) {
    super(properties);
    this.idTypeMap = idTypeMap;
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    return new FeasibleIdSoundnessChecker(idTypeMap, eh);
  }
}

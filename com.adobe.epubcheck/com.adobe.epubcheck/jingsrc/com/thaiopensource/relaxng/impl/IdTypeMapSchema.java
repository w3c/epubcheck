package com.thaiopensource.relaxng.impl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.AbstractSchema;
import org.xml.sax.ErrorHandler;

public class IdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public IdTypeMapSchema(IdTypeMap idTypeMap, PropertyMap properties) {
    super(properties);
    this.idTypeMap = idTypeMap;
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    return new IdSoundnessChecker(idTypeMap, eh);
  }
}

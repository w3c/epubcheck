package com.thaiopensource.validate.schematron;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.Validator;

import javax.xml.transform.Templates;

class SchemaImpl extends AbstractSchema {
  private final Templates templates;

  SchemaImpl(Templates templates, PropertyMap properties, PropertyId[] supportedPropertyIds) {
    super(properties, supportedPropertyIds);
    this.templates = templates;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(templates, properties);
  }
}

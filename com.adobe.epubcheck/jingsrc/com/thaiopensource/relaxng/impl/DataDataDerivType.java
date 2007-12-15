package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

class DataDataDerivType extends DataDerivType {
  private final Datatype dt;
  private PatternMemo validMemo;
  private PatternMemo invalidMemo;

  DataDataDerivType(Datatype dt) {
    this.dt = dt;
  }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc) {
    if (dt.isValid(str, vc)) {
      if (validMemo == null)
        validMemo = super.dataDeriv(builder, p, str, vc);
      return validMemo;
    }
    else {
      if (invalidMemo == null)
        invalidMemo = super.dataDeriv(builder, p, str, vc);
      return invalidMemo;
    }
  }

  DataDerivType copy() {
    return new DataDataDerivType(dt);
  }

  DataDerivType combine(DataDerivType ddt) {
    if (ddt instanceof DataDataDerivType) {
      if (((DataDataDerivType)ddt).dt == dt)
        return this;
      return InconsistentDataDerivType.getInstance();
    }
    if (ddt instanceof ValueDataDerivType) {
      if (((ValueDataDerivType)ddt).getDatatype() == dt)
        return ddt;
      return InconsistentDataDerivType.getInstance();
    }
    return ddt.combine(this);
  }
}
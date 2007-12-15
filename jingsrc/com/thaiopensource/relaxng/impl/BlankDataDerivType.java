package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.ValidationContext;

class BlankDataDerivType extends DataDerivType {
  private PatternMemo blankMemo;
  private PatternMemo nonBlankMemo;

  BlankDataDerivType() { }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc) {
    if (DataDerivFunction.isBlank(str)) {
      if (blankMemo == null)
        blankMemo = super.dataDeriv(builder, p, str, vc);
      return blankMemo;
    }
    else {
      if (nonBlankMemo == null)
        nonBlankMemo = super.dataDeriv(builder, p, str, vc);
      return nonBlankMemo;
    }
  }

  DataDerivType copy() {
    return new BlankDataDerivType();
  }

  DataDerivType combine(DataDerivType ddt) {
    if (ddt instanceof BlankDataDerivType || ddt instanceof SingleDataDerivType)
      return this;
    return InconsistentDataDerivType.getInstance();
  }
}
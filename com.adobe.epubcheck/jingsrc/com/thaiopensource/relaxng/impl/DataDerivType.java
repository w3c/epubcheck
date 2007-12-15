package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.ValidationContext;

abstract class DataDerivType {
  abstract DataDerivType copy();
  abstract DataDerivType combine(DataDerivType ddt);
  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc) {
    return builder.getPatternMemo(p.applyForPattern(new DataDerivFunction(str, vc, builder)));
  }
}

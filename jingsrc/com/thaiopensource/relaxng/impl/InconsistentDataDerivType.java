package com.thaiopensource.relaxng.impl;

class InconsistentDataDerivType extends DataDerivType {
  static private final InconsistentDataDerivType instance = new InconsistentDataDerivType();

  static InconsistentDataDerivType getInstance() {
    return instance;
  }

  private InconsistentDataDerivType() { }

  DataDerivType combine(DataDerivType ddt) {
    return this;
  }

  DataDerivType copy() {
    return this;
  }
}

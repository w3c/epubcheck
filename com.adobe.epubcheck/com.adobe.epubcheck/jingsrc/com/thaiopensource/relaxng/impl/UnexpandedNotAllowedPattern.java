package com.thaiopensource.relaxng.impl;

class UnexpandedNotAllowedPattern extends NotAllowedPattern {
  UnexpandedNotAllowedPattern() {
  }
  boolean isNotAllowed() {
    return false;
  }
  Pattern expand(SchemaPatternBuilder b) {
    return b.makeNotAllowed();
  }
}

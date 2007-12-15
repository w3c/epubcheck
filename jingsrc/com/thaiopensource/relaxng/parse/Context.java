package com.thaiopensource.relaxng.parse;

import org.relaxng.datatype.ValidationContext;

import java.util.Enumeration;

public interface Context extends ValidationContext {
  Enumeration prefixes();
  Context copy();
}

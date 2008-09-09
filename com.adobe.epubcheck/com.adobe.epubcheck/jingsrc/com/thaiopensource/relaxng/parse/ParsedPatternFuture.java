package com.thaiopensource.relaxng.parse;

public interface ParsedPatternFuture {
  ParsedPattern getParsedPattern() throws IllegalSchemaException;
}

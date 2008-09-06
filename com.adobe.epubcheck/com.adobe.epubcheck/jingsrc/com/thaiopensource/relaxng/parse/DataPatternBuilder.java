package com.thaiopensource.relaxng.parse;

public interface DataPatternBuilder {
  void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
    throws BuildException;
  void annotation(ParsedElementAnnotation ea);
  ParsedPattern makePattern(Location loc, Annotations anno)
    throws BuildException;
  ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
    throws BuildException;
}

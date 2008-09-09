package com.thaiopensource.relaxng.parse;

public interface Include extends GrammarSection {
  void endInclude(String uri, String ns,
                  Location loc, Annotations anno) throws BuildException, IllegalSchemaException;
}

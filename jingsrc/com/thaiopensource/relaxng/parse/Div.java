package com.thaiopensource.relaxng.parse;

public interface Div extends GrammarSection {
  void endDiv(Location loc, Annotations anno) throws BuildException;
}

package com.thaiopensource.relaxng.parse;

public interface Grammar extends GrammarSection, Scope {
  ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException;
}

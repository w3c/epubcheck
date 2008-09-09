package com.thaiopensource.relaxng.parse;

public interface SubParser {
  ParsedPattern parseInclude(String uri, SchemaBuilder f, IncludedGrammar g)
          throws BuildException, IllegalSchemaException;
  ParsedPattern parseExternal(String uri, SchemaBuilder f, Scope s)
          throws BuildException, IllegalSchemaException;
}

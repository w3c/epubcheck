package com.thaiopensource.relaxng.parse;

public interface ElementAnnotationBuilder extends Annotations {
  void addText(String value, Location loc, CommentList comments) throws BuildException;
  ParsedElementAnnotation makeElementAnnotation() throws BuildException;
}

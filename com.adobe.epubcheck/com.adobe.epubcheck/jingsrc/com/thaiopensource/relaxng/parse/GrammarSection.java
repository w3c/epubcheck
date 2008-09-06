package com.thaiopensource.relaxng.parse;

public interface GrammarSection {

  static final class Combine {
    private final String name;
    private Combine(String name) {
      this.name = name;
    }
    final public String toString() {
      return name;
    }
  }

  static final Combine COMBINE_CHOICE = new Combine("choice");
  static final Combine COMBINE_INTERLEAVE = new Combine("interleave");

  static final String START = new String("#start");

  void define(String name, Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
    throws BuildException;
  void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException;
  void topLevelComment(CommentList comments) throws BuildException;
  Div makeDiv();
  /**
   * Returns null if already in an include.
   */
  Include makeInclude();
}

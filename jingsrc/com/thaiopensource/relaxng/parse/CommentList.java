package com.thaiopensource.relaxng.parse;

public interface CommentList {
  void addComment(String value, Location loc) throws BuildException;
}

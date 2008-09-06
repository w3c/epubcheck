package com.thaiopensource.validate.nrl;

import com.thaiopensource.xml.util.Naming;

import java.util.Vector;

class Path {
  private final boolean root;
  private final Vector names;

  Path(boolean root, Vector names) {
    this.root = root;
    this.names = names;
  }

  boolean isRoot() {
    return root;
  }

  Vector getNames() {
    return names;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (root)
      buf.append('/');
    for (int i = 0, len = names.size(); i < len; i++) {
      if (i != 0)
        buf.append('/');
      buf.append((String)names.elementAt(i));
    }
    return buf.toString();
  }

  static class ParseException extends Exception {
    private final String messageKey;

    ParseException(String messageKey) {
      super(messageKey);
      this.messageKey = messageKey;
    }

    public String getMessageKey() {
      return messageKey;
    }
  }

  private static final int START = 0;
  private static final int IN_NAME = 1;
  private static final int AFTER_NAME = 2;
  private static final int AFTER_SLASH = 3;

  static Vector parse(String str) throws ParseException {
    int state = START;
    int nameStartIndex = -1;
    Vector paths = new Vector();
    Vector names = new Vector();
    boolean root = false;
    for (int i = 0, len = str.length(); i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case ' ':
      case '\r':
      case '\n':
      case '\t':
        if (state == IN_NAME) {
          names.addElement(makeName(str, nameStartIndex, i));
          state = AFTER_NAME;
        }
        break;
      case '/':
        switch (state) {
        case IN_NAME:
          names.addElement(makeName(str, nameStartIndex, i));
          break;
        case START:
          root = true;
          break;
        case AFTER_SLASH:
          throw new ParseException("unexpected_slash");
        }
        state = AFTER_SLASH;
        break;
      case '|':
        switch (state) {
        case START:
          throw new ParseException("empty_path");
        case AFTER_NAME:
          break;
        case AFTER_SLASH:
          throw new ParseException("expected_name");
        case IN_NAME:
          names.addElement(makeName(str, nameStartIndex, i));
          break;
        }
        paths.addElement(new Path(root, names));
        root = false;
        names = new Vector();
        state = START;
        break;
      default:
        switch (state) {
        case AFTER_NAME:
          throw new ParseException("expected_slash");
        case AFTER_SLASH:
        case START:
          nameStartIndex = i;
          state = IN_NAME;
          break;
        case IN_NAME:
          break;
        }
        break;
      }
    }
    switch (state) {
    case START:
      throw new ParseException("empty_path");
    case AFTER_NAME:
      break;
    case AFTER_SLASH:
      throw new ParseException("expected_name");
    case IN_NAME:
      names.addElement(makeName(str, nameStartIndex, str.length()));
      break;
    }
    paths.addElement(new Path(root, names));
    return paths;
  }

  private static String makeName(String str, int start, int end) throws ParseException {
    String name = str.substring(start, end);
    if (!Naming.isNcname(name))
      throw new ParseException("invalid_name");
    return name;
  }

  static public void main(String[] args) throws ParseException {
    Vector paths = parse(args[0]);
    for (int i = 0; i < paths.size(); i++) {
      if (i != 0)
        System.out.println("---");
      Path path = (Path)paths.elementAt(i);
      if (path.isRoot())
        System.out.println("/");
      for (int j = 0; j < path.getNames().size(); j++)
        System.out.println(path.getNames().elementAt(j));
    }
  }
}

package com.thaiopensource.xml.util;

public class StringSplitter {
  private StringSplitter() {
  }

  public static String[] split(String str) {
    int len = str.length();
    int nTokens = 0;
    for (int i = 0; i < len; i++)
     if (!isSpace(str.charAt(i)) && (i == 0 || isSpace(str.charAt(i - 1))))
       nTokens++;
    String[] tokens = new String[nTokens];
    nTokens = 0;
    int tokenStart = -1;
    for (int i = 0; i < len; i++) {
      if (isSpace(str.charAt(i))) {
        if (tokenStart >= 0) {
          tokens[nTokens++] = str.substring(tokenStart, i);
          tokenStart = -1;
        }
      }
      else if (i == 0 || isSpace(str.charAt(i - 1)))
       tokenStart = i;
    }
    if (tokenStart >= 0)
      tokens[nTokens] = str.substring(tokenStart, len);
    return tokens;
  }

  private static boolean isSpace(char c) {
    switch (c) {
    case ' ':
    case '\r':
    case '\n':
    case '\t':
      return true;
    }
    return false;
  }

}

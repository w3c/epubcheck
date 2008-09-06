package com.thaiopensource.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

public class UriOrFile {
  private UriOrFile() {
  }

  public static String toUri(String uriOrFile) {
    if (!hasScheme(uriOrFile)) {
      try {
	return fileToUri(uriOrFile);
      }
      catch (MalformedURLException e) { }
    }
    return uriOrFile;
  }

  private static boolean hasScheme(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    if (!isAlpha(str.charAt(0)))
      return false;
    for (int i = 1; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case ':':
	// Don't recognize single letters as schemes
	return i == 1 ? false : true;
      case '+':
      case '-':
	break;
      default:
	if (!isAlnum(c))
	  return false;
	break;
      }
    }
    return false;
  }

  private static boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  private static boolean isAlnum(char c) {
    return isAlpha(c) || ('0' <= c && c <= '9');
  }

  public static String fileToUri(String file) throws MalformedURLException {
    return fileToUri(new File(file));
  }

  public static String fileToUri(File file) throws MalformedURLException {
    String path = file.getAbsolutePath().replace(File.separatorChar, '/');
    if (path.length() > 0 && path.charAt(0) != '/')
      path = '/' + path;
    return new URL("file", "", path).toString();
  }

  public static String uriToUriOrFile(String uri) {
    if (!uri.startsWith("file:"))
      return uri;
    uri = uri.substring(5);
    int nSlashes = 0;
    while (nSlashes < uri.length() && uri.charAt(nSlashes) == '/')
      nSlashes++;
    File f = new File(uri.substring(nSlashes).replace('/', File.separatorChar));
    if (f.isAbsolute())
      return f.toString();
    return uri.replace('/', File.separatorChar);
  }

  static public void main(String[] args) {
    System.err.println(uriToUriOrFile(args[0]));
  }
}

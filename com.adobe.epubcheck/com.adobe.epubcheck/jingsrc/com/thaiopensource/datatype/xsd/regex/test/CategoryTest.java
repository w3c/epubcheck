package com.thaiopensource.datatype.xsd.regex.test;

import com.thaiopensource.datatype.xsd.regex.Regex;
import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;
import com.thaiopensource.util.Utf16;
import com.thaiopensource.util.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Enumeration;

public class CategoryTest {
  static private final String categories = "LMNPZSC";
  static private final String subCategories = "LuLlLtLmLoMnMcMeNdNlNoPcPdPsPePiPfPoZsZlZpSmScSkSoCcCfCoCn";

  private final Regex[] categoryPosRegexes = new Regex[categories.length()];
  private final Regex[] categoryNegRegexes = new Regex[categories.length()];
  private final Regex[] subCategoryPosRegexes = new Regex[subCategories.length()/2];
  private final Regex[] subCategoryNegRegexes = new Regex[subCategories.length()/2];

  static public void main(String[] args) throws IOException, RegexSyntaxException {
    if (args.length != 2) {
      System.err.println("usage: " + CategoryTest.class.getName() + " engineClass UnicodeData");
      System.exit(2);
    }
    BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
    Enumeration e = new Service(RegexEngine.class).getProviders();
    RegexEngine engine;
    for (;;) {
      if (!e.hasMoreElements()) {
        System.err.println("couldn't find regex engine");
        System.exit(2);
      }
      engine = (RegexEngine)e.nextElement();
      if (engine.getClass().getName().equals(args[0]))
        break;
    }
    int nFail = new CategoryTest(engine).testAll(r);
    System.err.println(nFail + " tests failed");
    System.exit(nFail > 0 ? 1 : 0);
  }

  CategoryTest(RegexEngine engine) throws RegexSyntaxException {
    for (int i = 0, len = categories.length(); i < len; i++) {
      String ch = categories.substring(i, i + 1);
      categoryPosRegexes[i] = engine.compile("\\p{" + ch + "}");
      categoryNegRegexes[i] = engine.compile("\\P{" + ch + "}");
    }
    for (int i = 0, len = subCategories.length(); i < len; i += 2) {
      String name = subCategories.substring(i, i + 2);
      subCategoryPosRegexes[i/2] = engine.compile("\\p{" + name + "}");
      subCategoryNegRegexes[i/2] = engine.compile("\\P{" + name + "}");
    }
  }

  int testAll(BufferedReader r) throws IOException {
    int lastCode = -1;
    for (;;) {
      String line = r.readLine();
      if (line == null)
        break;
      int semi = line.indexOf(';');
      if (semi < 0)
        continue;
      int code = Integer.parseInt(line.substring(0, semi), 16);
      int semi2 = line.indexOf(';', semi + 1);
      String name = line.substring(semi, semi2);
      String category = line.substring(semi2 + 1, semi2 + 3);
      if (lastCode + 1 != code) {
        String missingCategory = name.endsWith(", Last>") ? category : "Cn";
        for (int i = lastCode + 1; i < code; i++)
          test(i, missingCategory);
      }
      test(code, category);
      lastCode = code;
    }
    for (++lastCode; lastCode < 0x110000; lastCode++)
      test(lastCode, "Cn");
    return nFail;
  }

  void test(int ch, String category) {
    if (!isXmlChar(ch))
      return;
    if (subCategories.indexOf(category) < 0) {
      System.err.println("Missing category: " + category);
      System.exit(2);
    }
    for (int i = 0, len = categories.length(); i < len; i++)
      check(ch, categoryPosRegexes[i], categoryNegRegexes[i],
            category.charAt(0) == categories.charAt(i),
            categories.substring(i, i + 1));
    for (int i = 0, len = subCategories.length(); i < len; i += 2)
      check(ch, subCategoryPosRegexes[i/2], subCategoryNegRegexes[i/2],
            category.equals(subCategories.substring(i, i + 2)),
            subCategories.substring(i, i + 2));
  }

  void check(int ch, Regex pos, Regex neg, boolean inPos, String cat) {
    String str;
    if (ch > 0xFFFF)
      str = new String(new char[]{ Utf16.surrogate1(ch), Utf16.surrogate2(ch) });
    else
      str = new String(new char[]{ (char)ch });
    if (pos.matches(str) != inPos )
      fail(ch, cat);
    if (neg.matches(str) != !inPos)
      fail(ch, "-" + cat);
  }

  int nFail = 0;

  void fail(int ch, String cat) {
    nFail++;
    System.err.println("Failed: " + Integer.toHexString(ch) + "/" + cat);
  }

  static boolean isXmlChar(int code) {
    switch (code) {
    case '\r': case '\n': case '\t':
      return true;
    case 0xFFFE: case 0xFFFF:
      return false;
    default:
      if (code < 0x20)
        return false;
      if (code >= 0xD800 && code < 0xE000)
        return false;
      return true;
    }
  }
}

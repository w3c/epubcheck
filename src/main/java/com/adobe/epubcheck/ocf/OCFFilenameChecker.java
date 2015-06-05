package com.adobe.epubcheck.ocf;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;

import java.util.HashSet;

public final class OCFFilenameChecker
{
  private static final HashSet<String> restricted30CharacterSet;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("PRIVATE_USE_AREA");
    set.add("ARABIC_PRESENTATION_FORMS_A");
    set.add("SPECIALS");
    set.add("SUPPLEMENTARY_PRIVATE_USE_AREA_A");
    set.add("SUPPLEMENTARY_PRIVATE_USE_AREA_B");
    set.add("VARIATION_SELECTORS_SUPPLEMENT");
    set.add("TAGS");
    restricted30CharacterSet = set;
  }

  private OCFFilenameChecker()
  {
    // static util
  }


  public static String checkCompatiblyEscaped(final String str, Report report, EPUBVersion version)
  {
		// don't check remote resources
		if (str.matches("^[^:/?#]+://.*"))
    {
      return "";
    }

    // the test string will be used to compare test result
    String test = checkNonAsciiFilename(str, report);

    if (str.endsWith("."))
    {
      report.message(MessageId.PKG_011, EPUBLocation.create(str));
      test += ".";
    }

    boolean spaces = false;
    final char[] ascciGraphic = new char[]{'<', '>', '"', '{', '}', '|',
        '^', '`', '*', '?' /* , ':','/', '\\' */};
    String result = "";
    char[] chars = str.toCharArray();
    for (char c : chars)
    {
      for (char a : ascciGraphic)
      {
        if (c == a)
        {
          result += "\"" + Character.toString(c) + "\",";
          test += Character.toString(c);
        }
      }
      if (Character.isSpaceChar(c))
      {
        spaces = true;
        test += Character.toString(c);
      }
    }
    if (result.length() > 1)
    {
      result = result.substring(0, result.length() - 1);
      report.message(MessageId.PKG_009, EPUBLocation.create(str), result);
    }
    if (spaces)
    {
      report.message(MessageId.PKG_010, EPUBLocation.create(str));
    }

    if (version == EPUBVersion.VERSION_3)
    {
      checkCompatiblyEscaped30(str, test, report);
    }
    return test;
  }

  private static String checkNonAsciiFilename(final String str, Report report)
  {
    // TODO change this from warning to a compatibility hint message level

    String nonAscii = str.replaceAll("[\\p{ASCII}]", "");
    if (nonAscii.length() > 0)
    {
      report.message(MessageId.PKG_012, EPUBLocation.create(str), nonAscii);
    }
    return nonAscii;
  }

  private static String checkCompatiblyEscaped30(String str, String test, Report report)
  {
    String result = "";

    char[] chars = str.toCharArray();
    for (char c : chars)
    {
      if (Character.isISOControl(c))
      {
        result += "\"" + Character.toString(c) + "\",";
        test += Character.toString(c);
      }

      // DEL (U+007F)
      if (c == '\u007F')
      {
        result += "\"" + Character.toString(c) + "\",";
        test += Character.toString(c);
      }
      String unicodeType = Character.UnicodeBlock.of(c).toString();
      if (restricted30CharacterSet.contains(unicodeType))
      {
        result += "\"" + Character.toString(c) + "\",";
      }
    }
    if (result.length() > 1)
    {
      result = result.substring(0, result.length() - 1);
      report.message(MessageId.PKG_009, EPUBLocation.create(str), result);
    }
    return test;
  }
}

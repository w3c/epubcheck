package com.adobe.epubcheck.ocf;

import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.collect.ImmutableSet;

//FIXME 2022 update related PKG-* messages to contain the file name string
public final class OCFFilenameChecker
{
  private static final Set<String> RESTRICTED_30_CHARACTER_SET = ImmutableSet.of("PRIVATE_USE_AREA",
      "ARABIC_PRESENTATION_FORMS_A", "SPECIALS", "SUPPLEMENTARY_PRIVATE_USE_AREA_A",
      "SUPPLEMENTARY_PRIVATE_USE_AREA_B", "VARIATION_SELECTORS_SUPPLEMENT", "TAGS");

  private final Report report;
  private final EPUBVersion version;
  private final EPUBLocation location;

  public OCFFilenameChecker(ValidationContext context)
  {
    this.report = context.report;
    this.version = context.version;
    this.location = EPUBLocation.of(context);
  }

  public String checkCompatiblyEscaped(final String str)
  {
    // don't check remote resources
    if (str.matches("^[^:/?#]+://.*"))
    {
      return "";
    }

    // the test string will be used to compare test result
    String test = checkNonAsciiFilename(str);

    if (str.endsWith("."))
    {
      report.message(MessageId.PKG_011, location, str);
      test += ".";
    }

    boolean spaces = false;
    final char[] ascciGraphic = new char[] { '<', '>', '"', '{', '}', '|', '^', '`', '*',
        '?' /* , ':','/', '\\' */ };
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
      report.message(MessageId.PKG_009, location, str, result);
    }
    if (spaces)
    {
      report.message(MessageId.PKG_010, location, str);
    }

    if (version == EPUBVersion.VERSION_3)
    {
      checkCompatiblyEscaped30(str, test);
    }
    return test;
  }

  private String checkNonAsciiFilename(final String str)
  {
    String nonAscii = str.replaceAll("[\\p{ASCII}]", "");
    if (nonAscii.length() > 0)
    {
      report.message(MessageId.PKG_012, location, str, nonAscii);
    }
    return nonAscii;
  }

  private String checkCompatiblyEscaped30(String str, String test)
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
      if (RESTRICTED_30_CHARACTER_SET.contains(unicodeType))
      {
        result += "\"" + Character.toString(c) + "\",";
      }
    }
    if (result.length() > 1)
    {
      result = result.substring(0, result.length() - 1);
      report.message(MessageId.PKG_009, location, str, result);
    }
    return test;
  }
}

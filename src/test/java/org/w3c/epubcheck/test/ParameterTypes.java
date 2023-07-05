package org.w3c.epubcheck.test;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.Locale;

import org.hamcrest.Matcher;
import org.w3c.epubcheck.test.TestConfiguration.CheckerMode;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

public class ParameterTypes
{

  private static Function<String, MessageId> TO_ID = new Function<String, MessageId>()
  {

    @Override
    public MessageId apply(String input)
    {
      Preconditions.checkArgument(input != null);
      return MessageId.valueOf(input.replace('-', '_'));
    }

  };
  
  @ParameterType("true|false")
  public Boolean bool(String value) {
    return Boolean.valueOf(value);
  }

  @ParameterType("?i:(full publication|((Media Overlays|Navigation|Package|SVG Content|XHTML Content) Document))")
  public CheckerMode checkerMode(String mode)
  {
    switch (mode.toLowerCase(Locale.ENGLISH))
    {
    case "full publication":
      return CheckerMode.EPUB;
    case "media overlays document":
      return CheckerMode.MEDIA_OVERLAYS_DOC;
    case "navigation document":
      return CheckerMode.NAVIGATION_DOC;
    case "package document":
      return CheckerMode.PACKAGE_DOC;
    case "svg content document":
      return CheckerMode.SVG_CONTENT_DOC;
    case "xhtml content document":
      return CheckerMode.XHTML_CONTENT_DOC;
    default:
      throw new IllegalArgumentException("Unknown file type: " + mode);
    }
  }

  @ParameterType(".*?")
  public Locale locale(String locale)
  {
    try
    {
      return Locale.forLanguageTag(locale);
    } catch (NullPointerException e)
    {
      throw new IllegalArgumentException("Couldn’t set locale: " + locale, e);
    }
  }

  @ParameterType("[A-Z]{3}-[0-9]{3}[a-z]?")
  public MessageId messageId(String id)
  {
    return TO_ID.apply(id);
  }

  @ParameterType("(?: )?(\\d*)(?: time(?:s)?)?")
  public Integer messageQuantity(String times)
  {
    return (times.isEmpty()) ? 1 : Integer.parseInt(times);
  }

  @DataTableType
  public Matcher<? super MessageInfo> messageRow(List<String> row)
  {
    if (row.size() == 1)
    {
      return hasProperty("id", equalTo(MessageId.valueOf(row.get(0).replace('-', '_'))));
    }
    else
    {
      return both(hasProperty("id", equalTo(TO_ID.apply(row.get(0)))))
          .and(hasProperty("message", containsString(row.get(1))));
    }
  }

  @ParameterType(".*?")
  public EPUBProfile profile(String profile)
  {
    try
    {
      return EPUBProfile.valueOf(profile.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException e)
    {
      throw new IllegalArgumentException("Unknown EPUBCheck profile: " + profile, e);
    }
  }

  @ParameterType("?i:(fatal error|error|warning|usage|info)")
  public Severity severity(String severity)
  {
    if ("fatal error".equals(severity)) severity = "fatal";
    return Severity.valueOf(severity.toUpperCase(Locale.ENGLISH));
  }

  @ParameterType("\\d(?:\\.\\d)?(?:\\.\\d)?")
  public EPUBVersion version(String version)
  {
    if (version.equals("3") || version.startsWith("3.")) return EPUBVersion.VERSION_3;
    if (version.equals("2") || version.startsWith("2.")) return EPUBVersion.VERSION_2;
    return EPUBVersion.Unknown;
  }

}
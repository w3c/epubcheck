package com.adobe.epubcheck.ocf;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UForwardCharacterIterator;
import com.ibm.icu.text.UnicodeSet;

public final class OCFFilenameChecker implements Checker
{

  private static final UnicodeSet ASCII = new UnicodeSet("[:ascii:]").freeze();

  private static final UnicodeSet DISALLOWED_EPUB2 = new UnicodeSet()
      // .add(0x002F)// SOLIDUS '/' -- allowed as path separator
      .add(0x0022)// QUOTATION MARK '"'
      .add(0x002A)// ASTERISK '*'
      // .add(0x002E)// FULL STOP '.' -- only disallowed as the last character
      .add(0x003A)// COLON ':'
      .add(0x003C)// LESS-THAN SIGN '<'
      .add(0x003E)// GREATER-THAN SIGN '>'
      .add(0x003F)// QUESTION MARK '?'
      .add(0x005C)// REVERSE SOLIDUS '\'
      .freeze();

  private static final ImmutableMap<String, UnicodeSet> DISALLOWED_EPUB3 = new ImmutableMap.Builder<String, UnicodeSet>()
      .put("ASCII", new UnicodeSet() //
          .addAll(DISALLOWED_EPUB2)// all disallowed in EPUB 2.0.1
          .add(0x007C) // VERTICAL LINE '|'
          .freeze())
      .put("NON CHARACTER", new UnicodeSet("[:Noncharacter_Code_Point=Yes:]")//
          .freeze())
      .put("CONTROL", new UnicodeSet().add(0x007F) // DEL
          .addAll(0x0000, 0x001F) // C0 range
          .addAll(0x0080, 0x009F) // C1 range
          .freeze())
      .put("PRIVATE USE", new UnicodeSet() //
          .addAll(0xE000, 0xF8FF) // Private Use Area
          .addAll(0xF0000, 0xFFFFF) // Supplementary Private Use Area-A
          .addAll(0x100000, 0x10FFFF) // Supplementary Private Use Area-B
          .freeze())
      .put("SPECIALS", new UnicodeSet() //
          .addAll(0xFFF0, 0xFFFF) // Specials Blocks
          .freeze())
      .put("DEPRECATED", new UnicodeSet() //
          .add(0xE0001)// LANGUAGE TAG
          // .add(0xE007F)// CANCEL TAG -- reinstated in Emoji tag sequences
          .freeze())
      .build();

  private static String toString(int codepoint, String setName)
  {
    assert setName != null;
    StringBuilder result = new StringBuilder().append(String.format("U+%04X ", codepoint));
    if ("ASCII".equals(setName))
    {
      result.append('(').append(UCharacter.toString(codepoint)).append(')');
    }
    else
    {
      String characterName = UCharacter.getName(codepoint);
      if (characterName != null)
      {
        result.append(characterName).append(' ');
      }
      result.append('(').append(setName).append(')');
    }
    return result.toString();
  }

  private final Report report;
  private final EPUBVersion version;
  private final EPUBLocation location;
  private final String filename;

  public OCFFilenameChecker(String filename, ValidationContext context)
  {
    this(filename, context, null);
  }

  public OCFFilenameChecker(String filename, ValidationContext context, EPUBLocation location)
  {
    Preconditions.checkArgument(filename != null);
    Preconditions.checkArgument(context != null);
    this.filename = filename;
    this.report = context.report;
    this.version = context.version;
    this.location = (location != null) ? location : EPUBLocation.of(context);
  }

  @Override
  public void check()
  {
    // Iterate through the code points to search disallowed characters
    UCharacterIterator chars = UCharacterIterator.getInstance(filename);
    final Set<String> disallowed = new LinkedHashSet<>();
    boolean hasSpaces = false;
    boolean isASCIIOnly = true;
    int codepoint;
    while ((codepoint = chars.nextCodePoint()) != UForwardCharacterIterator.DONE)
    {
      // Check if the string has non-ASCII characters
      isASCIIOnly = isASCIIOnly && ASCII.contains(codepoint);
      // Check if the string has space characters
      hasSpaces = hasSpaces || UCharacter.isUWhiteSpace(codepoint);
      // Check for disallowed characters
      switch (version)
      {
      case VERSION_2:
        if (DISALLOWED_EPUB2.contains(codepoint))
        {
          disallowed.add(toString(codepoint, "ASCII"));
        }
        break;
      default:
        for (String name : DISALLOWED_EPUB3.keySet())
        {
          if (DISALLOWED_EPUB3.get(name).contains(codepoint))
          {
            disallowed.add(toString(codepoint, name));
            break;
          }
        }
        break;
      }
    }
    // Check that FULL STOP is not used as the last character
    if (chars.previousCodePoint() == 0x002E)
    {
      report.message(MessageId.PKG_011, location, filename);
    }
    // Report if disallowed characters were found
    if (!disallowed.isEmpty())
    {
      report.message(MessageId.PKG_009, location, filename,
          disallowed.stream().collect(Collectors.joining(", ")));
    }
    // Report whitespace characters
    if (hasSpaces)
    {
      report.message(MessageId.PKG_010, location, filename);
    }
    // Report non-ASCII characters as usage
    if (!isASCIIOnly)
    {
      report.message(MessageId.PKG_012, location, filename);
    }
  }

}

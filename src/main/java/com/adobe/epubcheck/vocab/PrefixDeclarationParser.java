package com.adobe.epubcheck.vocab;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.sf.saxon.om.NameChecker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Utility to parse a prefix declaration attribute into a map of prefixes to URI
 * strings.
 * 
 * @author Romain Deltour
 *
 */
public class PrefixDeclarationParser
{

  private enum State
  {
    START(CharMatcher.WHITESPACE, CharMatcher.NONE),
    PREFIX(CharMatcher.WHITESPACE.or(CharMatcher.is(':')).negate(), CharMatcher.ANY),
    PREFIX_END(CharMatcher.is(':'), CharMatcher.ANY),
    SPACE(CharMatcher.WHITESPACE, CharMatcher.is(' ')),
    URI(CharMatcher.WHITESPACE.negate(), CharMatcher.ANY),
    WHITESPACE(CharMatcher.WHITESPACE, CharMatcher.anyOf(" \t\r\n"));

    public final CharMatcher accepted;
    public final CharMatcher allowed;

    private State(CharMatcher accepted, CharMatcher allowed)
    {
      this.accepted = accepted;
      this.allowed = allowed;
    }
  };

  private static EnumSet<State> FINAL_STATES = EnumSet.of(State.START, State.PREFIX, State.WHITESPACE);

  /**
   * Parses a prefix declaration attribute into a map of prefixes to URI
   * strings.
   * 
   * @param value
   *          the string to parse (typically from a <code>prefix</code>
   *          attribute.
   * @param report
   *          to report errors on the fly.
   * @param location
   *          the location of attribute in the validated file.
   * @return a map of prefixes to URI strings.
   */
  public static Map<String, String> parsePrefixMappings(String value, Report report, EPUBLocation location)
  {
    // ---- prefix attribute EBNF ----
    // prefixes = mapping , { whitespace, { whitespace } , mapping } ;
    // mapping = prefix , ":" , space , { space } , ? xsd:anyURI ? ;
    // prefix = ? xsd:NCName ? ;
    // space = #x20 ;
    // whitespace = (#x20 | #x9 | #xD | #xA) ;

    ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
    if (value == null)
      return builder.build();
    StringReader reader = new StringReader(value);
    String prefix = null;
    String uri = null;
    try
    {
      State state = State.START;
      int c;
      String chars = "";
      String badChars;
      while ((c = reader.read()) != -1)
      {
        List<String> parsed = consume(reader, c, state);
        chars = parsed.get(0);
        badChars = parsed.get(1);
        switch (state)
        {
        case START:
          prefix = null;
          uri = null;
          if (!chars.isEmpty())
            report.message(MessageId.OPF_004, location);
          state = State.PREFIX;
          break;
        case PREFIX:
          if (chars.isEmpty())
          {
            // empty prefix
            report.message(MessageId.OPF_004a, location);
          } else if (!NameChecker.isValidNCName(chars))
          {
            // bad prefix
            report.message(MessageId.OPF_004b, location, chars);
          } else
          {
            prefix = chars;
          }
          state = State.PREFIX_END;
          break;
        case PREFIX_END:
          if (chars.isEmpty())
          {
            c = skip(reader, c, CharMatcher.WHITESPACE, State.PREFIX_END.accepted);
            if (((char) c) == ':')
            {
              // some space before the colon char
              report.message(MessageId.OPF_004c, location, prefix);
              state = State.PREFIX_END;
            } else
            {
              // no colon
              report.message(MessageId.OPF_004c, location, prefix);
              state = State.URI;
            }
            prefix = null;
            break;
          }
          state = State.SPACE;
          break;
        case SPACE:
          if (chars.isEmpty())
          {
            // no space
            report.message(MessageId.OPF_004d, location, prefix);
            prefix = null;
          } else if (!badChars.isEmpty())
          {
            // unexpected whitespace
            report.message(MessageId.OPF_004e, location, prefix);

          }
          state = State.URI;
          break;
        case URI:
          try
          {
            uri = new URI(chars).toString();
            if (prefix != null)
              builder.put(prefix, uri);
          } catch (URISyntaxException e)
          {
            // bad URI
            report.message(MessageId.OPF_006, location, chars, prefix);
          }
          prefix = null;
          state = State.WHITESPACE;
          break;
        case WHITESPACE:
          if (!badChars.isEmpty())
            report.message(MessageId.OPF_004f, location, prefix);
          state = State.PREFIX;
          break;
        }
      }
      if (!FINAL_STATES.contains(state))// string ends with a single prefix
        report.message(MessageId.OPF_005, location, prefix);
      if (state == State.PREFIX && !chars.isEmpty())// trailing whitespace
        report.message(MessageId.OPF_004, location);
    } catch (IOException e)
    {
      throw new IllegalStateException(e);// Unexpected
    } finally
    {
      reader.close();
    }
    return builder.build();
  }

  private static List<String> consume(Reader reader, int c, State state)
    throws IOException
  {
    StringBuilder sb = new StringBuilder();
    StringBuilder illegal = new StringBuilder();
    while (c != -1 && state.accepted.matches((char) c))
    {
      reader.mark(1);
      sb.append((char) c);
      if (!state.allowed.matches((char) c))
      {
        illegal.append((char) c);
      }
      reader.mark(1);
      c = reader.read();
    }
    reader.reset();
    return ImmutableList.of(sb.toString(), illegal.toString());
  }

  private static int skip(Reader reader, int c, CharMatcher skipped, CharMatcher stopping)
    throws IOException
  {
    while (c != -1 && !stopping.matches((char) c) && skipped.matches((char) c))
    {
      reader.mark(1);
      c = reader.read();
    }
    reader.reset();
    return c;
  }
}

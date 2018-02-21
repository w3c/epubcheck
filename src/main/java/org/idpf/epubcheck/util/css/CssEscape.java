/*
 * Copyright (c) 2012 International Digital Publishing Forum
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.idpf.epubcheck.util.css;

import static com.google.common.base.Preconditions.checkArgument;
import static org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.SCANNER_PREMATURE_EOF;
import static org.idpf.epubcheck.util.css.CssScanner.HEXCHAR;
import static org.idpf.epubcheck.util.css.CssScanner.WHITESPACE;
import static org.idpf.epubcheck.util.css.CssScanner.isNewLine;

import java.io.IOException;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssToken.TokenBuilder;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

/**
 * Represents a CSS escape sequence.
 *
 * @author mgylling
 */

class CssEscape
{
  static final Optional<CssEscape> ABSENT = Optional.absent();
  private final boolean debug = false;
  private final CssReader reader;
  private final TokenBuilder err;

  /**
   * The original escape sequence
   */
  private CharSequence sequence;

  /**
   * The character resulting from unescaping the original escape sequence
   */
  int character;

  /**
   * Constructor.
   *
   * @param reader A CssReader whose current char is the backslash.
   * @param err    token builder
   */

  CssEscape(final CssReader reader, final TokenBuilder err)
  {
    this.reader = reader;
    this.err = err;
  }

  Optional<CssEscape> create() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkArgument(reader.curChar == '\\', "must be backslash, was: %s", (char) reader.curChar);
    }
    /*
       * The incoming is either a hex escape or single char escape, max 8 chars long
       */
    StringBuilder sb = new StringBuilder();
    int[] eight = reader.peek(8);
    int first = eight[0];

    if (first == -1)
    {
      err.error(SCANNER_PREMATURE_EOF, reader);
      return Optional.absent();
    }
    else if (isNewLine(eight) > 0)
    {
      //"a backslash followed by a newline stands by itself"
      return Optional.absent();
    }

    if (CssScanner.HEXCHAR.matches((char) first))
    {
      //a hex escape, max six chars, + optionally single whitespace or cr+lf

      boolean seenSpace = false;

      for (int cur : eight)
      {
        if (cur == -1)
        {
          break;
        }
        char ch = (char) cur;

        boolean isHexChar = HEXCHAR.matches(ch);
        boolean isSpace = WHITESPACE.matches(ch);
        if (!isHexChar && !isSpace)
        {
          break;
        }

        if (HEXCHAR.matches(ch) && !seenSpace && sb.length() < 6)
        {
          sb.append((char) cur);
        }

        if (isSpace)
        {
          if (!seenSpace)
          {
            sb.append((char) cur);
            if (cur == '\r')
            {
              seenSpace = true;
            }
            else
            {
              break;
            }
          }
          else
          {
            //we have a prev space which is \r
            if (cur == '\f')
            {
              sb.append((char) cur);
            }
            break;
          }
        }
      }

      character = Integer.parseInt(WHITESPACE.trimTrailingFrom(sb.toString()), 16);
      sb.insert(0, '\\');
      sequence = sb.toString();

    }
    else
    {
      //a single char escape
      sb.append('\\').append((char) eight[0]);
      sequence = sb.toString();
      character = eight[0];
    }

    return Optional.of(this);

  }


  /**
   * Render this escape.
   *
   * @param builder   The TokenBuilder to render into
   * @param asLiteral If given matcher matches this escapes literal, then render as literal, else as escape.
   * @return the length of the token in the input character stream
   */
  int render(TokenBuilder builder, CharMatcher asLiteral)
  {
    char ch = (char) character;
    if (asLiteral.matches(ch))
    {
      builder.append(ch);
    }
    else
    {
      //TODO could normalize space end chars
      builder.append(sequence);
    }
    return sequence.length() - 1;
  }

  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(this)
        .add("escaped", sequence)
        .add("unescaped", (char) character)
        .toString();
  }

}
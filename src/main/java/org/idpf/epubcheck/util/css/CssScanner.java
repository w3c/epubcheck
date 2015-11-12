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

import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssReader.Mark;
import org.idpf.epubcheck.util.css.CssToken.CssTokenConsumer;
import org.idpf.epubcheck.util.css.CssToken.TokenBuilder;
import org.idpf.epubcheck.util.css.CssToken.Type;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.*;
import static org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.SCANNER_ILLEGAL_CHAR;
import static org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.SCANNER_PREMATURE_EOF;

/**
 * A lexical scanner for CSS.
 * <p>
 * Lexical errors are stored as attributes on the tokens in which they occurred.
 * The supplied CssErrorHandler is also invoked when a lexical error occurs, so
 * that clients can terminate the scanning by a rethrow.
 * </p>
 *
 * @author mgylling
 */

final class CssScanner
{

  private final CssReader reader;
  private final CssToken.CssTokenConsumer consumer;
  private final CssEscapeMemoizer escapes;
  private TokenBuilder builder;
  private final CssErrorHandler errHandler;
  private final boolean debug = false;
  private char cur;
  private Locale locale;

  private CssScanner(final Reader in, final String systemID, final CssErrorHandler errHandler,
      final CssTokenConsumer consumer, final int pushbackBufferSize, final Locale locale)
  {
    this.consumer = checkNotNull(consumer);
    this.errHandler = checkNotNull(errHandler);
    this.reader = new CssReader(in, systemID, pushbackBufferSize);
    this.escapes = new CssEscapeMemoizer(reader);
    this.locale = locale;
  }

  CssScanner(Reader in, final String systemID, final CssErrorHandler errHandler,
      final CssTokenConsumer consumer, final Locale locale)
  {
    this(in, systemID, errHandler, consumer, CssReader.DEFAULT_PUSHBACK_BUFFER_SIZE, locale);
  }

  void scan() throws
      IOException,
      CssException
  {
    int ch;
    int next;
    while (true)
    {
      ch = reader.next();
      if (ch == -1)
      {
        break;
      }
      builder = new TokenBuilder(reader, /* this, */errHandler, locale);
      cur = (char) ch;
      next = reader.peek();
      escapes.reset(builder);

      if (WHITESPACE.matches(cur))
      {
        _ws();
      }
      else if (cur == '-' && equals(reader.peek(2), CDC_LL))
      {
        _cdc();
      }
      else if (O.matches(cur) && matchesOrEOF(reader.at(4), WHITESPACE)
          && equals(reader.peek(3), ONLY_LL, true))
      {
        _only();
      }
      else if (N.matches(cur) && matchesOrEOF(reader.at(3), WHITESPACE)
          && equals(reader.peek(2), NOT_LL, true))
      {
        _not();
      }
      else if (A.matches(cur) && matchesOrEOF(reader.at(3), WHITESPACE)
          && equals(reader.peek(2), AND_LL, true))
      {
        _and();
      }
      else if (U.matches(cur) && equals(reader.peek(3), URI_LL, true))
      {
        _uri();
      }
      else if (U.matches(cur) && next == '+'
          && matchesOrEOF(reader.at(2), URANGESTART))
      {
        _urange();
      }
      else if (NMSTART.matches(cur) || cur == '-'
          && matches(next, NMSTART) || escapes.get(0).isPresent()
          || (cur == '-' && escapes.get(1).isPresent()))
      {
        _ident();
        if (reader.peek() == '(')
        {
          _function();
        }
      }
      else if (cur == '@'
          && ((matches(next, NMSTART) || escapes.get(1).isPresent()) || (next == '-'
          && (matches(reader.at(2), NMSTART)) || escapes.get(
          2).isPresent())))
      {
        _atkeyword();
      }
      else if (NUMEND.matches(cur) || NUMSTART.matches(cur)
          && matches(next, NUMEND) || UNARY.matches(cur)
          && next == '.' && matches(reader.at(2), NUMEND))
      {
        _num();
      }
      else if (cur == '<' && equals(reader.peek(3), CDO_LL))
      {
        _cdo();
      }
      else if (cur == '/' && next == '*')
      {
        _comment();
      }
      else if (QUOTES.matches(cur))
      {
        _string();
      }
      else if (cur == '#'
          && (matches(next, NMCHAR) || escapes.get(1).isPresent()))
      {
        _hashname();
      }
      else if (cur == '.'
          && (matches(next, NMCHAR) || escapes.get(1).isPresent()))
      {
        _classname();
      }
      else if (cur == '!'
          && forwardMatch("important", true, false))
      {
        _important();
      }
      else if (cur == '~' && next == '=')
      {
        _includes();
      }
      else if (cur == '|' && next == '=')
      {
        _dashmatch();
      }
      else if (cur == '^' && next == '=')
      {
        _prefixmatch();
      }
      else if (cur == '$' && next == '=')
      {
        _suffixmatch();
      }
      else if (cur == '*' && next == '=')
      {
        _substringmatch();
      }
      else
      {
        builder.type = Type.CHAR;
        builder.append(cur);
      }
      consumer.add(builder.asToken());
    }
  }

  /**
   * FUNCTION {ident}\(
   *
   * @throws IOException
   */
  private void _function() throws
      IOException
  {
    // Assumes that builder already contains an IDENT
    if (debug)
    {
      checkArgument('(' == reader.peek());
    }
    builder.type = Type.FUNCTION;
    builder.append(reader.next());
  }

  /**
   * URI url\({w}{string}{w}\) |
   * url\({w}([!#$%&*-\[\]-~]|{nonascii}|{escape})*{w}\)
   */
  private void _uri() throws
      IOException,
      CssException
  {
    /*
       * Need to run this before IDENT and FUNCTION since the first three
       * chars match IDENT and the entire trigger token matches FUNCTION.
       */

    builder.append("url(");
    reader.forward(3);
    if (debug)
    {
      checkArgument('(' == reader.curChar || -1 == reader.peek());
    }
    reader.forward(NOT_WHITESPACE);
    int uristart = reader.next();

    if (-1 == uristart)
    {
      builder.error(SCANNER_PREMATURE_EOF, reader);
    }
    else if (QUOTES.matches((char) uristart))
    {
      builder.append('\'');
      _string();
      builder.append('\'');

      if (debug && builder.errors.size() < 1)
      {
        checkArgument(QUOTES.matches((char) reader.curChar));
      }
      reader.forward(NOT_WHITESPACE);
      if (reader.peek() > -1)
      {
        reader.next(); // ')'
      }
    }
    else
    {
      // unquoted uri
      builder.append(uristart);
      if (debug)
      {
        checkArgument(NOT_WHITESPACE.matches((char) reader.curChar));
      }
      StringBuilder buf = new StringBuilder();
      while (true)
      {
        CssReader.Mark mark = reader.mark();
        int ch = reader.next();
        if (ch == -1)
        {
          builder.error(SCANNER_PREMATURE_EOF, reader);
          reader.unread(ch, mark);
          break;
        }
        else if (ch == ')')
        {
          break;
        }
        buf.append((char) ch);
      }
      builder.append(WHITESPACE.trimTrailingFrom(buf.toString()));
    }
    builder.append(')');
    builder.type = Type.URI;

    if (')' != reader.curChar && builder.errors.size() == 0)
    {
      builder.error(CssErrorCode.SCANNER_ILLEGAL_SYNTAX, reader, reader.curChar);
    }
  }

  /**
   * string1 \"([^\n\r\f\\"]|\\{nl}|{escape})*\"
   * string2 \'([^\n\r\f\\']|\\{nl}|{escape})*\'
   */
  private void _string() throws
      IOException,
      CssException
  {

    if (debug)
    {
      checkState(QUOTES.matches((char) reader.curChar));
    }

    /*
       * Note: resulting token excludes start+end quotes
       */

    builder.type = Type.STRING;
    int quoteType = reader.curChar;

    // in strings, we let escapes in general pass through
    while (true)
    {
      CssReader.Mark mark = reader.mark();
      int ch = reader.next();
      if (ch == -1)
      {
        builder.error(SCANNER_PREMATURE_EOF, reader);
        reader.unread(ch, mark);
        break;
      }
      else if (ch == '\n' || ch == '\r' || ch == '\f')
      {
        builder.error(SCANNER_ILLEGAL_CHAR, reader, "NEWLINE",
            Type.STRING.name());
        reader.forward(TERMINATOR);
        break;
      }
      else if (ch == '\\')
      {
        int[] peek = reader.peek(2);
        int nl = isNewLine(peek);
        if (nl > 0)
        {
          // in strings, ignore backslash followed by a literal
          // newline
          reader.forward(nl);
          continue;
        }

      }
      else if (ch == quoteType && reader.prevChar != '\\')
      {
        break;
      }

      builder.append(ch);
    }

    if (debug && builder.errors.size() == 0)
    {
      checkState(QUOTES.matches((char) reader.curChar));
    }
  }

  /**
   * ATKEYWORD '@'[-]?{nmstart}{nmchar}* nmstart [_a-z]|{nonascii}|{escape}
   * nmchar [_a-z0-9-]|{nonascii}|{escape}
   *
   * @throws CssException
   */
  private void _atkeyword() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkState('@' == reader.curChar);
    }

    builder.type = Type.ATKEYWORD;
    builder.append(cur); // @
    append(NMSTART);
    append(NMCHAR);

    if (debug)
    {
      int nxt = reader.peek();
      checkState(NMCHAR.matches((char) reader.curChar));
      if (nxt > -1)
      {
        checkState(!NMCHAR.matches((char) nxt));
      }
    }
  }

  /**
   * IDENT [-]?{nmstart}{nmchar}*
   */
  private void _ident() throws
      IOException,
      CssException
  {

    builder.type = Type.IDENT;

    Optional<CssEscape> esc = escapes.get(0);

    // first NMSTART char or '-'
    if (esc.isPresent())
    {
      int length = esc.get().render(builder, NMSTART);
      reader.forward(length);
    }
    else
    {
      builder.append(cur);
    }

    if (cur == '-' || (esc.isPresent() && esc.get().character == '-'))
    {
      // The NMSTART that matched in the main loop if clause
      if (escapes.get(1).isPresent())
      {
        reader.forward(escapes.get(1).get().render(builder, NMSTART));
      }
      else
      {
        builder.append(reader.next());
      }

    }
    append(NMCHAR);
  }

  /**
   * DASHMATCH |=
   */
  private void _dashmatch() throws
      IOException
  {
    if (debug)
    {
      checkState(reader.curChar == '|');
    }
    builder.type = Type.DASHMATCH;
    builder.append("|=");
    reader.next();
    if (debug)
    {
      checkState(reader.curChar == '=');
    }
  }

  /**
   * INCLUDES ~=
   */
  private void _includes() throws
      IOException
  {
    if (debug)
    {
      checkState(reader.curChar == '~');
    }
    builder.type = Type.INCLUDES;
    builder.append("~=");
    reader.next();
    if (debug)
    {
      checkState(reader.curChar == '=');
    }
  }

  /**
   * PREFIXMATCH ^=
   */
  private void _prefixmatch() throws
      IOException
  {
    if (debug)
    {
      checkState(reader.curChar == '^');
    }
    builder.type = Type.PREFIXMATCH;
    builder.append("^=");
    reader.next();
    if (debug)
    {
      checkState(reader.curChar == '=');
    }
  }

  /**
   * SUFFIXMATCH $=
   */
  private void _suffixmatch() throws
      IOException
  {
    if (debug)
    {
      checkState(reader.curChar == '$');
    }
    builder.type = Type.SUFFIXMATCH;
    builder.append("$=");
    reader.next();
    if (debug)
    {
      checkState(reader.curChar == '=');
    }
  }

  /**
   * SUBSTRINGMATCH *=
   */
  private void _substringmatch() throws
      IOException
  {
    if (debug)
    {
      checkState(reader.curChar == '*');
    }
    builder.type = Type.SUBSTRINGMATCH;
    builder.append("*=");
    reader.next();
    if (debug)
    {
      checkState(reader.curChar == '=');
    }
  }

  /**
   * HASHNAME "#"{name} name {nmchar}+ [_a-z0-9-]|{nonascii}|{escape}
   *
   * @throws CssException
   */
  private void _hashname() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkState(reader.curChar == '#');
      checkState(NMCHAR.matches((char) reader.peek()) || isNextEscape());
    }
    builder.type = Type.HASHNAME;
    builder.append('#');
    append(NMCHAR);
  }

  /**
   * CLASSNAME "."{name} This is not part of formal lexical constructs, but
   * seems to be safe to do at scanner level. name {nmchar}+
   * [_a-z0-9-]|{nonascii}|{escape}
   *
   * @throws CssException
   */
  private void _classname() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkState(reader.curChar == '.');
      checkState(NMCHAR.matches((char) reader.peek()) || isNextEscape());
    }
    builder.type = Type.CLASSNAME;
    builder.append('.');
    append(NMCHAR);
  }

  /**
   * IMPORTANT !{w}important
   */
  private void _important()
  {
    /*
       * Note that #lex needs to use #forwardMatch to maintain correct
       * position
       */
    builder.type = Type.IMPORTANT;
    builder.append("!important");
  }

  /**
   * Builds a comment token, excluding the leading and trailing comment
   * tokens.
   */
  private void _comment() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkState(reader.curChar == '/' && reader.peek() == '*');
    }
    /*
       * badcomment1 \/\*[^*]*\*+([^/*][^*]*\*+)* badcomment2
       * \/\*[^*]*(\*+[^/*][^*]*)* comment \/\*[^*]*\*+([^/*][^*]*\*+)*\/
       *
       *
       * "comments can not nest" just close at first occurrence of comment
       * close and let the grammar level handle reporting
       */

    builder.type = Type.COMMENT;

    reader.next(); // '*'

    while (true)
    {
      Mark mark = reader.mark();
      int ch = reader.next();
      if (ch == -1)
      {
        builder.error(SCANNER_PREMATURE_EOF, reader);
        reader.unread(ch, mark);
        break;
      }
      else if (ch == '*' && reader.peek() == '/')
      {
        reader.next();
        break;
      }
      else
      {
        builder.append(ch);
      }
    }

    if (debug && builder.errors.size() < 1)
    {
      checkState('/' == reader.curChar && '*' == reader.prevChar);
    }

  }

  private void _cdo() throws
      IOException
  {
    if (debug)
    {
      checkState('<' == reader.curChar);
    }
    builder.type = Type.CDO;
    builder.append("<!--");
    reader.forward(3);

    if (debug)
    {
      checkState('-' == reader.curChar && '-' == reader.prevChar);
    }
  }

  private void _num() throws
      IOException,
      CssException
  {

    /*
       * NUM [0-9]+|[0-9]*\.[0-9]+
       */

    if (debug)
    {
      checkState(NUMSTART.matches(cur));
    }

    builder.type = Type.INTEGER;
    builder.append(cur);
    if (cur == '.')
    {
      builder.type = Type.NUMBER;
    }

    while (true)
    {
      Mark mark = reader.mark();
      int nm = reader.next();
      if (nm == -1)
      {
        if (builder.getLength() == 1 && builder.getLast() == '.')
        {
          builder.type = Type.CHAR;
        }
        reader.unread(nm, mark);
        break;
      }
      else if (!NUM.matches((char) nm))
      {
        reader.unread(nm, mark);
        break;
      }
      else if (nm == '.' && !NUMEND.matches((char) reader.peek()))
      {
        reader.unread(nm, mark);
        break;
      }
      builder.append(nm);

      if (nm == '.')
      {
        builder.type = Type.NUMBER;
      }
    }

    int qnt = reader.peek();
    if (qnt > -1 && (QNTSTART.matches((char) qnt)) || isNextEscape())
    {
      // this num is the start of a quantity literal
      _quantity();
    }
  }

  /**
   * With incoming builder containing a valid NUMBER, and next char being a
   * valid QNTSTART, modify the type and append to the builder
   */
  private void _quantity() throws
      IOException,
      CssException
  {

    if (debug)
    {
      int ch = reader.peek();
      checkState(QNTSTART.matches((char) ch) || isNextEscape());
      checkState(builder.getLength() > 0
          && NUM.matches(builder.getLast()));
    }

    /*
       * Assume we have a {num}{ident} instance (DIMEN), and then override
       * that if a specific quantity literal is found.
       */
    builder.type = Type.QNTY_DIMEN;
    TokenBuilder suffix = new TokenBuilder(reader, errHandler, locale);
    append(QNTSTART, suffix);
    if (suffix.getLast() != '%')
    { // QNTSTART = NMSTART | '%'
      append(NMCHAR, suffix);
    }

    if (suffix.getLength() > QNT_TOKEN_MAXLENGTH)
    {
      // longer than max length in quantities map
      builder.append(suffix.toString());
      return;
    }

    // shorter or equal to max length in quantities map
    // we might have a more specific match
    final int[] ident = suffix.toArray();
    int[] match = null;

    for (int[] test : quantities.keySet())
    {
      if (equals(ident, test, true))
      {
        builder.type = quantities.get(test);
        match = test;
        break;
      }
    }

    if (builder.type == Type.QNTY_DIMEN)
    {
      builder.append(ident);
    }
    else
    {
      if (debug)
      {
        checkState(match != null);
      }
      builder.append(match);
    }

  }

  private void _and() throws
      IOException
  {
    /*
       * Need to run this before IDENT since the token alse matches IDENT
       * Whitespace as terminator required: see prose under example XX in MQ
       * spec
       */
    if (debug)
    {
      checkArgument('A' == reader.curChar || 'a' == reader.curChar);
    }

    builder.type = Type.AND;
    builder.append("and");
    reader.forward(2);

    if (debug)
    {
      checkArgument(('d' == reader.curChar || 'D' == reader.curChar)
          && (reader.peek() == -1 || WHITESPACE.matches((char) reader
          .peek())));
    }
  }

  private void _not() throws
      IOException
  {
    /*
       * Need to run this before IDENT since the token also matches IDENT
       * Whitespace as terminator required: see prose under example XX in MQ
       * spec
       */

    if (debug)
    {
      checkArgument('N' == reader.curChar || 'n' == reader.curChar);
    }

    builder.type = Type.NOT;
    builder.append("not");
    reader.forward(2);

    if (debug)
    {
      checkArgument(('t' == reader.curChar || 'T' == reader.curChar)
          && (reader.peek() == -1 || WHITESPACE.matches((char) reader
          .peek())));
    }
  }

  private void _only() throws
      IOException
  {
    /*
       * Need to run this before IDENT since the token else matches IDENT
       * Whitespace as terminator required: see prose under example XX in MQ
       * spec
       */
    if (debug)
    {
      checkArgument('o' == reader.curChar || 'O' == reader.curChar);
    }

    builder.type = Type.ONLY;
    builder.append("only");
    reader.forward(3);

    if (debug)
    {
      checkArgument(('y' == reader.curChar || 'Y' == reader.curChar)
          && (reader.peek() == -1 || WHITESPACE.matches((char) reader
          .peek())));
    }
  }

  private void _cdc() throws
      IOException
  {
    /*
       * Need to run this before IDENT since the first two chars match IDENT.
       */

    if (debug)
    {
      checkArgument('-' == reader.curChar);
    }

    builder.type = Type.CDC;
    builder.append("-->");
    reader.forward(2);

    if (debug)
    {
      checkArgument('>' == reader.curChar && '-' == reader.prevChar);
    }
  }

  /**
   * Whitespace w ::= wc wc ::= #x9 | #xA | #xC | #xD | #x20
   */
  private void _ws() throws
      IOException
  {
    builder.type = Type.S;
    // no need to preserve exact whitespace
    // but append an S char to keep the token
    // contract of always having a value
    builder.append(' ');
    reader.forward(NOT_WHITESPACE);

    if (debug)
    {
      int nxt = reader.peek();
      checkArgument(WHITESPACE.matches((char) reader.curChar));
      if (nxt > -1)
      {
        checkArgument(NOT_WHITESPACE.matches((char) nxt));
      }
    }
  }

  private static final int QNT_TOKEN_MAXLENGTH = 4; // update when adding more
  // to map
  private static final Map<int[], Type> quantities = new ImmutableMap.Builder<int[], Type>()
      .put(new int[]{'d', 'p', 'c', 'm'}, Type.QNTY_RESOLUTION)
      .put(new int[]{'d', 'p', 'p', 'x'}, Type.QNTY_RESOLUTION)
      .put(new int[]{'g', 'r', 'a', 'd'}, Type.QNTY_ANGLE)
      .put(new int[]{'t', 'u', 'r', 'n'}, Type.QNTY_ANGLE)
      .put(new int[]{'v', 'm', 'i', 'n'}, Type.QNTY_LENGTH)
      .put(new int[]{'d', 'e', 'g'}, Type.QNTY_ANGLE)
      .put(new int[]{'k', 'h', 'z'}, Type.QNTY_FREQ)
      .put(new int[]{'r', 'a', 'd'}, Type.QNTY_ANGLE)
      .put(new int[]{'r', 'e', 'm'}, Type.QNTY_REMS)
      .put(new int[]{'d', 'p', 'i'}, Type.QNTY_RESOLUTION)
      .put(new int[]{'e', 'm'}, Type.QNTY_EMS)
      .put(new int[]{'c', 'm'}, Type.QNTY_LENGTH)
      .put(new int[]{'p', 'x'}, Type.QNTY_LENGTH)
      .put(new int[]{'m', 'm'}, Type.QNTY_LENGTH)
      .put(new int[]{'i', 'n'}, Type.QNTY_LENGTH)
      .put(new int[]{'p', 't'}, Type.QNTY_LENGTH)
      .put(new int[]{'p', 'c'}, Type.QNTY_LENGTH)
      .put(new int[]{'c', 'h'}, Type.QNTY_LENGTH)
      .put(new int[]{'v', 'w'}, Type.QNTY_LENGTH)
      .put(new int[]{'v', 'h'}, Type.QNTY_LENGTH)
      .put(new int[]{'e', 'x'}, Type.QNTY_EXS)
      .put(new int[]{'m', 's'}, Type.QNTY_TIME)
      .put(new int[]{'h', 'z'}, Type.QNTY_FREQ)
      .put(new int[]{'%'}, Type.QNTY_PERCENTAGE)
      .put(new int[]{'s'}, Type.QNTY_TIME).build();

  /**
   * Builds a UNICODE_RANGE token.
   */
  private void _urange() throws
      IOException,
      CssException
  {
    if (debug)
    {
      checkArgument((reader.curChar == 'U' || reader.curChar == 'u')
          && reader.peek() == '+');
    }

    builder.type = Type.URANGE;
    reader.next(); // '+'

    List<Integer> cbuf = Lists.newArrayList();

    int count = 0;

    while (true)
    {
      Mark mark = reader.mark();
      int ch = reader.next();

      if (ch == -1)
      {
        reader.unread(ch, mark);
        break;
      }

      if (URANGECHAR.matches((char) ch))
      {
        count = ch == '-' ? 0 : count + 1;
        if (count == 7)
        {
          builder.error(CssErrorCode.SCANNER_ILLEGAL_URANGE, reader,
              "U+" + toString(cbuf) + (char) ch);
        }
        cbuf.add(ch);
      }
      else
      {
        reader.unread(ch, mark);
        break;
      }
    }
    builder.append("U+");
    builder.append(Ints.toArray(cbuf));
  }

  /**
   * Returns true if reader next() is the start of a valid escape sequence.
   *
   * @return whether or not the reader is at the start of a valid escape sequence.
   * @throws IOException
   */
  private boolean isNextEscape() throws
      IOException
  {
    boolean result = false;
    Mark mark = reader.mark();
    int ch = reader.next();
    if (ch == '\\')
    {
      try
      {
        Optional<CssEscape> esc = new CssEscape(reader, builder)
            .create();
        result = esc.isPresent();
      }
      catch (CssException ignore)
      {

      }
    }
    reader.unread(ch, mark);
    return result;
  }

  /**
   * Parse forward and append to the TokenBuilder field all characters that
   * match matcher, or until the next character is EOF. Escapes are included
   * verbatim if they don't match matcher, else literal.
   *
   * @throws IOException
   * @throws CssException
   */
  private void append(CharMatcher matcher) throws
      IOException,
      CssException
  {
    append(matcher, builder);
  }

  /**
   * Parse forward and append to the supplied builder all characters that
   * match matcher, or until the next character is EOF. Escapes are included
   * verbatim if they don't match matcher, else literal.
   */
  private void append(CharMatcher matcher, TokenBuilder builder)
      throws
      IOException,
      CssException
  {
    while (true)
    {
      Mark mark = reader.mark();
      int ch = reader.next();
      if (ch > -1 && matcher.matches((char) ch))
      {
        builder.append(ch);
      }
      else if (ch == '\\')
      {
        Optional<CssEscape> escape = new CssEscape(reader, builder)
            .create();
        if (escape.isPresent())
        {
          reader.forward(escape.get().render(builder, matcher));
        }
        else
        {
          reader.unread(ch, mark);
          break;
        }
      }
      else
      {
        reader.unread(ch, mark);
        break;
      }
    }
  }

  /**
   * Check if a forward scan will equal given match string
   *
   * @param match       The string to match
   * @param ignoreCase  Whether case should be ignored
   * @param resetOnTrue Whether the reader should be reset on found match
   * @throws IOException
   */
  private boolean forwardMatch(String match, boolean ignoreCase, boolean resetOnTrue)
      throws
      IOException
  {
    Mark mark = reader.mark();
    List<Integer> cbuf = Lists.newArrayList();
    StringBuilder builder = new StringBuilder();

    boolean result = true;
    boolean seenChar = false;

    while (true)
    {
      cbuf.add(reader.next());

      char ch = (char) reader.curChar;

      if (reader.curChar == -1)
      {
        result = false;
        break;
      }
      else if (WHITESPACE.matches(ch))
      {
        if (seenChar)
        {
          builder.append(ch);
        }
      }
      else
      {
        if (builder.length() == 0)
        {
          seenChar = true;
        }
        builder.append(ch);
        int index = builder.length() - 1;

        if (!ignoreCase
            && (builder.charAt(index) == match.charAt(index)))
        {
          result = false;
          break;
        }

        if (ignoreCase
            && (Ascii.toLowerCase(builder.charAt(index)) != Ascii
            .toLowerCase(match.charAt(index))))
        {
          result = false;
          break;
        }
      }

      if (builder.length() == match.length())
      {
        if (!match.equalsIgnoreCase(builder.toString()))
        {
          result = false;
        }
        break;
      }
    }

    if (!result || resetOnTrue)
    {
      reader.unread(cbuf, mark);
    }

    return result;
  }

  private String toString(List<Integer> ints)
  {
    StringBuilder builder = new StringBuilder();
    for (int i : ints)
    {
      builder.append((char) i);
    }
    return builder.toString();
  }

  /**
   * Like Arrays.equals, but does not return true when both are null.
   */
  private static boolean equals(int[] a, int[] b)
  {
    return equals(a, b, false);
  }

  /**
   * Like Arrays.equals, but does not return true when both are null.
   *
   * @param ignoreAsciiCase If true, ascii case differences are ignored.
   */
  private static boolean equals(int[] a, int[] b, boolean ignoreAsciiCase)
  {
    if ((a == null && b == null) || a == null || b == null
        || (a.length != b.length))
    {
      return false;
    }

    for (int i = 0; i < a.length; i++)
    {
      if (!ignoreAsciiCase)
      {
        if (a[i] != b[i])
        {
          return false;
        }
      }
      else
      {
        if (a[i] == -1 && b[i] == -1)
        {
        }
        else if (a[i] == -1 || b[i] == -1)
        {
          return false;
        }
        else if (Ascii.toLowerCase((char) a[i]) != Ascii
            .toLowerCase((char) b[i]))
        {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Return true if ch represents EOF (-1), or if it matches matcher.
   */
  private static boolean matchesOrEOF(final int ch, CharMatcher matcher)
  {
    return ch == -1 || matcher.matches((char) ch);
  }

  /**
   * Return true if ch matches matcher, false if not or if ch represents EOF
   * (-1).
   */
  private static boolean matches(final int ch, CharMatcher matcher)
  {
    return ch != -1 && matcher.matches((char) ch);
  }

  /**
   * Determine whether a sequence of chars begin with a CSS newline.
   *
   * @param chars An array with minimum two characters
   * @return 0 if there is no newline, else 1 or 2, representing the newline
   *         length in characters.
   */
  static int isNewLine(int[] chars)
  {
    checkArgument(chars.length > 1);
    // nl \n|\r\n|\r|\f
    if (chars[0] == '\r' && chars[1] == '\n')
    {
      return 2;
    }
    else if (chars[0] == '\n' || chars[0] == '\r' || chars[0] == '\f')
    {
      return 1;
    }
    return 0;
  }

  static final CharMatcher WHITESPACE = CharMatcher.anyOf(" \t\n\r\f")
      .precomputed();
  private static final CharMatcher NOT_WHITESPACE = WHITESPACE.negate().precomputed();
  static final CharMatcher QUOTES = CharMatcher.anyOf("\"'").precomputed();
  private static final CharMatcher U = CharMatcher.anyOf("Uu").precomputed();
  private static final CharMatcher O = CharMatcher.anyOf("Oo").precomputed();
  private static final CharMatcher N = CharMatcher.anyOf("Nn").precomputed();
  private static final CharMatcher A = CharMatcher.anyOf("Aa").precomputed();

  /**
   * {nmstart} excluding {escape}
   */
  private static final CharMatcher NMSTART = CharMatcher
      .inRange('A', 'Z')
      .or(CharMatcher.inRange('a', 'z').or(
          CharMatcher.is('_').or(
              CharMatcher.inRange('\u0080', '\uFFFF'))))
      .precomputed();

  /**
   * {nmchar} excluding {escape}
   */
  private static final CharMatcher NMCHAR = NMSTART.or(
      CharMatcher.inRange('0', '9').or(CharMatcher.is('-')))
      .precomputed();

  /**
   * start of quantities that followed after {num} excluding {escape}
   */
  private static final CharMatcher QNTSTART = NMSTART.or(CharMatcher.is('%'))
      .precomputed();

  /**
   * {num} end char cannot be period
   */
  private static final CharMatcher NUMEND = CharMatcher.inRange('0', '9')
      .precomputed();

  /**
   * {num}
   */
  private static final CharMatcher NUM = NUMEND.or(CharMatcher.is('.'))
      .precomputed();

  private static final CharMatcher UNARY = CharMatcher.anyOf("+-")
      .precomputed();

  /**
   * {num} start char can be unary operators
   */
  private static final CharMatcher NUMSTART = NUM.or(UNARY).precomputed();

  static final CharMatcher HEXCHAR = CharMatcher.anyOf(
      "AaBbCcDdEeFf0123456789").precomputed();

  private static final CharMatcher URANGESTART = HEXCHAR.or(CharMatcher
      .anyOf("?"));

  private static final CharMatcher URANGECHAR = HEXCHAR.or(CharMatcher
      .anyOf("?-"));

  static final CharMatcher TERMINATOR = CharMatcher.anyOf(";}{");

  /**
   * The three last characters in the CDO token
   */
  private static final int[] CDO_LL = new int[]{'!', '-', '-'}; // <!--

  /**
   * The two last characters in the CDC token
   */
  private static final int[] CDC_LL = new int[]{'-', '>'}; // -->

  /**
   * The three last characters in the URI start token
   */
  private static final int[] URI_LL = new int[]{'r', 'l', '('};

  /**
   * The three last characters in the ONLY token
   */
  private static final int[] ONLY_LL = new int[]{'n', 'l', 'y'};

  /**
   * The three last characters in the NOT token
   */
  private static final int[] NOT_LL = new int[]{'o', 't'};

  /**
   * The three last characters in the AND token
   */
  private static final int[] AND_LL = new int[]{'n', 'd'};

  /**
   * Memoizer for escapes at forward reader positions. Owner must invoke
   * reset() every time the reader position changes (excluding closured
   * unreads).
   */
  static class CssEscapeMemoizer
  {
    private final Map<Integer, Optional<CssEscape>> map = Maps.newHashMap();
    private TokenBuilder errFunnel;
    private final CssReader reader;

    CssEscapeMemoizer(final CssReader reader)
    {
      this.reader = reader;
    }

    CssEscapeMemoizer reset(final TokenBuilder errFunnel)
    {
      map.clear();
      this.errFunnel = errFunnel;
      return this;
    }

    Optional<CssEscape> get(final int n) throws
        IOException
    {
      checkNotNull(errFunnel);
      if (!map.containsKey(n))
      {
        map.put(n, create(n));
      }
      return map.get(n);
    }

    private Optional<CssEscape> create(int n) throws
        IOException
    {
      List<Integer> cbuf = Lists.newArrayList();
      Mark mark = reader.mark();

      for (int i = 0; i < n; i++)
      {
        int ch = reader.next();
        cbuf.add(ch);
        if (ch == -1)
        {
          reader.unread(cbuf, mark);
          return CssEscape.ABSENT;
        }
      }

      if (reader.curChar == '\\')
      {
        try
        {
          Optional<CssEscape> esc = new CssEscape(reader, errFunnel)
              .create();
          reader.unread(cbuf, mark);
          return esc;
        }
        catch (CssException ignore)
        {

        }
      }
      reader.unread(cbuf, mark);
      return CssEscape.ABSENT;

    }
  }
}

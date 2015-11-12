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

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssScannerException;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Represents a CSS token.
 *
 * @author mgylling
 * @see CssTokenList
 */
final class CssToken
{
  final Type type;
  final CssLocation location;
  final String chars;     //for tokens with string values
  Optional<List<CssScannerException>> errors = Optional.absent();

  /**
   * Constructor for tokens with type other than CHAR
   */
  CssToken(final Type type, final CssLocation location, final String chars,
      final List<CssScannerException> errors)
  {
    this.type = type;
    this.location = location;
    this.chars = chars;
    if (errors != null)
    {
      this.errors = Optional.of(errors);
    }
  }

  /**
   * Constructor for CHAR tokens
   */
  CssToken(final Type type, final CssLocation location, final char chr,
      final List<CssScannerException> errors)
  {
    this.type = type;
    this.location = location;
    this.chars = String.valueOf(chr);
    if (errors != null)
    {
      this.errors = Optional.of(errors);
    }
  }

  public Type getType()
  {
    return type;
  }

  public CssLocation getLocation()
  {
    return location;
  }

  public String getChars()
  {
    return chars;
  }

  public char getChar()
  {
    checkState(chars.length() == 1);
    return chars.charAt(0);
  }

  boolean hasErrors()
  {
    return errors.isPresent();
  }

  public Optional<Iterator<CssScannerException>> getErrors()
  {
    if (hasErrors())
    {
      return Optional.of(errors.get().iterator());
    }
    return Optional.absent();
  }

  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(this).add("type", type.name()).add("value", chars)
        .add("errors", errors.isPresent() ? Joiner.on(", ").join(errors.get()) : "none").toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof CssToken)
    {
      CssToken tk = (CssToken) obj;
      if (tk.type.equals(this.type) && tk.chars.equals(this.chars)
          && tk.location.equals(this.location))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Token types.
   */
  public static enum Type
  {
    S,
    COMMENT,
    CDO,         //		<!--
    CDC,         //		-->
    IDENT,         // 		[-]?{nmstart}{nmchar}*
    FUNCTION,      //		[-]?{nmstart}{nmchar}*[(]
    ATKEYWORD,       // 		[@][-]?{nmstart}{nmchar}*
    NUMBER,        //		[0-9]+|[0-9]*\.[0-9]+
    INTEGER,      //		[0-9]+
    STRING,        //
    URI,        //		url\({w}{string}{w}\) | url\({w}([!#$%&*-\[\]-~]|{nonascii}|{escape})*{w}\)
    HASHNAME,      //		"#"{name}
    CLASSNAME,      //		"."{name}
    URANGE,        //

    INCLUDES,       //		"~="
    DASHMATCH,       //		"|="
    PREFIXMATCH,    //		"^="
    SUFFIXMATCH,    //		"$="
    SUBSTRINGMATCH,    //		"*="

    QNTY_DIMEN,      //		{num}{ident}
    QNTY_PERCENTAGE,  //		{num}%
    QNTY_LENGTH,     //		{num}cm, {num}px, {num}mm, {num}in, {num}pt, {num}pc
    QNTY_EMS,      //		{num}em
    QNTY_EXS,      //		{num}ex
    QNTY_ANGLE,      //		{num}deg, {num}rad, {num}grad
    QNTY_TIME,      //		{num}ms, {num}s
    QNTY_FREQ,      //		{num}Hz, {num}kHz
    QNTY_RESOLUTION,  //		{num}{D}{P}{I}, {num}{D}{P}{C}{M}
    QNTY_REMS,      //		{num}rem

    ONLY,        //		MediaQueries
    NOT,        //		MediaQueries
    AND,        //		MediaQueries

    IMPORTANT,      //		"!{w}important"
    CHAR,          //		any other character and not ' or "

    ;
  }

  /**
   * Token predicate matchers.
   */
  static final class Matchers
  {

    /**
     * Matches a CssToken.Type.CHAR with values ';' or '{'
     */
    static final Predicate<CssToken> MATCH_SEMI_OPENBRACE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR
            && (input.getChar() == ';' || input.getChar() == '{');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with values ';' or '}'
     */
    static final Predicate<CssToken> MATCH_SEMI_CLOSEBRACE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR
            && (input.getChar() == ';' || input.getChar() == '}');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with values ',' or '}'
     */
    static final Predicate<CssToken> MATCH_COMMA_OPENBRACE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR
            && (input.getChar() == ',' || input.getChar() == '{');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value ':'
     */
    static final Predicate<CssToken> MATCH_COLON = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == ':');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '|'
     */
    static final Predicate<CssToken> MATCH_PIPE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '|');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '}'
     */
    static final Predicate<CssToken> MATCH_CLOSEBRACE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '}');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '{'
     */
    static final Predicate<CssToken> MATCH_OPENBRACE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '{');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '>', '+' or '~'. Note that S is the
     * fourth CSS combinator which is not matched here.
     */
    static final Predicate<CssToken> MATCH_COMBINATOR_CHAR = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR
            && (input.getChar() == '>' || input.getChar() == '+' || input.getChar() == '~');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value ';'
     */
    static final Predicate<CssToken> MATCH_SEMI = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == ';');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value ','
     */
    static final Predicate<CssToken> MATCH_COMMA = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == ',');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value ')'
     */
    static final Predicate<CssToken> MATCH_CLOSEPAREN = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == ')');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '('
     */
    static final Predicate<CssToken> MATCH_OPENPAREN = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '(');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '*' or '|'
     */
    static final Predicate<CssToken> MATCH_STAR_PIPE = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR
            && (input.getChar() == '*' || input.getChar() == '|');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '*'
     */
    static final Predicate<CssToken> MATCH_STAR = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '*');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value '['
     */
    static final Predicate<CssToken> MATCH_OPENSQUAREBRACKET = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == '[');
      }
    };

    /**
     * Matches a CssToken.Type.CHAR with value ']'
     */
    static final Predicate<CssToken> MATCH_CLOSESQUAREBRACKET = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.CHAR && (input.getChar() == ']');
      }
    };

    /**
     * Matches CssToken.Type.IDENT and CssToken.Type.STRING
     */
    static final Predicate<CssToken> MATCH_STRING_IDENT = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return input.type == CssToken.Type.IDENT || input.type == CssToken.Type.STRING;

      }
    };

    static final Predicate<CssToken> MATCH_ATTRIBUTE_SELECTOR_MATCHERS = new Predicate<CssToken>()
    {
      public final boolean apply(final CssToken input)
      {
        return (input.type == CssToken.Type.CHAR && input.getChar() == '=')
            || input.type == CssToken.Type.INCLUDES || input.type == CssToken.Type.DASHMATCH
            || input.type == CssToken.Type.PREFIXMATCH || input.type == CssToken.Type.SUFFIXMATCH
            || input.type == CssToken.Type.SUBSTRINGMATCH;

      }
    };
  }

  static class TokenBuilder
  {
    Type type;
    final int line;
    final int col;
    final int offset;
    final String systemID;
    private final StringBuilder chars;
    final List<CssScannerException> errors;
    private final boolean debug = false;
    private final CssErrorHandler errorListener;
    private final Locale locale;

    private TokenBuilder(final String systemID, final int line, final int col, final int offset,
        final CssErrorHandler errorListener, final Locale locale)
    {
      this.systemID = systemID;
      this.line = line;
      this.col = col;
      this.offset = offset;
      this.chars = new StringBuilder();
      this.errors = Lists.newArrayList();
      this.errorListener = errorListener;
      this.locale = locale;
    }

    TokenBuilder(final CssReader reader, final CssErrorHandler errorListener, final Locale locale)
    {
      this(reader.systemID, reader.line, reader.col, reader.offset, errorListener, locale);
    }

    TokenBuilder append(int ch)
    {
      chars.append((char) ch);
      return this;
    }

    TokenBuilder append(CharSequence str)
    {
      chars.append(str);
      return this;
    }

    int getLength()
    {
      return chars.length();
    }

    char getLast()
    {
      return chars.charAt(chars.length() - 1);
    }

    TokenBuilder append(int[] chrs)
    {
      for (int chr : chrs)
      {
        append(chr);
      }
      return this;
    }

    /**
     * All lexer-time errors are funnelled through this method. Reported errors are
     * stored in the resulting CssToken. This method also passes the error on to a
     * CssErrorHandler, which can opt to rethrow to terminate the scanning.
     */
    void error(CssErrorCode errorCode, CssReader reader, Object... arguments)
      throws CssException
    {
      CssScannerException cse = new CssScannerException(errorCode, CssLocation.create(reader),
          locale, arguments);
      errors.add(cse);
      errorListener.error(cse);
    }

    CssToken asToken()
    {
      String value = chars.toString();
      if (debug)
      {
        checkState(type != null);
        if (type == Type.STRING || type == Type.COMMENT)
        {
          // empty STRING and COMMENT tokens are not forbidden
          checkState(value != null);
        }
        else
        {
          checkState(!Strings.isNullOrEmpty(value));
        }
      }
      return new CssToken(type, new CssLocation(line, col, offset, systemID), value, errors);
    }

    /**
     * Return the chars appended so far to this builder.
     */
    int[] toArray()
    {
      int[] arr = new int[chars.length()];
      for (int i = 0; i < chars.length(); i++)
      {
        arr[i] = chars.charAt(i);
      }
      return arr;
    }

    /**
     * Return the chars appended so far to this builder.
     */
    @Override
    public String toString()
    {
      return chars.toString();
    }

  }

  interface CssTokenConsumer
  {
    public void add(CssToken token);
  }
}

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

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Exception types and error codes.
 *
 * @author mgylling
 */
public final class CssExceptions
{

  public enum CssErrorCode
  {
    SCANNER_ILLEGAL_SYNTAX(SCANNER_TOKEN_SYNTAX),         //a general error code, below refines
    SCANNER_ILLEGAL_CHAR(SCANNER_TOKEN_SYNTAX + ".char"),
    SCANNER_ILLEGAL_FIRSTCHAR(SCANNER_TOKEN_SYNTAX + ".firstChar"),
    SCANNER_MALFORMED_ESCAPE(SCANNER_TOKEN_SYNTAX + ".escape"),
    SCANNER_ILLEGAL_URANGE(SCANNER_TOKEN_SYNTAX + ".urange"),
    SCANNER_PREMATURE_EOF(SCANNER + ".prematureEOF"),

    GRAMMAR_PREMATURE_EOF(GRAMMAR + ".prematureEOF"),
    GRAMMAR_UNEXPECTED_TOKEN(GRAMMAR_TOKEN + ".unexpected"),
    GRAMMAR_EXPECTING_TOKEN(GRAMMAR_TOKEN + ".expecting"),
    GRAMMAR_INVALID_SELECTOR(GRAMMAR + ".invalidSelector"),;

    final String value;

    CssErrorCode(String value)
    {
      this.value = value;
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this.getClass()).addValue(value).toString();
    }

  }


  /**
   * An exception with grammatical origins.
   */
  static class CssGrammarException extends CssException
  {
    CssGrammarException(final CssErrorCode errorCode, final CssLocation location, final Object... arguments)
    {
      super(errorCode, location, arguments);
    }

    private static final long serialVersionUID = -7470976690623543450L;
  }

  /**
   * An exception with lexical origins.
   */
  static class CssScannerException extends CssException
  {

    CssScannerException(final CssToken token, final CssErrorCode errorCode, final CssLocation location, final Object... arguments)
    {
      super(token, errorCode, location, arguments);
    }

    CssScannerException(CssErrorCode errorCode, CssLocation location, Object... arguments)
    {
      super(errorCode, location, arguments);
    }

    private static final long serialVersionUID = 7105109387886737631L;
  }

  public static abstract class CssException extends Exception
  {
    final CssErrorCode errorCode;
    final CssLocation location;
    final Optional<CssToken> token;

    CssException(final CssToken token, final CssErrorCode errorCode, final CssLocation location, final Object... arguments)
    {
      super(Messages.get(errorCode.value, arguments));
      this.errorCode = checkNotNull(errorCode);
      this.location = checkNotNull(location);
      this.token = token == null ? absent : Optional.of(token);
    }

    CssException(final CssErrorCode errorCode, final CssLocation location, final Object... arguments)
    {
      this(null, errorCode, location, arguments);
    }

    public CssErrorCode getErrorCode()
    {
      return errorCode;
    }

    public CssLocation getLocation()
    {
      return location;
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this.getClass())
          .add("errorCode", errorCode)
          .add("location", location.toString())
          .toString();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof CssException)
      {
        CssException exc = (CssException) obj;
        if (exc.errorCode.equals(this.errorCode)
            && exc.location.equals(this.location))
        {
          return true;
        }
      }
      return false;
    }

    private static final long serialVersionUID = -4635263495562931206L;
  }

  private final static String SCANNER = "css.scanner";
  private final static String SCANNER_TOKEN_SYNTAX = SCANNER + ".token.syntax";
  private final static String GRAMMAR = "css.grammar";
  private final static String GRAMMAR_TOKEN = GRAMMAR + ".token";

  private static final Optional<CssToken> absent = Optional.absent();

}

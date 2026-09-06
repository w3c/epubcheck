package org.w3c.epubcheck.util.mime;

import static org.w3c.epubcheck.util.infra.CodePoints.isHTTPQuotedStringTokenCodePoint;
import static org.w3c.epubcheck.util.infra.CodePoints.isHTTPTokenCodePoint;
import static org.w3c.epubcheck.util.infra.CodePoints.isHTTPWhitespace;

import java.util.PrimitiveIterator.OfInt;

/**
 * Parses a MIME type according to MIME Sniffing
 * See https://mimesniff.spec.whatwg.org/#mime-type
 * 
 * Conforming to living standard update: 17 July 2026
 */
public final class MIMETypeParser
{

  public static enum ParseError
  {
    // Failures
    NULL,
    NO_TYPE,
    NO_SUBTYPE,
    ILLEGAL_CODE_POINT,
    // Recoverable
    UNEXPECTED_CODE_POINT,
    NO_PARAM_NAME,
    NO_PARAM_VALUE,
    END_QUOTE_MISSING;
  }

  public interface ErrorHandler
  {
    public void failure(ParseError error, int position);

    public void error(ParseError error, int position);
  }

  public static class DefaultErrorHandler implements ErrorHandler
  {

    @Override
    public void failure(ParseError error, int position)
    {
    }

    @Override
    public void error(ParseError error, int position)
    {
    }

  }

  private static enum State
  {
    TYPE,
    SUBTYPE,
    SUBTYPE_FINALIZE,
    PARAM_NAME,
    PARAM_VALUE,
    PARAM_VALUE_FINALIZE,
    PARAM_VALUE_QUOTED,
    PARAM_VALUE_QUOTED_ESCAPE,
    PARAM_VALUE_QUOTED_FINALIZE,
    PARAM_FINALIZE,
    SKIP_PARAM,
  }

  private final ErrorHandler errorHandler;

  public MIMETypeParser()
  {
    this(new DefaultErrorHandler());
  }

  public MIMETypeParser(ErrorHandler errorHandler)
  {
    this.errorHandler = (errorHandler != null) ? errorHandler : new DefaultErrorHandler();
  }

  @SuppressWarnings("null")
  public MIMEType parse(String string)
  {
    if (string == null)
    {
      errorHandler.failure(ParseError.NULL, -1);
      return null;
    }
    if (string.isEmpty())
    {
      errorHandler.failure(ParseError.NO_TYPE, -1);
      return null;
    }

    MIMEType.Builder builder = new MIMEType.Builder();

    OfInt input = string.codePoints().iterator();
    StringBuilder type = new StringBuilder();
    StringBuilder subtype = null;
    StringBuilder pname = null;
    StringBuilder pvalue = null;
    State state = State.TYPE;
    boolean consume = true;
    int c = 0;
    int position = 0;
    int wsstart = -1;
    while (!consume || input.hasNext())
    {
      if (consume)
      {
        c = input.nextInt();
      }
      consume = true;
      switch (state)
      {
      case TYPE:
        // skip leading whitespace
        if (type.length() == 0 && isHTTPWhitespace(c))
        {
        }
        // for conforming characters, build up the type value
        else if (isHTTPTokenCodePoint(c))
        {
          type.appendCodePoint(Character.toLowerCase(c));
        }
        // end of the type part, move on to parsing the sub-type
        else if (c == '/')
        {
          if (type.length() == 0) // abort early
          {
            errorHandler.failure(ParseError.NO_TYPE, position);
            return null;
          }
          state = State.SUBTYPE;
          subtype = new StringBuilder();
        }
        // for invalid code points, return failure
        else
        {
          errorHandler.failure(ParseError.ILLEGAL_CODE_POINT, position);
          return null;
        }
        break;
      case SUBTYPE:
        // space after the '/', return failure
        if (subtype.length() == 0 && isHTTPWhitespace(c))
        {
          errorHandler.failure(ParseError.ILLEGAL_CODE_POINT, position);
          return null;
        }
        // for conforming characters, build up the sub-type
        if (isHTTPTokenCodePoint(c))
        {
          subtype.appendCodePoint(Character.toLowerCase(c));
        }
        else // we reached ';', whitespace, or invalid code point
        {
          state = State.SUBTYPE_FINALIZE;
          consume = false;
        }
        break;
      case SUBTYPE_FINALIZE:
        // skip trailing whitespace
        if (isHTTPWhitespace(c))
        {
        }
        // if we reached parameters, move on to parsing parameters
        else if (c == ';')
        {
          if (subtype.length() == 0) // abort early
          {
            errorHandler.failure(ParseError.NO_SUBTYPE, position);
            return null;
          }
          state = State.PARAM_NAME;
          pname = new StringBuilder();
        }
        // invalid code point, return failure
        else
        {
          errorHandler.failure(ParseError.ILLEGAL_CODE_POINT, position);
          return null;
        }
        break;
      case PARAM_NAME:
        // skip leading whitespace
        if (pname.length() == 0 && isHTTPWhitespace(c))
        {
        }
        // for conforming characters, build up the parameter name
        else if (isHTTPTokenCodePoint(c))
        {
          pname.appendCodePoint(Character.toLowerCase(c));
        }
        // early end of declaration, report and skip to next parameter
        else if (c == ';')
        {
          errorHandler.error(
              (pname.length() == 0) ? ParseError.NO_PARAM_NAME : ParseError.NO_PARAM_VALUE,
              position);
          state = State.PARAM_NAME;
          pname = new StringBuilder();
        }
        // invalid code point, report and skip to next parameter
        else if (c != '=')
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          state = State.SKIP_PARAM;
        }
        // at this point we reached the end of the name (c == '=')
        else if (pname.length() == 0)
        {
          errorHandler.error(ParseError.NO_PARAM_NAME, position - 1);
          state = State.SKIP_PARAM;
        }
        else // move on to parsing the parameter value
        {
          state = State.PARAM_VALUE;
          pvalue = new StringBuilder();
        }
        break;
      case PARAM_VALUE:
        if (pvalue.length() == 0 && c == '"')
        {
          state = State.PARAM_VALUE_QUOTED;
        }
        // for conforming characters, build up the parameter value
        else if (isHTTPTokenCodePoint(c))
        {
          pvalue.appendCodePoint(c);
          // we found whitespace earlier, report and continue
          if (wsstart >= 0)
          {
            errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, wsstart);
            wsstart = -1;
          }
        }
        else if (isHTTPWhitespace(c))
        {
          if (pvalue.length() == 0)
          {
            errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          }
          pvalue.appendCodePoint(c);
          wsstart = (wsstart < 0) ? position : wsstart;
        }
        // for conforming quoted characters, report and build up the value
        else if (c != ';' && isHTTPQuotedStringTokenCodePoint(c))
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          pvalue.appendCodePoint(c);
          wsstart = -1;
        }
        // else switch to finalization
        else
        {
          state = State.PARAM_VALUE_FINALIZE;
          consume = false;
        }
        break;
      case PARAM_VALUE_FINALIZE:
        // early end of declaration, report and skip to next parameter
        if (c == ';' && pvalue.length() == 0)
        {
          errorHandler.error(ParseError.NO_PARAM_VALUE, position);
          state = State.PARAM_NAME;
          pname = new StringBuilder();
        }
        // end of declaration, finalize the parameter
        else if (c == ';')
        {
          // remove trailing whitespace, if any
          if (wsstart >= 0) pvalue.delete(pvalue.length() - position + wsstart, pvalue.length());
          state = State.PARAM_FINALIZE;
          consume = false;
        }
        // invalid code point, report and skip to next parameter
        else
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          state = State.SKIP_PARAM;
        }
        wsstart = -1;
        break;
      case PARAM_VALUE_QUOTED:
        if (c == '"')
        {
          state = State.PARAM_VALUE_QUOTED_FINALIZE;
        }
        else if (c == '\\')
        {
          if (!input.hasNext())
          {
            pvalue.append('\\');
          }
          state = State.PARAM_VALUE_QUOTED_ESCAPE;
        }
        // for other conforming characters, build up the param value
        else if (isHTTPQuotedStringTokenCodePoint(c))
        {
          pvalue.appendCodePoint(c);
        }
        else
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          state = State.SKIP_PARAM;
        }
        break;
      case PARAM_VALUE_QUOTED_ESCAPE:
        if (isHTTPQuotedStringTokenCodePoint(c))
        {
          pvalue.appendCodePoint(c);
          state = State.PARAM_VALUE_QUOTED;
        }
        else
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
          state = State.SKIP_PARAM;
        }
        break;
      case PARAM_VALUE_QUOTED_FINALIZE:
        // end of declaration, finalize parameter
        if (c == ';')
        {
          state = State.PARAM_FINALIZE;
          consume = false;
        }
        // report trailing code points and continue
        else if (!isHTTPWhitespace(c))
        {
          errorHandler.error(ParseError.UNEXPECTED_CODE_POINT, position);
        }
        // skip trailing whitespace
        break;
      case PARAM_FINALIZE:
        assert pname.length() > 0;
        builder.param(pname.toString(), pvalue.toString());
        state = State.PARAM_NAME;
        pname = new StringBuilder();
        break;
      case SKIP_PARAM:
        if (pname.length() > 0)
        {
          pname = new StringBuilder();
        }
        if (c == ';')
        {
          state = State.PARAM_NAME;
        }
        break;
      }
      if (consume)
      {
        position++;
      }
    }
    // Return failures if the MIME type is not complete
    if (type.length() == 0)
    {
      errorHandler.failure(ParseError.NO_TYPE, position);
      return null;
    }
    else if (subtype == null || subtype.length() == 0)
    {
      errorHandler.failure(ParseError.NO_SUBTYPE, position);
      return null;
    }
    // Finalize the last param
    switch (state)
    {
    case PARAM_NAME:
      // invalid ending state, report and continue
      errorHandler.error(
          (pname.length() == 0) ? ParseError.NO_PARAM_NAME : ParseError.NO_PARAM_VALUE,
          position);
      break;
    case PARAM_VALUE_QUOTED:
    case PARAM_VALUE_QUOTED_ESCAPE:
      // the quote was not closed, report and continue
      errorHandler.error(ParseError.END_QUOTE_MISSING, position);
    case PARAM_VALUE_QUOTED_FINALIZE:
      // build the last parameter, value can be empty
      assert pname.length() > 0;
      builder.param(pname.toString(), pvalue.toString());
      break;
    case PARAM_VALUE:
    case PARAM_VALUE_FINALIZE:
      if (c == '=')
      {
        errorHandler.error(ParseError.NO_PARAM_VALUE, position);
      }
      else
      {
        if (wsstart >= 0) pvalue.delete(pvalue.length() - position + wsstart, pvalue.length());
        // remove trailing whitespace, if any
        builder.param(pname.toString(), pvalue.toString());
      }
    default:
      break;
    }
    return builder.type(type.toString()).subtype(subtype.toString()).build();
  }
}

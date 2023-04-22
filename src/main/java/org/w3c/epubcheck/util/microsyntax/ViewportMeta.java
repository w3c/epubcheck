package org.w3c.epubcheck.util.microsyntax;

import static org.w3c.epubcheck.util.infra.InfraUtil.isASCIIWhitespace;

import java.nio.CharBuffer;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class ViewportMeta
{
  private static Pattern VIEWPORT_HEIGHT_REGEX = Pattern.compile("\\d+(\\.\\d+)?|device-height");
  private static Pattern VIEWPORT_WIDTH_REGEX = Pattern.compile("\\d+(\\.\\d+)?|device-width");

  public static ViewportMeta parse(String string, ErrorHandler errorHandler)
  {
    return new Parser(errorHandler).parse(string);
  }

  public static boolean isValidProperty(String name, String value)
  {
    Preconditions.checkNotNull(value);
    switch (Preconditions.checkNotNull(name))
    {
    case "width":
      
      return VIEWPORT_WIDTH_REGEX.matcher(value).matches();
    case "height":
      return VIEWPORT_HEIGHT_REGEX.matcher(value).matches();
    default:
      return true;
    }
  }

  public static enum ParseError
  {
    NULL_OR_EMPTY,
    NAME_EMPTY,
    VALUE_EMPTY,
    LEADING_SEPARATOR,
    TRAILING_SEPARATOR,
    ASSIGN_UNEXPECTED
  }

  /**
   * An error handler for the `srcset` parser
   */
  public interface ErrorHandler
  {
    public void error(ParseError error, int position);
  }

  private final static class Builder
  {
    public ImmutableListMultimap.Builder<String, String> properties = ImmutableListMultimap
        .builder();

    public ViewportMeta build()
    {
      return new ViewportMeta(this);
    }

    public void withProperty(String name, String value)
    {
      Preconditions.checkArgument(name != null);
      Preconditions.checkArgument(value != null);
      properties.put(name, value);
    }

  }

  private final static class Parser
  {
    private static enum State
    {
      NAME,
      ASSIGN,
      VALUE,
      SEPARATOR,
      SPACE_OR_SEPARATOR
    }

    private final ErrorHandler errorHandler;

    public Parser(ErrorHandler errorHandler)
    {
      this.errorHandler = errorHandler;
    }

    private void error(ParseError code, int position)
    {
      if (errorHandler != null) errorHandler.error(code, position);
    }

    public ViewportMeta parse(CharSequence string)
    {
      Builder builder = new Builder();
      if (string == null || string.length() == 0)
      {
        error(ParseError.NULL_OR_EMPTY, -1);
        return builder.build();
      }
      CharBuffer input = CharBuffer.wrap(string);
      StringBuilder name = new StringBuilder();
      StringBuilder value = new StringBuilder();
      State state = State.NAME;
      boolean consume = true;
      char c = ' ';
      while (!consume || input.hasRemaining())
      {
        if (consume)
        {
          c = input.get();
        }
        else
        {
          consume = true;
        }
        switch (state)
        {
        case NAME:
          if (isASCIIWhitespace(c) && name.length() == 0)
          {
            // skip leading whitespace
          }
          else if (c == '=' || isASCIIWhitespace(c))
          {
            state = State.ASSIGN;
            consume = false;
          }
          else if (c == ',' || c == ';')
          {
            state = State.SEPARATOR;
            consume = false;
          }
          else
          {
            name.append(c);
          }
          break;
        case ASSIGN:
          if (name.length()==0) {
            // assign state but no name was found
            error(ParseError.NAME_EMPTY, input.position());
            return builder.build();
          }
          else if (isASCIIWhitespace(c))
          {
            // skip whitespace
          }
          else if (c == '=')
          {
            state = State.VALUE;
          }
          else if (c == ',' || c == ';')
          {
            state = State.SEPARATOR;
            consume = false;
          }
          else
          {
            // no '=' was matched (i.e. no value is set)
            error(ParseError.VALUE_EMPTY, input.position());
            return builder.build();
          }
          break;
        case VALUE:
          if (isASCIIWhitespace(c) && value.length() == 0)
          {
            // skip whitespace, the value hasn't started
          }
          else if (c == ',' || c == ';' || isASCIIWhitespace(c))
          {
            if (value.length() == 0)
            {
              error(ParseError.VALUE_EMPTY, input.position());
              return builder.build();
            }
            state = State.SPACE_OR_SEPARATOR;
            consume = false;
          }
          else if (c == '=')
          {
            error(ParseError.ASSIGN_UNEXPECTED, input.position());
            return builder.build();
          }
          else
          {
            value.append(c);
          }
          break;
        case SPACE_OR_SEPARATOR:
          if (isASCIIWhitespace(c))
          {
            // skip whitespace
          }
          else
          {
            state = State.SEPARATOR;
            consume = false;
          }
        case SEPARATOR:
          if (name.length() == 0)
          {
            error(ParseError.LEADING_SEPARATOR, input.position());
            return builder.build();
          }
          if (c == ',' || c == ';' || isASCIIWhitespace(c))
          {
            // skip repeating separators
          }
          else
          {
            builder.withProperty(name.toString(), value.toString());
            name = new StringBuilder();
            value = new StringBuilder();
            state = State.NAME;
            consume = false;
          }
          break;
        }
      }
      // finalize, report if unexpected final state
      if (state == State.VALUE && value.length() == 0)
      {
        error(ParseError.VALUE_EMPTY, input.position());
      } else {
        builder.withProperty(name.toString(), value.toString());
      }
      if (state == State.SEPARATOR)
      {
        error(ParseError.TRAILING_SEPARATOR, input.position());
      }
      return builder.build();
    }
  }

  private final ImmutableListMultimap<String, String> properties;

  private ViewportMeta(Builder builder)
  {
    this.properties = builder.properties.build();
  }

  public boolean hasProperty(String name)
  {
    return properties.containsKey(name);
  }

  public List<String> getValues(String name)
  {
    return properties.get(name);
  }

  public ListMultimap<String, String> asMultimap()
  {
    return properties;
  }

}

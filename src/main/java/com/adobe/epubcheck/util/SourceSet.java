package com.adobe.epubcheck.util;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Represents an image source set. See https://html.spec.whatwg.org/#source-set
 */
public final class SourceSet
{

  /**
   * Represent an image source. See https://html.spec.whatwg.org/#image-source
   */
  public final static class ImageSource
  {
    public final String url;
    public final Optional<Integer> width;
    public final Optional<Float> density;

    private ImageSource(String url, Optional<Integer> width, Optional<Float> density)
    {
      this.url = url;
      this.width = width;
      this.density = density;
    }

    /**
     * Returns a new image source.
     * 
     * @param url
     *          the source URL, must not be {@code null} or empty.
     * @param width
     *          a width descriptor, will be absent if {@code null} or negative
     * @param density
     *          a density descriptor, will be absent if {@code null} or negative
     * @return a new image source
     */
    public static ImageSource create(String url, int width, float density)
    {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(url));
      return new ImageSource(url,
          Optional.<Integer> fromNullable((width < 0) ? null : Integer.valueOf(width)),
          Optional.<Float> fromNullable((density < 0) ? null : Float.valueOf(density)));
    }
  }

  /**
   * Codes for the various parsing errors. See
   * https://html.spec.whatwg.org/#concept-microsyntax-parse-error
   */
  public static enum ParseError
  {
    NULL_OR_EMPTY,
    EMPTY_START,
    EMPTY_MIDDLE,
    DESCRIPTOR_MIX_WIDTH_DENSITY,
    DESCRIPTOR_DENSITY_MORE_THAN_ONCE,
    DESCRIPTOR_DENSITY_NEGATIVE,
    DESCRIPTOR_DENSITY_NOT_FLOAT,
    DESCRIPTOR_WIDTH_MORE_THAN_ONCE,
    DESCRIPTOR_WIDTH_SIGNED,
    DESCRIPTOR_WIDTH_NOT_INT,
    DESCRIPTOR_WIDTH_ZERO,
    DESCRIPTOR_FUTURE_H_ZERO,
    DESCRIPTOR_FUTURE_H_MORE_THAN_ONCE,
    DESCRIPTOR_FUTURE_H_NOT_INT,
    DESCRIPTOR_FUTURE_H_WITHOUT_WIDTH,
    DESCRIPTOR_FUTURE_H_WITH_DENSITY,
    DESCRIPTOR_UNKNOWN_SUFFIX,
  }

  /**
   * An error handler for the `srcset` parser
   */
  public interface ErrorHandler
  {
    public void error(ParseError error, int position);
  }

  /**
   * Parses a `srcset` value and swallows the errors. See
   * https://html.spec.whatwg.org/#parsing-a-srcset-attribute
   */
  public static SourceSet parse(String srcset)
  {
    return parse(srcset, null);
  }

  /**
   * Parses a `srcset` value and reports the errors to the given
   * {@link ErrorHandler}.
   */
  public static SourceSet parse(String srcset, ErrorHandler errorHandler)
  {
    return new Parser(errorHandler).parse(srcset);
  }

  /**
   * The `srcser` parser implementation.
   * 
   * Folows the logic of the "parsing a srcset attribute" algorithm, see
   * https://html.spec.whatwg.org/#parsing-a-srcset-attribute
   */
  private final static class Parser
  {
    private static enum State
    {
      SPLIT,
      COLLECT_URL,
      TOKENIZE_DESCRIPTORS,
      IN_DESCRIPTOR,
      AFTER_DESCRIPTOR,
      IN_PARENS,
    }

    private static Pattern POSITIVE_FLOAT = Pattern
        .compile("(\\d+(\\.\\d+)?|\\.\\d+)([eE][-+]?\\d+)?");

    private final ErrorHandler errorHandler;

    public Parser(ErrorHandler errorHandler)
    {
      this.errorHandler = errorHandler;
    }

    public SourceSet parse(CharSequence srcset)
    {
      Builder builder = new Builder();
      if (srcset == null || srcset.length() == 0)
      {
        error(ParseError.NULL_OR_EMPTY, -1);
        return builder.build();
      }
      CharBuffer input = CharBuffer.wrap(srcset);
      List<String> descriptors = new LinkedList<>();
      StringBuilder url = new StringBuilder();
      StringBuilder descriptor = new StringBuilder();
      State state = State.SPLIT;
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
        case SPLIT:
          assert (url.length() == 0);
          if (isASCIIWhitespace(c))
          {
            // skip whitespace
          }
          else if (c == ',')
          {
            if (!builder.hasSources())
            {
              error(ParseError.EMPTY_START, input.position());
            }
            else
            {
              error(ParseError.EMPTY_MIDDLE, input.position());
            }
          }
          else
          {
            state = State.COLLECT_URL;
            consume = false;
          }
          break;
        case COLLECT_URL:
          if (isASCIIWhitespace(c))
          {
            if (url.charAt(url.length() - 1) == ',')
            {
              builder.add(finalizeSource(url, descriptors));
              state = State.SPLIT;
            }
            else
            {
              state = State.TOKENIZE_DESCRIPTORS;
            }
          }
          else
          {
            url.append(c);
          }
          break;
        case TOKENIZE_DESCRIPTORS:
          if (isASCIIWhitespace(c))
          {
            // skip whitespace
          }
          else
          {
            state = State.IN_DESCRIPTOR;
            consume = false;
          }
          break;
        case IN_DESCRIPTOR:
          if (isASCIIWhitespace(c))
          {
            if (descriptor.length() > 0)
            {
              descriptors.add(descriptor.toString());
              descriptor.setLength(0);
            }
            state = State.AFTER_DESCRIPTOR;
          }
          else if (c == ',')
          {
            if (descriptor.length() > 0)
            {
              descriptors.add(descriptor.toString());
              descriptor.setLength(0);
            }
            builder.add(finalizeSource(url, descriptors));
            state = State.SPLIT;
          }
          else if (c == '(')
          {
            descriptor.append(c);
            state = State.IN_PARENS;
          }
          else
          {
            descriptor.append(c);
          }
          break;
        case IN_PARENS:
          if (c == ')')
          {
            descriptor.append(c);
            state = State.IN_DESCRIPTOR;
          }
          else
          {
            descriptor.append(c);
          }
          break;
        case AFTER_DESCRIPTOR:
          if (!isASCIIWhitespace(c))
          {
            state = State.IN_DESCRIPTOR;
            consume = false;
          }
          break;
        }
      }
      // add the latest descriptor if not already added (EOF)
      if (descriptor.length() > 0)
      {
        descriptors.add(descriptor.toString());
      }
      builder.add(finalizeSource(url, descriptors));
      return builder.build();
    }

    /**
     * if a character is https://infra.spec.whatwg.org/#ascii-whitespace U+0009 TAB,
     * U+000A LF, U+000C FF, U+000D CR, or U+0020 SPACE.
     */
    private static boolean isASCIIWhitespace(char c)
    {
      return c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r';
    }

    /**
     * Remove trailing commas and return the count of commas removed
     */
    private static int removeTrailingCommas(StringBuilder sb)
    {
      int count = 0;
      while (sb.charAt(sb.length() - 1) == ',')
      {
        sb.deleteCharAt(sb.length() - 1);
        count++;
      }
      return count;
    }

    private ImageSource finalizeSource(StringBuilder url, List<String> descriptors)
    {
      if (url.length() == 0) return null;
      int trailingCommas = removeTrailingCommas(url);
      if (trailingCommas > 1)
      {
        // FIXME give correct position
        error(ParseError.EMPTY_MIDDLE, -1);
      }
      ParseError error = null;
      int width = -1;
      float density = -1;
      int futureH = -1;
      for (String descriptor : descriptors)
      {
        char last = descriptor.charAt(descriptor.length() - 1);
        char first = descriptor.charAt(0);
        String value = descriptor.substring(0, descriptor.length() - 1);
        if (first == '-' || first == '+')
        {
          error = ParseError.DESCRIPTOR_WIDTH_SIGNED;
        }
        else if (last == 'w')
        {
          if (width != -1 || density != -1)
          {
            error = (width != -1) ? ParseError.DESCRIPTOR_WIDTH_MORE_THAN_ONCE
                : ParseError.DESCRIPTOR_MIX_WIDTH_DENSITY;
          }
          else
          {
            try
            {
              width = Integer.parseInt(value);
              if (width == 0)
              {
                error = ParseError.DESCRIPTOR_WIDTH_ZERO;
                width = -1;
              }
            } catch (NumberFormatException ex)
            {
              error = ParseError.DESCRIPTOR_WIDTH_NOT_INT;
            }
          }
        }
        else if (last == 'x')
        {
          if (width != -1 || density != -1 || futureH != -1)
          {

            error = (density != -1) ? ParseError.DESCRIPTOR_DENSITY_MORE_THAN_ONCE
                : ParseError.DESCRIPTOR_MIX_WIDTH_DENSITY;
          }
          else
          {
            try
            {
              if (!POSITIVE_FLOAT.matcher(value).matches())
              {
                throw new NumberFormatException();
              }
              density = Float.parseFloat(value);
              if (density < 0)
              {
                error = ParseError.DESCRIPTOR_DENSITY_NEGATIVE;
                density = -1;
              }
            } catch (NumberFormatException ex)
            {
              error = ParseError.DESCRIPTOR_DENSITY_NOT_FLOAT;
            }
          }

        }
        else if (last == 'h')
        {
          if (futureH != -1 || density != -1)
          {
            error = (futureH != -1) ? ParseError.DESCRIPTOR_FUTURE_H_MORE_THAN_ONCE
                : ParseError.DESCRIPTOR_FUTURE_H_WITH_DENSITY;
          }
          else
          {
            try
            {
              futureH = Integer.parseInt(value);
              if (futureH == 0)
              {
                error = ParseError.DESCRIPTOR_FUTURE_H_ZERO;
                futureH = -1;
              }
            } catch (NumberFormatException ex)
            {
              error = ParseError.DESCRIPTOR_FUTURE_H_NOT_INT;
            }
          }
        }
        else
        {
          error = ParseError.DESCRIPTOR_UNKNOWN_SUFFIX;
        }
      }
      if (futureH != -1 && width == -1)
      {
        error = ParseError.DESCRIPTOR_FUTURE_H_WITHOUT_WIDTH;
      }
      String source = url.toString();
      url.setLength(0);
      descriptors.clear();
      if (error != null)
      {
        error(error, -1);
        return null;
      }
      else
      {
        return ImageSource.create(source, width, density);
      }
    }

    private void error(ParseError code, int position)
    {
      // FIXME give correct position
      if (errorHandler != null) errorHandler.error(code, position);
    }

  }

  private final static class Builder
  {
    private final List<ImageSource> sources = new LinkedList<>();
    private String size = null;

    public Builder add(ImageSource source)
    {
      if (source != null) sources.add(source);
      return this;
    }

    @SuppressWarnings("unused")
    public Builder size(String size)
    {
      this.size = size;
      return this;
    }

    public boolean hasSources()
    {
      return !sources.isEmpty();
    }

    public SourceSet build()
    {
      return new SourceSet(ImmutableList.copyOf(sources), Strings.nullToEmpty(size));
    }
  }

  private final static Function<ImageSource, String> LIST_TO_URL_FUNC = new Function<SourceSet.ImageSource, String>()
  {
    @Override
    public String apply(ImageSource input)
    {
      return input.url;
    }
  };

  private final List<ImageSource> sources;
  private final List<String> urls;
  private final String size;

  private SourceSet(List<ImageSource> sources, String size)
  {
    this.sources = Preconditions.checkNotNull(sources);
    this.size = size;
    this.urls = Lists.transform(sources, LIST_TO_URL_FUNC);
  }

  /**
   * @return the list of image sources in the source set.
   */
  public List<ImageSource> getImageSources()
  {
    return sources;
  }

  /**
   * @return the list of the URLs of the images sources in the source set.
   */
  public List<String> getImageURLs()
  {
    return urls;
  }

  /**
   * @return the number of sources in this source set.
   */
  public String getSourceSize()
  {
    return size;
  }

  /**
   * @return whether this source set is empty.
   */
  public boolean isEmpty()
  {
    return sources.isEmpty();
  }
}

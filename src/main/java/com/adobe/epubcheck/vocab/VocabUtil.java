package com.adobe.epubcheck.vocab;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Utilities related to property values, vocabularies, and prefix declarations.
 * 
 * @author Romain Deltour
 *
 */
public final class VocabUtil
{

  private static Pattern parser = Pattern.compile("(([^:]*):)?(.*)");
  private static Splitter splitter = Splitter.onPattern("\\s+").omitEmptyStrings();

  /**
   * Parses a property value or space-separated list of property values, and
   * report validation errors on the fly.
   * 
   * @param value
   *          the value to parse.
   * @param vocabs
   *          a map of prefix to vocabularies.
   * @param isList
   *          whether the value is a single property value or list of property
   *          values.
   * @param report
   *          used to report validation errors.
   * @param location
   *          the location in the validated file.
   * @return
   */
  public static Set<Property> parseProperties(String value, Map<String, Vocab> vocabs, boolean isList, Report report,
      MessageLocation location)
  {
    Preconditions.checkNotNull(vocabs);
    Preconditions.checkNotNull(report);
    Preconditions.checkNotNull(location);
    if (value == null)
    {
      return ImmutableSet.of();
    }

    ImmutableSet.Builder<Property> builder = ImmutableSet.builder();

    // split properties, report error if a list is found but not allowed
    Iterable<String> properties = splitter.split(value);
    if (!isList && !Iterables.isEmpty(Iterables.skip(properties, 1)))
    {
      report.message(MessageId.OPF_025, location, value);
      return ImmutableSet.of();
    }

    for (String property : properties)
    {
      // parse prefix and local name, report error if malformed
      Matcher matcher = parser.matcher(property);
      matcher.matches();
      if (matcher.group(1) != null && (matcher.group(2).isEmpty() || matcher.group(3).isEmpty()))
      {
        report.message(MessageId.OPF_026, location, property);
        continue;
      }
      String prefix = Strings.nullToEmpty(matcher.group(2));
      String name = matcher.group(3);

      // lookup property in the vocab for its prefix
      // report error if not found
      try
      {
        Optional<Property> found = vocabs.get(prefix).lookup(name);
        if (found.isPresent())
        {
          builder.add(found.get());
        } else
        {
          report.message(MessageId.OPF_027, location, property);
          continue;
        }
      } catch (NullPointerException e)
      {
        // vocab not found (i.e. prefix undeclared), report warning
        report.message(MessageId.OPF_028, location, prefix);
        continue;
      }
    }
    return builder.build();
  }

  private VocabUtil()
  {
  }

}

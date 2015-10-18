package com.adobe.epubcheck.vocab;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Utilities related to property values, vocabularies, and prefix declarations.
 * 
 * @author Romain Deltour
 *
 */
public final class VocabUtil
{
  public static Vocab EMPTY_VOCAB = new EnumVocab<EMPTY>(EMPTY.class, "");

  private enum EMPTY
  {
  }

  private static Pattern propertyPattern = Pattern.compile("(([^:]*):)?(.*)");
  private static Splitter whitespaceSplitter = Splitter.onPattern("\\s+").omitEmptyStrings();

  /**
   * Parses a single property value and report validation errors on the fly.
   * 
   * @param value
   *          the value to parse.
   * @param vocabs
   *          a map of prefix to vocabularies.
   * @param report
   *          used to report validation errors.
   * @param location
   *          the location in the validated file.
   * @return an {@link Optional} containing the property if it was parsed
   *         successfully or nothing if there was a parsing error
   */
  public static Optional<Property> parseProperty(String value, Map<String, Vocab> vocabs,
      Report report, EPUBLocation location)
  {

    return Optional.fromNullable(
        Iterables.get(parseProperties(value, vocabs, false, report, location), 0, null));
  }

  /**
   * Parses a space-separated list of property values, and report validation
   * errors on the fly.
   * 
   * @param value
   *          the value to parse.
   * @param vocabs
   *          a map of prefix to vocabularies.
   * @param report
   *          used to report validation errors.
   * @param location
   *          the location in the validated file.
   * @return
   */
  public static Set<Property> parsePropertyList(String value, Map<String, ? extends Vocab> vocabs,
      Report report, EPUBLocation location)
  {
    return parseProperties(value, vocabs, true, report, location);
  }

  /**
   * Parses a space-separated list of property values silently, and returns a
   * set the properties as a set of Enum values.
   * 
   * @param properties
   *          the properties string to parse
   * @param vocabs
   *          a map of prefix to vocabularies.
   * @param clazz
   *          the class of the Enum holding the returned properties
   * @return
   */
  public static <E extends Enum<E>> Set<E> parsePropertyListAsEnumSet(String properties,
      Map<String, ? extends Vocab> vocabs, Class<E> clazz)
  {
    return Sets.newEnumSet(Property.filter(VocabUtil.parsePropertyList(properties, vocabs,
        QuietReport.INSTANCE, EPUBLocation.create("")), clazz), clazz);
  }

  private static Set<Property> parseProperties(String value, Map<String, ? extends Vocab> vocabs,
      boolean isList, Report report, EPUBLocation location)
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
    Iterable<String> properties = whitespaceSplitter.split(value);
    if (!isList && !Iterables.isEmpty(Iterables.skip(properties, 1)))
    {
      report.message(MessageId.OPF_025, location, value);
      return ImmutableSet.of();
    }

    for (String property : properties)
    {
      // parse prefix and local name, report error if malformed
      Matcher matcher = propertyPattern.matcher(property);
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
        }
        else
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

  /**
   * Parses a prefix attribute value and returns a map of prefixes to
   * vocabularies, given a pre-existing set of reserved prefixes, known
   * vocabularies, and default vocabularies that cannot be re-declared.
   * 
   * @param value
   *          the prefix declaration to parse.
   * @param predefined
   *          a map of reserved prefixes to associated vocabularies.
   * @param known
   *          a map of known URIs to known vocabularies.
   * @param forbidden
   *          a set of URIs of default vocabularies that cannot be re-declared.
   * @param report
   *          to report errors on the fly.
   * @param location
   *          the location of the attribute in the source file.
   * @return
   */
  public static Map<String, Vocab> parsePrefixDeclaration(String value,
      Map<String, ? extends Vocab> predefined, Map<String, ? extends Vocab> known,
      Set<String> forbidden, Report report, EPUBLocation location)
  {
    Map<String, Vocab> vocabs = Maps.newHashMap(predefined);
    Map<String, String> mappings = PrefixDeclarationParser.parsePrefixMappings(value, report,
        location);
    for (Entry<String, String> mapping : mappings.entrySet())
    {
      String prefix = mapping.getKey();
      String uri = mapping.getValue();
      if ("_".equals(prefix))
      {
        // must not define the '_' prefix
        report.message(MessageId.OPF_007a, location);
      }
      else if (forbidden.contains(uri))
      {
        // must not declare a default vocab
        report.message(MessageId.OPF_007b, location, prefix);
      }
      else
      {
        if (predefined.containsKey(prefix)
            && !Strings.nullToEmpty(predefined.get(prefix).getURI()).equals(uri))
        {
          // re-declaration of reserved prefix
          report.message(MessageId.OPF_007, location, prefix);
        }
        Vocab vocab = known.get(uri);
        vocabs.put(mapping.getKey(), (vocab == null) ? new UncheckedVocab(uri, prefix) : vocab);
      }
    }
    return ImmutableMap.copyOf(vocabs);
  }

  private VocabUtil()
  {
  }

}

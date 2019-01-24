package com.adobe.epubcheck.vocab;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

/**
 * A {@link Vocab} implementation that is backed by an {@link Enum}.
 * 
 * <p>
 * Property names will be computed from {@link Enum} constant names by applying
 * the following transformation:
 * </p>
 * <ul>
 * <li>The name is converted to lower case</li>
 * <li>The underscore character (<code>'_'</code>) is replaced by the hyphen
 * character (<code>'-'</code>).</li>
 * </ul>
 * 
 * @author Romain Deltour
 *
 */
public final class EnumVocab<P extends Enum<P>> implements Vocab
{

  private final Map<String, Property> index;
  private final String uri;
  private final Converter<P, String> converter;

  /**
   * Creates a new vocabulary backed by the given {@link Enum} class and with
   * properties having the common URI stem <code>base</code>. Properties of the
   * created vocabulary will have an empty prefix (in other words, this creates a
   * default vocabulary).
   * 
   * @param clazz
   *          the enumeration backing this vocabulary.
   * @param base
   *          the common stem URI of properties in this vocabulary.
   */
  public EnumVocab(final Class<P> clazz, final String base)
  {
    this(clazz, CaseFormat.LOWER_HYPHEN, base, null);
  }

  /**
   * Creates a new vocabulary backed by the given {@link Enum} class and with
   * properties having the common URI stem <code>base</code>. Properties of the
   * created vocabulary will have an empty prefix (in other words, this creates a
   * default vocabulary).
   * 
   * @param clazz
   *          the enumeration backing this vocabulary.
   * @param format
   *          the case format used by properties in this vocabulary
   * @param base
   *          the common stem URI of properties in this vocabulary.
   */
  public EnumVocab(final Class<P> clazz, final CaseFormat format, final String base)
  {
    this(clazz, format, base, null);
  }

  /**
   * Creates a new vocabulary backed by the given {@link Enum} class and with
   * properties having the common URI stem <code>base</code> and prefix
   * <code>prefix</code>
   * 
   * @param clazz
   *          the enumeration backing this vocabulary.
   * @param base
   *          the common stem URI of properties in this vocabulary.
   * @param prefix
   *          the common prefix of properties in this vocabulary.
   */
  public EnumVocab(final Class<P> clazz, final String base, final String prefix)
  {
    this(clazz, CaseFormat.LOWER_HYPHEN, base, prefix);
  }

  /**
   * Creates a new vocabulary backed by the given {@link Enum} class and with
   * properties having the common URI stem <code>base</code> and prefix
   * <code>prefix</code>
   * 
   * @param clazz
   *          the enumeration backing this vocabulary.
   * @param format
   *          the case format used by properties in this vocabulary
   * @param base
   *          the common stem URI of properties in this vocabulary.
   * @param prefix
   *          the common prefix of properties in this vocabulary.
   */
  public EnumVocab(final Class<P> clazz, final CaseFormat format, final String base,
      final String prefix)
  {
    this.uri = Strings.nullToEmpty(base);
    this.converter = Enums.stringConverter(clazz).reverse().andThen(CaseFormat.UPPER_UNDERSCORE
        .converterTo((format == null) ? CaseFormat.LOWER_HYPHEN : format));
    this.index = ImmutableMap
        .copyOf(Maps.transformEntries(Maps.uniqueIndex(EnumSet.allOf(clazz), converter),
            new EntryTransformer<String, P, Property>()
            {

              @Override
              public Property transformEntry(String name, P enumee)
              {
                return Property.newFrom(name, base, prefix, enumee);
              }

            }));
  }

  @Override
  public Optional<Property> lookup(String name)
  {
    return Optional.fromNullable(index.get(name));
  }

  @Override
  public String getURI()
  {
    return uri;
  }

  /**
   * Returns the {@link Property} for the given enum item contained in this
   * vocabulary.
   * 
   * @param property
   *          the property to look up, must not be <code>null</code>
   * @return the result of looking up <code>property</code> in this vocabulary.
   */
  public Property get(P property)
  {
    Preconditions.checkNotNull(property);
    return lookup(converter.convert(property)).get();
  }

  /**
   * Returns the property name for the given enum item contained in this
   * vocabulary.
   * 
   * @param property
   *          the property to get the name of, must not be <code>null</code>
   * @return the name of <code>property</code>.
   */
  public String getName(P property)
  {
    Preconditions.checkNotNull(property);
    return converter.convert(property);
  }

  /**
   * Returns the property names of the given enum items contained in this
   * vocabulary.
   * 
   * @param properties
   *          a collection of properties to get the name of, must not be
   *          <code>null</code>
   * @return the collection of the names of properties in <code>properties</code>.
   */
  public Collection<String> getNames(Collection<P> properties)
  {
    Preconditions.checkNotNull(properties);
    return Collections2.transform(properties, new Function<P, String>()
    {
      @Override
      public String apply(P property)
      {
        return converter.convert(property);
      }
    });
  }
}
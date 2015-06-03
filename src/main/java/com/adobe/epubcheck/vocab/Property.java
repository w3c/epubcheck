package com.adobe.epubcheck.vocab;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Represents a value of the <code>property</code> datatype, as listed in
 * vocabulary of properties.
 * 
 * @author Romain Deltour
 *
 */
public final class Property
{
  private final String name;
  private final String prefixedName;
  private final String fullName;
  private final Enum<?> enumee;

  /**
   * Creates a new instance from a short name, a prefix, and a stem URI.
   * 
   * @param name
   *          the short name of the property (aka "reference").
   * @param base
   *          the stem URI used to compute the full URI value.
   * @param prefix
   *          the prefix used in the CURIE form.
   * @return
   */
  public static Property newFrom(String name, String base, String prefix)
  {
    return new Property(name, base, prefix, null);
  }

  /**
   * Creates a new instance from a short name, a prefix, and a stem URI, and an
   * optional {@link Enum} item used to represent this property in known
   * vocabularies.
   * 
   * @param name
   *          the short name of the property (aka "reference").
   * @param base
   *          the stem URI used to compute the full URI value.
   * @param prefix
   *          the prefix used in the CURIE form.
   * @return
   */
  public static Property newFrom(String name, String base, String prefix, Enum<?> enumee)
  {
    return new Property(name, base, prefix, enumee);
  }

  /**
   * Filters the given set of {@link Property} values and keeps the values
   * backed by an {@link Enum} constant of the given type.
   * 
   * @param properties
   *          the properties to filter
   * @param clazz
   *          the class of a vocabulary-backing enum
   * @return An immutable {@link EnumSet} representing the filtered properties
   */
  public static <E extends Enum<E>> Set<E> filter(Set<Property> properties, final Class<E> clazz)
  {
    Preconditions.checkNotNull(clazz);
    if (properties == null) return ImmutableSet.of();
    return Sets.immutableEnumSet(Collections2.filter(
        Collections2.transform(properties, new Function<Property, E>()
        {
          @Override
          public E apply(Property input)
          {
            try
            {
              return clazz.cast(input.toEnum());
            } catch (Exception e)
            {
              return null;
            }
          }
        }), Predicates.notNull()));
  }

  private Property(String name, String base, String prefix, Enum<?> enumee)
  {
    this.name = name;
    this.fullName = base + name;
    this.prefixedName = (Strings.isNullOrEmpty(prefix)) ? name : prefix + ':' + name;
    this.enumee = enumee;
  }

  /**
   * Returns the short name (afa "reference") of this property.
   * 
   * @return the short name (afa "reference") of this property.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the full URI value of this property.
   * 
   * @return the full URI value of this property.
   */
  public String getFullName()
  {
    return fullName;
  }

  /**
   * Returns the prefixed name (aka CURIE) of this property.
   * 
   * @return the prefixed name (aka CURIE) of this property.
   */
  public String getPrefixedName()
  {
    return prefixedName;
  }

  /**
   * Returns the {@link Enum} item that is used to represent this property in
   * enum-based vocabularies.
   * 
   * @return the {@link Enum} item that is used to represent this property.
   * @throws UnsupportedOperationException
   *           if this property doesn't represent a property from an enum-based
   *           vocabulary.
   */
  public Enum<?> toEnum()
  {
    if (enumee == null) throw new UnsupportedOperationException();
    return enumee;
  }

  @Override
  public String toString()
  {
    return "Property [" + prefixedName + "]";
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Property other = (Property) obj;
    if (fullName == null)
    {
      if (other.fullName != null) return false;
    }
    else if (!fullName.equals(other.fullName)) return false;
    return true;
  }
  

}

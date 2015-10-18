package com.adobe.epubcheck.vocab;

import com.google.common.base.Optional;

/**
 * Represents a vocabulary of properties.
 * 
 * @author Romain Deltour
 *
 */
public interface Vocab
{
  /**
   * Returns whether a property in allowed in this vocabulary.
   * 
   * @param name
   *          the property name (unprefixed).
   * @return an {@link Property} reference which contains a {@link Property} if
   *         the lookup was successful or nothing if the property was not found
   *         in this vocabulary.
   */
  Optional<Property> lookup(String name);

  /**
   * Returns the base URI of this vocabulary.
   * 
   * @return the base URI of this vocabulary.
   */
  String getURI();
}
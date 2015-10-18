package com.adobe.epubcheck.vocab;

import com.google.common.base.Optional;

/**
 * A {@link Vocab} implementation which always return a successful lookup
 * result. In other words, this represents an "unchecked" vocabulary which
 * assumes that all property names are allowed. It is used to represent
 * user-declared custom vocabularies that are not known to EpubCheck.
 * 
 * @author Romain Deltour
 *
 */
public final class UncheckedVocab implements Vocab
{

  private final String base;
  private final String prefix;

  /**
   * Creates a new unchecked vocabulary representing properties whose URIs start
   * with <code>base</code> and short names have the prefix <code>prefix</code>.
   * 
   * @param base
   *          the URI stem used to generate URIs of properties in this
   *          vocabulary.
   * @param prefix
   *          the prefix used for property names.
   */
  public UncheckedVocab(String base, String prefix)
  {
    this.base = base;
    this.prefix = prefix;
  }

  /**
   * Returns a reference to a new {@link Property} with the short name
   * <code>name</code>, the same prefix and stem URI as was given when creating
   * this vocabulary.
   */
  @Override
  public Optional<Property> lookup(String name)
  {
    return Optional.of(Property.newFrom(name, base, prefix));
  }

  @Override
  public String getURI()
  {
    return base;
  }

}

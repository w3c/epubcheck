package com.adobe.epubcheck.vocab;

/**
 * An {@link UncheckedVocab} that is deprecated when used with its
 * default prefix.
 */
public final class DeprecatedVocab extends UncheckedVocab
{

  private String prefix;

  /**
   * Creates a new unchecked vocabulary representing properties whose URIs start
   * with <code>base</code> and short names have the prefix <code>prefix</code>,
   * but which usage (with its default associated prefix) is deprecated.
   * 
   * @param base
   *        the URI stem used to generate URIs of properties in this
   *        vocabulary.
   * @param prefix
   *        the prefix used for property names.
   */
  public DeprecatedVocab(String base, String prefix)
  {
    super(base, prefix);
    this.prefix = prefix;
  }

  @Override
  public boolean isDeprecated(String prefix)
  {
    return this.prefix.equals(prefix);
  }

}

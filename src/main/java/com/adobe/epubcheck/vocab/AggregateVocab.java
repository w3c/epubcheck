package com.adobe.epubcheck.vocab;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class AggregateVocab implements Vocab
{

  private final List<Vocab> vocabs;
  private final String uri;

  /**
   * Returns a vocabulary composed of the union of the vocabularies given as
   * parameter. The given vocabularies must have the same base URI.
   * 
   * @param vocabs
   *          the vocabularies to aggregate.
   * @return the aggregated vocabulary.
   */
  public static Vocab of(Vocab... vocabs)
  {
    return new AggregateVocab(new ImmutableList.Builder<Vocab>().add(vocabs).build());
  }

  private AggregateVocab(List<Vocab> vocabs)
  {
    this.uri = (!vocabs.isEmpty()) ? Strings.nullToEmpty(vocabs.get(0).getURI()) : "";
    for (Vocab vocab : vocabs)
    {
      if (!uri.equals(Strings.nullToEmpty(vocab.getURI())))
      {
        throw new IllegalArgumentException("Aggregated vocabs must share the same base URI");
      }
    }
    this.vocabs = vocabs;
  }

  @Override
  public Optional<Property> lookup(String name)
  {
    for (Vocab vocab : vocabs)
    {
      Optional<Property> found = vocab.lookup(name);
      if (found.isPresent()) return found;
    }
    return Optional.absent();
  }

  @Override
  public String getURI()
  {
    return uri;
  }

}

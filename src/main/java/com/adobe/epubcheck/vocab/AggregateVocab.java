package com.adobe.epubcheck.vocab;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class AggregateVocab implements Vocab
{

  private final List<Vocab> vocabs;

  public static Vocab of(Vocab... vocabs)
  {
    return new AggregateVocab(new ImmutableList.Builder<Vocab>().add(vocabs).build());
  }

  private AggregateVocab(List<Vocab> vocabs)
  {
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

}

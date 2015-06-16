package com.adobe.epubcheck.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;

/**
 * A utility to store {@link XMLValidator} references and return a list of
 * validators applicable to a given {@link ValidationContext}.
 */
public final class ValidatorMap
{

  /**
   * Returns a new builder.
   */
  public static Builder builder()
  {
    return new Builder();
  }

  // internal immutable map of validators to predicates
  private Map<XMLValidators, Predicate<? super ValidationContext>> validators;

  private ValidatorMap(Map<XMLValidators, Predicate<? super ValidationContext>> validators)
  {
    this.validators = validators;
  }

  /**
   * Returns a list of {@link XMLValidator}s applicable to the given
   * {@link ValidationContext}, as determined by the {@link Predicate} used to
   * build this map. The validators are returned in the order in which they have
   * been added to the builder.
   * <p>
   * <code>XMLValidator</code> instances are built dynamically, only when the
   * context satisfies the underlying predicate.
   * </p>
   * 
   * @param context
   *          a validation context
   * @return the list of validators applicable to <code>context</code>
   */
  public List<XMLValidator> getValidators(final ValidationContext context)
  {
    return FluentIterable
        .from(validators.entrySet())
        .transform(
            new Function<Entry<XMLValidators, Predicate<? super ValidationContext>>, XMLValidator>()
            {

              @Override
              public XMLValidator apply(
                  Entry<XMLValidators, Predicate<? super ValidationContext>> entry)
              {
                return entry.getValue().apply(context) ? entry.getKey().get() : null;
              }
            }).filter(Predicates.notNull()).toList();
  }

  /**
   * A builder for the {@link ValidatorMap}
   */
  public static final class Builder
  {
    // we use an ordered multimap internally
    // so that client code can add the same validator more than once
    // (e.g. with different predicates)
    private LinkedListMultimap<XMLValidators, Predicate<? super ValidationContext>> validatorsBuilder = LinkedListMultimap
        .create();

    /**
     * Puts the given validator in this map and determines its applicability to
     * a validation context by the given predicate.
     */
    public Builder put(Predicate<? super ValidationContext> predicate, XMLValidators validator)
    {
      validatorsBuilder.put(validator, predicate);
      return this;
    }

    /**
     * Puts all the given validators in this map and determines their
     * applicability to a validation context by the given predicate.
     */
    public Builder putAll(Predicate<? super ValidationContext> predicate,
        XMLValidators... validators)
    {
      for (XMLValidators validator : validators)
      {
        put(predicate, validator);
      }
      return this;
    }

    /**
     * Puts the given validator in this map, applicable to any validation
     * context.
     */
    public Builder put(XMLValidators validator)
    {
      return put(Predicates.<ValidationContext> alwaysTrue(), validator);
    }

    /**
     * Puts all the given validators in this map, applicable to any validation
     * context.
     */
    public Builder putAll(XMLValidators... validators)
    {
      return putAll(Predicates.<ValidationContext> alwaysTrue(), validators);
    }

    /**
     * Returns a newly created {@link ValidatorMap} from the validators added to
     * this builder.
     */
    public ValidatorMap build()
    {
      // builds the final immutable map of validators
      // if a validator is mapped to multiple predicates,
      // they are combined with an 'or' operation
      return new ValidatorMap(
          ImmutableMap.copyOf(Maps.transformValues(
              validatorsBuilder.asMap(),
              new Function<Iterable<Predicate<? super ValidationContext>>, Predicate<? super ValidationContext>>()
              {
                @Override
                public Predicate<? super ValidationContext> apply(
                    Iterable<Predicate<? super ValidationContext>> predicates)
                {
                  return Predicates.<ValidationContext> or(predicates);
                }
              })));
    }
  }
}

package com.adobe.epubcheck.api;

import java.util.Set;

import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * A feature report holds a list of noteworthy EPUB features. Features are
 * typically reported by handlers at parsing time. The report can be looked-up
 * after the parsing phase to enable checks that depend on conditions spanning
 * over multiple documents.
 *
 */
public final class FeatureReport
{

  private final SetMultimap<FeatureEnum, Feature> features = Multimaps
      .synchronizedSetMultimap(LinkedHashMultimap.<FeatureEnum, Feature> create());

  /**
   * Add a new feature to this report.
   * 
   * @param name
   *          The name of the feature (must not be null)
   * @param path
   *          The location of the feature (can be null)
   */
  public void report(FeatureEnum name, EPUBLocation location)
  {
    features.put(name, new Feature(name, location, null));
  }

  /**
   * Add a new feature to this report.
   * 
   * @param name
   *          The name of the feature (must not be null)
   * @param path
   *          The location of the feature (can be null)
   * @param value
   *          The value of the feature (can be null)
   */
  public void report(FeatureEnum name, EPUBLocation location, String value)
  {
    features.put(name, new Feature(name, location, value));
  }

  /**
   * Returns <code>true</code> iff this reports contains data for a feature
   * name.
   * 
   * @param feature
   *          the feature name to look-up
   * @return <code>true</code> iff this reports contains data for a feature
   *         name.
   */
  public boolean hasFeature(FeatureEnum feature)
  {
    return features.containsKey(feature);
  }

  /**
   * Returns the set of {@link Feature} stored in this report for the given
   * feature name.
   * 
   * @param feature
   *          the feature name to look-up
   * @return the (possibly empty) set of {@link Feature} stored in this report
   *         for the given feature name.
   */
  public Set<Feature> getFeature(FeatureEnum feature)
  {
    return ImmutableSet.copyOf(features.get(feature));
  }

  /**
   * A noteworthy feature in an EPUB Rendition (e.g. presence of page breaks,
   * encryption, subject, etc.)
   */
  public static final class Feature
  {
    private FeatureEnum name;
    private Optional<EPUBLocation> location;
    private Optional<String> value;

    private Feature(FeatureEnum name, EPUBLocation location, String value)
    {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(location);
      this.name = name;
      this.location = Optional.fromNullable(location);
      this.value = Optional.fromNullable(Strings.emptyToNull(value));
    }

    /**
     * The name of the feature
     * 
     * @return the name of the feature
     */
    public FeatureEnum getName()
    {
      return name;
    }

    /**
     * The location of the feature.
     * 
     * @return the path to the document containing the feature (can be absent).
     */
    public Optional<EPUBLocation> getLocation()
    {
      return location;
    }

    /**
     * The value of the feature, when relevant (can be absent).
     * 
     * @return the value of the feature if it has one, or
     *         {@link Optional#absent()}.
     */
    public Optional<String> getValue()
    {
      return value;
    }

  }
}

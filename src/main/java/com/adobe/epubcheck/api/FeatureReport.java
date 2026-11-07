package com.adobe.epubcheck.api;

import java.util.Set;

import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
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

  private final SetMultimap<FeatureEnum, Feature> features;
  // forks read through to their parent
  private final FeatureReport parent;
  // snapshots are immutable and read without locking
  private final boolean readOnly;

  public FeatureReport()
  {
    this((FeatureReport) null);
  }

  private FeatureReport(FeatureReport parent)
  {
    this.parent = parent;
    this.readOnly = false;
    this.features = Multimaps
        .synchronizedSetMultimap(LinkedHashMultimap.<FeatureEnum, Feature> create());
  }

  private FeatureReport(ImmutableSetMultimap<FeatureEnum, Feature> snapshot)
  {
    this.parent = null;
    this.readOnly = true;
    this.features = snapshot;
  }

  /**
   * Returns a child report whose features stay local until merged.
   */
  public FeatureReport fork()
  {
    return new FeatureReport(this);
  }

  /**
   * Returns an immutable copy of the features of this report.
   */
  public FeatureReport snapshot()
  {
    SetMultimap<FeatureEnum, Feature> copy = LinkedHashMultimap.create();
    collectFeatures(copy);
    return new FeatureReport(ImmutableSetMultimap.copyOf(copy));
  }

  private void collectFeatures(SetMultimap<FeatureEnum, Feature> target)
  {
    if (parent != null)
    {
      parent.collectFeatures(target);
    }
    if (readOnly)
    {
      target.putAll(features);
    }
    else
    {
      synchronized (features)
      {
        target.putAll(features);
      }
    }
  }

  /**
   * Adds the features reported in the given fork to this report.
   */
  public void mergeSnapshot(FeatureReport child)
  {
    Preconditions.checkNotNull(child);
    mergeLocal(child);
  }

  private void mergeLocal(FeatureReport child)
  {
    // copy the child without holding this report's lock
    SetMultimap<FeatureEnum, Feature> copy = LinkedHashMultimap.create();
    if (child.readOnly)
    {
      copy.putAll(child.features);
    }
    else
    {
      synchronized (child.features)
      {
        copy.putAll(child.features);
      }
    }
    if (copy.isEmpty())
    {
      return;
    }
    Preconditions.checkState(!readOnly, "snapshot is read-only");
    synchronized (features)
    {
      features.putAll(copy);
    }
  }

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
    Preconditions.checkState(!readOnly, "snapshot is read-only");
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
    Preconditions.checkState(!readOnly, "snapshot is read-only");
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
    return features.containsKey(feature) || (parent != null && parent.hasFeature(feature));
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
    if (parent == null)
    {
      if (readOnly)
      {
        return features.get(feature);
      }
      synchronized (features)
      {
        return ImmutableSet.copyOf(features.get(feature));
      }
    }
    // read the parent without holding this report's lock
    Set<Feature> inherited = parent.getFeature(feature);
    synchronized (features)
    {
      return ImmutableSet.<Feature>builder().addAll(inherited).addAll(features.get(feature))
          .build();
    }
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

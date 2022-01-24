package com.adobe.epubcheck.opf;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 * Represents a set of metadata as declared in the <code>metadata</code> element
 * in an EPUB Publication document (OPF).
 * <p>
 * The graph of refines is resolved when building the metadata set.
 * <p>
 * 
 * @author Romain Deltour
 *
 */
public final class MetadataSet
{

  // multimap of all metadata expressions, by property
  private final SetMultimap<Property, Metadata> all;
  // multimap of all metadata primary expressions, by property
  private final SetMultimap<Property, Metadata> primary;
  // multimap of metadata expressions refining a given ID
  private final SetMultimap<String, Metadata> refiners;
  // map of refining metadata to refined metadata
  private final Map<Metadata, Metadata> refines;
  // memoized view of the set of all metadata expressions
  private final Supplier<Set<Metadata>> allSet = Suppliers.memoize(new Supplier<Set<Metadata>>()
  {
    @Override
    public Set<Metadata> get()
    {
      return ImmutableSet.copyOf(all.values());
    }

  });
  // memoized view of the set of all metadata primary expressions
  private final Supplier<Set<Metadata>> primarySet = Suppliers
      .memoize(new Supplier<Set<Metadata>>()
      {
        @Override
        public Set<Metadata> get()
        {
          return ImmutableSet.copyOf(primary.values());
        }

      });

  private MetadataSet(Multimap<Property, Metadata> all, Multimap<Property, Metadata> primary,
      Map<Metadata, Metadata> refines, Multimap<String, Metadata> refiners)
  {
    this.all = ImmutableSetMultimap.copyOf(all);
    this.primary = ImmutableSetMultimap.copyOf(primary);
    this.refines = ImmutableMap.copyOf(refines);
    this.refiners = ImmutableSetMultimap.copyOf(refiners);
  }

  /**
   * Returns a set of all primary metadata expressions.
   * 
   * @return an immutable set (possibly empty) of all primary metadata
   *         expressions.
   */
  public Set<Metadata> getPrimary()
  {
    return primarySet.get();
  }

  /**
   * Returns a set of all metadata expressions (primary+subexpressions).
   * 
   * @return an immutable set (possibly empty) of all metadata expressions.
   */
  public Set<Metadata> getAll()
  {
    return allSet.get();
  }

  /**
   * Returns <code>true</code> if this metadata set contains a primary
   * expression for the given property
   * 
   * @param property
   *          a property from a metadata vocabulary
   * @return <code>true</code> if this metadata set contains a primary
   *         expression for the given property
   */
  public boolean containsPrimary(Property property)
  {
    return primary.containsKey(property);
  }

  /**
   * Returns <code>true</code> if this metadata set contains a primary
   * expression for the given property and the given value
   * 
   * @param property
   *          a property from a metadata vocabulary
   * @param value
   *          the value to search
   * @return <code>true</code> if this metadata set contains a primary
   *         expression for the given property and value
   */
  public boolean containsPrimary(Property property, String value)
  {
    for (Metadata meta : primary.get(property))
    {
      if (meta.getValue().equals(value))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if this metadata set contains an expression
   * (primary or subexpression) for the given property
   * 
   * @param property
   *          a property from a metadata vocabulary
   * @return <code>true</code> if this metadata set contains an expression for
   *         the given property
   */
  public boolean containsAny(Property property)
  {
    return all.containsKey(property);
  }

  /**
   * Returns the set of metadata primary expressions for the given property.
   * 
   * @param property
   *          a property from a metadata vocabulary
   * @return the set of metadata primary expressions for the given property, or
   *         an empty set if none exist.
   */
  public Set<Metadata> getPrimary(Property property)
  {
    return primary.get(property);
  }

  /**
   * Returns the set of all metadata expressions (primary and subexpressions)
   * for the given property.
   * 
   * @param property
   *          a property from a metadata vocabulary
   * @return the set of all metadata expressions for the given property, or an
   *         empty set if none exist.
   */
  public Set<Metadata> getAny(Property property)
  {
    return all.get(property);
  }

  /**
   * Returns an optional metadata expression refined by the given metadata
   * expression.
   * 
   * @param meta
   *          a metadata expression
   * @return {@link Optional#absent()} if the given metadata expression does not
   *         refine another metadata expression or a
   *         {@link Optional#of(Metadata)} containing the refined metadata
   *         expression
   */
  public Optional<Metadata> getRefinedBy(Metadata meta)
  {
    return Optional.fromNullable(refines.get(meta));
  }

  /**
   * Returns the set of all metadata subexpressions refining the metadata or
   * resource identified by the given ID.
   * 
   * @param id
   *          a string ID
   * @return the set of all metadata subexpressions refining the given ID, or an
   *         empty set if none exist.
   */
  public Set<Metadata> getRefining(String id)
  {
    return refiners.get(id);
  }

  /**
   * Search all refining expressions of the given metadata expressions, and
   * return the first one that matches the given property and value (if
   * present).
   * <p>
   * If <code>value</code> is {@link Optional#absent()}
   * </p>
   * , only the property is used in the lookup.
   * 
   * @param metas
   *          A set of metadata expressions to search
   * @param property
   *          The property of the searched expression
   * @param value
   *          The value of the searched expression, can be absent if the value
   *          is not relevant in the search
   * @return an {@link Optional} containing an expression refining one of the
   *         expressions in <code>metas</code> and matching the given property
   *         and value, or {@link Optional#absent()} if none is found.
   */
  public static Optional<Metadata> tryFindInRefines(Set<Metadata> metas, final Property property,
      final Optional<String> value)
  {
    Preconditions.checkNotNull(metas);
    Preconditions.checkNotNull(property);
    Preconditions.checkNotNull(value);
    return Iterables.tryFind(metas, new Predicate<Metadata>()
    {
      @Override
      public boolean apply(Metadata meta)
      {
        return tryFind(meta.getRefiners(), property, value).isPresent();
      }
    });
  }

  /**
   * Search all the given expressions and return the first one that matches the
   * given property and value (if present).
   * <p>
   * If <code>value</code> is {@link Optional#absent()}
   * </p>
   * , only the property is used in the lookup.
   * 
   * @param metas
   *          A set of metadata expressions to search
   * @param property
   *          The property of the searched expression
   * @param value
   *          The value of the searched expression, can be absent if the value
   *          is not relevant in the search
   * @return an {@link Optional} containing an expression in the
   *         <code>metas</code> set matching the given property and value, or
   *         {@link Optional#absent()} if none is found.
   */
  public static Optional<Metadata> tryFind(Set<Metadata> metas, final Property property,
      final Optional<String> value)
  {
    Preconditions.checkNotNull(metas);
    Preconditions.checkNotNull(property);
    Preconditions.checkNotNull(value);
    return Iterables.tryFind(metas, new Predicate<Metadata>()
    {

      @Override
      public boolean apply(Metadata meta)
      {
        return property.equals(meta.getProperty())
            && (!value.isPresent() || value.get().equals(meta.getValue()));
      }
    });
  }

  /**
   * Represents a metadata expression.
   *
   */
  public static final class Metadata
  {
    private final Optional<String> id;
    private final Property property;
    private final String value;
    private final Optional<String> refines;
    private final Set<Metadata> refiners;

    private Metadata(String id, Property property, String value, String refines,
        Set<Metadata> refiners)
    {
      Preconditions.checkNotNull(property);
      this.id = Optional.fromNullable(id);
      this.property = property;
      this.value = value == null ? "" : value.trim();
      this.refines = refines == null ? Optional.<String> absent()
          : refines.startsWith("#") ? Optional.fromNullable(Strings.emptyToNull(refines
              .substring(1))) : Optional.of(refines);
      this.refiners = refiners == null ? ImmutableSet.<MetadataSet.Metadata> of() : refiners;
    }

    /**
     * The ID of the element holding the expression.
     * 
     * @return the ID of the element holding the expression (possibly absent).
     */
    public Optional<String> getId()
    {
      return id;
    }

    /**
     * The property representing the statement of the expression.
     * 
     * @return the property representing the statement of the expression.
     */
    public Property getProperty()
    {
      return property;
    }

    /**
     * The value of the assertion of the expression.
     * 
     * @return the value of the assertion of the expression.
     */
    public String getValue()
    {
      return value;
    }

    /**
     * The ID of the resource or expression refined by this expression.
     * 
     * @return the ID of the resource or expression refined by this expression
     *         (possibly absent).
     */
    public Optional<String> getRefines()
    {
      return refines;
    }

    /**
     * The set of metadata expressions refining this metadata expression.
     * 
     * @return the set (possibly empty) of metadata expressions refining this
     *         metadata expression.
     */
    public Set<Metadata> getRefiners()
    {
      return refiners;
    }
    
    /**
     * Whether this is a primary metadata expression (as opposed to a refining
     * expression)
     * @return <code>true</code> if and only if this is a primary metadata expression
     */
    public boolean isPrimary() {
      return !refines.isPresent();
    }

    @Override
    public String toString()
    {
      return "Metadata [id=" + id + ", property=" + property + ", value=" + value + ", refines="
          + refines + "]";
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((property == null) ? 0 : property.hashCode());
      result = prime * result + ((refines == null) ? 0 : refines.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Metadata other = (Metadata) obj;
      if (id == null)
      {
        if (other.id != null) return false;
      }
      else if (!id.equals(other.id)) return false;
      if (property == null)
      {
        if (other.property != null) return false;
      }
      else if (!property.equals(other.property)) return false;
      if (refines == null)
      {
        if (other.refines != null) return false;
      }
      else if (!refines.equals(other.refines)) return false;
      if (value == null)
      {
        if (other.value != null) return false;
      }
      else if (!value.equals(other.value)) return false;
      return true;
    }

  }

  /**
   * Creates a new builder. Calling this method is identical to calling the
   * empty {@link Builder} constructor.
   * 
   * @return a newly created builder.
   */
  public static Builder builder()
  {
    return new Builder();
  }

  /**
   * A builder for sets of metadata expressions.
   *
   */
  public static final class Builder
  {
    // Primary metadata expressions, mapped by properties
    private final Multimap<Property, Metadata> primary = HashMultimap.create();
    // All metadata expressions (primary+subexpressions), mapped by properties
    private final Multimap<Property, Metadata> all = HashMultimap.create();
    // A list of temporary incomplete metadata objects to build
    private final LinkedList<Metadata> tempMetas = Lists.newLinkedList();
    // A map of (possibly temporary) metadata objects refining the given IDs
    private final Multimap<String, Metadata> refinersMap = LinkedListMultimap.create();
    // A map of refining-to-refined metadata objects
    private final Map<Metadata, Metadata> refines = Maps.newHashMap();
    // A map of the status of metadata being visited by DFS
    private final Map<Metadata, Visit> visits = Maps.newHashMap();

    // holds the status of visited metadata in the DFS sort
    private static enum Visit
    {
      UNVISITED,
      VISITED,
      VISITING;
      public static Visit safe(Visit visit)
      {
        return visit != null ? visit : UNVISITED;
      }
    }

    /**
     * Builds the set. Must be called after all metadata expressions have been
     * added.
     * 
     * @return an immutable metadata set
     * @throws IllegalStateException
     *           if a cycle is found in the graph of refining expressions
     */
    public MetadataSet build()
    {
      // DFS recursive build of metadata and their refining metadata
      // Note: metadata consistency (e.g. whether @refines point to valid IDs)
      // has already been checked in schemas
      for (Metadata metadata : tempMetas)
      {
        build(metadata);
      }
      return new MetadataSet(all, primary, refines, refinersMap);
    }

    // builds a metadata and all its refining metadata recursively
    private Metadata build(Metadata meta)
    {
      Preconditions.checkArgument(meta.getId().isPresent());
      switch (Visit.safe(visits.get(meta)))
      {
      case VISITED:
        // has already been built
        return meta;
      case VISITING:
        throw new IllegalStateException("Not a DAG");
      default:
        break;
      }
      visits.put(meta, Visit.VISITING);
      Set<Metadata> refiners = new HashSet<Metadata>();
      // recursively build the current metadata's refining metadata
      for (Metadata refiner : refinersMap.get(meta.getId().get()))
      {
        // add the refining metadata to the "refiners" set
        // - if the refining metadata has no ID, it's final already
        // - otherwise, build it recursively
        refiners.add((refiner.getId().isPresent()) ? build(refiner) : refiner);
      }
      // build the finalized metadata and put it in the final maps
      Metadata result = new Metadata(meta.getId().get(), meta.getProperty(), meta.getValue(), meta
          .getRefines().orNull(), refiners);
      for (Metadata refiner : refiners)
      {
        refines.put(refiner, result);
      }
      if (result.getRefines().isPresent())
      {
        // re-add built metadata to the refiners map
        refinersMap.remove(result.getRefines().get(), meta);
        refinersMap.put(result.getRefines().get(), result);
      }
      else
      {
        primary.put(result.getProperty(), result);
      }
      all.put(result.getProperty(), result);
      visits.put(meta, Visit.VISITED);
      return result;
    }

    /**
     * Adds a metadata expression to the set being built.
     * 
     * @param id
     *          the ID of the element holding the expression, can be null.
     * @param property
     *          the property representing the statement of the expression (must
     *          not be null)
     * @param value
     *          the value representing the assertion of the expression (can, but
     *          should not, be null)
     * @param refines
     *          the ID of the expression or resource refined by this expression.
     *          If the given string starts with the character '#' (relative
     *          fragment URI), it is stripped to get the ID. Can be null
     * @return this builder
     */
    public Builder meta(String id, Property property, String value, String refines)
    {
      // create a (possibly temporary) metadata from the given fields
      Metadata meta = new Metadata(id, property, value, refines, null);
      if (id == null)
      {
        // the metadata cannot be refined, put it in the final maps
        if (!meta.refines.isPresent())
        {
          primary.put(property, meta);
        }
        all.put(property, meta);
      }
      else
      {
        // the metadata may be refined, store it to the temporary list
        tempMetas.add(meta);
      }
      if (meta.refines.isPresent())
      {
        // if the metadata refines something, store it to the map of "refiners"
        refinersMap.put(meta.refines.get(), meta);
      }
      return this;
    }
  }

}

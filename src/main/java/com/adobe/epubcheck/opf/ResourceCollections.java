package com.adobe.epubcheck.opf;

import java.util.LinkedHashSet;
import java.util.List;

import com.adobe.epubcheck.opf.ResourceCollection.Roles;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

/**
 * Represents a set of collections (as defined by <code>collection</code>
 * elements in a Package Document), with predictable iteration order.
 */
public final class ResourceCollections
{

  private final List<ResourceCollection> collections;
  private final ListMultimap<String, ResourceCollection> collectionsByRole;

  private ResourceCollections(Iterable<ResourceCollection> collections)
  {
    ImmutableList.Builder<ResourceCollection> listBuilder = ImmutableList.builder();
    ImmutableListMultimap.Builder<String, ResourceCollection> byRoleBuilder = ImmutableListMultimap
        .builder();
    for (ResourceCollection collection : collections)
    {
      listBuilder.add(collection);
      for (String role : collection.getRoles())
      {
        byRoleBuilder.put(role, collection);
      }
    }
    this.collections = listBuilder.build();
    this.collectionsByRole = byRoleBuilder.build();
  }

  /**
   * Returns the list of all collections in this set.
   */
  public List<ResourceCollection> asList()
  {
    return collections;
  }

  /**
   * Returns the list of collections in this set with the given role.
   */
  public List<ResourceCollection> getByRole(String role)
  {
    return collectionsByRole.get(role);
  }

  /**
   * Returns the list of collections in this set with the given IDPF-reserved
   * role.
   */
  public List<ResourceCollection> getByRole(Roles role)
  {
    return role == null ? ImmutableList.<ResourceCollection> of() : getByRole(role.toString());
  }

  /**
   * Returns <code>true</code> if this set contains one or more collections with
   * the given role.
   */
  public boolean hasRole(String role)
  {
    return !collectionsByRole.get(role).isEmpty();
  }

  /**
   * Returns <code>true</code> if this set contains one or more collections with
   * the given IDPF-reserved role.
   */
  public boolean hasRole(Roles role)
  {
    return role != null && hasRole(role.toString());
  }

  /**
   * Creates a new builder. Calling this method is identical to calling the
   * empty {@link Builder} constructor.
   * 
   * @return a newly created builder.
   */
  public static final Builder builder()
  {
    return new Builder();
  }

  /**
   * A builder for {@link ResourceCollections}.
   * 
   * @author Romain Deltour
   *
   */
  public static final class Builder
  {
    private final LinkedHashSet<ResourceCollection> collections = Sets.newLinkedHashSet();

    /**
     * Add the given collection to the list of collections to build.
     * 
     * @param collection
     *          the collection to add.
     * @return this builder.
     */
    public Builder add(ResourceCollection collection)
    {
      if (collection != null) collections.add(collection);
      return this;

    }

    /**
     * Returns a newly created list of collections.
     */
    public ResourceCollections build()
    {
      return new ResourceCollections(collections);
    }
  }
}

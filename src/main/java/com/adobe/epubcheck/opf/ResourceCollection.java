package com.adobe.epubcheck.opf;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Represent a collection of resources as defined by the <code>collection</code>
 * element in Package Documents.
 */
public final class ResourceCollection
{

  /**
   * The collection roles reserved by IDPF. See <a
   * href="http://www.idpf.org/epub/vocab/package/roles/"
   * >http://www.idpf.org/epub/vocab/package/roles/</a>
   *
   */
  public static enum Roles
  {
    DICTIONARY,
    DISTRIBUTABLE_OBJECT,
    INDEX,
    INDEX_GROUP,
    MANIFEST,
    PREVIEW,
    SCRIPTABLE_COMPONENT;

    /**
     * Returns the role name as defined by IDPF.
     */
    @Override
    public String toString()
    {
      return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Returns an {@link Optional} containing the enum constant for the given
     * role name or {@link Optional#absent()} if none is found.
     */
    public static Optional<Roles> fromString(String role)
    {
      try
      {
        return Optional
            .of(Roles.valueOf(Strings.nullToEmpty(role).toUpperCase(Locale.ROOT).replace('-', '_')));
      } catch (IllegalArgumentException e)
      {
        return Optional.absent();
      }
    }

  }

  private final Set<String> roles;
  private final MetadataSet metadata;
  private final LinkedResources metadataLinks;
  private final ResourceCollections collections;
  private final LinkedResources resources;

  private ResourceCollection(Set<String> roles, MetadataSet metadata,
      LinkedResources metadataLinks, ResourceCollections collections, LinkedResources resources)
  {
    this.roles = roles;
    this.metadata = metadata;
    this.metadataLinks = metadataLinks;
    this.collections = collections;
    this.resources = resources;
  }

  /**
   * Returns the roles of this collection.
   */
  public Set<String> getRoles()
  {
    return roles;
  }

  /**
   * Returns <code>true</code> if this collection has the given role.
   */
  public boolean hasRole(String role)
  {
    return role != null && roles.contains(role);
  }

  /**
   * Returns <code>true</code> if this collection has the given IDPF-reserved
   * role.
   */
  public boolean hasRole(Roles role)
  {
    return role != null && roles.contains(role.toString());
  }

  /**
   * Returns the metadata of this collection. Guaranteed non-null (can be
   * empty).
   */
  public MetadataSet getMetadata()
  {
    return metadata;
  }

  /**
   * Returns the linked resources defined as metadata in this collection.
   * Guaranteed non-null (can be empty).
   */
  public LinkedResources getMetadataLinks()
  {
    return metadataLinks;
  }

  /**
   * Returns the set of this collection's sub collections. Guaranteed non-null
   * (can be empty).
   */
  public ResourceCollections getCollections()
  {
    return collections;
  }

  /**
   * Returns the set of this collection's resources. Guaranteed non-null (can be
   * empty).
   */
  public LinkedResources getResources()
  {
    return resources;
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
   * A builder for {@link ResourceCollection}.
   *
   */
  public static final class Builder
  {
    private final Set<String> roles = new HashSet<String>();
    private MetadataSet metadata = null;
    private LinkedResources metadataLinks = null;
    private final ResourceCollections.Builder collections = ResourceCollections.builder();
    private LinkedResources resources = null;

    /**
     * Add the given roles to the collection to build.
     * 
     * @param iterable
     *          a set of roles.
     * @return this builder.
     */
    public Builder roles(Iterable<String> roles)
    {
      Iterables.addAll(this.roles, roles);
      return this;
    }

    /**
     * Add the given metadata set to the collection to build.
     * 
     * @param metadata
     *          a set of EPUB metadata.
     * @return this builder.
     */
    public Builder metadata(MetadataSet metadata)
    {
      this.metadata = metadata;
      return this;
    }

    /**
     * Add the given linked resources as metadata links to the collection to
     * build.
     * 
     * @param resources
     *          the resources to add.
     * @return this builder.
     */
    public Builder metadataLinks(LinkedResources metadataLinks)
    {
      this.metadataLinks = metadataLinks;
      return this;
    }

    /**
     * Add the given collection as a sub-collection of the collection to build.
     * 
     * @param collection
     *          a collection.
     * @return this builder.
     */
    public Builder collection(ResourceCollection collection)
    {
      this.collections.add(collection);
      return this;
    }

    /**
     * Add the given resources to the collection to build.
     * 
     * @param resources
     *          the resources to add.
     * @return this builder.
     */
    public Builder resources(LinkedResources resources)
    {
      this.resources = resources;
      return this;
    }

    /**
     * Returns a newly created collection.
     */
    public ResourceCollection build()
    {
      Preconditions.checkState(roles != null);
      return new ResourceCollection(ImmutableSet.copyOf(roles), metadata != null ? metadata
          : MetadataSet.builder().build(), metadataLinks != null ? metadataLinks : LinkedResources
          .builder().build(), collections.build(), resources != null ? resources : LinkedResources
          .builder().build());
    }
  }
}
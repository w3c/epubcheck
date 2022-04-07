package com.adobe.epubcheck.opf;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.mola.galimatias.URL;

/**
 * Represents a set of linked resources (i.e. resources defined by
 * <code>link</code> elements in a Package Document), with predictable iteration
 * order.
 */
public final class LinkedResources
{

  private final List<LinkedResource> resources;
  private final Map<String, LinkedResource> resourcesById;
  private final ListMultimap<URL, LinkedResource> resourcesByURL;

  /**
   * Search the linked resource with the given ID.
   * 
   * @param id
   *          the ID of the resource to search, can be <code>null</code>.
   * @return An {@link Optional} containing the linked resource if found, or
   *         {@link Optional#absent()} if not found.
   */
  public Optional<LinkedResource> getById(String id)
  {
    return Optional.fromNullable(resourcesById.get(id));
  }

  /**
   * Search the linked resource with the given URL. All resource whose
   * <code>href</code> URL minus fragment is equal to the given URL are
   * returned, in document order.
   * 
   * @param url
   *          the URL (without fragment) of the resource to search, can be
   *          <code>null</code>.
   * @return A list of linked resources referencing the resource at
   *         <code>url</code> or a fragment thereof ; an empty list is returned
   *         if no such resource is found.
   */
  public List<LinkedResource> getByURL(URL url)
  {
    return resourcesByURL.get(url);
  }

  /**
   * Returns the list of all linked resources in this set, in document order.
   * 
   * @return the list of all linked resources in this set.
   */
  public List<LinkedResource> asList()
  {
    return resources;
  }

  /**
   * Returns <code>true</code> if this set contains a linked resource
   * referencing the given resource (or fragment thereof).
   */
  public boolean hasResource(URL url)
  {
    return !getByURL(url).isEmpty();
  }

  private LinkedResources(Iterable<LinkedResource> resources)
  {
    ImmutableList.Builder<LinkedResource> listBuilder = ImmutableList.builder();
    ImmutableListMultimap.Builder<URL, LinkedResource> byURLBuilder = ImmutableListMultimap
        .builder();
    Map<String, LinkedResource> byIdMap = Maps.newHashMap();
    for (LinkedResource resource : resources)
    {
      listBuilder.add(resource);
      byURLBuilder.put(resource.getDocumentURL(), resource);
      if (resource.getId().isPresent()) byIdMap.put(resource.getId().get(), resource);
    }
    this.resources = listBuilder.build();
    this.resourcesByURL = byURLBuilder.build();
    this.resourcesById = ImmutableMap.copyOf(byIdMap);
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
   * A builder for {@link LinkedResources}.
   *
   */
  public static final class Builder
  {
    private final LinkedHashSet<LinkedResource> resources = Sets.newLinkedHashSet();

    /**
     * Add a new linked resource to this builder.
     * 
     * @param resource
     *          the resource to add.
     * @return this builder.
     */
    public Builder add(LinkedResource resource)
    {
      if (resource != null) resources.add(resource);
      return this;
    }

    /**
     * Returns a newly created set of linked resources.
     */
    public LinkedResources build()
    {
      return new LinkedResources(resources);
    }
  }

}

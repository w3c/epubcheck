package org.w3c.epubcheck.core.references;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.w3c.epubcheck.core.references.Reference.Type;

import com.adobe.epubcheck.opf.OPFItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.mola.galimatias.URL;

public final class ResourceRegistry
{

  private Map<URL, Resource> resources = new HashMap<>();
  // ordered for getIDPosition
  private Map<URL, Set<String>> ids = new HashMap<>();
  private Table<URL, String, Type> idTypes = HashBasedTable.create();

  /**
   * Registers an ID found in the given resource.
   *
   * @param id
   *          the ID to register
   * @param type
   *          the type of reference this ID can be the target of
   * @param resourceURL
   *          the URL of the resource the ID belongs to
   */
  public void registerID(String id, Type type, URL resourceURL)
  {
    if (id == null) return;
    Preconditions.checkArgument(resourceURL != null && resources.containsKey(resourceURL),
        "resource not registered");
    synchronized (ids)
    {
      Set<String> resourceIDs = ids.computeIfAbsent(resourceURL, key -> new LinkedHashSet<>());
      if (resourceIDs.add(id))
      {
        idTypes.put(resourceURL, id, type);
      }
    }
  }

  /**
   * Returns the position of the given ID in the document represented by this
   * resource.
   * 
   * @param resource
   * 
   * @return {@code -1} if the ID wasn't found in the document, or {@code 0} if
   *           the given ID is {@code null} or an empty string, or the 1-based
   *           position of the ID otherwise.
   */
  public int getIDPosition(String id, Resource resource)
  {
    Preconditions.checkArgument(resource != null);
    if (id == null || id.trim().isEmpty()) return 0;
    Set<String> resourceIDs = ids.get(resource.getURL());
    if (resourceIDs == null) return -1;
    int position = 1;
    for (String registeredID : resourceIDs)
    {
      if (registeredID.equals(id))
      {
        return position;
      }
      position++;
    }
    return -1;
  }

  public Type getIDType(String id, Resource resource)
  {
    Preconditions.checkArgument(resource != null);
    return idTypes.get(resource.getURL(), id);
  }

  public void registerResource(OPFItem item)
  {
    Preconditions.checkArgument(item != null);
    // Note: Duplicate manifest items are already checked in OPFChecker.
    if (!resources.containsKey(item.getURL()))
    {

      resources.put(item.getURL(), Resource.fromItem(item));
    }
  }

  public void registerResource(URL url, String mimetype)
  {
    Preconditions.checkArgument(url != null);
    if (!resources.containsKey(url))
    {
      resources.put(url, Resource.fromURL(url, mimetype));
    }
  }

  public String getMimeType(URL resource)
  {
    return resources.containsKey(resource) ? resources.get(resource).getMimeType() : null;
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<OPFItem> getOPFItem(URL url)
  {
    return Optional.ofNullable(resources.get(url)).map(r -> r.getItem());
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<Resource> getResource(URL url)
  {
    return Optional.ofNullable(resources.get(url));
  }
}

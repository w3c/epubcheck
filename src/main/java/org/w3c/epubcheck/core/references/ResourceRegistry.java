package org.w3c.epubcheck.core.references;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.epubcheck.core.references.Reference.Type;

import com.adobe.epubcheck.opf.OPFItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import io.mola.galimatias.URL;

public final class ResourceRegistry
{

  // registries are shared by concurrent checks
  private final Map<URL, Resource> resources = new ConcurrentHashMap<>();
  // ordered for getIDPosition
  private final Map<URL, Set<String>> ids = new ConcurrentHashMap<>();
  private final Table<URL, String, Type> idTypes = Tables.synchronizedTable(HashBasedTable.create());

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
    Set<String> resourceIDs = ids.computeIfAbsent(resourceURL,
        key -> Collections.synchronizedSet(new LinkedHashSet<>()));
    synchronized (resourceIDs)
    {
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
    synchronized (resourceIDs)
    {
      for (String registeredID : resourceIDs)
      {
        if (registeredID.equals(id))
        {
          return position;
        }
        position++;
      }
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
    resources.putIfAbsent(item.getURL(), Resource.fromItem(item));
  }

  public void registerResource(URL url, String mimetype)
  {
    Preconditions.checkArgument(url != null);
    resources.putIfAbsent(url, Resource.fromURL(url, mimetype));
  }

  public String getMimeType(URL resource)
  {
    if (resource == null) return null;
    Resource registered = resources.get(resource);
    return (registered != null) ? registered.getMimeType() : null;
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<OPFItem> getOPFItem(URL url)
  {
    if (url == null) return Optional.empty();
    return Optional.ofNullable(resources.get(url)).map(r -> r.getItem());
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<Resource> getResource(URL url)
  {
    if (url == null) return Optional.empty();
    return Optional.ofNullable(resources.get(url));
  }
}

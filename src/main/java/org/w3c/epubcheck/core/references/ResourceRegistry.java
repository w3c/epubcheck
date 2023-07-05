package org.w3c.epubcheck.core.references;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.w3c.epubcheck.core.references.Reference.Type;

import com.adobe.epubcheck.opf.OPFItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import io.mola.galimatias.URL;

public final class ResourceRegistry
{

  private Map<URL, Resource> resources = new HashMap<>();
  private Map<URL, List<String>> ids = new HashMap<>();
  private Table<URL, String, Type> idTypes = HashBasedTable.create();

  public void registerID(String id, Type type, URL resourceURL)
  {
    if (id == null) return;
    Preconditions.checkArgument(resourceURL != null && resources.containsKey(resourceURL),
        "resource not registered");
    synchronized (ids)
    {
      ids.putIfAbsent(resourceURL, new LinkedList<>());
      List<String> resourceIDs = ids.get(resourceURL);
      // Note: duplicate IDs are checked in schematron
      if (!resourceIDs.contains(id))
      {
        resourceIDs.add(id);
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
    int position = Optional.ofNullable(ids.get(resource.getURL())).orElse(ImmutableList.of())
        .indexOf(id);
    return (position == -1) ? -1 : position + 1;
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

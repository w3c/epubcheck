package com.adobe.epubcheck.opf;

import java.util.List;
import java.util.Map;

import com.adobe.epubcheck.opf.OPFItem.Builder;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Represents the set of Publication Resources in a Package Document (OPF).
 *
 */
public final class OPFItems
{

  private final List<OPFItem> items;
  private final List<OPFItem> spine;
  private final Map<String, OPFItem> itemsById;
  private final Map<String, OPFItem> itemsByPath;

  /**
   * Search the item with the given ID.
   * 
   * @param id
   *          the ID of the item to search, can be <code>null</code>.
   * @return An {@link Optional} containing the item if found, or
   *         {@link Optional#absent()} if not found.
   */
  public Optional<OPFItem> getItemById(String id)
  {
    return Optional.fromNullable(itemsById.get(id));
  }

  /**
   * Search the item with the given path.
   * 
   * @param id
   *          the path of the item to search, can be <code>null</code>.
   * @return An {@link Optional} containing the item if found, or
   *         {@link Optional#absent()} if not found.
   */
  public Optional<OPFItem> getItemByPath(String path)
  {
    return Optional.fromNullable(itemsByPath.get(path));
  }

  /**
   * Returns the list of items in the spine. A single {@link OPFItem} instance
   * can appear multiple times in the list.
   * 
   * @return the list of items in the spine.
   */
  public List<OPFItem> getSpineItems()
  {
    return spine;
  }

  /**
   * Returns the list of items in this set, in document order.
   * 
   * @return the list of items in this set.
   */
  public List<OPFItem> getItems()
  {
    return items;
  }

  private OPFItems(Iterable<OPFItem> items, Iterable<String> spineIDs)
  {
    this.items = ImmutableList.copyOf(Preconditions.checkNotNull(items));
    // Build the by-ID and by-Paths maps
    // We use temporary HashMaps to ignore potential duplicate keys
    Map<String, OPFItem> itemsById = Maps.newHashMap();
    Map<String, OPFItem> itemsByPath = Maps.newHashMap();
    for (OPFItem item : this.items)
    {
      itemsById.put(item.getId(), item);
      itemsByPath.put(item.getPath(), item);
    }
    this.itemsById = ImmutableMap.copyOf(itemsById);
    this.itemsByPath = ImmutableMap.copyOf(itemsByPath);
    // Build the spine view
    this.spine = FluentIterable.from(spineIDs).transform(new Function<String, OPFItem>()
    {
      @Override
      public OPFItem apply(final String id)
      {
        return OPFItems.this.itemsById.get(id.trim());
      }
    }).filter(Predicates.notNull()).toList();
  }

  /**
   * Creates a consolidated set of {@link OPFItem} from item builders and a list
   * of spine item IDs.
   * 
   * @param itemBuilders
   *          the builders of the {@link OPFItem} in the set.
   * @param spineIDs
   *          the IDs of the items in the spine.
   * @return a consolidated set of {@link OPFItem}s.
   */
  public static OPFItems build(Iterable<Builder> itemBuilders, Iterable<String> spineIDs)
  {
    return new OPFItems(Iterables.transform(Preconditions.checkNotNull(itemBuilders),
        new Function<OPFItem.Builder, OPFItem>()
        {
          @Override
          public OPFItem apply(Builder builder)
          {
            return builder.build();
          }
        }), Preconditions.checkNotNull(spineIDs));
  }
}

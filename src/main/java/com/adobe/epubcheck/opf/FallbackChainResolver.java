package com.adobe.epubcheck.opf;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.collect.ImmutableList;

public final class FallbackChainResolver
{

  private final Report report;
  private final EPUBVersion version;
  private final Map<String, OPFItem.Builder> items;

  public FallbackChainResolver(Map<String, OPFItem.Builder> items,
      ValidationContext context)
  {
    this.report = context.report;
    this.version = context.version;
    this.items = items;
  }

  public List<OPFItem> resolve()
  {
    Deque<OPFItem.Builder> itemQueue = new LinkedList<>(items.values());
    ImmutableList.Builder<OPFItem> resolved = ImmutableList.builderWithExpectedSize(
        itemQueue.size());
    int pending = 0; // counter for pending unresolved items

    // Loop through the items to resolve the fallback chain
    while (!itemQueue.isEmpty() && pending <= itemQueue.size())
    {
      OPFItem.Builder item = itemQueue.pop();

      // get this item's intrinsic fallback properties
      // (i.e. does this item even require a fallback?)
      String mimetype = item.mimetype();
      item.hasCoreMediaTypeFallback(item.hasCoreMediaTypeFallback()
          || (version == EPUBVersion.VERSION_2
              && OPFChecker.isBlessedImageType(mimetype, version))
          || (OPFChecker30.isCoreMediaType(mimetype)));
      item.hasContentDocumentFallback(item.hasContentDocumentFallback()
          || OPFChecker.isBlessedItemType(mimetype, version)
          || OPFChecker.isDeprecatedBlessedItemType(mimetype));

      // check the fallback item, if any
      if (item.hasFallback())
      {
        // get the fallback item builder
        OPFItem.Builder fallback = items.get(item.fallback());

        // report missing fallback item
        if (fallback == null)
        {
          report.message(MessageId.OPF_040, item.location(), item.fallback());
        }
        // if fallback is resolved already, get the fallback properties
        else if (fallback.isResolved())
        {
          item.hasContentDocumentFallback(
              item.hasContentDocumentFallback() || fallback.hasContentDocumentFallback());
          item.hasCoreMediaTypeFallback(
              item.hasCoreMediaTypeFallback() || fallback.hasCoreMediaTypeFallback());
        }
        // else the fallback is not resolved yet,
        // re-add this pending item to the queue
        else
        {
          itemQueue.add(item);
          pending++;
          continue;
        }
      }

      // in EPUB 2.0.1, check the fallback-style attribute
      if (version == EPUBVersion.VERSION_2 && item.hasFallbackStyle())
      {

        // get the fallback item builder
        OPFItem.Builder fallbackStyle = items.get(item.fallbackStyle());

        // report missing fallback style item
        if (fallbackStyle == null)
        {
          report.message(MessageId.OPF_041, item.location(), item.fallback());
        }
        // if fallback is resolved already, get the fallback properties
        else
        {
          boolean hasValidFallbackStyle = OPFChecker.isBlessedStyleType(fallbackStyle.mimetype())
              || OPFChecker.isDeprecatedBlessedStyleType(fallbackStyle.mimetype());
          item.hasContentDocumentFallback(
              item.hasContentDocumentFallback() || hasValidFallbackStyle);
          item.hasCoreMediaTypeFallback(
              item.hasCoreMediaTypeFallback() || hasValidFallbackStyle);
        }
      }

      // mark this item as resolved
      resolved.add(item.build());
      item.markResolved();
      pending = 0;
    }
    // if the queue is not empty, we found a circular chain
    if (!itemQueue.isEmpty())
    {
      // report and build the remaining items
      report.message(MessageId.OPF_045, itemQueue.peek().location());
      itemQueue.stream().forEach(i -> resolved.add(i.build()));
    }
    return resolved.build();
  }

}

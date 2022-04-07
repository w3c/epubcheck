/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.opf;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.epubcheck.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.LocalizableReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.LocalizedMessages;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class XRefChecker
{

  public static enum Type
  {
    GENERIC,
    FONT,
    HYPERLINK,
    LINK,
    IMAGE,
    OBJECT,
    STYLESHEET,
    AUDIO,
    VIDEO,
    SVG_PAINT,
    SVG_CLIP_PATH,
    SVG_SYMBOL,
    REGION_BASED_NAV,
    SEARCH_KEY,
    NAV_TOC_LINK,
    NAV_PAGELIST_LINK,
    OVERLAY_TEXT_LINK,
    PICTURE_SOURCE,
    PICTURE_SOURCE_FOREIGN;
  }

  private static class URLReference
  {
    public final URL url;
    public final URL targetDoc;
    public final EPUBLocation location;
    public final Type type;

    public URLReference(URL url, Type type, EPUBLocation location)
    {
      try
      {
        this.url = url;
        this.type = type;
        this.location = location;
        this.targetDoc = url.withFragment(null);
      } catch (GalimatiasParseException e)
      {
        throw new AssertionError(e);
      }
    }

  }

  private static class ID
  {

    @SuppressWarnings("unused")
    public final String id;
    public final Type type;
    public final int position;

    public ID(String id, int position, Type type)
    {
      this.id = id;
      this.position = position;
      this.type = type;
    }

  }

  private static class Resource
  {

    public final OPFItem item;
    private final Map<String, ID> ids;
    public final boolean hasValidItemFallback;
    public final boolean hasValidImageFallback;

    Resource(OPFItem item, boolean hasValidItemFallback, boolean hasValidImageFallback)
    {
      this.item = item;
      this.hasValidItemFallback = hasValidItemFallback;
      this.hasValidImageFallback = hasValidImageFallback;
      this.ids = new HashMap<String, ID>();
    }

    /**
     * Returns the position of the given ID in the document represented by this
     * resource.
     * 
     * @return {@code -1} if the ID wasn't found in the document, or {@code 0}
     *         if the given ID is {@code null} or an empty string, or the
     *         1-based position of the ID otherwise.
     */
    public int getIDPosition(String id)
    {
      if (id == null || id.trim().isEmpty()) return 0;
      ID anchor = ids.get(id);
      return (anchor != null) ? anchor.position : -1;
    }

    // FIXME 2022 refactor ID registration
    // public boolean hasID(String id)
    // {
    // return ids.containsKey(id);
    // }
    //
    // public Type getIDType(String id)
    // {
    // return ids.get(id).type;
    // }

  }

  private static final Pattern REGEX_SVG_VIEW = Pattern.compile("svgView\\(.*\\)");

  private final Map<URL, Resource> resources = new HashMap<URL, Resource>();

  private final Set<URL> undeclared = new HashSet<URL>();

  private final List<URLReference> references = new LinkedList<URLReference>();

  private final Map<String, String> bindings = new HashMap<String, String>();

  private final Report report;

  private final OCFContainer container;

  private final EPUBVersion version;

  private final Locale locale;

  public XRefChecker(ValidationContext context)
  {
    Preconditions.checkArgument(context.container.isPresent());
    this.container = context.container.get();
    this.report = context.report;
    this.version = context.version;
    this.locale = (report instanceof LocalizableReport) ? ((LocalizableReport) report).getLocale()
        : Locale.ENGLISH;
  }

  public String getMimeType(URL resource)
  {
    return resources.containsKey(resource) ? resources.get(resource).item.getMimeType() : null;
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<OPFItem> getResource(URL url)
  {
    return (url == null || !resources.containsKey(url)) ? Optional.<OPFItem> absent()
        : Optional.of(resources.get(url).item);
  }

  /**
   * Returns set (possibly multiple) types of references to the given resource
   * 
   * @param path
   *          the path to a publication resource
   * @return an immutable {@link EnumSet} containing the types of references to
   *         {@code path}.
   */
  public Set<Type> getTypes(URL resource)
  {
    Preconditions.checkArgument(resource != null);
    ImmutableSet.Builder<Type> types = ImmutableSet.builder();
    for (URLReference reference : references)
    {
      if (resource.equals(URLUtils.docURL(reference.url)))
      {
        types.add(reference.type);
      }
    }
    return types.build();
  }

  // FIXME 2022 move binding registration to OPFHandler
  public Set<String> getBindingsMimeTypes()
  {
    return bindings.keySet();
  }

  public String getBindingHandlerId(String mimeType)
  {
    return bindings.get(mimeType);
  }

  public void registerBinding(String mimeType, String handlerId)
  {
    bindings.put(mimeType, handlerId);
  }

  // FIXME 2022 simplify signature: fallback info can be moved to OPFItem
  public void registerResource(OPFItem item, boolean hasValidItemFallback,
      boolean hasValidImageFallback)
  {
    // Note: Duplicate manifest items are already checked in OPFChecker.
    if (!resources.containsKey(item.getURL()))
    {
      resources.put(item.getURL(), new Resource(item, hasValidItemFallback, hasValidImageFallback));
    }
  }

  public void registerID(String id, Type type, EPUBLocation location)
  {
    if (id == null) return;
    Resource res = resources.get(location.url);
    Preconditions.checkArgument(res != null, "resource not registered");
    // Note: duplicate IDs are checked in schematron
    if (!res.ids.containsKey(id))
    {
      res.ids.put(id, new ID(id, res.ids.size() + 1, type));
    }
  }

  public void registerReference(URL url, Type type, EPUBLocation location)
  {
    if (url == null) return;

    // Do not register data URLs
    if ("data".equals(url.scheme()))
    {
      return;
    }

    // Remove query component of local URLs
    // FIXME 2022 check how to deal with local query strings
    // see http://code.google.com/p/epubcheck/issues/detail?id=190
    // see http://code.google.com/p/epubcheck/issues/detail?id=261
    if (url.query() != null && !container.isRemote(url))
    {
      try
      {
        url = url.withQuery(null);
      } catch (GalimatiasParseException e)
      {
        new AssertionError("could not remove URL query");
      }
    }

    // Create and register a new reference
    URLReference xref = new URLReference(url, type, location);
    references.add(xref);
    report.info(location.getPath(), FeatureEnum.RESOURCE, container.relativize(xref.targetDoc));
  }

  public void checkReferences()
  {
    // if (checkReference(reference)) checkReferenceSubtypes(reference);
    Queue<URLReference> tocLinks = new LinkedList<>();
    Queue<URLReference> pageListLinks = new LinkedList<>();
    Queue<URLReference> overlayLinks = new LinkedList<>();
    for (URLReference reference : references)
    {
      switch (reference.type)
      {
      case REGION_BASED_NAV:
        checkRegionBasedNav(reference);
        break;
      case NAV_TOC_LINK:
        tocLinks.add(reference);
        break;
      case NAV_PAGELIST_LINK:
        pageListLinks.add(reference);
        break;
      case OVERLAY_TEXT_LINK:
        overlayLinks.add(reference);
        break;
      default:
        checkReference(reference);
        break;
      }
    }
    checkReadingOrder(tocLinks, -1, -1);
    checkReadingOrder(overlayLinks, -1, -1);
  }

  private void checkReference(URLReference reference)
  {
    Resource targetResource = resources.get(reference.targetDoc);
    Resource hostResource = resources.get(reference.location.url);

    // Check remote resources
    if (container.isRemote(reference.url)
        // remote links and hyperlinks are not Publication Resources
        && !EnumSet.of(Type.LINK, Type.HYPERLINK).contains(reference.type)
        // spine items are checked in OPFChecker30
        && !(version == EPUBVersion.VERSION_3 && targetResource != null
            && targetResource.item.isInSpine())
        // audio, video, and fonts can be remote resources in EPUB 3
        && !(version == EPUBVersion.VERSION_3 && (targetResource != null
            // if the item is declared, check its mime type
            && (OPFChecker30.isAudioType(targetResource.item.getMimeType())
                || OPFChecker30.isVideoType(targetResource.item.getMimeType())
                || OPFChecker30.isFontType(targetResource.item.getMimeType()))
            // else, check if the reference is a type allowing remote resources
            || reference.type == Type.FONT || reference.type == Type.AUDIO
            || reference.type == Type.VIDEO)))
    {
      report.message(MessageId.RSC_006, reference.location.context(reference.targetDoc.toString()));
      return;
    }

    // Check undeclared resources
    if (targetResource == null)
    {
      // Report references to missing local resources
      if (!container.contains(reference.url) && !container.isRemote(reference.url))
      {
        // only as a WARNING for 'link' references in EPUB 3
        if (version == EPUBVersion.VERSION_3 && reference.type == Type.LINK)
        {
          report.message(MessageId.RSC_007w, reference.location,
              container.relativize(reference.targetDoc));
        }
        // by default as an ERROR
        else
        {
          report.message(MessageId.RSC_007, reference.location,
              container.relativize(reference.targetDoc));
        }
      }
      // Report undeclared Publication Resources (once)
      else if (!undeclared.contains(reference.targetDoc)
          // links and remote hyperlinks are not Publication Resources
          && !(reference.type == Type.LINK
              || container.isRemote(reference.targetDoc) && reference.type == Type.HYPERLINK))
      {
        undeclared.add(reference.targetDoc);
        report.message(MessageId.RSC_008, reference.location,
            container.relativize(reference.targetDoc));
      }
      return;
    }

    // Type-specific checks
    switch (reference.type)
    {
    case HYPERLINK:
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedItemType(targetResource.item.getMimeType(), version)
          && !OPFChecker.isDeprecatedBlessedItemType(targetResource.item.getMimeType())
          && !targetResource.hasValidItemFallback)
      {
        report.message(MessageId.RSC_010,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      if (/* !res.mimeType.equals("font/opentype") && */!targetResource.item.isInSpine())
      {
        report.message(MessageId.RSC_011,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      break;
    case IMAGE:
    case PICTURE_SOURCE:
    case PICTURE_SOURCE_FOREIGN:
      if (reference.url.fragment() != null
          && !targetResource.item.getMimeType().equals("image/svg+xml"))
      {
        report.message(MessageId.RSC_009,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedImageType(targetResource.item.getMimeType(), version))
      {
        if (version == EPUBVersion.VERSION_3 && reference.type == Type.PICTURE_SOURCE)
        {
          report.message(MessageId.MED_007, reference.location,
              container.relativize(reference.targetDoc), targetResource.item.getMimeType());
          return;
        }
        else if (reference.type == Type.IMAGE && !targetResource.hasValidImageFallback)
        {
          report.message(MessageId.MED_003, reference.location,
              container.relativize(reference.targetDoc), targetResource.item.getMimeType());
        }
      }
      break;
    case SEARCH_KEY:
      // TODO update when we support EPUB CFI
      if ((reference.url.fragment() == null || !reference.url.fragment().startsWith("epubcfi("))
          && !targetResource.item.isInSpine())
      {
        report.message(MessageId.RSC_021, reference.location,
            container.relativize(reference.targetDoc));
        return;
      }
      break;
    case STYLESHEET:
      if (reference.url.fragment() != null)
      {
        report.message(MessageId.RSC_013,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      // if mimeType is null, we should have reported an error already

      // Implementations are allowed to process any stylesheet
      // language they desire; so this is an
      // error only if no fallback is available.

      // Since the presence of a 'text/css' stylesheet link can be considered
      // a valid "built-in" fallback for a non-standard stylesheet (e.g.
      // XPGT), the fallback chain test is performed in OPSHandler instead.

      // See also:
      // https://github.com/IDPF/epubcheck/issues/244
      // https://github.com/IDPF/epubcheck/issues/271
      // https://github.com/IDPF/epubcheck/issues/541
      break;
    case SVG_CLIP_PATH:
    case SVG_PAINT:
    case SVG_SYMBOL:
      if (reference.url.fragment() == null)
      {
        report.message(MessageId.RSC_015, reference.location.context(reference.url));
        return;
      }
      break;
    default:
      break;
    }

    // Fragment integrity checks
    String fragment = reference.url.fragment();
    if (fragment != null && !fragment.isEmpty())
    {
      // EPUB CFI
      if (fragment.startsWith("epubcfi("))
      {
        // FIXME epubcfi currently not supported (see issue 150).
        return;
      }
      // Media fragments in Data Navigation Documents
      else if (fragment.contains("=") && hostResource != null && hostResource.item.getProperties()
          .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
      {
        // Ignore,
        return;
      }
      // SVG view fragments are ignored
      else if (targetResource.item.getMimeType().equals("image/svg+xml")
          && REGEX_SVG_VIEW.matcher(fragment).matches())
      {
        return;
      }
      // Fragment Identifier (by default)
      else if (!container.isRemote(reference.targetDoc))
      {
        ID anchor = targetResource.ids.get(fragment);
        if (anchor == null)
        {
          report.message(MessageId.RSC_012, reference.location.context(reference.url.toString()));
          return;
        }
        switch (reference.type)
        {
        case SVG_PAINT:
        case SVG_CLIP_PATH:
          if (anchor.type != reference.type)
          {
            report.message(MessageId.RSC_014, reference.location.context(reference.url.toString()));
            return;
          }
          break;
        case SVG_SYMBOL:
        case HYPERLINK:
          if (anchor.type != reference.type && anchor.type != Type.GENERIC)
          {
            report.message(MessageId.RSC_014, reference.location.context(reference.url.toString()));
            return;
          }
          break;
        default:
          break;
        }
      }
    }
  }

  private void checkRegionBasedNav(URLReference ref)
  {
    Preconditions.checkArgument(ref.type == Type.REGION_BASED_NAV);
    Resource res = resources.get(ref.targetDoc);
    if (!res.item.isFixedLayout())
    {
      report.message(MessageId.NAV_009, ref.location);
    }
  }

  private void checkReadingOrder(Queue<URLReference> references, int lastSpinePosition,
      int lastAnchorPosition)
  {
    // de-queue
    URLReference ref = references.poll();
    if (ref == null) return;

    Preconditions.checkArgument(ref.type == Type.NAV_PAGELIST_LINK || ref.type == Type.NAV_TOC_LINK
        || ref.type == Type.OVERLAY_TEXT_LINK);
    Resource res = resources.get(ref.targetDoc);

    // abort early if the link target is not a spine item (checked elsewhere)
    if (res == null || !res.item.isInSpine()) return;

    // check that the link is in spine order
    int targetSpinePosition = res.item.getSpinePosition();
    if (targetSpinePosition < lastSpinePosition)
    {
      String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(MessageId.NAV_011,
          "spine");

      if (ref.type == Type.OVERLAY_TEXT_LINK)
      {
        report.message(MessageId.MED_015, ref.location, container.relativize(ref.url),
            orderContext);
      }
      else
      {
        report.message(MessageId.NAV_011, ref.location,
            (ref.type == Type.NAV_TOC_LINK) ? "toc" : "page-list", container.relativize(ref.url),
            orderContext);
      }
      lastSpinePosition = targetSpinePosition;
      lastAnchorPosition = -1;
    }
    else
    {

      // if new spine item, reset last positions
      if (targetSpinePosition > lastSpinePosition)
      {
        lastSpinePosition = targetSpinePosition;
        lastAnchorPosition = -1;
      }

      // check that the fragment is in document order
      int targetAnchorPosition = res.getIDPosition(ref.url.fragment());
      if (targetAnchorPosition < lastAnchorPosition)
      {
        String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(MessageId.NAV_011,
            "document");
        if (ref.type == Type.OVERLAY_TEXT_LINK)
        {
          report.message(MessageId.MED_015, ref.location, container.relativize(ref.url),
              orderContext);
        }
        else
        {
          report.message(MessageId.NAV_011, ref.location,
              (ref.type == Type.NAV_TOC_LINK) ? "toc" : "page-list", container.relativize(ref.url),
              orderContext);
        }
      }
      lastAnchorPosition = targetAnchorPosition;
    }
    checkReadingOrder(references, lastSpinePosition, lastAnchorPosition);
  }
}

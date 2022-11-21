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

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.url.URLFragment;
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
    public static final class Builder
    {

      private URL url;
      private OPFItem item = null;
      private boolean hasItemFallback = false;
      private boolean hasImageFallback = false;
      public String mimetype;

      public Builder url(URL url)
      {
        this.url = url;
        return this;
      }

      public Builder item(OPFItem item)
      {
        this.url = item.getURL();
        this.item = item;
        this.mimetype = item.getMimeType();
        return this;
      }

      public Builder mimetype(String mimetype)
      {
        this.mimetype = mimetype;
        return this;
      }

      public Builder hasItemFallback(boolean hasItemFallback)
      {
        this.hasItemFallback = hasItemFallback;
        return this;
      }

      public Builder hasImageFallback(boolean hasImageFallback)
      {
        this.hasImageFallback = hasImageFallback;
        return this;
      }

      public Resource build()
      {
        return new Resource(this);
      }
    }

    private final URL url;
    private final String mimetype;
    private final Optional<OPFItem> item;
    private final Map<String, ID> ids;
    private final boolean hasItemFallback;
    private final boolean hasImageFallback;

    private Resource(Builder builder)
    {
      Preconditions.checkState(builder.url != null, "A URL or OPF Item must be provided");
      Preconditions.checkState(builder.mimetype != null, "A MIME type must be provided");
      Preconditions
          .checkState(builder.item == null || builder.item.getMimeType().equals(builder.mimetype));
      this.url = builder.url;
      this.item = Optional.fromNullable(builder.item);
      this.hasItemFallback = builder.hasItemFallback;
      this.hasImageFallback = builder.hasImageFallback;
      this.ids = new HashMap<String, ID>();
      this.mimetype = builder.mimetype;

    }

    /**
     * Returns the position of the given ID in the document represented by this
     * resource.
     * 
     * @return {@code -1} if the ID wasn't found in the document, or {@code 0}
     *           if the given ID is {@code null} or an empty string, or the
     *           1-based position of the ID otherwise.
     */
    public int getIDPosition(String id)
    {
      if (id == null || id.trim().isEmpty()) return 0;
      ID anchor = ids.get(id);
      return (anchor != null) ? anchor.position : -1;
    }

    public String getMimeType()
    {
      return mimetype;
    }

    public boolean hasItem()
    {
      return item.isPresent();
    }

    public OPFItem getItem()
    {
      return item.orNull();
    }

    public URL getURL()
    {
      return url;
    }

    public boolean hasItemFallback()
    {
      return hasItemFallback;
    }

    public boolean hasImageFallback()
    {
      return hasImageFallback;
    }

    public boolean isInSpine()
    {
      return item.isPresent() && item.get().isInSpine();
    }
  }

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
    return resources.containsKey(resource) ? resources.get(resource).getMimeType() : null;
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no
   * resource has been registered for the given path.
   */
  public Optional<OPFItem> getResource(URL url)
  {
    return (url == null || !resources.containsKey(url)) ? Optional.<OPFItem> absent()
        : Optional.fromNullable(resources.get(url).getItem());
  }

  /**
   * Returns set (possibly multiple) types of references to the given resource
   * 
   * @param path
   *        the path to a publication resource
   * @return an immutable {@link EnumSet} containing the types of references to
   *           {@code path}.
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

  public void registerResource(URL url, String mimetype)
  {
    Preconditions.checkArgument(url != null);
    if (!resources.containsKey(url))
    {
      resources.put(url, new Resource.Builder().url(url).mimetype(mimetype).build());
    }
  }

  // FIXME 2022 simplify signature: fallback info can be moved to OPFItem
  public void registerResource(OPFItem item, boolean hasValidItemFallback,
      boolean hasValidImageFallback)
  {
    Preconditions.checkArgument(item != null);
    // Note: Duplicate manifest items are already checked in OPFChecker.
    if (!resources.containsKey(item.getURL()))
    {

      resources.put(item.getURL(), new Resource.Builder().item(item)
          .hasItemFallback(hasValidItemFallback).hasImageFallback(hasValidImageFallback).build());
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

    // If it is a data URL, also register a new resource
    if ("data".equals(url.scheme()))
    {
      registerResource(url, URLUtils.getDataURLType(url));
    }
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
        checkReference(reference);
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
    // Retrieve the target resource
    Resource targetResource = resources.get(reference.targetDoc);
    String targetMimetype = (targetResource != null) ? targetResource.getMimeType() : "";

    // Parse the URL fragment
    URLFragment fragment = URLFragment.parse(reference.url, targetMimetype);

    // Check remote resources
    if (container.isRemote(reference.url))
    {
      // Check if the remote reference is allowed
      if (// remote links and hyperlinks are not Publication Resources
      !EnumSet.of(Type.LINK, Type.HYPERLINK).contains(reference.type)
          // spine items are checked in OPFChecker30
          && !(version == EPUBVersion.VERSION_3 && targetResource != null
              && targetResource.isInSpine())
          // audio, video, and fonts can be remote resources in EPUB 3
          && !(version == EPUBVersion.VERSION_3 && (targetResource != null
              // if the item is declared, check its mime type
              && (OPFChecker30.isAudioType(targetResource.getMimeType())
                  || OPFChecker30.isVideoType(targetResource.getMimeType())
                  || OPFChecker30.isFontType(targetResource.getMimeType()))
              // else, check if the reference is a type allowing remote
              // resources
              || reference.type == Type.FONT || reference.type == Type.AUDIO
              || reference.type == Type.VIDEO)))
      {
        report.message(MessageId.RSC_006,
            reference.location.context(reference.targetDoc.toString()));
        return;
      }
      // Check if the remote resource is using HTTPS
      else if (version == EPUBVersion.VERSION_3
          && !EnumSet.of(Type.LINK, Type.HYPERLINK).contains(reference.type)
          && !"https".equals(reference.url.scheme())
          // file URLs are disallowed and reported elsewhere
          && !"file".equals(reference.url.scheme()))
      {
        report.message(MessageId.RSC_031, reference.location, reference.url);
      }
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

      if ("epubcfi".equals(fragment.getScheme()))
      {
        break; // EPUB CFI is not supported
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedItemType(targetMimetype, version)
          && !OPFChecker.isDeprecatedBlessedItemType(targetMimetype)
          && !targetResource.hasItemFallback())
      {
        report.message(MessageId.RSC_010,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      if (/* !res.mimeType.equals("font/opentype") && */!targetResource.isInSpine())
      {
        report.message(MessageId.RSC_011,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      break;
    case IMAGE:
    case PICTURE_SOURCE:
    case PICTURE_SOURCE_FOREIGN:
      if ("epubcfi".equals(fragment.getScheme()))
      {
        break; // EPUB CFI is not supported
      }
      if (fragment.exists() && !MIMEType.SVG.is(targetMimetype))
      {
        report.message(MessageId.RSC_009,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedImageType(targetMimetype, version))
      {
        if (version == EPUBVersion.VERSION_3 && reference.type == Type.PICTURE_SOURCE)
        {
          report.message(MessageId.MED_007, reference.location,
              container.relativize(reference.targetDoc), targetMimetype);
          return;
        }
        else if (reference.type == Type.IMAGE && !targetResource.hasImageFallback())
        {
          report.message(MessageId.MED_003, reference.location,
              container.relativize(reference.targetDoc), targetMimetype);
        }
      }
      break;
    case OVERLAY_TEXT_LINK:
      if (!OPFChecker.isBlessedItemType(targetMimetype, version))
      {
        report.message(MessageId.RSC_010,
            reference.location.context(container.relativize(reference.url)));
        return;
      }
      break;
    case SEARCH_KEY:
      // TODO update when we support EPUB CFI
      if ((!fragment.exists() || !"epubcfi".equals(fragment.getScheme()))
          && !targetResource.isInSpine())
      {
        report.message(MessageId.RSC_021, reference.location,
            container.relativize(reference.targetDoc));
        return;
      }
      break;
    case STYLESHEET:
      if (fragment.exists())
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
      if (!fragment.exists())
      {
        report.message(MessageId.RSC_015, reference.location.context(reference.url));
        return;
      }
      break;
    default:
      break;
    }

    // Fragment integrity checks
    if (fragment.exists() && !fragment.isEmpty())
    {
      // Check media overlays requirements
      if (reference.type == Type.OVERLAY_TEXT_LINK)
      {
        // Check that references to XHTML indicate an element by ID
        if (MIMEType.XHTML.is(targetMimetype) && fragment.getId().isEmpty())
        {
          report.message(MessageId.MED_017, reference.location, fragment.toString());
        }
        // Check that references to SVG use a SVG fragment identifier
        else if (MIMEType.SVG.is(targetMimetype) && !fragment.isValid())
        {
          report.message(MessageId.MED_018, reference.location, fragment.toString());
        }
      }

      // Check ID-based fragments
      // Other fragment types (e.g. EPUB CFI) are not currently supported
      if (!fragment.getId().isEmpty() && !container.isRemote(reference.targetDoc))
      {
        ID targetID = targetResource.ids.get(fragment.getId());
        if (targetID == null)
        {
          report.message(MessageId.RSC_012, reference.location.context(reference.url.toString()));
          return;
        }
        switch (reference.type)
        {
        case SVG_PAINT:
        case SVG_CLIP_PATH:
          if (targetID.type != reference.type)
          {
            report.message(MessageId.RSC_014, reference.location.context(reference.url.toString()));
            return;
          }
          break;
        case SVG_SYMBOL:
        case HYPERLINK:
        case OVERLAY_TEXT_LINK:
          if (targetID.type != reference.type && targetID.type != Type.GENERIC)
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
    if (res != null && res.hasItem() && !res.getItem().isFixedLayout())
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
    if (res == null || !res.hasItem() || !res.getItem().isInSpine()) return;

    // check that the link is in spine order
    int targetSpinePosition = res.getItem().getSpinePosition();
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
      URLFragment fragment = URLFragment.parse(ref.url, res.getMimeType());
      int targetAnchorPosition = res.getIDPosition(fragment.getId());
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

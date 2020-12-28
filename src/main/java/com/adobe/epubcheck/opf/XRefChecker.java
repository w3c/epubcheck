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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.LocalizableReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.LocalizedMessages;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

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

  private static class Reference
  {
    public final String source;
    public final int lineNumber;
    public final int columnNumber;
    public final String value;
    public final String refResource;
    public final String fragment;
    public final Type type;

    public Reference(String srcResource, int srcLineNumber, int srcColumnNumber, String value,
        String refResource, String fragment, Type type)
    {
      this.source = srcResource;
      this.lineNumber = srcLineNumber;
      this.columnNumber = srcColumnNumber;
      this.value = value;
      this.refResource = refResource;
      this.fragment = fragment;
      this.type = type;
    }

  }

  private static class Anchor
  {

    @SuppressWarnings("unused")
    public final String id;
    public final Type type;
    public final int position;

    public Anchor(String id, int position, Type type)
    {
      this.id = id;
      this.position = position;
      this.type = type;
    }

  }

  private static class Resource
  {

    public final OPFItem item;
    public final Hashtable<String, Anchor> anchors;
    public final boolean hasValidItemFallback;
    public final boolean hasValidImageFallback;

    Resource(OPFItem item, boolean hasValidItemFallback, boolean hasValidImageFallback)
    {
      this.item = item;
      this.hasValidItemFallback = hasValidItemFallback;
      this.hasValidImageFallback = hasValidImageFallback;
      this.anchors = new Hashtable<String, Anchor>();
    }

    /**
     * Returns the position of the given ID in the document represented by this
     * resource.
     * 
     * @return {@code -1} if the ID wasn't found in the document, or {@code 0} if
     *         the given ID is {@code null} or an empty string, or the 1-based
     *         position of the ID otherwise.
     */
    public int getAnchorPosition(String id)
    {
      if (id == null || id.trim().isEmpty()) return 0;
      Anchor anchor = anchors.get(id);
      return (anchor != null) ? anchor.position : -1;
    }
  }

  private static final Pattern REGEX_SVG_VIEW = Pattern.compile("svgView\\(.*\\)");

  private final Map<String, Resource> resources = new HashMap<String, Resource>();

  private final HashSet<String> undeclared = new HashSet<String>();

  private final List<Reference> references = new LinkedList<Reference>();

  private final Map<String, String> bindings = new HashMap<String, String>();

  private final Report report;

  private final OCFPackage ocf;

  private final EPUBVersion version;

  private final Locale locale;

  public XRefChecker(OCFPackage ocf, Report report, EPUBVersion version)
  {
    this.ocf = ocf;
    this.report = report;
    this.version = version;
    this.locale = (report instanceof LocalizableReport) ? ((LocalizableReport) report).getLocale()
        : Locale.ENGLISH;
  }

  public String getMimeType(String path)
  {
    return resources.get(path) != null ? resources.get(path).item.getMimeType() : null;
  }

  /**
   * Returns an {@link Optional} containing a boolean indicating whether the
   * resource at the given path has a valid item fallback, or
   * {@link Optional#absent()} if no resource has been registered for the given
   * path.
   */
  public Optional<Boolean> hasValidFallback(String path)
  {
    return resources.get(path) != null ? Optional.of(resources.get(path).hasValidItemFallback)
        : Optional.<Boolean> absent();
  }

  /**
   * Returns an {@link Optional} containing the Package Document item for the
   * given Publication Resource path, or {@link Optional#absent()} if no resource
   * has been registered for the given path.
   */
  public Optional<OPFItem> getResource(String path)
  {
    return (path == null || !resources.containsKey(path)) ? Optional.<OPFItem> absent()
        : Optional.of(resources.get(path).item);
  }

  /**
   * Returns set (possibly multiple) types of refereences to the given resource
   * 
   * @param path
   *          the path to a publication resource
   * @return an immutable {@link EnumSet} containing the types of references to
   *         {@code path}.
   */
  public Set<Type> getTypes(String path)
  {
    LinkedList<Type> types = new LinkedList<>();
    for (Reference reference : references)
    {
      if (Preconditions.checkNotNull(path).equals(reference.refResource))
      {
        types.add(reference.type);
      }
    }
    return Sets.immutableEnumSet(types);
  }

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

  public void registerResource(OPFItem item, boolean hasValidItemFallback,
      boolean hasValidImageFallback)
  {
    // Note: Duplicate manifest items are already checked in OPFChecker.
    if (!resources.containsKey(item.getPath()))
    {
      resources.put(item.getPath(),
          new Resource(item, hasValidItemFallback, hasValidImageFallback));
    }
  }

  public void registerAnchor(String path, int lineNumber, int columnNumber, String id, Type type)
  {
    Resource res = Preconditions.checkNotNull(resources.get(path));
    // Note: duplicate IDs are checked in schematron
    if (!res.anchors.contains(id))
    {
      res.anchors.put(id, new Anchor(id, res.anchors.size() + 1, type));
    }
  }

  public void registerReference(String srcResource, int srcLineNumber, int srcColumnNumber,
      String ref, Type type)
  {
    if (ref.startsWith("data:"))
    {
      return;
    }
    // see http://code.google.com/p/epubcheck/issues/detail?id=190
    // see http://code.google.com/p/epubcheck/issues/detail?id=261
    int query = ref.indexOf('?');
    if (query >= 0 && !PathUtil.isRemote(ref))
    {
      ref = ref.substring(0, query).trim();
    }

    String refResource = PathUtil.removeFragment(ref);
    String refFragment = PathUtil.getFragment(ref);
    report.info(srcResource, FeatureEnum.RESOURCE, refResource);
    references.add(new Reference(srcResource, srcLineNumber, srcColumnNumber, ref, refResource,
        refFragment, type));

  }

  public void checkReferences()
  {
    // if (checkReference(reference)) checkReferenceSubtypes(reference);
    Queue<Reference> tocLinks = new LinkedList<>();
    Queue<Reference> pageListLinks = new LinkedList<>();
    Queue<Reference> overlayLinks = new LinkedList<>();
    for (Reference reference : references)
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
    checkReadingOrder(pageListLinks, -1, -1);
    checkReadingOrder(overlayLinks, -1, -1);
  }

  private void checkReference(Reference ref)
  {
    Resource res = resources.get(ref.refResource);
    Resource host = resources.get(ref.source);

    // Check remote resources
    if (PathUtil.isRemote(ref.refResource)
        // remote links and hyperlinks are not Publication Resources
        && !EnumSet.of(Type.LINK, Type.HYPERLINK).contains(ref.type)
        // spine items are checked in OPFChecker30
        && !(version == EPUBVersion.VERSION_3 && res != null && res.item.isInSpine())
        // audio, video, and fonts can be remote resources in EPUB 3
        && !(version == EPUBVersion.VERSION_3
            && EnumSet.of(Type.AUDIO, Type.VIDEO, Type.FONT).contains(ref.type)))
    {
      report.message(MessageId.RSC_006,
          EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource));
      return;
    }

    // Check undeclared resources
    if (res == null)
    {
      // Report references to missing local resources
      if (!ocf.hasEntry(ref.refResource) && !PathUtil.isRemote(ref.refResource))
      {
        // only as a WARNING for 'link' references in EPUB 3
        if (version == EPUBVersion.VERSION_3 && ref.type == Type.LINK)
        {
          report.message(MessageId.RSC_007w,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource),
              ref.refResource);
        }
        // by default as an ERROR
        else
        {
          report.message(MessageId.RSC_007,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource),
              ref.refResource);
        }
      }
      // Report undeclared Publication Resources (once)
      else if (!undeclared.contains(ref.refResource)
          // links and remote hyperlinks are not Publication Resources
          && !(ref.type == Type.LINK
              || PathUtil.isRemote(ref.refResource) && ref.type == Type.HYPERLINK))
      {
        undeclared.add(ref.refResource);
        report.message(MessageId.RSC_008,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource),
            ref.refResource);
      }
      return;
    }

    // Type-specific checks
    switch (ref.type)
    {
    case HYPERLINK:
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedItemType(res.item.getMimeType(), version)
          && !OPFChecker.isDeprecatedBlessedItemType(res.item.getMimeType())
          && !res.hasValidItemFallback)
      {
        report.message(MessageId.RSC_010,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber,
                ref.refResource + ((ref.fragment != null) ? '#' + ref.fragment : "")));
        return;
      }
      if (/* !res.mimeType.equals("font/opentype") && */!res.item.isInSpine())
      {
        report.message(MessageId.RSC_011,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber,
                ref.refResource + ((ref.fragment != null) ? '#' + ref.fragment : "")));
        return;
      }
      break;
    case IMAGE:
    case PICTURE_SOURCE:
    case PICTURE_SOURCE_FOREIGN:
      if (ref.fragment != null && !res.item.getMimeType().equals("image/svg+xml"))
      {
        report.message(MessageId.RSC_009, EPUBLocation.create(ref.source, ref.lineNumber,
            ref.columnNumber, ref.refResource + "#" + ref.fragment));
        return;
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedImageType(res.item.getMimeType()))
      {
        if (version == EPUBVersion.VERSION_3 && ref.type == Type.PICTURE_SOURCE) {
          report.message(MessageId.MED_007,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber),
              ref.refResource, res.item.getMimeType());
          return;
        }
        else if (ref.type == Type.IMAGE && !res.hasValidImageFallback) {
          report.message(MessageId.MED_003,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber),
              ref.refResource, res.item.getMimeType());
        }
      }
      break;
    case SEARCH_KEY:
      // TODO update when we support EPUB CFI
      if ((ref.fragment == null || !ref.fragment.startsWith("epubcfi(")) && !res.item.isInSpine())
      {
        report.message(MessageId.RSC_021,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), ref.refResource);
        return;
      }
      break;
    case STYLESHEET:
      if (ref.fragment != null)
      {
        report.message(MessageId.RSC_013, EPUBLocation.create(ref.source, ref.lineNumber,
            ref.columnNumber, ref.refResource + "#" + ref.fragment));
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
      if (ref.fragment == null)
      {
        report.message(MessageId.RSC_015,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource));
        return;
      }
      break;
    default:
      break;
    }

    // Fragment integrity checks
    if (ref.fragment != null)
    {
      // EPUB CFI
      if (ref.fragment.startsWith("epubcfi("))
      {
        // FIXME epubcfi currently not supported (see issue 150).
        return;
      }
      // Media fragments in Data Navigation Documents
      else if (ref.fragment.contains("=") && host != null && host.item.getProperties()
          .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
      {
        // Ignore,
        return;
      }
      // SVG view fragments are ignored
      else if (res.item.getMimeType().equals("image/svg+xml")
          && REGEX_SVG_VIEW.matcher(ref.fragment).matches())
      {
        return;
      }
      // Fragment Identifier (by default)
      else if (!PathUtil.isRemote(ref.refResource))
      {
        Anchor anchor = res.anchors.get(ref.fragment);
        if (anchor == null)
        {
          report.message(MessageId.RSC_012, EPUBLocation.create(ref.source, ref.lineNumber,
              ref.columnNumber, ref.refResource + "#" + ref.fragment));
          return;
        }
        switch (ref.type)
        {
        case SVG_PAINT:
        case SVG_CLIP_PATH:
          if (anchor.type != ref.type)
          {
            report.message(MessageId.RSC_014, EPUBLocation.create(ref.source, ref.lineNumber,
                ref.columnNumber, ref.refResource + "#" + ref.fragment));
            return;
          }
          break;
        case SVG_SYMBOL:
        case HYPERLINK:
          if (anchor.type != ref.type && anchor.type != Type.GENERIC)
          {
            report.message(MessageId.RSC_014, EPUBLocation.create(ref.source, ref.lineNumber,
                ref.columnNumber, ref.refResource + "#" + ref.fragment));
            return;
          }
          break;
        default:
          break;
        }
      }
    }
  }

  private void checkRegionBasedNav(Reference ref)
  {
    Preconditions.checkArgument(ref.type == Type.REGION_BASED_NAV);
    Resource res = resources.get(ref.refResource);
    if (!res.item.isFixedLayout())
    {
      report.message(MessageId.NAV_009,
          EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber));
    }
  }

  private void checkReadingOrder(Queue<Reference> references, int lastSpinePosition,
      int lastAnchorPosition)
  {
    // de-queue
    Reference ref = references.poll();
    if (ref == null) return;

    Preconditions
        .checkArgument(ref.type == Type.NAV_PAGELIST_LINK || ref.type == Type.NAV_TOC_LINK || ref.type == Type.OVERLAY_TEXT_LINK);
    Resource res = resources.get(ref.refResource);

    // abort early if the link target is not a spine item (checked elsewhere)
    if (res == null || !res.item.isInSpine()) return;

    // check that the link is in spine order
    int targetSpinePosition = res.item.getSpinePosition();
    if (targetSpinePosition < lastSpinePosition)
    {
      String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(MessageId.NAV_011,
          "spine");
      
      if (ref.type == Type.OVERLAY_TEXT_LINK) {
        report.message(MessageId.MED_015,
                EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), ref.value, orderContext);
      }
      else {
        report.message(MessageId.NAV_011,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber),
            (ref.type == Type.NAV_TOC_LINK) ? "toc" : "page-list", ref.value, orderContext);
        report.message(MessageId.INF_001,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), "https://github.com/w3c/publ-epub-revision/issues/1283");
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
      int targetAnchorPosition = res.getAnchorPosition(ref.fragment);
      if (targetAnchorPosition < lastAnchorPosition)
      {
        String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(MessageId.NAV_011,
            "document");
        if (ref.type == Type.OVERLAY_TEXT_LINK) {
            report.message(MessageId.MED_015,
                    EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), ref.value, orderContext);
        }
        else {
          report.message(MessageId.NAV_011,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber),
              (ref.type == Type.NAV_TOC_LINK) ? "toc" : "page-list", ref.value, orderContext);
          report.message(MessageId.INF_001,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), "https://github.com/w3c/publ-epub-revision/issues/1283");
        }
      }
      lastAnchorPosition = targetAnchorPosition;
    }
    checkReadingOrder(references, lastSpinePosition, lastAnchorPosition);
  }
}

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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class XRefChecker
{

  public static enum Type
  {
    GENERIC,
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
    SEARCH_KEY;
  }

  private static class Reference
  {
    public final String source;
    public final int lineNumber;
    public final int columnNumber;
    public final String refResource;
    public final String fragment;
    public final Type type;

    public Reference(String srcResource, int srcLineNumber, int srcColumnNumber, String refResource,
        String fragment, Type type)
    {
      this.source = srcResource;
      this.lineNumber = srcLineNumber;
      this.columnNumber = srcColumnNumber;
      this.refResource = refResource;
      this.fragment = fragment;
      this.type = type;
    }

  }

  private static class Anchor
  {

    public final String id;
    public final int lineNumber;
    public final int columnNumber;
    public final Type type;

    public Anchor(String id, int lineNumber, int columnNumber, Type type)
    {
      this.id = id;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
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
  }

  private final Hashtable<String, Resource> resources = new Hashtable<String, Resource>();

  private final HashSet<String> undeclared = new HashSet<String>();

  private final Vector<Reference> references = new Vector<Reference>();

  private final Hashtable<String, String> bindings = new Hashtable<String, String>();

  private final Report report;

  private final OCFPackage ocf;

  private final EPUBVersion version;

  public XRefChecker(OCFPackage ocf, Report report, EPUBVersion version)
  {
    this.ocf = ocf;
    this.report = report;
    this.version = version;

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
    if (!resources.contains(item.getPath()))
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
      res.anchors.put(id, new Anchor(id, lineNumber, columnNumber, type));
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
    if (query >= 0 && !ref.matches("^[^:/?#]+://.*"))
    {
      ref = ref.substring(0, query).trim();
    }

    int hash = ref.indexOf("#");
    String refResource;
    String refFragment;
    if (hash >= 0)
    {
      refResource = ref.substring(0, hash);
      refFragment = ref.substring(hash + 1);
    }
    else
    {
      refResource = ref;
      refFragment = null;
    }
    report.info(srcResource, FeatureEnum.RESOURCE, refResource);
    references.add(
        new Reference(srcResource, srcLineNumber, srcColumnNumber, refResource, refFragment, type));

  }

  public void checkReferences()
  {
    Enumeration<Reference> refs = references.elements();
    while (refs.hasMoreElements())
    {
      Reference ref = refs.nextElement();
      checkReference(ref);
    }

  }

  private void checkReference(Reference ref)
  {
    Resource res = resources.get(ref.refResource);
    Resource host = resources.get(ref.source);

    // Check undeclared resources
    if (res == null)
    {
      if (version == EPUBVersion.VERSION_3 && ref.type == Type.LINK)
      {
        if (ref.refResource.matches("^[^:/?#]+://.*") || ocf.hasEntry(ref.refResource))
        {
          return;
        }
        else
        {
          report.message(MessageId.RSC_007w,
              EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource),
              ref.refResource);
        }
      }
      else if (ref.refResource.matches("^[^:/?#]+://.*") && !(version == EPUBVersion.VERSION_3
          && (ref.type == Type.AUDIO || ref.type == Type.VIDEO)))
      {
        report.message(MessageId.RSC_006,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource));
      }
      else if (!ocf.hasEntry(ref.refResource) && !ref.refResource.matches("^[^:/?#]+://.*"))
      {
        report.message(MessageId.RSC_007,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber, ref.refResource),
            ref.refResource);

      }
      else if (!undeclared.contains(ref.refResource))
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
      }
      if (/* !res.mimeType.equals("font/opentype") && */!res.item.isInSpine())
      {
        report.message(MessageId.RSC_011,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber,
                ref.refResource + ((ref.fragment != null) ? '#' + ref.fragment : "")));
      }
      break;
    case IMAGE:
      if (ref.fragment != null)
      {
        report.message(MessageId.RSC_009, EPUBLocation.create(ref.source, ref.lineNumber,
            ref.columnNumber, ref.refResource + "#" + ref.fragment));
        return;
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedImageType(res.item.getMimeType()) && !res.hasValidImageFallback)
      {
        report.message(MessageId.MED_003,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber),
            res.item.getMimeType());
      }
      break;
    case REGION_BASED_NAV:
      if (!res.item.isFixedLayout())
      {
        report.message(MessageId.NAV_009,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber));
      }
      return;
    case SEARCH_KEY:
      // TODO update when we support EPUB CFI
      if ((ref.fragment == null || !ref.fragment.startsWith("epubcfi(")) && !res.item.isInSpine())
      {
        report.message(MessageId.RSC_021,
            EPUBLocation.create(ref.source, ref.lineNumber, ref.columnNumber), ref.refResource);
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
      // Fragment Identifier (by default)
      else
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
          }
          break;
        case SVG_SYMBOL:
        case HYPERLINK:
          if (anchor.type != ref.type && anchor.type != Type.GENERIC)
          {
            report.message(MessageId.RSC_014, EPUBLocation.create(ref.source, ref.lineNumber,
                ref.columnNumber, ref.refResource + "#" + ref.fragment));
          }
          break;
        default:
          break;
        }
      }

    }

  }
}

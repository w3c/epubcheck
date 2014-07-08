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

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;

import java.util.*;

public class XRefChecker
{

  public static final int RT_GENERIC = 0;

  public static final int RT_HYPERLINK = 1;

  public static final int RT_IMAGE = 2;

  public static final int RT_OBJECT = 3;

  public static final int RT_STYLESHEET = 4;

  public static final int RT_AUDIO = 5;

  public static final int RT_VIDEO = 6;

  public static final int RT_SVG_PAINT = 0x10;

  public static final int RT_SVG_CLIP_PATH = 0x11;

  public static final int RT_SVG_SYMBOL = 0x12;

  private class Reference
  {
    final String resource;

    final int lineNumber;

    final int columnNumber;

    final String refResource;

    final String fragment;

    final int type;

    public Reference(String srcResource, int srcLineNumber,
        int srcColumnNumber, String refResource, String fragment,
        int type)
    {
      this.fragment = fragment;
      this.lineNumber = srcLineNumber;
      this.columnNumber = srcColumnNumber;
      this.refResource = refResource;
      this.resource = srcResource;
      this.type = type;
    }

  }

  private class Anchor
  {

    final String id;

    final int lineNumber;

    final int columnNumber;

    final int type;

    public Anchor(String id, int lineNumber, int columnNumber, int type)
    {
      this.id = id;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
      this.type = type;
    }

  }

  private class Resource
  {

    final String resource;

    final String mimeType;

    final Hashtable<String, Anchor> anchors;

    final boolean inSpine;

    final boolean hasValidItemFallback;

    final boolean hasValidImageFallback;

    Resource(String resource, String type, boolean inSpine,
        boolean hasValidItemFallback, boolean hasValidImageFallback)
    {
      this.mimeType = type;
      this.resource = resource;
      this.inSpine = inSpine;
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
    return resources.get(path) != null ? resources.get(path).mimeType
        : null;
  }

  public Set<String> getBindingsMimeTypes()
  {
    return bindings.keySet();
  }

  public String getBindingHandlerSrc(String mimeType)
  {
    return bindings.get(mimeType);
  }

  public void registerBinding(String mimeType, String handlerSrc)
  {
    bindings.put(mimeType, handlerSrc);
  }

  public void registerResource(String resource, String mimeType,
      boolean inSpine, boolean hasValidItemFallback,
      boolean hasValidImageFallback)
  {
    if (resources.get(resource) != null)
    {
      throw new IllegalArgumentException("duplicate resource: "
          + resource);
    }
    resources.put(resource, new Resource(resource, mimeType, inSpine,
        hasValidItemFallback, hasValidImageFallback));
  }

  public void registerAnchor(String resource, int lineNumber,
      int columnNumber, String id, int type)
  {
    Resource res = resources.get(resource);
    if (res == null)
    {
      throw new IllegalArgumentException("unregistered resource: "
          + resource);
    }
    if (res.anchors.get(id) != null)
    {
      throw new IllegalArgumentException("duplicate id: " + id);
    }
    res.anchors.put(id, new Anchor(id, lineNumber, columnNumber, type));
  }

  void registerReference(String srcResource, int srcLineNumber,
      int srcColumnNumber, String refResource, String refFragment,
      int type)
  {
    if (refResource.startsWith("data:"))
    {
      return;
    }
    report.info(srcResource, FeatureEnum.RESOURCE, refResource);
    references.add(new Reference(srcResource, srcLineNumber,
        srcColumnNumber, refResource, refFragment, type));
  }

  public void registerReference(String srcResource, int srcLineNumber,
      int srcColumnNumber, String ref, int type)
  {
    if (ref.startsWith("data:"))
    {
      return;
    }
		// see http://code.google.com/p/epubcheck/issues/detail?id=190
		// see http://code.google.com/p/epubcheck/issues/detail?id=261
    int query = ref.indexOf('?');
		if (query >= 0 && !ref.matches("^[^:/?#]+://.*")) {
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

    registerReference(srcResource, srcLineNumber, srcColumnNumber,
        refResource, refFragment, type);
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
    if (res == null)
	  {
      if(ref.refResource.matches("^[^:/?#]+://.*")
          && !(version == EPUBVersion.VERSION_3 && (ref.type == RT_AUDIO || ref.type == RT_VIDEO)))
      {
        report.message(MessageId.RSC_006,
            new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));
      }
      else if (!ocf.hasEntry(ref.refResource) && !ref.refResource.matches("^[^:/?#]+://.*"))
      {
        report.message(MessageId.RSC_007,
            new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));

      }
      else if (!undeclared.contains(ref.refResource))
      {
        undeclared.add(ref.refResource);
        report.message(MessageId.RSC_008,
            new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));
      }
      return;
    }

    if (ref.fragment == null)
    {
      switch (ref.type)
      {
        case RT_SVG_PAINT:
        case RT_SVG_CLIP_PATH:
        case RT_SVG_SYMBOL:
          report.message(MessageId.RSC_015,
              new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));
          break;
        case RT_HYPERLINK:
          // if mimeType is null, we should have reported an error already
          if (res.mimeType != null
              && !OPFChecker.isBlessedItemType(res.mimeType, version)
              && !OPFChecker
              .isDeprecatedBlessedItemType(res.mimeType)
              && !res.hasValidItemFallback)
          {
            report.message(MessageId.RSC_010,
                new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));
          }
          if (/* !res.mimeType.equals("font/opentype") && */!res.inSpine)
          {
            report.message(MessageId.RSC_011,
                new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource));
          }
          break;
        case RT_IMAGE:
          // if mimeType is null, we should have reported an error already
          if (res.mimeType != null
              && !OPFChecker.isBlessedImageType(res.mimeType)
              && !res.hasValidImageFallback)
          {
            report.message(MessageId.MED_003,
                new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber),
                res.mimeType);
          }
          break;
        case RT_STYLESHEET:
          // if mimeType is null, we should have reported an error already

          // Implementations are allowed to process any stylesheet
				// language they desire; so this is an
				// error only if no fallback is available.
				// See also:
				// https://code.google.com/p/epubcheck/issues/detail?id=244

				if (res.mimeType != null
						&& !OPFChecker.isBlessedStyleType(res.mimeType)
						&& !OPFChecker
								.isDeprecatedBlessedStyleType(res.mimeType)
						&& !res.hasValidItemFallback)
        {
          report.message(MessageId.CSS_010,
              new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber),
              res.mimeType);
        }
          break;
      }
    }
    else
    { //if (ref.fragment == null) {
      if (ref.fragment.startsWith("epubcfi("))
      {
        //Issue 150
        return;
      }

      switch (ref.type)
      {
        case RT_HYPERLINK:
          // if mimeType is null, we should have reported an error already
          if (res.mimeType != null
              && !OPFChecker.isBlessedItemType(res.mimeType, version)
              && !OPFChecker
              .isDeprecatedBlessedItemType(res.mimeType)
              && !res.hasValidItemFallback)
          {
            report.message(MessageId.RSC_010,
                new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
          }
          if (!res.inSpine)
          {
            report.message(MessageId.RSC_011,
                new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
          }
          break;
        case RT_IMAGE:
          report.message(MessageId.RSC_009,
              new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
          break;
        case RT_STYLESHEET:
          report.message(MessageId.RSC_013,
              new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
          break;
      }
      Anchor anchor = res.anchors.get(ref.fragment);
      if (anchor == null)
      {
        report.message(MessageId.RSC_012,
            new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
      }
      else
      {
        switch (ref.type)
        {
          case RT_SVG_PAINT:
          case RT_SVG_CLIP_PATH:
            if (anchor.type != ref.type)
            {
              report.message(MessageId.RSC_014,
                  new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
            }
            break;
          case RT_SVG_SYMBOL:
          case RT_HYPERLINK:
            if (anchor.type != ref.type && anchor.type != RT_GENERIC)
            {
              report.message(MessageId.RSC_014,
                  new MessageLocation(ref.resource, ref.lineNumber, ref.columnNumber, ref.refResource + "#" + ref.fragment));
            }
            break;
        }
      }
    }
  }
}

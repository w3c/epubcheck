/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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
import com.adobe.epubcheck.util.*;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OPFHandler30 extends OPFHandler
{
  private final HashSet<String> prefixSet;

  private static final String[] predefinedPrefixes = {"dcterms", "marc", "media", "onix", "xsd"};

  private static final HashSet<String> metaPropertySet;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("alternate-script");
    set.add("display-seq");
    set.add("file-as");
    set.add("group-position");
    set.add("identifier-type");
    set.add("meta-auth");
    set.add("role");
    set.add("title-type");
    metaPropertySet = set;
  }

  private static final HashSet<String> itemrefSet;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("page-spread-right");
    set.add("page-spread-left");
    itemrefSet = set;
  }

  private static final HashSet<String> linkRelSet;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("marc21xml-record");
    set.add("mods-record");
    set.add("onix-record");
    set.add("xml-signature");
    set.add("xmp-record");
    linkRelSet = set;
  }

  private static final HashSet<String> itemPropertySet;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("cover-image");
    set.add("mathml");
    set.add("nav");
    set.add("remote-resources");
    set.add("scripted");
    set.add("svg");
    set.add("switch");
    itemPropertySet = set;
  }

  private static final HashMap<String, String> itemPropertyTypeMap;

  static
  {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("cover-image", "image/gif image/jpeg image/png image/svg+xml");
    map.put("mathml", "application/xhtml+xml image/svg+xml");// ops
    map.put("nav", "application/xhtml+xml");
    map.put("remote-resources", "application/xhtml+xml image/svg+xml text/css");// ops + css
    map.put("scripted", "application/xhtml+xml image/svg+xml");// ops
    map.put("svg", "application/xhtml+xml");// ops
    map.put("switch", "application/xhtml+xml image/svg+xml");// ops
    itemPropertyTypeMap = map;
  }

  OPFHandler30(String path, Report report, XRefChecker xrefChecker, XMLParser parser, EPUBVersion version)
  {
    super(path, report, xrefChecker, parser, version);
    prefixSet = new HashSet<String>();
    Collections.addAll(prefixSet, predefinedPrefixes);
  }

  public void startElement()
  {
    super.startElement();

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("package"))
    {
      HandlerUtil.processPrefixes(e.getAttribute("prefix"), prefixSet,
          report, path, parser.getLineNumber(),
          parser.getColumnNumber());
    }
    else if (name.equals("meta"))
    {
      processMeta(e);
    }
    else if (name.equals("link"))
    {
      processLink(e);
    }
    else if (name.equals("item"))
    {
      processItemProperties(e.getAttribute("properties"), e.getAttribute("media-type"));
    }
    else if (name.equals("itemref"))
    {
      processItemrefProperties(e.getAttribute("properties"));
    }
    else if (name.equals("mediaType"))
    {
      processBinding(e);
    }
  }

  private void processBinding(XMLElement e)
  {
    String mimeType = e.getAttribute("media-type");
    String handlerId = e.getAttribute("handler");

    if ((mimeType != null) && (handlerId != null))
    {
      if (OPFChecker30.isCoreMediaType(mimeType))
      {
        report.message(MessageId.OPF_008,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
            mimeType);
        return;
      }

      if (xrefChecker != null && xrefChecker.getBindingHandlerSrc(mimeType) != null)
      {
        report.message(MessageId.OPF_009,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
            mimeType, xrefChecker.getBindingHandlerSrc(mimeType));
        return;
      }

      OPFItem handler = itemMapById.get(handlerId);
      if (handler != null && xrefChecker != null)
      {
        xrefChecker.registerBinding(mimeType, handler.path);
      }
    }
  }

  private void processLink(XMLElement e)
  {
    processLinkRel(e.getAttribute("rel"));
    // needs refactor: its problematic to register
    // link resources as items
    String id = e.getAttribute("id");
    String href = e.getAttribute("href");
	if (href != null && !href.matches("^[^:/?#]+://.*"))
	{  
      try
      {
        href = PathUtil.resolveRelativeReference(path, href, null);
      }
      catch (IllegalArgumentException ex)
      {
        report.message(MessageId.OPF_010,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), href),
            ex.getMessage());
        href = null;
      }
    }
        if (href != null && href.matches("^[^:/?#]+://.*")) {
      report.info(path, FeatureEnum.REFERENCE, href);
    }

    String mimeType = e.getAttribute("media-type");
    OPFItem item = new OPFItem(id, href, mimeType, "", "", "", null,
        parser.getLineNumber(), parser.getColumnNumber());

    if (id != null)
    {
      itemMapById.put(id, item);
    }

    //if (href != null) {
    //mgy: awaiting proper refactor, only add these if local
		if (href != null && !href.matches("^[^:/?#]+://.*"))
    {
      itemMapByPath.put(href, item);
      items.add(item);
    }
  }

  private void processItemrefProperties(String property)
  {
    if (property == null)
    {
      return;
    }

    int propertiesNumber = MetaUtils.validateProperties(property,
        itemrefSet, prefixSet, path, parser.getLineNumber(),
        parser.getColumnNumber(), report, false).size();

    if (propertiesNumber == 2)
    {
      report.message(MessageId.OPF_011, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
  }

  private void processItemProperties(String property, String mimeType)
  {
    if (property == null)
    {
      return;
    }

    Set<String> properties = MetaUtils.validateProperties(property,
        itemPropertySet, prefixSet, path, parser.getLineNumber(),
        parser.getColumnNumber(), report, false);
    mimeType = mimeType.trim();
    for (String propertyValue : properties)
    {
      boolean match = false;
      String expectedType = itemPropertyTypeMap.get(propertyValue);
      String expectedTypeArray[] = expectedType.split(" ");

      for (String expectedTypeItem : expectedTypeArray)
      {
        if (expectedTypeItem.equals(mimeType))
        {
          match = true;
          break;
        }
      }
      if (!match)
      {
        report.message(MessageId.OPF_012,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
            propertyValue,
            mimeType);
      }
    }
  }

  private void processLinkRel(String rel)
  {
    if (rel != null)
    {

      MetaUtils.validateProperties(rel, linkRelSet, prefixSet, path,
          parser.getLineNumber(), parser.getColumnNumber(),
          report, false);
    }
  }

  private void processMeta(XMLElement e)
  {
    processMetaProperty(e.getAttribute("property"));
    processMetaScheme(e.getAttribute("scheme"));
  }

  private void processMetaScheme(String scheme)
  {
    if (scheme != null)
    {

      MetaUtils.validateProperties(scheme, null, prefixSet, path,
          parser.getLineNumber(), parser.getColumnNumber(), report, true);
    }
  }

  private void processMetaProperty(String property)
  {
    if (property != null)
    {

      MetaUtils.validateProperties(property, metaPropertySet, prefixSet,
          path, parser.getLineNumber(), parser.getColumnNumber(), report,
          true);
    }
  }
}

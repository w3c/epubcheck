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

import static com.adobe.epubcheck.vocab.ForeignVocabs.*;
import static com.adobe.epubcheck.vocab.PackageVocabs.*;

import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.EnumVocab;
import com.adobe.epubcheck.vocab.MediaOverlaysVocab;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEMREF_PROPERTIES;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.RenditionVocabs;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class OPFHandler30 extends OPFHandler
{

  private static final Map<String, Vocab> RESERVED_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put(DCTERMS_PREFIX, DCTERMS_VOCAB).put(MARC_PREFIX, MARC_VOCAB)
      .put(MediaOverlaysVocab.PREFIX, MediaOverlaysVocab.VOCAB).put(ONIX_PREFIX, ONIX_VOCAB)
      .put(XSD_PREFIX, XSD_VOCAB).build();

  private static final Map<String, Vocab> KNOWN_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .put(DCTERMS_URI, DCTERMS_VOCAB).put(MARC_URI, MARC_VOCAB)
      .put(MediaOverlaysVocab.PREFIX, MediaOverlaysVocab.VOCAB).put(ONIX_URI, ONIX_VOCAB)
      .put(XSD_URI, XSD_VOCAB).build();

  private static final Map<String, Vocab> DEFAULT_ITEMREF_VOCABS = ImmutableMap.of("",
      ITEMREF_VOCAB, RenditionVocabs.PREFIX, RenditionVocabs.ITEMREF_VOCAB);
  private static final Map<String, Vocab> DEFAULT_ITEM_VOCABS = ImmutableMap.of("", ITEM_VOCAB,
      RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB);
  private static final Map<String, Vocab> DEFAULT_LINKREL_VOCABS = ImmutableMap.of("",
      LINKREL_VOCAB);
  private static final Map<String, Vocab> DEFAULT_META_VOCABS = ImmutableMap.of("", META_VOCAB,
      RenditionVocabs.PREFIX, RenditionVocabs.META_VOCAB);

  private static final Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(PACKAGE_VOCAB_URI,
      LINKREL_VOCAB_URI, RenditionVocabs.URI);

  private Map<String, Vocab> itemrefVocabs;
  private Map<String, Vocab> itemVocabs;
  private Map<String, Vocab> metaVocabs;
  private Map<String, Vocab> linkrelVocabs;

  OPFHandler30(String path, Report report, XRefChecker xrefChecker, XMLParser parser,
      EPUBVersion version)
  {
    super(path, report, xrefChecker, parser, version);
  }

  public void startElement()
  {
    super.startElement();

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("package"))
    {

      Map<String, Vocab> vocabs = VocabUtil.parsePrefixDeclaration(e.getAttribute("prefix"),
          RESERVED_VOCABS, KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, new MessageLocation(path,
              parser.getLineNumber(), parser.getColumnNumber()));
      itemrefVocabs = new ImmutableMap.Builder<String, Vocab>().putAll(vocabs)
          .putAll(DEFAULT_ITEMREF_VOCABS).build();
      itemVocabs = new ImmutableMap.Builder<String, Vocab>().putAll(vocabs)
          .putAll(DEFAULT_ITEM_VOCABS).build();
      metaVocabs = new ImmutableMap.Builder<String, Vocab>().putAll(vocabs)
          .putAll(DEFAULT_META_VOCABS).build();
      linkrelVocabs = new ImmutableMap.Builder<String, Vocab>().putAll(vocabs)
          .putAll(DEFAULT_LINKREL_VOCABS).build();
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
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()), mimeType);
        return;
      }

      if (xrefChecker != null && xrefChecker.getBindingHandlerSrc(mimeType) != null)
      {
        report.message(MessageId.OPF_009,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()), mimeType,
            xrefChecker.getBindingHandlerSrc(mimeType));
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
      } catch (IllegalArgumentException ex)
      {
        report.message(MessageId.OPF_010,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), href),
            ex.getMessage());
        href = null;
      }
    }
    if (href != null && href.matches("^[^:/?#]+://.*"))
    {
      report.info(path, FeatureEnum.REFERENCE, href);
    }

    String mimeType = e.getAttribute("media-type");
    OPFItem item = new OPFItem(id, href, mimeType, "", "", "", null, parser.getLineNumber(),
        parser.getColumnNumber());

    if (id != null)
    {
      itemMapById.put(id, item);
    }

    // if (href != null) {
    // mgy: awaiting proper refactor, only add these if local
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

    Set<Property> properties = VocabUtil.parsePropertyList(property, itemrefVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    Set<ITEMREF_PROPERTIES> propSet = Property.filter(properties, ITEMREF_PROPERTIES.class);

    if (propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_LEFT)
        && propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_RIGHT))
    {
      report.message(MessageId.OPF_011,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
  }

  private void processItemProperties(String property, String mimeType)
  {
    if (property == null)
    {
      return;
    }

    Set<Property> properties = VocabUtil.parsePropertyList(property, itemVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    Set<ITEM_PROPERTIES> itemProps = Property.filter(properties, ITEM_PROPERTIES.class);

    mimeType = mimeType.trim();
    for (ITEM_PROPERTIES itemProp : itemProps)
    {
      if (!itemProp.allowedOnTypes().contains(mimeType))
      {
        report.message(MessageId.OPF_012,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
            EnumVocab.ENUM_TO_NAME.apply(itemProp), mimeType);
      }
    }
  }

  private void processLinkRel(String rel)
  {
    VocabUtil.parsePropertyList(rel, linkrelVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
  }

  private void processMeta(XMLElement e)
  {
    processMetaProperty(e.getAttribute("property"));
    processMetaScheme(e.getAttribute("scheme"));
  }

  private void processMetaScheme(String scheme)
  {

    VocabUtil.parseProperty(scheme, metaVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
  }

  private void processMetaProperty(String property)
  {
    VocabUtil.parseProperty(property, metaVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
  }
}

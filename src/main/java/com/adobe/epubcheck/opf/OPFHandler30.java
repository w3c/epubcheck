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

import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.EnumVocab;
import com.adobe.epubcheck.vocab.MediaOverlaysVocab;
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
      .put(DCTERMS_PREFIX, DCTERMS_VOCAB).put(MARC_PREFIX, MARC_VOCAB).put(ONIX_PREFIX, ONIX_VOCAB)
      .put(SCHEMA_PREFIX, SCHEMA_VOCAB).put(XSD_PREFIX, XSD_VOCAB).build();
  private static final Map<String, Vocab> RESERVED_META_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", META_VOCAB).put(MediaOverlaysVocab.PREFIX, MediaOverlaysVocab.VOCAB)
      .put(RenditionVocabs.PREFIX, RenditionVocabs.META_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_ITEM_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", ITEM_VOCAB).put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_ITEMREF_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", ITEMREF_VOCAB).put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, RenditionVocabs.ITEMREF_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_LINKREL_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", LINKREL_VOCAB).put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB).putAll(RESERVED_VOCABS).build();

  private static final Map<String, Vocab> KNOWN_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .put(DCTERMS_URI, DCTERMS_VOCAB).put(MARC_URI, MARC_VOCAB).put(ONIX_URI, ONIX_VOCAB)
      .put(SCHEMA_URI, SCHEMA_VOCAB).put(XSD_URI, XSD_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_META_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(MediaOverlaysVocab.URI, MediaOverlaysVocab.VOCAB)
      .put(RenditionVocabs.URI, RenditionVocabs.META_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_ITEM_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, VocabUtil.EMPTY_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_ITEMREF_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, RenditionVocabs.ITEMREF_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_LINKREL_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, VocabUtil.EMPTY_VOCAB).build();

  private static final Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(PACKAGE_VOCAB_URI,
      LINKREL_VOCAB_URI);

  private Map<String, Vocab> itemrefVocabs;
  private Map<String, Vocab> itemVocabs;
  private Map<String, Vocab> metaVocabs;
  private Map<String, Vocab> linkrelVocabs;

  OPFHandler30(ValidationContext context, XMLParser parser)
  {
    super(context, parser);
  }

  public void startElement()
  {
    super.startElement();

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("package"))
    {
      // Note: the #parsePrefixDeclaration is called once for each "class" of
      // properties (meta+scheme, itemref, item, and link) so that default and
      // reserved vocabs can be set appropriately (e.g. the default vocab or
      // rendition vocab for 'meta' properties is not the same as for the 'item'
      // properties)
      // Messages are reported only on the first invocation; a quiet reporter is
      // used for subsequent invocations.
      String prefixDecl = e.getAttribute("prefix");
      MessageLocation loc = new MessageLocation(path, parser.getLineNumber(),
          parser.getColumnNumber());
      metaVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_META_VOCABS,
          KNOWN_META_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, loc);
      itemVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_ITEM_VOCABS,
          KNOWN_ITEM_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
      itemrefVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_ITEMREF_VOCABS,
          KNOWN_ITEMREF_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
      linkrelVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_LINKREL_VOCABS,
          KNOWN_LINKREL_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
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

      if (context.xrefChecker.isPresent()
          && context.xrefChecker.get().getBindingHandlerSrc(mimeType) != null)
      {
        report.message(MessageId.OPF_009,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()), mimeType,
            context.xrefChecker.get().getBindingHandlerSrc(mimeType));
        return;
      }

      OPFItem handler = itemMapById.get(handlerId);
      if (handler != null && context.xrefChecker.isPresent())
      {
        context.xrefChecker.get().registerBinding(mimeType, handler.path);
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
    if ("metadata".equals(e.getParent().getName()))
    {
      OPFItem item = new OPFItem(id, href, mimeType, null, null, "", null, parser.getLineNumber(),
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

  }

  private void processItemrefProperties(String property)
  {
    if (property == null)
    {
      return;
    }

    /* Set<Property> properties = */VocabUtil.parsePropertyList(property, itemrefVocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));

    // NOTE:
    // Checked with Schematron, although the code below is more prefix-safe

    // Set<ITEMREF_PROPERTIES> propSet = Property.filter(properties,
    // ITEMREF_PROPERTIES.class);
    // if (propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_LEFT)
    // && propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_RIGHT))
    // {
    // report.message(MessageId.OPF_011,
    // new MessageLocation(path, parser.getLineNumber(),
    // parser.getColumnNumber()));
    // }
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

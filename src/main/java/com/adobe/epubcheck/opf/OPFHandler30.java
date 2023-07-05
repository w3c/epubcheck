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

import static com.adobe.epubcheck.vocab.ForeignVocabs.DCTERMS_PREFIX;
import static com.adobe.epubcheck.vocab.ForeignVocabs.DCTERMS_URI;
import static com.adobe.epubcheck.vocab.ForeignVocabs.DCTERMS_VOCAB;
import static com.adobe.epubcheck.vocab.ForeignVocabs.MARC_PREFIX;
import static com.adobe.epubcheck.vocab.ForeignVocabs.MARC_URI;
import static com.adobe.epubcheck.vocab.ForeignVocabs.MARC_VOCAB;
import static com.adobe.epubcheck.vocab.ForeignVocabs.ONIX_PREFIX;
import static com.adobe.epubcheck.vocab.ForeignVocabs.ONIX_URI;
import static com.adobe.epubcheck.vocab.ForeignVocabs.ONIX_VOCAB;
import static com.adobe.epubcheck.vocab.ForeignVocabs.SCHEMA_PREFIX;
import static com.adobe.epubcheck.vocab.ForeignVocabs.SCHEMA_URI;
import static com.adobe.epubcheck.vocab.ForeignVocabs.SCHEMA_VOCAB;
import static com.adobe.epubcheck.vocab.ForeignVocabs.XSD_PREFIX;
import static com.adobe.epubcheck.vocab.ForeignVocabs.XSD_URI;
import static com.adobe.epubcheck.vocab.ForeignVocabs.XSD_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.ITEMREF_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.ITEMREF_VOCAB_URI;
import static com.adobe.epubcheck.vocab.PackageVocabs.ITEM_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.ITEM_VOCAB_URI;
import static com.adobe.epubcheck.vocab.PackageVocabs.LINKREL_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.LINK_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.LINK_VOCAB_URI;
import static com.adobe.epubcheck.vocab.PackageVocabs.META_VOCAB;
import static com.adobe.epubcheck.vocab.PackageVocabs.META_VOCAB_CAMEL;
import static com.adobe.epubcheck.vocab.PackageVocabs.META_VOCAB_URI;

import java.util.Deque;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.w3c.epubcheck.core.references.Reference;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.messages.LocalizedMessages;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.MetadataSet.Metadata;
import com.adobe.epubcheck.opf.ResourceCollection.Roles;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.AccessibilityVocab;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.DCMESVocab;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.MediaOverlaysVocab;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.PackageVocabs.LINKREL_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.RenditionVocabs;
import com.adobe.epubcheck.vocab.ScriptedCompVocab;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class OPFHandler30 extends OPFHandler
{

  private static final Map<String, Vocab> RESERVED_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put(DCTERMS_PREFIX, DCTERMS_VOCAB).put(MARC_PREFIX, MARC_VOCAB).put(ONIX_PREFIX, ONIX_VOCAB)
      .put(SCHEMA_PREFIX, SCHEMA_VOCAB).put(XSD_PREFIX, XSD_VOCAB).build();
  private static final Map<String, Vocab> RESERVED_META_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", AggregateVocab.of(META_VOCAB, META_VOCAB_CAMEL))
      .put(AccessibilityVocab.PREFIX, AccessibilityVocab.META_VOCAB)
      .put(MediaOverlaysVocab.PREFIX, MediaOverlaysVocab.VOCAB)
      .put(RenditionVocabs.PREFIX, RenditionVocabs.META_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_ITEM_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", ITEM_VOCAB).put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_ITEMREF_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", ITEMREF_VOCAB).put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, RenditionVocabs.ITEMREF_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_LINKREL_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", LINKREL_VOCAB).put(AccessibilityVocab.PREFIX, AccessibilityVocab.LINKREL_VOCAB)
      .put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB).putAll(RESERVED_VOCABS).build();
  private static final Map<String, Vocab> RESERVED_LINK_VOCABS = new ImmutableMap.Builder<String, Vocab>()
      .put("", LINK_VOCAB).put(AccessibilityVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(MediaOverlaysVocab.PREFIX, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.PREFIX, VocabUtil.EMPTY_VOCAB).putAll(RESERVED_VOCABS).build();

  private static final Map<String, Vocab> KNOWN_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .put(DCTERMS_URI, DCTERMS_VOCAB).put(MARC_URI, MARC_VOCAB).put(ONIX_URI, ONIX_VOCAB)
      .put(SCHEMA_URI, SCHEMA_VOCAB).put(XSD_URI, XSD_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_META_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(AccessibilityVocab.URI, AccessibilityVocab.META_VOCAB)
      .put(MediaOverlaysVocab.URI, MediaOverlaysVocab.VOCAB)
      .put(RenditionVocabs.URI, RenditionVocabs.META_VOCAB)
      .put(ScriptedCompVocab.URI, ScriptedCompVocab.VOCAB).build();
  private static final Map<String, Vocab> KNOWN_ITEM_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(AccessibilityVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, VocabUtil.EMPTY_VOCAB)
      .put(ScriptedCompVocab.URI, VocabUtil.EMPTY_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_ITEMREF_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(AccessibilityVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, RenditionVocabs.ITEMREF_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_LINK_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(AccessibilityVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, VocabUtil.EMPTY_VOCAB)
      .put(ScriptedCompVocab.URI, VocabUtil.EMPTY_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_LINKREL_VOCAB_URIS = new ImmutableMap.Builder<String, Vocab>()
      .putAll(KNOWN_VOCAB_URIS).put(AccessibilityVocab.URI, AccessibilityVocab.LINKREL_VOCAB)
      .put(MediaOverlaysVocab.URI, VocabUtil.EMPTY_VOCAB)
      .put(RenditionVocabs.URI, VocabUtil.EMPTY_VOCAB)
      .put(ScriptedCompVocab.URI, VocabUtil.EMPTY_VOCAB).build();

  private static final Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(ITEM_VOCAB_URI,
      ITEMREF_VOCAB_URI, META_VOCAB_URI, LINK_VOCAB_URI);

  private static final Splitter TOKENIZER = Splitter.onPattern("\\s+");

  private Map<String, Vocab> itemrefVocabs;
  private Map<String, Vocab> itemVocabs;
  private Map<String, Vocab> metaVocabs;
  private Map<String, Vocab> linkVocabs;
  private Map<String, Vocab> linkrelVocabs;
  private final Deque<MetadataSet.Builder> metadataBuilders = Lists.newLinkedList();
  private MetadataSet metadata = null;
  private final Deque<LinkedResources.Builder> linkedResourcesBuilders = Lists.newLinkedList();
  private LinkedResources linkedResources = null;
  private final Deque<ResourceCollection.Builder> collectionBuilders = Lists.newLinkedList();
  private final ResourceCollections.Builder collectionsBuilder = ResourceCollections.builder();
  private ResourceCollections collections = null;

  OPFHandler30(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void startElement()
  {
    super.startElement();

    XMLElement e = currentElement();
    String name = e.getName();

    // Check global attributes
    String xmllang = e.getAttributeNS(EpubConstants.XmlNamespaceUri, "lang");
    if (xmllang != null && !xmllang.isEmpty())
    {
      checkLanguageTag(xmllang);
    }

    if (EpubConstants.OpfNamespaceUri.equals(e.getNamespace()))
    {
      if (name.equals("package"))
      {
        // Note: the #parsePrefixDeclaration is called once for each "class" of
        // properties (meta+scheme, itemref, item, and link) so that default and
        // reserved vocabs can be set appropriately (e.g. the default vocab or
        // rendition vocab for 'meta' properties is not the same as for the
        // 'item'
        // properties)
        // Messages are reported only on the first invocation; a quiet reporter
        // is
        // used for subsequent invocations.
        String prefixDecl = e.getAttribute("prefix");
        EPUBLocation loc = location();
        metaVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_META_VOCABS,
            KNOWN_META_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, loc);
        itemVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_ITEM_VOCABS,
            KNOWN_ITEM_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
        itemrefVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_ITEMREF_VOCABS,
            KNOWN_ITEMREF_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
        linkrelVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_LINKREL_VOCABS,
            KNOWN_LINKREL_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
        linkVocabs = VocabUtil.parsePrefixDeclaration(prefixDecl, RESERVED_LINK_VOCABS,
            KNOWN_LINK_VOCAB_URIS, DEFAULT_VOCAB_URIS, QuietReport.INSTANCE, loc);
      }
      else if (name.equals("metadata"))
      {
        metadataBuilders.addFirst(MetadataSet.builder());
        linkedResourcesBuilders.addFirst(LinkedResources.builder());
      }
      else if (name.equals("link"))
      {
        processLink();
      }
      else if (name.equals("item"))
      {
        String id = e.getAttribute("id");
        OPFItem.Builder itemBuilder = itemBuilders.get(id);
        if (itemBuilder != null)
        {
          processItemProperties(itemBuilder, e.getAttribute("properties"),
              e.getAttribute("media-type"));
        }
      }
      else if (name.equals("itemref"))
      {
        String idref = e.getAttribute("idref");

        OPFItem.Builder itemBuilder = itemBuilders.get(idref);
        if (itemBuilder != null)
        {
          processItemrefProperties(itemBuilder, e.getAttribute("properties"));
        }
      }
      else if (name.equals("collection"))
      {
        collectionBuilders.addFirst(
            ResourceCollection.builder().roles(processCollectionRole(e.getAttribute("role"))));
        linkedResourcesBuilders.addFirst(LinkedResources.builder());
      }
    }
  }

  @Override
  public void endElement()
  {

    XMLElement e = currentElement();
    String name = e.getName();
    if (EpubConstants.OpfNamespaceUri.equals(e.getNamespace()))
    {
      if (name.equals("package"))
      {
        collections = collectionsBuilder.build();
        for (ResourceCollection collection : getCollections().getByRole(Roles.INDEX))
        {
          processItemsInIndexCollection(collection);
        }
      }
      else if (name.equals("meta"))
      {
        processMeta();
      }
      else if (name.equals("metadata"))
      {

        // else peek collection builder and add it

        // Build metadata declared in this metadata element
        MetadataSet metadata = null;
        try
        {
          if (!metadataBuilders.isEmpty()) metadata = metadataBuilders.removeFirst().build();
        } catch (IllegalStateException ex)
        {
          report.message(MessageId.OPF_065, location());
        }
        // Build linked resources declared in this metadata element
        LinkedResources linkedResources = (linkedResourcesBuilders.isEmpty()) ? null
            : linkedResourcesBuilders.removeFirst().build();

        // if we're not building a collection, assign to package-level objects
        if (collectionBuilders.isEmpty())
        {
          this.metadata = metadata;
          this.linkedResources = linkedResources;
          reportMetadata();
        }
        // else assign to the collection being built
        else
        {
          collectionBuilders.peekFirst().metadata(metadata).metadataLinks(linkedResources);
        }

      }
      else if (name.equals("collection"))
      {
        if (!collectionBuilders.isEmpty())
        {
          // Build linked resources declared in this collection
          if (!linkedResourcesBuilders.isEmpty())
          {
            collectionBuilders.peekFirst().resources(linkedResourcesBuilders.removeFirst().build());
          }
          // build this collection
          ResourceCollection collection = collectionBuilders.removeFirst().build();
          // if it's a top-level collection (no remaining parent collection)
          // assign to the set of Package collections
          if (collectionBuilders.isEmpty())
          {
            collectionsBuilder.add(collection);
          }
          // else add as a sub-collection of the collection being built
          else
          {
            collectionBuilders.peekFirst().collection(collection);
          }
        }
      }

    }
    else if (EpubConstants.DCElements.equals(e.getNamespace()))
    {
      processDCElem();
    }

    super.endElement();
  }

  /**
   * Returns the metadata for the Rendition represented by the current Package
   * Document. Must be called after the parsing.
   * 
   * @return the metadata for the Rendition represented by the current Package
   *           Document
   */
  public MetadataSet getMetadata()
  {
    return (metadata == null) ? new MetadataSet.Builder().build() : metadata;
  }

  /**
   * Returns the list of linked resources (i.e. resources referenced from
   * <code>link</code> elements) declared in the current Package Document at the
   * package level (i.e. in the package <code>metadata</code> element). Must be
   * called after the parsing.
   * 
   * @return the linked resources for the Rendition represented by the current
   *           Package Document
   */
  public LinkedResources getLinkedResources()
  {
    return (linkedResources == null) ? LinkedResources.builder().build() : linkedResources;
  }

  /**
   * Returns the list of collections (as defined in <code>collection</code>
   * elements) declared in the current Package Document. Must be called after
   * the parsing.
   * 
   * @return the linked resources for the Rendition represented by the current
   *           Package Document
   */
  public ResourceCollections getCollections()
  {
    return (collections == null) ? ResourceCollections.builder().build() : collections;
  }

  private List<String> processCollectionRole(String roleAtt)
  {
    ImmutableList.Builder<String> rolesBuilder = ImmutableList.builder();
    for (String role : TOKENIZER.split(Strings.nullToEmpty(roleAtt)))
    {
      if (URLUtils.isAbsoluteURLString(role))
      {
        // Role is an absolute IRI
        try
        {
          URL.parse(role);
        } catch (GalimatiasParseException e)
        {
          report.message(MessageId.OPF_070, location(), role);
          break;
        }
      }
      rolesBuilder.add(role);
    }
    return rolesBuilder.build();
  }

  private void processLink()
  {
    XMLElement e = currentElement();

    // check the 'href' URL
    // href presence is checked by schema
    String href = e.getAttribute("href");
    URL url = checkURL(href);
    if (url != null)
    {
      // Data URLs are not allowed on `link` elements
      if ("data".equals(url.scheme()))
      {
        report.message(MessageId.RSC_029, location());
        return;
      }
      // The `href` attribute MUST not reference resources via elements
      // in the package document itself
      if (url.fragment() != null && !url.fragment().isEmpty()
          && URLUtils.docURL(url).equals(context.url))
      {
        report.message(MessageId.OPF_098, location(), href);
        return;
      }

      if (context.isRemote(url))
      {
        report.info(path, FeatureEnum.REFERENCE, href);
      }
      registerReference(url, Reference.Type.LINK);

      // check the 'rel' attribute
      String rel = e.getAttribute("rel");
      Set<Property> relSet = processLinkRel(rel);
      Set<LINKREL_PROPERTIES> relEnum = Property.filter(relSet, LINKREL_PROPERTIES.class);

      // check the 'media-type' attribute
      String mediatype = e.getAttribute("media-type");
      if (mediatype == null)
      {
        // media-type is required for in-container URLs
        // NOTE: as legacy EPUB 3.2 collections made heavy use
        // of local links with no media type, we only check this
        // for metadata links, which may be a violation of EPUB.
        if (!context.isRemote(url) && !metadataBuilders.isEmpty())
        {
          if (linkedResourcesBuilders.size() == 1)
            report.message(MessageId.OPF_093, location());
        }
        // media-type is required by some keywords
        else if (relEnum.stream().anyMatch(
            keyword -> keyword == LINKREL_PROPERTIES.RECORD
                || keyword == LINKREL_PROPERTIES.VOICING))
        {
          report.message(MessageId.OPF_094, location(), rel);
        }
      }
      else
      {
        // 'voicing' links require an audio media type
        if (relEnum.contains(LINKREL_PROPERTIES.VOICING) && !OPFChecker30.isAudioType(mediatype))
        {
          report.message(MessageId.OPF_095, location(), mediatype);
        }
      }

      // check the 'properties' attribute
      processLinkProperties(e.getAttribute("properties"));

      // build the data model
      if (!linkedResourcesBuilders.isEmpty())
      {
        LinkedResource resource = new LinkedResource.Builder(url).id(e.getAttribute("id"))
            .rel(relSet).mimetype(mediatype).refines(e.getAttribute("refines")).build();
        linkedResourcesBuilders.peekFirst().add(resource);
      }
    }

    // check hreflang attribute
    String hreflang = e.getAttribute("hreflang");
    if (hreflang != null && !hreflang.isEmpty())
    {
      checkLanguageTag(hreflang);
    }
  }

  private void processItemrefProperties(OPFItem.Builder builder, String property)
  {
    Set<Property> properties = VocabUtil.parsePropertyList(property, itemrefVocabs, context,
        location());
    builder.properties(properties);
    if (properties.contains(
        RenditionVocabs.ITEMREF_VOCAB.get(RenditionVocabs.ITEMREF_PROPERTIES.LAYOUT_PRE_PAGINATED))
        || !properties.contains(
            RenditionVocabs.ITEMREF_VOCAB.get(RenditionVocabs.ITEMREF_PROPERTIES.LAYOUT_REFLOWABLE))
            && getMetadata().containsPrimary(
                RenditionVocabs.META_VOCAB.get(RenditionVocabs.META_PROPERTIES.LAYOUT),
                "pre-paginated"))
    {
      builder.fixedLayout();
    }

    // NOTE:
    // Checked with Schematron, although the code below is more prefix-safe

    // Set<ITEMREF_PROPERTIES> propSet = Property.filter(properties,
    // ITEMREF_PROPERTIES.class);
    // if (propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_LEFT)
    // && propSet.contains(ITEMREF_PROPERTIES.PAGE_SPREAD_RIGHT))
    // {
    // report.message(MessageId.OPF_011,
    // new EPUBLocation(path, parser.getLineNumber(),
    // parser.getColumnNumber()));
    // }
  }

  private void processItemProperties(OPFItem.Builder builder, String property, String mimeType)
  {
    if (property == null)
    {
      return;
    }

    Set<Property> properties = VocabUtil.parsePropertyList(property, itemVocabs, context,
        location());
    Set<ITEM_PROPERTIES> itemProps = Property.filter(properties, ITEM_PROPERTIES.class);

    mimeType = mimeType.trim();
    for (ITEM_PROPERTIES itemProp : itemProps)
    {
      if (!itemProp.isAllowedForType(mimeType))
      {
        report.message(MessageId.OPF_012, location(), ITEM_VOCAB.getName(itemProp), mimeType);
      }
    }
    builder.properties(properties);
  }

  private Set<Property> processLinkProperties(String properties)
  {
    return VocabUtil.parsePropertyList(properties, linkVocabs, context, location());
  }

  private Set<Property> processLinkRel(String rel)
  {
    Set<Property> linkRelProperties = VocabUtil.parsePropertyList(rel, linkrelVocabs, context,
        location());
    if (Property.filter(linkRelProperties, LINKREL_PROPERTIES.class)
        .contains(LINKREL_PROPERTIES.ALTERNATE) && linkRelProperties.size() > 1)
    {
      report.message(MessageId.OPF_089, location());
    }
    return linkRelProperties;
  }

  private void processMeta()
  {
    XMLElement e = currentElement();
    // get the property
    Optional<Property> prop = VocabUtil.parseProperty(e.getAttribute("property"), metaVocabs,
        context, location());

    if (prop.isPresent() && !metadataBuilders.isEmpty())
    {
      String value = Strings.nullToEmpty((String) e.getPrivateData(TEXT)).trim();
      metadataBuilders.peekFirst().meta(e.getAttribute("id"), prop.get(),
          value, e.getAttribute("refines"));

      // Primary metadata checks
      if (metadataBuilders.size() == 1)
      {
        switch (prop.get().getPrefixedName())
        {
        case "media:active-class":
          context.featureReport.report(FeatureEnum.MEDIA_OVERLAYS_ACTIVE_CLASS, location(),
              e.getPrivateData().toString());
          break;
        case "media:playback-active-class":
          context.featureReport.report(FeatureEnum.MEDIA_OVERLAYS_PLAYBACK_ACTIVE_CLASS, location(),
              e.getPrivateData().toString());
          break;
        case "rendition:spread":
          if (value.equals("portrait"))
          {
            report.message(MessageId.OPF_086, location(), "rendition:spread portrait",
                LocalizedMessages.getInstance(context.locale)
                    .getSuggestion(MessageId.OPF_086, null));
          }
          break;
        default:
          break;
        }
      }
    }

    // just parse the scheme for vocab errors
    VocabUtil.parseProperty(e.getAttribute("scheme"), metaVocabs, context, location());
  }

  private void processDCElem()
  {
    XMLElement e = currentElement();
    // get the property
    Optional<Property> prop = DCMESVocab.VOCAB.lookup(e.getName());
    // Add to the metadata model builder
    if (prop.isPresent() && !metadataBuilders.isEmpty())
    {
      metadataBuilders.peekFirst().meta(e.getAttribute("id"), prop.get(),
          (String) e.getPrivateData(TEXT), null);
    }
    // Check that dc:language is well-formed
    if ("language".equals(e.getName()))
    {
      String language = (String) e.getPrivateData(TEXT);
      // Empty dc:language is checked by the schema
      if (language != null && !language.trim().isEmpty())
      {
        checkLanguageTag(language.trim());
      }
    }
  }

  private void processItemsInIndexCollection(ResourceCollection collection)
  {
    if (collection.hasRole(Roles.INDEX) || collection.hasRole(Roles.INDEX_GROUP))
    {
      for (LinkedResource resource : collection.getResources().asList())
      {
        OPFItem.Builder itemBuilder = itemBuildersByURL.get(resource.getDocumentURL());
        if (itemBuilder != null)
        {
          itemBuilder.properties(ImmutableSet
              .of(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.IN_INDEX_COLLECTION)));
        }
      }
      for (ResourceCollection childCollection : collection.getCollections().asList())
      {
        processItemsInIndexCollection(childCollection);
      }
    }
  }

  private void checkLanguageTag(String language)
  {
    try
    {
      new Locale.Builder().setLanguageTag(language);
    } catch (IllformedLocaleException exception)
    {
      report.message(MessageId.OPF_092, location(), language, exception.getMessage());
    }
  }

  protected void reportMetadata()
  {
    // Report publication rendition layout
    if (getMetadata().containsPrimary(
        RenditionVocabs.META_VOCAB.get(RenditionVocabs.META_PROPERTIES.LAYOUT), "pre-paginated"))
    {
      report.info(null, FeatureEnum.RENDITION_LAYOUT, "pre-paginated");
      report.info(null, FeatureEnum.HAS_FIXED_LAYOUT, "true");
    }
    // Report publication rendition orientation (if set)
    Optional<Metadata> orientation = MetadataSet.tryFind(getMetadata().getAll(),
        RenditionVocabs.META_VOCAB.get(RenditionVocabs.META_PROPERTIES.ORIENTATION),
        Optional.absent());
    if (orientation.isPresent())
    {
      report.info(null, FeatureEnum.RENDITION_ORIENTATION, orientation.get().getValue());
    }
    // Report publication rendition spread (if set)
    Optional<Metadata> spread = MetadataSet.tryFind(getMetadata().getAll(),
        RenditionVocabs.META_VOCAB.get(RenditionVocabs.META_PROPERTIES.SPREAD),
        Optional.absent());
    if (spread.isPresent())
    {
      report.info(null, FeatureEnum.RENDITION_SPREAD, spread.get().getValue());
    }
  }

  @Override
  protected void reportItem(OPFItem item)
  {
    super.reportItem(item);

    // Report rendition properties overrides
    Set<RenditionVocabs.ITEMREF_PROPERTIES> properties = Property.filter(item.getProperties(),
        RenditionVocabs.ITEMREF_PROPERTIES.class);
    for (RenditionVocabs.ITEMREF_PROPERTIES property : properties)
    {
      switch (property)
      {
      // Rendition layout properties
      case LAYOUT_PRE_PAGINATED:
        report.info(item.getPath(), FeatureEnum.RENDITION_LAYOUT, "pre-paginated");
        report.info(item.getPath(), FeatureEnum.HAS_FIXED_LAYOUT, "true");
        break;
      case LAYOUT_REFLOWABLE:
        report.info(item.getPath(), FeatureEnum.RENDITION_LAYOUT, "reflowable");
        report.info(item.getPath(), FeatureEnum.HAS_FIXED_LAYOUT, "false");
        break;
      // Orientation properties
      case ORIENTATION_AUTO:
      case ORIENTATION_LANDSCAPE:
      case ORIENTATION_PORTRAIT:
        report.info(item.getPath(), FeatureEnum.RENDITION_ORIENTATION,
            property.name().substring(12).toLowerCase(Locale.ROOT));
        break;
      // Spread properties
      case SPREAD_AUTO:
      case SPREAD_BOTH:
      case SPREAD_LANDSCAPE:
      case SPREAD_NONE:
      case SPREAD_PORTRAIT:
        report.info(item.getPath(), FeatureEnum.RENDITION_SPREAD,
            property.name().substring(7).toLowerCase(Locale.ROOT));
        break;

      default:
        break;
      }
    }
  }
}

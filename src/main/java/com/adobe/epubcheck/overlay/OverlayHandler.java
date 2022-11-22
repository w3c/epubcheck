package com.adobe.epubcheck.overlay;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.epubcheck.core.references.Reference;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.StructureVocab;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import io.mola.galimatias.URL;

public class OverlayHandler extends XMLHandler
{

  private static Map<String, Vocab> RESERVED_VOCABS = ImmutableMap.<String, Vocab> of("",
      AggregateVocab.of(StructureVocab.VOCAB, StructureVocab.UNCHECKED_VOCAB));
  private static Map<String, Vocab> KNOWN_VOCAB_URIS = ImmutableMap.of();
  private static Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(StructureVocab.URI);

  private Map<String, Vocab> vocabs = RESERVED_VOCABS;

  private Set<String> resourceRefs = new HashSet<String>();

  private final Set<ITEM_PROPERTIES> requiredProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);

  public OverlayHandler(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void startElement()
  {
    XMLElement e = currentElement();
    String name = e.getName();

    processGlobalAttrs();

    switch (name)
    {
    case "smil":
      vocabs = VocabUtil.parsePrefixDeclaration(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"), RESERVED_VOCABS,
          KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, location());
      break;

    case "body":
    case "seq":
      processTextRef();
      break;

    case "text":
      processTextSrc();
      break;

    case "audio":
      processAudioSrc();
      checkTime(e.getAttribute("clipBegin"), e.getAttribute("clipEnd"));
      break;
    }
  }

  private void checkTime(String clipBegin, String clipEnd)
  {

    if (clipEnd == null)
    {
      // missing clipEnd attribute means clip plays to end so no comparisons
      // possible
      return;
    }

    if (clipBegin == null)
    {
      // set clipBegin to 0 if the attribute isn't set to allow comparisons
      clipBegin = "0";
    }

    SmilClock start;
    SmilClock end;

    try
    {
      start = new SmilClock(clipBegin);
      end = new SmilClock(clipEnd);
    } catch (Exception ex)
    {
      // invalid clock time will be reported by the schema
      return;
    }

    if (start.compareTo(end) == 1)
    {
      // clipEnd is chronologically before clipBegin
      report.message(MessageId.MED_008, location());
    }

    else if (start.equals(end))
    {
      // clipBegin and clipEnd are equal
      report.message(MessageId.MED_009, location());
    }
  }

  private void checkType(String type)
  {
    Set<Property> propList = VocabUtil.parsePropertyList(type, vocabs, context, location());

    // Check unrecognized properties from the structure vocab
    for (Property property : propList)
    {
      if (StructureVocab.URI.equals(property.getVocabURI())) try
      {
        property.toEnum();
      } catch (UnsupportedOperationException ex)
      {
        report.message(MessageId.OPF_088, location(), property.getName());
      }
    }
  }

  private void processTextSrc()
  {
    URL url = checkURL(currentElement().getAttribute("src"));
    processContentDocumentLink(url);
  }

  private void processTextRef()
  {
    URL url = checkURL(
        currentElement().getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "textref"));
    processContentDocumentLink(url);
  }

  private void processAudioSrc()
  {

    URL url = checkURL(currentElement().getAttribute("src"));

    // check that the URL has no fragment
    if (url.fragment() != null)
    {
      report.message(MessageId.MED_014, location(), url.fragment());
      url = URLUtils.docURL(url);
    }

    if (url != null && context.container.isPresent())
    {

      // check that the audio type is a core media type resource
      String mimeType = context.resourceRegistry.get().getMimeType(url);
      if (mimeType != null && !OPFChecker30.isBlessedAudioType(mimeType))
      {
        report.message(MessageId.MED_005, location(), context.relativize(url), mimeType);
      }

      // register the URL for cross-reference checking
      registerReference(url, Reference.Type.AUDIO, true);

      // if needed, register we found a remote resource
      if (context.isRemote(url))
      {
        requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
      }
    }
  }

  private void processContentDocumentLink(URL url)
  {
    if (url != null && context.container.isPresent())
    {
      assert context.overlayTextChecker.isPresent();
      URL documentURL = URLUtils.docURL(url);
      if (!context.overlayTextChecker.get().registerOverlay(documentURL,
          context.opfItem.get().getId()))
      {
        report.message(MessageId.MED_011, location(), context.relativize(url));
      }
      registerReference(url, Reference.Type.OVERLAY_TEXT_LINK);
    }
  }

  private void processGlobalAttrs()
  {
    XMLElement e = currentElement();
    checkType(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));
  }

  @Override
  public void endElement()
  {
    XMLElement e = currentElement();
    String name = e.getName();
    if (name.equals("smil"))
    {
      checkItemReferences();
      checkProperties();
    }
  }

  private void checkItemReferences()
  {

    if (this.resourceRefs.isEmpty())
    {
      return;
    }

  }

  protected void checkProperties()
  {
    if (!context.container.isPresent()) // single file validation
    {
      return;
    }

    Set<ITEM_PROPERTIES> itemProps = Property.filter(context.properties, ITEM_PROPERTIES.class);

    for (ITEM_PROPERTIES requiredProperty : Sets.difference(requiredProperties, itemProps))
    {
      report.message(MessageId.OPF_014, EPUBLocation.of(context),
          PackageVocabs.ITEM_VOCAB.getName(requiredProperty));
    }
  }
}

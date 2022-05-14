package com.adobe.epubcheck.overlay;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.StructureVocab;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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

    switch (name)
    {
    case "smil":
      vocabs = VocabUtil.parsePrefixDeclaration(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"), RESERVED_VOCABS,
          KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, location());
      break;

    case "body":
    case "seq":
    case "par":
      processGlobalAttrs();
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
    String src = currentElement().getAttribute("src");

    processRef(src, XRefChecker.Type.HYPERLINK);

    String resolvedSrc = PathUtil.resolveRelativeReference(path, src);

    if (context.xrefChecker.isPresent())
    {
      context.xrefChecker.get().registerReference(path, location().getLine(),
          location().getColumn(), resolvedSrc, XRefChecker.Type.OVERLAY_TEXT_LINK);
    }
  }

  private void processAudioSrc()
  {

    String src = currentElement().getAttribute("src");

    processRef(src, XRefChecker.Type.AUDIO);

    if (src != null && PathUtil.isRemote(src))
    {
      requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
    }

  }

  private void processRef(String ref, XRefChecker.Type type)
  {
    if (ref != null && context.xrefChecker.isPresent())
    {
      ref = PathUtil.resolveRelativeReference(path, ref);
      if (type == XRefChecker.Type.AUDIO)
      {
        String mimeType = context.xrefChecker.get().getMimeType(ref);
        if (mimeType != null && !OPFChecker30.isBlessedAudioType(mimeType))
        {
          report.message(MessageId.MED_005, location(), ref, mimeType);
        }
      }
      else
      {
        checkFragment(ref);
        String uniqueResource = PathUtil.removeFragment(ref);
        if (!Strings.isNullOrEmpty(uniqueResource))
        {
          if (!context.overlayTextChecker.get().add(uniqueResource, context.opfItem.get().getId()))
          {
            report.message(MessageId.MED_011, location(), ref);
          }
        }
      }
      context.xrefChecker.get().registerReference(path, location().getLine(),
          location().getColumn(), ref, type);
    }
  }

  private void processGlobalAttrs()
  {
    XMLElement e = currentElement();
    if (!e.getName().equals("audio"))
    {
      processRef(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "textref"),
          XRefChecker.Type.HYPERLINK);
    }
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

  private void checkFragment(String ref)
  {

    String frag = PathUtil.getFragment(ref.trim());

    if (ref.indexOf("#") == -1 || Strings.isNullOrEmpty(frag))
    {
      // must include a non-empty fragid
      report.message(MessageId.MED_014, location());
    }
  }

  protected void checkProperties()
  {
    if (!context.ocf.isPresent()) // single file validation
    {
      return;
    }

    Set<ITEM_PROPERTIES> itemProps = Property.filter(context.properties, ITEM_PROPERTIES.class);

    for (ITEM_PROPERTIES requiredProperty : Sets.difference(requiredProperties, itemProps))
    {
      report.message(MessageId.OPF_014, EPUBLocation.create(path),
          PackageVocabs.ITEM_VOCAB.getName(requiredProperty));
    }
  }
}

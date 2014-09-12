package com.adobe.epubcheck.ops;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.AltStylesheetVocab;
import com.adobe.epubcheck.vocab.EnumVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.StagingEdupubVocab;
import com.adobe.epubcheck.vocab.StructureVocab;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.XMLAttribute;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class OPSHandler30 extends OPSHandler
{

  private static Map<String, Vocab> ITEM_VOCABS = ImmutableMap.of("", PackageVocabs.ITEM_VOCAB);
  private static Map<String, Vocab> RESERVED_VOCABS = ImmutableMap.of("", StructureVocab.VOCAB);
  private static Map<String, Vocab> RESERVED_EDUPUB_VOCABS = ImmutableMap.of("",
      AggregateVocab.of(StructureVocab.VOCAB, StagingEdupubVocab.VOCAB));
  private static Map<String, Vocab> ALTCSS_VOCABS = ImmutableMap.of("", AltStylesheetVocab.VOCAB);
  private static Map<String, Vocab> KNOWN_VOCAB_URIS = ImmutableMap.of();
  private static Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(StructureVocab.URI);

  String properties;

  private Map<String, Vocab> vocabs = RESERVED_VOCABS;

  final Set<ITEM_PROPERTIES> propertiesSet = EnumSet.noneOf(ITEM_PROPERTIES.class);

  final String mimeType;

  boolean video = false;

  boolean audio = false;

  boolean hasValidFallback = false;

  int imbricatedObjects = 0;
  int imbricatedCanvases = 0;

  boolean anchorNeedsText = false;
  boolean inMathML = false;
  boolean inSvg = false;
  boolean hasAltorAnnotation = false;
  private final Set<String> pubTypes;

  static final String[] scriptEventsStrings = { "onafterprint", "onbeforeprint", "onbeforeunload",
      "onerror", "onhaschange", "onload", "onmessage", "onoffline", "onpagehide", "onpageshow",
      "onpopstate", "onredo", "onresize", "onstorage", "onundo", "onunload",

      "onblur", "onchange", "oncontextmenu", "onfocus", "onformchange", "onforminput", "oninput",
      "oninvalid", "onreset", "onselect", "onsubmit",

      "onkeydown", "onkeypress", "onkeyup",

      "onabort", "oncanplay", "oncanplaythrough", "ondurationchange", "onemptied", "onended",
      "onerror", "onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay",
      "onplaying", "onprogress", "onratechange", "onreadystatechange", "onseeked", "onseeking",
      "onstalled", "onsuspend", "ontimeupdate", "onvolumechange", "onwaiting" };

  static HashSet<String> scriptEvents;

  public static HashSet<String> getScriptEvents()
  {
    if (scriptEvents == null)
    {
      scriptEvents = new HashSet<String>();
      Collections.addAll(scriptEvents, scriptEventsStrings);
      Collections.addAll(scriptEvents, mouseEventsStrings);
    }
    return scriptEvents;
  }

  static final String[] mouseEventsStrings = { "onclick", "ondblclick", "ondrag", "ondragend",
      "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop", "onmousedown",
      "onmousemove", "onmouseout", "onmouseover", "onmouseup", "onmousewheel", "onscroll" };
  static HashSet<String> mouseEvents;

  public static HashSet<String> getMouseEvents()
  {
    if (mouseEvents == null)
    {
      mouseEvents = new HashSet<String>();
      Collections.addAll(mouseEvents, mouseEventsStrings);
    }
    return mouseEvents;
  }

  public OPSHandler30(OCFPackage ocf, String path, String mimeType, String properties,
      XRefChecker xrefChecker, XMLParser parser, Report report, EPUBVersion version,
      Set<String> pubTypes)
  {
    super(ocf, path, xrefChecker, parser, report, version);
    this.mimeType = mimeType;
    this.properties = properties;
    checkedUnsupportedXMLVersion = false;
    this.pubTypes = pubTypes;
  }

  void checkType(String type)
  {
    if (type == null)
    {
      return;
    }

    VocabUtil.parsePropertyList(type, vocabs, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));

  }

  void checkSSMLPh(String ph)
  {
    // issue 139; enhancement is to add real syntax check for IPA and x-SAMPA
    if (ph == null)
    {
      return;
    }
    if (ph.trim().length() < 1)
    {
      report.message(MessageId.HTM_007,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
  }

  @Override
  public void characters(char[] chars, int arg1, int arg2)
  {
    super.characters(chars, arg1, arg2);
    String str = new String(chars, arg1, arg2);
    str = str.trim();
    if (!str.equals("") && (audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0))
    {
      hasValidFallback = true;
    }
    if (anchorNeedsText)
    {
      anchorNeedsText = false;
    }
  }

  public void startElement()
  {
    super.startElement();

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("html"))
    {
      Map<String, Vocab> reserved = (this.pubTypes.contains(OPFData.DC_TYPE_EDUPUB)) ? RESERVED_EDUPUB_VOCABS
          : RESERVED_VOCABS;
      vocabs = VocabUtil.parsePrefixDeclaration(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"), reserved,
          KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
    else if (name.equals("link"))
    {
      processLink(e);
    }
    else if (name.equals("object"))
    {
      processObject(e);
    }
    else if (name.equals("math"))
    {
      propertiesSet.add(ITEM_PROPERTIES.MATHML);
      inMathML = true;
      hasAltorAnnotation = (null != e.getAttribute("alttext"));
    }
    else if (!mimeType.equals("image/svg+xml") && name.equals("svg"))
    {
      propertiesSet.add(ITEM_PROPERTIES.SVG);
      processStartSvg(e);
    }
    else if (name.equals("script"))
    {
      propertiesSet.add(ITEM_PROPERTIES.SCRIPTED);
    }
    else if (!mimeType.equals("image/svg+xml") && name.equals("switch"))
    {
      propertiesSet.add(ITEM_PROPERTIES.SWITCH);
    }
    else if (name.equals("audio"))
    {
      processAudio();
    }
    else if (name.equals("video"))
    {
      processVideo(e);
    }
    else if (name.equals("canvas"))
    {
      processCanvas();
    }
    else if (name.equals("img"))
    {
      processImg();
    }
    else if (name.equals("a"))
    {
      anchorNeedsText = true;
      processAnchor(e);
    }
    else if (name.equals("annotation-xml"))
    {
      hasAltorAnnotation = true;
    }

    processInlineScripts(e);

    processSrc(("source".equals(name)) ? e.getParent().getName() : name, e.getAttribute("src"));

    checkType(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));

    checkSSMLPh(e.getAttributeNS("http://www.w3.org/2001/10/synthesis", "ph"));
  }

  void processInlineScripts(com.adobe.epubcheck.xml.XMLElement e)
  {
    HashSet<String> scriptEvents = getScriptEvents();
    HashSet<String> mouseEvents = getMouseEvents();

    for (int i = 0; i < e.getAttributeCount(); ++i)
    {
      XMLAttribute attr = e.getAttribute(i);
      String name = attr.getName().toLowerCase();
      if (scriptEvents.contains(name) || mouseEvents.contains(name))
      {
        propertiesSet.add(ITEM_PROPERTIES.SCRIPTED);
        return;
      }
    }
  }

  void processLink(XMLElement e)
  {

    String classAttribute = e.getAttribute("class");
    if (classAttribute == null)
    {
      return;
    }

    Set<Property> properties = VocabUtil.parsePropertyList(classAttribute, ALTCSS_VOCABS, report,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    Set<AltStylesheetVocab.PROPERTIES> altClasses = Property.filter(properties,
        AltStylesheetVocab.PROPERTIES.class);

    if (properties.size() == 1)
    {
      return;
    }

    boolean vertical = altClasses.contains(AltStylesheetVocab.PROPERTIES.VERTICAL);
    boolean horizontal = altClasses.contains(AltStylesheetVocab.PROPERTIES.HORIZONTAL);
    boolean day = altClasses.contains(AltStylesheetVocab.PROPERTIES.DAY);
    boolean night = altClasses.contains(AltStylesheetVocab.PROPERTIES.NIGHT);

    if (vertical && horizontal || day && night)
    {
      report.message(MessageId.CSS_005,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
          classAttribute);
    }
  }

  void processAnchor(XMLElement e)
  {
    if (e.getAttribute("href") == null)
    {
      anchorNeedsText = false;
    }
    if (inSvg)
    {
      String titleAttribute = e.getAttributeNS(EpubConstants.XLinkNamespaceUri, "title");
      if (titleAttribute == null)
      {
        report
            .message(
                MessageId.ACC_011,
                new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), e
                    .getName()));
      }
    }
  }

  void processImg()
  {
    if ((audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0))
    {
      hasValidFallback = true;
    }
  }

  void processCanvas()
  {
    imbricatedCanvases++;
  }

  void processAudio()
  {
    audio = true;
  }

  void processVideo(XMLElement e)
  {
    video = true;

    String posterSrc = e.getAttribute("poster");

    String posterMimeType = null;
    if (xrefChecker != null && posterSrc != null)
    {
      posterMimeType = xrefChecker.getMimeType(PathUtil.resolveRelativeReference(path, posterSrc,
          base));
    }

    if (posterMimeType != null && !OPFChecker.isBlessedImageType(posterMimeType))
    {
      report.message(MessageId.MED_001,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }

    if (posterSrc != null)
    {
      hasValidFallback = true;
      processSrc(e.getName(), posterSrc);
    }

  }

  void processSrc(String name, String src)
  {

    if (src != null)
    {
      src = src.trim();
      if (src.equals(""))
      {
        report.message(MessageId.HTM_008,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), name));
      }
    }

    if (src == null || xrefChecker == null)
    {
      return;
    }

    if (src.matches("^[^:/?#]+://.*"))
    {
      propertiesSet.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
    }
    else
    {
      src = PathUtil.resolveRelativeReference(path, src, base);
    }

    int refType;
    if ("audio".equals(name))
    {
      refType = XRefChecker.RT_AUDIO;
    }
    else if ("video".equals(name))
    {
      refType = XRefChecker.RT_VIDEO;
    }
    else
    {
      refType = XRefChecker.RT_GENERIC;
    }
    xrefChecker.registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), src,
        refType);

    String srcMimeType = xrefChecker.getMimeType(src);

    if (srcMimeType == null)
    {
      return;
    }

    if (!mimeType.equals("image/svg+xml") && srcMimeType.equals("image/svg+xml"))
    {
      propertiesSet.add(ITEM_PROPERTIES.SVG);
    }

    if ((audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0)
        && OPFChecker30.isCoreMediaType(srcMimeType) && !name.equals("track"))
    {
      hasValidFallback = true;
    }

  }

  void processObject(XMLElement e)
  {
    imbricatedObjects++;

    String type = e.getAttribute("type");
    String data = e.getAttribute("data");

    if (data != null)
    {
      processSrc(e.getName(), data);
      data = PathUtil.resolveRelativeReference(path, data, base);
    }

    if (type != null && data != null && xrefChecker != null
        && !type.equals(xrefChecker.getMimeType(data)))
    {
      String context = "<object";
      for (int i = 0; i < e.getAttributeCount(); i++)
      {
        XMLAttribute attribute = e.getAttribute(i);
        context += " " + attribute.getName() + "=\"" + attribute.getValue() + "\"";
      }
      context += ">";
      report.message(MessageId.OPF_013,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), context),
          type, xrefChecker.getMimeType(data));
    }

    if (type != null)
    {
      if (!mimeType.equals("image/svg+xml") && type.equals("image/svg+xml"))
      {
        propertiesSet.add(ITEM_PROPERTIES.SVG);
      }

      if (OPFChecker30.isCoreMediaType(type))
      {
        hasValidFallback = true;
      }
    }

    if (hasValidFallback)
    {
      return;
    }
    // check bindings
    if (xrefChecker != null && type != null && xrefChecker.getBindingHandlerSrc(type) != null)
    {
      hasValidFallback = true;
    }
  }

  void processStartSvg(XMLElement e)
  {
    inSvg = true;
    boolean foundXmlLang = false;
    boolean foundLang = false;
    for (int i = 0; i < e.getAttributeCount() && !foundLang && !foundXmlLang; ++i)
    {
      XMLAttribute a = e.getAttribute(i);
      if ("lang".compareTo(a.getName()) == 0)
      {
        foundXmlLang = foundXmlLang
            | (EpubConstants.XmlNamespaceUri.compareTo(a.getNamespace()) == 0);
        foundLang = (EpubConstants.HtmlNamespaceUri.compareTo(a.getNamespace()) == 0);
      }
    }
    if (!foundLang || !foundXmlLang)
    {
      report.message(MessageId.HTM_043,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), e.getName()));
    }
  }

  @Override
  public void endElement()
  {
    super.endElement();
    XMLElement e = parser.getCurrentElement();
    String name = e.getName();
    if (openElements == 0 && (name.equals("html") || name.equals("svg")))
    {
      checkProperties();
    }
    else if (name.equals("object"))
    {
      imbricatedObjects--;
      if (imbricatedObjects == 0 && imbricatedCanvases == 0)
      {
        checkFallback("Object");
      }
    }
    else if (name.equals("canvas"))
    {
      imbricatedCanvases--;
      if (imbricatedObjects == 0 && imbricatedCanvases == 0)
      {
        checkFallback("Canvas");
      }
    }
    else if (name.equals("video"))
    {
      if (imbricatedObjects == 0 && imbricatedCanvases == 0)
      {
        checkFallback("Video");
      }
      video = false;
    }
    else if (name.equals("audio"))
    {
      if (imbricatedObjects == 0 && imbricatedCanvases == 0)
      {
        checkFallback("Audio");
      }
      audio = false;
    }
    else if (name.equals("a"))
    {
      if (anchorNeedsText)
      {
        report.message(MessageId.ACC_004,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), "a"));
        anchorNeedsText = false;
      }
    }
    else if (name.equals("math"))
    {
      inMathML = false;
      if (!hasAltorAnnotation)
      {
        report.message(MessageId.ACC_009,
            new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), "math"));
      }
    }
  }

  /*
   * Checks fallbacks for video, audio and object elements
   */
  void checkFallback(String elementType)
  {
    if (hasValidFallback)
    {
      hasValidFallback = false;
    }
    else
    {
      report.message(MessageId.MED_002,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()), elementType);
    }
  }

  void checkProperties()
  {
    properties = Strings.nullToEmpty(properties);

    if (ImmutableSet.copyOf(properties.split("\\s+")).contains("singleFileValidation"))
    {
      return;
    }
    // TODO shouldn't have to reparse the properties here.
    // this.properties should be a Set<Property>
    Set<ITEM_PROPERTIES> itemProps = Sets.newEnumSet(Property.filter(VocabUtil.parsePropertyList(
        properties, ITEM_VOCABS, QuietReport.INSTANCE,
        new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber())),
        ITEM_PROPERTIES.class), ITEM_PROPERTIES.class);

    itemProps.remove(ITEM_PROPERTIES.NAV);
    itemProps.remove(ITEM_PROPERTIES.COVER_IMAGE);

    for (ITEM_PROPERTIES propSet : propertiesSet)
    {
      if (itemProps.contains(propSet))
      {
        itemProps.remove(propSet);
      }
      else
      {
        report.message(MessageId.OPF_014, new MessageLocation(path, 0, 0),
            EnumVocab.ENUM_TO_NAME.apply(propSet));
      }
    }

    if (itemProps.contains(ITEM_PROPERTIES.REMOTE_RESOURCES))
    {
      itemProps.remove(ITEM_PROPERTIES.REMOTE_RESOURCES);
      report.message(MessageId.OPF_018,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }

    if (!itemProps.isEmpty())
    {
      report.message(MessageId.OPF_015, new MessageLocation(path, 0, 0),
          Joiner.on(", ").join(Collections2.transform(itemProps, EnumVocab.ENUM_TO_NAME)));
    }
  }
}

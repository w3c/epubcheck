package com.adobe.epubcheck.ops;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SourceSet;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.AltStylesheetVocab;
import com.adobe.epubcheck.vocab.ComicsVocab;
import com.adobe.epubcheck.vocab.DataNavVocab;
import com.adobe.epubcheck.vocab.DictVocab;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.ForeignVocabs;
import com.adobe.epubcheck.vocab.IndexVocab;
import com.adobe.epubcheck.vocab.MagazineNavigationVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.StagingEdupubVocab;
import com.adobe.epubcheck.vocab.StructureVocab;
import com.adobe.epubcheck.vocab.StructureVocab.EPUB_TYPES;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.XMLAttribute;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class OPSHandler30 extends OPSHandler
{

  private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:([^;]*)[^,]*,.*");

  private static Map<String, Vocab> RESERVED_VOCABS = ImmutableMap.<String, Vocab> of("",
      AggregateVocab.of(StructureVocab.VOCAB, StagingEdupubVocab.VOCAB, DataNavVocab.VOCAB,
          DictVocab.VOCAB, IndexVocab.VOCAB, ComicsVocab.VOCAB, StructureVocab.UNCHECKED_VOCAB),
      MagazineNavigationVocab.PREFIX, MagazineNavigationVocab.VOCAB, ForeignVocabs.PRISM_PREFIX,
      ForeignVocabs.PRISM_VOCAB);
  private static Map<String, Vocab> ALTCSS_VOCABS = ImmutableMap.<String, Vocab> of("",
      AltStylesheetVocab.VOCAB);
  private static Map<String, Vocab> KNOWN_VOCAB_URIS = ImmutableMap.of(MagazineNavigationVocab.URI,
      MagazineNavigationVocab.VOCAB, ForeignVocabs.PRISM_URI, ForeignVocabs.PRISM_VOCAB);
  private static Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(StructureVocab.URI);

  private Map<String, Vocab> vocabs = RESERVED_VOCABS;

  private final Set<ITEM_PROPERTIES> requiredProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);
  private final Set<ITEM_PROPERTIES> allowedProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);

  private final boolean isLinear;

  protected boolean inVideo = false;
  protected boolean inAudio = false;
  protected boolean inPicture = false;
  protected boolean hasValidFallback = false;

  protected int imbricatedObjects = 0;
  protected int imbricatedCanvases = 0;

  protected boolean anchorNeedsText = false;
  protected boolean inMathML = false;
  protected boolean inSvg = false;
  protected boolean inBody = false;
  protected boolean inRegionBasedNav = false;
  protected boolean isOutermostSVGAlreadyProcessed = false;
  protected boolean hasAltorAnnotation = false;
  protected boolean hasTitle = false;

  static protected final String[] scriptEventsStrings = { "onafterprint", "onbeforeprint",
      "onbeforeunload", "onerror", "onhaschange", "onload", "onmessage", "onoffline", "onpagehide",
      "onpageshow", "onpopstate", "onredo", "onresize", "onstorage", "onundo", "onunload",

      "onblur", "onchange", "oncontextmenu", "onfocus", "onformchange", "onforminput", "oninput",
      "oninvalid", "onreset", "onselect", "onsubmit",

      "onkeydown", "onkeypress", "onkeyup",

      "onabort", "oncanplay", "oncanplaythrough", "ondurationchange", "onemptied", "onended",
      "onerror", "onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay",
      "onplaying", "onprogress", "onratechange", "onreadystatechange", "onseeked", "onseeking",
      "onstalled", "onsuspend", "ontimeupdate", "onvolumechange", "onwaiting" };

  static protected HashSet<String> scriptEvents;

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

  static protected final String[] mouseEventsStrings = { "onclick", "ondblclick", "ondrag",
      "ondragend", "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop",
      "onmousedown", "onmousemove", "onmouseout", "onmouseover", "onmouseup", "onmousewheel",
      "onscroll" };
  static protected HashSet<String> mouseEvents;

  public static HashSet<String> getMouseEvents()
  {
    if (mouseEvents == null)
    {
      mouseEvents = new HashSet<String>();
      Collections.addAll(mouseEvents, mouseEventsStrings);
    }
    return mouseEvents;
  }

  public OPSHandler30(ValidationContext context, XMLParser parser)
  {
    super(context, parser);
    checkedUnsupportedXMLVersion = false;
    isLinear = !context.properties
        .contains(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR));
  }

  protected void checkImage(XMLElement e, String attrNS, String attr)
  {
    // if it's an SVG image, fall back to super's logic
    String ns = e.getNamespace();
    if ("http://www.w3.org/2000/svg".equals(ns))
    {
      super.checkImage(e, attrNS, attr);
    }
    // else process image source sets in HTML
    else if (xrefChecker.isPresent())
    {
      String src = e.getAttribute("src");
      String srcset = e.getAttribute("srcset");
      // if we're in a 'picture' element
      if (inPicture)
      {
        String type = e.getAttribute("type");
        // if in a 'source' element specifying a foreign MIME type,
        // register as foreign picture source
        if ("source".equals(e.getName()) && type != null && !OPFChecker.isBlessedImageType(type, EPUBVersion.VERSION_3))
        {
          registerImageSources(src, srcset, XRefChecker.Type.PICTURE_SOURCE_FOREIGN);
        }
        // else register as regular picture source (must be a CMT)
        else
        // register as picture source
        {
          registerImageSources(src, srcset, XRefChecker.Type.PICTURE_SOURCE);
        }
      }
      // register as regular image sources (must be a CMT or have a manifest fallback
      else
      {
        registerImageSources(src, srcset, XRefChecker.Type.IMAGE);
      }
    }
  }

  protected void registerImageSources(String src, String srcset, XRefChecker.Type type)
  {
    // compute a list of URLs to register
    Set<String> urls = new TreeSet<>();
    if (src != null) urls.add(src);
    urls.addAll(SourceSet.parse(srcset).getImageURLs());
    // register all the URLs
    for (String url : urls)
    {
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          PathUtil.resolveRelativeReference(base, url), type);
    }

  }

  protected void checkType(XMLElement e, String type)
  {
    if (type == null)
    {
      return;
    }
    Set<Property> propList = VocabUtil.parsePropertyList(type, vocabs, context,
        EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
    checkTypes(Property.filter(propList, StructureVocab.EPUB_TYPES.class));
    
    // Check unrecognized properties from the structure vocab  
    for (Property property : propList)
    {
      if (StructureVocab.URI.equals(property.getVocabURI())) try
      {
        property.toEnum();
      } catch (UnsupportedOperationException ex)
      {
        report.message(MessageId.OPF_088, parser.getLocation(), property.getName());
      }
    }

    // Check the 'region-based' property (Data Navigation Documents)
    if (propList.contains(DataNavVocab.VOCAB.get(DataNavVocab.EPUB_TYPES.REGION_BASED)))

    {
      if (!"nav".equals(e.getName()) || !context.properties
          .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
      {
        report.message(MessageId.HTM_052, parser.getLocation());
      }
      else
      {
        inRegionBasedNav = true;
      }
    }
    // Store whether the doc containt DICT content
    if (propList.contains(DictVocab.VOCAB.get(DictVocab.EPUB_TYPES.DICTIONARY)))
    {
      context.featureReport.report(FeatureEnum.DICTIONARY, parser.getLocation(), null);
    }
  }

  protected void checkTypes(Set<EPUB_TYPES> types)
  {
    if (types.contains(EPUB_TYPES.PAGEBREAK))
    {
      context.featureReport.report(FeatureEnum.PAGE_BREAK, parser.getLocation(), null);
    }
    if (types.contains(EPUB_TYPES.INDEX))
    {
      allowedProperties.add(ITEM_PROPERTIES.INDEX);
      context.featureReport.report(FeatureEnum.INDEX, parser.getLocation(), null);
    }
    if (types.contains(EPUB_TYPES.GLOSSARY))
    {
      allowedProperties.add(ITEM_PROPERTIES.GLOSSARY);
    }
  }
  
  @Override
  protected void checkSVGFontFaceURI(XMLElement e, String attrNS, String attr)
  {
    super.checkSVGFontFaceURI(e, attrNS, attr);
    String href = e.getAttributeNS(attrNS, attr);
    if (href != null && PathUtil.isRemote(href))
    {
      requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
    }
  }


  protected void checkSSMLPh(String ph)
  {
    // issue 139; enhancement is to add real syntax check for IPA and x-SAMPA
    if (ph == null)
    {
      return;
    }
    if (ph.trim().length() < 1)
    {
      report.message(MessageId.HTM_007,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
  }

  @Override
  public void characters(char[] chars, int arg1, int arg2)
  {
    super.characters(chars, arg1, arg2);
    String str = new String(chars, arg1, arg2);
    str = str.trim();
    if (!str.equals("") && (inAudio || inVideo || imbricatedObjects > 0 || imbricatedCanvases > 0))
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

    checkDiscouragedElements(e);
    processSemantics(e);
    processSectioning(e);

    String name = e.getName();
    if (name.equals("html"))
    {
      vocabs = VocabUtil.parsePrefixDeclaration(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"), RESERVED_VOCABS,
          KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
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
      requiredProperties.add(ITEM_PROPERTIES.MATHML);
      inMathML = true;
      hasAltorAnnotation = (null != e.getAttribute("alttext"));
    }
    else if (name.equals("svg"))
    {
      processSVG(e);
    }
    else if (EpubConstants.EpubTypeNamespaceUri.equals(e.getNamespace()) && name.equals("switch"))
    {
      requiredProperties.add(ITEM_PROPERTIES.SWITCH);
    }
    else if (name.equals("audio"))
    {
      processAudio();
    }
    else if (name.equals("video"))
    {
      processVideo(e);
    }
    else if (name.equals("figure"))
    {
      processFigure(e);
    }
    else if (name.equals("table"))
    {
      processTable(e);
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
    else if (name.equals("picture"))
    {
      inPicture = true;
    }
    else if (name.equals("source"))
    {
      if (inPicture) checkImage(e, null, null);
    }
    else if ("http://www.w3.org/2000/svg".equals(e.getNamespace()) && name.equals("title"))
    {
      hasTitle = true;
    }

    processInlineScripts(e);

    processSrc(("source".equals(name)) ? e.getParent().getName() : name, e.getAttribute("src"));

    checkType(e, e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));

    checkSSMLPh(e.getAttributeNS("http://www.w3.org/2001/10/synthesis", "ph"));
  }
  
  protected void checkDiscouragedElements(XMLElement elem)
  {
    if (EpubConstants.HtmlNamespaceUri.equals(elem.getNamespace()))
    {
      switch (elem.getName())
      {
      case "base":
      case "embed":
      case "rp":
        report.message(MessageId.HTM_055,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()),
            elem.getName());
      }

    }
  }

  protected void processInlineScripts(com.adobe.epubcheck.xml.XMLElement e)
  {
    HashSet<String> scriptEvents = getScriptEvents();
    HashSet<String> mouseEvents = getMouseEvents();

    for (int i = 0; i < e.getAttributeCount(); ++i)
    {
      XMLAttribute attr = e.getAttribute(i);
      String name = attr.getName().toLowerCase(Locale.ROOT);
      if (scriptEvents.contains(name) || mouseEvents.contains(name))
      {
        processJavascript();
        return;
      }
    }
  }
  
  @Override
  protected void processJavascript()
  {
    super.processJavascript();
    requiredProperties.add(ITEM_PROPERTIES.SCRIPTED);
  }

  protected void processLink(XMLElement e)
  {
    String classAttribute = e.getAttribute("class");
    if (classAttribute == null)
    {
      return;
    }

    Set<Property> properties = VocabUtil.parsePropertyList(classAttribute, ALTCSS_VOCABS, context,
        EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
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
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()),
          classAttribute);
    }
  }

  protected void processAnchor(XMLElement e)
  {
    if (e.getAttribute("href") == null)
    {
      anchorNeedsText = false;
    }
    if (inSvg || context.mimeType.equals("image/svg+xml"))
    {
      hasTitle = Strings
          .emptyToNull(e.getAttributeNS(EpubConstants.XLinkNamespaceUri, "title")) != null;
    }
  }

  protected void processImg()
  {
    if ((inAudio || inVideo || imbricatedObjects > 0 || imbricatedCanvases > 0))
    {
      hasValidFallback = true;
    }
  }

  protected void processCanvas()
  {
    imbricatedCanvases++;
  }

  protected void processAudio()
  {
    inAudio = true;
    context.featureReport.report(FeatureEnum.AUDIO, parser.getLocation());
  }

  protected void processVideo(XMLElement e)
  {
    inVideo = true;
    context.featureReport.report(FeatureEnum.VIDEO, parser.getLocation());

    String posterSrc = e.getAttribute("poster");

    String posterMimeType = null;
    if (xrefChecker.isPresent() && posterSrc != null)
    {
      posterMimeType = xrefChecker.get().getMimeType(PathUtil.resolveRelativeReference(base,
          posterSrc));
    }

    if (posterMimeType != null && !OPFChecker.isBlessedImageType(posterMimeType, EPUBVersion.VERSION_3))
    {
      report.message(MessageId.MED_001,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
    }

    if (posterSrc != null)
    {
      hasValidFallback = true;
      processSrc(e.getName(), posterSrc);
    }

  }

  protected void processHyperlink(String href)
  {
    super.processHyperlink(href);
    if (inRegionBasedNav && xrefChecker.isPresent())
    {
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.REGION_BASED_NAV);
    }
  }

  protected void processSrc(String name, String src)
  {

    if (src != null)
    {
      src = src.trim();
      if (src.equals(""))
      {
        report.message(MessageId.HTM_008,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), name));
      }
    }

    if (src == null || !xrefChecker.isPresent())
    {
      return;
    }

    String srcMimeType = null;
    Matcher matcher = DATA_URI_PATTERN.matcher(src);
    if (matcher.matches())
    {
      srcMimeType = matcher.group(1);
    }
    else
    {
      if (PathUtil.isRemote(src))
      {
        requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
      }
      else
      {
        src = PathUtil.resolveRelativeReference(base, src);
      }

      XRefChecker.Type refType;
      if ("audio".equals(name))
      {
        refType = XRefChecker.Type.AUDIO;
      }
      else if ("video".equals(name))
      {
        refType = XRefChecker.Type.VIDEO;
      }
      else
      {
        refType = XRefChecker.Type.GENERIC;
      }
      if (!"img".equals(name)) // img already registered in super class
      {
        xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
            src, refType);
      }

      srcMimeType = xrefChecker.get().getMimeType(src);
    }

    if (srcMimeType == null)
    {
      return;
    }

    if (!context.mimeType.equals("image/svg+xml") && srcMimeType.equals("image/svg+xml"))
    {
      allowedProperties.add(ITEM_PROPERTIES.SVG);
    }

    if ((inAudio || inVideo || imbricatedObjects > 0 || imbricatedCanvases > 0)
        && OPFChecker30.isCoreMediaType(srcMimeType) && !name.equals("track"))
    {
      hasValidFallback = true;
    }

  }

  protected void processObject(XMLElement e)
  {
    imbricatedObjects++;

    String type = e.getAttribute("type");
    String data = e.getAttribute("data");

    if (data != null)
    {
      processSrc(e.getName(), data);
      data = PathUtil.resolveRelativeReference(base, data);
    }

    if (type != null && data != null && xrefChecker.isPresent()
        && !type.equals(xrefChecker.get().getMimeType(data)))
    {
      String context = "<object";
      for (int i = 0; i < e.getAttributeCount(); i++)
      {
        XMLAttribute attribute = e.getAttribute(i);
        context += " " + attribute.getName() + "=\"" + attribute.getValue() + "\"";
      }
      context += ">";
      report.message(MessageId.OPF_013,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), context),
          type, xrefChecker.get().getMimeType(data));
    }

    if (type != null)
    {
      if (!context.mimeType.equals("image/svg+xml") && type.equals("image/svg+xml"))
      {
        allowedProperties.add(ITEM_PROPERTIES.SVG);
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
    if (xrefChecker.isPresent() && type != null
        && xrefChecker.get().getBindingHandlerId(type) != null)
    {
      hasValidFallback = true;
    }
  }

  protected void processSVG(XMLElement e)
  {
    inSvg = true;
    if (!context.mimeType.equals("image/svg+xml"))
    {
      requiredProperties.add(ITEM_PROPERTIES.SVG);
    }
    else if (!isOutermostSVGAlreadyProcessed)
    {
      isOutermostSVGAlreadyProcessed = true;
      if (context.opfItem.isPresent() && context.opfItem.get().isFixedLayout()
          && e.getAttribute("viewBox") == null)
      {

        report.message(MessageId.HTM_048,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
      }
    }
  }

  protected void processTable(XMLElement e)
  {
    context.featureReport.report(FeatureEnum.TABLE, parser.getLocation());
  }

  protected void processFigure(XMLElement e)
  {
    context.featureReport.report(FeatureEnum.FIGURE, parser.getLocation());
  }

  private void processSemantics(XMLElement e)
  {
    if (e.getAttribute("itemscope") != null
        && !context.featureReport.hasFeature(FeatureEnum.HAS_MICRODATA))
    {
      context.featureReport.report(FeatureEnum.HAS_MICRODATA, parser.getLocation());
    }
    if (e.getAttribute("property") != null
        && !context.featureReport.hasFeature(FeatureEnum.HAS_RDFA))
    {
      context.featureReport.report(FeatureEnum.HAS_RDFA, parser.getLocation());
    }
  }

  private void processSectioning(XMLElement e)
  {
    if (isLinear && context.profile == EPUBProfile.EDUPUB
        && EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()))
    {
      if ("body".equals(e.getName()))
      {
        inBody = true;
      }
      else if (inBody && !"section".equals(e.getName()))
      {
        context.featureReport.report(FeatureEnum.SECTIONS, parser.getLocation());
        inBody = false;
      }
      else if ("section".equals(e.getName()))
      {
        inBody = false;
        context.featureReport.report(FeatureEnum.SECTIONS, parser.getLocation());
      }
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
      inVideo = false;
    }
    else if (name.equals("audio"))
    {
      if (imbricatedObjects == 0 && imbricatedCanvases == 0)
      {
        checkFallback("Audio");
      }
      inAudio = false;
    }
    else if (name.equals("a"))
    {
      if (anchorNeedsText)
      {
        report.message(MessageId.ACC_004,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), "a"));
        anchorNeedsText = false;
      }
      if ((inSvg || context.mimeType.equals("image/svg+xml")) && !hasTitle)
      {
        report.message(MessageId.ACC_011, EPUBLocation.create(path, parser.getLineNumber(),
            parser.getColumnNumber(), e.getName()));
      }
    }
    else if (name.equals("math"))
    {
      inMathML = false;
      if (!hasAltorAnnotation)
      {
        report.message(MessageId.ACC_009,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), "math"));
      }
    }
    else if (name.equals("nav") && inRegionBasedNav)
    {
      inRegionBasedNav = false;
    }
    else if (name.equals("picture"))
    {
      inPicture = false;
    }
    else if (name.equals("svg"))
    {
      inSvg = false;
    }
  }

  /*
   * Checks fallbacks for video, audio and object elements
   */
  protected void checkFallback(String elementType)
  {
    if (hasValidFallback)
    {
      hasValidFallback = false;
    }
    else
    {
      report.message(MessageId.MED_002,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()), elementType);
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

    Set<ITEM_PROPERTIES> uncheckedProperties = Sets.difference(itemProps, requiredProperties)
        .copyInto(EnumSet.noneOf(ITEM_PROPERTIES.class));
    uncheckedProperties.remove(ITEM_PROPERTIES.NAV);
    uncheckedProperties.remove(ITEM_PROPERTIES.DATA_NAV);
    uncheckedProperties.remove(ITEM_PROPERTIES.COVER_IMAGE);
    uncheckedProperties.removeAll(allowedProperties);
    if (uncheckedProperties.contains(ITEM_PROPERTIES.REMOTE_RESOURCES))
    {
      uncheckedProperties.remove(ITEM_PROPERTIES.REMOTE_RESOURCES);
      if (!requiredProperties.contains(ITEM_PROPERTIES.SCRIPTED)) {
        report.message(MessageId.OPF_018,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
      } else {
        report.message(MessageId.OPF_018b,
            EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber()));
      }
    }

    if (!uncheckedProperties.isEmpty())
    {
      report.message(MessageId.OPF_015, EPUBLocation.create(path), Joiner.on(", ")
          .join(PackageVocabs.ITEM_VOCAB.getNames(uncheckedProperties)));
    }
  }
}

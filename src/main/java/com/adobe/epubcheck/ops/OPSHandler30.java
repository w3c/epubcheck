package com.adobe.epubcheck.ops;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.*;
import com.adobe.epubcheck.xml.XMLAttribute;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;

import java.util.*;

public class OPSHandler30 extends OPSHandler
{
  String properties;

  final HashSet<String> prefixSet;

  final HashSet<String> propertiesSet;

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

  static final HashSet<String> linkClassSet;

  boolean reportedUnsupportedXMLVersion;

  static
  {
    HashSet<String> set = new HashSet<String>();
    set.add("vertical");
    set.add("horizontal");
    set.add("day");
    set.add("night");
    linkClassSet = set;
  }

  static final String[] scriptEventsStrings =
      {
          "onblur",
          "onchange",
          "oncontextmenu",
          "onfocus",
          "onformchange",
          "onforminput",
          "oninput",
          "oninvalid",
          "onselect",
          "onsubmit",
          "onkeydown",
          "onkeypress",
          "onkeyup",
          "onclick",
          "ondblclick",
          "ondrag",
          "ondragend",
          "ondragenter",
          "ondragleave",
          "ondragover",
          "ondragstart",
          "ondrop",
          "onmousedown",
          "onmousemove",
          "onmouseout",
          "onmouseover",
          "onmouseup",
          "onmousewheel",
          "onscroll"
      };
  static HashSet<String> scriptEvents;

  public static HashSet<String> getScriptEvents()
  {
    if (scriptEvents == null)
    {
      scriptEvents = new HashSet<String>();
      Collections.addAll(scriptEvents, scriptEventsStrings);
    }
    return scriptEvents;
  }

  static final String[] mouseEventsStrings =
      {
          "onclick",
          "ondblclick",
          "ondrag",
          "ondragend",
          "ondragenter",
          "ondragleave",
          "ondragover",
          "ondragstart",
          "ondrop",
          "onmousedown",
          "onmousemove",
          "onmouseout",
          "onmouseover",
          "onmouseup",
          "onmousewheel",
      };
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
      XRefChecker xrefChecker, XMLParser parser, Report report, EPUBVersion version)
  {
    super(ocf, path, xrefChecker, parser, report, version);
    this.mimeType = mimeType;
    this.properties = properties;
    prefixSet = new HashSet<String>();
    propertiesSet = new HashSet<String>();
    reportedUnsupportedXMLVersion = false;
  }


  void checkType(String type)
  {
    if (type == null)
    {
      return;
    }
    MetaUtils.validateProperties(type, EpubTypeAttributes.EpubTypeSet,
        prefixSet, path, parser.getLineNumber(),
        parser.getColumnNumber(), report, false);

  }

  void checkSSMLPh(String ph)
  {
    //issue 139; enhancement is to add real syntax check for IPA and x-SAMPA
    if (ph == null)
    {
      return;
    }
    if (ph.trim().length() < 1)
    {
      report.message(MessageId.HTM_007, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
  }

  @Override
  public void characters(char[] chars, int arg1, int arg2)
  {
    super.characters(chars, arg1, arg2);
    String str = new String(chars, arg1, arg2);
    str = str.trim();
    if (!str.equals("")
        && (audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0))
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

    if (!reportedUnsupportedXMLVersion)
    {
      reportedUnsupportedXMLVersion = HandlerUtil.checkXMLVersion(parser);
    }

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("html"))
    {
      HandlerUtil.processPrefixes(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"),
          prefixSet, report, path, parser.getLineNumber(),
          parser.getColumnNumber());
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
      propertiesSet.add("mathml");
      inMathML = true;
      hasAltorAnnotation = (null != e.getAttribute("alt"));
    }
    else if (!mimeType.equals("image/svg+xml") && name.equals("svg"))
    {
      propertiesSet.add("svg");
      processStartSvg(e);
    }
    else if (name.equals("script"))
    {
      propertiesSet.add("scripted");
    }
    else if (name.equals("switch"))
    {
      propertiesSet.add("switch");
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
    for (int i = 0; i < e.getAttributeCount(); ++i)
    {
      XMLAttribute attr = e.getAttribute(i);
      if (scriptEvents.contains(attr.getName().toLowerCase()))
      {
        propertiesSet.add("scripted");
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

    Set<String> values = MetaUtils.validateProperties(classAttribute,
        linkClassSet, null, path, parser.getLineNumber(),
        parser.getColumnNumber(), report, false);

    if (values.size() == 1)
    {
      return;
    }

    boolean vertical = false, horizontal = false, day = false, night = false;

    for (String attribute : values)
    {
      if (attribute.equals("vertical"))
      {
        vertical = true;
      }
      else if (attribute.equals("horizontal"))
      {
        horizontal = true;
      }
      else if (attribute.equals("day"))
      {
        day = true;
      }
      else if (attribute.equals("night"))
      {
        night = true;
      }
    }

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
        report.message(MessageId.ACC_011, new MessageLocation(path, parser.getLineNumber(),
            parser.getColumnNumber(), e.getName()));
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
      posterMimeType = xrefChecker.getMimeType(PathUtil
          .resolveRelativeReference(path, posterSrc, base));
    }

    if (posterMimeType != null
        && !OPFChecker.isBlessedImageType(posterMimeType))
    {
      report.message(MessageId.MED_001, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
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
        report.message(MessageId.HTM_008, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), name));
      }
    }

    if (src == null || xrefChecker == null)
    {
      return;
    }

		if (src.matches("^[^:/?#]+://.*"))
    {
      propertiesSet.add("remote-resources");
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
    xrefChecker.registerReference(path, parser.getLineNumber(),
        parser.getColumnNumber(), src, refType);

    String srcMimeType = xrefChecker.getMimeType(src);

    if (srcMimeType == null)
    {
      return;
    }

    if (!mimeType.equals("image/svg+xml")
        && srcMimeType.equals("image/svg+xml"))
    {
      propertiesSet.add("svg");
    }

    if ((audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0)
        && OPFChecker30.isCoreMediaType(srcMimeType)
        && !name.equals("track"))
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
          type,
          xrefChecker.getMimeType(data));
    }

    if (type != null)
    {
      if (!mimeType.equals("image/svg+xml") && type.equals("image/svg+xml"))
      {
        propertiesSet.add("svg");
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
    if (xrefChecker != null && type != null
        && xrefChecker.getBindingHandlerSrc(type) != null)
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
        foundXmlLang = foundXmlLang | (EpubConstants.XmlNamespaceUri.compareTo(a.getNamespace()) == 0);
        foundLang = (EpubConstants.HtmlNamespaceUri.compareTo(a.getNamespace()) == 0);
      }
    }
    if (!foundLang || !foundXmlLang)
    {
      report.message(MessageId.HTM_043, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), e.getName()));
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
    else if (name.equals("svg"))
    {
      inSvg = false;
      if (!hasAltorAnnotation)
      {
        report.message(MessageId.ACC_009, new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber(), "math"));
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
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
          elementType);
    }
  }

  void checkProperties()
  {
    Set<String> props = new HashSet<String>(Arrays.asList((properties!=null) ? properties.split("\\s+") : new String[]{})); 
    if (props.contains("singleFileValidation"))
    {
      return;
    }
    props.remove("nav");
    props.remove("cover-image");

    Iterator<String> propertyIterator = propertiesSet.iterator();
    while (propertyIterator.hasNext())
    {
      String prop = propertyIterator.next();
      if (props.contains(prop))
      {
        props.remove(prop);
      }
      else
      {
        report.message(MessageId.OPF_014, new MessageLocation(path, 0, 0), prop);
      }
    }

    if (props.contains("remote-resources"))
    {
      props.remove("remote-resources");
      report.message(MessageId.OPF_018,
          new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()));
    }
	
    if (!props.isEmpty())			
    {

      StringBuilder sb = new StringBuilder();
      ArrayList<String> remainingProps = new ArrayList<String>(props.size());
      Collections.addAll(remainingProps, props.toArray(new String[props.size()]));
      Collections.sort(remainingProps);
      boolean needsComma = false;
      for (String s : remainingProps)
      {
         if (needsComma)
         {
           sb.append(", ");
         }
         else
         {
           needsComma = true;
         }
         sb.append(s);
      }
      report.message(MessageId.OPF_015, new MessageLocation(path, 0, 0), sb.toString());
    }
  }
}

package com.adobe.epubcheck.ctc.xml;

import static com.adobe.epubcheck.opf.OPFChecker30.isBlessedAudioType;
import static com.adobe.epubcheck.opf.OPFChecker30.isBlessedVideoType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.NamespaceHelper;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class HTMLTagsAnalyseHandler extends DefaultHandler
{
  private String fileName;
  private Report report;
  private final HashSet<String> html5SpecTags;
  private final HashSet<String> html4SpecTags;
  private final HashSet<String> nonTextTagsAlt;
  private final HashSet<String> nonTextTagsTitle;
  private final HashSet<String> headerTags;

  private final Stack<String> tagStack;
  private int html4SpecTagsCounter = 0;
  private int html5SpecTagsCounter = 0;
  private final ArrayList<Integer> listItemCounters;
  private HashMap<String, ControlMark> formInputMarks;
  private Locator locator;
  private boolean hasTitle;
  private boolean inTitle;
  private boolean inFigure;
  private boolean inBlockQuote;
  private NamespaceHelper namespaceHelper = new NamespaceHelper();

  private final int HAS_INPUT = 1;
  private final int HAS_LABEL = 2;
  private boolean hasViewport = false;
  private boolean isFixed = false;
  private int landmarkNavCount = 0;
  private EPUBVersion version;

  public int getLandmarkNavCount()
  {
    return landmarkNavCount;
  }

  public int getHtml5SpecTagsCounter()
  {
    return html5SpecTagsCounter;
  }

  public HTMLTagsAnalyseHandler()
  {
    String[] HTML5SpecTags =
        {
            "article", "aside", "audio", "bdi", "canvas", "command", "datalist", "details", "dialog", "embed", "figcaption",
            "figure", "footer", "header", "hgroup", "keygen", "mark", "meter", "nav", "output", "progress", "rp", "rt", "ruby",
            "section", "source", "summary", "time", "track", "wbr", "video"
        };
    String[] HTML4SpecTags =
        {
            "acronym", "applet", "basefont", "big", "center", "dir", "font", "frame", "frameset", "noframes", "strike"
        };

    String[] NonTextTagsAlt =
        {
            "img", "area", // images
        };

    String[] NonTextTagsTitle =
        {
            "map", "figure",   // images
            "audio",                          // audio
            "video",                          // video
        };

    String[] HeaderTags =
        {
            "h1", "h2", "h3", "h4", "h5", "h6", // headers
        };

    this.html4SpecTags = new HashSet<String>();
    Collections.addAll(html4SpecTags, HTML4SpecTags);

    this.html5SpecTags = new HashSet<String>();
    Collections.addAll(html5SpecTags, HTML5SpecTags);

    this.nonTextTagsAlt = new HashSet<String>();
    Collections.addAll(nonTextTagsAlt, NonTextTagsAlt);

    this.nonTextTagsTitle = new HashSet<String>();
    Collections.addAll(nonTextTagsTitle, NonTextTagsTitle);

    this.headerTags = new HashSet<String>();
    Collections.addAll(headerTags, HeaderTags);

    tagStack = new Stack<String>();
    listItemCounters = new ArrayList<Integer>();
    formInputMarks = new HashMap<String, ControlMark>();
  }

  String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public void setVersion(EPUBVersion version)
  {
    this.version = version;
  }
  public EPUBVersion getVersion()
  {
    return version;
  }

  private class ControlMark
  {
    public String controlId;
    public int mark;
    public EPUBLocation location;
  }

  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
  }

  public void setReport(Report report)
  {
    this.report = report;
  }

  public boolean isFixed()
  {
    return isFixed;
  }

  public void setIsFixed(boolean isFixed)
  {
    this.isFixed = isFixed;
  }

  @Override
  public void notationDecl (String name, String publicId, String systemId)
      throws SAXException
  {
    System.out.printf("%1$s : %2$s : %3$s ", name, publicId, systemId);
  }

  @Override
  public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName)
      throws SAXException
  {
    System.out.printf("%1$s : %2$s : %3$s : %4$s", name, publicId, systemId, notationName);
  }

  @Override
  public void startPrefixMapping (String prefix, String uri) throws SAXException
  {
    namespaceHelper.declareNamespace(prefix, uri, EPUBLocation.create(fileName, locator.getLineNumber(), locator.getColumnNumber(), prefix), report);
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    namespaceHelper.onStartElement(fileName, locator, uri, qName, attributes, report);

    //outWriter.println("Start Tag -->:<" +qName+">");
    String tagName = qName.toLowerCase(Locale.ROOT);
    if (html5SpecTags.contains(tagName))
    {
      html5SpecTagsCounter++;
    }
    if (html4SpecTags.contains(tagName))
    {
      html4SpecTagsCounter++;
    }

    if (("source".compareTo(tagName) == 0) && ("video".compareTo(tagStack.peek()) == 0))
    {
      String mimeType = attributes.getValue("type");

      if (mimeType == null || !isBlessedVideoType(mimeType))
      {
        if (mimeType == null)
        {
          mimeType = "null";
        }
        report.message(MessageId.OPF_036, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber()), mimeType);
      }
    }
    else if (("source".compareTo(tagName) == 0) && ("audio".compareTo(tagStack.peek()) == 0))
    {
      String mimeType = attributes.getValue("type");

      if (mimeType == null || !isBlessedAudioType(mimeType))
      {
        if (mimeType == null)
        {
          mimeType = "null";
        }
        report.message(MessageId.OPF_056, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber()), mimeType);
      }
    }
    else if (("ul".compareTo(tagName) == 0) || ("ol".compareTo(tagName) == 0) || ("Dl".compareTo(tagName) == 0))
    {
      listItemCounters.add(0);
    }
    else if (("li".compareTo(tagName) == 0) &&
        (("ul".compareTo(tagStack.peek()) == 0) ||
            ("ol".compareTo(tagStack.peek()) == 0)))
    {
      listItemCounters.set(listItemCounters.size() - 1, 1 + listItemCounters.get(listItemCounters.size() - 1));
    }
    else if (("dh".compareTo(tagName) == 0) && ("dl".compareTo(tagStack.peek()) == 0))
    {
      listItemCounters.set(listItemCounters.size() - 1, 1 + listItemCounters.get(listItemCounters.size() - 1));
    }
    else if ("input".compareTo(tagName) == 0)
    {
      String id = attributes.getValue("id");
      String type = attributes.getValue("type");
      if (id != null)
      {
        ControlMark mark = formInputMarks.get(id);
        if (mark == null)
        {
          mark = new ControlMark();
          mark.controlId = id;
        }
        mark.location = EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), id);
        mark.mark |= HAS_INPUT;
        formInputMarks.put(id, mark);
      }
      else if (type == null || "submit".compareToIgnoreCase(type) != 0)  // submit buttons don't need a label
      {
        report.message(MessageId.HTM_028, EPUBLocation.create(this.fileName, locator.getLineNumber(), locator.getColumnNumber()), tagName);
      }
    }
    else if ("label".compareTo(tagName) == 0)
    {
      String id = attributes.getValue("for");
      if (id != null)
      {
        ControlMark mark = formInputMarks.get(id);
        if (mark == null)
        {
          mark = new ControlMark();
          mark.controlId = id;

          // only set the location if we are creating the entry here.  This location will be overwritten
          // by the input control location, but if there is no input that overrides it, the label location will
          // be the one reported.
          mark.location = EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), id);
        }
        mark.mark |= HAS_LABEL;
        formInputMarks.put(id, mark);
      }
      else
      {
        report.message(MessageId.HTM_029, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
      }
    }
    else if ("form".compareTo(tagName) == 0)
    {
      this.formInputMarks = new HashMap<String, ControlMark>();
    }
    else if ("html".compareTo(tagName) == 0)
    {
      String ns = attributes.getValue("xmlns");
      if (ns == null || EpubConstants.HtmlNamespaceUri.compareTo(ns) != 0)
      {
        report.message(MessageId.HTM_049, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
      }
    }
    else if ("body".compareTo(tagName) == 0)
    {
      String title = attributes.getValue("title");
      if (title != null && title.length() > 0)
      {
        hasTitle = true;
      }
    }
    else if (("title".compareTo(tagName) == 0) && ("head".compareTo(tagStack.peek()) == 0))
    {
      inTitle = true;
    }
    else if ("nav".compareTo(tagName) == 0)
    {
      String epubPrefix = namespaceHelper.findPrefixForUri(EpubConstants.EpubTypeNamespaceUri);
      String type = attributes.getValue(epubPrefix+":type");
      if (type != null && "landmarks".compareToIgnoreCase(type) == 0)
      {
        ++landmarkNavCount;
      }
    }
    else if ("blockquote".compareTo(tagName) == 0)
    {
      inBlockQuote = true;
    }
    else if ("figure".compareTo(tagName) == 0)
    {
      inFigure = true;
    }
    else if ("meta".compareTo(tagName) == 0)
    {
      String nameAttribute = attributes.getValue("name");
      if (nameAttribute != null && "viewport".compareTo(nameAttribute) == 0)
      {
        hasViewport = true;
        String contentAttribute = attributes.getValue("content");
        if (isFixed && (contentAttribute == null || !(contentAttribute.contains("width") && contentAttribute.contains("height"))))
        {
          report.message(MessageId.HTM_047, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
        }
      }
    }
    if (headerTags.contains(tagName))
    {
      if (inBlockQuote || inFigure)
      {
        report.message(MessageId.ACC_010, EPUBLocation.create(getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
      }
    }

    if (nonTextTagsAlt.contains(tagName))
    {
      if (null != this.getFileName() && null == attributes.getValue("alt"))
      {
        report.message(MessageId.ACC_001, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
      }
    }
    if (nonTextTagsTitle.contains(tagName))
    {
      if (null != this.getFileName() && null == attributes.getValue("title"))
      {
        report.message(MessageId.ACC_003, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), tagName));
      }
    }
    String epubPrefix = namespaceHelper.findPrefixForUri(EpubConstants.EpubTypeNamespaceUri);
    if (epubPrefix != null)
    {
      String typeAttr = attributes.getValue(epubPrefix+":type");
      if (typeAttr != null)
      {
        if (typeAttr.contains("pagebreak"))
        {
          report.message(MessageId.HTM_050, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), "pagebreak"));
        }
      }
    }
    tagStack.push(tagName);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    namespaceHelper.onEndElement(report);

    String tagName = qName.toLowerCase(Locale.ROOT);
    String top = tagStack.pop();

    if (top.compareTo(tagName) == 0)
    {
      if (("ul".compareTo(tagName) == 0) || ("ol".compareTo(tagName) == 0) || ("Dl".compareTo(tagName) == 0))
      {
        Integer count = listItemCounters.remove(listItemCounters.size() - 1);
        if (count < 1)
        {
          report.message(MessageId.HTM_027,
              EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber(), qName)
          );
        }
      }
      if ("body".compareTo(tagName) == 0)
      {
        for (String id : formInputMarks.keySet())
        {
          ControlMark mark = formInputMarks.get(id);
          if (((mark.mark & HAS_LABEL) != HAS_LABEL) && (mark.mark & HAS_INPUT) == HAS_INPUT)
          {
            report.message(MessageId.ACC_002, mark.location, id);
          }
        }
      }
      if (inTitle && "title".compareTo(tagName) == 0)
      {
        inTitle = false;
      }
      else if ("head".compareTo(tagName) == 0)
      {
        if (!hasTitle)
        {
          report.message(MessageId.HTM_033, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber()));
        }
        if (isFixed() && !hasViewport)
        {
          report.message(MessageId.HTM_046, EPUBLocation.create(this.getFileName(), locator.getLineNumber(), locator.getColumnNumber()));
        }
      }
      else if ("blockquote".compareTo(tagName) == 0)
      {
        inBlockQuote = false;
      }
      else if ("figure".compareTo(tagName) == 0)
      {
        inFigure = false;
      }
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException
  {
    if (inTitle && (length > 0))
    {
      hasTitle = true;
    }
  }
}
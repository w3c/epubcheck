package com.adobe.epubcheck.overlay;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.*;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

import java.util.HashSet;

public class OverlayHandler implements XMLHandler
{

  private final String path;

  private final XRefChecker xrefChecker;

  private final Report report;

  private final HashSet<String> prefixSet;

  private final XMLParser parser;

  private boolean checkedUnsupportedXMLVersion;

  public OverlayHandler(String path, XRefChecker xrefChecker,
      XMLParser parser, Report report)
  {
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.report = report;
    this.parser = parser;
    prefixSet = new HashSet<String>();
    checkedUnsupportedXMLVersion = false;
  }

  public void startElement()
  {
    if (!checkedUnsupportedXMLVersion)
    {
      HandlerUtil.checkXMLVersion(parser);
      checkedUnsupportedXMLVersion = true;
    }

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if (name.equals("smil"))
    {
      HandlerUtil.processPrefixes(
          e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"),
          prefixSet, report, path, parser.getLineNumber(),
          parser.getColumnNumber());
    }
    else if (name.equals("seq"))
    {
      processSeq(e);
    }
    else if (name.equals("text"))
    {
      processSrc(e);
    }
    else if (name.equals("audio"))
    {
      processRef(e.getAttribute("src"), XRefChecker.RT_AUDIO);
    }
    else if (name.equals("body") || name.equals("par"))
    {
      checkType(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));
    }
  }

  private void checkType(String type)
  {
    if (type == null)
    {
      return;
    }
    MetaUtils.validateProperties(type, EpubTypeAttributes.EpubTypeSet,
        prefixSet, path, parser.getLineNumber(),
        parser.getColumnNumber(), report, false);
  }

  private void processSrc(XMLElement e)
  {
    processRef(e.getAttribute("src"), XRefChecker.RT_HYPERLINK);

  }

  private void processRef(String ref, int type)
  {
    if (ref != null && xrefChecker != null)
    {
      ref = PathUtil.resolveRelativeReference(path, ref, null);
      if (type == XRefChecker.RT_AUDIO)
      {
        String mimeType = xrefChecker.getMimeType(ref);
        if (mimeType != null
            && !OPFChecker30.isBlessedAudioType(mimeType))
        {
          report.message(MessageId.MED_005,
              new MessageLocation(path, parser.getLineNumber(), parser.getColumnNumber()),
              ref,
              mimeType);
        }
      }
      xrefChecker.registerReference(path, parser.getLineNumber(),
          parser.getColumnNumber(), ref, type);
    }
  }

  private void processSeq(XMLElement e)
  {
    processRef(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "textref"), XRefChecker.RT_HYPERLINK);
    checkType(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));
  }

  public void characters(char[] chars, int arg1, int arg2)
  {
  }

  public void endElement()
  {
  }

  public void ignorableWhitespace(char[] chars, int arg1, int arg2)
  {
  }

  public void processingInstruction(String arg0, String arg1)
  {
  }

}

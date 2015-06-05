/*
 * Copyright (c) 2007 Adobe Systems Incorporated
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

package com.adobe.epubcheck.ops;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Stack;

import javax.xml.XMLConstants;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Optional;

public class OPSHandler implements XMLHandler
{

  class ElementLocation
  {
    int lineNumber;
    int columnNumber;

    public ElementLocation(int line, int column)
    {
      this.lineNumber = line;
      this.columnNumber = column;
    }

    int getLineNumber()
    {
      return lineNumber;
    }

    int getColumnNumber()
    {
      return columnNumber;
    }
  }

  static final HashSet<String> regURISchemes = fillRegURISchemes();

  /**
   * null unless head/base or xml:base is given
   */
  protected String base;

  protected final ValidationContext context;
  protected final XMLParser parser;
  protected final String path;
  protected final Report report;
  protected final Optional<XRefChecker> xrefChecker;
  protected long openElements;
  protected long charsCount;
  protected int tableDepth = 0;
  protected boolean hasTh = false;
  protected boolean hasThead = false;
  protected boolean hasCaption = false;
  protected boolean epubTypeInUse = false;
  protected boolean checkedUnsupportedXMLVersion = false;
  protected StringBuilder textNode;
  protected Stack<ElementLocation> elementLocationStack = new Stack<ElementLocation>();

  public OPSHandler(ValidationContext context, XMLParser parser)
  {
    this.context = context;
    this.path = context.path;
    this.xrefChecker = context.xrefChecker;
    this.report = context.report;
    this.parser = parser;
  }

  protected void checkPaint(XMLElement e, String attr)
  {
    String paint = e.getAttribute(attr);
    if (xrefChecker.isPresent() && paint != null && paint.startsWith("url(") && paint.endsWith(")"))
    {
      String href = paint.substring(4, paint.length() - 1);
      href = PathUtil.resolveRelativeReference(path, href, base);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_SVG_PAINT);
    }
  }

  protected void checkImage(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(path, href, base);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_IMAGE);
    }
  }

  protected void checkObject(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(path, href, base);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_OBJECT);
    }
  }

  protected void checkLink(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    String rel = e.getAttributeNS(attrNS, "rel");
    if (xrefChecker.isPresent() && href != null && rel != null && rel.contains("stylesheet")
        && rel.toLowerCase().indexOf("stylesheet") >= 0)
    {
      href = PathUtil.resolveRelativeReference(path, href, base);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_STYLESHEET);
    }
  }

  protected void checkSymbol(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(path, href, base);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_SVG_SYMBOL);
    }
  }

  protected void checkHRef(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    // outWriter.println("HREF: '" + href +"'");
    if (href == null)
    {
      return;
    }

    if (href.contains("#epubcfi"))
    {
      return; // temp until cfi implemented
    }

    href = href.trim();

    if (href.length() < 1)
    {
      // if href="" then selfreference which is valid,
      // but as per issue 225, issue a hint
      report.message(MessageId.HTM_045,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href));
      return;
    }

    if (".".equals(href))
    {
      // selfreference, no need to check
    }

    if (href.startsWith("http"))
    {
      report.info(path, FeatureEnum.REFERENCE, href);
    }

    /*
     * mgy 20120417 adding check for base to initial if clause as part of
     * solution to issue 155
     */
    if (isRegisteredSchemeType(href) || (null != base && isRegisteredSchemeType(base)))
    {
      return;
    }

    // This if statement is needed to make sure XML Fragment identifiers
    // are not reported as non-registered URI scheme types
    else if (href.indexOf(':') > 0)
    {
      report.message(MessageId.HTM_025,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href));
      return;
    }

    try
    {
      href = PathUtil.resolveRelativeReference(path, href, base);
    } catch (IllegalArgumentException err)
    {
      report.message(MessageId.OPF_010,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href),
          err.getMessage());
      return;
    }
    if (xrefChecker.isPresent())
    {
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(), href,
          XRefChecker.RT_HYPERLINK);
    }
  }

  public static boolean isRegisteredSchemeType(String href)
  {
    int colonIndex = href.indexOf(':');
    return colonIndex >= 0
        && (regURISchemes.contains(href.substring(0, colonIndex + 1)) || href.length() > colonIndex + 2
            && href.substring(colonIndex + 1, colonIndex + 3).equals("//")
            && regURISchemes.contains(href.substring(0, colonIndex + 3)));
  }

  public void startElement()
  {
    openElements++;
    XMLElement e = parser.getCurrentElement();
    ElementLocation currentLocation = new ElementLocation(parser.getLineNumber(),
        parser.getColumnNumber());
    elementLocationStack.push(currentLocation);

    if (!checkedUnsupportedXMLVersion)
    {
      HandlerUtil.checkXMLVersion(parser);
      checkedUnsupportedXMLVersion = true;
    }

    String id = e.getAttribute("id");

    String baseTest = e.getAttributeNS(XMLConstants.XML_NS_URI, "base");
    if (baseTest != null)
    {
      base = baseTest;
    }

    if (!epubTypeInUse)
    {
      String eNS = e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type");
      if (eNS != null)
      {
        epubTypeInUse = true;
      }
    }

    String ns = e.getNamespace();
    String name = e.getName().toLowerCase();
    int resourceType = XRefChecker.RT_GENERIC;
    if (ns != null)
    {
      if (ns.equals("http://www.w3.org/2000/svg"))
      {
        if (name.equals("lineargradient") || name.equals("radialgradient")
            || name.equals("pattern"))
        {
          resourceType = XRefChecker.RT_SVG_PAINT;
        }
        else if (name.equals("clippath"))
        {
          resourceType = XRefChecker.RT_SVG_CLIP_PATH;
        }
        else if (name.equals("symbol"))
        {
          resourceType = XRefChecker.RT_SVG_SYMBOL;
        }
        else if (name.equals("a"))
        {
          checkHRef(e, "http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("use"))
        {
          checkSymbol(e, "http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("image"))
        {
          checkImage(e, "http://www.w3.org/1999/xlink", "href");
        }
        checkPaint(e, "fill");
        checkPaint(e, "stroke");
      }
      else if (ns.equals(EpubConstants.HtmlNamespaceUri))
      {
        if (name.equals("a"))
        {
          checkHRef(e, null, "href");
        }
        else if (name.equals("img"))
        {
          checkImage(e, null, "src");
        }
        else if (name.equals("object"))
        {
          checkObject(e, null, "data");
        }
        else if (name.equals("link"))
        {
          checkLink(e, null, "href");
        }
        else if (name.equals("base"))
        {
          base = e.getAttribute("href");
        }
        else if (name.equals("style"))
        {
          textNode = new StringBuilder();
        }
        else if (name.equals("iframe"))
        {
          checkIFrame(e);
        }
        else if (name.equals("table"))
        {
          ++tableDepth;
        }
        else if (name.equals("th") && tableDepth > 0)
        {
          hasTh = true;
        }
        else if (name.equals("thead") && tableDepth > 0)
        {
          hasThead = true;
        }
        else if (name.equals("caption") && tableDepth > 0)
        {
          hasCaption = true;
        }
        else if (name.equals("i") || name.equals("b") || name.equals("em") || name.equals("strong"))
        {
          checkBoldItalics(e);
        }

        resourceType = XRefChecker.RT_HYPERLINK;

        String style = e.getAttribute("style");
        if (style != null && style.length() > 0)
        {
          CSSCheckerFactory.getInstance()
              .newInstance(context, style, currentLocation.getLineNumber(), true).runChecks();
        }
      }
    }
    if (xrefChecker.isPresent() && id != null)
    {
      xrefChecker.get().registerAnchor(path, currentLocation.getLineNumber(),
          currentLocation.getColumnNumber(), id, resourceType);
    }
  }

  protected void checkBoldItalics(XMLElement e)
  {
    report.message(MessageId.HTM_038,
        EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), e.getName()));
  }

  protected void checkIFrame(XMLElement e)
  {
    report.message(MessageId.HTM_036,
        EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), e.getName()));
  }

  public void endElement()
  {
    openElements--;
    XMLElement e = parser.getCurrentElement();
    String ns = e.getNamespace();
    String name = e.getName();

    if (openElements == 0)
    {
      report.info(path, FeatureEnum.CHARS_COUNT, Long.toString(charsCount));
      if (!epubTypeInUse)
      {
        if (context.version == EPUBVersion.VERSION_3)
        {
          report.message(MessageId.ACC_007, EPUBLocation.create(path));
        }
      }
      else
      {
        epubTypeInUse = false;
      }
    }

    ElementLocation currentLocation = elementLocationStack.pop();

    boolean inXhtml = EpubConstants.HtmlNamespaceUri.equals(ns);

    if (inXhtml && "script".equals(name))
    {
      String attr = e.getAttribute("type");
      report.info(path, FeatureEnum.HAS_SCRIPTS, (attr == null) ? "" : attr);
    }

    if (inXhtml && "style".equals(name))
    {
      String style = textNode.toString();
      if (style.length() > 0)
      {
        CSSCheckerFactory.getInstance()
            .newInstance(context, style, currentLocation.getLineNumber(), false).runChecks();
      }
      textNode = null;
    }

    if (inXhtml && ("table".equals(name)))
    {
      if (tableDepth > 0)
      {
        --tableDepth;
        EPUBLocation location = EPUBLocation.create(path, currentLocation.getLineNumber(), currentLocation.getColumnNumber(), "table");

        checkDependentCondition(MessageId.ACC_005, tableDepth == 0, hasTh, location);
        checkDependentCondition(MessageId.ACC_006, tableDepth == 0, hasThead, location);
        checkDependentCondition(MessageId.ACC_012, tableDepth == 0, hasCaption, location);

        hasTh = hasThead = hasCaption = false;
      }
    }
  }

  // Report the message id when primary condition1 is true but dependent
  // condition2 is false.
  protected void checkDependentCondition(MessageId id, boolean condition1, boolean condition2,
      EPUBLocation location)
  {
    if (condition1 && !condition2)
    {
      report.message(id, location);
    }
  }

  public void ignorableWhitespace(char[] chars, int arg1, int arg2)
  {
  }

  public void characters(char[] chars, int start, int length)
  {
    charsCount += length;
    if (textNode != null)
    {
      textNode.append(chars, start, length);
    }
  }

  public void processingInstruction(String arg0, String arg1)
  {
  }

  static HashSet<String> fillRegURISchemes()
  {
    InputStream schemaStream = null;
    BufferedReader schemaReader = null;
    try
    {
      HashSet<String> set = new HashSet<String>();
      schemaStream = OPSHandler.class.getResourceAsStream("registeredSchemas.txt");
      schemaReader = new BufferedReader(new InputStreamReader(schemaStream));
      String schema = schemaReader.readLine();
      while (schema != null)
      {
        set.add(schema);
        schema = schemaReader.readLine();
      }
      return set;
    } catch (Exception e)
    {
      e.printStackTrace();
    } finally
    {
      try
      {
        if (schemaReader != null)
        {
          schemaReader.close();
        }
        if (schemaStream != null)
        {
          schemaStream.close();
        }
      } catch (Exception ignored)
      {
      }
    }
    return null;
  }
}

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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Stack;

import javax.xml.XMLConstants;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.URISchemes;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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

  protected String base;
  protected String baseScheme;
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
  protected Optional<EPUBLocation> nonStandardStylesheetLink = Optional.absent();
  protected boolean hasCss = false;
  protected boolean epubTypeInUse = false;
  protected boolean checkedUnsupportedXMLVersion = false;
  protected StringBuilder textNode;
  protected Stack<ElementLocation> elementLocationStack = new Stack<ElementLocation>();

  public OPSHandler(ValidationContext context, XMLParser parser)
  {
    this.context = context;
    this.path = context.path;
    this.base = path;
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
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.SVG_PAINT);
    }
  }

  protected void checkImage(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.IMAGE);
    }
  }

  protected void checkObject(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.OBJECT);
    }
  }

  protected void checkLink(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    String rel = e.getAttributeNS(attrNS, "rel");
    if (xrefChecker.isPresent() && href != null && rel != null
        && rel.toLowerCase(Locale.ROOT).contains("stylesheet"))
    {
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.STYLESHEET);

      // Check the mimetype to record possible non-standard stylesheets
      // with no fallback
      String mimetype = xrefChecker.get().getMimeType(href);
      if (mimetype != null)
      {
        if (OPFChecker.isBlessedStyleType(mimetype)
            || OPFChecker.isDeprecatedBlessedStyleType(mimetype))
        {
          hasCss = true;
        }
        else
        {
          nonStandardStylesheetLink = Optional.of(
              EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href));
        }
      }
    }
  }

  protected void checkStylesheetFallback()
  {
    // stylesheet is considered as having "built-in" fallback if
    // at least one is found with a blessed CMT (i.e. text/css).
    // Implem note: xrefChecker is necessarily present if
    // nonStandardStylesheetLink is present.
    if (nonStandardStylesheetLink.isPresent() && !hasCss && !xrefChecker.get()
        .hasValidFallback(nonStandardStylesheetLink.get().getContext().get()).or(false))
    {
      report.message(MessageId.CSS_010, nonStandardStylesheetLink.get());
    }
  }

  // end head: if no-css stylesheet found AND no css present, report CSS_010

  protected void checkSymbol(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.SVG_SYMBOL);
    }
  }

  protected void checkHRef(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (href == null)
    {
      return;
    }
    href = href.trim();
    if (href.isEmpty())
    {
      // if href="" then selfreference which is valid,
      // but as per issue 225, issue a hint
      report.message(MessageId.HTM_045,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href));
      return;
    }
    else if (href.contains("#epubcfi"))
    {
      return; // temp until cfi implemented
    }
    else if (".".equals(href))
    {
      // selfreference, no need to check
      return;
    }

    URI uri = checkURI(href);
    if (uri == null) return;

    if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
    {
      report.info(path, FeatureEnum.REFERENCE, href);

      // Report if the host part couldn't be parsed correctly
      // (either due to missing slashes (issue #708) or invalid characters (issue #1034)
      if (uri.getHost() == null)
      {
        try
        {
          // if the URL contains underscore characters, try reparsing it without them,
          // as underscores are accepted by browsers in the host part (even if it's disallowed)
          // see issue #1079 
          if (!href.contains("_") || new URI(href.replace('_', 'x')).getHost() == null) {
            report.message(MessageId.RSC_023, parser.getLocation(), uri);
          }
        } catch (URISyntaxException ignored)
        {
          // ignored (well-formedness errors are caught earlier)
        }
      }
    }

    /*
     * mgy 20120417 adding check for base to initial if clause as part of
     * solution to issue 155
     */
    if (URISchemes.contains(uri.getScheme())
        || (null != base && URISchemes.contains(baseScheme)))
    {
      return;
    }
    // This if statement is needed to make sure XML Fragment identifiers
    // are not reported as non-registered URI scheme types
    else if (uri.getScheme() != null)
    {
      report.message(MessageId.HTM_025,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href));
      return;
    }

    try
    {
      href = PathUtil.resolveRelativeReference(base, href);
    } catch (IllegalArgumentException err)
    {
      report.message(MessageId.OPF_010,
          EPUBLocation.create(path, parser.getLineNumber(), parser.getColumnNumber(), href),
          err.getMessage());
      return;
    }
    processHyperlink(href);
  }
  


  protected void checkSVGFontFaceURI(XMLElement e, String attrNS, String attr)
  {
    String href = e.getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(base, href);
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.FONT);
    }
  }

  protected void processHyperlink(String href)
  {
    if (xrefChecker.isPresent())
    {
      xrefChecker.get().registerReference(path, parser.getLineNumber(), parser.getColumnNumber(),
          href, XRefChecker.Type.HYPERLINK);
    }
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
      URI baseURI = checkURI(baseTest);
      baseScheme = baseURI.getScheme();
      base = PathUtil.resolveRelativeReference(path, baseURI.toString());
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
    String name = e.getName().toLowerCase(Locale.ROOT);
    XRefChecker.Type resourceType = XRefChecker.Type.GENERIC;
    if (ns != null)
    {
      if (ns.equals("http://www.w3.org/2000/svg"))
      {
        if (name.equals("lineargradient") || name.equals("radialgradient")
            || name.equals("pattern"))
        {
          resourceType = XRefChecker.Type.SVG_PAINT;
        }
        else if (name.equals("clippath"))
        {
          resourceType = XRefChecker.Type.SVG_CLIP_PATH;
        }
        else if (name.equals("symbol"))
        {
          resourceType = XRefChecker.Type.SVG_SYMBOL;
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
        else if (name.equals("font-face-uri"))
        {
          checkSVGFontFaceURI(e, "http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("script"))
        {
          checkScript(e);
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
          URI baseURI = checkURI(e.getAttribute("href"));
          baseScheme = baseURI.getScheme();
          base = PathUtil.resolveRelativeReference(path, baseURI.toString());
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
        else if (name.equals("script"))
        {
          checkScript(e);
        }

        resourceType = XRefChecker.Type.HYPERLINK;

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

  protected URI checkURI(String uri)
  {
    try
    {
      return new URI(Preconditions.checkNotNull(uri).trim());
    } catch (URISyntaxException e)
    {
      report.message(MessageId.RSC_020, parser.getLocation(), uri);
      return null;
    }
  }
  
  protected void checkScript(XMLElement e) {
      String type = e.getAttribute("type");
      if (type == null || OPFChecker.isScriptType(type)) {
        processJavascript();
      }
  }
  
  protected void processJavascript()
  {
    report.info(path, FeatureEnum.HAS_SCRIPTS, "");
    context.featureReport.report(FeatureEnum.HAS_SCRIPTS, EPUBLocation.create(path, 
        parser.getLineNumber(), parser.getColumnNumber()));
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

    if (EpubConstants.HtmlNamespaceUri.equals(ns))
    {

      if ("style".equals(name))
      {
        String style = textNode.toString();
        if (style.length() > 0)
        {
          CSSCheckerFactory.getInstance()
              .newInstance(context, style, currentLocation.getLineNumber(), false).runChecks();
        }
        textNode = null;
      }
      else if ("head".equals(name))
      {
        checkStylesheetFallback();
      }
      else if ("table".equals(name))
      {
        if (tableDepth > 0)
        {
          --tableDepth;
          EPUBLocation location = EPUBLocation.create(path, currentLocation.getLineNumber(),
              currentLocation.getColumnNumber(), "table");

          checkDependentCondition(MessageId.ACC_005, tableDepth == 0, hasTh, location);
          checkDependentCondition(MessageId.ACC_006, tableDepth == 0, hasThead, location);
          checkDependentCondition(MessageId.ACC_012, tableDepth == 0, hasCaption, location);

          hasTh = hasThead = hasCaption = false;
        }
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

}

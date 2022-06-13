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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Stack;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.css.CSSChecker;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.URISchemes;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class OPSHandler extends XMLHandler
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

  protected final Optional<XRefChecker> xrefChecker;
  protected long openElements;
  protected long charsCount;
  protected int tableDepth = 0;
  protected boolean hasTh = false;
  protected boolean hasThead = false;
  protected boolean hasCaption = false;
  protected boolean epubTypeInUse = false;
  protected StringBuilder textNode;
  protected Stack<ElementLocation> elementLocationStack = new Stack<ElementLocation>();

  public OPSHandler(ValidationContext context)
  {
    super(context);
    this.xrefChecker = context.xrefChecker;
  }

  protected void checkPaint(String attr)
  {
    String paint = currentElement().getAttribute(attr);
    if (xrefChecker.isPresent() && paint != null && paint.startsWith("url(") && paint.endsWith(")"))
    {
      String href = paint.substring(4, paint.length() - 1);
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.SVG_PAINT);
    }
  }

  protected void checkImage(String attrNS, String attr)
  {
    String href = currentElement().getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.IMAGE);
    }
  }

  protected void checkObject(String attrNS, String attr)
  {
    String href = currentElement().getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.OBJECT);
    }
  }

  protected void checkLink(String attrNS, String attr)
  {
    XMLElement e = currentElement();
    String href = e.getAttributeNS(attrNS, attr);
    String rel = e.getAttributeNS(attrNS, "rel");
    if (xrefChecker.isPresent() && href != null && rel != null
        && rel.toLowerCase(Locale.ROOT).contains("stylesheet"))
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.STYLESHEET);
    }
  }

  // end head: if no-css stylesheet found AND no css present, report CSS_010

  protected void checkSymbol(String attrNS, String attr)
  {
    String href = currentElement().getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.SVG_SYMBOL);
    }
  }

  protected void checkHRef(String attrNS, String attr)
  {
    String href = currentElement().getAttributeNS(attrNS, attr);
    if (href == null)
    {
      return;
    }
    href = href.trim();
    if (href.isEmpty())
    {
      // if href="" then selfreference which is valid,
      // but as per issue 225, issue a hint
      report.message(MessageId.HTM_045, location());
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
      // (either due to missing slashes (issue #708) or invalid characters
      // (issue #1034)
      if (uri.getHost() == null)
      {
        try
        {
          // if the URL contains underscore characters, try reparsing it without
          // them,
          // as underscores are accepted by browsers in the host part (even if
          // it's disallowed)
          // see issue #1079
          if (!href.contains("_") || new URI(href.replace('_', 'x')).getHost() == null)
          {
            report.message(MessageId.RSC_023, location(), uri);
          }
        } catch (URISyntaxException ignored)
        {
          // ignored (well-formedness errors are caught earlier)
        }
      }
    }
    if ("file".equals(uri.getScheme()))
    {
      report.message(MessageId.HTM_053, location(), uri);
    }

    /*
     * mgy 20120417 adding check for base to initial if clause as part of
     * solution to issue 155
     */
    if (URISchemes.contains(uri.getScheme()) || (URISchemes.contains(getURIScheme(baseURL()))))
    {
      return;
    }
    // This if statement is needed to make sure XML Fragment identifiers
    // are not reported as non-registered URI scheme types
    else if (uri.getScheme() != null)
    {
      report.message(MessageId.HTM_025,
          EPUBLocation.create(path, location().getLine(), location().getColumn(), href));
      return;
    }

    try
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
    } catch (IllegalArgumentException err)
    {
      report.message(MessageId.OPF_010,
          EPUBLocation.create(path, location().getLine(), location().getColumn(), href),
          err.getMessage());
      return;
    }
    processHyperlink(href);
  }

  protected void checkSVGFontFaceURI(String attrNS, String attr)
  {
    String href = currentElement().getAttributeNS(attrNS, attr);
    if (xrefChecker.isPresent() && href != null)
    {
      href = PathUtil.resolveRelativeReference(baseURL(), href);
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.FONT);
    }
  }

  protected void processHyperlink(String href)
  {
    if (xrefChecker.isPresent())
    {
      xrefChecker.get().registerReference(path, location().getLine(), location().getColumn(), href,
          XRefChecker.Type.HYPERLINK);
    }
  }

  @Override
  public void startElement()
  {
    openElements++;
    XMLElement e = currentElement();
    ElementLocation currentLocation = new ElementLocation(location().getLine(),
        location().getColumn());
    elementLocationStack.push(currentLocation);

    String id = e.getAttribute("id");

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
          checkHRef("http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("use"))
        {
          checkSymbol("http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("image"))
        {
          checkImage("http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("font-face-uri"))
        {
          checkSVGFontFaceURI("http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("script"))
        {
          checkScript();
        }
        checkPaint("fill");
        checkPaint("stroke");
      }
      else if (ns.equals(EpubConstants.HtmlNamespaceUri))
      {
        if (name.equals("a"))
        {
          checkHRef(null, "href");
        }
        else if (name.equals("img"))
        {
          checkImage(null, "src");
        }
        else if (name.equals("object"))
        {
          checkObject(null, "data");
        }
        else if (name.equals("link"))
        {
          checkLink(null, "href");
        }
        else if (name.equals("style"))
        {
          textNode = new StringBuilder();
        }
        else if (name.equals("iframe"))
        {
          checkIFrame();
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
          checkBoldItalics();
        }
        else if (name.equals("script"))
        {
          checkScript();
        }

        resourceType = XRefChecker.Type.HYPERLINK;

        String style = e.getAttribute("style");
        if (style != null && style.length() > 0)
        {
          new CSSChecker(context, style, currentLocation.getLineNumber(), true).check();
        }
      }
    }
    if (xrefChecker.isPresent() && id != null)
    {
      xrefChecker.get().registerAnchor(path, currentLocation.getLineNumber(),
          currentLocation.getColumnNumber(), id, resourceType);
    }
  }

  protected void checkBoldItalics()
  {
    report.message(MessageId.HTM_038, EPUBLocation.create(path, location().getLine(),
        location().getColumn(), currentElement().getName()));
  }

  protected void checkIFrame()
  {
    report.message(MessageId.HTM_036, EPUBLocation.create(path, location().getLine(),
        location().getColumn(), currentElement().getName()));
  }

  protected URI checkURI(String uri)
  {
    try
    {
      return new URI(Preconditions.checkNotNull(uri).trim());
    } catch (URISyntaxException e)
    {
      report.message(MessageId.RSC_020, location(), uri);
      return null;
    }
  }
  
  private String getURIScheme(String uri) {
    try
    {
      return new URI(uri).getScheme();
    } catch (URISyntaxException e)
    {
      return null;
    }
  }

  protected void checkScript()
  {
    String type = currentElement().getAttribute("type");
    if (type == null || OPFChecker.isScriptType(type))
    {
      processJavascript();
    }
  }

  protected void processJavascript()
  {
    report.info(path, FeatureEnum.HAS_SCRIPTS, "");
    context.featureReport.report(FeatureEnum.HAS_SCRIPTS, location());
  }

  @Override
  public void endElement()
  {
    openElements--;
    XMLElement e = currentElement();
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
          new CSSChecker(context, style, currentLocation.getLineNumber(), false).check();
        }
        textNode = null;
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

  @Override
  public void characters(char[] chars, int start, int length)
  {
    charsCount += length;
    if (textNode != null)
    {
      textNode.append(chars, start, length);
    }
  }
  
}

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
import com.adobe.epubcheck.util.URISchemes;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Optional;

import io.mola.galimatias.URL;

public class OPSHandler extends XMLHandler
{

  protected final Optional<XRefChecker> xrefChecker;
  protected long openElements;
  protected long charsCount;
  protected int tableDepth = 0;
  protected boolean hasTh = false;
  protected boolean hasThead = false;
  protected boolean hasCaption = false;
  protected boolean epubTypeInUse = false;
  protected StringBuilder textNode;
  protected Stack<EPUBLocation> elementLocationStack = new Stack<EPUBLocation>();

  public OPSHandler(ValidationContext context)
  {
    super(context);
    this.xrefChecker = context.xrefChecker;
  }

  private void checkPaint(String attr)
  {
    String paint = currentElement().getAttribute(attr);
    if (xrefChecker.isPresent() && paint != null && paint.startsWith("url(") && paint.endsWith(")"))
    {
      URL url = checkURL(paint.substring(4, paint.length() - 1));
      xrefChecker.get().registerReference(url, XRefChecker.Type.SVG_PAINT, location());
    }
  }

  protected void checkImage(String attrNS, String attr)
  {
    URL imageURL = checkURL(currentElement().getAttributeNS(attrNS, attr));
    if (xrefChecker.isPresent() && imageURL != null)
    {
      xrefChecker.get().registerReference(imageURL, XRefChecker.Type.IMAGE, location());
    }
  }

  private void checkObject()
  {
    URL objectURL = checkURL(currentElement().getAttribute("data"));
    if (xrefChecker.isPresent() && objectURL != null)
    {
      xrefChecker.get().registerReference(objectURL, XRefChecker.Type.OBJECT, location());
    }
  }

  protected void checkLink()
  {
    XMLElement e = currentElement();
    URL href = checkURL(e.getAttribute("href"));
    String rel = e.getAttribute("rel");
    if (xrefChecker.isPresent() && href != null && rel != null
        && rel.toLowerCase(Locale.ROOT).contains("stylesheet"))
    {
      xrefChecker.get().registerReference(href, XRefChecker.Type.STYLESHEET, location());
    }
  }

  // end head: if no-css stylesheet found AND no css present, report CSS_010

  protected void checkSymbol()
  {
    URL href = checkURL(currentElement().getAttributeNS("http://www.w3.org/1999/xlink", "href"));
    if (xrefChecker.isPresent() && href != null)
    {
      xrefChecker.get().registerReference(href, XRefChecker.Type.SVG_SYMBOL, location());
    }
  }

  private void checkHRef(String attrNS, String attr)
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
    else if (".".equals(href))
    {
      // selfreference, no need to check
      return;
    }

    URL url = checkURL(href);

    // If the URL was not properly parsed, return early
    if (url == null) return;
    // If the URL is an EPUB CFI, return (not implemented)
    if (url.fragment() != null && url.fragment().matches("epubcfi\\(.*\\)"))
    {
      return; // temp until cfi implemented
    }

    if ("file".equals(url.scheme()))
    {
      // FIXME next disallow file URLs
      report.message(MessageId.HTM_053, location(), url);
    }
    if (context.isRemote(url))
    {
      report.info(path, FeatureEnum.REFERENCE, href);
      if (!URISchemes.contains(url.scheme()))
      {
        report.message(MessageId.HTM_025, location().context(href));
      }
      return;
    }

    processHyperlink(url);
  }

  protected URL checkSVGFontFaceURI()
  {
    URL href = checkURL(currentElement().getAttributeNS("http://www.w3.org/1999/xlink", "href"));
    if (xrefChecker.isPresent() && href != null)
    {
      xrefChecker.get().registerReference(href, XRefChecker.Type.FONT, location());
    }
    return href;
  }

  protected void processHyperlink(URL href)
  {
    if (xrefChecker.isPresent())
    {
      xrefChecker.get().registerReference(href, XRefChecker.Type.HYPERLINK, location());
    }
  }

  @Override
  public void startElement()
  {
    openElements++;
    XMLElement e = currentElement();
    elementLocationStack.push(location());

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
          checkSymbol();
        }
        else if (name.equals("image"))
        {
          checkImage("http://www.w3.org/1999/xlink", "href");
        }
        else if (name.equals("font-face-uri"))
        {
          checkSVGFontFaceURI();
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
          checkObject();
        }
        else if (name.equals("link"))
        {
          checkLink();
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
          new CSSChecker(context, style, location().getLine(), true).check();
        }
      }
    }
    if (xrefChecker.isPresent() && id != null)
    {
      xrefChecker.get().registerID(id, resourceType, location());
    }
  }

  protected void checkBoldItalics()
  {
    report.message(MessageId.HTM_038, location().context(currentElement().getName()));
  }

  protected void checkIFrame()
  {
    report.message(MessageId.HTM_036, location().context(currentElement().getName()));
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
          report.message(MessageId.ACC_007, EPUBLocation.of(context));
        }
      }
      else
      {
        epubTypeInUse = false;
      }
    }

    EPUBLocation currentLocation = elementLocationStack.pop();

    if (EpubConstants.HtmlNamespaceUri.equals(ns))
    {

      if ("style".equals(name))
      {
        String style = textNode.toString();
        if (style.length() > 0)
        {
          new CSSChecker(context, style, currentLocation.getLine(), false).check();
        }
        textNode = null;
      }
      else if ("table".equals(name))
      {
        if (tableDepth > 0)
        {
          --tableDepth;
          EPUBLocation location = currentLocation.context("table");

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

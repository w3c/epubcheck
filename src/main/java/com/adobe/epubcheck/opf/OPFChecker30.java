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

package com.adobe.epubcheck.opf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.overlay.OverlayCheckerFactory;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.xml.XMLValidator;
import com.google.common.io.Files;

public class OPFChecker30 extends OPFChecker implements DocumentValidator
{

  public OPFChecker30(OCFPackage ocf, Report report, String path,
      EPUBVersion version)
  {
    super(ocf, report, path, version);
  }

  public OPFChecker30(String path, GenericResourceProvider resourceProvider,
      Report report)
  {
    super(path, resourceProvider, report, EPUBVersion.VERSION_3);
  }


  @Override
  protected void initContentCheckerFactoryMap()
  {
    HashMap<String, ContentCheckerFactory> map = new HashMap<String, ContentCheckerFactory>();
    map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
    map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
    map.put("image/jpeg", BitmapCheckerFactory.getInstance());
    map.put("image/gif", BitmapCheckerFactory.getInstance());
    map.put("image/png", BitmapCheckerFactory.getInstance());
    map.put("image/svg+xml", OPSCheckerFactory.getInstance());
    map.put("text/css", CSSCheckerFactory.getInstance());
    map.put("application/smil+xml", OverlayCheckerFactory.getInstance());
    contentCheckerFactoryMap.clear();
    contentCheckerFactoryMap.putAll(map);
  }
  
  @Override
  protected void initValidators()
  {
    opfValidators.clear();
    opfValidators.add(new XMLValidator("schema/30/package-30.rnc"));
    opfValidators.add(new XMLValidator("schema/30/package-30.sch"));
    opfValidators.add(new XMLValidator("schema/30/collection-do-30.sch"));
    opfValidators.add(new XMLValidator("schema/30/collection-manifest-30.sch"));
    if (opfData != null && opfData.getTypes().contains(OPFData.DC_TYPE_EDUPUB))
    {
      opfValidators.add(new XMLValidator("schema/30/edupub/edu-opf.sch"));
    }
  }

  @Override
  public void initHandler()
  {
    opfHandler = new OPFHandler30(path, report, xrefChecker, opfParser, version);
  }

  @Override
  protected void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();
    String fallback = item.getFallback();
    if (mimeType == null || mimeType.equals(""))
    {
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "empty media-type attribute");
      return;
    }

    if (!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+"))
    {
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "invalid content for media-type attribute");
      return;
    }
    
    if ("application/xhtml+xml".equals(mimeType) && !"xhtml".equals(Files.getFileExtension(item.getPath())))
    {
      report.message(MessageId.HTM_014a,
          new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()), item.getPath());
    }
    
    if (fallback != null)
    {
      OPFItem fallbackItem = opfHandler.getItemById(fallback);
      if (fallbackItem == null)
      {
        report.message(MessageId.OPF_040,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()));
      }
    }
  }

  @Override
  protected void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {

    String mimeType = item.getMimeType();
    if (mimeType == null)
    {
      return;
    }

    if (isBlessedItemType(mimeType, version))
    {
      return;
    }

    if (item.getFallback() == null)
    {
      report.message(MessageId.OPF_043,
          new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
          mimeType);
    }

    else if (!new FallbackChecker().checkItemFallbacks(item, opfHandler, false))
    {
      report.message(MessageId.OPF_044,
          new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
          mimeType);
    }
  }

  @Override
  protected void checkBindings()
  {
    Set<String> mimeTypes = xrefChecker.getBindingsMimeTypes();
    Iterator<String> it = mimeTypes.iterator();
    String mimeType;
    while (it.hasNext())
    {
      mimeType = it.next();
      String handlerSrc = xrefChecker.getBindingHandlerSrc(mimeType);
      OPFItem handler = opfHandler.getItemByPath(handlerSrc);
      if (!handler.isScripted())
      {
        report.message(MessageId.OPF_046,
            new MessageLocation(handlerSrc, handler.lineNumber, handler.columnNumber));
      }
    }
  }


//	protected boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler) {
//		String fallback = item.getFallback();
//		if (fallback != null) {
//			OPFItem fallbackItem = opfHandler.getItemById(fallback);
//			if (fallbackItem != null) {
//				String mimeType = fallbackItem.getMimeType();
//				if (mimeType != null) {
//					if (OPFChecker.isBlessedItemType(mimeType, version))
//						return true;
//					if (checkItemFallbacks(fallbackItem, opfHandler))
//						return true;
//				}
//			}
//		}
//		return false;
//	}

  public static boolean isBlessedAudioType(String type)
  {
    return type.equals("audio/mpeg") || type.equals("audio/mp4") || type.equals("audio/ogg");
  }

  public static boolean isBlessedVideoType(String type)
  {
    return type.startsWith("video/h264") || type.startsWith("video/webm") || type.startsWith("video/mp4");
  }

  public static boolean isBlessedFontType(String type)
  {
    return type.equals("application/vnd.ms-opentype")
        || type.equals("application/font-woff")
        || type.equals("image/svg+xml");
  }

  public static boolean isCoreMediaType(String type)
  {
    return isBlessedAudioType(type)
        || isBlessedVideoType(type)
        || isBlessedFontType(type)
        || isBlessedItemType(type, EPUBVersion.VERSION_3)
        || isBlessedImageType(type)
        || type.equals("text/javascript")
        || type.equals("application/pls+xml")
        || type.equals("application/smil+xml")
        || type.equals("image/svg+xml");
  }
}

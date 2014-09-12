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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.nav.NavCheckerFactory;
import com.adobe.epubcheck.ncx.NCXCheckerFactory;
import com.adobe.epubcheck.ocf.OCFFilenameChecker;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;

public class OPFChecker implements DocumentValidator
{
  final OCFPackage ocf;
  final OPFData opfData;
  final Report report;
  final String path;
  final XRefChecker xrefChecker;
  OPFHandler opfHandler = null;
  XMLParser opfParser = null;
  final List<XMLValidator> opfValidators = new LinkedList<XMLValidator>();
  
  final Hashtable<String, ContentCheckerFactory> contentCheckerFactoryMap = new Hashtable<String, ContentCheckerFactory>();
  final EPUBVersion version;
  final GenericResourceProvider resourceProvider;

  protected void initContentCheckerFactoryMap()
  {
    Hashtable<String, ContentCheckerFactory> map = new Hashtable<String, ContentCheckerFactory>();
    map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
    map.put("text/html", OPSCheckerFactory.getInstance());
    map.put("text/x-oeb1-document", OPSCheckerFactory.getInstance());
    map.put("image/jpeg", BitmapCheckerFactory.getInstance());
    map.put("image/gif", BitmapCheckerFactory.getInstance());
    map.put("image/png", BitmapCheckerFactory.getInstance());
    map.put("image/svg+xml", OPSCheckerFactory.getInstance());
    map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
    map.put("text/css", CSSCheckerFactory.getInstance());

    contentCheckerFactoryMap.putAll(map);
  }
  
  protected void initValidators()
  {
    opfValidators.add(XMLValidators.OPF_20_RNG.get());
    opfValidators.add(XMLValidators.OPF_20_SCH.get());
  }

  public OPFChecker(OCFPackage ocf, Report report, String path, EPUBVersion version)
  {
    this.ocf = ocf;
    this.resourceProvider = ocf;
    this.report = report;
    this.path = path;
    this.xrefChecker = new XRefChecker(ocf, report, version);
    this.version = version;
    this.opfData = ocf.getOpfData().get(path);
    initValidators();
    initContentCheckerFactoryMap();
  }

  public OPFChecker(String path, GenericResourceProvider resourceProvider, Report report)
  {
    this(path, resourceProvider, report, EPUBVersion.VERSION_2);
  }
  
  protected OPFChecker(String path, GenericResourceProvider resourceProvider, Report report, EPUBVersion version)
  {

    this.ocf = null; //unused in this mode
    this.xrefChecker = null; //unused in this mode
    this.opfData = null; //unused in this mode
    this.resourceProvider = resourceProvider;
    this.report = report;
    this.path = path;
    this.version = version;
    initValidators();
    initContentCheckerFactoryMap();
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.PKG_020, new MessageLocation(this.ocf.getName(), 0, 0), path);
      return;
    }
    validate();

    if (!opfHandler.checkUniqueIdentExists())
    {
      report.message(MessageId.OPF_030, new MessageLocation(path, -1, -1), opfHandler.getIdentId());
		} else {
			ocf.setUniqueIdentifier(opfHandler.getUid());
    }

    int itemCount = opfHandler.getItemCount();
    report.info(null, FeatureEnum.ITEMS_COUNT, Integer.toString(itemCount));
    for (int i = 0; i < itemCount; i++)
    {
      OPFItem item = opfHandler.getItem(i);
      try
      {
        xrefChecker.registerResource(item.getPath(),
            item.getMimeType(), item.isInSpine(),
            new FallbackChecker().checkItemFallbacks(item, opfHandler, true),
            new FallbackChecker().checkImageFallbacks(item, opfHandler));
      }
      catch (IllegalArgumentException e)
      {
        report.message(MessageId.RSC_005,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
            e.getMessage());
      }

      report.info(item.getPath(), FeatureEnum.DECLARED_MIMETYPE, item.getMimeType());
    }

    checkGuide();
    checkBindings();

    for (int i = 0; i < itemCount; i++)
    {
      OPFItem item = opfHandler.getItem(i);

			if (!item.path.matches("^[^:/?#]+://.*"))
      {
        checkItemContent(item);
      }
    }

    xrefChecker.checkReferences();
  }

  void checkBindings()
  {

  }

  void checkGuide()
  {
    int refCount = opfHandler.getReferenceCount();
    for (int i = 0; i < refCount; i++)
    {
      OPFReference ref = opfHandler.getReference(i);
      String itemPath = PathUtil.removeAnchor(ref.getHref());
      OPFItem item = opfHandler.getItemByPath(itemPath);
      if (item == null)
      {
        report.message(MessageId.OPF_031,
            new MessageLocation(path, ref.getLineNumber(), ref.getColumnNumber()),
            ref.getHref());
      }
      else
      {
        if (!isBlessedItemType(item.mimeType, version) &&
            !isDeprecatedBlessedItemType(item.mimeType))
        {
          report.message(MessageId.OPF_032,
              new MessageLocation(path, ref.getLineNumber(), ref.getColumnNumber()),
              ref.getHref());
        }
      }
    }
  }

  void initHandler()
  {
  		opfHandler = new OPFHandler(path, report, xrefChecker, opfParser, version);
  }

  public OPFHandler getOPFHandler()
  {
    return opfHandler;
  }

  @Override
  public boolean validate()
  {
    int fatalErrorsSoFar = report.getFatalErrorCount();
    int errorsSoFar = report.getErrorCount();
    int warningsSoFar = report.getWarningCount();

    InputStream in = null;
    try
    {
      in = resourceProvider.getInputStream(path);
      opfParser = new XMLParser(ocf, new BufferedInputStream(
          in), path, "opf",
          report, version);
      initHandler();
      opfParser.addXMLHandler(opfHandler);
      for (XMLValidator validator : opfValidators)
      {
        opfParser.addValidator(validator);
      }
      opfParser.process();
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_008, new MessageLocation(path, 0, 0), path);
    }
    finally
    {
      try
      {
        if (in != null)
        {
          in.close();
        }
      }
      catch (Exception ignored)
      {
        // eat the error
      }
    }


    int itemCount = opfHandler.getItemCount();
    for (int i = 0; i < itemCount; i++)
    {
      OPFItem item = opfHandler.getItem(i);
			
			// only check Filename CompatiblyEscaped when in "-mode opf"
			// this is when 'xrefChecker' Object is null which is an indicator for single file validation
			// (Had no better possibility in mind since "mode" isn't available in OPFChecker.java)
			//
			// bugfix for issue 239
			if(xrefChecker == null) {
      OCFFilenameChecker.checkCompatiblyEscaped(item.getPath(), report, version);
			}
      checkItem(item, opfHandler);
    }

    int spineItemCount = opfHandler.getSpineItemCount();
    int nonLinearCount = 0;
    for (int i = 0; i < spineItemCount; i++)
    {
      OPFItem item = opfHandler.getSpineItem(i);
      checkSpineItem(item, opfHandler);
      if (!item.getSpineLinear())
      {
        nonLinearCount++;
      }
      report.info(item.getPath(), FeatureEnum.SPINE_INDEX, Integer.toString(i));
    }
    if (nonLinearCount == spineItemCount && spineItemCount > 0)
    {
      //test > 0 to not trigger this when opf is malformed etc
      report.message(MessageId.OPF_033, new MessageLocation(path, -1, -1));
    }

    if (version == EPUBVersion.VERSION_2)
    {
      // check for >1 itemrefs to any given spine item
      // http://code.google.com/p/epubcheck/issues/detail?id=182
      List<OPFItem> seen = new ArrayList<OPFItem>();
      for (int i = 0; i < opfHandler.getSpineItemCount(); i++)
      {
        OPFItem item = opfHandler.getSpineItem(i);
        if (seen.contains(item))
        {
          report.message(MessageId.OPF_034,
              new MessageLocation(path, item.getLineNumber(), item.getLineNumber()),
              item.getId());
        }
        else
        {
          seen.add(item);
        }
      }
    }

    return fatalErrorsSoFar == report.getFatalErrorCount()
        && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }

  public static boolean isBlessedItemType(String type, EPUBVersion version)
  {
    if (version == EPUBVersion.VERSION_2)
    {
      return type.equals("application/xhtml+xml")
          || type.equals("application/x-dtbook+xml");
    }
    else
    {
      return type.equals("application/xhtml+xml")
          || type.equals("image/svg+xml");
    }
  }

  public static boolean isDeprecatedBlessedItemType(String type)
  {
    return type.equals("text/x-oeb1-document") || type.equals("text/html");
  }

  protected static boolean isBlessedStyleType(String type)
  {
    return type.equals("text/css");
  }

  protected static boolean isDeprecatedBlessedStyleType(String type)
  {
    return type.equals("text/x-oeb1-css");
  }

  public static boolean isBlessedImageType(String type)
  {
    return type.equals("image/gif") || type.equals("image/png")
        || type.equals("image/jpeg") || type.equals("image/svg+xml");
  }

  public static boolean isBlessedFontMimetype20(String mime)
  {
    return mime != null && (mime.startsWith("font/") || mime.startsWith("application/font") || mime.startsWith("application/x-font") || "application/vnd.ms-opentype".equals(mime));
  }

  void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();
    String fallback = item.getFallback();
    if (mimeType == null || mimeType.equals(""))
    {
      // Ensures that media-type attribute is not empty
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "empty media-type attribute");
    }
    else if (!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+"))
    {
      /*
          * Ensures that media-type attribute has correct content. The
          * media-type must have a type and a sub-type divided by '/' The
          * allowable content for the media-type attribute is defined in
          * RFC4288 section 4.2
          */
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "invalid content for media-type attribute");
    }
    else if (isDeprecatedBlessedItemType(mimeType)
        || isDeprecatedBlessedStyleType(mimeType))
    {
      if (opfHandler.getOpf20PackageFile()
          && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_035, new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
      else if (opfHandler.getOpf12PackageFile()
          && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_038, new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()), mimeType);
      }
      else if (opfHandler.getOpf20PackageFile())
      {
        report.message(MessageId.OPF_037,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
    }
    if (opfHandler.getOpf12PackageFile() && fallback == null)
    {
      if (isBlessedItemType(mimeType, version))
      {
        report.message(MessageId.OPF_038,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
      else if (isBlessedStyleType(mimeType))
      {
        report.message(MessageId.OPF_039,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
    }
    if (fallback != null)
    {
      OPFItem fallbackItem = opfHandler.getItemById(fallback);
      if (fallbackItem == null)
      {
        report.message(MessageId.OPF_040,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
    }
		
    String fallbackStyle = item.getFallbackStyle();
    if (fallbackStyle != null)
    {
      OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
      if (fallbackStyleItem == null)
      {
        report.message(MessageId.OPF_041,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
    }
  }

  void checkItemContent(OPFItem item)
  {
    String mimeType = item.getMimeType();
    String path = item.getPath();
    String properties = item.getProperties();

    if (mimeType != null)
    {
      ContentCheckerFactory checkerFactory;
      if (item.isNcx())
      {
        checkerFactory = NCXCheckerFactory.getInstance();
      }
      else if (item.isNav())
      {
        checkerFactory = NavCheckerFactory.getInstance();
      }
      else
      {
        checkerFactory = contentCheckerFactoryMap.get(mimeType);
      }

      if (checkerFactory == null)
      {
        checkerFactory = GenericContentCheckerFactory.getInstance();
      }
      if (checkerFactory != null)
      {
        ContentChecker checker = checkerFactory.newInstance(ocf,
            report, path, mimeType, properties, xrefChecker,
            version, opfData.getTypes());
        checker.runChecks();
      }
    }
  }

  void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {
    // These checks are okay to be done on <spine> items, but they really
    // should be done on all
    // <manifest> items instead. I am avoiding making this change now
    // pending a few issue
    // resolutions in the EPUB Maint Working Group (e.g. embedded fonts not
    // needing fallbacks).
    // [GC 11/15/09]
    String mimeType = item.getMimeType();
    if (mimeType != null)
    {
      if (isBlessedStyleType(mimeType)
          || isDeprecatedBlessedStyleType(mimeType)
          || isBlessedImageType(mimeType))
      {
        report.message(MessageId.OPF_042,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
            mimeType);
      }
      else if (!isBlessedItemType(mimeType, version)
          && !isDeprecatedBlessedItemType(mimeType)
          && item.getFallback() == null)
      {
        report.message(MessageId.OPF_043,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
            mimeType);
      }
      else if (!isBlessedItemType(mimeType, version)
          && !isDeprecatedBlessedItemType(mimeType)
          && !new FallbackChecker().checkItemFallbacks(item, opfHandler, true))
      {
        report.message(MessageId.OPF_044,
            new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()),
            mimeType);
      }
    }
  }

  class FallbackChecker
  {
    private final Set<String> checked;

    public FallbackChecker()
    {
      checked = new HashSet<String>();
    }

    boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler, boolean checkFallbackStyle)
    {
      String fallback = item.getFallback();
      if (fallback != null)
      {
        fallback = fallback.trim();
        if (checked.contains(fallback))
        {
          report.message(MessageId.OPF_045,
              new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()));
          return false;
        }
        else
        {
          checked.add(fallback);
        }

        OPFItem fallbackItem = opfHandler.getItemById(fallback);
        if (fallbackItem != null)
        {
          String mimeType = fallbackItem.getMimeType();
          if (mimeType != null)
          {
            if (isBlessedItemType(mimeType, version)
                || isDeprecatedBlessedItemType(mimeType))
            {
              return true;
            }
            if (checkItemFallbacks(fallbackItem, opfHandler, checkFallbackStyle))
            {
              return true;
            }

          }
        }
      }
      if (!checkFallbackStyle)
      {
        return false;
      }

      String fallbackStyle = item.getFallbackStyle();
      if (fallbackStyle != null)
      {
        OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
        if (fallbackStyleItem != null)
        {
          String mimeType = fallbackStyleItem.getMimeType();
          return mimeType != null && (isBlessedStyleType(mimeType)
              || isDeprecatedBlessedStyleType(mimeType));
        }
      }
      return false;
    }

    boolean checkImageFallbacks(OPFItem item, OPFHandler opfHandler)
    {
      String fallback = item.getFallback();
      if (fallback != null)
      {
        fallback = fallback.trim();
        if (checked.contains(fallback))
        {
          report.message(MessageId.OPF_045,
              new MessageLocation(path, item.getLineNumber(), item.getColumnNumber()));
          return false;
        }
        else
        {
          checked.add(fallback);
        }
        OPFItem fallbackItem = opfHandler.getItemById(fallback);
        if (fallbackItem != null)
        {
          String mimeType = fallbackItem.getMimeType();
          if (mimeType != null)
          {
            if (isBlessedImageType(mimeType))
            {
              return true;
            }
            if (checkImageFallbacks(fallbackItem, opfHandler))
            {
              return true;
            }
          }
        }
      }
      return false;
    }
  }
}

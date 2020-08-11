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

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.*;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.nav.NavCheckerFactory;
import com.adobe.epubcheck.ncx.NCXCheckerFactory;
import com.adobe.epubcheck.ocf.OCFFilenameChecker;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.overlay.OverlayTextChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

public class OPFChecker implements DocumentValidator, ContentChecker
{

  private final static ValidatorMap validatorMap = ValidatorMap.builder()
      .put(version(EPUBVersion.VERSION_2), XMLValidators.OPF_20_RNG)
      .put(version(EPUBVersion.VERSION_2), XMLValidators.OPF_20_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_RNC)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_DO_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_DICT_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_IDX_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_MANIFEST_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_PREVIEW_SCH)
      .put(Predicates.or(profile(EPUBProfile.DICT), hasPubType(OPFData.DC_TYPE_DICT)),
          XMLValidators.OPF_DICT_SCH)
      .put(Predicates.or(profile(EPUBProfile.EDUPUB), hasPubType(OPFData.DC_TYPE_EDUPUB)),
          XMLValidators.OPF_EDUPUB_SCH)
      .put(Predicates.or(profile(EPUBProfile.PREVIEW), hasPubType(OPFData.DC_TYPE_PREVIEW)),
          XMLValidators.OPF_PREVIEW_SCH)
      .build();

  protected final ValidationContext context;
  protected final Report report;
  protected final String path;
  protected final EPUBVersion version;
  protected OPFHandler opfHandler = null;
  protected XMLParser opfParser = null;
  protected final Hashtable<String, ContentCheckerFactory> contentCheckerFactoryMap = new Hashtable<String, ContentCheckerFactory>();

  protected void initContentCheckerFactoryMap()
  {
    Hashtable<String, ContentCheckerFactory> map = new Hashtable<String, ContentCheckerFactory>();
    map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
    map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
    map.put("image/jpeg", BitmapCheckerFactory.getInstance());
    map.put("image/gif", BitmapCheckerFactory.getInstance());
    map.put("image/png", BitmapCheckerFactory.getInstance());
    map.put("image/svg+xml", OPSCheckerFactory.getInstance());
    map.put("text/css", CSSCheckerFactory.getInstance());
    map.put("text/html", OPSCheckerFactory.getInstance());
    map.put("text/x-oeb1-document", OPSCheckerFactory.getInstance());

    contentCheckerFactoryMap.putAll(map);
  }

  public OPFChecker(ValidationContext context)
  {
    // The following context fields are not overridden from the parent context
    this.report = context.report;
    this.path = context.path;
    this.version = context.version;

    // Create a new validation context from the parent
    ValidationContextBuilder newContext = new ValidationContextBuilder(context);
    if (context.ocf.isPresent())
    {
      // Get the OPFData peeked from the OCF
      OPFData opfData = context.ocf.get().getOpfData().get(context.path);
      newContext.pubTypes(opfData != null ? opfData.getTypes() : null);
      newContext.xrefChecker(new XRefChecker(context.ocf.get(), context.report, context.version));
      newContext.profile(EPUBProfile.makeOPFCompatible(context.profile, opfData, path, report));
      newContext.overlayTextChecker(new OverlayTextChecker());
    }
    this.context = newContext.build();

    // Initialize validators and factories
    initContentCheckerFactoryMap();
  }

  public void runChecks()
  {
    OCFPackage ocf = context.ocf.get();
    XRefChecker xrefChecker = context.xrefChecker.get();
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.PKG_020, EPUBLocation.create(ocf.getName()), path);
      return;
    }
    validate();

    if (!opfHandler.checkUniqueIdentExists())
    {
      report.message(MessageId.OPF_030, EPUBLocation.create(path), opfHandler.getIdentId());
    }

    List<OPFItem> items = opfHandler.getItems();
    report.info(null, FeatureEnum.ITEMS_COUNT, Integer.toString(items.size()));
    for (OPFItem item : items)
    {
      xrefChecker.registerResource(item,
          new FallbackChecker().checkItemFallbacks(item, opfHandler, true),
          new FallbackChecker().checkImageFallbacks(item, opfHandler));

      report.info(item.getPath(), FeatureEnum.DECLARED_MIMETYPE, item.getMimeType());
    }

    checkGuide();
    checkBindings();

    // Check items content (publication resources)
    for (OPFItem item : items)
    {
      if (!item.getPath().matches("^[^:/?#]+://.*"))
      {
        checkItemContent(item);
      }
    }
    
    // Checks items after the content-validation pass
    // This allows to run checks depending on info collected in publication resources
    for (OPFItem item : items) {
      checkItemAfterResourceValidation(item);
    }

    xrefChecker.checkReferences();
  }
  
  protected void checkItemAfterResourceValidation(OPFItem item) {
  }

  protected void checkBindings()
  {
  }

  protected void checkGuide()
  {
    int refCount = opfHandler.getReferenceCount();
    for (int i = 0; i < refCount; i++)
    {
      OPFReference ref = opfHandler.getReference(i);
      String itemPath = PathUtil.removeFragment(ref.getHref());
      Optional<OPFItem> item = opfHandler.getItemByPath(itemPath);
      if (!item.isPresent())
      {
        report.message(MessageId.OPF_031,
            EPUBLocation.create(path, ref.getLineNumber(), ref.getColumnNumber()), ref.getHref());
      }
      else
      {
        if (!isBlessedItemType(item.get().getMimeType(), version)
            && !isDeprecatedBlessedItemType(item.get().getMimeType()))
        {
          report.message(MessageId.OPF_032,
              EPUBLocation.create(path, ref.getLineNumber(), ref.getColumnNumber()), ref.getHref());
        }
      }
    }
  }

  protected void initHandler()
  {
    opfHandler = new OPFHandler(context, opfParser);
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

    opfParser = new XMLParser(new ValidationContextBuilder(context).mimetype("opf").build());
    initHandler();
    opfParser.addXMLHandler(opfHandler);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      opfParser.addValidator(validator);
    }
    opfParser.process();

    for (OPFItem item : opfHandler.getItems())
    {
      // only check Filename CompatiblyEscaped when in "-mode opf"
      // this is when 'xrefChecker' Object is null which is an indicator for
      // single file validation
      // (Had no better possibility in mind since "mode" isn't available in
      // OPFChecker.java)
      //
      // bugfix for issue 239
      if (!context.xrefChecker.isPresent())
      {
        OCFFilenameChecker.checkCompatiblyEscaped(item.getPath(), report, version);
      }
      if (!item.equals(opfHandler.getItemByPath(item.getPath()).orNull()))
      {
        report.message(MessageId.OPF_074,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()),
            item.getPath());
      }
      else
      {
        checkItem(item, opfHandler);
      }
    }

    if (!opfHandler.getSpineItems().isEmpty())
    {
      boolean linearFound = false;
      int spineIndex = 0;
      for (OPFItem item : opfHandler.getSpineItems())
      {
        checkSpineItem(item, opfHandler);
        if (item.isLinear())
        {
          linearFound = true;
        }
        report.info(item.getPath(), FeatureEnum.SPINE_INDEX, Integer.toString(spineIndex++));
      }
      if (!linearFound)
      {
        report.message(MessageId.OPF_033, EPUBLocation.create(path));
      }
    }

    if (version == EPUBVersion.VERSION_2)
    {
      // check for >1 itemrefs to any given spine item
      // http://code.google.com/p/epubcheck/issues/detail?id=182
      Set<OPFItem> seen = new HashSet<OPFItem>();
      for (OPFItem item : opfHandler.getSpineItems())
      {
        if (seen.contains(item))
        {
          report.message(MessageId.OPF_034,
              EPUBLocation.create(path, item.getLineNumber(), item.getLineNumber()), item.getId());
        }
        else
        {
          seen.add(item);
        }
      }
    }

    return fatalErrorsSoFar == report.getFatalErrorCount() && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }

  public static boolean isBlessedItemType(String type, EPUBVersion version)
  {
    if (version == EPUBVersion.VERSION_2)
    {
      return type.equals("application/xhtml+xml") || type.equals("application/x-dtbook+xml");
    }
    else
    {
      return type.equals("application/xhtml+xml") || type.equals("image/svg+xml");
    }
  }

  public static boolean isDeprecatedBlessedItemType(String type)
  {
    return type.equals("text/x-oeb1-document") || type.equals("text/html");
  }

  public static boolean isBlessedStyleType(String type)
  {
    return type.equals("text/css");
  }

  public static boolean isDeprecatedBlessedStyleType(String type)
  {
    return type.equals("text/x-oeb1-css");
  }

  public static boolean isBlessedImageType(String type)
  {
    return type.equals("image/gif") || type.equals("image/png") || type.equals("image/jpeg")
        || type.equals("image/svg+xml");
  }

  public static boolean isBlessedFontMimetype20(String mime)
  {
    return mime != null && (mime.startsWith("font/") || mime.startsWith("application/font")
        || mime.startsWith("application/x-font") || "application/vnd.ms-opentype".equals(mime));
  }
  
  public static boolean isScriptType(String type)
  {
    type = (type == null)? null : type.toLowerCase(Locale.ENGLISH);
    return "application/javascript".equals(type)
        || "text/javascript".equals(type)
        || "application/ecmascript".equals(type)
        || "application/x-ecmascript".equals(type)
        || "application/x-javascript".equals(type)
        || "text/ecmascript".equals(type)
        || "text/javascript1.0".equals(type)
        || "text/javascript1.1".equals(type)
        || "text/javascript1.2".equals(type)
        || "text/javascript1.3".equals(type)
        || "text/javascript1.4".equals(type)
        || "text/javascript1.5".equals(type)
        || "text/jscript".equals(type)
        || "text/livescript".equals(type)
        || "text/x-ecmascript".equals(type)
        || "text/x-javascript".equals(type);
  }

  protected void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();
    Optional<String> fallback = item.getFallback();
    if (!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+"))
    {
      /*
       * Ensures that media-type attribute has correct content. The media-type
       * must have a type and a sub-type divided by '/' The allowable content
       * for the media-type attribute is defined in RFC4288 section 4.2
       */
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "invalid content for media-type attribute");
    }
    else if (isDeprecatedBlessedItemType(mimeType) || isDeprecatedBlessedStyleType(mimeType))
    {
      if (opfHandler.getOpf20PackageFile() && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_035,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
      else if (opfHandler.getOpf12PackageFile() && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_038,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
      else if (opfHandler.getOpf20PackageFile())
      {
        report.message(MessageId.OPF_037,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
    }
    if (opfHandler.getOpf12PackageFile() && !fallback.isPresent())
    {
      if (isBlessedItemType(mimeType, version))
      {
        report.message(MessageId.OPF_038,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
      else if (isBlessedStyleType(mimeType))
      {
        report.message(MessageId.OPF_039,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()),
            mimeType);
      }
    }
    if (fallback.isPresent())
    {
      Optional<OPFItem> fallbackItem = opfHandler.getItemById(fallback.get());
      if (!fallbackItem.isPresent())
      {
        report.message(MessageId.OPF_040,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
    }

    if (item.getFallbackStyle().isPresent())
    {
      Optional<OPFItem> fallbackStyleItem = opfHandler.getItemById(item.getFallbackStyle().get());
      if (!fallbackStyleItem.isPresent())
      {
        report.message(MessageId.OPF_041,
            EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber(), item.getId()));
      }
    }
  }

  protected void checkItemContent(OPFItem item)
  {
    String mimetype = item.getMimeType();
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
      checkerFactory = contentCheckerFactoryMap.get(mimetype);
    }

    if (checkerFactory == null)
    {
      checkerFactory = GenericContentCheckerFactory.getInstance();
    }
    if (checkerFactory != null)
    {
      try {
        // Create the content checker with an overridden validation context
        ContentChecker checker = checkerFactory.newInstance(new ValidationContextBuilder(context)
            .path(item.getPath()).mimetype(mimetype).properties(item.getProperties()).build());
        // Validate
        checker.runChecks();
      } catch (IllegalStateException e) {
        report.message(MessageId.CHK_008, EPUBLocation.create(path), item.getPath());
      }
    }
  }

  protected void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {
    // These checks are okay to be done on <spine> items, but they really
    // should be done on all
    // <manifest> items instead. I am avoiding making this change now
    // pending a few issue
    // resolutions in the EPUB Maint Working Group (e.g. embedded fonts not
    // needing fallbacks).
    // [GC 11/15/09]
    String mimeType = item.getMimeType();
    if (isBlessedStyleType(mimeType) || isDeprecatedBlessedStyleType(mimeType)
        || isBlessedImageType(mimeType))
    {
      report.message(MessageId.OPF_042,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), mimeType);
    }
    else if (!isBlessedItemType(mimeType, version) && !isDeprecatedBlessedItemType(mimeType)
        && !item.getFallback().isPresent())
    {
      report.message(MessageId.OPF_043,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), mimeType);
    }
    else if (!isBlessedItemType(mimeType, version) && !isDeprecatedBlessedItemType(mimeType)
        && !new FallbackChecker().checkItemFallbacks(item, opfHandler, true))
    {
      report.message(MessageId.OPF_044,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), mimeType);
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
      if (item.getFallback().isPresent())
      {
        String fallback = item.getFallback().get();
        if (checked.contains(fallback))
        {
          report.message(MessageId.OPF_045,
              EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()));
          return false;
        }
        else
        {
          checked.add(fallback);
        }

        Optional<OPFItem> fallbackItem = opfHandler.getItemById(fallback);
        if (fallbackItem.isPresent())
        {
          String mimeType = fallbackItem.get().getMimeType();
          if (isBlessedItemType(mimeType, version) || isDeprecatedBlessedItemType(mimeType))
          {
            return true;
          }
          if (checkItemFallbacks(fallbackItem.get(), opfHandler, checkFallbackStyle))
          {
            return true;
          }

        }
      }
      if (!checkFallbackStyle)
      {
        return false;
      }
      if (item.getFallbackStyle().isPresent())
      {
        String fallbackStyle = item.getFallbackStyle().get();
        Optional<OPFItem> fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
        if (fallbackStyleItem.isPresent())
        {
          String mimeType = fallbackStyleItem.get().getMimeType();
          return (isBlessedStyleType(mimeType) || isDeprecatedBlessedStyleType(mimeType));
        }
      }
      return false;
    }

    boolean checkImageFallbacks(OPFItem item, OPFHandler opfHandler)
    {
      if (item.getFallback().isPresent())
      {
        String fallback = item.getFallback().get();
        if (checked.contains(fallback))
        {
          report.message(MessageId.OPF_045,
              EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()));
          return false;
        }
        else
        {
          checked.add(fallback);
        }
        Optional<OPFItem> fallbackItem = opfHandler.getItemById(fallback);
        if (fallbackItem.isPresent())
        {
          String mimeType = fallbackItem.get().getMimeType();
          if (isBlessedImageType(mimeType))
          {
            return true;
          }
          if (checkImageFallbacks(fallbackItem.get(), opfHandler))
          {
            return true;
          }
        }
      }
      return false;
    }
  }
}

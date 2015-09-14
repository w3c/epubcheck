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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport.Feature;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.dict.SearchKeyMapCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.MetadataSet.Metadata;
import com.adobe.epubcheck.opf.ResourceCollection.Roles;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.overlay.OverlayCheckerFactory;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.DCMESVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

public class OPFChecker30 extends OPFChecker implements DocumentValidator
{

  public OPFChecker30(ValidationContext context)
  {
    super(context);
  }

  @Override
  protected void initContentCheckerFactoryMap()
  {
    HashMap<String, ContentCheckerFactory> map = new HashMap<String, ContentCheckerFactory>();
    map.put("application/vnd.epub.search-key-map+xml", SearchKeyMapCheckerFactory.getInstance());
    map.put("application/smil+xml", OverlayCheckerFactory.getInstance());
    map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
    map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
    map.put("image/jpeg", BitmapCheckerFactory.getInstance());
    map.put("image/gif", BitmapCheckerFactory.getInstance());
    map.put("image/png", BitmapCheckerFactory.getInstance());
    map.put("image/svg+xml", OPSCheckerFactory.getInstance());
    map.put("text/css", CSSCheckerFactory.getInstance());
    contentCheckerFactoryMap.clear();
    contentCheckerFactoryMap.putAll(map);
  }

  @Override
  public void initHandler()
  {
    opfHandler = new OPFHandler30(context, opfParser);
  }

  @Override
  public void runChecks()
  {
    super.runChecks();
    checkCollectionsContent();
    checkPagination();
    checkSemantics();
    checkNav();
    checkSpecifics();
  }

  @Override
  public boolean validate()
  {
    int fatalErrorsSoFar = report.getFatalErrorCount();
    int errorsSoFar = report.getErrorCount();
    int warningsSoFar = report.getWarningCount();

    super.validate();
    checkLinkedResources();
    checkCollections();

    return fatalErrorsSoFar == report.getFatalErrorCount() && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }

  @Override
  protected void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();
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

    if ("application/xhtml+xml".equals(mimeType)
        && !"xhtml".equals(Files.getFileExtension(item.getPath())))
    {
      report.message(MessageId.HTM_014a,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), item.getPath());
    }

    // Note: item fallback existence is checked in schematron, i.e.:
    // opfHandler.getItemById(item.getFallback().get()).isPresent() == true

  }

  @Override
  protected void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();

    if (item.getProperties()
        .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
    {
      report.message(MessageId.OPF_077,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()));
    }

    if (isBlessedItemType(mimeType, version))
    {
      return;
    }

    if (!item.getFallback().isPresent())
    {
      report.message(MessageId.OPF_043,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), mimeType);
    }

    else if (!new FallbackChecker().checkItemFallbacks(item, opfHandler, false))
    {
      report.message(MessageId.OPF_044,
          EPUBLocation.create(path, item.getLineNumber(), item.getColumnNumber()), mimeType);
    }
  }

  @Override
  protected void checkBindings()
  {
    Set<String> mimeTypes = context.xrefChecker.get().getBindingsMimeTypes();
    Iterator<String> it = mimeTypes.iterator();
    String mimeType;
    while (it.hasNext())
    {
      mimeType = it.next();
      String handlerId = context.xrefChecker.get().getBindingHandlerId(mimeType);
      OPFItem handler = opfHandler.getItemById(handlerId).get();
      if (!handler.isScripted())
      {
        report.message(MessageId.OPF_046, EPUBLocation.create(handler.getPath(),
            handler.getLineNumber(), handler.getColumnNumber()));
      }
    }
  }

  // protected boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler) {
  // String fallback = item.getFallback();
  // if (fallback != null) {
  // OPFItem fallbackItem = opfHandler.getItemById(fallback);
  // if (fallbackItem != null) {
  // String mimeType = fallbackItem.getMimeType();
  // if (mimeType != null) {
  // if (OPFChecker.isBlessedItemType(mimeType, version))
  // return true;
  // if (checkItemFallbacks(fallbackItem, opfHandler))
  // return true;
  // }
  // }
  // }
  // return false;
  // }

  private void checkCollections()
  {
    for (ResourceCollection collection : ((OPFHandler30) opfHandler).getCollections().asList())
    {
      if (collection.hasRole(ResourceCollection.Roles.DICTIONARY))
      {
        checkDictCollection(collection);
      }
      if (collection.hasRole(ResourceCollection.Roles.INDEX))
      {
        checkIndexCollection(collection);
      }
      if (collection.hasRole(ResourceCollection.Roles.PREVIEW))
      {
        checkPreviewCollection(collection);
      }
    }

  }

  private void checkCollectionsContent()
  {
    for (ResourceCollection collection : ((OPFHandler30) opfHandler).getCollections().asList())
    {
      if (collection.hasRole(ResourceCollection.Roles.DICTIONARY))
      {
        checkDictCollectionContent(collection);
      }
    }

  }

  private void checkDictCollection(ResourceCollection collection)
  {
    if (collection.hasRole(Roles.DICTIONARY))
    {
      boolean skmFound = false;
      for (LinkedResource resource : collection.getResources().asList())
      {
        Optional<OPFItem> item = opfHandler.getItemByPath(resource.getPath());
        if (!item.isPresent())
        {
          report.message(MessageId.OPF_081, EPUBLocation.create(path), resource.getPath());
        }
        else if ("application/vnd.epub.search-key-map+xml".equals(item.get().getMimeType()))
        {
          if (skmFound)
          {
            // More than one Search Key Map
            report.message(MessageId.OPF_082, EPUBLocation.create(path));
          }
          skmFound = true;
        }
        else if (!"application/xhtml+xml".equals(item.get().getMimeType()))
        {
          report.message(MessageId.OPF_084, EPUBLocation.create(path), resource.getPath());
        }
      }
      if (!skmFound)
      {
        // No Search Key Map
        report.message(MessageId.OPF_083, EPUBLocation.create(path));
      }
    }
  }

  private void checkDictCollectionContent(ResourceCollection collection)
  {
    if (collection.hasRole(Roles.DICTIONARY))
    {
      boolean dictFound = false;
      for (LinkedResource resource : collection.getResources().asList())
      {
        final Optional<OPFItem> item = opfHandler.getItemByPath(resource.getPath());
        if (!dictFound && item.isPresent()
            && "application/xhtml+xml".equals(item.get().getMimeType()))
        {
          // Search if this resource was reported as DICTIONARY content
          dictFound = Iterables.tryFind(context.featureReport.getFeature(FeatureEnum.DICTIONARY),
              new Predicate<Feature>()
              {

                @Override
                public boolean apply(Feature dict)
                {
                  return item.get().getPath().equals(dict.getLocation().get().getPath());
                }
              }).isPresent();
        }
      }
      if (!dictFound)
      {
        // No Dictionary content
        report.message(MessageId.OPF_078, EPUBLocation.create(path));
      }
    }
  }

  private void checkIndexCollection(ResourceCollection collection)
  {
    if (collection.hasRole(Roles.INDEX) || collection.hasRole(Roles.INDEX_GROUP))
    {
      for (LinkedResource resource : collection.getResources().asList())
      {
        Optional<OPFItem> item = opfHandler.getItemByPath(resource.getPath());
        if (!item.isPresent() || !"application/xhtml+xml".equals(item.get().getMimeType()))
        {
          report.message(MessageId.OPF_071, EPUBLocation.create(path));
        }
      }
      for (ResourceCollection childCollection : collection.getCollections().asList())
      {
        checkIndexCollection(childCollection);
      }
    }
  }

  private void checkPreviewCollection(ResourceCollection collection)
  {

    if (collection.hasRole(Roles.PREVIEW))
    {
      for (LinkedResource resource : collection.getResources().asList())
      {
        Optional<OPFItem> item = opfHandler.getItemByPath(resource.getPath());
        if (!item.isPresent() || !("application/xhtml+xml".equals(item.get().getMimeType())
            || "image/svg+xml".equals(item.get().getMimeType())))
        {
          report.message(MessageId.OPF_075, EPUBLocation.create(path));
        }
        else
        {
          try
          {
            URI uri = new URI(resource.getURI());
            if (Optional.fromNullable(uri.getFragment()).or("").startsWith("epubcfi("))
            {
              report.message(MessageId.OPF_076, EPUBLocation.create(path));
            }
          } catch (URISyntaxException e)
          {
            report.message(MessageId.RSC_020, EPUBLocation.create(path));
          }
        }
      }
    }

  }

  private void checkLinkedResources()
  {
    LinkedResources links = ((OPFHandler30) opfHandler).getLinkedResources();
    for (LinkedResource link : links.asList())
    {
      if (opfHandler.getItemByPath(link.getPath()).isPresent())
      {
        report.message(MessageId.OPF_067, EPUBLocation.create(path), link.getPath());
      }
    }
  }

  private void checkPagination()
  {
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(OPFData.DC_TYPE_EDUPUB))
    {
      if (context.featureReport.hasFeature(FeatureEnum.PAGE_BREAK))
      {
        // Check there is a page list
        if (!context.featureReport.hasFeature(FeatureEnum.PAGE_LIST))
        {
          report.message(MessageId.NAV_003, EPUBLocation.create(path));
        }
        // Search a "dc:source" metadata expression
        Set<Metadata> dcSourceMetas = ((OPFHandler30) opfHandler).getMetadata()
            .getPrimary(DCMESVocab.VOCAB.get(DCMESVocab.PROPERTIES.SOURCE));
        if (dcSourceMetas.isEmpty())
        {
          report.message(MessageId.OPF_066, EPUBLocation.create(path));
        }
        else
        {
          // Search a "source-of : pagination" expression refining a "dc:source"
          if (!MetadataSet.tryFindInRefines(dcSourceMetas,
              PackageVocabs.META_VOCAB.get(PackageVocabs.META_PROPERTIES.SOURCE_OF),
              Optional.of("pagination")).isPresent())
          {
            report.message(MessageId.OPF_066, EPUBLocation.create(path));
          }
        }
      }
    }
  }

  private void checkSemantics()
  {
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(OPFData.DC_TYPE_EDUPUB))
    {
      if (context.featureReport.hasFeature(FeatureEnum.HAS_MICRODATA)
          && !context.featureReport.hasFeature(FeatureEnum.HAS_RDFA))
      {
        report.message(MessageId.HTM_051, context.featureReport
            .getFeature(FeatureEnum.HAS_MICRODATA).iterator().next().getLocation().get());
      }
    }
  }

  private void checkNav()
  {
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(OPFData.DC_TYPE_EDUPUB))
    {
      Set<Feature> sections = context.featureReport.getFeature(FeatureEnum.SECTIONS);
      Set<Feature> tocLinks = context.featureReport.getFeature(FeatureEnum.TOC_LINKS);
      if (sections.size() != tocLinks.size())
      {
        report.message(MessageId.NAV_004, tocLinks.isEmpty() ? EPUBLocation.create(path)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.AUDIO)
          && !context.featureReport.hasFeature(FeatureEnum.LOA))
      {
        report.message(MessageId.NAV_005, tocLinks.isEmpty() ? EPUBLocation.create(path)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.FIGURE)
          && !context.featureReport.hasFeature(FeatureEnum.LOI))
      {
        report.message(MessageId.NAV_006, tocLinks.isEmpty() ? EPUBLocation.create(path)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.TABLE)
          && !context.featureReport.hasFeature(FeatureEnum.LOT))
      {
        report.message(MessageId.NAV_007, tocLinks.isEmpty() ? EPUBLocation.create(path)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.VIDEO)
          && !context.featureReport.hasFeature(FeatureEnum.LOV))
      {
        report.message(MessageId.NAV_008, tocLinks.isEmpty() ? EPUBLocation.create(path)
            : tocLinks.iterator().next().getLocation().get());
      }
    }
  }

  private void checkSpecifics()
  {
    if (context.featureReport.hasFeature(FeatureEnum.DICTIONARY)
        && !context.pubTypes.contains(OPFData.DC_TYPE_DICT))
    {
      report.message(MessageId.OPF_079, context.featureReport.getFeature(FeatureEnum.DICTIONARY)
          .iterator().next().getLocation().get());
    }
    if (context.profile == EPUBProfile.DICT || context.pubTypes.contains(OPFData.DC_TYPE_DICT))
    {
      if (!context.featureReport.hasFeature(FeatureEnum.DICTIONARY))
      {
        report.message(MessageId.OPF_078, EPUBLocation.create(path));
      }
    }
  }

  public static boolean isBlessedAudioType(String type)
  {
    return type.equals("audio/mpeg") || type.equals("audio/mp4");
  }

  public static boolean isBlessedVideoType(String type)
  {
    return type.startsWith("video/h264") || type.startsWith("video/webm")
        || type.startsWith("video/mp4");
  }

  public static boolean isBlessedFontType(String type)
  {
    return type.equals("application/vnd.ms-opentype") || type.equals("application/font-woff")
        || type.equals("image/svg+xml");
  }

  public static boolean isCoreMediaType(String type)
  {
    return isBlessedAudioType(type) || isBlessedVideoType(type) || isBlessedFontType(type)
        || isBlessedItemType(type, EPUBVersion.VERSION_3) || isBlessedImageType(type)
        || type.equals("text/javascript") || type.equals("application/pls+xml")
        || type.equals("application/smil+xml") || type.equals("image/svg+xml");
  }
}

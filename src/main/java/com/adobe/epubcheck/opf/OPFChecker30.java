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

import java.util.Set;

import org.w3c.epubcheck.core.references.Reference;
import org.w3c.epubcheck.util.url.URLFragment;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport.Feature;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.MetadataSet.Metadata;
import com.adobe.epubcheck.opf.ResourceCollection.Roles;
import com.adobe.epubcheck.overlay.OverlayTextChecker;
import com.adobe.epubcheck.overlay.SmilClock;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.vocab.DCMESVocab;
import com.adobe.epubcheck.vocab.MediaOverlaysVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import io.mola.galimatias.URL;

public class OPFChecker30 extends OPFChecker
{

  public OPFChecker30(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void initHandler()
  {
    opfHandler = new OPFHandler30(context);
  }

  @Override
  protected boolean checkPackage()
  {
    super.checkPackage();
    checkCollectionsContent();
    checkPagination();
    checkSemantics();
    checkNav();
    checkSpecifics();
    return false;
  }

  @Override
  protected boolean checkContent()
  {
    super.checkContent();
    checkLinkedResources();
    checkCollections();
    checkMediaOverlaysDuration();
    return true;
  }

  @Override
  protected void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    // Items with `data:` URLs are not allowed in EPUB 3
    if (item.hasDataURL())
    {
      report.message(MessageId.RSC_029, item.getLocation());
      return;
    }
    if (item.getPath().startsWith("META-INF/"))
    {
      report.message(MessageId.PKG_025, item.getLocation());
    }

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

    // Check preferred media types
    String preferredMimeType = getPreferredMediaType(mimeType, item.getPath());
    if (preferredMimeType != null)
    {
      report.message(MessageId.OPF_090, item.getLocation(), preferredMimeType, mimeType);
    }
    if (!item.isRemote() && item.getURL().fragment() != null)
    {
      report.message(MessageId.OPF_091, item.getLocation());
    }

    // Register media overlay usage
    if (context.referenceRegistry.isPresent() && !Strings.isNullOrEmpty(item.getMediaOverlay()))
    {
      Optional<OPFItem> overlay = opfHandler.getItemById(item.getMediaOverlay());
      if (overlay.isPresent())
      {
        context.referenceRegistry.get().registerReference(overlay.get().getURL(),
            Reference.Type.MEDIA_OVERLAY, item.getLocation());
      }
    }
  }

  @Override
  protected void checkItemAfterResourceValidation(OPFItem item)
  {
    // Check remote resources
    String mediatype = item.getMimeType();
    if (item.isRemote()
        // audio, video, and fonts can be remote resources
        && !(isAudioType(mediatype) || isVideoType(mediatype)
            || "application/x-shockwave-flash".equals(mediatype) || isFontType(mediatype)))
    {
      // spine items cannot be remote resources
      // (except, theoretically, for video/audio/fonts)
      if (item.isInSpine())
      {
        report.message(MessageId.RSC_006, item.getLocation(), item.getPath());
      }
      // if no direct reference to the resource was found,
      else if (!context.referenceRegistry.get().hasReferencesTo(item.getURL()))
      {
        // if may be allowed when if the resource is retrieved from a script
        if (context.featureReport.hasFeature(FeatureEnum.HAS_SCRIPTS))
        {
          report.message(MessageId.RSC_006b, item.getLocation(), item.getPath());
        }
        // otherwise, still report it as an error, even if not used
        else
        {
          report.message(MessageId.RSC_006, item.getLocation(), item.getPath());
        }
      }
    }

    // check that resources in the manifest are referenced (usage report)
    // - search the reference registry
    // - report if no reference (of a publication-resource type) is found
    if (!(item.isInSpine() || item.isNav() || item.isNcx())
        && context.referenceRegistry.isPresent()
        && context.referenceRegistry.get().asList().stream()
            .noneMatch(ref -> ref.targetResource.equals(item.getURL())
                && ref.type.isPublicationResourceReference()))
    {
      report.message(MessageId.OPF_097, item.getLocation(), item.getPath());
    }

    if (isBlessedItemType(mediatype, version))
    {
      // check whether media-overlay attribute needs to be specified
      OverlayTextChecker overlayTextChecker = context.overlayTextChecker.get();
      String mo = item.getMediaOverlay();
      URL docURL = item.getURL();
      if (overlayTextChecker.isReferencedByOverlay(docURL))
      {
        if (Strings.isNullOrEmpty(mo))
        {
          // missing media-overlay attribute
          report.message(MessageId.MED_010, item.getLocation().context(item.getPath()));
        }
        else if (!overlayTextChecker.isCorrectOverlay(docURL, mo))
        {
          // media-overlay attribute references the wrong media overlay
          report.message(MessageId.MED_012, item.getLocation().context(item.getPath()));
        }
      }
      else
      {
        if (!Strings.isNullOrEmpty(mo))
        {
          // referenced overlay does not reference this content document
          report.message(MessageId.MED_013, item.getLocation().context(item.getPath()));
        }
      }
    }

    // check that non-linear content documents are reachable
    if (item.isInSpine() && !item.isLinear() && context.referenceRegistry.isPresent()
    // search the reference registry for any hyperlink pointing to this item
        && !context.referenceRegistry.get().asList().stream()
            .anyMatch(ref -> ref.type == Reference.Type.HYPERLINK
                && ref.targetResource.equals(item.getURL())))
    {
      // if content is scripted, references can be added by scripting
      // se we only report a usage
      if (context.featureReport.hasFeature(FeatureEnum.HAS_SCRIPTS))
      {
        report.message(MessageId.OPF_096b, item.getLocation(), item.getPath());
      }
      // else, report an error if no hyperlink were found
      else
      {
        report.message(MessageId.OPF_096, item.getLocation(), item.getPath());
      }
    }
  }

  @Override
  protected void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {
    // Items with `data:` URLs are not allowed and reported earlier
    if (item.hasDataURL())
    {
      return;
    }

    // check properties
    if (item.getProperties()
        .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
    {
      report.message(MessageId.OPF_077, item.getLocation());
    }

    // check that spine items have content document fallback
    String mimeType = item.getMimeType();
    if (!isBlessedItemType(mimeType, version))
    {
      if (!item.hasFallback())
      {
        report.message(MessageId.OPF_043, item.getLocation(), mimeType);
      }
      else if (!item.hasContentDocumentFallback())
      {
        report.message(MessageId.OPF_044, item.getLocation(), mimeType);
      }
    }
  }

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
        Optional<OPFItem> item = opfHandler.getItemByURL(resource.getDocumentURL());
        if (!item.isPresent())
        {
          // FIXME 2022 check how to report the URL
          report.message(MessageId.OPF_081, EPUBLocation.of(context),
              resource.getDocumentURL().path());
        }
        else if ("application/vnd.epub.search-key-map+xml".equals(item.get().getMimeType()))
        {
          if (skmFound)
          {
            // More than one Search Key Map
            report.message(MessageId.OPF_082, EPUBLocation.of(context));
          }
          skmFound = true;
        }
        else if (!"application/xhtml+xml".equals(item.get().getMimeType()))
        {
          // FIXME 2022 check how to report the URL
          report.message(MessageId.OPF_084, EPUBLocation.of(context),
              resource.getDocumentURL().path());
        }
      }
      if (!skmFound)
      {
        // No Search Key Map
        report.message(MessageId.OPF_083, EPUBLocation.of(context));
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
        final Optional<OPFItem> item = opfHandler.getItemByURL(resource.getDocumentURL());
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
        report.message(MessageId.OPF_078, EPUBLocation.of(context));
      }
    }
  }

  private void checkIndexCollection(ResourceCollection collection)
  {
    if (collection.hasRole(Roles.INDEX) || collection.hasRole(Roles.INDEX_GROUP))
    {
      for (LinkedResource resource : collection.getResources().asList())
      {
        Optional<OPFItem> item = opfHandler.getItemByURL(resource.getDocumentURL());
        if (!item.isPresent() || !"application/xhtml+xml".equals(item.get().getMimeType()))
        {
          report.message(MessageId.OPF_071, EPUBLocation.of(context));
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
        Optional<OPFItem> item = opfHandler.getItemByURL(resource.getDocumentURL());
        if (!item.isPresent() || !("application/xhtml+xml".equals(item.get().getMimeType())
            || "image/svg+xml".equals(item.get().getMimeType())))
        {
          report.message(MessageId.OPF_075, EPUBLocation.of(context));
        }
        else
        {
          URLFragment fragment = URLFragment.parse(resource.getURL());
          if (fragment.exists() && "epubcfi".equals(fragment.getScheme()))
          {
            report.message(MessageId.OPF_076, EPUBLocation.of(context));
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
      Optional<OPFItem> item = opfHandler.getItemByURL(link.getDocumentURL());
      if (item.isPresent() && !item.get().isInSpine())
      {
        report.message(MessageId.OPF_067, EPUBLocation.of(context), item.get().getPath());
      }
    }
  }

  // Checks that the total MO duration equals the sum of durations
  private void checkMediaOverlaysDuration()
  {
    MetadataSet metadata = ((OPFHandler30) opfHandler).getMetadata();
    // search total durations metadata expressions
    Set<Metadata> totalDurationExpressions = metadata
        .getPrimary(MediaOverlaysVocab.VOCAB.get(MediaOverlaysVocab.PROPERTIES.DURATION));
    if (!totalDurationExpressions.isEmpty())
    {
      try
      {
        // the total duration is the first primary duration found
        SmilClock totalDuration = new SmilClock(
            totalDurationExpressions.iterator().next().getValue());
        // sum all the individual durations (non-primary metadata expressions)
        SmilClock sumDuration = new SmilClock();
        Set<Metadata> allDurations = metadata
            .getAny(MediaOverlaysVocab.VOCAB.get(MediaOverlaysVocab.PROPERTIES.DURATION));
        for (Metadata durationExpression : allDurations)
        {
          if (!durationExpression.isPrimary())
          {
            sumDuration = sumDuration.addTime(new SmilClock(durationExpression.getValue()));
          }
        }
        // report if the sum and total don't match
        if (!totalDuration.eqWithinTolerance(sumDuration, 1000))
        {
          report.message(MessageId.MED_016, EPUBLocation.of(context));
        }
      } catch (NumberFormatException e)
      {
        return; // Abort sum check. Invalid values are reported by the schema.
      }
    }
  }

  private void checkPagination()
  {
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(PublicationType.EDUPUB))
    {
      if (context.featureReport.hasFeature(FeatureEnum.PAGE_BREAK))
      {
        // Check there is a page list
        if (!context.featureReport.hasFeature(FeatureEnum.PAGE_LIST))
        {
          report.message(MessageId.NAV_003, EPUBLocation.of(context));
        }
        // Search a "dc:source" metadata expression
        Set<Metadata> dcSourceMetas = ((OPFHandler30) opfHandler).getMetadata()
            .getPrimary(DCMESVocab.VOCAB.get(DCMESVocab.PROPERTIES.SOURCE));
        if (dcSourceMetas.isEmpty())
        {
          report.message(MessageId.OPF_066, EPUBLocation.of(context));
        }
        else
        {
          // Search a "source-of : pagination" expression refining a "dc:source"
          if (!MetadataSet.tryFindInRefines(dcSourceMetas,
              PackageVocabs.META_VOCAB.get(PackageVocabs.META_PROPERTIES.SOURCE_OF),
              Optional.of("pagination")).isPresent())
          {
            report.message(MessageId.OPF_066, EPUBLocation.of(context));
          }
        }
      }
    }
  }

  private void checkSemantics()
  {
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(PublicationType.EDUPUB))
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
    if (context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(PublicationType.EDUPUB))
    {
      Set<Feature> sections = context.featureReport.getFeature(FeatureEnum.SECTIONS);
      Set<Feature> tocLinks = context.featureReport.getFeature(FeatureEnum.TOC_LINKS);
      if (sections.size() != tocLinks.size())
      {
        report.message(MessageId.NAV_004, tocLinks.isEmpty() ? EPUBLocation.of(context)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.AUDIO)
          && !context.featureReport.hasFeature(FeatureEnum.LOA))
      {
        report.message(MessageId.NAV_005, tocLinks.isEmpty() ? EPUBLocation.of(context)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.FIGURE)
          && !context.featureReport.hasFeature(FeatureEnum.LOI))
      {
        report.message(MessageId.NAV_006, tocLinks.isEmpty() ? EPUBLocation.of(context)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.TABLE)
          && !context.featureReport.hasFeature(FeatureEnum.LOT))
      {
        report.message(MessageId.NAV_007, tocLinks.isEmpty() ? EPUBLocation.of(context)
            : tocLinks.iterator().next().getLocation().get());
      }
      if (context.featureReport.hasFeature(FeatureEnum.VIDEO)
          && !context.featureReport.hasFeature(FeatureEnum.LOV))
      {
        report.message(MessageId.NAV_008, tocLinks.isEmpty() ? EPUBLocation.of(context)
            : tocLinks.iterator().next().getLocation().get());
      }
    }
  }

  private void checkSpecifics()
  {
    if (context.featureReport.hasFeature(FeatureEnum.DICTIONARY)
        && !context.pubTypes.contains(PublicationType.DICTIONARY))
    {
      report.message(MessageId.OPF_079, context.featureReport.getFeature(FeatureEnum.DICTIONARY)
          .iterator().next().getLocation().get());
    }
    if (context.profile == EPUBProfile.DICT
        || context.pubTypes.contains(PublicationType.DICTIONARY))
    {
      if (!context.featureReport.hasFeature(FeatureEnum.DICTIONARY))
      {
        report.message(MessageId.OPF_078, EPUBLocation.of(context));
      }
    }
  }

  public static boolean isAudioType(String type)
  {
    return type != null && type.startsWith("audio/");
  }

  public static boolean isBlessedAudioType(String type)
  {
    return type.equals("audio/mpeg") || type.equals("audio/mp4") || type.matches("audio/ogg\\s*;\\s*codecs=opus");
  }

  public static boolean isVideoType(String type)
  {
    return type != null && type.startsWith("video/");
  }

  public static boolean isBlessedVideoType(String type)
  {
    return isVideoType(type);
  }

  public static boolean isCommonVideoType(String type)
  {
    return "video/h264".equals(type) || "video/webm".equals(type) || "video/mp4".equals(type);
  }

  public static boolean isFontType(String type)
  {
    return type.startsWith("font/") || type.startsWith("application/font-")
        || type.equals("application/vnd.ms-opentype");
  }

  public static boolean isBlessedFontType(String type)
  {
    return type.equals("font/otf") || type.equals("font/ttf") || type.equals("font/woff")
        || type.equals("font/woff2") || type.equals("application/font-sfnt")
        || type.equals("application/font-woff") || type.equals("application/vnd.ms-opentype")
        || type.equals("image/svg+xml");
  }

  public static boolean isBlessedScriptType(String type)
  {
    return type.equals("text/javascript") || type.equals("application/javascript")
        || type.equals("application/ecmascript");
  }

  public static boolean isCoreMediaType(String type)
  {
    return type != null
        && (isBlessedAudioType(type) || isBlessedVideoType(type) || isBlessedFontType(type)
            || isBlessedItemType(type, EPUBVersion.VERSION_3)
            || isBlessedImageType(type, EPUBVersion.VERSION_3) || isBlessedScriptType(type)
            || isBlessedStyleType(type)
            || type.equals("application/pls+xml") || type.equals("application/smil+xml")
            || type.equals("image/svg+xml"));
  }

  public static String getPreferredMediaType(String type, String path)
  {
    switch (Strings.nullToEmpty(type))
    {
    case "application/font-sfnt":
      return (path.endsWith(".ttf")) ? "font/ttf"
          : (path.endsWith(".otf")) ? "font/otf" : "font/(ttf|otf)";
    case "application/vnd.ms-opentype":
      return "font/otf";
    case "application/font-woff":
      return "font/woff";
    case "text/javascript":
    case "application/ecmascript":
      return "application/javascript";
    default:
      return null;
    }
  }
}

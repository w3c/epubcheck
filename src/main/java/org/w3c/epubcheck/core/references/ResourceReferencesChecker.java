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

package org.w3c.epubcheck.core.references;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.core.CheckAbortException;
import org.w3c.epubcheck.util.url.URLFragment;

import com.adobe.epubcheck.api.LocalizableReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.LocalizedMessages;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

public class ResourceReferencesChecker
{

  private final Report report;
  private final OCFContainer container;
  private final EPUBVersion version;
  private final ReferenceRegistry referenceRegistry;
  private final ResourceRegistry resourceRegistry;
  private Locale locale;

  private Set<URL> undeclared;

  public ResourceReferencesChecker(ValidationContext context)
  {
    Preconditions.checkArgument(context.container.isPresent());
    Preconditions.checkArgument(context.resourceRegistry.isPresent());
    Preconditions.checkArgument(context.referenceRegistry.isPresent());
    this.report = context.report;
    this.container = context.container.get();
    this.version = context.version;
    this.referenceRegistry = context.referenceRegistry.get();
    this.resourceRegistry = context.resourceRegistry.get();
    this.locale = (report instanceof LocalizableReport)
        ? ((LocalizableReport) report).getLocale()
        : Locale.ENGLISH;
  }

  public void check()
  {
    undeclared = new HashSet<URL>();
    Queue<Reference> tocLinks = new LinkedList<>();
    Queue<Reference> pageListLinks = new LinkedList<>();
    Queue<Reference> overlayLinks = new LinkedList<>();
    for (Reference reference : referenceRegistry.asList())
    {
      switch (reference.type)
      {
      case REGION_BASED_NAV:
        checkRegionBasedNav(reference);
        break;
      case NAV_TOC_LINK:
        tocLinks.add(reference);
        break;
      case NAV_PAGELIST_LINK:
        pageListLinks.add(reference);
        break;
      case OVERLAY_TEXT_LINK:
        overlayLinks.add(reference);
        checkReference(reference);
        break;
      default:
        checkReference(reference);
        break;
      }
    }
    checkReadingOrder(tocLinks);
    checkReadingOrder(overlayLinks);
  }

  private void checkReference(Reference reference)
  {
    // Report the reference
    report.info(reference.location.getPath(), FeatureEnum.RESOURCE, container.relativize(reference.url));
    
    // Retrieve the target resource
    Optional<Resource> targetResource = resourceRegistry.getResource(reference.targetResource);
    try
    {
      // Check remote resources
      if (container.isRemote(reference.url))
      {
        checkRemoteReference(reference, targetResource);
      }

      // Check undeclared resources
      if (!targetResource.isPresent())
      {
        checkUndeclaredReference(reference);
      }
      assert targetResource.isPresent();

      // Check fallbacks
      checkFallbacks(reference, targetResource.get());

      // Parse the URL fragment
      URLFragment fragment = URLFragment.parse(reference.url, targetResource.get().getMimeType());

      // Check reference type
      checkReferenceType(reference, targetResource.get(), fragment);

      // Fragment integrity checks
      checkFragment(reference, targetResource.get(), fragment);
    } catch (CheckAbortException e)
    {
      // an error was reported, abort early
    }
  }

  private void checkFragment(Reference reference, Resource targetResource, URLFragment fragment)
    throws CheckAbortException
  {
    String targetMimetype = targetResource.getMimeType();
    if (fragment.exists() && !fragment.isEmpty())
    {
      // Check media overlays requirements
      if (reference.type == Reference.Type.OVERLAY_TEXT_LINK)
      {
        // Check that references to XHTML indicate an element by ID
        if (MIMEType.XHTML.is(targetMimetype) && fragment.getId().isEmpty())
        {
          report.message(MessageId.MED_017, reference.location, fragment.toString());
        }
        // Check that references to SVG use a SVG fragment identifier
        else if (MIMEType.SVG.is(targetMimetype) && !fragment.isValid())
        {
          report.message(MessageId.MED_018, reference.location, fragment.toString());
        }
      }

      // Check ID-based fragments
      // Other fragment types (e.g. EPUB CFI) are not currently supported
      if (!fragment.getId().isEmpty() && !container.isRemote(reference.targetResource))
      {
        Reference.Type targetIDType = resourceRegistry.getIDType(fragment.getId(),
            targetResource);

        // Check that target ID exists (if the target is XHTML or SVG)
        if (targetIDType == null
            && (MIMEType.SVG.is(targetMimetype) || MIMEType.XHTML.is(targetMimetype)))
        {
          report.message(MessageId.RSC_012, reference.location.context(reference.url.toString()));
          throw new CheckAbortException();
        }

        switch (reference.type)
        {
        case SVG_PAINT:
        case SVG_CLIP_PATH:
          if (targetIDType != reference.type)
          {
            report.message(MessageId.RSC_014, reference.location.context(reference.url.toString()));
            throw new CheckAbortException();
          }
          break;
        case SVG_SYMBOL:
        case CITE:
        case HYPERLINK:
        case OVERLAY_TEXT_LINK:
          if (targetIDType != reference.type && targetIDType != Reference.Type.GENERIC)
          {
            report.message(MessageId.RSC_014, reference.location.context(reference.url.toString()));
            throw new CheckAbortException();
          }
          break;
        default:
          break;
        }
      }
    }

  }

  private void checkReferenceType(Reference reference, Resource targetResource,
      URLFragment fragment)
    throws CheckAbortException
  {
    String targetMimetype = targetResource.getMimeType();
    switch (reference.type)
    {
    case HYPERLINK:

      if ("epubcfi".equals(fragment.getScheme()))
      {
        break; // EPUB CFI is not supported
      }
      // if mimeType is null, we should have reported an error already
      if (!OPFChecker.isBlessedItemType(targetMimetype, version)
          && !OPFChecker.isDeprecatedBlessedItemType(targetMimetype)
          && !targetResource.hasContentDocumentFallback())
      {
        report.message(MessageId.RSC_010,
            reference.location.context(container.relativize(reference.url)));
        throw new CheckAbortException();
      }
      if (/* !res.mimeType.equals("font/opentype") && */!targetResource.isInSpine())
      {
        report.message(MessageId.RSC_011,
            reference.location.context(container.relativize(reference.url)));
        throw new CheckAbortException();
      }
      break;
    case IMAGE:
      if ("epubcfi".equals(fragment.getScheme()))
      {
        break; // EPUB CFI is not supported
      }
      if (fragment.exists() && !MIMEType.SVG.is(targetMimetype))
      {
        report.message(MessageId.RSC_009,
            reference.location.context(container.relativize(reference.url)));
        throw new CheckAbortException();
      }
      break;
    case OVERLAY_TEXT_LINK:
      if (!OPFChecker.isBlessedItemType(targetMimetype, version))
      {
        report.message(MessageId.RSC_010,
            reference.location.context(container.relativize(reference.url)));
        throw new CheckAbortException();
      }
      break;
    case SEARCH_KEY:
      if ((!fragment.exists() || !"epubcfi".equals(fragment.getScheme()))
          && !targetResource.isInSpine())
      {
        report.message(MessageId.RSC_021, reference.location,
            container.relativize(reference.targetResource));
        throw new CheckAbortException();
      }
      break;
    case STYLESHEET:
      if (fragment.exists())
      {
        report.message(MessageId.RSC_013,
            reference.location.context(container.relativize(reference.url)));
        throw new CheckAbortException();
      }
      break;
    case SVG_CLIP_PATH:
    case SVG_PAINT:
    case SVG_SYMBOL:
      if (!fragment.exists())
      {
        report.message(MessageId.RSC_015, reference.location.context(reference.url));
        throw new CheckAbortException();
      }
      break;
    default:
      break;
    }

  }

  private void checkFallbacks(Reference reference, Resource targetResource)
  {
    String targetMimetype = targetResource.getMimeType();
    switch (reference.type)
    {
    case IMAGE:
    case AUDIO:
    case VIDEO:
    case GENERIC:
      if (!reference.hasIntrinsicFallback && !OPFChecker30.isCoreMediaType(targetMimetype)
          && !targetResource.hasCoreMediaTypeFallback())
      {
        report.message(MessageId.RSC_032, reference.location,
            container.relativize(reference.targetResource), targetMimetype);
      }
      break;
    default:
      break;
    }

  }

  private void checkUndeclaredReference(Reference reference)
    throws CheckAbortException
  {
    assert !resourceRegistry.getResource(reference.targetResource).isPresent();

    // Report references to missing local resources
    if (!container.contains(reference.url) && !container.isRemote(reference.url))
    {
      // only as a WARNING for 'link' references in EPUB 3
      if (version == EPUBVersion.VERSION_3 && reference.type == Reference.Type.LINK)
      {
        report.message(MessageId.RSC_007w, reference.location,
            container.relativize(reference.targetResource));
      }
      // by default as an ERROR
      else
      {
        report.message(MessageId.RSC_007, reference.location,
            container.relativize(reference.targetResource));
      }
    }
    // Report undeclared Publication Resources (once)
    else if (!undeclared.contains(reference.targetResource)
        // links and remote hyperlinks are not Publication Resources
        && !(reference.type == Reference.Type.LINK
            || container.isRemote(reference.targetResource)
                && (reference.type == Reference.Type.HYPERLINK
                    || reference.type == Reference.Type.CITE)))
    {
      undeclared.add(reference.targetResource);
      report.message(MessageId.RSC_008, reference.location,
          container.relativize(reference.targetResource));
    }
    throw new CheckAbortException();

  }

  private void checkRemoteReference(Reference reference, Optional<Resource> targetResource)
    throws CheckAbortException
  {
    assert container.isRemote(reference.url);

    // Check if the remote reference is allowed
    if (// remote links and hyperlinks are not Publication Resources
    !EnumSet.of(Reference.Type.CITE, Reference.Type.LINK, Reference.Type.HYPERLINK)
        .contains(reference.type)
        // spine items are checked in OPFChecker30
        && !(version == EPUBVersion.VERSION_3 && targetResource.isPresent()
            && targetResource.get().isInSpine())
        // audio, video, and fonts can be remote resources in EPUB 3
        && !(version == EPUBVersion.VERSION_3 && (targetResource.isPresent()
            // if the item is declared, check its mime type
            && (OPFChecker30.isAudioType(targetResource.get().getMimeType())
                || OPFChecker30.isVideoType(targetResource.get().getMimeType())
                || OPFChecker30.isFontType(targetResource.get().getMimeType()))
            // else, check if the reference is a type allowing remote
            // resources
            || reference.type == Reference.Type.FONT
            || reference.type == Reference.Type.AUDIO
            || reference.type == Reference.Type.VIDEO)))
    {
      report.message(MessageId.RSC_006, reference.location, reference.url);
      throw new CheckAbortException();
    }

    // Check if the remote resource is using HTTPS
    else if (version == EPUBVersion.VERSION_3
        && !EnumSet.of(Reference.Type.LINK, Reference.Type.HYPERLINK)
            .contains(reference.type)
        && !"https".equals(reference.url.scheme())
        // file URLs are disallowed and reported elsewhere
        && !"file".equals(reference.url.scheme()))
    {
      report.message(MessageId.RSC_031, reference.location, reference.url);
    }

  }

  private void checkRegionBasedNav(Reference ref)
  {
    Preconditions.checkArgument(ref.type == Reference.Type.REGION_BASED_NAV);
    Optional<Resource> optionalResource = resourceRegistry.getResource(ref.targetResource);
    // abort early if the link target is not a spine item (checked elsewhere)
    if (!optionalResource.isPresent() || !optionalResource.get().hasItem()
        || !optionalResource.get().getItem().isFixedLayout())
    {
      report.message(MessageId.NAV_009, ref.location);
    }
  }

  private void checkReadingOrder(Queue<Reference> references)
  {
    int lastSpinePosition = -1;
    int lastAnchorPosition = -1;
    while (!references.isEmpty())
    {
      Reference ref = references.poll();
      Preconditions
          .checkArgument(ref.type == Reference.Type.NAV_PAGELIST_LINK
              || ref.type == Reference.Type.NAV_TOC_LINK
              || ref.type == Reference.Type.OVERLAY_TEXT_LINK);

      // Retrieve the target resource
      Optional<Resource> optionalTarget = resourceRegistry.getResource(ref.targetResource);
      // abort early if the link target is not a spine item (checked elsewhere)
      if (!optionalTarget.isPresent() || !optionalTarget.get().hasItem()
          || !optionalTarget.get().getItem().isInSpine())
      {
        continue;
      }
      Resource res = optionalTarget.get();

      // check that the link is in spine order
      int targetSpinePosition = res.getItem().getSpinePosition();
      if (targetSpinePosition < lastSpinePosition)
      {
        String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(MessageId.NAV_011,
            "spine");

        if (ref.type == Reference.Type.OVERLAY_TEXT_LINK)
        {
          report.message(MessageId.MED_015, ref.location, container.relativize(ref.url),
              orderContext);
        }
        else
        {
          report.message(MessageId.NAV_011, ref.location,
              (ref.type == Reference.Type.NAV_TOC_LINK) ? "toc" : "page-list",
              container.relativize(ref.url),
              orderContext);
        }
        lastSpinePosition = targetSpinePosition;
        lastAnchorPosition = -1;
      }
      else
      {

        // if new spine item, reset last positions
        if (targetSpinePosition > lastSpinePosition)
        {
          lastSpinePosition = targetSpinePosition;
          lastAnchorPosition = -1;
        }

        // check that the fragment is in document order
        URLFragment fragment = URLFragment.parse(ref.url, res.getMimeType());
        int targetAnchorPosition = resourceRegistry.getIDPosition(fragment.getId(), res);
        if (targetAnchorPosition > -1) {
          if (targetAnchorPosition < lastAnchorPosition)
          {
            String orderContext = LocalizedMessages.getInstance(locale).getSuggestion(
                MessageId.NAV_011,
                "document");
            if (ref.type == Reference.Type.OVERLAY_TEXT_LINK)
            {
              report.message(MessageId.MED_015, ref.location, container.relativize(ref.url),
                  orderContext);
            }
            else
            {
              report.message(MessageId.NAV_011, ref.location,
                  (ref.type == Reference.Type.NAV_TOC_LINK) ? "toc" : "page-list",
                      container.relativize(ref.url),
                      orderContext);
            }
          }
          lastAnchorPosition = targetAnchorPosition;
        }
      }
    }

  }
}

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

package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.core.AbstractChecker;
import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.core.CheckerFactory;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.OPFHandler;
import com.adobe.epubcheck.opf.OPFHandler30;
import com.adobe.epubcheck.opf.OPFItem;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.overlay.OverlayTextChecker;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import io.mola.galimatias.URL;

public final class OCFChecker extends AbstractChecker
{

  public OCFChecker(ValidationContext context)
  {
    super(context);
    Preconditions.checkArgument(MIMEType.EPUB.is(context.mimeType));
  }

  public void check()
  {
    // Create a new validation context builder from the parent context
    // It will be augmented with detected validation version, profile, etc.
    OCFCheckerState state = new OCFCheckerState(context);

    // Check the EPUB file (zip)
    // -------------------------
    //
    checkZipFile();

    // Check the OCF Container file structure
    // --------------------------------------
    //
    checkContainerStructure(state);
    OCFContainer container = state.getContainer();

    //
    // Check the mimetype file
    // ------------------------------
    //
    checkMimetypeFile(state);

    //
    // Check the container.xml file
    // ----------------------------
    //
    if (!checkContainerFile(state))
    {
      return;
    }
    List<URL> packageDocs = state.getPackageDocuments();

    //
    // Check the declared package documents
    // ------------------------------------
    //
    if (!checkDeclaredPackageDocuments(state))
    {
      return;
    }

    //
    // Override the context-provided version and profile
    // by what is actually declared in the publication
    // -------------------------------------------------
    EPUBVersion validationVersion = checkPublicationVersion(state);
    state.setVersion(validationVersion);
    state.setProfile(checkPublicationProfile(state, validationVersion));

    //
    // Check if there are multiple renditions
    // --------------------------------------
    // EPUB 2.0 says there SHOULD be only one OPS rendition
    if (validationVersion == EPUBVersion.VERSION_2 && packageDocs.size() > 1)
    {
      report.message(MessageId.PKG_013, OCFMetaFile.CONTAINER.asLocation(container));
    }
    // EPUB 3.0 Multiple Renditions recommends the presence of a metadata file
    if (validationVersion == EPUBVersion.VERSION_3 && packageDocs.size() > 1)
    {
      state.addProperty(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION));
      if (!OCFMetaFile.METADATA.isPresent(container))
      {
        report.message(MessageId.RSC_019, EPUBLocation.of(context));
      }
      if (state.getMappingDocument().isPresent())
      {
        new MappingDocumentChecker(
            state.context().mimetype("application/xhtml+xml").url(state.getMappingDocument().get())
                .addProperty(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.RENDITION_MAPPING))
                .build()).check();
      }
    }

    //
    // Check other META-INF files
    // ------------------------------
    //
    checkEncryptionFile(state);
    checkOtherMetaFiles(state);
    container = state.getContainer();

    //
    // Check each Publication (i.e. Package Document)
    // ----------------------------------------------
    //
    List<OPFHandler> opfHandlers = new LinkedList<OPFHandler>();
    for (URL packageDoc : packageDocs)
    {
      ValidationContextBuilder opfContext = state.context().url(packageDoc)
          .mimetype(MIMEType.PACKAGE_DOC.toString()).featureReport(new FeatureReport());

      opfContext.container(container);
      opfContext.pubTypes(state.getPublicationTypes(packageDoc));
      opfContext.xrefChecker(new XRefChecker(state.context().build()));
      opfContext.overlayTextChecker(new OverlayTextChecker());

      Checker opfChecker = CheckerFactory.newChecker(opfContext.build());
      assert opfChecker instanceof OPFChecker;
      opfChecker.check();
      opfHandlers.add(((OPFChecker) opfChecker).getOPFHandler());
    }

    //
    // Check container consistency with Package Documents content
    // ----------------------------------------------------------
    //

    for (final URL resource : container.getResources())
    {
      String path = resource.path().substring(1);
      // if the entry is not in the whitelist (META-INF/* + mimetype)
      // and not declared in (one of) the OPF document(s)

      // FIXME 2022 add a method to register known files in state, to fix #1115
      // (in this case, only the last mapping document is considered known)
      if (!path.startsWith("META-INF/") && !path.startsWith("META-INF\\")
          && !path.equals("mimetype") && !state.isDeclared(resource)
          && !Iterables.tryFind(opfHandlers, new Predicate<OPFHandler>()
          {
            @Override
            public boolean apply(OPFHandler opfHandler)
            {
              // found if declared as an OPF item
              // or in an EPUB 3 link element
              return opfHandler.getItemByURL(resource).isPresent()
                  || (validationVersion == EPUBVersion.VERSION_3
                      && ((OPFHandler30) opfHandler).getLinkedResources().hasResource(resource));
            }
          }).isPresent())
      {
        report.message(MessageId.OPF_003, EPUBLocation.of(context), path);
      }

      // check obfuscated resource are Font Core Media Types
      if (state.isObfuscated(resource))
      {
        for (OPFHandler opf : opfHandlers)
        {
          // try to find the first Package Document where the entry is
          // declared
          Optional<OPFItem> item = opf.getItemByURL(resource);
          if (item.isPresent())
          {
            // report if it is not a font core media type
            if (!OPFChecker30.isBlessedFontType(item.get().getMimeType()))
            {
              report.message(MessageId.PKG_026, state.getObfuscationLocation(resource),
                  item.get().getMimeType(), opf.getPath());
            }
            break;
          }
        }
      }
    }

    //
    // Check other file properties
    // ---------------------------
    //
    checkFileExtension(state);

  }

  private boolean checkContainerFile(OCFCheckerState state)
  {
    OCFContainer container = state.getContainer();
    // Check the presence of the container file
    // If absent, report and return early.
    if (!OCFMetaFile.CONTAINER.isPresent(container))
    {
      // do not report the missing container entry if a fatal error was already
      // reported
      if (report.getFatalErrorCount() == 0)
      {
        report.message(MessageId.RSC_002, EPUBLocation.of(context));
      }
      return false;
    }
    // FIXME 2022 - report container info
    // long l = container.getTimeEntry(OCFData.containerEntry);
    // if (l > 0)
    // {
    // Date d = new Date(l);
    // String formattedDate = new
    // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d);
    // report.info(OCFData.containerEntry, FeatureEnum.CREATION_DATE,
    // formattedDate);
    // }

    ValidationContext containerFileContext = state.context()
        .url(OCFMetaFile.CONTAINER.asURL(container)).mimetype("application/xml").build();
    OCFContainerFileChecker containerFileChecker = new OCFContainerFileChecker(containerFileContext,
        state);
    containerFileChecker.check();
    return true;
  }

  private void checkContainerStructure(OCFCheckerState state)
  {
    // Get a container
    Iterable<OCFResource> resourcesProvider;
    try
    {
      // FIXME 2022 build resourcesProvider depending on MIME type
      resourcesProvider = new OCFZipResources(context.url);
    } catch (IOException e)
    {
      // FIXME 2022 see how to propagate fatal IOError
      report.message(MessageId.PKG_008, EPUBLocation.of(context), e.getLocalizedMessage());
      return;
    }
    // Map to store the container resource files
    Map<String, OCFResource> resources = new HashMap<>();
    // List to store the container resource directories
    List<String> directories = new LinkedList<>();

    // Loop through the entries
    OCFFilenameChecker filenameChecker = new OCFFilenameChecker(state.context().build());
    for (OCFResource resource : resourcesProvider)
    {
      Preconditions.checkNotNull(resource.getPath());
      Preconditions.checkNotNull(resource.getProperties());

      // FIXME 2022 report symbolic links and continue

      // Check duplicate entries
      if (resources.containsKey(resource.getPath().toLowerCase(Locale.ROOT)))
      {
        context.report.message(MessageId.OPF_060, EPUBLocation.of(context), resource.getPath());
      }
      // Check duplicate entries after NFC normalization
      else if (resources.containsKey(
          Normalizer.normalize(resource.getPath().toLowerCase(Locale.ROOT), Normalizer.Form.NFC)))
      {
        context.report.message(MessageId.OPF_061, EPUBLocation.of(context), resource.getPath());
      }

      // Store the resource in the data structure
      if (resource.isDirectory())
      {
        // the container resource is a directory,
        // store it for later checking of empty directories
        directories.add(resource.getPath());
      }
      else
      {
        // Check file name requirements
        filenameChecker.checkCompatiblyEscaped(resource.getPath());

        // report entry metadata
        reportFeatures(resource.getProperties());
        // the container resource is a file,
        // add the resource to the container model
        resources.put(resource.getPath().toLowerCase(Locale.ROOT), resource);
        state.addResource(resource);
      }
    }

    // Report empty directories
    for (String directory : directories)
    {
      boolean hasContents = false;
      for (OCFResource resource : resources.values())
      {
        if (resource.getPath().startsWith(directory))
        {
          hasContents = true;
          break;
        }
      }
      if (!hasContents)
      {
        report.message(MessageId.PKG_014, EPUBLocation.of(context), directory);
      }
    }
  }

  private boolean checkDeclaredPackageDocuments(OCFCheckerState state)
  {
    List<URL> packageDocs = state.getPackageDocuments();
    OCFContainer container = state.getContainer();
    if (packageDocs.isEmpty())
    {
      report.message(MessageId.RSC_003, OCFMetaFile.CONTAINER.asLocation(container));
      return false;
    }
    else
    {
      if (packageDocs.size() > 1)
      {
        report.info(null, FeatureEnum.EPUB_RENDITIONS_COUNT, Integer.toString(packageDocs.size()));
      }
      boolean isPrimary = true;
      for (URL packageDoc : packageDocs)
      {
        if (!container.contains(packageDoc))
        {
          report.message(MessageId.OPF_002, OCFMetaFile.CONTAINER.asLocation(container),
              packageDoc);
          return false;
        }

        ValidationContext packageDocContext = state.context().url(packageDoc).mimetype("").build();
        PackageDocumentPeeker peeker = new PackageDocumentPeeker(packageDocContext, state);
        peeker.peek();
        // FIXME 2022 find a better way to detect errors (notably for
        // VERSION_NOT_FOUND)
        if (!state.getError().isEmpty())
        {

          context.report.message(MessageId.OPF_001, EPUBLocation.of(packageDocContext),
              state.getError());
          if (isPrimary) return false;
        }
        else if (state.getPublicationVersion(packageDoc) == EPUBVersion.Unknown)
        {
          context.report.message(MessageId.OPF_001, EPUBLocation.of(packageDocContext),
              InvalidVersionException.VERSION_NOT_FOUND);
        }
        isPrimary = false;
      }
    }
    return true;
  }

  private void checkEncryptionFile(OCFCheckerState state)
  {
    OCFContainer container = state.getContainer();
    if (OCFMetaFile.ENCRYPTION.isPresent(container))
    {
      report.info(null, FeatureEnum.HAS_ENCRYPTION, OCFMetaFile.ENCRYPTION.asPath());
      ValidationContext encryptionFileContext = state.context()
          .url(OCFMetaFile.ENCRYPTION.asURL(container)).mimetype("application/xml").build();
      OCFEncryptionFileChecker checker = new OCFEncryptionFileChecker(encryptionFileContext, state);
      checker.check();
    }
  }

  private void checkZipFile()
  {
    File packageFile = new File(context.path);
    // If the container is packaged (= not a directory):
    if (packageFile.isFile())
    {
      new OCFZipChecker(context).check();
    }
  }

  private void checkFileExtension(OCFCheckerState state)
  {
    File packageFile = new File(context.path);
    // If the container is packaged (= not a directory):
    if (packageFile.isFile())
    {
      new OCFExtensionChecker(state.context().build()).check();
    }

  }

  private void checkMimetypeFile(OCFCheckerState state)
  {
    OCFContainer container = state.getContainer();
    if (OCFMetaFile.MIMETYPE.isPresent(container))
    {
      try (InputStream is = container.openStream(OCFMetaFile.MIMETYPE.asURL(container)))
      {
        StringBuilder sb = new StringBuilder(2048);
        // FIXME next check mimetype file content here
        if (!CheckUtil.checkTrailingSpaces(is, sb))
        {
          report.message(MessageId.PKG_007, OCFMetaFile.MIMETYPE.asLocation(container));
        }
        if (sb.length() != 0)
        {
          report.info(null, FeatureEnum.FORMAT_NAME, sb.toString().trim());
        }
      } catch (IOException e)
      {
        // ignore, missing file is reported elsewhere.
      }
    }
    else
    {
      // FIXME 2022 report missing mimetype file if not a ZIP
      // FIXME 2022 check mimetype file content if not a ZIP
    }
  }

  private void checkOtherMetaFiles(OCFCheckerState state)
  {
    assert state.getContainer() != null;

    OCFContainer container = state.getContainer();
    if (OCFMetaFile.CONTAINER.isPresent(container))
    {
      // Re-check container file for schema errors
      // the previous parsing only peeked critical info
      new OCFMetaFileChecker(state.context().url(OCFMetaFile.CONTAINER.asURL(container))
          .mimetype("application/xml").build()).check();
    }
    if (OCFMetaFile.METADATA.isPresent(container))
    {
      new OCFMetaFileChecker(state.context().url(OCFMetaFile.METADATA.asURL(container))
          .mimetype("application/xml").build()).check();
    }
    if (OCFMetaFile.SIGNATURES.isPresent(container))
    {
      new OCFMetaFileChecker(state.context().url(OCFMetaFile.SIGNATURES.asURL(container))
          .mimetype("application/xml").build()).check();
      report.info(null, FeatureEnum.HAS_SIGNATURES, OCFMetaFile.SIGNATURES.asPath());
    }
  }

  private EPUBProfile checkPublicationProfile(OCFCheckerState state, EPUBVersion validationVersion)
  {
    assert state.getContainer() != null;
    assert !state.getPackageDocuments().isEmpty();

    switch (validationVersion)
    {
    case VERSION_2:
      if (context.profile != EPUBProfile.DEFAULT)
      {
        // Validation profile is unsupported for EPUB 2.0
        report.message(MessageId.PKG_023,
            EPUBLocation.of(state.getPackageDocuments().get(0), state.getContainer()));
      }
      return EPUBProfile.DEFAULT;
    case VERSION_3:

      // Override the given validation profile depending on the dc:type
      // declared in the package document
      EPUBProfile checkedProfile = context.profile.makeTypeCompatible(state.getPublicationTypes());
      if (checkedProfile != context.profile)
      {
        report.message(MessageId.OPF_064,
            EPUBLocation.of(state.getPackageDocuments().get(0), state.getContainer()),
            checkedProfile.matchingType(), checkedProfile);
      }
      return checkedProfile;

    default:
      throw new IllegalStateException();
    }
  }

  private EPUBVersion checkPublicationVersion(OCFCheckerState state)
  {
    Preconditions.checkState(!state.getPackageDocuments().isEmpty());

    // Detect the version of the first root file
    // and compare with the asked version (if set)
    Preconditions.checkState(state.getPublicationVersion().isPresent());
    final EPUBVersion detectedVersion = state.getPublicationVersion().get();
    report.info(null, FeatureEnum.FORMAT_VERSION, detectedVersion.toString());

    if (context.version != detectedVersion && context.version != EPUBVersion.Unknown)
    {
      report.message(MessageId.PKG_001,
          EPUBLocation.of(state.getPackageDocuments().get(0), state.getContainer()),
          context.version, detectedVersion);

      return context.version;
    }
    else
    {
      return detectedVersion;
    }
  }

  private void reportFeatures(Map<FeatureEnum, String> features)
  {
    for (FeatureEnum feature : features.keySet())
    {
      report.info(context.path, feature, features.get(feature));
    }
  }

}

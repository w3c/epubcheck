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

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasProp;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.path;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.profile;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.core.CheckerFactory;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.OPFData;
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
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class OCFChecker implements Checker
{

  private static final ValidatorMap validatorMap = ValidatorMap.builder()
      .put(Predicates.and(path(OCFData.containerEntry), version(EPUBVersion.VERSION_2)),
          XMLValidators.CONTAINER_20_RNG)
      .putAll(Predicates.and(path(OCFData.containerEntry), version(EPUBVersion.VERSION_3)),
          XMLValidators.CONTAINER_30_RNC, XMLValidators.CONTAINER_30_RENDITIONS_SCH)
      .putAll(Predicates.and(path(OCFData.encryptionEntry), version(EPUBVersion.VERSION_3)),
          XMLValidators.ENC_30_RNC, XMLValidators.ENC_30_SCH)
      .put(Predicates.and(path(OCFData.encryptionEntry), version(EPUBVersion.VERSION_2)),
          XMLValidators.ENC_20_RNG)
      .put(Predicates.and(path(OCFData.signatureEntry), version(EPUBVersion.VERSION_2)),
          XMLValidators.SIG_20_RNG)
      .put(Predicates.and(path(OCFData.signatureEntry), version(EPUBVersion.VERSION_3)),
          XMLValidators.SIG_30_RNC)
      .put(
          Predicates.and(path(OCFData.metadataEntry),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION))),
          XMLValidators.META_30_RNC)
      .put(
          Predicates.and(path(OCFData.metadataEntry),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION))),
          XMLValidators.META_30_SCH)
      .put(Predicates.and(path(OCFData.metadataEntry),
          hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION)),
          profile(EPUBProfile.EDUPUB)), XMLValidators.META_EDUPUB_SCH)
      .putAll(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.RENDITION_MAPPING)),
          XMLValidators.RENDITION_MAPPING_RNC, XMLValidators.RENDITION_MAPPING_SCH)
      .build();

  private final ValidationContext context;
  private final OCFPackage ocf;
  private final Report report;

  public OCFChecker(ValidationContext context)
  {
    Preconditions.checkState(context.ocf.isPresent());
    this.context = context;
    this.ocf = context.ocf.get();
    this.report = context.report;
  }

  public void check()
  {
    // Create a new validation context builder from the parent context
    // It will be augmented with detected validation version, profile, etc.
    ValidationContextBuilder newContextBuilder = new ValidationContextBuilder(context);

    ocf.setReport(report);
    if (!ocf.hasEntry(OCFData.containerEntry))
    {
      checkZipHeader(); // run ZIP header checks in any case before returning
      // do not report the missing container entry if a fatal error was already
      // reported
      if (report.getFatalErrorCount() == 0)
      {
        report.message(MessageId.RSC_002, EPUBLocation.create(ocf.getName()));
      }
      return;
    }
    long l = ocf.getTimeEntry(OCFData.containerEntry);
    if (l > 0)
    {
      Date d = new Date(l);
      String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d);
      report.info(OCFData.containerEntry, FeatureEnum.CREATION_DATE, formattedDate);
    }
    OCFData containerData = ocf.getOcfData();

    // retrieve the paths of root files
    List<String> opfPaths = containerData.getEntries(MIMEType.PACKAGE_DOC.toString());
    if (opfPaths == null || opfPaths.isEmpty())
    {
      checkZipHeader(); // run ZIP header checks in any case before returning
      report.message(MessageId.RSC_003, EPUBLocation.create(OCFData.containerEntry));
      return;
    }
    else if (opfPaths.size() > 0)
    {
      if (opfPaths.size() > 1)
      {
        report.info(null, FeatureEnum.EPUB_RENDITIONS_COUNT, Integer.toString(opfPaths.size()));
      }

      // test every element for empty or missing @full-path attribute
      // bugfix for issue 236 / issue 95
      int rootfileErrorCounter = 0;
      for (String opfPath : opfPaths)
      {
        if (opfPath == null)
        {
          ++rootfileErrorCounter;
          report.message(MessageId.OPF_016, EPUBLocation.create(OCFData.containerEntry));
        }
        else if (opfPath.isEmpty())
        {
          ++rootfileErrorCounter;
          report.message(MessageId.OPF_017, EPUBLocation.create(OCFData.containerEntry));
        }
        else if (!ocf.hasEntry(opfPath))
        {
          checkZipHeader(); // run ZIP header checks in any case before
                            // returning
          report.message(MessageId.OPF_002, EPUBLocation.create(OCFData.containerEntry), opfPath);
          return;
        }
      }
      if (rootfileErrorCounter == opfPaths.size())
      {
        checkZipHeader(); // run ZIP header checks in any case before returning
        // end validation at this point when @full-path attribute is missing in
        // container.xml
        // otherwise, tons of errors would be thrown
        // ("XYZ exists in the zip file, but is not declared in the OPF file")
        return;
      }
    }

    //
    // Compute the validation version
    // ------------------------------
    // Detect the version of the first root file
    // and compare with the asked version (if set)
    EPUBVersion detectedVersion = null;
    final EPUBVersion validationVersion;
    OPFData opfData = ocf.getOpfData().get(opfPaths.get(0));
    if (opfData == null)
    {
      checkZipHeader(); // run ZIP header checks in any case before returning
      return;// The error must have been reported during
    }
    // parsing
    detectedVersion = opfData.getVersion();
    report.info(null, FeatureEnum.FORMAT_VERSION, detectedVersion.toString());
    assert (detectedVersion != null);

    if (context.version != EPUBVersion.Unknown && context.version != detectedVersion)
    {
      report.message(MessageId.PKG_001, EPUBLocation.create(opfPaths.get(0)), context.version,
          detectedVersion);

      validationVersion = context.version;
    }
    else
    {
      validationVersion = detectedVersion;
    }
    newContextBuilder.version(validationVersion);

    //
    // Check the EPUB file header
    // ------------------------------
    checkZipHeader();

    //
    // Compute the validation profile
    // ------------------------------
    // FIXME get profile from metadata.xml if available
    EPUBProfile validationProfile = context.profile;
    if (validationVersion == EPUBVersion.VERSION_2 && context.profile != EPUBProfile.DEFAULT)
    {
      // Validation profile is unsupported for EPUB 2.0
      report.message(MessageId.PKG_023, EPUBLocation.create(opfPaths.get(0)));
      validationProfile = EPUBProfile.DEFAULT;
    }
    else if (validationVersion == EPUBVersion.VERSION_3)
    {
      // Override the given validation profile depending on the primary OPF
      // dc:type

      EPUBProfile opfProfile = validationProfile.makeTypeCompatible(opfData.getTypes());
      if (opfProfile != validationProfile) {
        report.message(MessageId.OPF_064, EPUBLocation.create(opfPaths.get(0)), opfProfile.matchingType(),
            opfProfile);
      }
      validationProfile = opfProfile;
    }
    newContextBuilder.profile(validationProfile);

    //
    // Check multiple renditions
    // ------------------------------
    // EPUB 2.0 says there SHOULD be only one OPS rendition
    if (validationVersion == EPUBVersion.VERSION_2 && opfPaths.size() > 1)
    {
      report.message(MessageId.PKG_013, EPUBLocation.create(OCFData.containerEntry));
    }
    // EPUB 3.0 Multiple Renditions recommends the presence of a metadata file
    if (validationVersion == EPUBVersion.VERSION_3 && opfPaths.size() > 1)
    {
      newContextBuilder
          .addProperty(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION));
      if (!ocf.hasEntry(OCFData.metadataEntry))
      {
        report.message(MessageId.RSC_019, EPUBLocation.create(ocf.getName()));
      }
      if (containerData.getMapping().isPresent())
      {
        validateRenditionMapping(new ValidationContextBuilder(newContextBuilder.build())
            .mimetype("application/xhtml+xml").path(containerData.getMapping().get())
            .addProperty(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.RENDITION_MAPPING))
            .build());
      }
    }

    //
    // Check the mimetype file
    // ------------------------------
    //
    InputStream mimetype = null;
    try
    {
      mimetype = ocf.getInputStream("mimetype");
      StringBuilder sb = new StringBuilder(2048);
      if (ocf.hasEntry("mimetype")
          && !CheckUtil.checkTrailingSpaces(mimetype, validationVersion, sb))
      {
        report.message(MessageId.PKG_007, EPUBLocation.create("mimetype"));
      }
      if (sb.length() != 0)
      {
        report.info(null, FeatureEnum.FORMAT_NAME, sb.toString().trim());
      }
    } catch (IOException ignored)
    {
      // missing file will be reported later
    } finally
    {
      try
      {
        if (mimetype != null)
        {
          mimetype.close();
        }
      } catch (IOException ignored)
      {
        // eat it
      }
    }

    //
    // Check the META-INF files
    // ------------------------------
    //
    validateMetaFiles(newContextBuilder.mimetype("xml").build());

    //
    // Check each OPF (i.e. Rendition)
    // -------------------------------
    //
    // Validate each OPF and keep a reference of the OPFHandler
    List<OPFHandler> opfHandlers = new LinkedList<OPFHandler>();
    for (String opfPath : opfPaths)
    {
      OPFData opfInfo = context.ocf.get().getOpfData().get(opfPath);
      EPUBProfile opfProfile = validationProfile.makeTypeCompatible(opfInfo.getTypes());
      if (opfProfile != validationProfile) {
        report.message(MessageId.OPF_064, EPUBLocation.create(opfPath), opfProfile.matchingType(),
            opfProfile);
      }
      
      ValidationContext opfContext = newContextBuilder.path(opfPath)
          .mimetype(MIMEType.PACKAGE_DOC.toString()).featureReport(new FeatureReport())
          .pubTypes(opfInfo != null ? opfInfo.getTypes() : null)
          .xrefChecker(new XRefChecker(context.ocf.get(), report, validationVersion))
          .profile(opfProfile)
          .overlayTextChecker(new OverlayTextChecker()).build();

      Checker opfChecker = CheckerFactory.newChecker(opfContext);
      assert opfChecker instanceof OPFChecker;
      opfChecker.check();
      opfHandlers.add(((OPFChecker) opfChecker).getOPFHandler());
    }

    //
    // Check container integrity
    // -------------------------------
    //
    try
    {
      // report duplicate entries
      Set<String> entriesSet = new HashSet<String>();
      Set<String> normalizedEntriesSet = new HashSet<String>();
      // run duplicate check from the LinkedList which may contain duplicates
      for (final String entry : ocf.getEntries())
      {
        if (!entriesSet.add(entry.toLowerCase(Locale.ENGLISH)))
        {
          report.message(MessageId.OPF_060, EPUBLocation.create(ocf.getPackagePath()), entry);
        }
        else if (!normalizedEntriesSet.add(Normalizer.normalize(entry, Form.NFC)))
        {
          report.message(MessageId.OPF_061, EPUBLocation.create(ocf.getPackagePath()), entry);
        }
      }

      // check all file entries without duplicates
      for (final String entry : ocf.getFileEntries())
      {
        ocf.reportMetadata(entry, report);

        // if the entry is not in the whitelist (META-INF/* + mimetype)
        // and not declared in (one of) the OPF document(s)
        if (!entry.startsWith("META-INF/") && !entry.startsWith("META-INF\\")
            && !entry.equals("mimetype") && !containerData.getEntries().contains(entry)
            && !entry.equals(containerData.getMapping().orNull())
            && !Iterables.tryFind(opfHandlers, new Predicate<OPFHandler>()
            {
              @Override
              public boolean apply(OPFHandler opfHandler)
              {
                // found if declared as an OPF item
                // or in an EPUB 3 link element
                return opfHandler.getItemByPath(entry).isPresent()
                    || (validationVersion == EPUBVersion.VERSION_3
                        && ((OPFHandler30) opfHandler).getLinkedResources().hasPath(entry));
              }
            }).isPresent())
        {
          report.message(MessageId.OPF_003, EPUBLocation.create(ocf.getName()), entry);
        }
        OCFFilenameChecker.checkCompatiblyEscaped(entry, report, validationVersion);

        // check obfuscated resource are Font Core Media Types
        if (ocf.isObfuscatedFont(entry))
        {
          for (OPFHandler opf : opfHandlers)
          {
            // try to find the first Package Document where the entry is
            // declared
            Optional<OPFItem> item = opf.getItemByPath(entry);
            if (item.isPresent())
            {
              // report if it is not a font core media type
              if (!OPFChecker30.isBlessedFontType(item.get().getMimeType()))
              {
                report.message(MessageId.PKG_026, ocf.getObfuscationDeclarationLocation(entry),
                    item.get().getMimeType(), opf.getPath());
              }
              break;
            }
          }
        }
      }

      // check all directory entries without duplicates
      for (String directory : ocf.getDirectoryEntries())
      {
        boolean hasContents = false;
        for (String file : ocf.getFileEntries())
        {
          if (file.startsWith(directory))
          {
            hasContents = true;
            break;
          }
        }
        if (!hasContents)
        {
          report.message(MessageId.PKG_014, EPUBLocation.create(ocf.getName()), directory);
        }
      }
    } catch (IOException e)
    {
      report.message(MessageId.PKG_015, EPUBLocation.create(ocf.getName()), e.getMessage());
    }
  }

  private boolean validateMetaFiles(ValidationContext context)
  {
    // validate container
    validateMetaFile(new ValidationContextBuilder(context).path(OCFData.containerEntry).build());

    // Validate encryption.xml
    if (ocf.hasEntry(OCFData.encryptionEntry))
    {
      validateMetaFile(new ValidationContextBuilder(context).path(OCFData.encryptionEntry).build());
      report.info(null, FeatureEnum.HAS_ENCRYPTION, OCFData.encryptionEntry);
    }

    // validate signatures.xml
    if (ocf.hasEntry(OCFData.signatureEntry))
    {
      validateMetaFile(new ValidationContextBuilder(context).path(OCFData.signatureEntry).build());
      report.info(null, FeatureEnum.HAS_SIGNATURES, OCFData.signatureEntry);
    }

    // validate signatures.xml
    if (ocf.hasEntry(OCFData.metadataEntry))
    {
      validateMetaFile(new ValidationContextBuilder(context).path(OCFData.metadataEntry).build());
    }

    return false;
  }

  private void validateMetaFile(ValidationContext context)
  {
    XMLParser parser = new XMLParser(context);
    if (context.path.equals(OCFData.encryptionEntry))
    {
      parser.addContentHandler(new EncryptionHandler(context));
    }
    else
    {
      parser.addContentHandler(new OCFHandler(context));
    }
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      parser.addValidator(validator);
    }
    parser.process();
  }

  private void validateRenditionMapping(ValidationContext context)
  {
    XMLParser parser = new XMLParser(context);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      parser.addValidator(validator);
    }
    parser.process();
  }

  private void checkZipHeader()
  {
    checkZipHeader(new File(context.path), report);
  }

  public static void checkZipHeader(File epubFile, Report report)
  {

    FileInputStream epubIn = null;
    try
    {

      epubIn = new FileInputStream(epubFile);

      byte[] header = new byte[58];

      int readCount = epubIn.read(header);
      if (readCount != -1)
      {
        while (readCount < header.length)
        {
          int read = epubIn.read(header, readCount, header.length - readCount);
          // break on eof
          if (read == -1)
          {
            break;
          }
          readCount += read;
        }
      }

      if (readCount != header.length)
      {
        report.message(MessageId.PKG_003, EPUBLocation.create(epubFile.getName(), ""));
      }
      else
      {
        int fnsize = getIntFromBytes(header, 26);
        int extsize = getIntFromBytes(header, 28);

        if (header[0] != 'P' && header[1] != 'K')
        {
          report.message(MessageId.PKG_004, EPUBLocation.create(epubFile.getName()));
        }
        else if (fnsize != 8)
        {
          report.message(MessageId.PKG_006, EPUBLocation.create(epubFile.getName()));
        }
        else if (extsize != 0)
        {
          report.message(MessageId.PKG_005, EPUBLocation.create(epubFile.getName()), extsize);
        }
        else if (!CheckUtil.checkString(header, 30, "mimetype"))
        {
          report.message(MessageId.PKG_006, EPUBLocation.create(epubFile.getName()));
        }
        else if (!CheckUtil.checkString(header, 38, "application/epub+zip"))
        {
          report.message(MessageId.PKG_007, EPUBLocation.create("mimetype"));
        }
      }
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(epubFile.getName(), ""),
          e.getMessage());
    } finally
    {
      try
      {
        if (epubIn != null)
        {
          epubIn.close();
        }
      } catch (IOException ignored)
      {
      }
    }
  }

  private static int getIntFromBytes(byte[] bytes, int offset)
  {
    int hi = 0xFF & bytes[offset + 1];
    int lo = 0xFF & bytes[offset];
    return hi << 8 | lo;
  }

}

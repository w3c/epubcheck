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

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.OPFHandler;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;

public class OCFChecker
{
  private final OCFPackage ocf;
  private final Report report;
  private final EPUBVersion version;
  private final EPUBProfile profile;
  // Hashtable encryptedItems;
  // private EPUBVersion version = EPUBVersion.VERSION_3;

  private static final HashMap<OPSType, XMLValidator> xmlValidatorMap;

  static
  {
    HashMap<OPSType, XMLValidator> map = new HashMap<OPSType, XMLValidator>();
    map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_2), XMLValidators.CONTAINER_20_RNG.get());
    map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_3), XMLValidators.CONTAINER_30_RNC.get());

    map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_2), XMLValidators.ENC_20_RNG.get());
    map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_3), XMLValidators.ENC_30_RNC.get());

    map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_2), XMLValidators.SIG_20_RNG.get());
    map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_3), XMLValidators.SIG_30_RNC.get());

    xmlValidatorMap = map;
  }

  public OCFChecker(OCFPackage ocf, Report report, EPUBVersion version)
  {
    this(ocf, report, version, EPUBProfile.DEFAULT);
  }

  public OCFChecker(OCFPackage ocf, Report report, EPUBVersion version, EPUBProfile profile)
  {
    this.ocf = ocf;
    this.report = report;
    this.version = version;
    this.profile = profile==null? EPUBProfile.DEFAULT : profile;
  }

  public void runChecks()
  {
    ocf.setReport(report);
    if (!ocf.hasEntry(OCFData.containerEntry))
    {
      report.message(MessageId.RSC_002, new MessageLocation(ocf.getName(), 0, 0));
      return;
    }
    long l = ocf.getTimeEntry(OCFData.containerEntry);
    if (l > 0)
    {
      Date d = new Date(l);
      String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d);
      report.info(OCFData.containerEntry, FeatureEnum.CREATION_DATE, formattedDate);
    }
    OCFData containerHandler = ocf.getOcfData();

    // retrieve the paths of root files
    List<String> opfPaths = containerHandler.getEntries(OPFData.OPF_MIME_TYPE);
    if (opfPaths == null || opfPaths.isEmpty())
    {
      report.message(MessageId.RSC_003, new MessageLocation(OCFData.containerEntry, -1, -1));
      return;
    }
    else if (opfPaths.size() > 0)
    {
      if(opfPaths.size() > 1)
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
          report.message(MessageId.OPF_016, new MessageLocation(OCFData.containerEntry, -1, -1));
        }
        else if (opfPath.isEmpty())
        {
          ++rootfileErrorCounter;
          report.message(MessageId.OPF_017, new MessageLocation(OCFData.containerEntry, -1, -1));
         }
        else if (!ocf.hasEntry(opfPath))
        {
          report.message(MessageId.OPF_002, new MessageLocation(OCFData.containerEntry, -1, -1), opfPath);
          return;
        }
      }
      if(rootfileErrorCounter == opfPaths.size())
      {
        // end validation at this point when @full-path attribute is missing in container.xml
        // otherwise, tons of errors would be thrown ("XYZ exists in the zip file, but is not declared in the OPF file")
        return;
      }
    }


    // Detect the version of the first root file
    // and compare with the asked version (if set)
    EPUBVersion detectedVersion = null;
    EPUBVersion validationVersion;
    OPFData opfData = ocf.getOpfData().get(opfPaths.get(0));
    if (opfData == null)
        return;// The error must have been reported during parsing
    detectedVersion = opfData.getVersion();
    report.info(null, FeatureEnum.FORMAT_VERSION, detectedVersion.toString());
    assert (detectedVersion != null);

    if (version != null && version != detectedVersion)
    {
      report.message(MessageId.PKG_001, new MessageLocation(opfPaths.get(0), -1, -1), version, detectedVersion);

      validationVersion = version;
    }
    else
    {
      validationVersion = detectedVersion;
    }

    EPUBProfile validationProfile=profile;
    if (validationVersion == EPUBVersion.VERSION_2 && profile != EPUBProfile.DEFAULT) {
      // Validation profile is unsupported for EPUB 2.0
      report.message(MessageId.PKG_023, new MessageLocation(opfPaths.get(0), -1, -1));
    } else if (validationVersion == EPUBVersion.VERSION_3) {
      // Override the given validation profile depending on the primary OPF dc:type
      validationProfile = EPUBProfile.makeOPFCompatible(profile, opfData, opfPaths.get(0), report);
    }
    
    // EPUB 2.0 says there SHOULD be only one OPS rendition
    if (validationVersion == EPUBVersion.VERSION_2 && opfPaths.size() > 1)
    {
      report.message(MessageId.PKG_013, new MessageLocation(OCFData.containerEntry, -1, -1));
    }
    


    // Check the mimetype file
    InputStream mimetype = null;
    try
    {
      mimetype = ocf.getInputStream("mimetype");
      StringBuilder sb = new StringBuilder(2048);
      if (ocf.hasEntry("mimetype")
          && !CheckUtil.checkTrailingSpaces(mimetype,
          validationVersion, sb))
      {
        report.message(MessageId.PKG_007, new MessageLocation("mimetype", 0, 0));
      }
      if (sb.length() != 0)
      {
        report.info(null, FeatureEnum.FORMAT_NAME, sb.toString().trim());
      }
    }
    catch (IOException ignored)
    {
      // missing file will be reported later
    }
    finally
    {
      try
      {
        if (mimetype != null)
        {
          mimetype.close();
        }
      }
      catch (IOException ignored)
      {
        // eat it
      }
    }

    // Validate the OCF files against the schema definitions
    validate(validationVersion);

    // Validate each OPF and keep a reference of the OPFHandler
    List<OPFHandler> opfHandlers = new LinkedList<OPFHandler>();
    for (String opfPath : opfPaths)
    {
      OPFChecker opfChecker;

      if (validationVersion == EPUBVersion.VERSION_2)
      {
        opfChecker = new OPFChecker(ocf, report, opfPath, validationVersion, validationProfile);
      }
      else
      {
        opfChecker = new OPFChecker30(ocf, report, opfPath, validationVersion, profile);
      }
      opfChecker.runChecks();
      opfHandlers.add(opfChecker.getOPFHandler());
    }

		
		
    // Check all file and directory entries in the container
    try
    {
			Set<String> entriesSet = new HashSet<String>();
			Set<String> normalizedEntriesSet = new HashSet<String>();
			for (String entry : ocf.getFileEntries())
      {
				if (!entriesSet.add(entry.toLowerCase(Locale.ENGLISH)))
        {
          report.message(MessageId.OPF_060, new MessageLocation(ocf.getPackagePath(), 0, 0), entry);
        }
        else if (!normalizedEntriesSet.add(Normalizer.normalize(entry, Form.NFC)))
        {
          report.message(MessageId.OPF_061, new MessageLocation(ocf.getPackagePath(), 0, 0), entry);
        }

        ocf.reportMetadata(entry, report);

        if (!entry.startsWith("META-INF/")
            && !entry.startsWith("META-INF\\")
            && !entry.equals("mimetype")
            && !containerHandler.getEntries().contains(entry))
        {
          boolean isDeclared = false;
          for (OPFHandler opfHandler : opfHandlers)
          {
            if (opfHandler.getItemByPath(entry) != null)
            {
              isDeclared = true;
              break;
            }
          }
          if (!isDeclared)
          {
            report.message(MessageId.OPF_003, new MessageLocation(ocf.getName(), -1, -1), entry);
          }
        }
        OCFFilenameChecker.checkCompatiblyEscaped(entry, report, validationVersion);
      }

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
          report.message(MessageId.PKG_014, new MessageLocation(ocf.getName(), -1, -1), directory);
        }
      }
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_015, new MessageLocation(ocf.getName(), -1, -1), e.getMessage());
    }
  }

  boolean validate(EPUBVersion version)
  {
    XMLParser parser;
    InputStream in = null;
    try
    {
      // validate container
      in = ocf.getInputStream(OCFData.containerEntry);
      parser = new XMLParser(ocf, in, OCFData.containerEntry, "xml", report, version);
      XMLHandler handler = new OCFHandler(parser);
      parser.addXMLHandler(handler);
      parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.containerEntry, version)));
      parser.process();
      try
      {
        if (in != null)
        {
          in.close();
          in = null;
        }
      }
      catch (IOException ignored)
      {
        // eat it
      }

      // Validate encryption.xml
      if (ocf.hasEntry(OCFData.encryptionEntry))
      {
        in = ocf.getInputStream(OCFData.encryptionEntry);
        parser = new XMLParser(ocf, in, OCFData.encryptionEntry, "xml", report, version);
        handler = new EncryptionHandler(ocf, parser);
        parser.addXMLHandler(handler);
        parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.encryptionEntry, version)));
        parser.process();
        try
        {
          if (in != null)
          {
            in.close();
            in = null;
          }
        }
        catch (IOException ignored)
        {
          // eat it
        }
        report.info(null, FeatureEnum.HAS_ENCRYPTION, OCFData.encryptionEntry);
      }

      // validate signatures.xml
      if (ocf.hasEntry(OCFData.signatureEntry))
      {
        in = ocf.getInputStream(OCFData.signatureEntry);
        parser = new XMLParser(ocf, in, OCFData.signatureEntry, "xml", report, version);
        handler = new OCFHandler(parser);
        parser.addXMLHandler(handler);
        parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.signatureEntry, version)));
        parser.process();
        try
        {
          in.close();
        }
        catch (Exception ignored)
        {
        }
        report.info(null, FeatureEnum.HAS_SIGNATURES, OCFData.signatureEntry);
      }

    }
    catch (Exception ignored)
    {
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
      catch (IOException ignored)
      {
        // eat it
      }
    }

    return false;
  }

}

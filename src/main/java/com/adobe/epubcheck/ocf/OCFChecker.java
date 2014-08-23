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
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OCFChecker
{
  private final OCFPackage ocf;
  private Report report;
  private final EPUBVersion version;
  // Hashtable encryptedItems;
  // private EPUBVersion version = EPUBVersion.VERSION_3;

  private static final XMLValidator containerValidator = new XMLValidator("schema/20/rng/container.rng");
  private static final XMLValidator encryptionValidator = new XMLValidator("schema/20/rng/encryption.rng");
  private static final XMLValidator signatureValidator = new XMLValidator("schema/20/rng/signatures.rng");
  private static final XMLValidator containerValidator30 = new XMLValidator("schema/30/ocf-container-30.rnc");
  private static final XMLValidator encryptionValidator30 = new XMLValidator("schema/30/ocf-encryption-30.rnc");
  private static final XMLValidator signatureValidator30 = new XMLValidator("schema/30/ocf-signatures-30.rnc");

  private static final HashMap<OPSType, XMLValidator> xmlValidatorMap;

  static
  {
    HashMap<OPSType, XMLValidator> map = new HashMap<OPSType, XMLValidator>();
    map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_2), containerValidator);
    map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_3), containerValidator30);

    map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_2), encryptionValidator);
    map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_3), encryptionValidator30);

    map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_2), signatureValidator);
    map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_3), signatureValidator30);

    xmlValidatorMap = map;
  }

  public OCFChecker(OCFPackage ocf, Report report, EPUBVersion version)
  {
    this.ocf = ocf;
    this.setReport(report);
    this.version = version;
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(OCFData.containerEntry))
    {
      getReport().message(MessageId.RSC_002, new MessageLocation(ocf.getName(), 0, 0));
      return;
    }
    long l = ocf.getTimeEntry(OCFData.containerEntry);
    if (l > 0)
    {
      Date d = new Date(l);
      String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d);
      getReport().info(OCFData.containerEntry, FeatureEnum.CREATION_DATE, formattedDate);
    }
    OCFData containerHandler = ocf.getOcfData(getReport());

    // retrieve the paths of root files
    List<String> opfPaths = containerHandler.getEntries(OPFData.OPF_MIME_TYPE);
    if (opfPaths == null || opfPaths.isEmpty())
    {
      getReport().message(MessageId.RSC_003, new MessageLocation(OCFData.containerEntry, -1, -1));
      return;
    }
    else if (opfPaths.size() > 0)
    {
      if(opfPaths.size() > 1)
      {
        getReport().info(null, FeatureEnum.EPUB_RENDITIONS_COUNT, Integer.toString(opfPaths.size()));
      }

      // test every element for empty or missing @full-path attribute
      // bugfix for issue 236 / issue 95
      int rootfileErrorCounter = 0;
      for (String opfPath : opfPaths)
      {
        if (opfPath == null)
        {
          ++rootfileErrorCounter;
          getReport().message(MessageId.OPF_016, new MessageLocation(OCFData.containerEntry, -1, -1));
        }
        else if (opfPath.isEmpty())
        {
          ++rootfileErrorCounter;
          getReport().message(MessageId.OPF_017, new MessageLocation(OCFData.containerEntry, -1, -1));
         }
        else if (!ocf.hasEntry(opfPath))
        {
          getReport().message(MessageId.OPF_002, new MessageLocation(OCFData.containerEntry, -1, -1), opfPath);
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
    try
    {
      OPFData opfData = ocf.getOpfData(containerHandler, getReport()).get(opfPaths.get(0));
      detectedVersion = opfData.getVersion();
      report.info(null, FeatureEnum.FORMAT_VERSION, detectedVersion.toString());
    }
    catch (InvalidVersionException e)
    {
      getReport().message(MessageId.OPF_001, new MessageLocation(opfPaths.get(0), -1, -1), e.getMessage());
      return;
    }
    catch (IOException ignored)
    {
      // missing file will be reported later
    }

    assert (detectedVersion != null);

    if (version != null && version != detectedVersion)
    {
      getReport().message(MessageId.PKG_001, new MessageLocation(opfPaths.get(0), -1, -1), version, detectedVersion);

      validationVersion = version;
    }
    else
    {
      validationVersion = detectedVersion;
    }

    // EPUB 2.0 says there SHOULD be only one OPS rendition
    if (validationVersion == EPUBVersion.VERSION_2 && opfPaths.size() > 1)
    {
      getReport().message(MessageId.PKG_013, new MessageLocation(OCFData.containerEntry, -1, -1));
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
        getReport().message(MessageId.PKG_007, new MessageLocation("mimetype", 0, 0));
      }
      if (sb.length() != 0)
      {
        getReport().info(null, FeatureEnum.FORMAT_NAME, sb.toString().trim());
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
        opfChecker = new OPFChecker(ocf, getReport(), opfPath, validationVersion);
      }
      else
      {
        opfChecker = new OPFChecker30(ocf, getReport(), opfPath, validationVersion);
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
          getReport().message(MessageId.OPF_060, new MessageLocation(ocf.getPackagePath(), 0, 0), entry);
        }
        else if (!normalizedEntriesSet.add(Normalizer.normalize(entry, Form.NFC)))
        {
          getReport().message(MessageId.OPF_061, new MessageLocation(ocf.getPackagePath(), 0, 0), entry);
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
        OCFFilenameChecker.checkCompatiblyEscaped(entry, getReport(), validationVersion);
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
          getReport().message(MessageId.PKG_014, new MessageLocation(ocf.getName(), -1, -1), directory);
        }
      }
    }
    catch (IOException e)
    {
      getReport().message(MessageId.PKG_015, new MessageLocation(ocf.getName(), -1, -1), e.getMessage());
    }

    Report r = getReport();

  }

  boolean validate(EPUBVersion version)
  {
    XMLParser parser;
    InputStream in = null;
    try
    {
      // validate container
      in = ocf.getInputStream(OCFData.containerEntry);
      parser = new XMLParser(ocf, in, OCFData.containerEntry, "xml", getReport(), version);
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
        parser = new XMLParser(ocf, in, OCFData.encryptionEntry, "xml", getReport(), version);
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
        getReport().info(null, FeatureEnum.HAS_ENCRYPTION, OCFData.encryptionEntry);
      }

      // validate signatures.xml
      if (ocf.hasEntry(OCFData.signatureEntry))
      {
        in = ocf.getInputStream(OCFData.signatureEntry);
        parser = new XMLParser(ocf, in, OCFData.signatureEntry, "xml", getReport(), version);
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
        getReport().info(null, FeatureEnum.HAS_SIGNATURES, OCFData.signatureEntry);
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

  private Report getReport()
  {
    return report;
  }

  private void setReport(Report report)
  {
    this.report = report;
  }
}

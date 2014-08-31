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

package com.adobe.epubcheck.ops;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class OPSChecker implements ContentChecker, DocumentValidator
{

  private OCFPackage ocf;

  private final Report report;

  private final String path;

  private final String mimeType;

  private XRefChecker xrefChecker;

  private final EPUBVersion version;

  private final GenericResourceProvider resourceProvider;

  private final String properties;
  
  private final Set<String> pubTypes;
  
  private static final OPSType XHTML_20 = new OPSType("application/xhtml+xml", EPUBVersion.VERSION_2);
  private static final OPSType XHTML_30 = new OPSType("application/xhtml+xml", EPUBVersion.VERSION_3);
  private static final OPSType SVG_20 = new OPSType("image/svg+xml", EPUBVersion.VERSION_2);
  private static final OPSType SVG_30 = new OPSType("image/svg+xml", EPUBVersion.VERSION_3);


  private ListMultimap<OPSType, XMLValidator> validatorMap;

  private void initEpubValidatorMap()
  {
    ImmutableListMultimap.Builder<OPSType, XMLValidator> builder = ImmutableListMultimap.builder();
    builder.putAll(XHTML_20, XMLValidators.XHTML_20_NVDL.get(), XMLValidators.IDUNIQUE_20_SCH.get())
        .putAll(XHTML_30, XMLValidators.XHTML_30_RNC.get(), XMLValidators.XHTML_30_SCH.get())
        .putAll(SVG_20, XMLValidators.SVG_20_RNG.get(), XMLValidators.IDUNIQUE_20_SCH.get())
        .putAll(SVG_30, XMLValidators.SVG_30_RNC.get(), XMLValidators.SVG_30_SCH.get());
    if (pubTypes.contains(OPFData.DC_TYPE_EDUPUB))
    {
      builder.put(XHTML_30, XMLValidators.XHTML_EDUPUB_HEADINGS_SCH.get());
      builder.put(XHTML_30, XMLValidators.XHTML_EDUPUB_SEMANTICS_SCH.get());
    }
    validatorMap = builder.build();
  }

  public OPSChecker(OCFPackage ocf, Report report, String path,
      String mimeType, String properties, XRefChecker xrefChecker,
      EPUBVersion version, Set<String> pubTypes)
  {
    this.ocf = ocf;
    this.resourceProvider = ocf;
    this.report = report;
    this.path = path;
    this.xrefChecker = xrefChecker;
    this.mimeType = mimeType;
    this.version = version;
    this.properties = properties;
    this.pubTypes = pubTypes;
    initEpubValidatorMap();
  }

  public OPSChecker(String path, String mimeType,
      GenericResourceProvider resourceProvider, Report report,
      EPUBVersion version)
  {
    this.resourceProvider = resourceProvider;
    this.mimeType = mimeType;
    this.report = report;
    this.path = path;
    this.version = version;
    this.properties = "singleFileValidation";
    this.pubTypes = Collections.emptySet();
    initEpubValidatorMap();
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.RSC_001, new MessageLocation(this.ocf.getName(), -1, -1), path);
    }
    else if (!ocf.canDecrypt(path))
    {
      report.message(MessageId.RSC_004, new MessageLocation(this.ocf.getName(), 0, 0), path);
    }
    else
    {
      validate();
    }
  }

  public boolean validate()
  {
    int fatalErrorsSoFar = report.getFatalErrorCount();
    int errorsSoFar = report.getErrorCount();
    int warningsSoFar = report.getWarningCount();
    OPSType type = new OPSType(mimeType, version);
    List<XMLValidator> validators = validatorMap
        .get(type);
    try
    {
      validate(validators);
    }
    catch (IOException e)
    {
      report.message(MessageId.PKG_008, new MessageLocation(path, 0, 0), path);
    }
    return fatalErrorsSoFar == report.getFatalErrorCount()
        && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }

  void validate(List<XMLValidator> validators) throws
      IOException
  {
    InputStream in = null;
    OPSHandler opsHandler;
    try
    {
      in = resourceProvider.getInputStream(path);
      XMLParser opsParser = new XMLParser( ocf,
          in, path, mimeType, report,
          version);

      if (version == EPUBVersion.VERSION_2)
      {
        opsHandler = new OPSHandler(ocf, path, xrefChecker, opsParser, report, version);
      }
      else
      {
        opsHandler = new OPSHandler30(ocf, path, mimeType, properties,
            xrefChecker, opsParser, report, version);
      }

      opsParser.addXMLHandler(opsHandler);
      
      for (XMLValidator validator : validators)
      {
        opsParser.addValidator(validator);
      }

      opsParser.process();
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

      }
    }

  }
}

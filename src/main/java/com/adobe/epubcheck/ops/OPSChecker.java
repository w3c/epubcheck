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
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

public class OPSChecker implements ContentChecker, DocumentValidator
{

  private final ValidationContext context;
  private final Report report;
  private final String path;

  private static final OPSType XHTML_20 = new OPSType("application/xhtml+xml",
      EPUBVersion.VERSION_2);
  private static final OPSType XHTML_30 = new OPSType("application/xhtml+xml",
      EPUBVersion.VERSION_3);
  private static final OPSType SVG_20 = new OPSType("image/svg+xml", EPUBVersion.VERSION_2);
  private static final OPSType SVG_30 = new OPSType("image/svg+xml", EPUBVersion.VERSION_3);

  private ListMultimap<OPSType, XMLValidator> validatorMap;

  private void initEpubValidatorMap()
  {
    ImmutableListMultimap.Builder<OPSType, XMLValidator> builder = ImmutableListMultimap.builder();
    builder
        .putAll(XHTML_20, XMLValidators.XHTML_20_NVDL.get(), XMLValidators.IDUNIQUE_20_SCH.get())
        .putAll(XHTML_30, XMLValidators.XHTML_30_RNC.get(), XMLValidators.XHTML_30_SCH.get())
        .putAll(SVG_20, XMLValidators.SVG_20_RNG.get(), XMLValidators.IDUNIQUE_20_SCH.get())
        .putAll(SVG_30, XMLValidators.SVG_30_RNC.get(), XMLValidators.SVG_30_SCH.get());
    if ((context.profile == EPUBProfile.EDUPUB || context.pubTypes.contains(OPFData.DC_TYPE_EDUPUB))
        && !context.properties.contains(EpubCheckVocab.VOCAB
            .get(EpubCheckVocab.PROPERTIES.NON_LINEAR)))
    {
      builder.put(XHTML_30, XMLValidators.XHTML_EDUPUB_STRUCTURE_SCH.get());
      builder.put(XHTML_30, XMLValidators.XHTML_EDUPUB_SEMANTICS_SCH.get());
    }
    validatorMap = builder.build();
  }

  public OPSChecker(ValidationContext context)
  {
    this.context = context;
    this.path = context.path;
    this.report = context.report;
    initEpubValidatorMap();
  }

  public void runChecks()
  {
    OCFPackage ocf = context.ocf.get();
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.RSC_001, EPUBLocation.create(ocf.getName()), path);
    }
    else if (!ocf.canDecrypt(path))
    {
      report.message(MessageId.RSC_004, EPUBLocation.create(ocf.getName()), path);
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
    OPSType type = new OPSType(context.mimeType, context.version);
    List<XMLValidator> validators = validatorMap.get(type);
    try
    {
      validate(validators);
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(path), path);
    }
    return fatalErrorsSoFar == report.getFatalErrorCount() && errorsSoFar == report.getErrorCount()
        && warningsSoFar == report.getWarningCount();
  }

  void validate(List<XMLValidator> validators)
    throws IOException
  {
    OPSHandler opsHandler;
    XMLParser opsParser = new XMLParser(context);

    if (context.version == EPUBVersion.VERSION_2)
    {
      opsHandler = new OPSHandler(context, opsParser);
    }
    else
    {
      opsHandler = new OPSHandler30(context, opsParser);
    }

    opsParser.addXMLHandler(opsHandler);

    for (XMLValidator validator : validators)
    {
      opsParser.addValidator(validator);
    }

    opsParser.process();

  }
}

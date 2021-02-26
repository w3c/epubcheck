/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

package com.adobe.epubcheck.nav;

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.*;
import static com.google.common.base.Predicates.*;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

public class NavChecker implements ContentChecker, DocumentValidator
{

  @SuppressWarnings("unchecked")
  private final static ValidatorMap validatorMap = ValidatorMap.builder()
      .putAll(XMLValidators.NAV_30_RNC, XMLValidators.XHTML_30_SCH, XMLValidators.NAV_30_SCH)
      .putAll(
          and(Predicates.or(profile(EPUBProfile.EDUPUB), hasPubType(OPFData.DC_TYPE_EDUPUB)),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.FIXED_LAYOUT))),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR)))),
          XMLValidators.XHTML_EDUPUB_STRUCTURE_SCH, XMLValidators.XHTML_EDUPUB_SEMANTICS_SCH,
          XMLValidators.XHTML_IDX_SCH)
      .putAll(
          and(or(profile(EPUBProfile.DICT), hasPubType(OPFData.DC_TYPE_DICT)),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_DICT_SCH)
      .putAll(
          and(or(hasProp(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.INDEX)),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.IN_INDEX_COLLECTION))),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_IDX_SCH, XMLValidators.XHTML_IDX_INDEX_SCH)
      .build();

  private final ValidationContext context;
  private final Report report;
  private final String path;

  public NavChecker(ValidationContext context)
  {
    Preconditions.checkState("application/xhtml+xml".equals(context.mimeType));
    this.context = context;
    this.report = context.report;
    this.path = context.path;
    if (context.version == EPUBVersion.VERSION_2)
    {
      context.report.message(MessageId.NAV_001, EPUBLocation.create(path));
    }
  }

  @Override
  public void runChecks()
  {
    if (!context.ocf.get().hasEntry(path))
    {
      report.message(MessageId.RSC_001, EPUBLocation.create(context.ocf.get().getName()), path);
    }
    else if (!context.ocf.get().canDecrypt(path))
    {
      report.message(MessageId.RSC_004, EPUBLocation.create(context.ocf.get().getName()), path);
    }
    else
    {
      validate();
    }
  }

  public boolean validate()
  {
    int fatalErrors = report.getFatalErrorCount();
    int errors = report.getErrorCount();
    int warnings = report.getWarningCount();
    XMLParser navParser = new XMLParser(context);

    XMLHandler navHandler = new NavHandler(context, navParser);
    navParser.addXMLHandler(navHandler);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      navParser.addValidator(validator);
    }
    navParser.process();

    return ((fatalErrors == report.getFatalErrorCount()) && (errors == report.getErrorCount())
        && (warnings == report.getWarningCount()));
  }
}

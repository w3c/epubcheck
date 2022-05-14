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

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasProp;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasPubType;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.mimetype;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.profile;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

import java.io.IOException;
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.PublicationResourceChecker;
import com.adobe.epubcheck.opf.PublicationType;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Predicates;

public class OPSChecker extends PublicationResourceChecker
{

  private final static ValidatorMap validatorMap = ValidatorMap.builder()
      .putAll(Predicates.and(mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_2)),
          XMLValidators.XHTML_20_NVDL, XMLValidators.XHTML_20_SCH, XMLValidators.IDUNIQUE_20_SCH)
      .putAll(Predicates.and(mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_30_RNC, XMLValidators.XHTML_30_SCH, XMLValidators.SVG_INFORMATIVE_30_NVDL)
      .putAll(Predicates.and(mimetype("image/svg+xml"), version(EPUBVersion.VERSION_2)),
          XMLValidators.SVG_20_NVDL, XMLValidators.IDUNIQUE_20_SCH)
      .putAll(Predicates.and(mimetype("image/svg+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.SVG_30_NVDL, XMLValidators.SVG_INFORMATIVE_30_NVDL)
      .putAll(
          and(or(profile(EPUBProfile.DICT), hasPubType(PublicationType.DICTIONARY)),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_DICT_SCH)
      .putAll(
          and(or(profile(EPUBProfile.EDUPUB), hasPubType(PublicationType.EDUPUB)),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.FIXED_LAYOUT))),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR))),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_EDUPUB_STRUCTURE_SCH, XMLValidators.XHTML_EDUPUB_SEMANTICS_SCH,
          XMLValidators.XHTML_IDX_SCH)
      .putAll(
          and(or(profile(EPUBProfile.IDX), hasPubType(PublicationType.INDEX),
              hasProp(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.INDEX)),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.IN_INDEX_COLLECTION))),
          mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_IDX_SCH, XMLValidators.XHTML_IDX_INDEX_SCH)
      .put(hasProp(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)),
          XMLValidators.XHTML_DATANAV_SCH)
      .build();


  public OPSChecker(ValidationContext context)
  {
    super(context);
  }

  @Override
  protected boolean checkContent()
  {
    List<XMLValidator> validators = validatorMap.getValidators(context);
    try
    {
      validate(validators);
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(context.path), context.path);
    }
    return true;
  }

  void validate(List<XMLValidator> validators)
    throws IOException
  {
    OPSHandler opsHandler;
    XMLParser opsParser = new XMLParser(context);

    if (context.version == EPUBVersion.VERSION_2)
    {
      opsHandler = new OPSHandler(context);
    }
    else
    {
      opsHandler = new OPSHandler30(context);
    }

    opsParser.addContentHandler(opsHandler);

    for (XMLValidator validator : validators)
    {
      opsParser.addValidator(validator);
    }

    opsParser.process();

  }
}

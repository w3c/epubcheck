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

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasProp;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasPubType;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.mimetype;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.profile;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

public class NavChecker extends PublicationResourceChecker
{

  private final static ValidatorMap validatorMap = ValidatorMap.builder()
      .putAll(XMLValidators.NAV_30_RNC, XMLValidators.XHTML_30_SCH, XMLValidators.NAV_30_SCH)
      .putAll(
          and(Predicates.or(profile(EPUBProfile.EDUPUB), hasPubType(PublicationType.EDUPUB)),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.FIXED_LAYOUT))),
              not(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR)))),
          XMLValidators.XHTML_EDUPUB_STRUCTURE_SCH, XMLValidators.XHTML_EDUPUB_SEMANTICS_SCH,
          XMLValidators.XHTML_IDX_SCH)
      .putAll(
          and(or(profile(EPUBProfile.DICT), hasPubType(PublicationType.DICTIONARY)),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_DICT_SCH)
      .putAll(
          and(or(hasProp(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.INDEX)),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.IN_INDEX_COLLECTION))),
              mimetype("application/xhtml+xml"), version(EPUBVersion.VERSION_3)),
          XMLValidators.XHTML_IDX_SCH, XMLValidators.XHTML_IDX_INDEX_SCH)
      .build();


  public NavChecker(ValidationContext context)
  {
    super(context);
    Preconditions.checkState("application/xhtml+xml".equals(context.mimeType));
    if (context.version == EPUBVersion.VERSION_2)
    {
      context.report.message(MessageId.NAV_001, EPUBLocation.create(context.path));
    }
  }

  @Override
  protected boolean checkContent()
  {
    XMLParser navParser = new XMLParser(context);
    
    XMLHandler navHandler = new NavHandler(context, navParser);
    navParser.addXMLHandler(navHandler);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      navParser.addValidator(validator);
    }
    navParser.process();
    return true;
  }

}

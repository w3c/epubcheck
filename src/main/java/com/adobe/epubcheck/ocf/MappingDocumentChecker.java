package com.adobe.epubcheck.ocf;

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasProp;

import org.w3c.epubcheck.core.AbstractChecker;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;

class MappingDocumentChecker extends AbstractChecker
{

  private static final ValidatorMap validatorMap = ValidatorMap.builder()
      .putAll(hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.RENDITION_MAPPING)),
          XMLValidators.RENDITION_MAPPING_RNC, XMLValidators.RENDITION_MAPPING_SCH)
      .build();

  public MappingDocumentChecker(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void check()
  {
    XMLParser parser = new XMLParser(context);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      parser.addValidator(validator);
    }
    parser.process();

  }

}

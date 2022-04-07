package com.adobe.epubcheck.ocf;

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasProp;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.path;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.profile;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;

import org.w3c.epubcheck.core.AbstractChecker;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Predicates;

class OCFMetaFileChecker extends AbstractChecker
{

  private static final ValidatorMap validatorMap = ValidatorMap.builder()
      .put(Predicates.and(path(OCFMetaFile.CONTAINER.asPath()), version(EPUBVersion.VERSION_2)),
          XMLValidators.CONTAINER_20_RNG)
      .putAll(Predicates.and(path(OCFMetaFile.CONTAINER.asPath()), version(EPUBVersion.VERSION_3)),
          XMLValidators.CONTAINER_30_RNC, XMLValidators.CONTAINER_30_RENDITIONS_SCH)

      .put(Predicates.and(path(OCFMetaFile.SIGNATURES.asPath()), version(EPUBVersion.VERSION_2)),
          XMLValidators.SIG_20_RNG)
      .put(Predicates.and(path(OCFMetaFile.SIGNATURES.asPath()), version(EPUBVersion.VERSION_3)),
          XMLValidators.SIG_30_RNC)
      .put(
          Predicates.and(path(OCFMetaFile.METADATA.asPath()),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION))),
          XMLValidators.META_30_RNC)
      .put(
          Predicates.and(path(OCFMetaFile.METADATA.asPath()),
              hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION))),
          XMLValidators.META_30_SCH)
      .put(Predicates.and(path(OCFMetaFile.METADATA.asPath()),
          hasProp(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.MULTIPLE_RENDITION)),
          profile(EPUBProfile.EDUPUB)), XMLValidators.META_EDUPUB_SCH)
      .build();

  public OCFMetaFileChecker(ValidationContext context)
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

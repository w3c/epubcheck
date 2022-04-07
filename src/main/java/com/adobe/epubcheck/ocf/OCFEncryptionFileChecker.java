package com.adobe.epubcheck.ocf;

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;

import org.w3c.epubcheck.core.AbstractChecker;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;

final class OCFEncryptionFileChecker extends AbstractChecker
{
  private static final ValidatorMap validatorMap = ValidatorMap.builder()
      .putAll(version(EPUBVersion.VERSION_3), XMLValidators.ENC_30_RNC, XMLValidators.ENC_30_SCH)
      .put(version(EPUBVersion.VERSION_2), XMLValidators.ENC_20_RNG).build();

  private final OCFCheckerState state;

  public OCFEncryptionFileChecker(ValidationContext context, OCFCheckerState state)
  {
    super(context);
    this.state = state;
  }

  @Override
  public void check()
  {
    XMLParser parser = new XMLParser(context);
    OCFEncryptionFileHandler handler = new OCFEncryptionFileHandler(context, state);
    parser.addContentHandler(handler);
    for (XMLValidator validator : validatorMap.getValidators(context))
    {
      parser.addValidator(validator);
    }
    parser.process();
  }

}

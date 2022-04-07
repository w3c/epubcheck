package com.adobe.epubcheck.ocf;

import org.w3c.epubcheck.core.AbstractChecker;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.XMLParser;

final class OCFContainerFileChecker extends AbstractChecker
{

  private final OCFCheckerState state;

  public OCFContainerFileChecker(ValidationContext context, OCFCheckerState state)
  {
    super(context);
    this.state = state;
  }

  @Override
  public void check()
  {
    XMLParser parser = new XMLParser(context);
    OCFContainerFileHandler handler = new OCFContainerFileHandler(context, state);
    parser.addContentHandler(handler);
    parser.process();
  }

}

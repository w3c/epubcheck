package com.adobe.epubcheck.xml;

import org.xml.sax.Locator;
import org.xml.sax.ext.Locator2;

class DocumentLocatorImpl implements Locator2
{

  private final Locator locator;
  private Locator2 locator2 = null;

  public DocumentLocatorImpl(Locator locator)
  {
    this.locator = locator;
    if (locator instanceof Locator2)
    {
      locator2 = (Locator2) locator;
    }
  }

  @Override
  public int getColumnNumber()
  {
    return locator.getColumnNumber();
  }

  @Override
  public int getLineNumber()
  {
    return locator.getLineNumber();
  }

  @Override
  public String getPublicId()
  {
    return locator.getPublicId();
  }

  @Override
  public String getSystemId()
  {
    return locator.getSystemId();
  }

  @Override
  public String getEncoding()
  {
    if (locator2 != null)
    {
      return locator2.getEncoding();
    }
    return null;
  }

  @Override
  public String getXMLVersion()
  {
    if (locator2 != null)
    {
      return locator2.getXMLVersion();
    }
    return null;
  }

}

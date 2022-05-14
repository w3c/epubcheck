package com.adobe.epubcheck.xml.handlers;

import org.xml.sax.Locator;
import org.xml.sax.ext.Locator2;

public class DelegateLocator implements Locator2
{

  private final Locator locator;

  public DelegateLocator(Locator locator)
  {
    this.locator = locator;
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
    if (locator instanceof Locator2)
    {
      return ((Locator2) locator).getEncoding();
    }
    return null;
  }

  @Override
  public String getXMLVersion()
  {
    if (locator instanceof Locator2)
    {
      return ((Locator2) locator).getXMLVersion();
    }
    return null;
  }

}

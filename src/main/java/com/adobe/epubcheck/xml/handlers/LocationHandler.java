package com.adobe.epubcheck.xml.handlers;

import org.xml.sax.Locator;
import org.xml.sax.ext.DefaultHandler2;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.opf.ValidationContext;

public class LocationHandler extends DefaultHandler2
{
  public static EPUBLocation location(ValidationContext context, Locator locator)
  {
    if (locator == null)
    {
      return EPUBLocation.of(context);
    }
    else
    {
      return EPUBLocation.of(context).at(locator.getLineNumber(), locator.getColumnNumber());
    }
  }

  private final ValidationContext context;
  private Locator locator;

  public LocationHandler(ValidationContext context)
  {
    this.context = context;
  }

  @Override
  public void setDocumentLocator(Locator locator)
  {
    this.locator = new DelegateLocator(locator);
  }

  protected final EPUBLocation location()
  {
    return location(context, locator);
  }
}

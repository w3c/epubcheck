package com.adobe.epubcheck.xml.handlers;

import javax.xml.XMLConstants;

import org.w3c.epubcheck.core.references.URLChecker;
import org.xml.sax.Attributes;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EpubConstants;
import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

public abstract class BaseURLHandler extends LocationHandler
{

  private URL baseURL;
  private final URLChecker urlChecker;

  public BaseURLHandler(ValidationContext context)
  {
    this(context, context.url);
  }

  public BaseURLHandler(ValidationContext context, URL baseURL)
  {
    super(context);
    this.urlChecker = new URLChecker(context, baseURL);
    this.baseURL = Preconditions.checkNotNull(baseURL);
  }

  protected final URL baseURL()
  {
    return baseURL;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
  {
    String newBase = null;
    // In HTML, `base` element sets a new base URL
    if (EpubConstants.HtmlNamespaceUri.equals(uri) && "base".equals(localName))
    {
      newBase = attributes.getValue("", "href");
    }
    // In XML, `xml:base` attribute sets a new base URL
    else
    {
      newBase = attributes.getValue(XMLConstants.XML_NS_URI, "base");
    }
    // Update the base if a new one was found
    if (newBase != null)
    {
      baseURL = urlChecker.setBase(newBase, location());
    }

  }

  protected final URL checkURL(String string)
  {
    return urlChecker.checkURL(string, location());
  }
}

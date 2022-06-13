package com.adobe.epubcheck.xml.handlers;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.PathUtil;

public class BaseURLHandler extends LocationHandler
{

  private String baseURL;
  private Report report;

  public BaseURLHandler(ValidationContext context)
  {
    super(context);
    this.baseURL = context.path;
    this.report = context.report;
  }

  protected final String baseURL()
  {
    return baseURL;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
  {
    // In HTML, `base` element sets a new base URL
    if (EpubConstants.HtmlNamespaceUri.equals(uri) && "base".equals(localName))
    {
      setBase(attributes.getValue("", "href"));
    }
    // In XML, `xml:base` attribute sets a new base URL
    else
    {
      setBase(attributes.getValue(XMLConstants.XML_NS_URI, "base"));
    }

  }

  private void setBase(String newBase)
  {
    if (newBase == null) return;
    try
    {
      new URI(newBase);
    } catch (URISyntaxException e)
    {
      report.message(MessageId.RSC_020, location(), newBase);
      return;
    }
    baseURL = PathUtil.resolveRelativeReference(baseURL, newBase);

  }
}

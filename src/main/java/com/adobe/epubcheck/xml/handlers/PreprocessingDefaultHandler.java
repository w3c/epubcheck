package com.adobe.epubcheck.xml.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.xml.HTMLUtils;
import com.adobe.epubcheck.xml.Namespaces;
import com.google.common.collect.ImmutableSet;

public final class PreprocessingDefaultHandler extends WrappingDefaultHandler
{

  private static final Set<String> KNOWN_XHTML_NAMESPACES = ImmutableSet.of(Namespaces.XHTML,
      Namespaces.XML, Namespaces.OPS, Namespaces.SVG, Namespaces.MATHML, Namespaces.SSML,
      Namespaces.XMLEVENTS, Namespaces.XLINK);

  private static boolean isDataAttribute(Attributes attributes, int index)
  {
    return "".equals(attributes.getURI(index))
        && attributes.getLocalName(index).startsWith("data-");
  }

  private static boolean isHTMLCustomNamespace(String namespace)
  {
    if (namespace == null || namespace.trim().isEmpty()) return false;
    return !KNOWN_XHTML_NAMESPACES.contains(namespace.trim());
  }

  private static String findReservedStringInHTMLCustomNamespace(String namespace)
  {
    if (namespace != null)
    {
      try
      {
        URI uri = new URI(namespace);
        if (uri.getHost().contains("w3.org")) return "w3.org";
        if (uri.getHost().contains("idpf.org")) return "idpf.org";
      } catch (URISyntaxException e)
      {
        // ignore
      }
    }
    return null;
  }

  private static boolean isCaseInsensitiveAttribute(Attributes attributes, int index)
  {
    return (attributes.getURI(index).isEmpty()
        && HTMLUtils.isCaseInsensitiveAttribute(attributes.getLocalName(index)));
  }

  private final ValidationContext context;
  private Locator locator;

  public PreprocessingDefaultHandler(DefaultHandler handler, ValidationContext context)
  {
    super(handler);
    this.context = context;
  }

  @Override
  public void setDocumentLocator(Locator locator)
  {
    this.locator = locator;
    super.setDocumentLocator(locator);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    super.startElement(preprocessNamespace(uri, localName), localName, qName,
        preprocessAttributes(uri, localName, attributes));
  }

  private String preprocessNamespace(String uri, String localName)
  {
    if (context.version == EPUBVersion.VERSION_3 && "application/xhtml+xml".equals(context.mimeType)
        && HTMLUtils.isCustomElement(uri, localName))
    {
      // Pre-process HTML custom elements to set them in the proprietary
      // namespace supported by the Nu Html Checker
      return "http://n.validator.nu/custom-elements/";
    }
    return uri;
  }

  private Attributes preprocessAttributes(String uri, String localName, Attributes atts)
  {
    AttributesImpl attributes = new AttributesImpl(atts);
    try
    {
      for (int i = attributes.getLength() - 1; i >= 0; i--)
      {
        if (context.version == EPUBVersion.VERSION_3)
        {
          String attrNamespace = attributes.getURI(i);
          // Remove data-* attributes in both XHTML and SVG
          if (isDataAttribute(attributes, i))
          {
            attributes.removeAttribute(i);
          }
          // Remove custom namespace attributes in XHTML
          else if ("application/xhtml+xml".equals(context.mimeType)
              && isHTMLCustomNamespace(attrNamespace))
          {
            String reserved = findReservedStringInHTMLCustomNamespace(attrNamespace);
            if (reserved != null)
            {
              context.report.message(MessageId.HTM_054, LocationHandler.location(context, locator),
                  attrNamespace, reserved);
            }
            attributes.removeAttribute(i);
          }
          // Normalize case of case-insensitive attributes in XHTML
          else if ("application/xhtml+xml".equals(context.mimeType) && Namespaces.XHTML.equals(uri)
              && isCaseInsensitiveAttribute(attributes, i))
          {
            attributes.setValue(i, attributes.getValue(i).toLowerCase(Locale.ENGLISH));
          }
        }
      }
    } catch (Exception e)
    {
      throw new IllegalStateException("Unexpected error when pre-processing attributes", e);
    }
    return attributes;
  }

}

package com.adobe.epubcheck.ocf;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.xml.SAXAbortException;

import io.mola.galimatias.URL;

class PackageDocumentPeekerHandler extends DefaultHandler
{

  private static final String VERSION_3 = "3.0";
  private static final String VERSION_2 = "2.0";

  private final URL documentURL;
  private final OCFCheckerState state;
  private boolean isPackageRoot = false;
  private String currentText = null;
  private String uniqueId = null;
  private boolean isUniqueId = false;

  public PackageDocumentPeekerHandler(ValidationContext context, OCFCheckerState state)
  {
    this.state = state;
    this.documentURL = context.url;
    state.errorReset();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
  {
    if ("package".equals(localName))
    {
      processPackage(attributes);
      isPackageRoot = true;
    }
    else if (!isPackageRoot)
    {
      state.addError(InvalidVersionException.PACKAGE_ELEMENT_NOT_FOUND);
      throw new SAXAbortException();
    }
    else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
    {
      currentText = "";
    }
    else if ("identifier".equals(localName) && EpubConstants.DCElements.equals(uri))
    {
      String id = attributes.getValue("id");
      isUniqueId = id != null && id.trim().equals(uniqueId);
      if (isUniqueId)
      {
        currentText = "";
      }
    }
  }

  @Override
  public void characters(char[] ch, int start, int length)
    throws SAXException
  {
    if (currentText != null)
    {
      currentText += String.valueOf(ch, start, length);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName)
    throws SAXException
  {
    if ("metadata".equals(localName) || "package".equals(localName))
    {
      throw new SAXAbortException();
    }
    else if ("type".equals(localName) && EpubConstants.DCElements.equals(uri))
    {
      currentText = currentText.trim();
      if (currentText.length() > 0) state.addType(documentURL, currentText);
      currentText = null;
    }
    else if (isUniqueId && "identifier".equals(localName) && EpubConstants.DCElements.equals(uri))
    {
      currentText = currentText.trim();
      if (currentText.length() > 0) state.addUniqueId(documentURL, currentText);
      isUniqueId = false;
      currentText = null;
    }
  }

  private void processPackage(Attributes attributes)
    throws SAXException
  {
    String version = attributes.getValue("version");
    if (version == null)
    {

      state.addError(InvalidVersionException.VERSION_ATTRIBUTE_NOT_FOUND);
      throw new SAXAbortException();
    }
    else if (VERSION_3.equals(version))
    {
      state.addVersion(documentURL, EPUBVersion.VERSION_3);
    }
    else if (VERSION_2.equals(version))
    {
      state.addVersion(documentURL, EPUBVersion.VERSION_2);
    }
    else
    {

      state.addError(InvalidVersionException.UNSUPPORTED_VERSION);
      throw new SAXAbortException();
    }
    String uniqueId = attributes.getValue("unique-identifier");
    if (uniqueId != null) this.uniqueId = uniqueId;
  }
}
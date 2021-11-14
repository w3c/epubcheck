/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ResourceUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closer;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;

public class XMLParser extends DefaultHandler implements LexicalHandler, DeclHandler
{

  private static final class UsageErrorHandler implements ErrorHandler
  {
    private final ValidationContext context;

    public UsageErrorHandler(ValidationContext context)
    {
      this.context = context;
    }

    @Override
    public void warning(SAXParseException exception)
      throws SAXException
    {
      context.report.message(MessageId.RSC_024,
          EPUBLocation.create(context.path, exception.getLineNumber(), exception.getColumnNumber()),
          exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception)
      throws SAXException
    {
      context.report.message(MessageId.RSC_025,
          EPUBLocation.create(context.path, exception.getLineNumber(), exception.getColumnNumber()),
          exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception)
      throws SAXException
    {
      context.report.message(MessageId.RSC_016,
          EPUBLocation.create(context.path, exception.getLineNumber(), exception.getColumnNumber()),
          exception.getMessage());

    }

  }

  private static final String SAXPROP_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
  private static final String SAXPROP_DECL_HANDLER = "http://xml.org/sax/properties/declaration-handler";
  private SAXParser parser;
  private final ValidationContext context;
  private final Report report;
  private final String path;
  private final Vector<XMLHandler> contentHandlers = new Vector<XMLHandler>();
  private XMLElement currentElement;
  private final Vector<ContentHandler> validatorContentHandlers = new Vector<ContentHandler>();
  private final Vector<DTDHandler> validatorDTDHandlers = new Vector<DTDHandler>();
  private final Vector<LexicalHandler> validatorLexicalHandlers = new Vector<LexicalHandler>();
  private final Vector<DeclHandler> validatorDeclHandlers = new Vector<DeclHandler>();
  private Locator2 documentLocator;
  private static final String zipRoot = "file:///epub-root/";
  private static final Hashtable<String, String> systemIdMap;
  private final HashSet<String> entities = new HashSet<String>();
  private boolean firstStartDTDInvocation = true;

  public XMLParser(ValidationContext context)
  {
    this.context = context;
    this.report = context.report;
    this.path = context.path;

    // XML predefined
    entities.add("gt");
    entities.add("lt");
    entities.add("amp");
    entities.add("quot");
    entities.add("apos");

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);


    try
    {
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      if (context.version == EPUBVersion.VERSION_3)
      {
        factory.setXIncludeAware(false);
      }
    } catch (Exception ignored)
    {
    }

    try
    {
      parser = factory.newSAXParser();

      XMLReader reader = parser.getXMLReader();
      reader.setDTDHandler(this);
      reader.setContentHandler(this);
      reader.setEntityResolver(this);
      reader.setErrorHandler(this);

      try
      {
        reader.setProperty(SAXPROP_LEXICAL_HANDLER, this);
        reader.setProperty(SAXPROP_DECL_HANDLER, this);
      } catch (SAXNotRecognizedException e)
      {
        e.printStackTrace();
      } catch (SAXNotSupportedException e)
      {
        e.printStackTrace();
      }
    } catch (ParserConfigurationException e)
    {
      e.printStackTrace();
    } catch (SAXException e)
    {
      e.printStackTrace();
    }
  }

  public void addXMLHandler(XMLHandler handler)
  {
    if (handler != null)
    {
      contentHandlers.add(handler);
    }
  }

  public void addValidator(XMLValidator xv)
  {
    PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
    ErrorHandler eh = xv.isNormative() ? this : new UsageErrorHandler(context);
    propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER, eh);
    Validator validator = xv.getSchema().createValidator(propertyMapBuilder.toPropertyMap());
    ContentHandler contentHandler = validator.getContentHandler();
    if (contentHandler != null)
    {
      validatorContentHandlers.add(contentHandler);
    }
    DTDHandler dtdHandler = validator.getDTDHandler();
    if (dtdHandler != null)
    {
      validatorDTDHandlers.add(dtdHandler);
    }
  }

  public void addDeclHandler(DeclHandler handler)
  {
    if (handler != null)
    {
      validatorDeclHandlers.add(handler);
    }
  }

  public void addLexicalHandler(LexicalHandler handler)
  {
    if (handler != null)
    {
      validatorLexicalHandlers.add(handler);
    }
  }

  public void process()
  {
    try
    {
      Closer closer = Closer.create();
      try
      {
        InputStream in = closer.register(context.resourceProvider.getInputStream(path));
        if (in == null) {
          return; // Abort processing. Missing required files are reported elsewhere.
        }
        // System.err.println("DEBUG XMLParser#process on" + resource);
        if (!in.markSupported())
        {
          in = new BufferedInputStream(in);
        }

        String encoding = sniffEncoding(in);
        if (encoding != null && !encoding.equals("UTF-8") && !encoding.equals("UTF-16"))
        {
          report.message(MessageId.CSS_003, EPUBLocation.create(path, ""), encoding);
        }

        InputSource ins = new InputSource(in);
        ins.setSystemId(zipRoot + path);
        parser.parse(ins, this);

      } catch (Throwable e)
      {
        // ensure that any checked exception types other than IOException that
        // could be thrown are
        // provided here, e.g. throw closer.rethrow(e,
        // CheckedException.class);
        // throw closer.rethrow(e);
        throw closer.rethrow(e, SAXException.class);
      } finally
      {
        closer.close();
      }
    } catch (FileNotFoundException e)
    {
      String message = e.getMessage();
      message = new File(message).getName();
      int p = message.indexOf("(");
      if (p > 0)
      {
        message = message.substring(0, message.indexOf("("));
      }
      message = message.trim();
      report.message(MessageId.RSC_001, EPUBLocation.create(path), message);
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(path), path);
    } catch (IllegalArgumentException e)
    {
      report.message(MessageId.RSC_005, EPUBLocation.create(path), e.getMessage());
    } catch (SAXException e)
    {
      report.message(MessageId.RSC_005, EPUBLocation.create(path), e.getMessage());
    }
  }

  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException,
    IOException
  {
    // if (systemId.startsWith(zipRoot))
    // {
    // InputStream inStream =
    // this.thePackage.getInputStream(systemId.substring(zipRoot.length()));
    // if (inStream != null)
    // {
    // InputSource source = new InputSource(inStream);
    // source.setPublicId(publicId);
    // source.setSystemId(systemId);
    // return source;
    // }
    // }
    // outWriter.println("DEBUG XMLParser#resolveEntity ==> "+ publicId + ", " +
    // systemId + ", " );

    String resourcePath = systemIdMap.get(systemId);

    if (resourcePath != null)
    {
      InputStream resourceStream = ResourceUtil.getResourceStream(resourcePath);
      InputSource source = new InputSource(resourceStream);
      source.setPublicId(publicId);
      source.setSystemId(systemId);
      return source;
    }
    else if (systemId.equals("about:legacy-compat"))
    {
      // special case
      return new InputSource(new StringReader(""));

    }
    else
    {
      // check for a system prop that turns off online fetching
      // the default is to attempt online fetching, as this has been the default
      // forever
      boolean offline = Boolean.parseBoolean(System.getProperty("epubcheck.offline"));
      // outWriter.println("offline value is " + offline);
      if (systemId.startsWith("http:") && offline)
      {
        return new InputSource(new StringReader(""));
      }
      // else return null and let the caller try to fetch the goods
      return null;
    }
  }

  public void notationDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    int len = validatorDTDHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (validatorDTDHandlers.elementAt(i)).notationDecl(name, publicId, systemId);
    }
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    throws SAXException
  {
    int len = validatorDTDHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (validatorDTDHandlers.elementAt(i))
          .unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  public void error(SAXParseException ex)
    throws SAXException
  {
    String message = ex.getMessage().trim();
    if (message != null && message.startsWith("WARNING:"))
    {
      report.message(MessageId.RSC_017,
          EPUBLocation.create(path, ex.getLineNumber(), ex.getColumnNumber()),
          message.substring(9, message.length()));
    }
    else
    {
      report.message(MessageId.RSC_005,
          EPUBLocation.create(path, ex.getLineNumber(), ex.getColumnNumber()),
          message);
    }
  }

  public void fatalError(SAXParseException ex)
    throws SAXException
  {
    report.message(MessageId.RSC_016,
        EPUBLocation.create(path, ex.getLineNumber(), ex.getColumnNumber()),
        ex.getMessage());
  }

  public void warning(SAXParseException ex)
    throws SAXException
  {
    report.message(MessageId.RSC_017,
        EPUBLocation.create(path, ex.getLineNumber(), ex.getColumnNumber()),
        ex.getMessage());
  }

  public void characters(char[] arg0, int arg1, int arg2)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).characters(arg0, arg1, arg2);
    }

    int len = contentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (contentHandlers.elementAt(i)).characters(arg0, arg1, arg2);
    }
  }

  public void endDocument()
    throws SAXException
  {
    int len = validatorContentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (validatorContentHandlers.elementAt(i)).endDocument();
    }
  }

  public void endElement(String arg0, String arg1, String arg2)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).endElement(arg0, arg1, arg2);
    }
    int len = contentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (contentHandlers.elementAt(i)).endElement();
    }
    currentElement = currentElement.getParent();
  }

  public void endPrefixMapping(String arg0)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).endPrefixMapping(arg0);
    }
  }

  public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).ignorableWhitespace(arg0, arg1, arg2);
    }
    int len = contentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (contentHandlers.elementAt(i)).ignorableWhitespace(arg0, arg1, arg2);
    }
  }

  public void processingInstruction(String arg0, String arg1)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).processingInstruction(arg0, arg1);
    }
    int len = contentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (contentHandlers.elementAt(i)).processingInstruction(arg0, arg1);
    }
  }

  public void setDocumentLocator(Locator locator)
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).setDocumentLocator(locator);
    }
    documentLocator = new DocumentLocatorImpl(locator);
  }

  public void skippedEntity(String arg0)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).skippedEntity(arg0);
    }
  }

  public void startDocument()
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).startDocument();
    }
  }

  public void startElement(String namespaceURI, String localName, String qName,
      Attributes parsedAttribs)
    throws SAXException
  {
    Attributes attribs = preprocessAttributes(namespaceURI, localName, qName, parsedAttribs);
    
    if ("application/xhtml+xml".equals(context.mimeType)
        && context.version == EPUBVersion.VERSION_3) {
      // Pre-process HTML custom elements to set them in the proprietary namespace supported
      // by the Nu Html Checker
      if (HTMLUtils.isCustomElement(namespaceURI, localName)) {
        namespaceURI = "http://n.validator.nu/custom-elements/";
      }
    }

    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).startElement(namespaceURI, localName, qName, attribs);
    }
    int index = qName.indexOf(':');
    String prefix;
    String name;
    if (index < 0)
    {
      prefix = null;
      name = qName;
    }
    else
    {
      prefix = qName.substring(0, index);
      name = qName.substring(index + 1);
    }
    int count = attribs.getLength();
    XMLAttribute[] attributes = count == 0 ? null : new XMLAttribute[count];
    for (int i = 0; i < count; i++)
    {
      String attName = attribs.getLocalName(i);
      String attNamespace = attribs.getURI(i);
      String attQName = attribs.getQName(i);
      int attIndex = attQName.indexOf(':');
      String attPrefix;
      if (attIndex < 0)
      {
        attPrefix = null;
        attNamespace = null;
      }
      else
      {
        attPrefix = attQName.substring(0, attIndex);
      }
      String attValue = attribs.getValue(i);
      assert attributes != null;
      attributes[i] = new XMLAttribute(attNamespace, attPrefix, attName, attValue);
    }
    currentElement = new XMLElement(namespaceURI, prefix, name, attributes, currentElement);
    int len = contentHandlers.size();
    for (int i = 0; i < len; i++)
    {
      (contentHandlers.elementAt(i)).startElement();
    }
  }

  private Attributes preprocessAttributes(String elemNamespace, String elemName, String elemQName,
      Attributes originalAttribs)
  {
    AttributesImpl attributes = new AttributesImpl(originalAttribs);
    try
    {
      for (int i = attributes.getLength() - 1; i >= 0; i--)
      {
        if (context.version == EPUBVersion.VERSION_3)
        {
          // Remove data-* attributes in both XHTML and SVG
          if (isDataAttribute(attributes, i))
          {
            attributes.removeAttribute(i);
          }
          // Remove custom namespace attributes in XHTML
          else if ("application/xhtml+xml".equals(context.mimeType)
              && isHTMLCustomNamespace(attributes.getURI(i)))
          {
            attributes.removeAttribute(i);
          }
          // Normalize case of case-insensitive attributes in XHTML
          else if ("application/xhtml+xml".equals(context.mimeType)
              && Namespaces.XHTML.equals(elemNamespace)
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

  private static boolean isDataAttribute(Attributes attributes, int index)
  {
    return "".equals(attributes.getURI(index))
        && attributes.getLocalName(index).startsWith("data-");
  }

  private static final Set<String> KNOWN_XHTML_NAMESPACES = ImmutableSet.of(Namespaces.XHTML,
      Namespaces.XML, Namespaces.OPS, Namespaces.SVG, Namespaces.MATHML, Namespaces.SSML,
      Namespaces.XMLEVENTS, Namespaces.XLINK);

  private static boolean isHTMLCustomNamespace(String namespace)
  {
    if (namespace == null || namespace.trim().isEmpty()) return false;
    return !KNOWN_XHTML_NAMESPACES.contains(namespace.trim());
  }

  private static boolean isCaseInsensitiveAttribute(Attributes attributes, int index)
  {
    return (attributes.getURI(index).isEmpty()
        && HTMLUtils.isCaseInsensitiveAttribute(attributes.getLocalName(index)));
  }

  public void startPrefixMapping(String arg0, String arg1)
    throws SAXException
  {
    int vlen = validatorContentHandlers.size();
    for (int i = 0; i < vlen; i++)
    {
      (validatorContentHandlers.elementAt(i)).startPrefixMapping(arg0, arg1);
    }
  }

  public void comment(char[] text, int arg1, int arg2)
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.comment(text, arg1, arg2);
      }
    }
  }

  public void endCDATA()
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.endCDATA();
      }
    }
  }

  public void endDTD()
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.endDTD();
      }
    }
  }

  public void endEntity(String ent)
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.endEntity(ent);
      }
    }
  }

  public void startCDATA()
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.startCDATA();
      }
    }
  }

  public void startDTD(String root, String publicId, String systemId)
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.startDTD(root, publicId, systemId);
      }
    }

    handleDocTypeUserInfo(root, publicId, systemId);
  }

  private void handleDocTypeUserInfo(String root, String publicId, String systemId)
  {
    final String mimeType = context.mimeType;
    // outWriter.println("DEBUG doctype ==> "+ root + ", " + publicId + ", " +
    // systemId + ", " );

    // for modular DTDs etc, just issue a warning for the top level IDs.
    if (!firstStartDTDInvocation)
    {
      return;
    }

    if (context.version == EPUBVersion.VERSION_2)
    {

      if (mimeType != null && "application/xhtml+xml".equals(mimeType) && root.equals("html"))
      {
        // OPS 2.0(.1)
        String complete = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n"
            + "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";

        if (matchDoctypeId("-//W3C//DTD XHTML 1.1//EN", publicId, complete))
        {
          matchDoctypeId("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", systemId, complete);
        }

      }

      if (mimeType != null && "opf".equals(mimeType) && (publicId != null || systemId != null))
      {

        // 1.2: <!DOCTYPE package PUBLIC
        // "+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN"
        // "http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd">
        // http://http://idpf.org/dtds/oeb-1.2/oebpkg12.dtd
        if ("package".equals(root)
            && (publicId == null || publicId
                .equals("+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN"))
            && (systemId == null || systemId
                .equals("http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd")))
        {
          // for heritage content collections, dont warn about this, as its not
          // explicitly forbidden by the spec
        }
        else
        {
          report.message(MessageId.HTM_009, EPUBLocation.create(path));
        }

      }

      if (mimeType != null && "application/x-dtbncx+xml".equals(mimeType))
      {
        String complete = "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" "
            + "\n \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">";
        if (matchDoctypeId("-//NISO//DTD ncx 2005-1//EN", publicId, complete))
        {
          matchDoctypeId("http://www.daisy.org/z3986/2005/ncx-2005-1.dtd", systemId, complete);
        }
      }

    }
    else if (context.version == EPUBVersion.VERSION_3)
    {
      if (mimeType != null && "application/xhtml+xml".equals(mimeType)
          && "html".equalsIgnoreCase(root))
      {
        String complete = "<!DOCTYPE html>";
        // warn for obsolete or unknown doctypes
        if (publicId == null && (systemId == null || systemId.equals("about:legacy-compat")))
        {
          // we assume to have have <!DOCTYPE html> or <!DOCTYPE html SYSTEM
          // "about:legacy-compat">
        }
        else
        {
          report.message(MessageId.HTM_004, EPUBLocation.create(path), publicId, complete);
        }
      }
      else if (publicId != null || systemId != null)
      {
        report.message(MessageId.OPF_073, getLocation());
      }
    }

    firstStartDTDInvocation = false;
  }

  boolean checkDTD(String expectedPublicId, String expectedSystemId, String actualPublicId,
      String actualSystemId)
  {
    if ((actualPublicId == null || (actualPublicId != null && expectedPublicId
        .equalsIgnoreCase(actualPublicId)))
        && (actualSystemId == null || (actualSystemId != null && expectedSystemId
            .equalsIgnoreCase(actualSystemId))))
    {
      return true;
    }
    return false;
  }

  boolean matchDoctypeId(String expected, String given, String messageParam)
  {
    if (!expected.equals(given))
    {
      report.message(MessageId.HTM_004, EPUBLocation.create(path), given==null?"":given, messageParam);
      return false;
    }
    return true;
  }

  public void startEntity(String ent)
    throws SAXException
  {
    if (validatorLexicalHandlers.size() > 0)
    {
      for (LexicalHandler h : this.validatorLexicalHandlers)
      {
        h.startEntity(ent);
      }
    }
    if (!entities.contains(ent) && !ent.equals("[dtd]"))
    {
      // This message may never be reported. Undeclared entities result in a Sax
      // Parser Error and message RSC_005.
      report.message(MessageId.HTM_011,
          EPUBLocation.create(path, getLineNumber(), getColumnNumber(), ent));
    }
  }

  public void attributeDecl(String name, String name2, String type, String mode, String value)
    throws SAXException
  {
    if (validatorDeclHandlers.size() > 0)
    {
      for (DeclHandler h : this.validatorDeclHandlers)
      {
        h.attributeDecl(name, name2, type, mode, value);
      }
    }
  }

  public void elementDecl(String name, String model)
    throws SAXException
  {
    if (validatorDeclHandlers.size() > 0)
    {
      for (DeclHandler h : this.validatorDeclHandlers)
      {
        h.elementDecl(name, model);
      }
    }
  }

  public void externalEntityDecl(String name, String publicId, String systemId)
    throws SAXException
  {
    if (validatorDeclHandlers.size() > 0)
    {
      for (DeclHandler h : this.validatorDeclHandlers)
      {
        h.externalEntityDecl(name, publicId, systemId);
      }
    }

    if (context.version == EPUBVersion.VERSION_3)
    {
      report.message(MessageId.HTM_003,
          EPUBLocation.create(path, getLineNumber(), getColumnNumber(), name), name);
      return;
    }
    entities.add(name);
  }

  public void internalEntityDecl(String name, String value)
    throws SAXException
  {
    if (validatorDeclHandlers.size() > 0)
    {
      for (DeclHandler h : this.validatorDeclHandlers)
      {
        h.internalEntityDecl(name, value);
      }
    }
    entities.add(name);
  }

  public XMLElement getCurrentElement()
  {
    return currentElement;
  }

  public Report getReport()
  {
    return report;
  }

  public int getLineNumber()
  {
    return documentLocator.getLineNumber();
  }

  public int getColumnNumber()
  {
    return documentLocator.getColumnNumber();
  }

  public EPUBLocation getLocation()
  {
    return EPUBLocation.create(path, documentLocator.getLineNumber(),
        documentLocator.getColumnNumber());
  }

  public String getXMLVersion()
  {
    return documentLocator.getXMLVersion();
  }

  public String getResourceName()
  {
    return path;
  }

  private static final byte[][] utf16magic = { { (byte) 0xFE, (byte) 0xFF },
      { (byte) 0xFF, (byte) 0xFE }, { 0, 0x3C, 0, 0x3F }, { 0x3C, 0, 0x3F, 0 } };

  private static final byte[][] ucs4magic = { { 0, 0, (byte) 0xFE, (byte) 0xFF },
      { (byte) 0xFF, (byte) 0xFE, 0, 0 }, { 0, 0, (byte) 0xFF, (byte) 0xFE },
      { (byte) 0xFE, (byte) 0xFF, 0, 0 }, { 0, 0, 0, 0x3C }, { 0, 0, 0x3C, 0 }, { 0, 0x3C, 0, 0 },
      { 0x3C, 0, 0, 0 } };

  private static final byte[] utf8magic = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

  private static final byte[] ebcdicmagic = { 0x4C, 0x6F, (byte) 0xA7, (byte) 0x94 };

  private static boolean matchesMagic(byte[] magic, byte[] buffer)
  {
    for (int i = 0; i < magic.length; i++)
    {
      if (buffer[i] != magic[i])
      {
        return false;
      }
    }
    return true;
  }

  private static String sniffEncoding(InputStream in)
    throws IOException
  {
    // see http://www.w3.org/TR/REC-xml/#sec-guessing
    byte[] buffer = new byte[256];
    in.mark(buffer.length);
    int len = in.read(buffer);
    in.reset();
    if (len < 4)
    {
      return null;
    }
    for (byte[] magic : utf16magic)
    {
      if (matchesMagic(magic, buffer))
      {
        return "UTF-16";
      }
    }
    for (byte[] anUcs4magic : ucs4magic)
    {
      if (matchesMagic(anUcs4magic, buffer))
      {
        return "UCS-4";
      }
    }
    if (matchesMagic(utf8magic, buffer))
    {
      return "UTF-8";
    }
    if (matchesMagic(ebcdicmagic, buffer))
    {
      return "EBCDIC";
    }

    // some ASCII-compatible encoding; read ASCII
    int asciiLen = 0;
    while (asciiLen < len)
    {
      int c = buffer[asciiLen] & 0xFF;
      if (c == 0 || c > 0x7F)
      {
        break;
      }
      asciiLen++;
    }

    // read it into a String
    String header = new String(buffer, 0, asciiLen, "ASCII");
    int encIndex = header.indexOf("encoding=");
    if (encIndex < 0)
    {
      return null; // probably UTF-8
    }

    encIndex += 9;
    if (encIndex >= header.length())
    {
      return null; // encoding did not fit!
    }

    char quote = header.charAt(encIndex);
    if (quote != '"' && quote != '\'')
    {
      return null; // confused...
    }

    int encEnd = header.indexOf(quote, encIndex + 1);
    if (encEnd < 0)
    {
      return null; // encoding did not fit!
    }

    String encoding = header.substring(encIndex + 1, encEnd);
    return encoding.toUpperCase(Locale.ROOT);
  }

  static
  {
    Hashtable<String, String> map = new Hashtable<String, String>();

    // OEB 1.2
    map.put("http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/oebpkg12.dtd"));
    map.put("http://http://idpf.org/dtds/oeb-1.2/oebpkg12.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/oebpkg12.dtd"));
    map.put("http://openebook.org/dtds/oeb-1.2/oeb12.ent",
        ResourceUtil.getResourcePath("schema/20/dtd/oeb12.dtdinc"));
    map.put("http://openebook.org/dtds/oeb-1.2/oebdoc12.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/oebdoc12.dtd"));

    // 2.0 dtd, probably never published
    map.put("http://www.idpf.org/dtds/2007/opf.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/opf20.dtd"));
    // xhtml 1.0
    map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml1-transitional.dtd"));
    map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml1-strict.dtd"));
    map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml-lat1.dtdinc"));
    map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml-symbol.dtdinc"));
    map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml-special.dtdinc"));
    // svg 1.1
    map.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/svg11.dtd"));
    // dtbook
    map.put("http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/dtbook-2005-2.dtd"));
    // ncx
    map.put("http://www.daisy.org/z3986/2005/ncx-2005-1.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/ncx-2005-1.dtd"));

    // xhtml 1.1: just reference the character entities, as we validate with rng
    map.put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml11-ent.dtd"));
    map.put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd",
        ResourceUtil.getResourcePath("schema/20/dtd/xhtml11-ent.dtd"));

    // non-resolved names; Saxon (which schematron requires and registers as
    // preferred parser, it seems) passes us those (bad, bad!), work around it
    map.put("xhtml-lat1.ent", ResourceUtil.getResourcePath("dtd/xhtml-lat1.dtdinc"));
    map.put("xhtml-symbol.ent", ResourceUtil.getResourcePath("dtd/xhtml-symbol.dtdinc"));
    map.put("xhtml-special.ent", ResourceUtil.getResourcePath("dtd/xhtml-special.dtdinc"));
    systemIdMap = map;
  }
}

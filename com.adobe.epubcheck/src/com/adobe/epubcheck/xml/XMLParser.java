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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
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
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.ResourceUtil;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;

public class XMLParser extends DefaultHandler {

	OCFPackage ocf;

	SAXParser parser;

	Report report;

	String resource;

	Vector contentHandlers = new Vector();

	XMLElement currentElement;

	// ContentHandler validatorContentHandler;
	Vector validatorContentHandlers = new Vector();

	// DTDHandler validatorDTDHandler;
	Vector validatorDTDHandlers = new Vector();

	Locator documentLocator;

	static String zipRoot = "file:///epub-root/";

	static Hashtable systemIdMap;

	static {
		Hashtable map = new Hashtable();

		// fully-resolved names
		map.put("http://www.idpf.org/dtds/2007/opf.dtd", ResourceUtil
				.getResourcePath("dtd/opf20.dtd"));
		map.put("http://openebook.org/dtds/oeb-1.2/oeb12.ent", ResourceUtil
				.getResourcePath("dtd/oeb12.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
				ResourceUtil.getResourcePath("dtd/xhtml1-transitional.dtd"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
				ResourceUtil.getResourcePath("dtd/xhtml1-strict.dtd"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent", ResourceUtil
				.getResourcePath("dtd/xhtml-lat1.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent",
				ResourceUtil.getResourcePath("dtd/xhtml-symbol.dtdinc"));
		map.put("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent",
				ResourceUtil.getResourcePath("dtd/xhtml-special.dtdinc"));
		map.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
				ResourceUtil.getResourcePath("dtd/svg11.dtd"));
		map.put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", ResourceUtil
				.getResourcePath("dtd/opf20.dtd"));
		map.put("http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd",
				ResourceUtil.getResourcePath("dtd/dtbook-2005-2.dtd"));
		map.put("http://www.daisy.org/z3986/2005/ncx-2005-1.dtd", ResourceUtil
				.getResourcePath("dtd/ncx-2005-1.dtd"));

		// non-resolved names; Saxon (which schematron requires and registers as
		// preferred parser, it seems)
		// passes us those (bad, bad!), work around it
		map.put("xhtml-lat1.ent", ResourceUtil
				.getResourcePath("dtd/xhtml-lat1.dtdinc"));
		map.put("xhtml-symbol.ent", ResourceUtil
				.getResourcePath("dtd/xhtml-symbol.dtdinc"));
		map.put("xhtml-special.ent", ResourceUtil
				.getResourcePath("dtd/xhtml-special.dtdinc"));

		systemIdMap = map;
	}

	public XMLParser(OCFPackage ocf, String entryName, Report report) {
		this.report = report;
		this.resource = entryName;
		this.ocf = ocf;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		boolean hasXML11 = false;
		try {
			hasXML11 = factory
					.getFeature("http://xml.org/sax/features/xml-1.1");
		} catch (Exception e) {
		}
		if (!hasXML11) {
			System.err
					.println("Your configuration does not support XML 1.1 parsing");
			System.err
					.println("\tAre you using off-the-shelf saxon.jar? It contains file named");
			System.err
					.println("\tMETA-INF/services/javax.xml.parsers.SAXParserFactory");
			System.err
					.println("\tThis interferes with Java default XML-1.1-compliant parser.");
			System.err
					.println("\tEither remove that file from saxon.jar or define");
			System.err
					.println("\tjavax.xml.parsers.SAXParserFactory system property");
			System.err.println("\tto point to XML-1.1-compliant parser.");
		}
		try {
			parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setDTDHandler(this);
			reader.setContentHandler(this);
			reader.setEntityResolver(this);
			reader.setErrorHandler(this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	public void addXMLHandler(XMLHandler handler) {
		contentHandlers.add(handler);
	}

	public void addValidator(XMLValidator xv) {
		PropertyMapBuilder propertyMapBuilder = new PropertyMapBuilder();
		propertyMapBuilder.put(ValidateProperty.ERROR_HANDLER,
				(ErrorHandler) this);
		Validator validator = xv.schema.createValidator(propertyMapBuilder
				.toPropertyMap());
		ContentHandler contentHandler = validator.getContentHandler();
		if (contentHandler != null)
			validatorContentHandlers.add(contentHandler);
		DTDHandler dtdHandler = validator.getDTDHandler();
		if (dtdHandler != null)
			validatorDTDHandlers.add(dtdHandler);
	}

	static final byte[][] utf16magic = { { (byte) 0xFE, (byte) 0xFF },
			{ (byte) 0xFF, (byte) 0xFE }, { 0, 0x3C, 0, 0x3F },
			{ 0x3C, 0, 0x3F, 0 } };

	static final byte[][] ucs4magic = { { 0, 0, (byte) 0xFE, (byte) 0xFF },
			{ (byte) 0xFF, (byte) 0xFE, 0, 0 },
			{ 0, 0, (byte) 0xFF, (byte) 0xFE },
			{ (byte) 0xFE, (byte) 0xFF, 0, 0 }, { 0, 0, 0, 0x3C },
			{ 0, 0, 0x3C, 0 }, { 0, 0x3C, 0, 0 }, { 0x3C, 0, 0, 0 } };

	static final byte[] utf8magic = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	static final byte[] ebcdicmagic = { 0x4C, 0x6F, (byte) 0xA7, (byte) 0x94 };

	static boolean matchesMagic(byte[] magic, byte[] buffer) {
		for (int i = 0; i < magic.length; i++)
			if (buffer[i] != magic[i])
				return false;
		return true;
	}

	static String sniffEncoding(InputStream in) throws IOException {
		// see http://www.w3.org/TR/REC-xml/#sec-guessing
		byte[] buffer = new byte[256];
		in.mark(buffer.length);
		int len = in.read(buffer);
		in.reset();
		if (len < 4)
			return null;
		for (int k = 0; k < utf16magic.length; k++)
			if (matchesMagic(utf16magic[k], buffer))
				return "UTF-16";
		for (int k = 0; k < ucs4magic.length; k++)
			if (matchesMagic(ucs4magic[k], buffer))
				return "UCS-4";
		if (matchesMagic(utf8magic, buffer))
			return "UTF-8";
		if (matchesMagic(ebcdicmagic, buffer))
			return "EBCDIC";
		
		// some ASCII-compatible encoding; read ASCII
		int asciiLen = 0;
		while( asciiLen < len ) {
			int c = buffer[asciiLen] & 0xFF;
			if( c == 0 || c > 0x7F )
				break;
			asciiLen++;
		}

		// read it into a String
		String header = new String(buffer, 0, asciiLen, "ASCII");
		int encIndex = header.indexOf("encoding=");
		if( encIndex < 0 )
			return null; // probably UTF-8
		
		encIndex += 9;
		if( encIndex >= header.length() )
			return null; // encoding did not fit!
		
		char quote = header.charAt(encIndex);
		if( quote != '"' && quote != '\'')
			return null; // confused...
		
		int encEnd = header.indexOf(quote, encIndex+1);
		if( encEnd < 0 )
			return null; // encoding did not fit!
		
		String encoding = header.substring(encIndex+1, encEnd);
		return encoding.toUpperCase();
	}

	public void process() {
		try {
			InputStream in = ocf.getInputStream(resource);
			if (!in.markSupported())
				in = new BufferedInputStream(in);
			String encoding = sniffEncoding(in);
			if (encoding != null && !encoding.equals("UTF-8")
					&& !encoding.equals("UTF-16")) {
				report.error(resource, 0,
						"Only UTF-8 and UTF-16 encodings are allowed for XML, detected "
								+ encoding);
			}
			InputSource ins = new InputSource(in);
			ins.setSystemId(zipRoot + resource);
			parser.parse(ins, this);
			in.close();
		} catch (IOException e) {
			report.error(null, 0, "I/O error reading " + resource);
		} catch (IllegalArgumentException e) {
			report.error(null, 0, "could not parse " + resource + ": "
					+ e.getMessage());
		} catch (SAXException e) {
			report.error(resource, 0, e.getMessage());
		}
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		String resourcePath = (String) systemIdMap.get(systemId);
		if (resourcePath != null) {
			InputStream resourceStream = ResourceUtil
					.getResourceStream(resourcePath);
			if (systemId.equals("xhtml-lat1.ent")
					|| systemId.equals("xhtml-symbol.ent")
					|| systemId.equals("xhtml-special.ent")) {
				System.err
						.println("A problem in XML parser detected: external XML entity URLs are not resolved");
				System.err
						.println("\tPlease configure your runtime environment to use a different XML parser");
				System.err
						.println("\t(e.g. using javax.xml.parsers.SAXParserFactory system property)");
			}
			InputSource source = new InputSource(resourceStream);
			source.setPublicId(publicId);
			source.setSystemId(systemId);
			return source;
		} else if (systemId.startsWith(zipRoot)) {
			String rname = systemId.substring(zipRoot.length());
			if (!ocf.hasEntry(rname))
				throw new SAXException("Could not resolve local XML entity '"
						+ rname + "'");
			if (!ocf.canDecrypt(rname))
				throw new SAXException("Could not decrypt local XML entity '"
						+ rname + "'");
			InputStream resourceStream = ocf.getInputStream(rname);
			InputSource source = new InputSource(resourceStream);
			source.setPublicId(publicId);
			source.setSystemId(systemId);
			return source;
		} else {
			report.warning(resource, 0, "Unresolved external XML entity '"
					+ systemId + "'");
			InputStream urlStream = new URL(systemId).openStream();
			InputSource source = new InputSource(urlStream);
			source.setPublicId(publicId);
			source.setSystemId(systemId);
			return source;

		}
	}

	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		int len = validatorDTDHandlers.size();
		for (int i = 0; i < len; i++) {
			((DTDHandler) validatorDTDHandlers.elementAt(i)).notationDecl(name,
					publicId, systemId);
		}
	}

	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		int len = validatorDTDHandlers.size();
		for (int i = 0; i < len; i++) {
			((DTDHandler) validatorDTDHandlers.elementAt(i))
					.unparsedEntityDecl(name, publicId, systemId, notationName);
		}
	}

	public void error(SAXParseException ex) throws SAXException {
		report.error(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		report.error(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void warning(SAXParseException ex) throws SAXException {
		report.warning(resource, ex.getLineNumber(), ex.getMessage());
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.characters(arg0, arg1, arg2);
		}

		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).characters(arg0, arg1,
					arg2);
	}

	public void endDocument() throws SAXException {
		int len = validatorContentHandlers.size();
		for (int i = 0; i < len; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.endDocument();
		}
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.endElement(arg0, arg1, arg2);
		}
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).endElement();
		currentElement = currentElement.getParent();
	}

	public void endPrefixMapping(String arg0) throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.endPrefixMapping(arg0);
		}
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.ignorableWhitespace(arg0, arg1, arg2);
		}
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).ignorableWhitespace(
					arg0, arg1, arg2);
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.processingInstruction(arg0, arg1);
		}
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).processingInstruction(
					arg0, arg1);
	}

	public void setDocumentLocator(Locator locator) {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.setDocumentLocator(locator);
		}
		documentLocator = locator;
	}

	public void skippedEntity(String arg0) throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.skippedEntity(arg0);
		}
	}

	public void startDocument() throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.startDocument();
		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.startElement(namespaceURI, localName, qName, atts);
		}
		int index = qName.indexOf(':');
		String prefix;
		String name;
		if (index < 0) {
			prefix = null;
			name = qName;
		} else {
			prefix = qName.substring(0, index);
			name = qName.substring(index + 1);
		}
		int count = atts.getLength();
		XMLAttribute[] attributes = count == 0 ? null : new XMLAttribute[count];
		for (int i = 0; i < count; i++) {
			String attName = atts.getLocalName(i);
			String attNamespace = atts.getURI(i);
			String attQName = atts.getQName(i);
			int attIndex = attQName.indexOf(':');
			String attPrefix;
			if (attIndex < 0) {
				attPrefix = null;
				attNamespace = null;
			} else {
				attPrefix = attQName.substring(0, attIndex);
			}
			String attValue = atts.getValue(i);
			attributes[i] = new XMLAttribute(attNamespace, attPrefix, attName,
					attValue);
		}
		currentElement = new XMLElement(namespaceURI, prefix, name, attributes,
				currentElement);
		int len = contentHandlers.size();
		for (int i = 0; i < len; i++)
			((XMLHandler) contentHandlers.elementAt(i)).startElement();
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		int vlen = validatorContentHandlers.size();
		for (int i = 0; i < vlen; i++) {
			((ContentHandler) validatorContentHandlers.elementAt(i))
					.startPrefixMapping(arg0, arg1);
		}
	}

	public XMLElement getCurrentElement() {
		return currentElement;
	}

	public Report getReport() {
		return report;
	}

	public int getLineNumber() {
		return documentLocator.getLineNumber();
	}

}

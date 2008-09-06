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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.adobe.epubcheck.util.ResourceUtil;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.rng.SAXSchemaReceiverFactory;
import com.thaiopensource.xml.sax.XMLReaderCreator;

public class XMLValidator {

	String schemaName;
	Schema schema;

	private class ResourceEntityResolver implements EntityResolver {

		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			String path = systemId;
			//if( path.indexOf("basic-table.rng") >= 0 )
			//	throw new RuntimeException("path is '" + path + "' schema name is '" + schemaName + "'");
			if (path.startsWith("file:/"))
				path = path.substring(6);
			while (path.startsWith("/"))
				path = path.substring(1);
			InputStream in = ResourceUtil.getResourceStream(ResourceUtil
					.getResourcePath(path));
			if (in == null)
				return null;
			InputSource source = new InputSource(in);
			source.setSystemId(systemId);
			return source;
		}
	}

	private class XMLReaderCreatorImpl implements XMLReaderCreator {

		public XMLReader createXMLReader() throws SAXException {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			try {
				SAXParser parser = factory.newSAXParser();
				XMLReader reader = parser.getXMLReader();
				reader.setEntityResolver(new ResourceEntityResolver());
				return reader;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new SAXException(e.toString());
			}
		}

	}

	// handles errors in schemas
	private class ErrorHandlerImpl implements ErrorHandler {

		public void error(SAXParseException exception) throws SAXException {			
			exception.printStackTrace();
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			exception.printStackTrace();
		}

		public void warning(SAXParseException exception) throws SAXException {
			exception.printStackTrace();
		}
		
	}

	public XMLValidator(String schemaName) {
		try {
			String resourcePath = ResourceUtil.getResourcePath(schemaName);
			InputStream schemaStream = ResourceUtil
					.getResourceStream(resourcePath);
			if( schemaStream == null ) {
				throw new RuntimeException("Could not find resource " + resourcePath);
			}
			InputSource schemaSource = new InputSource(schemaStream);
			schemaSource.setPublicId("/" + schemaName);
			PropertyMapBuilder mapBuilder = new PropertyMapBuilder();
			mapBuilder.put(ValidateProperty.XML_READER_CREATOR,
					new XMLReaderCreatorImpl());
			mapBuilder.put(SchemaReceiverFactory.PROPERTY,
					new SAXSchemaReceiverFactory());
			mapBuilder.put(ValidateProperty.ERROR_HANDLER,
					new ErrorHandlerImpl());
			AutoSchemaReader schemaReader = new AutoSchemaReader();
			this.schemaName = schemaName;
			schema = schemaReader.createSchema(schemaSource, mapBuilder
					.toPropertyMap());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Internal error: " + e + " " + schemaName);
		}
	}
	
}

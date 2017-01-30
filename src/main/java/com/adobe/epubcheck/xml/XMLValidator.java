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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.TransformerFactory;

import org.idpf.epubcheck.util.saxon.ColumnNumberFunction;
import org.idpf.epubcheck.util.saxon.LineNumberFunction;
import org.idpf.epubcheck.util.saxon.SystemIdFunction;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.adobe.epubcheck.util.ResourceUtil;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.auto.SchemaReaderFactorySchemaReceiverFactory;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.schematron.NewSaxonSchemaReaderFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathStaticContext;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.om.StandardNames;


public class XMLValidator
{

  Schema schema;

  /**
   * Basic Resolver from Jing modified to add support for resolving zip and
   * jar relative locations.
   *
   * @author george@oxygenxml.com
   */
  static public class BasicResolver implements Resolver
  {
    static private final BasicResolver theInstance = new BasicResolver();

    BasicResolver()
    {
    }

    public static BasicResolver getInstance()
    {
      return theInstance;
    }

    public void resolve(Identifier id, Input input) throws
        IOException,
        ResolverException
    {
      if (!input.isResolved())
      {
        input.setUri(resolveUri(id));
      }
    }

    public void open(Input input) throws
        IOException,
        ResolverException
    {
      if (!input.isUriDefinitive())
      {
        return;
      }
      URI uri;
      try
      {
        uri = new URI(input.getUri());
      }
      catch (URISyntaxException e)
      {
        throw new ResolverException(e);
      }
      if (!uri.isAbsolute())
      {
        throw new ResolverException("cannot open relative URI: " + uri);
      }
      URL url = new URL(uri.toASCIIString());
      // XXX should set the encoding properly
      // XXX if this is HTTP and we've been redirected, should do
      // input.setURI with the new URI
      input.setByteStream(url.openStream());
    }

    public static String resolveUri(Identifier id) throws
        ResolverException
    {
      try
      {
        String uriRef = id.getUriReference();
        URI uri = new URI(uriRef);
        if (!uri.isAbsolute())
        {
          String base = id.getBase();
          if (base != null)
          {
            // OXYGEN PATCH START
            // Use class URL in order to resolve protocols like zip
            // and jar.
            URI baseURI = new URI(base);
            if ("zip".equals(baseURI.getScheme())
                || "jar".equals(baseURI.getScheme()))
            {
              uriRef = new URL(new URL(base), uriRef)
                  .toExternalForm();
              // OXYGEN PATCH END
            }
            else
            {
              uriRef = baseURI.resolve(uri).toString();
            }
          }
        }

        return uriRef;
      }
      catch (URISyntaxException e)
      {
        throw new ResolverException(e);
      }
      catch (MalformedURLException e)
      {
        throw new ResolverException(e);
      }
    }
  }

  /**
   * Extends Jing's Saxon 9 schema reader factory by registering
   * extension functions.
   */
  static public class ExtendedSaxonSchemaReaderFactory extends NewSaxonSchemaReaderFactory
  {
    public void initTransformerFactory(TransformerFactory factory)
    {
      super.initTransformerFactory(factory);
      SymbolicName lineNumberFn = new SymbolicName(StandardNames.XSL_FUNCTION, LineNumberFunction.QNAME, 0);
      SymbolicName columnNumberFn = new SymbolicName(StandardNames.XSL_FUNCTION, ColumnNumberFunction.QNAME, 0);
      SymbolicName systemIdFn = new SymbolicName(StandardNames.XSL_FUNCTION, SystemIdFunction.QNAME, 0);
      if (factory instanceof TransformerFactoryImpl)
      {
        Configuration configuration = ((TransformerFactoryImpl) factory).getConfiguration();
        XPathStaticContext xpathContext = new IndependentContext(configuration);
        if (!xpathContext.getFunctionLibrary().isAvailable(lineNumberFn))
        {
          configuration.registerExtensionFunction(new LineNumberFunction());
        }
        if (!xpathContext.getFunctionLibrary().isAvailable(columnNumberFn))
        {
          configuration.registerExtensionFunction(new ColumnNumberFunction());
        }
        if (!xpathContext.getFunctionLibrary().isAvailable(systemIdFn))
        {
          configuration.registerExtensionFunction(new SystemIdFunction());
        }
      }
    }
  }

  // handles errors in schemas
  private class ErrorHandlerImpl implements ErrorHandler
  {

    public void error(SAXParseException exception) throws
        SAXException
    {
      exception.printStackTrace();
    }

    public void fatalError(SAXParseException exception) throws
        SAXException
    {
      exception.printStackTrace();
    }

    public void warning(SAXParseException exception) throws
        SAXException
    {
      exception.printStackTrace();
    }

  }

  public XMLValidator(String schemaName)
  {
    try
    {
      String resourcePath = ResourceUtil.getResourcePath(schemaName);
      URL systemIdURL = ResourceUtil.getResourceURL(resourcePath);
      if (systemIdURL == null)
      {
        throw new RuntimeException("Could not find resource "
            + resourcePath);
      }
      InputSource schemaSource = new InputSource(systemIdURL.toString());
      PropertyMapBuilder mapBuilder = new PropertyMapBuilder();
      mapBuilder.put(ValidateProperty.RESOLVER,
          BasicResolver.getInstance());
      mapBuilder.put(ValidateProperty.ERROR_HANDLER,
          new ErrorHandlerImpl());

      SchemaReader schemaReader;

      if (schemaName.endsWith(".rnc"))
      {
        schemaReader = CompactSchemaReader.getInstance();
			} else if (schemaName.endsWith(".sch")) {
				schemaReader = new AutoSchemaReader(
						new SchemaReaderFactorySchemaReceiverFactory(
  								new ExtendedSaxonSchemaReaderFactory()));
      }
      else
      {

        schemaReader = new AutoSchemaReader();
      }

      schema = schemaReader.createSchema(schemaSource,
          mapBuilder.toPropertyMap());
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new Error("Internal error: " + e + " " + schemaName);
    }
  }
}

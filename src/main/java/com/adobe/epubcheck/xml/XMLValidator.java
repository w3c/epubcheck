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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

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
import com.thaiopensource.validate.rng.CompactSchemaReader;

public class XMLValidator
{

  private static final String NVDL_SCHEMA_TYPE = "com.thaiopensource.validate.nvdl.SchemaImpl";
  private static final String MODE_TYPE = "com.thaiopensource.validate.nvdl.Mode";
  private static final String MODE_USAGE_TYPE = "com.thaiopensource.validate.nvdl.ModeUsage";
  private static final String CONTEXT_MAP_TYPE = "com.thaiopensource.validate.nvdl.ContextMap";


  private final Schema schema;
  private final boolean isNormative;

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

    public void resolve(Identifier id, Input input)
      throws IOException,
      ResolverException
    {
      if (!input.isResolved())
      {
        input.setUri(resolveUri(id));
      }
    }

    public void open(Input input)
      throws IOException,
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
      } catch (URISyntaxException e)
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

    public static String resolveUri(Identifier id)
      throws ResolverException
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
      } catch (URISyntaxException e)
      {
        throw new ResolverException(e);
      } catch (MalformedURLException e)
      {
        throw new ResolverException(e);
      }
    }
  }

  // handles errors in schemas
  private class ErrorHandlerImpl implements ErrorHandler
  {

    public void error(SAXParseException exception)
      throws SAXException
    {
      exception.printStackTrace();
    }

    public void fatalError(SAXParseException exception)
      throws SAXException
    {
      exception.printStackTrace();
    }

    public void warning(SAXParseException exception)
      throws SAXException
    {
      exception.printStackTrace();
    }

  }

  public XMLValidator(String schemaName, boolean isNormative)
  {
    this.isNormative = isNormative;
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
      }
      else
      {
        schemaReader = new AutoSchemaReader();
      }

      schema = schemaReader.createSchema(schemaSource,
          mapBuilder.toPropertyMap());
      initialiseNvdlSchema(schema);
    } catch (RuntimeException e)
    {
      throw e;
    } catch (Exception e)
    {
      e.printStackTrace();
      throw new Error("Internal error: " + e + " " + schemaName);
    }
  }

  private static void initialiseNvdlSchema(Schema schema)
  {
    if (!isNvdlSchema(schema))
    {
      return;
    }
    NvdlState state = collectNvdlState(schema);
    try
    {
      Method usageAttributeProcessing = Class.forName(MODE_USAGE_TYPE)
          .getDeclaredMethod("getAttributeProcessing");
      usageAttributeProcessing.setAccessible(true);
      for (Object usage : state.usages)
      {
        usageAttributeProcessing.invoke(usage);
      }
      Method modeAttributeProcessing = Class.forName(MODE_TYPE)
          .getDeclaredMethod("getAttributeProcessing");
      modeAttributeProcessing.setAccessible(true);
      for (Object mode : state.modes)
      {
        modeAttributeProcessing.invoke(mode);
      }
    } catch (ReflectiveOperationException e)
    {
      // an Error, so that it is not reported as CHK-008
      throw new Error("Internal error: could not initialize the NVDL schema", e);
    }
  }

  static final class NvdlState
  {
    final Set<Object> modes = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
    final Set<Object> usages = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
  }

  static NvdlState collectNvdlState(Schema schema)
  {
    NvdlState state = new NvdlState();
    Deque<Object> pendingModes = new ArrayDeque<>();
    Deque<Object> pendingUsages = new ArrayDeque<>();
    try
    {
      Object modeMap = read(schema, "modeMap");
      if (modeMap instanceof Map)
      {
        pendingModes.addAll(((Map<?, ?>) modeMap).values());
      }
      add(pendingModes, read(schema, "startMode"));
      add(pendingModes, read(schema, "defaultBaseMode"));

      while (!pendingModes.isEmpty() || !pendingUsages.isEmpty())
      {
        while (!pendingModes.isEmpty())
        {
          Object mode = pendingModes.poll();
          if (CONTEXT_MAP_TYPE.equals(mode.getClass().getName()))
          {
            // context maps can be nested
            add(pendingModes, read(mode, "rootValue"));
            add(pendingModes, read(mode, "otherValue"));
            Object nameTable = read(mode, "nameTable");
            if (nameTable instanceof Map)
            {
              pendingModes.addAll(((Map<?, ?>) nameTable).values());
            }
            continue;
          }
          if (!MODE_TYPE.equals(mode.getClass().getName()) || !state.modes.add(mode))
          {
            continue;
          }
          add(pendingModes, read(mode, "baseMode"));
          Object includedModes = read(mode, "includedModes");
          if (includedModes instanceof Collection)
          {
            pendingModes.addAll((Collection<?>) includedModes);
          }
          collectModeUsages(read(mode, "elementMap"), pendingUsages);
          collectModeUsages(read(mode, "nssElementMap"), pendingUsages);
        }
        while (!pendingUsages.isEmpty())
        {
          Object usage = pendingUsages.poll();
          if (!state.usages.add(usage))
          {
            continue;
          }
          add(pendingModes, read(usage, "mode"));
          add(pendingModes, read(usage, "currentMode"));
          add(pendingModes, read(usage, "modeMap"));
        }
      }
    } catch (ReflectiveOperationException e)
    {
      throw new Error("Internal error: could not read the internals of the NVDL schema "
          + schema.getClass().getName() + ": " + e.getMessage(), e);
    }
    return state;
  }

  private static void collectModeUsages(Object actionMap, Deque<Object> usages)
    throws ReflectiveOperationException
  {
    if (!(actionMap instanceof Map))
    {
      return;
    }
    for (Object actionSet : ((Map<?, ?>) actionMap).values())
    {
      Object resultAction = read(actionSet, "resultAction");
      if (resultAction != null)
      {
        add(usages, read(resultAction, "modeUsage"));
      }
      Object[] noResultActions = (Object[]) read(actionSet, "noResultActions");
      if (noResultActions != null)
      {
        for (Object action : noResultActions)
        {
          add(usages, read(action, "modeUsage"));
        }
      }
    }
  }

  private static boolean isNvdlSchema(Schema schema)
  {
    return NVDL_SCHEMA_TYPE.equals(schema.getClass().getName());
  }

  private static Object read(Object target, String name) throws ReflectiveOperationException
  {
    Field field = field(target.getClass(), name);
    field.setAccessible(true);
    return field.get(target);
  }

  private static Field field(Class<?> type, String name) throws NoSuchFieldException
  {
    for (Class<?> current = type; current != null; current = current.getSuperclass())
    {
      try
      {
        return current.getDeclaredField(name);
      } catch (NoSuchFieldException e)
      {
        // look in the superclass
      }
    }
    throw new NoSuchFieldException(name + " in " + type.getName());
  }

  private static void add(Deque<Object> queue, Object value)
  {
    if (value != null)
    {
      queue.add(value);
    }
  }

  public Schema getSchema()
  {
    return schema;
  }

  public boolean isNormative()
  {
    return isNormative;
  }
}

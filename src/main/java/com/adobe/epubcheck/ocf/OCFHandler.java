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

package com.adobe.epubcheck.ocf;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class OCFHandler extends XMLHandler implements OCFData
{
  public OCFHandler(ValidationContext context)
  {
    super(context);
  }

  private final Map<String, List<String>> entries = new HashMap<String, List<String>>();
  private String mappingDoc = null;

  @Override
  public List<String> getEntries(String mediatype)
  {
    if (entries.containsKey(mediatype))
    {
      return Collections.unmodifiableList(entries.get(mediatype));
    }
    else
    {
      return Collections.emptyList();
    }
  }

  @Override
  public List<String> getEntries()
  {
    LinkedList<String> result = new LinkedList<String>();
    for (List<String> paths : entries.values())
    {
      result.addAll(paths);
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public Optional<String> getMapping()
  {
    return Optional.fromNullable(mappingDoc);
  }

  @Override
  public void startElement()
  {
    XMLElement element = currentElement();
    String ns = element.getNamespace();
    if ("urn:oasis:names:tc:opendocument:xmlns:container".equals(ns))
    {
      if ("rootfile".equals(element.getName()))
      {
        String mediaType = (element.getAttribute("media-type") != null)
            ? element.getAttribute("media-type").trim() : "unknown";
        String fullPath = element.getAttribute("full-path");
        if (!entries.containsKey(mediaType))
        {
          entries.put(mediaType, new LinkedList<String>());
        }
        entries.get(mediaType).add(fullPath);
      }
      else if ("link".equals(element.getName()))
      {
        if ("mapping".equals(Strings.nullToEmpty(element.getAttribute("rel")).trim()))
        {
          mappingDoc = element.getAttribute("href");
        }
      }
    }
  }
}

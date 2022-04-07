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

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

final class OCFContainerFileHandler extends XMLHandler
{

  private final OCFCheckerState state;

  public OCFContainerFileHandler(ValidationContext context, OCFCheckerState state)
  {
    super(context, state.getContainer().getRootURL());
    this.state = state;
  }

  @Override
  public void startElement()
  {
    XMLElement element = currentElement();
    String ns = element.getNamespace();
    if ("urn:oasis:names:tc:opendocument:xmlns:container".equals(ns))
    {
      switch (element.getName())
      {
      case "rootfile":
        processRootFile();
        break;

      case "link":
        processMappingDoc();
        break;
      }
    }
  }

  private void processRootFile()
  {
    assert "rootfile".equals(currentElement().getName());
    String fullPath = currentElement().getAttribute("full-path");
    String mediaType = Optional.fromNullable(currentElement().getAttribute("media-type"))
        .or("unknown").trim();

    // Check that the full-path attribute was found
    if (fullPath == null)
    {
      report.message(MessageId.OPF_016, location());
      return;
    }
    else if (fullPath.trim().isEmpty())
    {
      report.message(MessageId.OPF_017, location());
      return;
    }

    try
    {
      // Parse the rootfile URL
      URL rootfileURL = URL.parse(baseURL(), fullPath);

      // Register the parsed rootfile entry to the data model
      state.addRootfile(mediaType, rootfileURL);

    } catch (GalimatiasParseException e)
    {
      // FIXME 2022 - test this is reported
      report.message(MessageId.RSC_020, location(), fullPath);
      return;
    }
  }

  private void processMappingDoc()
  {
    assert "link".equals(currentElement().getName());
    String href = currentElement().getAttribute("href");

    if ("mapping".equals(Strings.nullToEmpty(currentElement().getAttribute("rel")).trim())
        && !Strings.nullToEmpty(href).trim().isEmpty())
    {

      try
      {
        // Parse the href attribute against the container root URL
        URL mappingDocURL = URL.parse(baseURL(), href);

        // Register the parsed mapping document entry to the data model
        state.addMappingDocument(mappingDocURL);
      } catch (GalimatiasParseException e)
      {
        // FIXME 2022 - test this is reported
        report.message(MessageId.RSC_020, location(), href);
        return;
      }
    }
  }

}

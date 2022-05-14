/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

package com.adobe.epubcheck.dict;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.PublicationResourceChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Preconditions;

public class SearchKeyMapChecker extends  PublicationResourceChecker
{


  public SearchKeyMapChecker(ValidationContext context)
  {
    super(context);
    Preconditions.checkState("application/vnd.epub.search-key-map+xml".equals(context.mimeType));
  }

  @Override
  protected boolean checkContent()
  {
    // File check
    if (!context.path.endsWith(".xml"))
    {
      report.message(MessageId.OPF_080, EPUBLocation.create(context.path));
    }
    // Content checks
    XMLParser parser = new XMLParser(context);
    parser.addValidator(XMLValidators.SEARCH_KEY_MAP_RNC.get());
    parser.addContentHandler(new SearchKeyMapHandler(context));
    parser.process();
    return true;
  }
}

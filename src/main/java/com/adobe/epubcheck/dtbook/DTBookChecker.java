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

package com.adobe.epubcheck.dtbook;

import com.adobe.epubcheck.opf.PublicationResourceChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Preconditions;

public class DTBookChecker extends  PublicationResourceChecker
{


  public DTBookChecker(ValidationContext context)
  {
    super(context);
    Preconditions.checkState("application/x-dtbook+xml".equals(context.mimeType));
  }

  @Override
  protected boolean checkContent()
  {
    XMLParser dtbookParser = new XMLParser(context);
    dtbookParser.addValidator(XMLValidators.DTBOOK_RNG.get());
    dtbookParser.addContentHandler(new DTBookHandler(context));
    dtbookParser.process();
    return true;
  }
}

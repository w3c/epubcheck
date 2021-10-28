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

package com.adobe.epubcheck.ncx;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.PublicationResourceChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Preconditions;

public class NCXChecker extends PublicationResourceChecker
{
  
  private String ncxId = null;

  public NCXChecker(ValidationContext context)
  {
    super(context);
    Preconditions.checkState("application/x-dtbncx+xml".equals(context.mimeType));
  }

  @Override
  protected boolean checkContent()
  { 
    // relaxng
    XMLParser ncxParser;
    NCXHandler ncxHandler;

    ncxParser = new XMLParser(context);
    ncxParser.addValidator(XMLValidators.NCX_RNG.get());
    ncxParser.addValidator(XMLValidators.NCX_SCH.get());
    ncxHandler = new NCXHandler(ncxParser, context.path, context.xrefChecker.get());
    ncxParser.addXMLHandler(ncxHandler);
    ncxParser.process();

    // report this for EPUB2 and ALSO for EPUB3 (see discussion in #669)
    ncxId = ncxHandler.getUid();
    if (ncxId != null && !ncxId.equals(ncxId.trim()))
    {
      report.message(MessageId.NCX_004, EPUBLocation.create(context.path));
    }
    return true;
  }
  
  @Override
  protected boolean checkPublicationAfterContent() {
    // Check that the ID matches the OPF’s ID
    // TODO improve way to get this EPUB 2's single OPF
    String uid = context.ocf.get().getOpfData().values().iterator().next().getUniqueIdentifier();
    if (uid != null && ncxId != null && !uid.equals(ncxId.trim()))
    {
      report.message(MessageId.NCX_001, EPUBLocation.create(context.path), ncxId, uid);
    }
    return true;
  }
}

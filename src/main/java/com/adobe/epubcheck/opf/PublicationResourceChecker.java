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

package com.adobe.epubcheck.opf;

import org.w3c.epubcheck.core.AbstractChecker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.google.common.base.Preconditions;

//FIXME 2021 Romain - document
public class PublicationResourceChecker extends AbstractChecker
{

  public PublicationResourceChecker(ValidationContext context)
  {
    super(context);
  }
  

  @Override
  public final void check()
  {
    boolean cont = !context.container.isPresent() || checkPublicationBeforeContent();
    cont = cont && checkContent();
    cont = cont && !context.container.isPresent() || checkPublicationAfterContent();
  }

  // by construction we know context.ocf is present
  protected boolean checkPublicationBeforeContent()
  {
    return checkResourceExists(context)
        && checkResourceCanBeDecrypted(context);
  }

  protected boolean checkContent()
  {
    return true;
  }

  //by construction we know context.ocf is present
  protected boolean checkPublicationAfterContent()
  {
    return true;
  }


  private static boolean checkResourceExists(ValidationContext context)
  {
    Preconditions.checkState(context.container.isPresent());
    if (!context.container.get().contains(context.url))
    {
      context.report.message(MessageId.RSC_001, EPUBLocation.of(context),
          context.path);
      return false;
    }
    else
    {
      return true;
    }
  }

  private static boolean checkResourceCanBeDecrypted(ValidationContext context)
  {
    Preconditions.checkState(context.container.isPresent());
    if (!context.container.get().canDecrypt(context.url))
    {
      context.report.message(MessageId.RSC_004, EPUBLocation.of(context),
          context.path);
      return false;
    }
    else
    {
      return true;
    }
  }
}

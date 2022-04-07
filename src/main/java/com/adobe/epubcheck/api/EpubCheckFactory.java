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

package com.adobe.epubcheck.api;

import java.io.File;
import java.io.IOException;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.opf.ValidationContext;

public class EpubCheckFactory
{
  static private final EpubCheckFactory instance = new EpubCheckFactory();

  static public EpubCheckFactory getInstance()
  {
    return instance;
  }

  public Checker newInstance(ValidationContext context)
  {
    //FIXME next test on context.url instead of context.path
    if (context.path.startsWith("http://") || context.path.startsWith("https://"))
    {
      try
      {
        return new EpubCheck(context.resourceProvider.openStream(context.url), context.report,
            context.path, context.profile);
      } catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    else
    {
      return new EpubCheck(new File(context.path), context.report, context.profile);
    }
  }
}

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
package com.adobe.epubcheck.css;

import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ContentCheckerFactory;

public class CSSCheckerFactory implements ContentCheckerFactory
{
  /*
    * (non-Javadoc)
    * @see com.adobe.epubcheck.opf.ContentCheckerFactory#newInstance(com.adobe.epubcheck.ocf.OCFPackage, com.adobe.epubcheck.api.Report, java.lang.String, java.lang.String, java.lang.String, com.adobe.epubcheck.opf.XRefChecker, com.adobe.epubcheck.util.EPUBVersion)
    */
  public ContentChecker newInstance(ValidationContext context)
  {

    return new CSSChecker(context);
  }

  /**
   * Additional constructor for validating CSS strings (style attributes and elements)
   */
  public ContentChecker newInstance(ValidationContext context, String value, int line,
      boolean isStyleAttribute)
  {
    return new CSSChecker(context, value, line, isStyleAttribute);
  }

  static private final CSSCheckerFactory instance = new CSSCheckerFactory();

  static public CSSCheckerFactory getInstance()
  {
    return instance;
  }
}

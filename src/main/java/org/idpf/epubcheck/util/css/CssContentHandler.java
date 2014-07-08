/*
 * Copyright (c) 2012 International Digital Publishing Forum
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

package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;

import java.util.List;

/**
 * ContentHandler interface for CssParser.
 *
 * @author mgylling
 */
public interface CssContentHandler
{

  public void startDocument();

  public void endDocument();

  public void startAtRule(CssAtRule atRule);

  public void endAtRule(String name);

  public void selectors(List<CssSelector> selectors);

  public void endSelectors(List<CssSelector> selectors);

  public void declaration(CssDeclaration declaration);

  /**
   * A default, do-nothing implementation of CssContentHandler.
   */
  public class CssDefaultHandler implements CssContentHandler
  {

    public void startDocument()
    {
    }

    public void endDocument()
    {
    }

    public void startAtRule(CssAtRule atRule)
    {
    }

    public void endAtRule(String name)
    {
    }

    public void selectors(List<CssSelector> selectors)
    {
    }

    public void endSelectors(List<CssSelector> selectors)
    {
    }

    public void declaration(CssDeclaration declaration)
    {
    }

  }
}

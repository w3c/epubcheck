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

import com.google.common.base.Predicate;
import org.idpf.epubcheck.util.css.CssToken.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

final class CssTokenList extends ArrayList<CssToken>
{

  public CssTokenList()
  {
    this(2048);
  }

  private CssTokenList(int initSize)
  {
    super(initSize);
  }

  CssTokenIterator iterator(final Predicate<CssToken> filter)
  {
    return new CssTokenIterator(filter, this);
  }

  class CssTokenIterator
  {

    private final Predicate<CssToken> filter;
    final List<CssToken> list;
    int next; // index of next element to return
    int lastRet = -1; // index of last element returned
    CssToken last = null; // last token returned

    /**
     * Constructor.
     *
     * @param filter A filter Predicate that is applied to all tokens unless
     *               another Predicate is passed using #next(Predicate)
     */
    CssTokenIterator(final Predicate<CssToken> filter, List<CssToken> list)
    {
      this.filter = checkNotNull(filter);
      this.list = list;
    }

    /**
     * Get the next token in the token list. This method uses the
     * constructor filter.
     */
    CssToken next()
    {
      return next(filter);
    }

    /**
     * Get the next token in the token list that matches the given filter,
     * or NoSuchElementException if no more tokens exist.
     */
    CssToken next(final Predicate<CssToken> filter)
    {
      while (true)
      {
        int i = next;
        if (i >= size())
        {
          throw new NoSuchElementException();
        }
        next = i + 1;
        CssToken tk = get(lastRet = i);
        if (filter.apply(tk))
        {
          last = tk;
          return tk;
        }
      }
    }

    /**
     * Using the constructor filter.
     */
    boolean hasNext()
    {
      return hasNext(filter);
    }

    /**
     * Using the supplied filter.
     */
    boolean hasNext(final Predicate<CssToken> filter)
    {
      int i = next;
      while (true)
      {
        if (i == size())
        {
          return false;
        }
        if (filter.apply(get(i)))
        {
          return true;
        }
        i++;
      }
    }

    /**
     * Get the next token without advancing the iterators position.
     */
    public CssToken peek()
    {
      return peek(filter);
    }

    /**
     * Get the next token without advancing the iterators position.
     */
    public CssToken peek(Predicate<CssToken> filter)
    {
      int _next = next;
      int _lastRet = lastRet;
      CssToken _last = last;
      if (hasNext(filter))
      {
        CssToken tk = next(filter);
        next = _next;
        lastRet = _lastRet;
        last = _last;
        return tk;
      }
      throw new NoSuchElementException();
    }

    /**
     * Get the list index of the last element returned prior to filtering,
     * or -1 if no element has yet been returned.
     */
    int index()
    {
      return lastRet;
    }

    /**
     * Get the filter that is currently being used for invocations of next()
     * and hasNext()
     */
    Predicate<CssToken> filter()
    {
      return filter;
    }

  }

  static final class Filters
  {

    /**
     * A filter that filters whitespace and comments
     */
    static final Predicate<CssToken> FILTER_S_CMNT = new Predicate<CssToken>()
    {
      public boolean apply(final CssToken input)
      {
        return !(input.type == Type.S || input.type == Type.COMMENT);
      }
    };

    /**
     * A filter that filters whitespace, comments, CDO and CDC
     */
    static final Predicate<CssToken> FILTER_S_CMNT_CDO_CDC = new Predicate<CssToken>()
    {
      public boolean apply(final CssToken input)
      {
        return !(input.type == Type.S || input.type == Type.COMMENT
            || input.type == Type.CDO || input.type == Type.CDC);
      }
    };

    /**
     * A filter that filters nothing.
     */
    static final Predicate<CssToken> FILTER_NONE = new Predicate<CssToken>()
    {
      public boolean apply(final CssToken input)
      {
        return true;
      }
    };

  }

  static final class PrematureEOFException extends RuntimeException
  {
    private static final long serialVersionUID = 343701466381708884L;
  }

  private static final long serialVersionUID = 924068097174100851L;

}

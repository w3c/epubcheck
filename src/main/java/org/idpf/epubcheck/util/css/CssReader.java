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

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * A wrapper around java.io.Reader with a pushback buffer, offset and
 * line+column tracking. This is used by CssScanner.
 *
 * @author mgylling
 */
final class CssReader
{
  static final int DEFAULT_PUSHBACK_BUFFER_SIZE = 8096;
  private final int[] buf;
  private int pos;
  private final Reader in;
  private int prevLine = 1;

  /**
   * The systemID of the resource being read. If the resource being read
   * is virtual the value is set to CssLocaton.NO_SID
   */
  final String systemID;

  /**
   * The char that this reader is currently positioned at, or -1 if EOF is
   * reached.
   */
  int curChar = 0;

  /**
   * The previous char, or 0 if curChar is the first character in the resource
   * being read.
   */
  int prevChar = 0;

  /**
   * The offset (in characters) of prevChar from the start of the resource
   * being read.
   */
  int offset = 0;

  /**
   * The current line in the resource being read
   */
  int line = 1;

  /**
   * The current column in the resource being read
   */
  int col = 1;

  CssReader(Reader reader, String systemID, int pushbackSize)
  {
    this.in = checkNotNull(reader) instanceof BufferedReader
        ? reader
        : new BufferedReader(reader);
    this.systemID = checkNotNull(systemID);
    checkArgument(pushbackSize >= 1);
    this.pos = pushbackSize;
    this.buf = new int[pushbackSize];

  }

  /**
   * Returns the next character in the stream and advances the readers
   * position. If there are no more characters, -1 is returned the first time
   * this method is invoked in that state; multiple invocations at EOF will
   * yield an IllegalStateException.
   */
  int next() throws
      IOException
  {
    prevChar = curChar;
    checkState(prevChar > -1);

    if (pos < buf.length)
    {
      curChar = buf[pos++];
    }
    else
    {
      curChar = in.read();
    }

    offset++;

    /*
       * Handle line and col
       * lf=\n, cr=\r
       */
    if (curChar == '\r' || curChar == '\n')
    {
      if (curChar == '\n' && prevChar == '\r')
      {
        //second char in a windows CR+LF
      }
      else
      {
        prevLine = line;
        line++;
      }

    }
    else if (prevLine < line)
    {
      col = 1;
      prevLine = line;
    }
    else
    {
      if (prevChar != 0)
      {
        col++;
      }
    }

    return curChar;
  }

  /**
   * Returns the next character in the stream without advancing the readers
   * position. If there are no more characters, -1 is returned.
   */
  int peek() throws
      IOException
  {
    Mark m = mark();
    int ch = next();
    unread(ch, m);
    return ch;
  }

  /**
   * Returns the the next n characters in the stream without advancing the
   * readers position.
   *
   * @param n the number of characters to read
   * @return An array with guaranteed length n, with -1 being the value for
   *         all elements at and after EOF.
   * @throws IOException
   */
  int[] peek(int n) throws
      IOException
  {
    int[] buf = new int[n];
    Mark m = mark();
    boolean seenEOF = false;

    for (int i = 0; i < buf.length; i++)
    {
      if (!seenEOF)
      {
        buf[i] = next();
      }
      else
      {
        buf[i] = -1;
      }
      if (buf[i] == -1)
      {
        seenEOF = true;
      }
    }
    if (!seenEOF)
    {
      unread(buf, m);
    }
    else
    {
      List<Integer> ints = Lists.newArrayList();
      for (int aBuf : buf)
      {
        ints.add(aBuf);
        if (aBuf == -1)
        {
          break;
        }
      }
      unread(ints, m);
    }

    return buf;
  }

  /**
   * Peek and return the character at position n from current position, or -1
   * if EOF is reached before or at that position.
   */
  int at(int n) throws
      IOException
  {
    Mark mark = mark();
    List<Integer> cbuf = Lists.newArrayList();
    for (int i = 0; i < n; i++)
    {
      cbuf.add(next());
      if (curChar == -1)
      {
        break;
      }
    }
    unread(cbuf, mark);
    return cbuf.get(cbuf.size() - 1);
  }

  /**
   * Reads forward and returns the the next n characters in the stream. Escapes
   * are returned verbatim. At return, the reader is at positioned such that
   * next() will return n+1 or EOF (-1).
   *
   * @param n the number of characters to read
   * @return An array with guaranteed length n, with -1 being the value for
   *         all elements at and after EOF.
   * @throws IOException
   */
  int[] collect(int n) throws
      IOException
  {
    int[] buf = new int[n];
    boolean seenEOF = false;
    for (int i = 0; i < buf.length; i++)
    {
      if (seenEOF)
      {
        buf[i] = -1;
      }
      else
      {
        buf[i] = next();
        if (curChar == -1)
        {
          seenEOF = true;
        }
      }
    }
    return buf;
  }

  /**
   * Read forward until the next non-escaped character matches the given
   * CharMatcher or is EOF.
   *
   * @throws IOException
   */
  CssReader forward(CharMatcher matcher) throws
      IOException
  {
    while (true)
    {
      Mark mark = mark();
      next();
      //TODO escape awareness
      if (curChar == -1 || (matcher.matches((char) curChar) && prevChar != '\\'))
      {
        unread(curChar, mark);
        break;
      }
    }
    return this;
  }

  /**
   * Read forward n characters, or until the next character is EOF.
   *
   * @throws IOException
   */
  CssReader forward(int n) throws
      IOException
  {
    for (int i = 0; i < n; i++)
    {
      //TODO escape awareness
      Mark mark = mark();
      next();
      if (curChar == -1)
      {
        unread(curChar, mark);
        break;
      }
    }
    return this;
  }

  void unread(final int ch, final Mark mark)
  {
    checkState(pos > 0);
    buf[--pos] = ch;
    reset(mark);
  }

  void unread(final int cbuf[], final Mark mark) throws
      IOException
  {
    unread(cbuf, 0, cbuf.length, mark);
  }

  void unread(final List<Integer> cbuf, final Mark mark) throws
      IOException
  {
    unread(Ints.toArray(cbuf), mark);
  }

  void unread(final int cbuf[], final int off, final int len, final Mark mark)
  {
    checkArgument(len < pos);
    pos -= len;
    System.arraycopy(cbuf, off, buf, pos, len);
    reset(mark);
  }

  Mark mark()
  {
    return new Mark(curChar, prevChar, line, col, prevLine, offset);
  }

  private CssReader reset(final Mark mark)
  {
    this.curChar = mark.mCur;
    this.prevChar = mark.mPrev;
    this.line = mark.mLine;
    this.col = mark.mCol;
    this.prevLine = mark.mPrevLine;
    this.offset = mark.mOffset;
    return this;
  }

  final class Mark
  {
    final int mLine;
    final int mCol;
    final int mPrev;
    final int mCur;
    final int mPrevLine;
    final int mOffset;

    private Mark(final int curChar, final int prevChar, final int line, final int col,
        final int prevLine, final int offset)
    {
      this.mCur = curChar;
      this.mPrev = prevChar;
      this.mLine = line;
      this.mCol = col;
      this.mPrevLine = prevLine;
      this.mOffset = offset;
    }
  }

  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
        .add("curChar", (char) curChar)
        .add("prevChar", (char) prevChar)
        .toString();
  }

}
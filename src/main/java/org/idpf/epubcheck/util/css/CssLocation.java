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

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a location in a CSS file.
 *
 * @author mgylling
 */
public final class CssLocation
{

  final int line;
  final int col;
  private final int charOffset;
  private final String systemID;

  CssLocation(final int line, final int col, final int charOffset, final String systemID)
  {
    this.line = checkNotNull(line);
    this.col = checkNotNull(col);
    this.charOffset = checkNotNull(charOffset);
    this.systemID = checkNotNull(systemID);
  }

  static CssLocation create(final CssReader reader)
  {
    return new CssLocation(reader.line, reader.col, reader.offset, reader.systemID);
  }

  public int getLine()
  {
    return line;
  }

  public int getColumn()
  {
    return col;
  }

  public int getCharOffset()
  {
    return charOffset;
  }

  public String getSystemID()
  {
    return systemID;
  }

  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
        .add("line", line)
        .add("col", col)
        .add("charOffset", charOffset)
        .toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof CssLocation)
    {
      CssLocation loc = (CssLocation) obj;
      if (loc.charOffset == this.charOffset
          && loc.line == this.line
          && loc.col == this.col
          && loc.systemID.equals(this.systemID))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Canonical representation of the location system id
   * for virtual resources
   */
  public static final String NO_SID = "VIRTUAL";
}

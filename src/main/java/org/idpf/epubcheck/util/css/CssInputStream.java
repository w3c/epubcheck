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

import com.adobe.epubcheck.util.outWriter;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.Map;

import static org.idpf.epubcheck.util.css.CssScanner.QUOTES;
import static org.idpf.epubcheck.util.css.CssScanner.TERMINATOR;

/**
 * An InputStream for CSS files that detects and skips past BOMs, and
 * peeks for @charset rules.
 *
 * @author mgylling
 */
public class CssInputStream extends PushbackInputStream
{
  Optional<String> bom = Optional.absent();
  Optional<String> charset = Optional.absent();
  private static final int MAX_PUSHBACK = 256;
  private static final Map<String, byte[]> boms = new ImmutableMap.Builder<String, byte[]>()
      .put("UTF-32BE", new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF})
      .put("UTF-32LE", new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0x00, (byte) 0x00})
      .put("UTF-8", new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF})
      .put("UTF-16BE", new byte[]{(byte) 0xFE, (byte) 0xFF})
      .put("UTF-16LE", new byte[]{(byte) 0xFF, (byte) 0xFE})
      .build();
	private boolean debug = false;

  public CssInputStream(final InputStream in) throws
      IOException
  {
    super(in instanceof BufferedInputStream
        ? in : new BufferedInputStream(in), MAX_PUSHBACK);

    String enc = getBOM();
    if (!Strings.isNullOrEmpty(enc))
    {
      this.bom = Optional.of(enc);
    }

    enc = getCssCharset(enc);
    if (!Strings.isNullOrEmpty(enc))
    {
      this.charset = Optional.of(enc);
    }
    	
    	if(debug)
      {
    		String s = bom.isPresent() ? bom.get() : " none.";
    		outWriter.println("detected BOM: " + s);
    		s = charset.isPresent() ? charset.get() : " none.";
    		outWriter.println("detected charset: " + s);
    	}

  }

  private String getBOM() throws
      IOException
  {
    byte[] data = new byte[4];
    int read, unread;
    read = ByteStreams.read(this, data, 0, data.length);
    String key = map(data);
    unread = key == null ? read : data.length - boms.get(key).length;
    if (unread > 0)
    {
      unread(data, read - unread, unread);
    }
    return key;
  }

  private String getCssCharset(final String bom) throws
      IOException
  {
    /*
       * Because each char can be represented by 1-4 bytes, and because
       * there can be any amount of whitespace between @charset and the
       * open quote, this is fiddly.
       *
       * We read maximally four bytes at a time (required to decode UTF32 to a single char)
       */

    String enc = bom != null ? bom : "UTF-8";

    int len = 1;
    if (enc.startsWith("UTF-16"))
    {
      len = 2;
    }
    else if (enc.startsWith("UTF-32"))
    {
      len = 4;
    }

    Endian endian = null;
    if (len != 1)
    {
      endian = enc.endsWith("BE") ? Endian.BIG : Endian.LITTLE;
    }

    String value = null;
    List<Byte> unread = Lists.newArrayList(); //all bytes read
    StringBuilder sbuf = new StringBuilder(); //all chars read
    char openQuote = 0;
    int openQuotePos = -1;
    byte[] bbuf;

    while (true)
    {
      bbuf = new byte[len];
      for (int i = 0; i < len; i++)
      {
        int b = read();
        if (b == -1)
        {
          break;
        }
        unread.add((byte) b);
        bbuf[i] = (byte) b;
      }

      if (unread.size() == MAX_PUSHBACK)
      {
        break;
      }

      if (len == 1 || endian == Endian.LITTLE)
      {
        sbuf.append((char) bbuf[0]);
      }
      else
      {
        sbuf.append((char) bbuf[len - 1]);
      }

      char cur = sbuf.charAt(sbuf.length() - 1);
      if ((sbuf.length() == 1 && cur != '@')
          || (TERMINATOR.matches(cur))
          || (sbuf.length() == 8
          && !sbuf.toString().equals("@charset")))
      {
        break;
      }
      else if (openQuote == 0 && QUOTES.matches(cur))
      {
        openQuote = cur;
        openQuotePos = sbuf.length();
      }
      else if (openQuote == cur)
      {
        if (QUOTES.matches(cur))
        {
          value = sbuf.substring(openQuotePos, sbuf.length() - 1);
        }
        break;
      }

    }

    unread(Bytes.toArray(unread));
    return value;
  }

  private static String map(byte[] data)
  {
    for (String name : boms.keySet())
    {
      byte[] bom = boms.get(name);
      boolean match = true;
      for (int i = 0; i < bom.length; i++)
      {
        if (data[i] != bom[i])
        {
          match = false;
          break;
        }
      }
      if (match)
      {
        return name;
      }
    }
    return null;
  }

  private enum Endian
  {
    LITTLE, BIG
  }

  /**
   * Get the character set as detected from a BOM. If present, the returned string
   * is one of 'UTF-32BE', 'UTF-32LE", 'UTF-8", 'UTF-16BE' or 'UTF-16LE".
   */

  public final Optional<String> getBomCharset()
  {
    return bom;
  }

  /**
   * Get the value of the CSS @charset rule, if present.
   */
  public final Optional<String> getCssCharset()
  {
    return charset;
  }


}
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

package com.adobe.epubcheck.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

// FIXME 2022 rename to ByteArrayUtils
public class CheckUtil
{
  public static boolean checkString(byte[] arr, int offset, String string)
  {
    try
    {
      byte[] bytes = string.getBytes("UTF-8");
      if (bytes.length + offset > arr.length)
      {
        return false;
      }
      for (int i = 0; i < bytes.length; i++)
      {
        if (arr[offset + i] != bytes[i])
        {
          return false;
        }
      }
      return true;
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace(); // internal problem: UTF-8 not supported??!
      return false;
    }
  }

  /*
    * MimeType already verified to match application/epub+zip. Depending on
    * version, verifying trailing spaces.
    */
  public static boolean checkTrailingSpaces(InputStream input, StringBuilder sb) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);

    int c;
    for (int i = 0; i < 20; i++)
    {
      if ((c = input.read()) == -1)
      {
        return true; // ignored; should be checked by checkEpubHeader() in com.adobe.epubcheck.api.EpubCheck
      }
      else
      {
        baos.write(c);
      }
    }
    if (! baos.toString().equals("application/epub+zip")) {
        return true; // ignored; should be checked by checkEpubHeader() in com.adobe.epubcheck.api.EpubCheck
    }

    int ch = input.read();
    if (ch != -1)
    {
      return false;
    }

    int len;
    byte[] buf = new byte[1024];

    while ((len = input.read(buf)) > 0)
    {
      for (int i = 0; i < len; i++)
      {
        if (buf[i] != ' ')
        {
          return false;
        }
        else
        {
          baos.write(buf[i]);
        }
      }
    }
    sb.append(baos.toString());
    baos.close();

    return true;
  }

  public static int readBytes(InputStream in, byte[] b, int off, int len) throws
      IOException
  {
    if (len < 1)
    {
      throw new InvalidParameterException(Integer.toString(len));
    }
    int total = 0;
    while (total < len)
    {
      int result = in.read(b, off + total, len - total);
      if (result == -1)
      {
        break;
      }
      total += result;
    }
    return total;
  }
}

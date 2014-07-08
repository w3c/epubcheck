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

import java.io.UnsupportedEncodingException;
import com.google.common.base.Objects;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a CSS source.
 *
 * @author mgylling
 */
public class CssSource
{
  private final String systemID;
  private final CssInputStream stream;

  public CssSource(String systemID, InputStream input) throws
      IOException
  {
    this.systemID = checkNotNull(systemID);
    this.stream = checkNotNull(input) instanceof CssInputStream
        ? (CssInputStream) input
        : new CssInputStream(input);
  }

  public CssSource(String systemID, CharSequence input) throws
      IOException
  {
    this.systemID = checkNotNull(systemID);
    this.stream = new CssInputStream(new ByteArrayInputStream(input.toString().getBytes()));
  }

  public String getSystemID()
  {
    return systemID;
  }

  public CssInputStream getInputStream()
  {
    return stream;
  }

	public Reader newReader() 
	{		
		String enc = "utf-8";
		if (stream.bom.isPresent()) {
			enc = stream.bom.get();
		} else if (stream.charset.isPresent()) {
			enc = stream.charset.get();
		}
		try
		{
			return new BufferedReader(new InputStreamReader(stream, enc));
		}
		catch (UnsupportedEncodingException e) 
		{
		  //TODO log/errout
          return new BufferedReader(new InputStreamReader(stream));
	    }	
  }

  @Override
  public String toString()
  {
    return Objects.toStringHelper(this).addValue(systemID).toString();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof CssSource)
    {
      CssSource cs = (CssSource) obj;
      return cs.systemID.equals(this.systemID);
    }
    return false;
  }
}

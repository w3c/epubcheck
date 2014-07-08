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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Stack;
import java.util.StringTokenizer;

public class PathUtil
{
  static final String workingDirectory  = System.getProperty("user.dir");

  public static String resolveRelativeReference(String base, String ref,
			String baseRewrite) throws IllegalArgumentException {

    //baseRewrite is null unless head/base or xml:base is set in the instance
    String actualBase = base;
    if (baseRewrite != null && baseRewrite.length() > 0 && !baseRewrite.equals("."))
    {

      actualBase = baseRewrite;
    }

    if (ref.startsWith("data:") || ref.startsWith("http:"))
    {
      return ref;
    }
    try
    {
      ref = URLDecoder.decode(ref.replace("+", "%2B"), "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      // UTF-8 is guaranteed to be supported
      throw new InternalError(e.toString());
    }

    if (ref.startsWith("#"))
    {
      int index = actualBase.indexOf("#");
      if (index < 0)
      {
        ref = actualBase + ref;
      }
      else
      {
        ref = actualBase.substring(0, index) + ref;
      }
    }
    else
    {
      int index = actualBase.lastIndexOf("/");
      ref = actualBase.substring(0, index + 1) + ref;
    }
    return normalizePath(ref);
  }

	public static String normalizePath(String path)throws IllegalArgumentException 
	{
			
		// Test for any ../ or ./
		if (!path.contains("./"))
		{
			return path;
    }

    Stack<String> pathSegments = new Stack<String>();
    StringTokenizer tokenizer = new StringTokenizer(path, "/");
    while (tokenizer.hasMoreTokens())
    {
      String pathSegment = tokenizer.nextToken();
			if (".".equals(pathSegment))
			{
			  continue;
			}
			if ("..".equals(pathSegment)) 
			{
				if (pathSegments.size() == 0)
				{
					throw new IllegalArgumentException("Invalid path: " + path);
				}
				pathSegments.pop();
			}
		    else
		    {
				pathSegments.push(pathSegment);
			}
		}
		StringBuilder sb = new StringBuilder(path.length());
		int len = pathSegments.size();
		for (int i = 0; i < len; i++)
    {
			if (i != 0)
      {
        sb.append('/');
      }
			sb.append(pathSegments.elementAt(i));
		}
		return sb.toString();
	}

  public static String removeWorkingDirectory(String path)
  {
    if (path == null || path.length() == 0)
    {
      return path;
    }
    return path.replace(workingDirectory, ".");
  }


  public static String removeAnchor(String href)
  {
    int index = href.indexOf("#");
    if (index == -1)
    {
      return href;
    }
    return (href.substring(0, index));
  }
}

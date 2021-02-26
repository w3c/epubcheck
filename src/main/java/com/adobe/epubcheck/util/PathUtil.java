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
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

// Note: EPUBCheck should really use URLs everywhere,
// and use URL normalization/relativization algorithms
// This class should probably be entirely refactored at some point 
public class PathUtil
{

  private static final Pattern REGEX_URI_SCHEME = Pattern
      .compile("^\\p{Alpha}(\\p{Alnum}|\\.|\\+|-)*:");
  private static final Pattern REGEX_URI = Pattern
      .compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
  private static final Pattern REGEX_URI_FRAGMENT = Pattern.compile("#");
  private static final Pattern REGEX_REMOTE_URI = Pattern.compile("^[^:/?#]+://.*");

  public static boolean isRemote(String path)
  {
    return REGEX_REMOTE_URI.matcher(Preconditions.checkNotNull(path)).matches();
  }

  public static String resolveRelativeReference(String base, String ref)
    throws IllegalArgumentException
  {
    Preconditions.checkNotNull(ref);

    // If we can find a URI scheme, return ref
    if (REGEX_URI_SCHEME.matcher(ref).lookingAt())
    {
      return ref;
    }
    if (base == null)
    {
      return normalizePath(ref);
    }

    try
    {
      ref = URLDecoder.decode(ref.replace("+", "%2B"), "UTF-8");
    } catch (UnsupportedEncodingException e)
    {
      // UTF-8 is guaranteed to be supported
      throw new InternalError(e.toString());
    }

    // Normalize base
    base = normalizePath(REGEX_URI_FRAGMENT.split(base)[0]);

    if (ref.startsWith("#"))
    {
      ref = base + normalizePath(ref);
    }
    else
    {
      ref = base.substring(0, base.lastIndexOf("/") + 1) + normalizePath(ref);
    }
    return normalizePath(ref);
  }

  public static String normalizePath(String path)
    throws IllegalArgumentException
  {
    Preconditions.checkNotNull(path);

    if (path.startsWith("data:"))
    {
      return path;
    }

    Matcher matcher = REGEX_URI.matcher(path);
    String prepath = "";
    String postpath = "";
    if (matcher.matches())
    {
      prepath = ((matcher.group(1) != null) ? matcher.group(1) : "")
          + ((matcher.group(3) != null) ? matcher.group(3) : "");
      path = matcher.group(5);
      postpath = ((matcher.group(6) != null) ? matcher.group(6) : "")
          + ((matcher.group(8) != null) ? matcher.group(8) : "");
    }

    Stack<String> segments = new Stack<String>();
    Iterator<String> tokenized = Splitter.on('/').trimResults().split(path).iterator();
    while (tokenized.hasNext())
    {
      String segment = (String) tokenized.next();
      switch (segment)
      {
      case ".":
        if (!tokenized.hasNext()) segments.push("");
        break;
      case "":
        if (segments.empty() || !tokenized.hasNext()) segments.push("");
        break;
      case "..":
        if (segments.size() > 0 && !"..".equals(segments.peek()) && !"".equals(segments.peek()))
        {
          segments.pop();
        }
        else
        {
          segments.push(segment);
        }
        if (!tokenized.hasNext()) segments.push("");
        break;
      default:
        segments.push(segment);
        break;
      }
    }
    return prepath + Joiner.on('/').join(segments) + postpath;
  }

  public static String removeWorkingDirectory(String path)
  {
    if (path == null || path.length() == 0)
    {
      return path;
    }
    String workingDirectory = System.getProperty("user.dir");
    if ("/".equals(workingDirectory) || !path.startsWith(workingDirectory)) {
      return path;
    }
    return ".".concat(path.substring(workingDirectory.length()));
  }

  public static String getFragment(String uri)
  {
    int hash = Preconditions.checkNotNull(uri).indexOf("#") + 1;
    return (hash > 0) ? Strings.emptyToNull(uri.substring(hash)) : null;
  }

  public static String removeFragment(String uri)
  {
    int hash = Preconditions.checkNotNull(uri).indexOf("#");
    return (hash > -1) ? uri.substring(0, hash) : uri;
  }
}

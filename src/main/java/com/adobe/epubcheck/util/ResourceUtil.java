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

import java.io.InputStream;
import java.net.URL;

public class ResourceUtil
{
  public static String getResourcePath(String localName)
  {
    String classPath = ResourceUtil.class.getName().replace('.', '/');
    String classPackage = classPath
        .substring(0, classPath.lastIndexOf("/"));
    String projectPackage = classPackage.substring(0,
        classPackage.lastIndexOf("/"));
    return projectPackage + "/" + localName;
  }

  public static String getExtension(String path)
  {
    int index = path.lastIndexOf(".");
    if (index > 0)
    {
      return path.substring(index + 1);
    }
    return null;
  }

  public static InputStream getResourceStream(String resourcePath)
  {
    ClassLoader loader = ResourceUtil.class.getClassLoader();
    if (loader == null)
    {
      return ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    else
    {
      return loader.getResourceAsStream(resourcePath);
    }
  }

  public static URL getResourceURL(String resourcePath)
  {
    ClassLoader loader = ResourceUtil.class.getClassLoader();
    if (loader == null)
    {
      return ClassLoader.getSystemResource(resourcePath);
    }
    else
    {
      return loader.getResource(resourcePath);
    }
  }

}

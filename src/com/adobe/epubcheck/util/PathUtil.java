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

public class PathUtil {

	public static String resolveRelativeReference(String base, String ref)
			throws IllegalArgumentException {
		try {
			ref = URLDecoder.decode(ref, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(e.toString()); // UTF-8 is guarateed to be
			// supported
		}
		if (ref.startsWith("#")) {
			int index = base.indexOf("#");
			if (index < 0)
				ref = base + ref;
			else
				ref = base.substring(0, index) + ref;
		} else {
			int index = base.lastIndexOf("/");
			ref = base.substring(0, index + 1) + ref;
		}
		return normalizePath(ref);
	}

	public static String normalizePath(String path)
			throws IllegalArgumentException {
		if (path.indexOf("..") < 0)
			return path;
		Stack pathSegments = new Stack();
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		while (tokenizer.hasMoreTokens()) {
			String pathSegment = tokenizer.nextToken();
			if (pathSegment.equals("."))
				continue;
			if (pathSegment.equals("..")) {
				if (pathSegments.size() == 0)
					throw new IllegalArgumentException("Invalid path: " + path);
				pathSegments.pop();
			} else
				pathSegments.push(pathSegment);
		}
		StringBuffer sb = new StringBuffer();
		String sep = "";
		int len = pathSegments.size();
		for (int i = 0; i < len; i++) {
			sb.append(sep);
			sb.append(pathSegments.elementAt(i));
			sep = "/";
		}
		return sb.toString();
	}
}

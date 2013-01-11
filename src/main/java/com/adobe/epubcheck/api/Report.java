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

package com.adobe.epubcheck.api;

import com.adobe.epubcheck.util.FeatureEnum;

/**
 * Interface that is used to report issues found in epub.
 */
public interface Report {

	/**
	 * Called when a violation of the standard is found in epub.
	 * 
	 * @param resource
	 *            name of the resource in the epub zip container that caused
	 *            error or null if the error is on the container level.
	 * @param line
	 *            line number in the resource which has caused error (lines
	 *            start with 1), non-positive number if the resource is not text
	 *            or line is not available.
	 * @param message
	 *            error message.
	 */
	public void error(String resource, int line, int column, String message);

	/**
	 * Called when some notable issue is found in epub.
	 * 
	 * @param resource
	 *            name of the resource in the epub zip container that caused
	 *            warning or null if the error is on the container level.
	 * @param line
	 *            line number in the resource which has caused warning (lines
	 *            start with 1), non-positive number if the resource is not text
	 *            or line is not available.
	 * @param message
	 *            warning message.
	 */
	public void warning(String resource, int line, int column, String message);

	public void exception(String resource, Exception e);

	public int getErrorCount();

	public int getWarningCount();

	public int getExceptionCount();
	
	/**
	 * Called when when a feature is found in epub.
     *
     * @param resource
     *            name of the resource in the epub zip container that has this feature
     *             or null if the feature is on the container level.
     * @param feature
     *            a keyword to know what kind of feature has been found
     * @param value
     *            value found
	 */
	public void info(String resource, FeatureEnum feature, String value);
}

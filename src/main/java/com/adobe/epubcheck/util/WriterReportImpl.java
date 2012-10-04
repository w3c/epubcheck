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

import java.io.PrintWriter;

import com.adobe.epubcheck.api.Report;

public class WriterReportImpl implements Report {

	private PrintWriter out;

	private int errorCount, warningCount, exceptionCount;

	public WriterReportImpl(PrintWriter out) {
		this.out = out;
		errorCount = 0;
		warningCount = 0;
		exceptionCount = 0;
	}

	public WriterReportImpl(PrintWriter out, String info) {
		this.out = out;
		warning("", 0, 0, info);
		errorCount = 0;
		warningCount = 0;
		exceptionCount = 0;
	}

	private String fixMessage(String message) {
		return message.replaceAll("[\\s]+", " ");
	}

	public void error(String resource, int line, int column, String message) {
		errorCount++;
		message = fixMessage(message);
		out.println("ERROR: "
				+ (resource == null ? "[top level]" : resource)
				+ (line <= 0 ? "" : "(" + line
						+ (column <= 0 ? "" : "," + column) + ")") + ": "
				+ message);
	}

	public void warning(String resource, int line, int column, String message) {
		warningCount++;
		message = fixMessage(message);
		out.println("WARNING: "
				+ (resource == null ? "[top level]" : resource)
				+ (line <= 0 ? "" : "(" + line
						+ (column <= 0 ? "" : "," + column) + ")") + ": "
				+ message);
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public void exception(String resource, Exception e) {
		exceptionCount++;
		out.println("EXCEPTION: " + (resource == null ? "" : "/" + resource)
				+ e.getMessage());
	}

	public int getExceptionCount() {
		return exceptionCount;
	}

	@Override
    public void info(String resource, FeatureEnum feature, String value) {
	    if (feature == FeatureEnum.FORMAT_VERSION) {
            out.println("INFO: " + String.format(Messages.VALIDATING_VERSION_MESSAGE, value));
        }
    }

}

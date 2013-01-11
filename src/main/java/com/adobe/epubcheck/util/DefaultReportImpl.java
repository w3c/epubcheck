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

import com.adobe.epubcheck.api.Report;

public class DefaultReportImpl implements Report {

    private static boolean DEBUG = false;
	private String ePubName;
	private int errorCount, warningCount, exceptionCount;

	public DefaultReportImpl(String ePubName) {
		this.ePubName = ePubName;
		errorCount = 0;
		warningCount = 0;
		exceptionCount = 0;
	}

	public DefaultReportImpl(String ePubName, String info) {
		this.ePubName = ePubName;
		warning("", 0, 0, info);
		errorCount = 0;
		warningCount = 0;
		exceptionCount = 0;
	}

	private String fixMessage(String message) {
		if (message == null)
			return "";
		return message.replaceAll("[\\s]+", " ");
	}

	public void error(String resource, int line, int column, String message) {
		errorCount++;
		message = fixMessage(message);
		System.err.println("ERROR: "
				+ ePubName
				+ (resource == null ? "" : "/" + resource)
				+ (line <= 0 ? "" : "(" + line
						+ (column <= 0 ? "" : "," + column) + ")") + ": "
				+ message);
	}

	public void warning(String resource, int line, int column, String message) {
		warningCount++;
		message = fixMessage(message);
		System.err.println("WARNING: "
				+ ePubName
				+ (resource == null ? "" : "/" + resource)
				+ (line <= 0 ? "" : "(" + line
						+ (column <= 0 ? "" : "," + column) + ")") + ": "
				+ message);
	}

	public void exception(String resource, Exception e) {
		exceptionCount++;
		System.err.println("EXCEPTION: " + ePubName
				+ (resource == null ? "" : "/" + resource) + e.getMessage());
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public int getExceptionCount() {
		return exceptionCount;
	}

    @Override
    public void info(String resource, FeatureEnum feature, String value) {
        switch (feature) {
            case FORMAT_VERSION:
                System.out.println(String.format(Messages.VALIDATING_VERSION_MESSAGE, value));
                break;
            default:
                if (DEBUG) {
                    if (resource == null) {
                        System.out.println("INFO: [" + feature + "]=" + value);
                    } else {
                        System.out.println("INFO: [" + feature + " (" + 
                                resource + ")]=" + value);
                    }
                }
                break;      
        }
    }

}

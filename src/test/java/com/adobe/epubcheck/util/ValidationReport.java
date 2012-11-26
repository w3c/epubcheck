/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

import java.util.ArrayList;

import com.adobe.epubcheck.api.Report;

public class ValidationReport implements Report {

	public class ItemReport {
		String resource;
		int line;
		int column;
		public String message;

		public ItemReport(String resource, int line, int column, String message) {
			this.resource = resource;
			this.line = line;
			this.column = column;
			this.message = message;
		}

	}

	private int errorCount, warningCount, exceptionCount;
	public ArrayList<ItemReport> errorList, warningList, exceptionList, infoList;

    public String fileName;
	String info = "";

	public ValidationReport(String file) {
		errorCount = warningCount = exceptionCount = 0;
		fileName = file;
		errorList = new ArrayList<ItemReport>();
		warningList = new ArrayList<ItemReport>();
		exceptionList = new ArrayList<ItemReport>();
		infoList = new ArrayList<ItemReport>();
	}

	public ValidationReport(String file, String info) {
		errorCount = warningCount = exceptionCount = 0;
		fileName = file;
		if (!info.equals(""))
			info = info + "\n";
		this.info = info;
		errorList = new ArrayList<ItemReport>();
		warningList = new ArrayList<ItemReport>();
		exceptionList = new ArrayList<ItemReport>();
        infoList = new ArrayList<ItemReport>();
	}

	public void error(String resource, int line, int column, String message) {
		errorCount++;
		ItemReport item = new ItemReport(resource, line, column,
				fixMessage(message));
		errorList.add(item);
	}

	public void warning(String resource, int line, int column, String message) {
		warningCount++;
		ItemReport item = new ItemReport(resource, line, column,
				fixMessage(message));
		warningList.add(item);
	}

	public void exception(String resource, Exception e) {
		exceptionCount++;
		ItemReport item = new ItemReport(resource, 0, 0,
				fixMessage(e.getMessage()));
		exceptionList.add(item);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(fileName + ": " + info);

		buffer.append("Errors: " + errorCount + "; Warnings: " + warningCount
				+ "\n");
		for (int i = 0; i < errorList.size(); i++) {
			ItemReport item = (ItemReport) errorList.get(i);
			buffer.append("ERROR: "
					+ fileName
					+ (item.resource != null ? ":" + item.resource : "")
					+ (item.line > 0 ? "(" + item.line
							+ (item.column > 0 ? "," + item.column : "") + ")"
							: "") + ": " + item.message + "\n");
		}

		for (int i = 0; i < warningList.size(); i++) {
			ItemReport item = (ItemReport) warningList.get(i);
			buffer.append("WARNING: "
					+ fileName
					+ (item.resource != null ? ":" + item.resource : "")
					+ (item.line > 0 ? "(" + item.line
							+ (item.column > 0 ? "," + item.column : "") + ")"
							: "") + ": " + item.message + "\n");
		}
		for (int i = 0; i < exceptionList.size(); i++) {
			ItemReport item = (ItemReport) exceptionList.get(i);
			buffer.append("EXCEPTION: " + fileName
					+ (item.resource != null ? ":" + item.resource : "")
					+ item.message + "\n");
		}
        for (int i = 0; i < infoList.size(); i++) {
            ItemReport item = (ItemReport) infoList.get(i);
            buffer.append("INFO: " + fileName
                    + (item.resource != null ? ":" + item.resource : "")
                    + item.message + "\n");
        }
		return buffer.toString();
	}

	private String fixMessage(String message) {
		if(message==null) return "No message";
		return message.replaceAll("[\\s]+", " ");
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
        ItemReport item = new ItemReport(resource, 0, 0,
                fixMessage("[" + feature + "] " + value));
        infoList.add(item);
    }

    /**
     * @return the infoList
     */
    public ArrayList<ItemReport> getInfoList() {
        return infoList;
    }

    public boolean hasInfoMessage(String msg) {
        for (ItemReport it : infoList) {
            if (it.message.equals(msg)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasWarningMessage(String msg) {
        for (ItemReport it : warningList) {
            if (it.message.equals(msg)) {
                return true;
            }
        }
        return false;
    }
}

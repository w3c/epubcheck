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

package com.adobe.epubcheck.opf;

public class OPFItem {

	String id;

	String path;

	String mimeType;

	String fallback;

	String fallbackStyle;

	String namespace;

	int lineNumber;

	int columnNumber;

	boolean ncx;

	boolean inSpine;

	boolean nav;

	boolean scripted;

	String properties;
	
	boolean linear = true;

	OPFItem(String id, String path, String mimeType, String fallback,
			String fallbackStyle, String namespace, String properties,
			int lineNumber, int columnNumber) {
		this.fallback = fallback;
		this.fallbackStyle = fallbackStyle;
		this.id = id;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.mimeType = mimeType;
		this.namespace = namespace;
		this.path = path;
		this.properties = properties;
	}

	public String getFallback() {
		return fallback;
	}

	public String getFallbackStyle() {
		return fallbackStyle;
	}

	public String getId() {
		return id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getPath() {
		return path;
	}

	public String getProperties() {
		return properties;
	}

	public String getNamespace() {
		return namespace;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public boolean isNcx() {
		return ncx;
	}

	public void setNcx(boolean ncx) {
		this.ncx = ncx;
	}

	public boolean isScripted() {
		return scripted;
	}

	public void setScripted(boolean scripted) {
		this.scripted = scripted;
	}

	public boolean isNav() {
		return nav;
	}

	public void setNav(boolean nav) {
		this.nav = nav;
	}

	public boolean isInSpine() {
		return inSpine;
	}

	public void setInSpine(boolean inSpine) {
		this.inSpine = inSpine;
	}

	/**
	 * Reflects the value of spine/itemref/@linear. Only applies to manifest items
	 * that appear in the spine. 
	 */
	public void setSpineLinear(boolean linear) {
		this.linear = linear;		
	}
	
	/**
	 * Reflects the value of spine/itemref/@linear. Only applies to manifest items
	 * that appear in the spine. 
	 */
	public boolean getSpineLinear() {
		if(!inSpine) throw new IllegalStateException("linear");
		return linear;		
	}
}

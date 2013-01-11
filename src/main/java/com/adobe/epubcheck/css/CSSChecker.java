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

package com.adobe.epubcheck.css;

import java.io.StringReader;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssSource;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.Messages;

public class CSSChecker implements ContentChecker {

	private OCFPackage ocf;
	private Report report;
	private String path; //css file path when Mode.FILE, host path when Mode.STRING
	private XRefChecker xrefChecker;
	private EPUBVersion version;
	private Mode mode;
	
	//Below only used when checking css strings		
	private String value; //css string
	private int line;	//where css string occurs in host
	private int col; //where css string occurs in host
	private boolean isStyleAttribute = false;
	
	/**
	 * Constructor for CSS files.
	 */
	public CSSChecker(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
		this.mode = Mode.FILE;
	}
	
	/**
	 * Constructor for CSS strings (html style attributes and elements) .
	 */
	public CSSChecker(OCFPackage ocf, Report report, String value, boolean isStyleAttribute, String path, int line, int col,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
		this.value = value;
		this.line = line;
		this.col = col;
		this.isStyleAttribute = isStyleAttribute;
		this.mode = Mode.STRING;
	}

	public void runChecks() {
		
		CssSource source = null;
		
		try {
			
			if (this.mode == Mode.FILE && !ocf.hasEntry(path)) {
				report.error(null, 0, 0, String.format(Messages.MISSING_FILE, path));
				return;
			}
									
			if(this.mode == Mode.FILE) {				
				source = new CssSource(this.path, ocf.getInputStream(this.path));				
				String charset;				
				if(source.getInputStream().getBomCharset().isPresent()) {
					charset = source.getInputStream().getBomCharset().get().toLowerCase();					
					if(!charset.equals("utf-8") && !charset.startsWith("utf-16")) {
						report.error(path, -1, -1, String.format(Messages.UTF_NOT_SUPPORTED_BOM, charset));
					}
				}				
				if(source.getInputStream().getCssCharset().isPresent()) {
					charset = source.getInputStream().getCssCharset().get().toLowerCase();
					if(!charset.equals("utf-8") && !charset.startsWith("utf-16")) {
						report.error(path, 0, 0, String.format(Messages.UTF_NOT_SUPPORTED, charset));
					}
				}
			} // Mode.FILE
			else {
				// Mode.STRING
								
			}
			
			CSSHandler handler = new CSSHandler(path, xrefChecker, report, version);
			if(this.mode == Mode.STRING && this.line > -1) {
				handler.setLineOffset(this.line);
			}
			
			if(!isStyleAttribute) {
				if(this.mode == Mode.FILE) {
					new CssParser().parse(source, handler, handler);	
				} else {
					new CssParser().parse(new StringReader(this.value), this.path, handler, handler);
				}
					
			} else {
				new CssParser().parseStyleAttribute(new StringReader(this.value), this.path, handler, handler);
			}
						
		} catch (Exception e) {
			report.error(path, -1, 0, e.getMessage());
		} finally {						
			if(source != null) {
				try{
					source.getInputStream().close();
				} catch (Exception e) {
					
				}
			}
		}

	}

	private enum Mode {FILE, STRING;};
	
}

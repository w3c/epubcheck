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

package com.adobe.epubcheck.ncx;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class NCXChecker implements ContentChecker {

	OCFPackage ocf;

	Report report;

	String path;

	XRefChecker xrefChecker;

	EPUBVersion version;

	static XMLValidator ncxValidator = new XMLValidator("schema/20/rng/ncx.rng");

	static XMLValidator ncxSchematronValidator = new XMLValidator(
			"schema/20/sch/ncx.sch");

	public NCXChecker(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
	}

	public void runChecks() {
		if (!ocf.hasEntry(path))
			report.error(null, 0, 0, "NCX file " + path + " is missing");
		else if (!ocf.canDecrypt(path))
			report.error(null, 0, 0, "NCX file " + path
					+ " cannot be decrypted");
		else {
			// relaxng
			XMLParser ncxParser = null;
			InputStream in = null;
			NCXHandler ncxHandler = null;
			try {
				in = ocf.getInputStream(path);
				ncxParser = new XMLParser(in, path, "",
						report, version);			
				ncxParser.addValidator(ncxValidator);
				ncxHandler = new NCXHandler(ncxParser, path, xrefChecker);
				ncxParser.addXMLHandler(ncxHandler);
				ncxParser.process();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}finally{
				try{
					in.close();
				}catch (Exception e) {
					
				}
			}
			
			// schematron needs to go in a separate step, because of the catch
			// below
			// TODO: do it in a single step
			try {
				in = ocf.getInputStream(path);
				ncxParser = new XMLParser(ocf.getInputStream(path), path,
						"application/x-dtbncx+xml", report, version);
				ncxParser.addValidator(ncxSchematronValidator);
				ncxHandler = new NCXHandler(ncxParser, path, xrefChecker);
				ncxParser.process();
			} catch (Throwable t) {
				report.error(
						path,
						-1,
						0,
						"Failed performing NCX Schematron tests: "
								+ t.getMessage());
			}finally{
				try{
					in.close();
				}catch (Exception e) {
					
				}
			}
		}
	}

}

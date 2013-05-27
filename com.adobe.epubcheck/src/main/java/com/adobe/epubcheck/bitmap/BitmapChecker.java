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

package com.adobe.epubcheck.bitmap;

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.util.CheckUtil;

public class BitmapChecker implements ContentChecker {

	OCFPackage ocf;

	Report report;

	String path;

	String mimeType;

	BitmapChecker(OCFPackage ocf, Report report, String path, String mimeType) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.mimeType = mimeType;
	}

	private void checkHeader(byte[] header) {
		boolean passed;
		if (mimeType.equals("image/jpeg"))
			passed = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
		else if (mimeType.equals("image/gif"))
			passed = header[0] == (byte) 'G' && header[1] == (byte) 'I'
					&& header[2] == (byte) 'F' && header[3] == (byte) '8';
		else if (mimeType.equals("image/png"))
			passed = header[0] == (byte) 0x89 && header[1] == (byte) 'P'
					&& header[2] == (byte) 'N' && header[3] == (byte) 'G';
		else
			passed = true;
		if (!passed)
			report.error(null, 0, 0, "The file " + path
					+ " does not appear to be of type " + mimeType);
	}

	public void runChecks() {
		if (!ocf.hasEntry(path))
			report.error(null, 0, 0, "image file " + path + " is missing");
		else if (!ocf.canDecrypt(path))
			report.error(null, 0, 0, "image file " + path
					+ " cannot be decrypted");
		else {
			InputStream in = null;
			try {
				in = ocf.getInputStream(path);
				byte[] header = new byte[4];
				int rd = CheckUtil.readBytes(in, header, 0, 4);
				if (rd < 4) {
					report.error(null, 0, 0, "image file " + path
							+ " is too short");
				} else {
					checkHeader(header);
				}
			} catch (IOException e) {
				report.error(null, 0, 0, "I/O error reading " + path);
			} finally {
				try {
					in.close();
				} catch (Exception e) {

				}
			}
		}
	}

	

}

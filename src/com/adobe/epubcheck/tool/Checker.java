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
 *    <AdobeIP#0000474>
 */

package com.adobe.epubcheck.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.ocf.OCFChecker;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.Report;

public class Checker {

	public Checker(String epubName, Report report) {
		try {

			FileInputStream epubIn = new FileInputStream(epubName);

			//System.err.println(epubName);

			byte[] header = new byte[58];

			if (epubIn.read(header) != header.length) {
				report.error(null, 0, "cannot read header");
			} else {
				if (header[0] != 'P'
						&& header[1] != 'K') {
					report.error(null, 0, "corrupted ZIP header");
				} else if (!CheckUtil.checkString(header, 30, "mimetype")) {
					report.error(null, 0, "mimetype entry missing or not the first in archive");
				} else if (!CheckUtil.checkString(header, 38, "application/epub+zip")) {
					report.error(null, 0, "mimetype contains wrong type (application/epub+zip expected)");
				}
			}

			epubIn.close();

			ZipFile zip = new ZipFile(new File(epubName));

			OCFChecker checker = new OCFChecker(zip, report);

			checker.runChecks();

			zip.close();

			System.out.println("Finished Check");

		} catch (IOException e) {
			report.error(null, 0, "I/O error: "
					+ e.getMessage());
		}

		report.flush();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("single argument expected");
			return;
		}

		String epubName = args[0];
		Report report = new Report(epubName);

		if (!epubName.endsWith(".epub"))
			report.warning(null, 0, "filename does not include '.epub' suffix");
			
		new Checker(epubName, report);

	}

}

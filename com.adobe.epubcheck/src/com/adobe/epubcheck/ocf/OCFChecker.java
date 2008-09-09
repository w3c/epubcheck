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

package com.adobe.epubcheck.ocf;

import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OCFChecker {

	ZipFile zip;

	Report report;

	HashSet encryptedItemsSet;

	static XMLValidator containerValidator = new XMLValidator(
			"rng/container.rng");

	static XMLValidator encryptionValidator = new XMLValidator(
			"rng/encryption.rng");

	static XMLValidator signatureValidator = new XMLValidator(
			"rng/signatures.rng");

	public OCFChecker(ZipFile zip, Report report) {
		this.zip = zip;
		this.report = report;
	}

	public void runChecks() {

		String rootPath;

		// Validate container.xml
		String containerEntry = "META-INF/container.xml";
		ZipEntry container = zip.getEntry("META-INF/container.xml");
		if (container == null) {
			report.error(null, 0, "META-INF/container.xml is missing");
			return;
		} else {
			XMLParser containerParser = new XMLParser(zip, containerEntry,
					report);
			OCFHandler containerHandler = new OCFHandler(containerParser);
			containerParser.addXMLHandler(containerHandler);
			containerParser.addValidator(containerValidator);
			containerParser.process();
			rootPath = containerHandler.getRootPath();
		}

		// Validate encryption.xml
		String encryptionEntry = "META-INF/encryption.xml";
		ZipEntry encryption = zip.getEntry(encryptionEntry);
		if (encryption == null) {
			// System.out.println("No encryption.xml found!");
			// placeholder! No error is generated if encryption.xml is missing
		} else {
			XMLParser encryptionParser = new XMLParser(zip, encryptionEntry,
					report);
			OCFHandler encryptionHandler = new OCFHandler(encryptionParser);
			encryptionHandler.setPopulateEnryptedItems(true); // put encrypted
																// items in
																// HashSet
			encryptionHandler.setRootBase(processRootPath(rootPath)); // send
																		// the
																		// base
																		// path
																		// for
																		// inside
																		// the
																		// ZIP
			encryptionParser.addXMLHandler(encryptionHandler);
			encryptionParser.addValidator(encryptionValidator);
			encryptionParser.process();
			// retrieve encrypted items hash set
			encryptedItemsSet = encryptionHandler.getEncryptedItems();

		}

		// Validate signatures.xml
		String signatureEntry = "META-INF/signatures.xml";
		ZipEntry signatures = zip.getEntry(signatureEntry);
		if (signatures == null) {
			// System.out.println("No signatures.xml found!");
			// placeholder! No error is generated if signature.xml is missing
		} else {
			XMLParser signatureParser = new XMLParser(zip, signatureEntry,
					report);
			OCFHandler signatureHandler = new OCFHandler(signatureParser);
			signatureParser.addXMLHandler(signatureHandler);
			signatureParser.addValidator(signatureValidator);
			signatureParser.process();
		}

		OPFChecker opfChecker = new OPFChecker(zip, report, rootPath);
		opfChecker.setEncryptedItemsSet(encryptedItemsSet); // pass encrypted
															// items hash set to
															// OPFChecker
		opfChecker.runChecks();
	}

	/**
	 * This method processes the rootPath String and returns the base path to
	 * the directory that contains the OPF content file.
	 * 
	 * @param rootPath
	 *            path+name of OPF content file
	 * @return String containing path to OPF content file's directory inside ZIP
	 */
	public String processRootPath(String rootPath) {
		String rootBase = rootPath;
		if (rootPath.endsWith(".opf")) {
			int slash = rootPath.lastIndexOf("/");
			if (slash < rootPath.lastIndexOf("\\"))
				slash = rootPath.lastIndexOf("\\");
			if (slash >= 0 && (slash + 1) < rootPath.length())
				rootBase = rootPath.substring(0, slash + 1);
			else
				rootBase = rootPath;
			return rootBase;
		} else {
			System.out.println("RootPath is not an OPF file");
			return null;
		}
	}
}

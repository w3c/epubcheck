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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.OPFHandler;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OCFChecker {

	private OCFPackage ocf;

	private Report report;
	
	private EPUBVersion version;

	// Hashtable encryptedItems;

	// private EPUBVersion version = EPUBVersion.VERSION_3;

	static XMLValidator containerValidator = new XMLValidator(
			"schema/20/rng/container.rng");

	static XMLValidator encryptionValidator = new XMLValidator(
			"schema/20/rng/encryption.rng");

	static XMLValidator signatureValidator = new XMLValidator(
			"schema/20/rng/signatures.rng");

	static XMLValidator containerValidator30 = new XMLValidator(
			"schema/30/ocf-container-30.rnc");

	static XMLValidator encryptionValidator30 = new XMLValidator(
			"schema/30/ocf-encryption-30.rnc");

	static XMLValidator signatureValidator30 = new XMLValidator(
			"schema/30/ocf-signatures-30.rnc");

	private static HashMap<OPSType, XMLValidator> xmlValidatorMap;
	static {
		HashMap<OPSType, XMLValidator> map = new HashMap<OPSType, XMLValidator>();
		map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_2),
				containerValidator);
		map.put(new OPSType(OCFData.containerEntry, EPUBVersion.VERSION_3),
				containerValidator30);

		map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_2),
				encryptionValidator);
		map.put(new OPSType(OCFData.encryptionEntry, EPUBVersion.VERSION_3),
				encryptionValidator30);

		map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_2),
				signatureValidator);
		map.put(new OPSType(OCFData.signatureEntry, EPUBVersion.VERSION_3),
				signatureValidator30);

		xmlValidatorMap = map;
	}

	public OCFChecker(OCFPackage ocf, Report report, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.version = version;
	}

	public void runChecks() {


		if (!ocf.hasEntry(OCFData.containerEntry)) {
			report.error(null, 0, 0,
					"Required META-INF/container.xml resource is missing");
			return;
		}
		long l = ocf.getTimeEntry(OCFData.containerEntry);
		if (l > 0) {
		    Date d = new Date(l);
		    String formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(d);
    		report.info(OCFData.containerEntry, FeatureEnum.CREATION_DATE, formattedDate);
		}
		OCFData containerHandler = ocf.getOcfData(report);

		// retrieve the paths of root files
		List<String> opfPaths = containerHandler.getEntries(OPFData.OPF_MIME_TYPE);
		if (opfPaths==null || opfPaths.isEmpty()) {
			report.error(OCFData.containerEntry, -1, -1,
					"No rootfile with media type 'application/oebps-package+xml'");
			return;
		} else if (opfPaths.size()>1) {
			report.info(null, FeatureEnum.EPUB_RENDITIONS_COUNT, Integer.toString(opfPaths.size()));
		}

		
		// Detect the version of the first root file
		// and compare with the asked version (if set)
		EPUBVersion detectedVersion = null;
		EPUBVersion validationVersion;
		try {
			OPFData opfData = ocf.getOpfData(containerHandler, report).get(
					opfPaths.get(0));
			detectedVersion = opfData.getVersion();
		} catch (InvalidVersionException e) {
			report.error(opfPaths.get(0), -1, -1, e.getMessage());
			return;
		} catch (IOException e1) {
			// missing file will be reported later
		}
		if (version == null && detectedVersion == null) {
			report.warning(opfPaths.get(0),-1,-1,
					String.format(Messages.VERSION_NOT_FOUND, EPUBVersion.VERSION_3));
			validationVersion = EPUBVersion.VERSION_3;
		} else if (version != null && version != detectedVersion) {
			report.warning(opfPaths.get(0), -1, -1, 
					String.format(Messages.VERSION_MISMATCH, version, detectedVersion));
			validationVersion = version;
		} else {
			validationVersion = detectedVersion;
		}
		
		// EPUB 2.0 says there SHOULD be only one OPS rendition
		if (validationVersion == EPUBVersion.VERSION_2 && opfPaths.size()>1) {
			report.warning(null, -1, -1, Messages.MULTIPLE_OPS_RENDITIONS);
		}

		
		// Check the mimetype file
		InputStream mimetype = null;
		try {
			mimetype = ocf.getInputStream("mimetype");
			StringBuilder sb = new StringBuilder(2048);
			if (ocf.hasEntry("mimetype")
					&& !CheckUtil.checkTrailingSpaces(mimetype,
							validationVersion, sb))
				report.error("mimetype", 0, 0,
						"Mimetype file should contain only the string \"application/epub+zip\".");
			if (sb.length() != 0) {
				report.info(null, FeatureEnum.FORMAT_NAME, sb.toString().trim());
			}
		} catch (IOException e) {
			// missing file will be reported later
		} finally {
			try {
				if (mimetype!=null) mimetype.close();
			} catch (Exception e) {}
		}
		

		// Validate the OCF files against the schema definitions
		validate(validationVersion);
				
		
		// Validate each OPF and keep a reference of the OPFHandler
		List<OPFHandler> opfHandlers = new LinkedList<OPFHandler>();
		for (String opfPath : opfPaths) {
			if (!ocf.hasEntry(opfPath)) {
				report.error(OCFData.containerEntry, -1, -1,
						"Entry '" + opfPath + "' not found in the container.");
			} else {
				OPFChecker opfChecker;

				if (validationVersion == EPUBVersion.VERSION_2)
					opfChecker = new OPFChecker(ocf, report, opfPath,
							validationVersion);
				else
					opfChecker = new OPFChecker30(ocf, report, opfPath,
							validationVersion);
				opfChecker.runChecks();
				opfHandlers.add(opfChecker.getOPFHandler());
			}
		}
		
		// Check all file and directory entries in the container
		try {
			for (String entry : ocf.getFileEntries()) {
				if (!entry.startsWith("META-INF/")
						&& !entry.startsWith("META-INF\\")
						&& !entry.equals("mimetype")
						&& !containerHandler.getEntries().contains(entry)) {
					boolean isDeclared = false;
					for (OPFHandler opfHandler : opfHandlers) {
						if (opfHandler.getItemByPath(entry) !=null) {
							isDeclared = true;
							break;
						}
					}
					if (!isDeclared)
						report.warning(null,-1,-1,
								"item ("+ entry
										+ ") exists in the zip file, but is not declared in the OPF file");
				}
				OCFFilenameChecker.checkCompatiblyEscaped(entry, report, validationVersion);
			}

			for (String directory : ocf.getDirectoryEntries()) {
				boolean hasContents = false;
				for (String file : ocf.getFileEntries()) {
					if (file.startsWith(directory)) {
						hasContents = true;
						break;
					}
				}
				if (!hasContents) {
					report.warning(null, -1, -1,
							"zip file contains empty directory " + directory);
				}

			}

		} catch (IOException e) {
			report.error(null, -1, -1, "Unable to read zip file entries.");
		}
		
	}

	public boolean validate(EPUBVersion version) {
		XMLParser parser = null;
		InputStream in = null;
		try {
			
			// validate container
			in = ocf.getInputStream(OCFData.containerEntry);
			parser = new XMLParser(in, OCFData.containerEntry, "xml", report, version);
			XMLHandler handler = new OCFHandler(parser);
			parser.addXMLHandler(handler);
			parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.containerEntry, version)));
			parser.process();
			try{ in.close(); } catch (Exception e) {}

			// Validate encryption.xml
			if (ocf.hasEntry(OCFData.encryptionEntry)) {
				in = ocf.getInputStream(OCFData.encryptionEntry);
				parser = new XMLParser(in, OCFData.encryptionEntry, "xml", report, version);
				handler = new EncryptionHandler(ocf, parser);
				parser.addXMLHandler(handler);
				parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.encryptionEntry, version)));
				parser.process();
				try{ in.close(); } catch (Exception e) {}				
                report.info(null, FeatureEnum.HAS_ENCRYPTION, OCFData.encryptionEntry);
			}

			// validate signatures.xml
			if (ocf.hasEntry(OCFData.signatureEntry)) {
				in = ocf.getInputStream(OCFData.signatureEntry);
				parser = new XMLParser(in, OCFData.signatureEntry, "xml", report, version);
				handler = new OCFHandler(parser);
				parser.addXMLHandler(handler);
				parser.addValidator(xmlValidatorMap.get(new OPSType(OCFData.signatureEntry, version)));
				parser.process();
				try{ in.close(); } catch (Exception e) {}
                report.info(null, FeatureEnum.HAS_SIGNATURES, OCFData.signatureEntry);
			}
			
		} catch (Exception ignore) {
			
		} finally {
			try{
				in.close();
			} catch (Exception e) {
				
			}
	}

		return false;
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
		    report.error(null,  0, 0, "RootPath is not an OPF file");
			//System.out.println("RootPath is not an OPF file");
			return null;
		}
	}

}

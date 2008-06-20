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

import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.ncx.NCXCheckerFactory;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OPFChecker {

	ZipFile zip;

	Report report;

	String path;

	static XMLValidator opfValidator = new XMLValidator("rng/opf.rng");

	XRefChecker xrefChecker;

	static Hashtable contentCheckerFactoryMap;

	static {
		Hashtable map = new Hashtable();
		map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
		map.put("text/html", OPSCheckerFactory.getInstance());
		map.put("text/x-oeb1-document", OPSCheckerFactory.getInstance());
		map.put("image/jpeg", BitmapCheckerFactory.getInstance());
		map.put("image/gif", BitmapCheckerFactory.getInstance());
		map.put("image/png", BitmapCheckerFactory.getInstance());
		map.put("image/svg+xml", OPSCheckerFactory.getInstance());
		map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
		
		contentCheckerFactoryMap = map;
	}

	public OPFChecker(ZipFile zip, Report report, String path) {
		this.zip = zip;
		this.report = report;
		this.path = path;
		this.xrefChecker = new XRefChecker(zip, report);
	}

	public void runChecks() {
		ZipEntry opfEntry = zip.getEntry(path);
		if (opfEntry == null)
			report.error(null, 0, "OPF file " + path + " is missing");
		else {
			XMLParser opfParser = new XMLParser(zip, path, report);
			OPFHandler opfHandler = new OPFHandler(opfParser, path);
			opfParser.addXMLHandler(opfHandler);
			opfParser.addValidator(opfValidator);
			opfParser.process();

			int itemCount = opfHandler.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				OPFItem item = opfHandler.getItem(i);
				try {
					xrefChecker.registerResource(item.getPath(), item
							.getMimeType());
				} catch (IllegalArgumentException e) {
					report.error(path, item.getLineNumber(), e.getMessage());
				}
				checkItem(item, opfHandler);
			}

			int spineItemCount = opfHandler.getSpineItemCount();
			for (int i = 0; i < spineItemCount; i++) {
				OPFItem item = opfHandler.getSpineItem(i);
				checkSpineItem(item, opfHandler);
			}

			for (int i = 0; i < itemCount; i++) {
				OPFItem item = opfHandler.getItem(i);
				checkItemContent(item, opfHandler);
			}

			xrefChecker.checkReferences();
		}
	}

	static boolean isBlessedItemType(String type) {
		return type.equals("application/xhtml+xml")
		|| type.equals("application/x-dtbook+xml");
		
	}

	static boolean isDeprecatedBlessedItemType(String type) {
		return type.equals("text/x-oeb1-document") || type.equals("text/html");
	}

	static boolean isBlessedStyleType(String type) {
		return type.equals("text/css");
	}

	static boolean isDeprecatedBlessedStyleType(String type) {
		return type.equals("text/x-oeb1-css");
	}

	static boolean isBlessedImageType(String type) {
		return type.equals("image/gif") || type.equals("image/png")
				|| type.equals("image/jpeg") || type.equals("image/svg+xml");
	}

	private void checkItem(OPFItem item, OPFHandler opfHandler) {
		String mimeType = item.getMimeType();
		String fallback = item.getFallback();
		if (mimeType != null) 
		{
			if(mimeType == null || mimeType.equals("")) {
				// Ensures that media-type attribute is not empty
				report.error(path, item.getLineNumber(), "empty media-type attribute");
			}else if(!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+")) {
				/* Ensures that media-type attribute has correct content. 
				 * The media-type must have a type and a sub-type divided by '/'
				 * The allowable content for the media-type attribute is 
				 * defined in RFC4288 section 4.2
				 */
				report.error(path, item.getLineNumber(), "invalid content for media-type attribute");
			} else 
			if (isDeprecatedBlessedItemType(mimeType)
					|| isDeprecatedBlessedStyleType(mimeType)) 
			{
				if (opfHandler.getOpf20PackageFile() && mimeType.equals("text/html"))
					report.warning(path, item.getLineNumber(),
									"text/html is not appropriate for XHTML/OPS, use application/xhtml+xml instead");
				else if (opfHandler.getOpf12PackageFile() && mimeType.equals("text/html"))
						 report.warning(path, item.getLineNumber(),
										"text/html is not appropriate for OEBPS 1.2, use text/x-oeb1-document instead");
				else if (opfHandler.getOpf20PackageFile())
					report.warning(path, item.getLineNumber(),
							"deprecated media-type '" + mimeType + "'");
			}
			if (opfHandler.getOpf12PackageFile() && fallback == null)
			{
				if (isBlessedItemType(mimeType))
					report.warning(path, item.getLineNumber(), "use of OPS media-type '" + mimeType + "' in OEBPS 1.2 context; use text/x-oeb1-document instead");
				else if (isBlessedStyleType(mimeType))
					report.warning(path, item.getLineNumber(), "use of OPS media-type '" + mimeType + "' in OEBPS 1.2 context; use text/x-oeb1-css instead");
			}
		}
		if (fallback != null) {
			OPFItem fallbackItem = opfHandler.getItemById(fallback);
			if (fallbackItem == null)
				report.error(path, item.getLineNumber(),
						"fallback item could not be found");
		}
		String fallbackStyle = item.getFallbackStyle();
		if (fallbackStyle != null) {
			OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
			if (fallbackStyleItem == null)
				report.error(path, item.getLineNumber(),
						"fallback-style item could not be found");
		}
	}

	private void checkItemContent(OPFItem item, OPFHandler opfHandler) {
		String mimeType = item.getMimeType();
		String path = item.getPath();
		if (mimeType != null) {
			ContentCheckerFactory checkerFactory;
			if (item.isNcx())
				checkerFactory = NCXCheckerFactory.getInstance();
			else
				checkerFactory = (ContentCheckerFactory) contentCheckerFactoryMap
						.get(mimeType);
			if (checkerFactory == null)
				checkerFactory = GenericContentCheckerFactory.getInstance();
			if (checkerFactory != null) {
				ContentChecker checker = checkerFactory.newInstance(zip,
						report, path, mimeType, xrefChecker);
				checker.runChecks();
			}
		}
	}

	private void checkSpineItem(OPFItem item, OPFHandler opfHandler) {
		String mimeType = item.getMimeType();
		if (mimeType != null) {
			if (isBlessedStyleType(mimeType)
					|| isDeprecatedBlessedStyleType(mimeType)
					|| isBlessedImageType(mimeType))
				report.error(path, item.getLineNumber(), 
						"'" + mimeType + "' is not a permissible spine media-type");
			else if (!isBlessedItemType(mimeType)
					&& !isDeprecatedBlessedItemType(mimeType)
					&& !checkItemFallbacks(item, opfHandler))
				report.error(path, item.getLineNumber(),
						"non-standard media-type '" + mimeType
								+ "' with no fallback");
		}
	}

	private boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler) {
		String fallback = item.getFallback();
		if (fallback != null) {
			OPFItem fallbackItem = opfHandler.getItemById(fallback);
			if (fallbackItem != null) {
				String mimeType = fallbackItem.getMimeType();
				if (mimeType != null) {
					if (isBlessedItemType(mimeType)
							|| isDeprecatedBlessedItemType(mimeType))
						return true;
					if (checkItemFallbacks(item, opfHandler))
						return true;
				}
			}
		}
		String fallbackStyle = item.getFallbackStyle();
		if (fallbackStyle != null) {
			OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
			if (fallbackStyleItem != null) {
				String mimeType = fallbackStyleItem.getMimeType();
				if (mimeType != null) {
					if (isBlessedStyleType(mimeType)
							|| isDeprecatedBlessedStyleType(mimeType))
						return true;
				}
			}
		}
		return false;
	}

}

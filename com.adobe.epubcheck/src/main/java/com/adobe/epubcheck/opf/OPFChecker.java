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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.bitmap.BitmapCheckerFactory;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.dtbook.DTBookCheckerFactory;
import com.adobe.epubcheck.nav.NavCheckerFactory;
import com.adobe.epubcheck.ncx.NCXCheckerFactory;
import com.adobe.epubcheck.ocf.OCFFilenameChecker;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;

public class OPFChecker implements DocumentValidator {

	OCFPackage ocf;

	Report report;

	String path;

	protected XMLValidator opfValidator = new XMLValidator(
			"schema/20/rng/opf.rng");

	protected XMLValidator opfSchematronValidator = new XMLValidator(
			"schema/20/sch/opf.sch");

	XRefChecker xrefChecker;

	protected Hashtable<String, ContentCheckerFactory> contentCheckerFactoryMap;

	OPFHandler opfHandler = null;

	protected EPUBVersion version;

	protected GenericResourceProvider resourceProvider = null;

	XMLParser opfParser = null;

	private void initContentCheckerFactoryMap() {
		Hashtable<String, ContentCheckerFactory> map = new Hashtable<String, ContentCheckerFactory>();
		map.put("application/xhtml+xml", OPSCheckerFactory.getInstance());
		map.put("text/html", OPSCheckerFactory.getInstance());
		map.put("text/x-oeb1-document", OPSCheckerFactory.getInstance());
		map.put("image/jpeg", BitmapCheckerFactory.getInstance());
		map.put("image/gif", BitmapCheckerFactory.getInstance());
		map.put("image/png", BitmapCheckerFactory.getInstance());
		map.put("image/svg+xml", OPSCheckerFactory.getInstance());
		map.put("application/x-dtbook+xml", DTBookCheckerFactory.getInstance());
		map.put("text/css", CSSCheckerFactory.getInstance());

		contentCheckerFactoryMap = map;
	}

	public OPFChecker(OCFPackage ocf, Report report, String path,
			EPUBVersion version) {
		this.ocf = ocf;
		this.resourceProvider = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = new XRefChecker(ocf, report, version);
		this.version = version;
		initContentCheckerFactoryMap();
	}

	public OPFChecker(String path, GenericResourceProvider resourceProvider,
			Report report) {

		this.resourceProvider = resourceProvider;
		this.report = report;
		this.path = path;
		this.version = EPUBVersion.VERSION_2;
		initContentCheckerFactoryMap();
	}

	public void runChecks() {
		if (!ocf.hasEntry(path)) {
			report.error(null, 0, 0, "OPF file " + path + " is missing");
			return;
		}
		validate();

		if (!opfHandler.checkUniqueIdentExists()) {
			report.error(
					path,
					-1,
					-1,
					"unique-identifier attribute in package element must reference an existing identifier element id");
		}

		int itemCount = opfHandler.getItemCount();
		report.info(null, FeatureEnum.ITEMS_COUNT, Integer.toString(itemCount));
		for (int i = 0; i < itemCount; i++) {
			OPFItem item = opfHandler.getItem(i);
			try {
				xrefChecker.registerResource(item.getPath(),
						item.getMimeType(), item.isInSpine(),
						new FallbackChecker().checkItemFallbacks(item, opfHandler, true),
						new FallbackChecker().checkImageFallbacks(item, opfHandler));
			} catch (IllegalArgumentException e) {
				report.error(path, item.getLineNumber(),
						item.getColumnNumber(), e.getMessage());
			}
			
			report.info(item.getPath(), FeatureEnum.DECLARED_MIMETYPE, item.getMimeType());
			checkItem(item, opfHandler);
		}

		checkGuide();
		checkBindings();

		for (int i = 0; i < itemCount; i++) {
			OPFItem item = opfHandler.getItem(i);

			if (!item.path.startsWith("http://"))
				checkItemContent(item, opfHandler);
		}

		xrefChecker.checkReferences();
	}

	protected void checkBindings() {

	}

	protected void checkGuide() {
		int refCount = opfHandler.getReferenceCount();
		for (int i = 0; i < refCount; i++) {
			OPFReference ref = opfHandler.getReference(i);			
			String itemPath = PathUtil.removeAnchor(ref.getHref());
			OPFItem item = opfHandler.getItemByPath(itemPath);
			if (item == null) {
				report.error(path, ref.getLineNumber(), ref.getColumnNumber(),
						"File listed in reference element in guide was not declared in OPF manifest: "
								+ ref.getHref());
			} else {
				if(!isBlessedItemType(item.mimeType, version) && 
						!isDeprecatedBlessedItemType(item.mimeType)) {
					report.error(path, ref.getLineNumber(), ref.getColumnNumber(),
							"Guide reference to an item that is not a Content Document: "
									+ ref.getHref());
				}
			}
		}
	}
	
	public void initHandler() {
		opfHandler = new OPFHandler(path, report, xrefChecker, opfParser,
				version);
	}
	
	public OPFHandler getOPFHandler() {
		return opfHandler;
	}

	@Override
	public boolean validate() {
		int errorsSoFar = report.getErrorCount();
		int warningsSoFar = report.getWarningCount();
		
		InputStream in = null;
		try {
			in = resourceProvider.getInputStream(path);
			opfParser = new XMLParser(new BufferedInputStream(
					in), path, "opf",
					report, version);
			initHandler();
			opfParser.addXMLHandler(opfHandler);

			opfParser.addValidator(opfValidator);
			opfParser.addValidator(opfSchematronValidator);

			opfParser.process();
		} catch (IOException e) {
			report.error(path, 0, 0, e.getMessage());
		}finally{
			try{
				in.close();
			}catch (Exception e) {

			}
		}

		

		int itemCount = opfHandler.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			OPFItem item = opfHandler.getItem(i);
			OCFFilenameChecker.checkCompatiblyEscaped(item.getPath(),report,version);
			checkItem(item, opfHandler);
		}

		int spineItemCount = opfHandler.getSpineItemCount();
		int nonLinearCount = 0;
		for (int i = 0; i < spineItemCount; i++) {
			OPFItem item = opfHandler.getSpineItem(i);
			checkSpineItem(item, opfHandler);
			if (!item.getSpineLinear()) {
				nonLinearCount++;
			}
		}
		if(nonLinearCount == spineItemCount && spineItemCount > 0) {
			//test > 0 to not trigger this when opf is malformed etc
			report.warning(path, -1, -1, "spine contains only non-linear resources");
		}
		
		if (version == EPUBVersion.VERSION_2) {
			// check for >1 itemrefs to any given spine item
			// http://code.google.com/p/epubcheck/issues/detail?id=182
			List<OPFItem> seen = new ArrayList<OPFItem>();
			for (int i = 0; i < opfHandler.getSpineItemCount(); i++) {
				OPFItem item = opfHandler.getSpineItem(i);
				if(seen.contains(item)) {
					report.error(path, item.getLineNumber(), item.getLineNumber(), 
							"spine contains multiple references to the manifest item with id " 
									+ item.getId());
				} else {
					seen.add(item);
				}
			}
		}
			

		return errorsSoFar == report.getErrorCount()
				&& warningsSoFar == report.getWarningCount();
	}

	public static boolean isBlessedItemType(String type, EPUBVersion version) {
		if (version == EPUBVersion.VERSION_2)
			return type.equals("application/xhtml+xml")
					|| type.equals("application/x-dtbook+xml");
		else
			return type.equals("application/xhtml+xml")
					|| type.equals("image/svg+xml");
	}

	public static boolean isDeprecatedBlessedItemType(String type) {
		return type.equals("text/x-oeb1-document") || type.equals("text/html");
	}

	public static boolean isBlessedStyleType(String type) {
		return type.equals("text/css");
	}

	public static boolean isDeprecatedBlessedStyleType(String type) {
		return type.equals("text/x-oeb1-css");
	}

	public static boolean isBlessedImageType(String type) {
		return type.equals("image/gif") || type.equals("image/png")
				|| type.equals("image/jpeg") || type.equals("image/svg+xml");
	}
	
	public static boolean isBlessedFontMimetype20(String mime) {
        if (mime == null) return false;
        return (mime.startsWith("font/") || mime.startsWith("application/font") 
                || mime.startsWith("application/x-font") 
                || "application/vnd.ms-opentype".equals(mime));
    }

	protected void checkItem(OPFItem item, OPFHandler opfHandler) {
		String mimeType = item.getMimeType();
		String fallback = item.getFallback();
		if (mimeType == null || mimeType.equals("")) {
			// Ensures that media-type attribute is not empty
			// report.error(path, item.getLineNumber(), item.getColumnNumber(),
			// "empty media-type attribute");
		} else if (!mimeType
				.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+")) {
			/*
			 * Ensures that media-type attribute has correct content. The
			 * media-type must have a type and a sub-type divided by '/' The
			 * allowable content for the media-type attribute is defined in
			 * RFC4288 section 4.2
			 */
			// report.error(path, item.getLineNumber(), item.getColumnNumber(),
			// "invalid content for media-type attribute");
		} else if (isDeprecatedBlessedItemType(mimeType)
				|| isDeprecatedBlessedStyleType(mimeType)) {
			if (opfHandler.getOpf20PackageFile()
					&& mimeType.equals("text/html"))
				report.warning(path, item.getLineNumber(),
						item.getColumnNumber(),
						"text/html is not appropriate for XHTML/OPS, use application/xhtml+xml instead");
			else if (opfHandler.getOpf12PackageFile()
					&& mimeType.equals("text/html"))
				report.warning(path, item.getLineNumber(),
						item.getColumnNumber(),
						"text/html is not appropriate for OEBPS 1.2, use text/x-oeb1-document instead");
			else if (opfHandler.getOpf20PackageFile())
				report.warning(path, item.getLineNumber(),
						item.getColumnNumber(), "deprecated media-type '"
								+ mimeType + "'");
		}
		if (opfHandler.getOpf12PackageFile() && fallback == null) {
			if (isBlessedItemType(mimeType, version))
				report.warning(
						path,
						item.getLineNumber(),
						item.getColumnNumber(),
						"use of OPS media-type '"
								+ mimeType
								+ "' in OEBPS 1.2 context; use text/x-oeb1-document instead");
			else if (isBlessedStyleType(mimeType))
				report.warning(
						path,
						item.getLineNumber(),
						item.getColumnNumber(),
						"use of OPS media-type '"
								+ mimeType
								+ "' in OEBPS 1.2 context; use text/x-oeb1-css instead");
		}
		if (fallback != null) {
			OPFItem fallbackItem = opfHandler.getItemById(fallback);
			if (fallbackItem == null)
				report.error(path, item.getLineNumber(),
						item.getColumnNumber(),
						"fallback item could not be found");
		}
		String fallbackStyle = item.getFallbackStyle();
		if (fallbackStyle != null) {
			OPFItem fallbackStyleItem = opfHandler.getItemById(fallbackStyle);
			if (fallbackStyleItem == null)
				report.error(path, item.getLineNumber(),
						item.getColumnNumber(),
						"fallback-style item could not be found");
		}
	}

	protected void checkItemContent(OPFItem item, OPFHandler opfHandler) {
		String mimeType = item.getMimeType();
		String path = item.getPath();
		String properties = item.getProperties();
		
		if (mimeType != null) {
			ContentCheckerFactory checkerFactory;
			if (item.isNcx()) {
				checkerFactory = NCXCheckerFactory.getInstance();
			} else if (item.isNav()) {
				checkerFactory = NavCheckerFactory.getInstance();	
			} else {
				checkerFactory = (ContentCheckerFactory) contentCheckerFactoryMap.get(mimeType);
			}
			
			if (checkerFactory == null)
				checkerFactory = GenericContentCheckerFactory.getInstance();
			if (checkerFactory != null) {
				ContentChecker checker = checkerFactory.newInstance(ocf,
						report, path, mimeType, properties, xrefChecker,
						version);
				checker.runChecks();
			}
		}
	}

	protected void checkSpineItem(OPFItem item, OPFHandler opfHandler) {
		// These checks are okay to be done on <spine> items, but they really
		// should be done on all
		// <manifest> items instead. I am avoiding making this change now
		// pending a few issue
		// resolutions in the EPUB Maint Working Group (e.g. embedded fonts not
		// needing fallbacks).
		// [GC 11/15/09]
		String mimeType = item.getMimeType();
		if (mimeType != null) {
			if (isBlessedStyleType(mimeType)
					|| isDeprecatedBlessedStyleType(mimeType)
					|| isBlessedImageType(mimeType))
				report.error(path, item.getLineNumber(),
						item.getColumnNumber(), "'" + mimeType
								+ "' is not a permissible spine media-type");
			else if (!isBlessedItemType(mimeType, version)
					&& !isDeprecatedBlessedItemType(mimeType)
					&& item.getFallback() == null)
				report.error(path, item.getLineNumber(),
						item.getColumnNumber(), "non-standard media-type '"
								+ mimeType + "' with no fallback");
			else if (!isBlessedItemType(mimeType, version)
					&& !isDeprecatedBlessedItemType(mimeType)
					&& !new FallbackChecker().checkItemFallbacks(item, opfHandler, true))
				report.error(
						path,
						item.getLineNumber(),
						item.getColumnNumber(),
						"non-standard media-type '"
								+ mimeType
								+ "' with fallback to non-spine-allowed media-type");
		}
	}
	
	class FallbackChecker {
		private Set<String> checked;
		
		public FallbackChecker() {
			checked = new HashSet<String>();
		}
		
		protected boolean checkItemFallbacks(OPFItem item, OPFHandler opfHandler, boolean checkFallbackStyle) {
			String fallback = item.getFallback();			
			if (fallback != null) {
				fallback = fallback.trim();
				if(checked.contains(fallback)) {
					report.error(
							path,
							item.getLineNumber(),
							item.getColumnNumber(), "circular reference in fallback chain");
					return false;
				} else {
					checked.add(fallback);
				}
				
				OPFItem fallbackItem = opfHandler.getItemById(fallback);
				if (fallbackItem != null) {
					String mimeType = fallbackItem.getMimeType();
					if (mimeType != null) {
						if (isBlessedItemType(mimeType, version)
								|| isDeprecatedBlessedItemType(mimeType)) {						
							return true;
						}	
						if (checkItemFallbacks(fallbackItem, opfHandler, checkFallbackStyle)) {
							return true;						
						}
							
					}
				}								
			}
			if(!checkFallbackStyle) {
				return false;
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
	
		protected boolean checkImageFallbacks(OPFItem item, OPFHandler opfHandler) {
			String fallback = item.getFallback();
			if (fallback != null) {
				fallback = fallback.trim();
				if(checked.contains(fallback)) {
					report.error(
							path,
							item.getLineNumber(),
							item.getColumnNumber(), "circular reference in fallback chain");
					return false;
				} else {
					checked.add(fallback);
				}
				OPFItem fallbackItem = opfHandler.getItemById(fallback);
				if (fallbackItem != null) {
					String mimeType = fallbackItem.getMimeType();
					if (mimeType != null) {
						if (isBlessedImageType(mimeType))
							return true;
						if (checkImageFallbacks(fallbackItem, opfHandler))
							return true;
					}
				}
			}
			return false;
		}
	}
}

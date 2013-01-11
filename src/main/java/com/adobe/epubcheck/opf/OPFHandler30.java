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

package com.adobe.epubcheck.opf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.MetaUtils;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;

public class OPFHandler30 extends OPFHandler {

	HashSet<String> prefixSet;

	boolean reportedUnsupportedXMLVersion;

	static String[] predefinedPrefixes = { "dcterms", "marc", "media", "onix",
			"xsd" };

	static HashSet<String> metaPropertySet;
	static {
		HashSet<String> set = new HashSet<String>();
		set.add("alternate-script");
		set.add("display-seq");
		set.add("file-as");
		set.add("group-position");
		set.add("identifier-type");
		set.add("meta-auth");
		set.add("role");
		set.add("title-type");
		metaPropertySet = set;
	}

	static HashSet<String> itemrefSet;
	static {
		HashSet<String> set = new HashSet<String>();
		set.add("page-spread-right");
		set.add("page-spread-left");
		itemrefSet = set;
	}

	static HashSet<String> linkRelSet;
	static {
		HashSet<String> set = new HashSet<String>();
		set.add("marc21xml-record");
		set.add("mods-record");
		set.add("onix-record");
		set.add("xml-signature");
		set.add("xmp-record");
		linkRelSet = set;
	}

	static HashSet<String> itemPropertySet;
	static {
		HashSet<String> set = new HashSet<String>();
		set.add("cover-image");
		set.add("mathml");
		set.add("nav");
		set.add("remote-resources");
		set.add("scripted");
		set.add("svg");
		set.add("switch");
		itemPropertySet = set;
	}
	static HashMap<String, String> itemPropertyTypeMap;
	static {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("cover-image", "image/gif image/jpeg image/png image/svg+xml");
		map.put("mathml", "application/xhtml+xml image/svg+xml");// ops
		map.put("nav", "application/xhtml+xml");
		map.put("remote-resources",
				"application/xhtml+xml image/svg+xml text/css");// ops + css
		map.put("scripted", "application/xhtml+xml image/svg+xml");// ops
		map.put("svg", "application/xhtml+xml");// ops
		map.put("switch", "application/xhtml+xml image/svg+xml");// ops
		itemPropertyTypeMap = map;
	}

	OPFHandler30(OCFPackage ocf, String path, Report report,
			XRefChecker xrefChecker, XMLParser parser, EPUBVersion version) {
		super(path, report, xrefChecker, parser, version);
		prefixSet = new HashSet<String>();
		reportedUnsupportedXMLVersion = false;
		for (int i = 0; i < predefinedPrefixes.length; i++)
			prefixSet.add(predefinedPrefixes[i]);
	}

	public void startElement() {
		super.startElement();
		if (!reportedUnsupportedXMLVersion)
			reportedUnsupportedXMLVersion = HandlerUtil.checkXMLVersion(parser);
		XMLElement e = parser.getCurrentElement();
		String name = e.getName();

		if (name.equals("package"))
			HandlerUtil.processPrefixes(e.getAttribute("prefix"), prefixSet,
					report, path, parser.getLineNumber(),
					parser.getColumnNumber());
		else if (name.equals("meta"))
			processMeta(e);
		else if (name.equals("link"))
			processLink(e);
		else if (name.equals("item"))
			processItemProperties(e.getAttribute("properties"),
					e.getAttribute("media-type"));
		else if (name.equals("itemref"))
			processItemrefProperties(e.getAttribute("properties"));
		else if (name.equals("mediaType"))
			processBinding(e);
	}

	private void processBinding(XMLElement e) {
		String mimeType = e.getAttribute("media-type");
		String handlerId = e.getAttribute("handler");

		if (mimeType == null || handlerId == null)
			return;

		if (OPFChecker30.isCoreMediaType(mimeType)) {
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), "The media-type " + mimeType
							+ " is a core media type");
			return;
		}

		if (xrefChecker != null
				&& xrefChecker.getBindingHandlerSrc(mimeType) != null) {
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), "The media-type " + mimeType
							+ " has already been assigned a handler");
			return;
		}

		OPFItem handler = itemMapById.get(handlerId);
		if (handler != null && xrefChecker != null)
			xrefChecker.registerBinding(mimeType, handler.path);
	}

	private void processLink(XMLElement e) {
		processLinkRel(e.getAttribute("rel"));
		// needs refactor: its problematic to register
		// link resources as items
		String id = e.getAttribute("id");
		String href = e.getAttribute("href");
		if (href != null && !href.startsWith("http://")) {
			try {
				href = PathUtil.resolveRelativeReference(path, href, null);
			} catch (IllegalArgumentException ex) {
				report.error(path, parser.getLineNumber(),
						parser.getColumnNumber(), ex.getMessage());
				href = null;
			}
		}
        if (href != null && href.startsWith("http")) {
            report.info(path, FeatureEnum.REFERENCE, href);
        }
		String mimeType = e.getAttribute("media-type");

		OPFItem item = new OPFItem(id, href, mimeType, "", "", "", null,
				parser.getLineNumber(), parser.getColumnNumber());

		if (id != null)
			itemMapById.put(id, item);

		//if (href != null) {
		//mgy: awaiting proper refactor, only add these if local 
		if (href != null && !href.startsWith("http://")) {
			itemMapByPath.put(href, item);
			items.add(item);
		}
	}

	private void processItemrefProperties(String property) {
		if (property == null)
			return;
		int propertiesNumber = MetaUtils.validateProperties(property,
				itemrefSet, prefixSet, path, parser.getLineNumber(),
				parser.getColumnNumber(), report, false).size();
		if (propertiesNumber == 2)
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(),
					"itemref can't have both page-spread-right and page-spread-left properties");

	}

	private void processItemProperties(String property, String mimeType) {
		if (property == null)
			return;

		Set<String> properties = MetaUtils.validateProperties(property,
				itemPropertySet, prefixSet, path, parser.getLineNumber(),
				parser.getColumnNumber(), report, false);
		mimeType = mimeType.trim();
		Iterator<String> it = properties.iterator();
		while (it.hasNext()) {
			boolean match = false;
			String propertyValue = it.next();
			String expectedType = itemPropertyTypeMap.get(propertyValue);
			String expectedTypeArray[] = expectedType.split(" ");

			for (int j = 0; j < expectedTypeArray.length; j++)
				if (expectedTypeArray[j].equals(mimeType)) {
					match = true;
					break;
				}
			if (!match)
				report.error(path, parser.getLineNumber(),
						parser.getColumnNumber(), "Item property: "
								+ propertyValue
								+ " is not defined for media type: " + mimeType);

		}
	}

	private void processLinkRel(String rel) {
		if (rel == null)
			return;

		MetaUtils
				.validateProperties(rel, linkRelSet, prefixSet, path,
						parser.getLineNumber(), parser.getColumnNumber(),
						report, false);
	}

	private void processMeta(XMLElement e) {
		processMetaProperty(e.getAttribute("property"));
		processMetaScheme(e.getAttribute("scheme"));
	}

	private void processMetaScheme(String scheme) {
		if (scheme == null)
			return;
		MetaUtils.validateProperties(scheme, null, prefixSet, path,
				parser.getLineNumber(), parser.getColumnNumber(), report, true);
	}

	private void processMetaProperty(String property) {
		if (property == null)
			return;
		MetaUtils.validateProperties(property, metaPropertySet, prefixSet,
				path, parser.getLineNumber(), parser.getColumnNumber(), report,
				true);
	}
}

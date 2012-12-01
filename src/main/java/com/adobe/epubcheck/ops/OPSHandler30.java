package com.adobe.epubcheck.ops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubTypeAttributes;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.MetaUtils;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLParser;

public class OPSHandler30 extends OPSHandler {

	String properties;

	HashSet<String> prefixSet;

	HashSet<String> propertiesSet;

	String mimeType;

	boolean video = false;

	boolean audio = false;

	boolean hasValidFallback = false;

	int imbricatedObjects = 0;

	int imbricatedCanvases = 0;

	public static HashSet<String> linkClassSet;

	boolean reportedUnsupportedXMLVersion;

	static {
		HashSet<String> set = new HashSet<String>();
		set.add("vertical");
		set.add("horizontal");
		set.add("day");
		set.add("night");
		linkClassSet = set;
	}

	public OPSHandler30(OCFPackage ocf, String path, String mimeType, String properties,
			XRefChecker xrefChecker, XMLParser parser, Report report, EPUBVersion version) {
		super(ocf, path, xrefChecker, parser, report, version);
		this.mimeType = mimeType;
		this.properties = properties;
		prefixSet = new HashSet<String>();
		propertiesSet = new HashSet<String>();
		reportedUnsupportedXMLVersion = false;
	}

	boolean checkPrefix(String prefix) {
		prefix = prefix.trim();
		if (!prefixSet.contains(prefix)) {
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), "Undecleared prefix: " + prefix);
			return false;
		}
		return true;
	}

	private void checkType(String type) {
		if (type == null)
			return;
		MetaUtils.validateProperties(type, EpubTypeAttributes.EpubTypeSet,
				prefixSet, path, parser.getLineNumber(),
				parser.getColumnNumber(), report, false);

	}
	
	private void checkSSMLPh(String ph) {
		//issue 139; enhancement is to add real syntax check for IPA and x-SAMPA 
		if(ph == null) 
			return;
		if (ph.trim().length() < 1)
			report.warning(path, parser.getLineNumber(),
				parser.getColumnNumber(), "Empty or whitespace-only value of attribute ssml:ph");
	}

	@Override
	public void characters(char[] chars, int arg1, int arg2) {
		super.characters(chars, arg1, arg2);
		String str = new String(chars, arg1, arg2);
		str = str.trim();
		if (!str.equals("")
				&& (audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0))
			hasValidFallback = true;
	}

	public void startElement() {
		super.startElement();
		
		if (!reportedUnsupportedXMLVersion)
			reportedUnsupportedXMLVersion = HandlerUtil.checkXMLVersion(parser);

		XMLElement e = parser.getCurrentElement();
		String name = e.getName();

		if (name.equals("html"))
			HandlerUtil.processPrefixes(
					e.getAttributeNS("http://www.idpf.org/2007/ops", "prefix"),
					prefixSet, report, path, parser.getLineNumber(),
					parser.getColumnNumber());
		else if (name.equals("link"))
			processLink(e);
		else if (name.equals("object"))
			processObject(e);
		else if (name.equals("math"))
			propertiesSet.add("mathml");
		else if (!mimeType.equals("image/svg+xml") && name.equals("svg"))
			propertiesSet.add("svg");
		else if (name.equals("script"))
			propertiesSet.add("scripted");
		else if (name.equals("switch"))
			propertiesSet.add("switch");
		else if (name.equals("audio"))
			processAudio(e);
		else if (name.equals("video"))
			processVideo(e);
		else if (name.equals("canvas"))
			processCanvas(e);
		else if (name.equals("img"))
			processImg(e);

		processSrc(("source".equals(name)) ? e.getParent().getName() : name, e.getAttribute("src"));

		checkType(e.getAttributeNS("http://www.idpf.org/2007/ops", "type"));
		
		checkSSMLPh(e.getAttributeNS("http://www.w3.org/2001/10/synthesis", "ph"));
	}

	

	private void processLink(XMLElement e) {
		String classAttribute = e.getAttribute("class");
		if (classAttribute == null)
			return;

		Set<String> values = MetaUtils.validateProperties(classAttribute,
				linkClassSet, null, path, parser.getLineNumber(),
				parser.getColumnNumber(), report, false);

		if (values.size() == 1)
			return;

		boolean vertical = false, horizontal = false, day = false, night = false;

		Iterator<String> it = values.iterator();

		while (it.hasNext()) {
			String attribute = it.next();
			if (attribute.equals("vertical"))
				vertical = true;
			else if (attribute.equals("horizontal"))
				horizontal = true;
			else if (attribute.equals("day"))
				day = true;
			else if (attribute.equals("night"))
				night = true;
		}

		if (vertical && horizontal || day && night)
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), Messages.CONFLICTING_ATTRIBUTES
							+ classAttribute);

	}

	private void processImg(XMLElement e) {
		if ((audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0))
			hasValidFallback = true;
	}

	private void processCanvas(XMLElement e) {
		imbricatedCanvases++;
	}

	private void processAudio(XMLElement e) {
		audio = true;
	}

	private void processVideo(XMLElement e) {
		video = true;

		String posterSrc = e.getAttribute("poster");

		String posterMimeType = null;
		if (xrefChecker != null && posterSrc != null)
			posterMimeType = xrefChecker.getMimeType(PathUtil
					.resolveRelativeReference(path, posterSrc, base));

		if (posterMimeType != null
				&& !OPFChecker.isBlessedImageType(posterMimeType))
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(),
					"Video poster must have core media image type");

		if (posterSrc != null) {
			hasValidFallback = true;
			processSrc(e.getName(), posterSrc);
		}

	}

	private void processSrc(String name, String src) {
		
		if (src != null) {
			src.trim();
			if (src.equals(""))
				report.error(path, parser.getLineNumber(),
						parser.getColumnNumber(),
						"The src attribute must not be empty");
		}
						
		if (src == null || xrefChecker == null)
			return;
		
		if (src.startsWith("http://"))
			propertiesSet.add("remote-resources");
		else
			src = PathUtil.resolveRelativeReference(path, src, base);

		int refType;
		if ("audio".equals(name)) {
			refType = XRefChecker.RT_AUDIO;
		} else if ("video".equals(name)) {
			refType = XRefChecker.RT_VIDEO;
		} else {
			refType = XRefChecker.RT_GENERIC;
		}
		xrefChecker.registerReference(path, parser.getLineNumber(),
				parser.getColumnNumber(), src, refType);

		String srcMimeType = xrefChecker.getMimeType(src);

		if (srcMimeType == null)
			return;

		if (!mimeType.equals("image/svg+xml")
				&& srcMimeType.equals("image/svg+xml"))
			propertiesSet.add("svg");

		if ((audio || video || imbricatedObjects > 0 || imbricatedCanvases > 0)
				&& OPFChecker30.isCoreMediaType(srcMimeType)
				&& !name.equals("track"))
			hasValidFallback = true;

	}

	private void processObject(XMLElement e) {
		imbricatedObjects++;

		String type = e.getAttribute("type");
		String data = e.getAttribute("data");

		if (data != null) {
			processSrc(e.getName(), data);
			data = PathUtil.resolveRelativeReference(path, data, base);
		}

		if (type != null && data != null && xrefChecker != null
				&& !type.equals(xrefChecker.getMimeType(data)))
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(),
					"Object type and the item media-type declared in manifest, do not match");

		if (type != null) {
			if (!mimeType.equals("image/svg+xml")
					&& type.equals("image/svg+xml"))
				propertiesSet.add("svg");

			if (OPFChecker30.isCoreMediaType(type))
				hasValidFallback = true;
		}

		if (hasValidFallback)
			return;
		// check bindings
		if (xrefChecker != null && type != null
				&& xrefChecker.getBindingHandlerSrc(type) != null)
			hasValidFallback = true;
	}

		
	@Override
	public void endElement() {
		super.endElement();
		XMLElement e = parser.getCurrentElement();
		String name = e.getName();		
		if (openElements == 0 && (name.equals("html") || name.equals("svg"))) {					
			checkProperties();
		} else if (name.equals("object")) {
			imbricatedObjects--;
			if (imbricatedObjects == 0 && imbricatedCanvases == 0)
				checkFallback("Object");
		} else if (name.equals("canvas")) {
			imbricatedCanvases--;
			if (imbricatedObjects == 0 && imbricatedCanvases == 0)
				checkFallback("Canvas");
		} else if (name.equals("video")) {
			if (imbricatedObjects == 0 && imbricatedCanvases == 0)
				checkFallback("Video");
			video = false;
		} else if (name.equals("audio")) {
			if (imbricatedObjects == 0 && imbricatedCanvases == 0)
				checkFallback("Audio");
			audio = false;
		}
	}

	/*
	 * Checks fallbacks for video, audio and object elements
	 */
	private void checkFallback(String elementType) {
		if (hasValidFallback)
			hasValidFallback = false;
		else
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), elementType
							+ " element doesn't provide fallback");
	}

	private void checkProperties() {
		if (properties != null && properties.equals("singleFileValidation"))
			return;
		if (properties != null) {
			properties = properties.replaceAll("nav", "");
			properties = properties.replaceAll("cover-image", "");
		}
			 
		Iterator<String> propertyIterator = propertiesSet.iterator();
		while (propertyIterator.hasNext()) {
			String prop = propertyIterator.next();
			if (properties != null && properties.contains(prop))
				properties = properties.replaceAll(prop, "");
			else
				report.error(path, 0, 0,
						"This file should declare in opf the property: " + prop);
		}
		if (properties != null)
			properties = properties.trim();
		if (properties != null && !properties.equals(""))			
			report.error(path, 0, 0,
					"This file should not declare in opf the properties: "
							+ properties);

	}
}

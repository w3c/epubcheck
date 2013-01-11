package com.adobe.epubcheck.overlay;

import java.util.HashSet;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EpubTypeAttributes;
import com.adobe.epubcheck.util.HandlerUtil;
import com.adobe.epubcheck.util.MetaUtils;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OverlayHandler implements XMLHandler {

	String path;

	XRefChecker xrefChecker;

	Report report;

	HashSet<String> prefixSet;

	XMLParser parser;

	boolean reportedUnsupportedXMLVersion;

	public OverlayHandler(String path, XRefChecker xrefChecker,
			XMLParser parser, Report report) {
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.report = report;
		this.parser = parser;
		prefixSet = new HashSet<String>();
		reportedUnsupportedXMLVersion = false;
	}

	public void startElement() {
		if (!reportedUnsupportedXMLVersion)
			reportedUnsupportedXMLVersion = HandlerUtil.checkXMLVersion(parser);

		XMLElement e = parser.getCurrentElement();
		String name = e.getName();

		if (name.equals("smil"))
			HandlerUtil.processPrefixes(
					e.getAttributeNS("http://www.idpf.org/2007/ops", "prefix"),
					prefixSet, report, path, parser.getLineNumber(),
					parser.getColumnNumber());
		else if (name.equals("seq"))
			processSeq(e);
		else if (name.equals("text"))
			processSrc(e);
		else if (name.equals("audio"))
			processRef(e.getAttribute("src"), XRefChecker.RT_AUDIO);
		else if (name.equals("body") || name.equals("par"))
			checkType(e.getAttributeNS("http://www.idpf.org/2007/ops", "type"));
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

	private void processSrc(XMLElement e) {
		processRef(e.getAttribute("src"), XRefChecker.RT_HYPERLINK);

	}

	private void processRef(String ref, int type) {
		if (ref != null && xrefChecker != null) {
			ref = PathUtil.resolveRelativeReference(path, ref, null);
			if (type == XRefChecker.RT_AUDIO) {
				String mimeType = xrefChecker.getMimeType(ref);
				if (mimeType != null
						&& !OPFChecker30.isBlessedAudioType(mimeType))
					report.error(path, parser.getLineNumber(),
							parser.getColumnNumber(),
							"Media Overlay audio refernence " + ref
									+ " to non-standard audio type " + mimeType);
			}
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), ref, type);
		}
	}

	private void processSeq(XMLElement e) {
		processRef(e.getAttributeNS("http://www.idpf.org/2007/ops", "textref"),
				XRefChecker.RT_HYPERLINK);
		checkType(e.getAttributeNS("http://www.idpf.org/2007/ops", "type"));
	}

	public void characters(char[] chars, int arg1, int arg2) {
	}

	public void endElement() {
	}

	public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
	}

	public void processingInstruction(String arg0, String arg1) {
	}

}

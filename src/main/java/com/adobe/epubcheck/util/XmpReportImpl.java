package com.adobe.epubcheck.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.reporting.CheckMessage;

public class XmpReportImpl extends XmlReportAbstract {

	public XmpReportImpl(PrintWriter out, String ePubName, String versionEpubCheck) {
		super(out, ePubName, versionEpubCheck);
	}

	@SuppressWarnings("unchecked")
	public int generateReport() {
		if (out == null)
			return 1;

		int returnCode = 1;
		int ident = 0;

		generationDate = fromTime(System.currentTimeMillis());
		try {
			output(ident, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			startElement(ident++, "x:xmpmeta", KeyValue.with("xmlns:x", "adobe:ns:meta/"),
			        KeyValue.with("x:xmptk", "Adobe XMP Core 5.1.0-jc003"));
			startElement(ident++, "rdf:RDF", KeyValue.with("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
			List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
			attrs.add(KeyValue.with("rdf:about", ""));
			attrs.add(KeyValue.with("xmlns:dc", "http://purl.org/dc/elements/1.1/"));
			attrs.add(KeyValue.with("xmlns:xmp", "http://ns.adobe.com/xap/1.0/"));
			attrs.add(KeyValue.with("xmlns:xmpTPg", "http://ns.adobe.com/xap/1.0/t/pg/"));
			attrs.add(KeyValue.with("xmlns:stFnt", "http://ns.adobe.com/xap/1.0/sType/Font#"));
			// Unused core-properties like keywords, lastModifiedBy, revision, category 
			// attrs.add(KeyValue.with("xmlns:cp", "http://schemas.openxmlformats.org/package/2006/metadata/core-properties/"));
			// Used extended-properties for Characters 
			// but could be for Words, Lines, Paragraphs, CharactersWithSpaces, Template, DocSecurity, Application, AppVersion
			attrs.add(KeyValue.with("xmlns:extended-properties",
			        "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties/"));
			attrs.add(KeyValue.with("xmlns:premis", "http://www.loc.gov/premis/rdf/v1#"));
			
			if (formatName == null) {
				attrs.add(KeyValue.with("dc:format", "application/octet-stream"));
			} else {
				if (formatVersion == null) {
					attrs.add(KeyValue.with("dc:format", formatName));
				} else {
					attrs.add(KeyValue.with("dc:format", formatName + ";version=" + formatVersion));
				}
			}
			if (creationDate != null) {
				attrs.add(KeyValue.with("xmp:CreateDate", creationDate));
			}
			if (charsCount != 0) {
				attrs.add(KeyValue.with("extended-properties:Characters", Long.toString(charsCount)));
			}
			if (pagesCount != 0) {
				attrs.add(KeyValue.with("xmpTPg:NPages", Long.toString(pagesCount)));
			}
			if (publisher != null) {
				attrs.add(KeyValue.with("dc:publisher", publisher));
			}
			attrs.add(KeyValue.with("dc:identifier", identifier));
			if (language != null) {
				attrs.add(KeyValue.with("dc:language", language));
			}

			startElement(ident++, "rdf:Description", attrs);

			// DC
			if (!creators.isEmpty()) {
				startElement(ident++, "dc:creator");
				startElement(ident++, "rdf:Seq");
				for (String creator : creators) {
					generateElement(ident, "rdf:li", creator);
				}
				endElement(--ident, "rdf:Seq");
				endElement(--ident, "dc:creator");
			}
			if (!titles.isEmpty()) {
				startElement(ident++, "dc:title");
				startElement(ident++, "rdf:Alt");
				boolean first = true;
				for (String title : titles) {
					if (first) {
						generateElement(ident, "rdf:li", title.trim(), KeyValue.with("xml:lang", "x-default"));
						first = false;
					} else {
						generateElement(ident, "rdf:li", title);
					}
				}
				endElement(--ident, "rdf:Alt");
				endElement(--ident, "dc:title");
			}
			if (!subjects.isEmpty()) {
				startElement(ident++, "dc:subject");
				startElement(ident++, "rdf:Bag");
				for (String subject : subjects) {
					generateElement(ident, "rdf:li", subject);
				}
				endElement(--ident, "rdf:Bag");
				endElement(--ident, "dc:subject");
			}

			// Fonts
			if (!embeddedFonts.isEmpty() || !refFonts.isEmpty()) {
				startElement(ident++, "xmpTPg:Fonts");
				startElement(ident++, "rdf:Bag");
				for (String font : embeddedFonts) {
					generateFont(ident, font, true);
				}
				for (String font : refFonts) {
					generateFont(ident, font, false);
				}
				endElement(--ident, "rdf:Bag");
				endElement(--ident, "xmpTPg:Fonts");
			}

			// Premis:event
			startElement(ident++, "premis:hasEvent", KeyValue.with("rdf:parseType", "Resource"));
			generateElement(ident, "premis:hasEventDateTime", generationDate,
					KeyValue.with("rdf:datatype", "http://www.w3.org/2001/XMLSchema#dateTime"));
			generateElement(ident, "premis:hasEventType", null,
			        KeyValue.with("rdf:resource", "http://id.loc.gov/vocabulary/preservation/eventType/val"));
			if (fatalErrors.isEmpty() && errors.isEmpty()) {
				generateElement(ident, "premis:hasEventDetail", "Well-formed");
			} else {
				generateElement(ident, "premis:hasEventDetail", "Not well-formed");
			}
			if (fatalErrors.size() + errors.size() + warns.size() + hints.size() != 0) {
                startElement(ident++, "premis:hasEventOutcomeInformation");
                startElement(ident++, "rdf:Seq");
                
                generateEventOutcome(ident, fatalErrors, "FATAL");
                generateEventOutcome(ident, errors, "ERROR");
                generateEventOutcome(ident, warns, "WARN");
                generateEventOutcome(ident, hints, "HINT");
                
                endElement(--ident, "rdf:Seq");
                endElement(--ident, "premis:hasEventOutcomeInformation");
			}
			startElement(ident++, "premis:hasEventRelatedAgent", KeyValue.with("rdf:parseType", "Resource"));
			generateElement(ident, "premis:hasAgentType", null,
			        KeyValue.with("rdf:resource", "http://id.loc.gov/vocabulary/preservation/agentType/sof"));
			if (epubCheckVersion == null) {
				generateElement(ident, "premis:hasAgentName", epubCheckName);
			} else {
				generateElement(ident, "premis:hasAgentName", epubCheckName + " " + epubCheckVersion);
			}
			endElement(--ident, "premis:hasEventRelatedAgent");

			endElement(--ident, "premis:hasEvent");

			// Significant properties
			startElement(ident++, "premis:hasSignificantProperties");
			startElement(ident++, "rdf:Bag");
			generateSignificantProperty(ident, "renditionLayout", hasFixedLayout ? "fixed-layout" : "reflowable");
			generateSignificantProperty(ident, "isScripted", Boolean.toString(hasScripts));
			generateSignificantProperty(ident, "hasEncryption", Boolean.toString(hasEncryption));
			generateSignificantProperty(ident, "hasAudio", Boolean.toString(hasAudio));
			generateSignificantProperty(ident, "hasVideo", Boolean.toString(hasVideo));
			generateSignificantProperty(ident, "hasSignatures", Boolean.toString(hasSignatures));
			generateSignificantProperty(ident, "hasAllFontsEmbedded", Boolean.toString(refFonts.isEmpty()));
			int nRefs = 0;
			for (String ref : references) {
				nRefs++;
				if (nRefs > 50) {
					generateSignificantProperty(ident, "reference", "" + (references.size() - 50) + " more references");
					break;
				}
				generateSignificantProperty(ident, "reference", ref);
			}
			endElement(--ident, "rdf:Bag");
			endElement(--ident, "premis:hasSignificantProperties");

			endElement(--ident, "rdf:Description");
			endElement(--ident, "rdf:RDF");
			endElement(--ident, "x:xmpmeta");

			returnCode = 0;
		} catch (Exception e) {
			System.err.println("Exception encountered: " + e.getMessage());
			returnCode = 1;
		}
		return returnCode;
	}

	protected void generateFont(int ident, String font, boolean embeded) {
		// stFnt:fontName, stFnt:fontType, stFnt:versionString, stFnt:composite, stFnt:fontFileName
		String[] elFont = font.split(",");

		List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
		attrs.add(KeyValue.with("stFnt:fontFamily", capitalize(elFont[0])));
		String fontFace = "";
		for (int i = 1; i < elFont.length; i++) {
			fontFace += capitalize(elFont[i]) + " ";
		}
		fontFace = fontFace.trim();
		if (fontFace.length() == 0) {
			attrs.add(KeyValue.with("stFnt:fontFace", "Regular"));
		} else {
			attrs.add(KeyValue.with("stFnt:fontFace", fontFace));
		}
		generateElement(ident, "rdf:li", null, attrs);
	}

	@SuppressWarnings("unchecked")
	private void generateEventOutcome(int ident, List<CheckMessage> messages, String sev) {
		for (CheckMessage c : messages) {
			startElement(ident++, "rdf:li", KeyValue.with("rdf:parseType", "Resource"));
			generateElement(ident, "premis:hasEventOutcome", c.getID() + ", " + sev + ", " + encodeContent(c.getMessage()));
			if (c.getLocations().size() != 0) {
				startElement(ident++, "premis:hasEventOutcomeDetail");
				startElement(ident++, "rdf:Seq");
				String previousValue = "";
    			for (EPUBLocation ml : c.getLocations()) {
    				String value = PathUtil.removeWorkingDirectory(ml.getPath());
    				if (ml.getLine() > 0 || ml.getColumn() > 0) {
    					value += " (" + ml.getLine() + "-" + ml.getColumn() + ")";
    				}
    				if (!previousValue.equals(value)) {
        				generateElement(ident, "rdf:li", null,
        						KeyValue.with("premis:hasEventOutcomeDetailNote", value));
        				previousValue = value;
    				}
    			}

    			endElement(--ident, "rdf:Seq");
    			endElement(--ident, "premis:hasEventOutcomeDetail");
			}
			endElement(--ident, "rdf:li");
		}
	}

	private void generateSignificantProperty(int ident, String property, String value) {
		// Significant property
		List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
		attrs.add(KeyValue.with("premis:hasSignificantPropertiesType", property));
		attrs.add(KeyValue.with("premis:hasSignificantPropertiesValue", value));
		
		generateElement(ident, "rdf:li", null, attrs);
	}
}

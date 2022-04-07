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

		generationDate = fromTime(System.currentTimeMillis());
		try {
			// Declared the wanted prefix
			addPrefixNamespace("x", "adobe:ns:meta/");
			addPrefixNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			addPrefixNamespace("dc", "http://purl.org/dc/elements/1.1/");
			addPrefixNamespace("xmp", "http://ns.adobe.com/xap/1.0/");
			addPrefixNamespace("xmpTPg", "http://ns.adobe.com/xap/1.0/t/pg/");
			addPrefixNamespace("stFnt", "http://ns.adobe.com/xap/1.0/sType/Font#");
			addPrefixNamespace("extended-properties", "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties/");
			addPrefixNamespace("premis", "http://www.loc.gov/premis/rdf/v1#");

			// Generate the information
			startElement("x:xmpmeta", KeyValue.with("xmlns:x", "adobe:ns:meta/"),
			        KeyValue.with("x:xmptk", "Adobe XMP Core 5.1.0-jc003"));
			startElement("rdf:RDF", KeyValue.with("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
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

			startElement("rdf:Description", attrs);

			// DC
			if (!creators.isEmpty()) {
				startElement("dc:creator");
				startElement("rdf:Seq");
				for (String creator : creators) {
					generateElement("rdf:li", creator);
				}
				endElement("rdf:Seq");
				endElement("dc:creator");
			}
			if (!titles.isEmpty()) {
				startElement("dc:title");
				startElement("rdf:Alt");
				boolean first = true;
				for (String title : titles) {
					if (first) {
						generateElement("rdf:li", title.trim(), KeyValue.with("xml:lang", "x-default"));
						first = false;
					} else {
						generateElement("rdf:li", title);
					}
				}
				endElement("rdf:Alt");
				endElement("dc:title");
			}
			if (!subjects.isEmpty()) {
				startElement("dc:subject");
				startElement("rdf:Bag");
				for (String subject : subjects) {
					generateElement("rdf:li", subject);
				}
				endElement("rdf:Bag");
				endElement("dc:subject");
			}

			// Fonts
			if (!embeddedFonts.isEmpty() || !refFonts.isEmpty()) {
				startElement("xmpTPg:Fonts");
				startElement("rdf:Bag");
				for (String font : embeddedFonts) {
					generateFont(font, true);
				}
				for (String font : refFonts) {
					generateFont(font, false);
				}
				endElement("rdf:Bag");
				endElement("xmpTPg:Fonts");
			}

			// Premis:event
			startElement("premis:hasEvent", KeyValue.with("rdf:parseType", "Resource"));
			generateElement("premis:hasEventDateTime", generationDate,
					KeyValue.with("rdf:datatype", "http://www.w3.org/2001/XMLSchema#dateTime"));
			generateElement("premis:hasEventType", null,
			        KeyValue.with("rdf:resource", "http://id.loc.gov/vocabulary/preservation/eventType/val"));
			if (fatalErrors.isEmpty() && errors.isEmpty()) {
				generateElement("premis:hasEventDetail", "Well-formed");
			} else {
				generateElement("premis:hasEventDetail", "Not well-formed");
			}
			if (fatalErrors.size() + errors.size() + warns.size() + hints.size() != 0) {
                startElement("premis:hasEventOutcomeInformation");
                startElement("rdf:Seq");
                
                generateEventOutcome(fatalErrors, "FATAL");
                generateEventOutcome(errors, "ERROR");
                generateEventOutcome(warns, "WARN");
                generateEventOutcome(hints, "HINT");
                
                endElement("rdf:Seq");
                endElement("premis:hasEventOutcomeInformation");
			}
			startElement("premis:hasEventRelatedAgent", KeyValue.with("rdf:parseType", "Resource"));
			generateElement("premis:hasAgentType", null,
			        KeyValue.with("rdf:resource", "http://id.loc.gov/vocabulary/preservation/agentType/sof"));
			if (epubCheckVersion == null) {
				generateElement("premis:hasAgentName", epubCheckName);
			} else {
				generateElement("premis:hasAgentName", epubCheckName + " " + epubCheckVersion);
			}
			endElement("premis:hasEventRelatedAgent");

			endElement("premis:hasEvent");

			// Significant properties
			startElement("premis:hasSignificantProperties");
			startElement("rdf:Bag");
			generateSignificantProperty("renditionLayout", hasFixedLayout ? "fixed-layout" : "reflowable");
			generateSignificantProperty("isScripted", Boolean.toString(hasScripts));
			generateSignificantProperty("hasEncryption", Boolean.toString(hasEncryption));
			generateSignificantProperty("hasAudio", Boolean.toString(hasAudio));
			generateSignificantProperty("hasVideo", Boolean.toString(hasVideo));
			generateSignificantProperty("hasSignatures", Boolean.toString(hasSignatures));
			generateSignificantProperty("hasAllFontsEmbedded", Boolean.toString(refFonts.isEmpty()));
			int nRefs = 0;
			for (String ref : references) {
				nRefs++;
				if (nRefs > 50) {
					generateSignificantProperty("reference", "" + (references.size() - 50) + " more references");
					break;
				}
				generateSignificantProperty("reference", ref);
			}
			endElement("rdf:Bag");
			endElement("premis:hasSignificantProperties");

			endElement("rdf:Description");
			endElement("rdf:RDF");
			endElement("x:xmpmeta");

			returnCode = 0;
		} catch (Exception e) {
			System.err.println("Exception encountered: " + e.getMessage());
			returnCode = 1;
		}
		return returnCode;
	}

	protected void generateFont(String font, boolean embeded) {
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
		generateElement("rdf:li", null, attrs);
	}

	@SuppressWarnings("unchecked")
	private void generateEventOutcome(List<CheckMessage> messages, String sev) {
		for (CheckMessage c : messages) {
			startElement("rdf:li", KeyValue.with("rdf:parseType", "Resource"));
			generateElement("premis:hasEventOutcome", c.getID() + ", " + sev + ", " + c.getMessage());
			if (c.getLocations().size() != 0) {
				startElement("premis:hasEventOutcomeDetail");
				startElement("rdf:Seq");
				String previousValue = "";
    			for (EPUBLocation ml : c.getLocations()) {
    				String value = ml.getPath();
    				if (ml.getLine() > 0 || ml.getColumn() > 0) {
    					value += " (" + ml.getLine() + "-" + ml.getColumn() + ")";
    				}
    				if (!previousValue.equals(value)) {
        				generateElement("rdf:li", null,
        						KeyValue.with("premis:hasEventOutcomeDetailNote", value));
        				previousValue = value;
    				}
    			}

    			endElement("rdf:Seq");
    			endElement("premis:hasEventOutcomeDetail");
			}
			endElement("rdf:li");
		}
	}

	private void generateSignificantProperty(String property, String value) {
		// Significant property
		List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
		attrs.add(KeyValue.with("premis:hasSignificantPropertiesType", property));
		attrs.add(KeyValue.with("premis:hasSignificantPropertiesValue", value));
		
		generateElement("rdf:li", null, attrs);
	}
}

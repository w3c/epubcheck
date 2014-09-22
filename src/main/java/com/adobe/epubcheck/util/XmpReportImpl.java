package com.adobe.epubcheck.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.reporting.CheckMessage;


public class XmpReportImpl extends XmlReportAbstract
{

  public XmpReportImpl(PrintWriter out, String ePubName, String versionEpubCheck)
  {
	  super(out, ePubName, versionEpubCheck);
  }


  @SuppressWarnings("unchecked")
  public int generateReport()
  {
	if (out == null) return 1;
	
    int returnCode = 1;
    int ident = 0;
    
    generationDate = fromTime(System.currentTimeMillis());
    try
    {
      output(ident, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	  startElement(ident++, "x:xmpmeta", 
			  KeyValue.with("xmlns:x", "adobe:ns:meta/"), 
			  KeyValue.with("x:xmptk", "Adobe XMP Core 5.1.0-jc003") 
	  );
	  startElement(ident++, "rdf:RDF", 
			  KeyValue.with("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
	  );
	  List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
	  attrs.add(KeyValue.with("rdf:about", ""));
	  attrs.add(KeyValue.with("xmlns:dc", "http://purl.org/dc/elements/1.1/"));
	  attrs.add(KeyValue.with("xmlns:xmp", "http://ns.adobe.com/xap/1.0/"));
	  attrs.add(KeyValue.with("xmlns:xmpTPg", "http://ns.adobe.com/xap/1.0/t/pg/"));
	  attrs.add(KeyValue.with("stFnt", "http:ns.adobe.com/xap/1.0/sType/Font#")); 
	  attrs.add(KeyValue.with("xmlns:cp", "http://schemas.openxmlformats.org/package/2006/metadata/core-properties/"));
	  attrs.add(KeyValue.with("xmlns:extended-properties", "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties/"));
	  attrs.add(KeyValue.with("xmlns:premis", "http://www.loc.gov/premis/rdf/v1"));
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
		  attrs.add(KeyValue.with("xmpTPg:NPage", Long.toString(pagesCount)));
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
			  generateElement(ident,  "rdf:li", creator);
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
				  output(ident, "<rdf:li xml:lang=\"x-default\">" + title + "</rdf:li>");
				  first =false;
			  } else {
				  generateElement(ident,  "rdf:li", title);
			  }
		  }
		  endElement(--ident, "rdf:Alt");
		  endElement(--ident, "dc:title");
	  }
	  if (!subjects.isEmpty()) {
		  startElement(ident++, "dc:subject");
		  startElement(ident++, "rdf:Bag");
		  for (String subject : subjects) {
			  generateElement(ident,  "rdf:li", subject);
		  }
		  endElement(--ident, "rdf:Bag");
		  endElement(--ident, "dc:subject");
	  }
	  
	  // Fonts
	  if (!embeddedFonts.isEmpty() || !refFonts.isEmpty()) {
		  startElement(ident++, "xmpTPg:Font");
		  startElement(ident++, "rdf:Bag"); 
		  for (String font : embeddedFonts) {
			  generateFont(ident, font);
		  }
		  for (String font : refFonts) {
			  generateFont(ident, font);
		  }
		  endElement(--ident, "rdf:Bag");
		  
		  endElement(ident++, "xmpTPg:Font");
	  }	  
	  
	  // Premis:event
	  startElement(ident++, "premis:hasEvent");
	  generateElement(ident, "premis:hasEventDateTime", generationDate);
      output(ident, "<premis:hasEventType rdf:resource=\"http://id.loc.gov/vocabulary/preservation/eventType/val\" />");
      if (fatalErrors.isEmpty() && errors.isEmpty()) {
        generateElement(ident, "premis:hasEventDetail", "Well-formed");
      } else {
        generateElement(ident, "premis:hasEventDetail", "Not well-formed");
      }
      generateEventOutcome(ident, fatalErrors, "FATAL");
      generateEventOutcome(ident, errors, "ERROR");
      generateEventOutcome(ident, warns, "WARN");
      generateEventOutcome(ident, hints, "HINT");

	  startElement(ident++, "premis:hasEventRelatedAgent");
      output(ident, "<premis:hasAgentType rdf:resource=\"http://id.loc.gov/vocabulary/preservation/agentType/sof\" />");
      if (epubCheckVersion == null) {
    	  generateElement(ident, "premis:hasAgentName", epubCheckName);
      } else {
    	  generateElement(ident, "premis:hasAgentName", epubCheckName + " " + epubCheckVersion);
      }
	  endElement(--ident, "premis:hasEventRelatedAgent");

	  endElement(--ident, "premis:hasEvent");

	  // Significant properties
	  generateSignificantProperty(ident, "renditionLayout", hasFixedLayout?"fixed-layout":"reflowable");
	  generateSignificantProperty(ident, "isScripted", Boolean.toString(hasScripts));
	  generateSignificantProperty(ident, "hasEncryption", Boolean.toString(hasEncryption));
	  generateSignificantProperty(ident, "hasAudio", Boolean.toString(hasAudio));
	  generateSignificantProperty(ident, "hasVideo", Boolean.toString(hasVideo));
	  generateSignificantProperty(ident, "hasSignatures", Boolean.toString(hasSignatures));
	  int nRefs = 0;
	  for (String ref : references) {
		  nRefs++;
		  if (nRefs > 50) {
			  generateSignificantProperty(ident, "reference", "" + (references.size() - 50) + " more references");
			  break;
		  }
		  generateSignificantProperty(ident, "reference", ref);
	  }
	  
	  endElement(--ident, "rdf:Description");
	  endElement(--ident, "rdf:RDF");
	  endElement(--ident, "x:xmpmeta");

	  returnCode = 0;
    }
    catch (Exception e)
    {
      System.err.println("Exception encountered: " + e.getMessage());
      returnCode = 1;
    }
    return returnCode;
  }

  protected void generateFont(int ident, String font) {
	String[] elFont = font.split(",");
	  startElement(ident++,  "rdf:li");
	  // stFnt:fontName, stFnt:fontType, stFnt:versionString, stFnt:composite, stFnt:fontFileName
	  generateElement(ident, "stFnt:fontFamily", capitalize(elFont[0]));
	  String fontFace = "";
	  for (int i = 1; i < elFont.length; i++) {
		  fontFace += capitalize(elFont[i]) + " ";
	  }
	  fontFace = fontFace.trim();
	  if (fontFace.length() == 0) {
		  generateElement(ident, "stFnt:fontFace", "Regular");
	  } else {
		  generateElement(ident, "stFnt:fontFace", fontFace);
	  }
	  endElement(--ident,  "rdf:li");
  }

  private void generateEventOutcome(int ident, List<CheckMessage> messages, String sev) {
	  	for (CheckMessage c : messages) {
			startElement(ident++, "premis:hasEventOutcomeInformation");
			generateElement(ident, "premis:hasEventOutcome", c.getID() + ", " + sev + ", " + encodeContent(c.getMessage()));
			for (MessageLocation ml : c.getLocations()) {
				String loc = "";
				if (ml.getLine() > 0 || ml.getColumn() > 0) {
					loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
				}
				startElement(ident++, "premis:hasEventOutcomeDetail");
				generateElement(ident, "premis:hasEventOutcomeDetailNote", PathUtil.removeWorkingDirectory(ml.getFileName()) + loc);
				endElement(--ident, "premis:hasEventOutcomeDetail");
		    }
			endElement(--ident, "premis:hasEventOutcomeInformation");
		}
  }
  
  private void generateSignificantProperty(int ident, String property, String value) {
	  // Significant properties
	  startElement(ident++, "premis:hasSignificantProperties");
	  generateElement(ident, "premis:hasSignificantPropertiesType", property);
	  generateElement(ident, "premis:hasSignificantPropertiesValue", value);
	  endElement(--ident, "premis:hasSignificantProperties");

  }
}

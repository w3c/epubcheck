package com.adobe.epubcheck.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.reporting.CheckMessage;


public class XmlReportImpl extends XmlReportAbstract
{

  public XmlReportImpl(PrintWriter out, String ePubName, String versionEpubCheck)
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
	  List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
	  attrs.add(KeyValue.with("xmlns", "http://hul.harvard.edu/ois/xml/ns/jhove"));
	  attrs.add(KeyValue.with("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
	  // attrs.add(KeyValue.with("xsi:schemaLocation", "http://hul.harvard.edu/ois/xml/ns/jhove jhove.xsd"));
	  attrs.add(KeyValue.with("name", epubCheckName));
	  attrs.add(KeyValue.with("release", epubCheckVersion)); 
	  attrs.add(KeyValue.with("date", epubCheckDate));
	  startElement(ident++, "jhove", attrs);

	  generateElement(ident, "date", generationDate);
	  startElement(ident++, "repInfo", KeyValue.with("uri", getNameFromPath(getEpubFileName())));
      generateElement(ident, "created", creationDate);
      generateElement(ident, "lastModified", lastModifiedDate);
      if (formatName == null) {
        generateElement(ident, "format", "application/octet-stream");
      } else {
        generateElement(ident, "format", formatName); //application/epub+zip
      }
      generateElement(ident, "version", formatVersion);
      String customMessageFileName = this.getCustomMessageFile();
      if (customMessageFileName != null && !customMessageFileName.isEmpty())
      {
        generateElement(ident, "customMessageFileName", customMessageFileName);
      }
      if (fatalErrors.isEmpty() && errors.isEmpty())
      {
        generateElement(ident, "status", "Well-formed");
      }
      else
      {
        generateElement(ident, "status", "Not well-formed");
      }
      if (!warns.isEmpty() || !fatalErrors.isEmpty() || !errors.isEmpty() || !hints.isEmpty())
      {
        startElement(ident++, "messages");
        for (CheckMessage c : fatalErrors) {
        	String m = c.getID() + ", FATAL, [" + encodeContent(c.getMessage()) + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement(ident, "message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc);
        	}
        }
        for (CheckMessage c : errors) {
        	String m = c.getID() + ", ERROR, [" + encodeContent(c.getMessage()) + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement(ident, "message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc);
        	}
        }
        for (CheckMessage c : warns) {
        	String m = c.getID() + ", WARN, [" + encodeContent(c.getMessage()) + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement(ident, "message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc);
        	}
        }
        for (CheckMessage c : hints) {
        	String m = c.getID() + ", HINT, [" + encodeContent(c.getMessage()) + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement(ident, "message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc);
        	}
        }
        endElement(--ident, "messages");
      }
      generateElement(ident, "mimeType", formatName);
      startElement(ident++, "properties");

      generateProperty(ident, "PageCount", pagesCount);
      generateProperty(ident, "CharacterCount", charsCount);
      generateProperty(ident, "Language", language, "String");

  	  startElement(ident++, "property");
      generateElement(ident, "name", "Info");
      startElement(ident++, "values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));

      generateProperty(ident, "Identifier", identifier, "String");
      generateProperty(ident, "CreationDate", creationDate, "Date");
      generateProperty(ident, "ModDate", lastModifiedDate, "Date");

      if (!titles.isEmpty())
      {
          String[] cs = titles.toArray(new String[titles.size()]);
          generateProperty(ident, "Title", cs, "String");
      }
      if (!creators.isEmpty())
      {
        String[] cs = creators.toArray(new String[creators.size()]);
        generateProperty(ident, "Creator", cs, "String");
      }
      if (!contributors.isEmpty())
      {
        String[] cs = contributors.toArray(new String[contributors.size()]);
        generateProperty(ident, "Contributor", cs, "String");
      }
      generateProperty(ident, "Date", date, "String");
      generateProperty(ident, "Publisher", publisher, "String");
      if (!subjects.isEmpty())
      {
        String[] cs = subjects.toArray(new String[subjects.size()]);
        generateProperty(ident, "Subject", cs, "String");
      }
      if (!rights.isEmpty())
      {
        String[] cs = rights.toArray(new String[rights.size()]);
        generateProperty(ident, "Rights", cs, "String");
      }
      endElement(--ident, "values");
      endElement(--ident, "property");

      if (!embeddedFonts.isEmpty() || !refFonts.isEmpty())
      {
 	    startElement(ident++, "property");
        generateElement(ident, "name", "Fonts");
        startElement(ident++, "values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));

        for (String f : embeddedFonts)
        {
      	  startElement(ident++, "property");
          generateElement(ident, "name", "Font");
          startElement(ident++, "values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
          generateProperty(ident, "FontName", encodeContent(getNameFromPath(f)), "String");
          generateProperty(ident, "FontFile", false);
          endElement(--ident, "values");
          endElement(--ident, "property");
        }
        for (String f : refFonts)
        {
          startElement(ident++, "property");
          generateElement(ident, "name", "Font");
          startElement(ident++, "values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
          generateProperty(ident, "FontName", encodeContent(getNameFromPath(f)), "String");
          generateProperty(ident, "FontFile", false);
          endElement(--ident, "values");
          endElement(--ident, "property");
        }
        
        endElement(--ident, "values");
        endElement(--ident, "property");
      }

      if (!references.isEmpty())
      {
    	startElement(ident++, "property");
    	generateElement(ident++, "name", "References");
    	startElement(ident++, "values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
        for (String r : references)
        {
          generateProperty(ident, "Reference", encodeContent(r), "String");
        }
        endElement(--ident, "values");
        endElement(--ident, "property");
      }

      if (hasEncryption)
      {
        generateProperty(ident, "hasEncryption", hasEncryption);
      }
      if (hasSignatures)
      {
        generateProperty(ident, "hasSignatures", hasSignatures);
      }
      if (hasAudio)
      {
        generateProperty(ident, "hasAudio", hasAudio);
      }
      if (hasVideo)
      {
        generateProperty(ident, "hasVideo", hasVideo);
      }
      if (hasFixedLayout)
      {
        generateProperty(ident, "hasFixedLayout", hasFixedLayout);
      }
      if (hasScripts)
      {
        generateProperty(ident, "hasScripts", hasScripts);
      }

      endElement(--ident, "properties");
      endElement(--ident, "repInfo");
      endElement(--ident, "jhove");
      returnCode = 0;
    }
    catch (Exception e)
    {
      System.err.println("Exception encountered: " + e.getMessage());
      returnCode = 1;
    }
    return returnCode;
  }

  @SuppressWarnings("unchecked")
  private void generateProperty(int ident, String name, String[] value, String type)
  {
    if (value == null || value.length == 0)
    {
      return;
    }
	startElement(ident++, "property");
    generateElement(ident, "name", name);
    startElement(ident++, "values", KeyValue.with("arity", value.length == 1 ? "Scalar" : "Array"), KeyValue.with("type", type));
    for (String v : value)
    {
      generateElement(ident, "value", v);
    }
    endElement(--ident, "values");
    endElement(--ident, "property");
  }

  @SuppressWarnings("unchecked")
  private void generateProperty(int ident, String name, String value, String type)
  {
    if (value == null || value.trim().length() == 0)
    {
      return;
    }
	startElement(ident++, "property");
    generateElement(ident, "name", name);
    startElement(ident++, "values", KeyValue.with("arity", "Scalar"), KeyValue.with("type", type));
    generateElement(ident, "value", value);
    endElement(--ident, "values");
    endElement(--ident, "property");
  }

  private void generateProperty(int ident, String name, long value)
  {
    if (value == 0)
    {
      return;
    }
    generateProperty(ident, name, Long.toString(value), "Long");
  }

  private void generateProperty(int ident, String name, boolean value)
  {
    generateProperty(ident, name, value ? "true" : "false", "Boolean");
  }

}

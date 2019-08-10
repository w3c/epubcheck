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
    
    generationDate = fromTime(System.currentTimeMillis());
    try
    {
      setNamespace("http://schema.openpreservation.org/ois/xml/ns/jhove");
      addPrefixNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
	  List<KeyValue<String, String>> attrs = new ArrayList<KeyValue<String, String>>();
	  attrs.add(KeyValue.with("name", epubCheckName));
	  attrs.add(KeyValue.with("release", epubCheckVersion)); 
	  attrs.add(KeyValue.with("date", epubCheckDate));
	  attrs.add(KeyValue.with("xsi:schemaLocation", "http://schema.openpreservation.org/ois/xml/ns/jhove https://schema.openpreservation.org/ois/xml/xsd/jhove/jhove.xsd"));
	  startElement("jhove", attrs);

	  generateElement("date", generationDate);
	  startElement("repInfo", KeyValue.with("uri", getEpubFileName()));
      generateElement("created", creationDate);
      generateElement("lastModified", lastModifiedDate);
      if (formatName == null) {
        generateElement("format", "application/octet-stream");
      } else {
        generateElement("format", formatName); //application/epub+zip
      }
      generateElement("version", formatVersion);
      String customMessageFileName = this.getCustomMessageFile();
      if (customMessageFileName != null && !customMessageFileName.isEmpty())
      {
        generateElement("customMessageFileName", customMessageFileName);
      }
      if (fatalErrors.isEmpty() && errors.isEmpty())
      {
        generateElement("status", "Well-formed");
      }
      else
      {
        generateElement("status", "Not well-formed");
      }
      if (!warns.isEmpty() || !fatalErrors.isEmpty() || !errors.isEmpty() || !hints.isEmpty())
      {
        startElement("messages");
        for (CheckMessage c : fatalErrors) {
        	String m = c.getID() + ", FATAL, [" + c.getMessage() + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement("message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc,
            		  KeyValue.with("id", c.getID()), KeyValue.with("severity", "error"));
        	}
        }
        for (CheckMessage c : errors) {
        	String m = c.getID() + ", ERROR, [" + c.getMessage() + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement("message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc,
            		  KeyValue.with("id", c.getID()), KeyValue.with("severity", "error"));
        	}
        }
        for (CheckMessage c : warns) {
        	String m = c.getID() + ", WARN, [" + c.getMessage() + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement("message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc,
            		  KeyValue.with("id", c.getID()), KeyValue.with("severity", "warning"));
        	}
        }
        for (CheckMessage c : hints) {
        	String m = c.getID() + ", HINT, [" + c.getMessage() + "], ";
        	for (EPUBLocation ml : c.getLocations()) {
			  String loc = "";
			  if (ml.getLine() > 0 || ml.getColumn() > 0) {
				loc = " (" + ml.getLine() + "-" + ml.getColumn() + ")";
			  }
              generateElement("message", m + PathUtil.removeWorkingDirectory(ml.getPath()) + loc,
            		  KeyValue.with("id", c.getID()), KeyValue.with("severity", "info"));
        	}
        }
        endElement("messages");
      }
      generateElement("mimeType", formatName);
      startElement("properties");

      generateProperty("FileName", getNameFromPath(getEpubFileName()), "String");
      generateProperty("PageCount", pagesCount);
      generateProperty("CharacterCount", charsCount);
      generateProperty("Language", language, "String");

  	  startElement("property");
      generateElement("name", "Info");
      startElement("values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));

      generateProperty("Identifier", identifier, "String");
      generateProperty("CreationDate", creationDate, "Date");
      generateProperty("ModDate", lastModifiedDate, "Date");

      if (!titles.isEmpty())
      {
          String[] cs = titles.toArray(new String[titles.size()]);
          generateProperty("Title", cs, "String");
      }
      if (!creators.isEmpty())
      {
        String[] cs = creators.toArray(new String[creators.size()]);
        generateProperty("Creator", cs, "String");
      }
      if (!contributors.isEmpty())
      {
        String[] cs = contributors.toArray(new String[contributors.size()]);
        generateProperty("Contributor", cs, "String");
      }
      generateProperty("Date", date, "String");
      generateProperty("Publisher", publisher, "String");
      if (!subjects.isEmpty())
      {
        String[] cs = subjects.toArray(new String[subjects.size()]);
        generateProperty("Subject", cs, "String");
      }
      if (!rights.isEmpty())
      {
        String[] cs = rights.toArray(new String[rights.size()]);
        generateProperty("Rights", cs, "String");
      }
      endElement("values");
      endElement("property");

      if (!embeddedFonts.isEmpty() || !refFonts.isEmpty())
      {
 	    startElement("property");
        generateElement("name", "Fonts");
        startElement("values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));

        for (String f : embeddedFonts)
        {
      	  startElement("property");
          generateElement("name", "Font");
          startElement("values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
          generateProperty("FontName", getNameFromPath(f), "String");
          generateProperty("FontFile", true);
          endElement("values");
          endElement("property");
        }
        for (String f : refFonts)
        {
          startElement("property");
          generateElement("name", "Font");
          startElement("values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
          generateProperty("FontName", getNameFromPath(f), "String");
          generateProperty("FontFile", false);
          endElement("values");
          endElement("property");
        }
        
        endElement("values");
        endElement("property");
      }

      if (!references.isEmpty())
      {
    	startElement("property");
    	generateElement("name", "References");
    	startElement("values", KeyValue.with("arity", "List"), KeyValue.with("type", "Property"));
        for (String r : references)
        {
          generateProperty("Reference", r, "String");
        }
        endElement("values");
        endElement("property");
      }
      if (!mediaTypes.isEmpty())
      {
          String[] cs = mediaTypes.toArray(new String[mediaTypes.size()]);
          generateProperty("MediaTypes", cs, "String");
      }

      if (hasEncryption)
      {
        generateProperty("hasEncryption", hasEncryption);
      }
      if (hasSignatures)
      {
        generateProperty("hasSignatures", hasSignatures);
      }
      if (hasAudio)
      {
        generateProperty("hasAudio", hasAudio);
      }
      if (hasVideo)
      {
        generateProperty("hasVideo", hasVideo);
      }
      if (hasFixedLayout)
      {
        generateProperty("hasFixedLayout", hasFixedLayout);
      }
      if (hasScripts)
      {
        generateProperty("hasScripts", hasScripts);
      }

      endElement("properties");
      endElement("repInfo");
      endElement("jhove");
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
  private void generateProperty(String name, String[] value, String type)
  {
    if (value == null || value.length == 0)
    {
      return;
    }
	startElement("property");
    generateElement("name", name);
    startElement("values", KeyValue.with("arity", value.length == 1 ? "Scalar" : "Array"), KeyValue.with("type", type));
    for (String v : value)
    {
      generateElement("value", v);
    }
    endElement("values");
    endElement("property");
  }

  @SuppressWarnings("unchecked")
  private void generateProperty(String name, String value, String type)
  {
    if (value == null || value.trim().length() == 0)
    {
      return;
    }
	startElement("property");
    generateElement("name", name);
    startElement("values", KeyValue.with("arity", "Scalar"), KeyValue.with("type", type));
    generateElement("value", value);
    endElement("values");
    endElement("property");
  }

  private void generateProperty(String name, long value)
  {
    if (value == 0)
    {
      return;
    }
    generateProperty(name, Long.toString(value), "Long");
  }

  private void generateProperty(String name, boolean value)
  {
    generateProperty(name, value ? "true" : "false", "Boolean");
  }

}

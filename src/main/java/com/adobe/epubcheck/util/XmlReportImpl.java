package com.adobe.epubcheck.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.EPUBVersion;


public class XmlReportImpl implements Report {
    private File outputFile;
    private PrintWriter out;
    
    private boolean withDocumentMD = false;
    
    private String epubCheckName = "epubcheck";
    private String epubCheckVersion;
    private String epubCheckDate = "2012-10-31";

    private String ePubName;
    private String creationDate;
    private String lastModifiedDate;
    private String identifier;
    private String title;
    private Set<String> creators = new LinkedHashSet<String>(); 
    private Set<String> contributors = new LinkedHashSet<String>(); 
    private String publisher;
    private Set<String> rights = new LinkedHashSet<String>(); 
    private String date;

    private String formatName;
    private String formatVersion;
    private long pagesCount;
    private long charsCount;
    private String language;
    private Set<String> embeddedFonts = new LinkedHashSet<String>(); 
    private Set<String> refFonts = new LinkedHashSet<String>(); 
    private Set<String> references = new LinkedHashSet<String>(); 
    private boolean hasEncryption;
    private boolean hasSignatures;
    private boolean hasAudio;
    private boolean hasVideo;
    private boolean hasFixedLayout;
    private boolean hasScripts;
    
    private List<String> warns = new ArrayList<String>(); 
    private List<String> errors = new ArrayList<String>(); 
    private List<String> exceptions = new ArrayList<String>(); 
    
    
    public XmlReportImpl( File out, String ePubName, String versionEpubCheck ) {
        this.outputFile = out;
        this.ePubName = ePubName;
        this.epubCheckVersion = versionEpubCheck;
    }

    public void setEpubVersion(EPUBVersion version) {
        this.formatVersion = version.toString();
    }
    
    @Override
    public void error(String resource, int line, int column, String message) {
        errors.add((resource == null ? "" : "/" + resource) +
                (line <= 0 ? "" : "(" + line + ")") + ": " + message );

    }

    @Override
    public void exception(String resource, Exception e) {
        exceptions.add((resource == null ? "" : "/" + resource) +
                e.getMessage());

    }

    @Override
    public void warning(String resource, int line, int column, String message) {
        warns.add((resource == null ? "" : "/" + resource) +
                (line <= 0 ? "" : "(" + line + ")") + ": " + message );

    }

    @Override
    public int getErrorCount() {
        return errors.size();
    }

    @Override
    public int getExceptionCount() {
        return exceptions.size();
    }

    @Override
    public int getWarningCount() {
        return warns.size();
    }

    @Override
    public void info(String resource, FeatureEnum feature, String value) {
        switch (feature) {
            case TOOL_NAME: this.epubCheckName = value; break;
            case TOOL_VERSION: this.epubCheckVersion = value; break;
            case FORMAT_NAME: this.formatName = value; break;
            case FORMAT_VERSION: this.formatVersion = value; break;
            case CREATION_DATE:
                this.creationDate = value; break;
            case MODIFIED_DATE:
                this.lastModifiedDate = value; break;
            case PAGES_COUNT: this.pagesCount = Long.parseLong(value); break;
            case CHARS_COUNT: this.charsCount += Long.parseLong(value); break;
            case DECLARED_MIMETYPE:
                if (value != null && value.startsWith("audio/")) {
                    this.hasAudio = true;
                } else if (value != null && value.startsWith("video/")) {
                    this.hasVideo = true;
                }
                break;
            case FONT_EMBEDDED:
                this.embeddedFonts.add(value);
                break;
            case FONT_REFERENCE:
                this.refFonts.add(value);
                break;
            case REFERENCE:
                this.references.add(value);
                break;
            case DC_LANGUAGE: this.language = value; break;
            case DC_TITLE: this.title = value; break;
            case DC_CREATOR: this.creators.add(value); break;
            case DC_CONTRIBUTOR: this.contributors.add(value); break;
            case DC_PUBLISHER: this.publisher = value; break;
            case DC_RIGHTS: this.rights.add(value); break;
            case DC_DATE: this.date = value; break;
            case UNIQUE_IDENT: this.identifier = value; break;

            case HAS_SIGNATURES: this.hasSignatures = true; break;
            case HAS_ENCRYPTION: this.hasEncryption = true; break;
            case HAS_FIXED_LAYOUT: this.hasFixedLayout = true; break;
            case HAS_SCRIPTS: this.hasScripts = true; break;

        }
    }

    private String getNameFromPath(String path) {
        if (path == null || path.length() == 0) return null;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return path;
        } else {
            return path.substring(lastSlash + 1);
        }
    }
    
    public void generate() {
        // Quick and dirty XML generation...
        out = null;
        int ident = 0;
        
        try {
            out = new PrintWriter(outputFile, "UTF-8");
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            output(ident++, 
              "<jhove xmlns=\"http://hul.harvard.edu/ois/xml/ns/jhove\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
              // " xsi:schemaLocation=\"http://hul.harvard.edu/ois/xml/ns/jhove jhove.xsd\"" + 
              " name=\"" + epubCheckName + "\" release=\"" + epubCheckVersion + 
              "\" date=\"" + epubCheckDate +"\">");
            generateElement(ident, "date", fromTime(System.currentTimeMillis()));
            output(ident++, "<repInfo uri=\"" + getNameFromPath(ePubName) + "\">");
            generateElement(ident, "created", creationDate);
            generateElement(ident, "lastModified", lastModifiedDate);
            if (formatName == null) {
              generateElement(ident, "format", "application/octet-stream");
            } else {
              generateElement(ident, "format", formatName); //application/epub+zip
            }
            generateElement(ident, "version", formatVersion);
            if (exceptions.isEmpty() && errors.isEmpty()) {
              generateElement(ident, "status", "Well-formed");
            } else {
              generateElement(ident, "status", "Not well-formed");
            }
            if (!warns.isEmpty() || !exceptions.isEmpty() || !errors.isEmpty()) {
              output(ident++, "<messages>");
              for (String w : warns) {
                generateElement(ident, "message", "WARN: " + encodeContent(w));
              }
              for (String e : errors) {
                generateElement(ident, "message", "ERROR: " + encodeContent(e));
              }
              for (String e : exceptions) {
                generateElement(ident, "message", "EXCEPTION: " + encodeContent(e));
              }
              output(--ident, "</messages>");
            }
            generateElement(ident, "mimeType", formatName);
            output(ident++, "<properties>");
            
            generateProperty(ident, "PageCount", pagesCount);
            generateProperty(ident, "CharacterCount", charsCount);
            generateProperty(ident, "Language", language, "String");

            output(ident++, "<property><name>Info</name><values arity=\"List\" type=\"Property\">");
            generateProperty(ident, "Identifier", identifier, "String");
            generateProperty(ident, "CreationDate", creationDate, "Date");
            generateProperty(ident, "ModDate", lastModifiedDate, "Date");
            generateProperty(ident, "Title", title, "String");
            if (!creators.isEmpty()) {
              String[] cs = creators.toArray(new String[0]);
              generateProperty(ident, "Creator", cs, "String");
            }
            if (!contributors.isEmpty()) {
              String[] cs = contributors.toArray(new String[0]);
              generateProperty(ident, "Contributor", cs, "String");
            }
            generateProperty(ident, "Date", date, "String");
            generateProperty(ident, "Publisher", publisher, "String");
            if (!rights.isEmpty()) {
              String[] cs = rights.toArray(new String[0]);
              generateProperty(ident, "Rights", cs, "String");
            }
            output(--ident, "</values></property>");

            if (!embeddedFonts.isEmpty() || !refFonts.isEmpty()) {
              output(ident++, "<property><name>Fonts</name><values arity=\"List\" type=\"Property\">");
              
              for (String f : embeddedFonts) {
                  output(ident++, "<property><name>Font</name><values arity=\"List\" type=\"Property\">");
                  generateProperty(ident, "FontName", encodeContent(getNameFromPath(f)), "String");
                  generateProperty(ident, "FontFile", true);
                  output(--ident, "</values></property>");
              }
              for (String f : refFonts) {
                  output(ident++, "<property><name>Font</name><values arity=\"List\" type=\"Property\">");
                  generateProperty(ident, "FontName", encodeContent(getNameFromPath(f)), "String");
                  generateProperty(ident, "FontFile", false);
                  output(--ident, "</values></property>");
              }
              output(--ident, "</values></property>");
            }
            
            if (!references.isEmpty()) {
              output(ident++, "<property><name>References</name><values arity=\"List\" type=\"Property\">");
              for (String r : references) {
                  generateProperty(ident, "Reference", encodeContent(r), "String");
              }
              output(--ident, "</values></property>");
            }
            
            if (hasEncryption) generateProperty(ident, "hasEncryption", hasEncryption);
            if (hasSignatures) generateProperty(ident, "hasSignatures", hasSignatures);
            if (hasAudio) generateProperty(ident, "hasAudio", hasAudio);
            if (hasVideo) generateProperty(ident, "hasVideo", hasVideo);
            if (hasFixedLayout) generateProperty(ident, "hasFixedLayout", hasFixedLayout);
            if (hasScripts) generateProperty(ident, "hasScripts", hasScripts);

            if (withDocumentMD) {
              output(ident++, "<property><name>DocumentMDMetadata</name><values arity=\"Scalar\" type=\"Object\"><value>");
              generateDocumentMD(ident);
              output(--ident, "</value></values></property>");
            }
            output(--ident, "</properties>");
            output(--ident, "</repInfo>");
            output(--ident, "</jhove>");

        } catch (FileNotFoundException e) {
            System.err.println("FileNotFound error: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("FileNotFound error: " + e.getMessage());
        } finally {
            out.close();
        }
    }

    private void generateDocumentMD(int ident) {
      output(ident++, "<docmd:document xmlns:docmd=\"http://www.fcla.edu/docmd\">");

      generateElement(ident, "docmd:PageCount", pagesCount);
      generateElement(ident, "docmd:CharacterCount", charsCount);
      generateElement(ident, "docmd:Language", language);
      for (String f : embeddedFonts) {
          output(ident, "<docmd:Font FontName=\"" + encodeContent(getNameFromPath(f)) + "\" isEmbedded=\"true\" />");
      }
      for (String f : refFonts) {
          output(ident, "<docmd:Font FontName=\"" + encodeContent(getNameFromPath(f)) + "\" isEmbedded=\"false\" />");
      }
      for (String r : references) {
          generateElement(ident, "docmd:Reference", encodeContent(r));
      }
      //if (hasEncryption) generateElement(ident, "docmd:Features", "hasEncryption");
      //if (hasSignatures) generateElement(ident, "docmd:Features", "hasSignatures");
      if (hasAudio) generateElement(ident, "docmd:Features", "hasAudio");
      if (hasVideo) generateElement(ident, "docmd:Features", "hasVideo");
      if (hasFixedLayout) generateElement(ident, "docmd:Features", "hasFixedLayout");
      if (hasScripts) generateElement(ident, "docmd:Features", "hasScripts");

      output(--ident, "</docmd:document>");
    }
    
    private void output(int ident, String value) {
        char[] spaces = new char[ident];
        Arrays.fill(spaces, ' ');
        out.print(spaces);
        out.println(value);
    }
    
    private void generateElement(int ident, String name, String value) {
        if (value == null || value.trim().length() == 0) return;
        StringBuilder sb = new StringBuilder();
        sb.append('<').append(name).append('>');
        sb.append(encodeContent(value.trim()));
        sb.append("</").append(name).append('>');
        output(ident, sb.toString());
    }
    private void generateElement(int ident, String name, long value) {
        if (value == 0) return;
        generateElement(ident, name, Long.toString(value));
    }

    private void generateProperty(int ident, String name, String[] value, String type) {
      if (value == null || value.length == 0) return;
      StringBuilder sb = new StringBuilder();
      sb.append("<property><name>").append(name).append("</name>");
      sb.append("<values arity=\"").append(value.length==1?"Scalar":"Array").append("\" type=\"").append(type).append("\">");
      for (String v:value) {
        sb.append("<value>").append(encodeContent(v)).append("</value>");
      }
      sb.append("</values></property>");
      output(ident, sb.toString());
    }
    private void generateProperty(int ident, String name, String value, String type) {
      if (value == null || value.trim().length() == 0) return;
      StringBuilder sb = new StringBuilder();
      sb.append("<property><name>").append(name).append("</name><values arity=\"Scalar\" type=\"").append(type).append("\">");
      sb.append("<value>").append(encodeContent(value)).append("</value>");
      sb.append("</values></property>");
      output(ident, sb.toString());
    }
    private void generateProperty(int ident, String name, long value) {
        if (value == 0) return;
        generateProperty(ident, name, Long.toString(value), "Long");
    }
    private void generateProperty(int ident, String name, boolean value) {
        generateProperty(ident, name, value?"true":"false", "Boolean");
    }
   
    /**
     *   Encodes a content String in XML-clean form, converting characters
     *   to entities as necessary.  The null string will be
     *   converted to an empty string.
     */
    private static String encodeContent(String content) {
        if (content == null) {
            content = "";
        }
        StringBuilder buffer = new StringBuilder(content);

        int n = 0;
        while ((n = buffer.indexOf("&", n)) > -1) {
            buffer.insert(n + 1, "amp;");
            n += 5;
        }
        n = 0;
        while ((n = buffer.indexOf("<", n)) > -1) {
            buffer.replace(n, n + 1, "&lt;");
            n += 4;
        }
        n = 0;
        while ((n = buffer.indexOf(">", n)) > -1) {
            buffer.replace(n, n + 1, "&gt;");
            n += 4;
        }

        return buffer.toString();
    }
    
    /** Transform time into ISO 8601 string. */
    public static String fromTime(final long time) {
        Date date = new Date(time);
        // Waiting for Java 7: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }
    
}

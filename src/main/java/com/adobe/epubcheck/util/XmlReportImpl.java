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
    
    private String epubCheckName = "epubcheck";
    private String epubCheckVersion;

    private String ePubName;
    private String creationDate;
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
    private Set<String> embededFonts = new LinkedHashSet<String>(); 
    private Set<String> refFonts = new LinkedHashSet<String>(); 
    private Set<String> references = new LinkedHashSet<String>(); 
    private boolean hasEncryption;
    private boolean hasSignature;
    private boolean hasAudio;
    private boolean hasVideo;
    private boolean hasFixedLayout;
    private boolean hasScript;
    
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
                this.creationDate = fromTime(Long.parseLong(value)); break;
            case PAGES_COUNT: this.pagesCount = Long.parseLong(value); break;
            case CHARS_COUNT: this.charsCount += Long.parseLong(value); break;
            case DECLARED_MIMETYPE:
                if (value != null && value.startsWith("audio/")) {
                    this.hasAudio = true;
                } else if (value != null && value.startsWith("video/")) {
                    this.hasVideo = true;
                }
                break;
            case FONT_EMBEDED:
                this.embededFonts.add(value);
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

            case HAS_SIGNATURE: this.hasSignature = true; break;
            case HAS_ENCRYPTION: this.hasEncryption = true; break;
            case HAS_FIXED_LAYOUT: this.hasFixedLayout = true; break;
            case HAS_SCRIPT: this.hasScript = true; break;

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
            output(ident++, "<doc>");
            output(ident, "<!-- An extension of documentMD (http://www.fcla.edu/dls/md/docmd.xsd) -->");
            if (creationDate == null) {
                output(ident++, "<document>");
            } else {
                output(ident++, "<document creationDateTime=\"" + creationDate + "\">");
            }

            output(ident++, "<documentInformation>");
            generateElement(ident, "fileName", getNameFromPath(ePubName));
            generateElement(ident, "identifier", identifier);
            generateElement(ident, "title", title);
            for (String c : creators) {
                generateElement(ident, "creator", c);
            }
            for (String c : contributors) {
                generateElement(ident, "contributor", c);
            }
            generateElement(ident, "publisher", publisher);
            for (String r : rights) {
                generateElement(ident, "rights", r);
            }
            generateElement(ident, "date", date);
            output(--ident, "</documentInformation>");

            output(ident++, "<formatDesignation>");
            if (formatName != null) {
                generateElement(ident, "formatName", formatName); //application/epub+zip
            } else {
                generateElement(ident, "formatName", "application/octet-stream");
            }
            generateElement(ident, "formatVersion", formatVersion);
            output(--ident, "</formatDesignation>");
            
            output(ident++, "<assessmentInformation agentName=\"" + epubCheckName + "\" agentVersion=\"" + epubCheckVersion + "\">");
            if (exceptions.isEmpty() && errors.isEmpty()) {
                generateElement(ident, "outcome", "Valid");
            } else {
                generateElement(ident, "outcome", "Not valid");
            }
            for (String w : warns) {
                output(ident, "<outcomeDetailNote>WARN: " + encodeContent(w) + "</outcomeDetailNote>");
            }
            for (String e : errors) {
                output(ident, "<outcomeDetailNote>ERROR: " + encodeContent(e) + "</outcomeDetailNote>");
            }
            for (String e : exceptions) {
                output(ident, "<outcomeDetailNote>EXCEPTION: " + encodeContent(e) + "</outcomeDetailNote>");
            }
            output(--ident, "</assessmentInformation>");
            
            generateElement(ident, "PageCount", pagesCount);
            generateElement(ident, "CharacterCount", charsCount);
            generateElement(ident, "Language", language);
            for (String f : embededFonts) {
                output(ident, "<Font FontName=\"" + encodeContent(getNameFromPath(f)) + "\" isEmbeded=\"true\" />");
            }
            for (String f : refFonts) {
                output(ident, "<Font FontName=\"" + encodeContent(getNameFromPath(f)) + "\" isEmbeded=\"false\" />");
            }
            for (String r : references) {
                generateElement(ident, "Reference", encodeContent(r));
            }
            if (hasEncryption) generateElement(ident, "Features", "hasEncryption");
            if (hasSignature) generateElement(ident, "Features", "hasSignature");
            if (hasAudio) generateElement(ident, "Features", "hasAudio");
            if (hasVideo) generateElement(ident, "Features", "hasVideo");
            if (hasFixedLayout) generateElement(ident, "Features", "hasFixedLayout");
            if (hasScript) generateElement(ident, "Features", "hasScript");
            
            output(--ident, "</document>");
            output(--ident, "</doc>");

        } catch (FileNotFoundException e) {
            System.err.println("FileNotFound error: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("FileNotFound error: " + e.getMessage());
        } finally {
            out.close();
        }
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

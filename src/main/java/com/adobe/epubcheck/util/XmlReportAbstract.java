package com.adobe.epubcheck.util;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.reporting.CheckMessage;

public abstract class XmlReportAbstract extends MasterReport {
	protected PrintWriter out;

	protected String epubCheckName = "epubcheck";
	protected String epubCheckVersion;
	protected String epubCheckDate = "2012-10-31"; // default date to be
												   // overiden by the property
	protected String generationDate;

	protected String creationDate;
	protected String lastModifiedDate;
	protected String identifier;
	protected Set<String> titles = new LinkedHashSet<String>();
	protected final Set<String> creators = new LinkedHashSet<String>();
	protected final Set<String> contributors = new LinkedHashSet<String>();
	protected final Set<String> subjects = new LinkedHashSet<String>();
	protected String publisher;
	protected final Set<String> rights = new LinkedHashSet<String>();
	protected String date;

	protected String formatName;
	protected String formatVersion;
	protected long pagesCount;
	protected long charsCount;
	protected String language;
	protected final Set<String> embeddedFonts = new LinkedHashSet<String>();
	protected final Set<String> refFonts = new LinkedHashSet<String>();
	protected final Set<String> references = new LinkedHashSet<String>();
	protected boolean hasEncryption;
	protected boolean hasSignatures;
	protected boolean hasAudio;
	protected boolean hasVideo;
	protected boolean hasFixedLayout;
	protected boolean hasScripts;

	protected final List<CheckMessage> warns = new ArrayList<CheckMessage>();
	protected final List<CheckMessage> errors = new ArrayList<CheckMessage>();
	protected final List<CheckMessage> fatalErrors = new ArrayList<CheckMessage>();
	protected final List<CheckMessage> hints = new ArrayList<CheckMessage>();

	public XmlReportAbstract(PrintWriter out, String ePubName, String versionEpubCheck) {
		this.out = out;
		this.setEpubFileName(PathUtil.removeWorkingDirectory(ePubName));
		this.epubCheckVersion = versionEpubCheck;
	}

	public void initialize() {
	}

	@Override
	public void close() {
	}

	@Override
	public void message(Message message, EPUBLocation location, Object... args) {
		Severity s = message.getSeverity();
		switch (s) {
		case FATAL:
			CheckMessage.addCheckMessage(fatalErrors, message, location, args);
			break;
		case ERROR:
			CheckMessage.addCheckMessage(errors, message, location, args);
			break;
		case WARNING:
			CheckMessage.addCheckMessage(warns, message, location, args);
			break;
		case USAGE:
			CheckMessage.addCheckMessage(hints, message, location, args);
			break;
		}
	}

	@Override
	public void info(String resource, FeatureEnum feature, String value) {
		switch (feature) {
		case TOOL_DATE:
			if (value != null && !value.startsWith("$")) {
				this.epubCheckDate = value;
			}
			break;
		case TOOL_NAME:
			this.epubCheckName = value;
			break;
		case TOOL_VERSION:
			this.epubCheckVersion = value;
			break;
		case FORMAT_NAME:
			this.formatName = value;
			break;
		case FORMAT_VERSION:
			this.formatVersion = value;
			break;
		case CREATION_DATE:
			this.creationDate = value;
			break;
		case MODIFIED_DATE:
			this.lastModifiedDate = value;
			break;
		case PAGES_COUNT:
			this.pagesCount = Long.parseLong(value);
			break;
		case CHARS_COUNT:
			this.charsCount += Long.parseLong(value);
			break;
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
		case DC_LANGUAGE:
			this.language = value;
			break;
		case DC_TITLE:
			this.titles.add(value);
			break;
		case DC_CREATOR:
			this.creators.add(value);
			break;
		case DC_CONTRIBUTOR:
			this.contributors.add(value);
			break;
		case DC_PUBLISHER:
			this.publisher = value;
			break;
		case DC_SUBJECT:
			this.subjects.add(value);
			break;
		case DC_RIGHTS:
			this.rights.add(value);
			break;
		case DC_DATE:
			this.date = value;
			break;
		case UNIQUE_IDENT:
			if (resource == null) {
				this.identifier = value;
			}
			break;
		case HAS_SIGNATURES:
			this.hasSignatures = true;
			break;
		case HAS_ENCRYPTION:
			this.hasEncryption = true;
			break;
		case HAS_FIXED_LAYOUT:
			this.hasFixedLayout = true;
			break;
		case HAS_SCRIPTS:
			this.hasScripts = true;
			break;
		default:
		  break;
		}
	}

	protected String getNameFromPath(String path) {
		if (path == null || path.length() == 0) {
			return null;
		}
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) {
			return path;
		} else {
			return path.substring(lastSlash + 1);
		}
	}

	public abstract int generateReport();

	public int generate() {
		int returnCode = 1;
		try {
			returnCode = generateReport();
		} finally {
        	if (out != null) {
        		out.flush();
        		out.close();
        	}
		}
		return returnCode;
	}

	protected void output(int ident, String value) {
		if (ident != 0) {
			char[] spaces = new char[ident];
			Arrays.fill(spaces, ' ');
			out.print(spaces);
		}
		out.println(value);
	}

	protected String capitalize(String in) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (i == 0)
				sb.append(Character.toUpperCase(c));
			else
				sb.append(c);
		}
		return sb.toString();
	}

	protected void startElement(int ident, String name, List<KeyValue<String, String>> attrs) {
		if (name == null || name.trim().length() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(name);
		if (attrs != null && attrs.size() != 0) {
			for (KeyValue<String, String> attr : attrs) {
				sb.append(' ').append(attr.getKey()).append("=\"");
				sb.append(encodeContent(attr.getValue())).append('"');
			}
		}
		sb.append('>');
		output(ident, sb.toString());
	}

	protected void startElement(int ident, String name, KeyValue<String, String>... attrs) {
		startElement(ident, name, Arrays.asList(attrs));
	}

	protected void startElement(int ident, String name) {
		startElement(ident, name, (List<KeyValue<String, String>>) null);
	}

	protected void endElement(int ident, String name) {
		if (name == null || name.trim().length() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("</").append(name).append('>');
		output(ident, sb.toString());
	}

	protected void generateElement(int ident, String name, String value) {
		if (value == null || value.trim().length() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(name).append('>');
		sb.append(encodeContent(value.trim()));
		sb.append("</").append(name).append('>');
		output(ident, sb.toString());
	}

	protected void generateElement(int ident, String name, String value, KeyValue<String, String>... attrs) {
		generateElement(ident, name, value, Arrays.asList(attrs));
	}

	protected void generateElement(int ident, String name, String value, List<KeyValue<String, String>> attrs) {
		if (name == null || name.trim().length() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(name);
		for (KeyValue<String, String> attr : attrs) {
			sb.append(' ').append(attr.getKey()).append("=\"");
			sb.append(encodeContent(attr.getValue())).append('"');
		}
		if (value == null || value.trim().length() == 0) {
			sb.append(" />");
		} else {
			sb.append('>');
			sb.append(encodeContent(value.trim()));
			sb.append("</").append(name).append('>');
		}
		output(ident, sb.toString());
	}
	
	/**
	 * Encodes a content String in XML-clean form, converting characters to
	 * entities as necessary. The null string will be converted to an empty
	 * string.
	 */
	protected static String encodeContent(String content) {
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

	/**
	 * Transform time into ISO 8601 string.
	 */
	protected static String fromTime(final long time) {
		Date date = new Date(time);
		// Waiting for Java 7: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
		return formatted.substring(0, 22) + ":" + formatted.substring(22);
	}

}

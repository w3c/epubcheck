package com.adobe.epubcheck.util;

import java.io.File;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.reporting.CheckMessage;

/**
 * Abstract class to generate a report in XML.
 * 
 * It collects the information needed for the report and provides helper methods to generate proper XML.
 * In order to generate a specific XML, the generateReport method should be provided in a derived class.
 * 
 */
public abstract class XmlReportAbstract extends MasterReport {
	protected PrintWriter out;

	protected String epubCheckName = "epubcheck";
	protected String epubCheckVersion;
	protected String epubCheckDate = "2012-10-31"; // default date to be
												   // overridden by the property
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
	protected final Set<String> mediaTypes = new LinkedHashSet<String>();

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
		case INFO:
			break;
		case SUPPRESSED:
			break;
		default:
			break;
		}
	}

	@Override
	public void info(String resource, FeatureEnum feature, String value) {
		// Dont store 'null' values
		if (value == null) return;
		
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
			mediaTypes.add(value);
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
		case SPINE_INDEX:
			break;
		default:
		  break;
		}
	}

	protected String getNameFromPath(String path) {
		if (path == null || path.length() == 0) {
			return null;
		}
		// Try / because of uris 
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) {
			if (File.separatorChar != '/') {
				int lastSlash2 = path.lastIndexOf(File.separatorChar);
				if (lastSlash2 == -1) {
					return path;
				} else {
					return path.substring(lastSlash2 + 1);
				}
			} else {
				return path;
			}
		} else {
			return path.substring(lastSlash + 1);
		}
	}

	/**
	 * Method to implement effective report generation.
	 * @return errorCode 
	 */
	public abstract int generateReport();

	// Variables for report generation
	private Document doc;
	private Element currentEl;
	private String namespaceURI;
	private Map<String, String> namespaces;

	public void setNamespace(String uri) {
		namespaceURI = uri;
	}
	public void addPrefixNamespace(String prefix, String uri) {
		namespaces.put(prefix, uri);
	}
	
	
	public int generate() {
		namespaces = new HashMap<String, String>();
		
		int returnCode = 1;
		try {
			// Initialize the DOM 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();

			// Calculate the report
			returnCode = generateReport();
			
			if (returnCode == 0) {
				// Output the report
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(out);
				transformer.transform(source, result);			
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			returnCode = 1;
		} catch (TransformerException e) {
	        //System.err.println(Messages.get("error_generating_report"));
			System.err.println("Error while generating the XML report " + e.getMessage());
			e.printStackTrace();
			returnCode = 1;
		} finally {
        	if (out != null) {
        		out.flush();
        		out.close();
        	}
		}
		return returnCode;
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

	private Element makeElement(String name) {
		Element el;
		int index = name.indexOf(':');
		if (index == -1) {
			if (namespaceURI == null) {
				el = doc.createElement(name);
			} else {
				el = doc.createElementNS(namespaceURI, name);
			} 
		} else {
			String prefix = name.substring(0, index);
			String uri = namespaces.get(prefix);
			if (uri == null) {
				el = doc.createElement(name);
			} else {
				el = doc.createElementNS(uri, name);
			} 
		}
		return el;
	}

	private Attr makeAttribute(KeyValue<String, String> kv) {
		Attr attr;
		String attName = kv.getKey();
		int iAttr = attName.indexOf(':');
		if (iAttr == -1) {
			attr = doc.createAttribute(attName);
		} else {
			String prefix = attName.substring(0, iAttr);
			String uri = namespaces.get(prefix);
			if (uri == null) {
				attr = doc.createAttribute(attName);
			} else {
				attr = doc.createAttributeNS(uri, attName);
			} 
		}
		attr.setValue(kv.getValue());
		return attr;
	}

	protected void startElement(String name, List<KeyValue<String, String>> attrs) {
		if (name == null || name.trim().length() == 0) {
			return;
		}
		Element el = makeElement(name.trim());

		if (attrs != null && attrs.size() != 0) {
			for (KeyValue<String, String> attr : attrs) {
				el.setAttributeNode(makeAttribute(attr));
			}
		}
		if (currentEl == null) {
			doc.appendChild(el);
		} else {
			currentEl.appendChild(el);
		}
		currentEl = el;
	}

	@SuppressWarnings("unchecked")
	protected void startElement(String name, KeyValue<String, String>... attrs) {
		startElement(name, Arrays.asList(attrs));
	}

	protected void startElement(String name) {
		startElement(name, (List<KeyValue<String, String>>) null);
	}

	protected void endElement(String name) {
		if (currentEl == null) return;
		Node parent = currentEl.getParentNode();
		if (parent == null || parent == doc) {
			currentEl = null;
		} else if (parent instanceof Element) {
			currentEl = (Element)currentEl.getParentNode();
		} else {
			System.out.println("Pb at Element [" + currentEl.getLocalName() + "] with parent " + parent);
		}
	}
	
	protected void generateElement(String name, String value) {
		if (name == null || name.trim().length() == 0 || value == null || value.trim().length() == 0) {
			return;
		}
		Element el = makeElement(name.trim());
		el.appendChild(doc.createTextNode(correctToUtf8(value.trim())));
		currentEl.appendChild(el);
	}

	@SuppressWarnings("unchecked")
	protected void generateElement(String name, String value, KeyValue<String, String>... attrs) {
		generateElement(name, value, Arrays.asList(attrs));
	}

	protected void generateElement(String name, String value, List<KeyValue<String, String>> attrs) {
		if (name == null || name.trim().length() == 0) {
			return;
		}
		Element el = makeElement(name);
		if (attrs != null && attrs.size() != 0) {
			for (KeyValue<String, String> attr : attrs) {
				el.setAttributeNode(makeAttribute(attr));
			}
		}
		if (value != null && value.trim().length() != 0) {
			el.appendChild(doc.createTextNode(correctToUtf8(value.trim())));
		}
		currentEl.appendChild(el);
	}
	
    /**
     * Make sure the string contains valid UTF-8 characters
     * @param inputString
     * @return escaped String
     */
    protected static String correctToUtf8(String inputString) {
        final StringBuilder result = new StringBuilder(inputString.length());
        final StringCharacterIterator it = new StringCharacterIterator(inputString);
        char ch = it.current();
        boolean modified = false;
        while (ch != CharacterIterator.DONE) {
            if (Character.isISOControl(ch)) {
            	if (ch == '\r' || ch == '\n') {
                    result.append(ch);
            	} else {
		            modified = true;
		            result.append(String.format("0x%x", (int) ch));
            	}
            } else {
                result.append(ch);
            }
            ch = it.next();
        }

        if (!modified) return inputString;

        return result.toString();
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

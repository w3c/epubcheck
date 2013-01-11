/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.opf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.DateParser;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.InvalidDateException;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OPFHandler implements XMLHandler {

	Hashtable<String, OPFItem> itemMapById = new Hashtable<String, OPFItem>();

	Hashtable<String, OPFItem> itemMapByPath = new Hashtable<String, OPFItem>();

	// Hashtable encryptedItems;

	XMLParser parser;

	Vector<OPFItem> spine = new Vector<OPFItem>();
	Vector<OPFItem> items = new Vector<OPFItem>();
	Vector<OPFReference> refs = new Vector<OPFReference>();

	Report report;

	static HashSet<String> validRoles = new HashSet<String>();

	String path;

	XRefChecker xrefChecker;

	// This string holds the value of the <package> element's unique-identifier
	// attribute
	// that will be used to make sure that the unique-identifier references an
	// existing
	// <dc:identifier> id attribute
	String uniqueIdent;

	// This boolean specifies whether or not there has been a <dc:identifier>
	// element
	// parsed that has an id attribute that corresponds with the
	// unique-identifier attribute
	// from the packaging element. The default value is false.
	boolean uniqueIdentExists = false;

	OPFItem toc;

	boolean opf12PackageFile = false;

	EPUBVersion version;

	static {
		String[] list = { "acp", "act", "adp", "aft", "anl", "anm", "ann",
				"ant", "app", "aqt", "arc", "ard", "arr", "art", "asg", "asn",
				"att", "auc", "aud", "aui", "aus", "aut", "bdd", "bjd", "bkd",
				"bkp", "bnd", "bpd", "bsl", "ccp", "chr", "clb", "cli", "cll",
				"clr", "clt", "cmm", "cmp", "cmt", "cng", "cnd", "cns", "coe",
				"col", "com", "cos", "cot", "cov", "cpc", "cpe", "cph", "cpl",
				"cpt", "cre", "crp", "crr", "csl", "csp", "cst", "ctb", "cte",
				"ctg", "ctr", "cts", "ctt", "cur", "cwt", "dfd", "dfe", "dft",
				"dgg", "dis", "dln", "dnc", "dnr", "dpc", "dpt", "drm", "drt",
				"dsr", "dst", "dtc", "dte", "dtm", "dto", "dub", "edt", "egr",
				"elg", "elt", "eng", "etr", "exp", "fac", "fld", "flm", "fmo",
				"fpy", "fnd", "frg", "gis", "grt", "hnr", "hst", "ill", "ilu",
				"ins", "inv", "itr", "ive", "ivr", "lbr", "lbt", "ldr", "led",
				"lee", "lel", "len", "let", "lgd", "lie", "lil", "lit", "lsa",
				"lse", "lso", "ltg", "lyr", "mcp", "mfr", "mdc", "mod", "mon",
				"mrk", "msd", "mte", "mus", "nrt", "opn", "org", "orm", "oth",
				"own", "pat", "pbd", "pbl", "pdr", "pfr", "pht", "plt", "pma",
				"pmn", "pop", "ppm", "ppt", "prc", "prd", "prf", "prg", "prm",
				"pro", "prt", "pta", "pte", "ptf", "pth", "ptt", "rbr", "rce",
				"rcp", "red", "ren", "res", "rev", "rps", "rpt", "rpy", "rse",
				"rsg", "rsp", "rst", "rth", "rtm", "sad", "sce", "scl", "scr",
				"sds", "sec", "sgn", "sht", "sng", "spk", "spn", "spy", "srv",
				"std", "stl", "stm", "stn", "str", "tcd", "tch", "ths", "trc",
				"trl", "tyd", "tyg", "vdg", "voc", "wam", "wdc", "wde", "wit" };
		for (int i = 0; i < list.length; i++)
			validRoles.add(list[i]);
	}

	public OPFHandler(String path, Report report,
			XRefChecker xrefChecker, XMLParser parser, EPUBVersion version) {
		this.path = path;
		this.report = report;
		this.xrefChecker = xrefChecker;
		this.parser = parser;
		this.version = version;
	}

	public boolean getOpf12PackageFile() {
		return (opf12PackageFile);
	}

	public boolean getOpf20PackageFile() {
		return (!opf12PackageFile);
	}

	public OPFItem getTOC() {
		return toc;
	}

	public OPFItem getItemById(String id) {
		return (OPFItem) itemMapById.get(id);
	}

	public OPFItem getItemByPath(String path) {
		return (OPFItem) itemMapByPath.get(path);
	}

	public int getSpineItemCount() {
		return spine.size();
	}

	public OPFItem getSpineItem(int index) {
		return (OPFItem) spine.elementAt(index);
	}

	public int getItemCount() {
		return items.size();
	}

	public OPFItem getItem(int index) {
		return (OPFItem) items.elementAt(index);
	}

	public int getReferenceCount() {
		return refs.size();
	}

	public OPFReference getReference(int index) {
		return (OPFReference) refs.elementAt(index);
	}

	/**
	 * Checks to see if the unique-identifier attribute of the package element
	 * references an existing DC metadata identifier element's id attribute
	 * 
	 * @return true if there is an identifier with an id attribute that matches
	 *         the value of the unique-identifier attribute of the package
	 *         element. False otherwise.
	 */
	public boolean checkUniqueIdentExists() {
		return uniqueIdentExists;
	}

	// public void setEncryptedItems(Hashtable encryptedItems) {
	// this.encryptedItems = encryptedItems;
	// }

	private static boolean isValidRole(String role) {
		return validRoles.contains(role) || role.startsWith("oth.");
	}

	public void startElement() {
		boolean registerEntry = true;
		XMLElement e = parser.getCurrentElement();
		String ns = e.getNamespace();
		if (ns == null
				|| ns.equals("")
				|| ns.equals("http://openebook.org/namespaces/oeb-package/1.0/")
				|| ns.equals("http://www.idpf.org/2007/opf")) {
			String name = e.getName();
			if (name.equals("package")) {
				if (!ns.equals("http://www.idpf.org/2007/opf")) {
					report.warning(path, parser.getLineNumber(),
							parser.getColumnNumber(),
							"OPF file is using OEBPS 1.2 syntax allowing backwards compatibility");
					opf12PackageFile = true;
				}
				/*
				 * This section checks to see the value of the unique-identifier
				 * attribute and stores it in the String uniqueIdent or reports
				 * an error if the unique-identifier attribute is missing or
				 * does not have a value
				 */
				String uniqueIdentAttr = e.getAttribute("unique-identifier");
				if (uniqueIdentAttr != null && !uniqueIdentAttr.equals("")) {
					uniqueIdent = uniqueIdentAttr;
				} else {
					report.error(
							path,
							parser.getLineNumber(),
							parser.getColumnNumber(),
							"unique-identifier attribute in package element must be present and have a value");
				}
			} else if (name.equals("item")) {
				String id = e.getAttribute("id");
				String href = e.getAttribute("href");
				if (href != null
						&& !(version == EPUBVersion.VERSION_3 && href
								.startsWith("http://"))) {
					try {
						href = PathUtil.resolveRelativeReference(path, href,
								null);
					} catch (IllegalArgumentException ex) {
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(), ex.getMessage());
						href = null;
					}
				}
				if (href != null && href.startsWith("http")) {
					report.info(path, FeatureEnum.REFERENCE, href);
				}
				String mimeType = e.getAttribute("media-type");
				String fallback = e.getAttribute("fallback");
				String fallbackStyle = e.getAttribute("fallback-style");
				String namespace = e.getAttribute("island-type");
				String properties = e.getAttribute("properties");
				if (properties != null)
					properties = properties.replaceAll("[\\s]+", " ");

				if (version == EPUBVersion.VERSION_3
						&& href.startsWith("http://")
						&& !OPFChecker30.isBlessedAudioType(mimeType))
					if (OPFChecker30.isCoreMediaType(mimeType)) {
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(),
								"Only audio and video remote resources are permitted");
					} else {
						// mgy 20120414: this shouldn't even be a warning
						// report.warning(
						// path,
						// parser.getLineNumber(),
						// parser.getColumnNumber(),
						// "Remote resource not validated");
					}

				OPFItem item = new OPFItem(id, href, mimeType, fallback,
						fallbackStyle, namespace, properties,
						parser.getLineNumber(), parser.getColumnNumber());

				if (id != null)
					itemMapById.put(id, item);
				if (properties != null) {
					String propertyArray[] = properties.split(" ");
					for (int i = 0; i < propertyArray.length; i++) {
						if (propertyArray[i].equals("nav"))
							item.setNav(true);
						if (propertyArray[i].equals("scripted"))
							item.setScripted(true);
					}

				}
				if (href != null && registerEntry) {
					itemMapByPath.put(href, item);
					items.add(item);
				}
			} else if (name.equals("reference")) {
				String type = e.getAttribute("type");
				String title = e.getAttribute("title");
				String href = e.getAttribute("href");
				if (href != null && xrefChecker != null) {
					try {
						href = PathUtil.resolveRelativeReference(path, href,
								null);
						xrefChecker.registerReference(path,
								parser.getLineNumber(),
								parser.getColumnNumber(), href,
								XRefChecker.RT_GENERIC);
					} catch (IllegalArgumentException ex) {
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(), ex.getMessage());
						href = null;
					}
				}
				if (href != null && href.startsWith("http")) {
					report.info(path, FeatureEnum.REFERENCE, href);
				}
				OPFReference ref = new OPFReference(type, title, href,
						parser.getLineNumber(), parser.getColumnNumber());
				refs.add(ref);
			} else if (name.equals("spine")) {
				String idref = e.getAttribute("toc");
				if (idref != null) {
					toc = (OPFItem) itemMapById.get(idref);
					if (toc == null)
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(), "item with id '"
										+ idref + "' not found");
					else {
						toc.setNcx(true);
						if (toc.getMimeType() != null
								&& !toc.getMimeType().equals(
										"application/x-dtbncx+xml"))
							report.error(
									path,
									parser.getLineNumber(),
									parser.getColumnNumber(),
									"toc attribute references resource with non-NCX mime type; \"application/x-dtbncx+xml\" is expected");
					}
				}
			} else if (name.equals("itemref")) {
				String idref = e.getAttribute("idref");
				if (idref != null) {
					OPFItem item = getItemById(idref);
					if (item != null) {
						spine.add(item);
						item.setInSpine(true);

						String linear = e.getAttribute("linear");
						if (linear != null && "no".equals(linear.trim())) {
							item.setSpineLinear(false);
						} else {
							item.setSpineLinear(true);
						}

					} else {
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(), "item with id '"
										+ idref + "' not found");
					}
				}
			} else if (name.equals("dc-metadata") || name.equals("x-metadata")) {
				if (!opf12PackageFile)
					report.error(path, parser.getLineNumber(),
							parser.getColumnNumber(),
							"use of deprecated element '" + name + "'");
			}
		} else if (ns.equals("http://purl.org/dc/elements/1.1/")) {
			// in the DC metadata, when the <identifier> element is parsed, if
			// it has a non-null and non-empty id attribute value that is the
			// same as the value of the unique-identifier attribute of the
			// package element, set uniqueIdentExists = true (to make sure that
			// the unique-identifier attribute references an existing
			// <identifier> id attribute
			String name = e.getName();
			if (name.equals("identifier")) {
				String idAttr = e.getAttribute("id");
				if (idAttr != null && !idAttr.equals("")
						&& idAttr.equals(uniqueIdent)) {
					uniqueIdentExists = true;
				}
			} else if (name.equals("creator")) {
				String role = e.getAttributeNS("http://www.idpf.org/2007/opf",
						"role");
				if (role != null && !role.equals("")) {
					if (!isValidRole(role))
						report.error(path, parser.getLineNumber(),
								parser.getColumnNumber(), "role value '" + role
										+ "' is not valid");
				}
			}

		}
	}

	/**
	 * This method is used to check whether the passed href has and extension
	 * that is a required content file inside the OPF spec. This method returns
	 * false if it is requried, or true if it is not.
	 * 
	 * @param href
	 *            String to check
	 * @return true if it is not required, false if it is.
	 */
	public boolean isNotRequiredContent(String href) {
		if (href.endsWith(".opf"))
			return false;
		else if (href.endsWith(".html"))
			return false;
		else if (href.endsWith(".ncx"))
			return false;
		else if (href.endsWith(".xpgt"))
			return false;
		else if (href.endsWith(".xhtml"))
			return false;
		else
			return true;

	}

	public void endElement() {

		XMLElement e = parser.getCurrentElement();
		if ("http://www.idpf.org/2007/opf".equals(e.getNamespace())) {
			String name = e.getName();
            // <meta property="dcterms:modified">2012-02-27T16:38:35Z</meta>
			// <meta property="rendition:layout">pre-paginated</meta>
			if ("meta".equals(name)) {
				String attr = e.getAttribute("property");
				if ("dcterms:modified".equals(attr)) {
                    String val = (String) e.getPrivateData();
                    report.info(null, FeatureEnum.MODIFIED_DATE,
                            val);
                } else if ("rendition:layout".equals(attr)) {
					String val = (String) e.getPrivateData();
					if ("pre-paginated".equals(val)) {
						report.info(null, FeatureEnum.HAS_FIXED_LAYOUT,
								"pre-paginated");
					}
				} else {
					String attr1 = e.getAttribute("name");
					if ("fixed-layout".equals(attr1)
							&& "true".equals(e.getAttribute("content"))) {
						report.info(null, FeatureEnum.HAS_FIXED_LAYOUT,
								"fixed-layout");
					}
				}
			}
		} else if (e.getNamespace().equals("http://purl.org/dc/elements/1.1/")) {
			String name = e.getName();
			if (name.equals("identifier")) {
				String idAttr = e.getAttribute("id");
				if (idAttr != null && !idAttr.equals("")
						&& idAttr.equals(uniqueIdent)) {
					String idval = (String) e.getPrivateData();
					// if (idval != null && ocf != null)
					// ocf.setUniqueIdentifier(idval);
					if (idval != null) {
						report.info(null, FeatureEnum.UNIQUE_IDENT,
								idval.trim());
					}
				}
			} else if (name.equals("date")) {
				String dateval = (String) e.getPrivateData();
				boolean valid = true;
				String detail = null;

				if (dateval == null || "".equals(dateval)) {
					valid = false;
					detail = "zero-length string";
				} else {
					DateParser dateParser = new DateParser();
					try {
						Date date = dateParser.parse(dateval.trim());
						/*
						 * mg: DateParser does not enforce four-digit years,
						 * which http://www.w3.org/TR/NOTE-datetime seems to
						 * want
						 */
						String year = new SimpleDateFormat("yyyy").format(date);
						if (year.length() > 4)
							throw new InvalidDateException(year);
						report.info(null, FeatureEnum.DC_DATE, dateval);
					} catch (InvalidDateException d) {
						valid = false;
						detail = d.getMessage();
					}
				}

				if (!valid) {
					if (this.version == EPUBVersion.VERSION_3) {
						report.warning(
								path,
								parser.getLineNumber(),
								parser.getColumnNumber(),
								"date value '"
										+ (dateval == null ? "" : dateval)
										+ "' does not follow recommended syntax as per http://www.w3.org/TR/NOTE-datetime:"
										+ detail);
					} else {
						report.error(
								path,
								parser.getLineNumber(),
								parser.getColumnNumber(),
								"date value '"
										+ (dateval == null ? "" : dateval)
										+ "' is not valid as per http://www.w3.org/TR/NOTE-datetime:"
										+ detail);
					}
				}
			} else if (name.equals("title") || name.equals("language")) {
				// issue 138: issue a warning if dc:title and dc:language is
				// empty for 2.0 and 2.0.1
				// note that an empty dc:identifier is checked in opf20.rng and
				// will
				// therefore be reported as an error, that may or may not be a
				// good idea.
				if ("language".equals(name)) {
					String value = (String) e.getPrivateData();
					if (value != null) {
						report.info(null, FeatureEnum.DC_LANGUAGE, value.trim());
					}
				} else if ("title".equals(name)) {
					String value = (String) e.getPrivateData();
					if (value != null) {
						report.info(null, FeatureEnum.DC_TITLE, value.trim());
					}
				}
				if (version == EPUBVersion.VERSION_2) {
					String value = (String) e.getPrivateData();
					if (value == null || value.trim().length() < 1) {
						report.warning(path, parser.getLineNumber(),
								parser.getColumnNumber(), name
										+ " element is empty");
					}
				}
			} else if (name.equals("creator")) {
				String value = (String) e.getPrivateData();
				if (value != null) {
					report.info(null, FeatureEnum.DC_CREATOR, value.trim());
				}
			} else if (name.equals("contributor")) {
				String value = (String) e.getPrivateData();
				if (value != null) {
					report.info(null, FeatureEnum.DC_CONTRIBUTOR, value.trim());
				}
			} else if (name.equals("publisher")) {
				String value = (String) e.getPrivateData();
				if (value != null) {
					report.info(null, FeatureEnum.DC_PUBLISHER, value.trim());
				}
			} else if (name.equals("rights")) {
				String value = (String) e.getPrivateData();
				if (value != null) {
					report.info(null, FeatureEnum.DC_RIGHTS, value.trim());
				}
			}
		}
	}

	public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
	}

	public void characters(char[] chars, int start, int len) {

		XMLElement e = parser.getCurrentElement();
		String name = e.getName();
		String ns = e.getNamespace();
		boolean keepValue = ("http://www.idpf.org/2007/opf".equals(ns) && "meta"
				.equals(name))
				|| ("http://purl.org/dc/elements/1.1/".equals(ns) && (name
						.equals("identifier")
						|| name.equals("date")
						|| name.equals("title")
						|| name.equals("language")
						|| name.equals("creator")
						|| name.equals("contributor")
						|| name.equals("publisher") || name.equals("rights")));
		if (keepValue) {
			String val = (String) e.getPrivateData();
			String text = new String(chars, start, len);
			if (val == null) {
				val = text;
			} else {
				val = val + text;
			}
			e.setPrivateData(val);
		}
	}

	public void processingInstruction(String arg0, String arg1) {
	}

	public EPUBVersion getVersion() {
		return version;
	}
}

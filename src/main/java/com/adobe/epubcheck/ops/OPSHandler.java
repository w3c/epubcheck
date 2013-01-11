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

package com.adobe.epubcheck.ops;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.xml.XMLConstants;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.css.CSSCheckerFactory;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class OPSHandler implements XMLHandler {

	String path;
	
	/** null unless head/base or xml:base is given */
	protected String base; 

	XRefChecker xrefChecker;

	static HashSet<String> regURISchemes = fillRegURISchemes();

	XMLParser parser;
	OCFPackage ocf;
	Report report;
	EPUBVersion version;

	long openElements;  
	long charsCount;
	
	StringBuilder textNode;
	
	public OPSHandler(OCFPackage ocf, String path, XRefChecker xrefChecker, XMLParser parser,
			Report report, EPUBVersion version) {
		this.ocf = ocf;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.report = report;
		this.parser = parser;
		this.version = version;		
	}

	private void checkPaint(XMLElement e, String attr) {
		String paint = e.getAttribute(attr);
		if (xrefChecker != null && paint != null && paint.startsWith("url(")
				&& paint.endsWith(")")) {
			String href = paint.substring(4, paint.length() - 1);
			href = PathUtil.resolveRelativeReference(path, href, base);
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href, XRefChecker.RT_SVG_PAINT);
		}
	}

	private void checkClip(XMLElement e, String attr) {

	}

	private void checkImage(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (xrefChecker != null && href != null) {
			href = PathUtil.resolveRelativeReference(path, href, base);
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href, XRefChecker.RT_IMAGE);
		}
	}

	private void checkObject(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (xrefChecker != null && href != null) {
			href = PathUtil.resolveRelativeReference(path, href, base);
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href, XRefChecker.RT_OBJECT);
		}
	}

	private void checkLink(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		String rel = e.getAttributeNS(attrNS, "rel");
		if (xrefChecker != null && href != null && rel != null
				&& rel.indexOf("stylesheet") >= 0) {
			href = PathUtil.resolveRelativeReference(path, href, base);
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href, XRefChecker.RT_STYLESHEET);
		}
	}

	private void checkSymbol(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		if (xrefChecker != null && href != null) {
			href = PathUtil.resolveRelativeReference(path, href, base);
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href, XRefChecker.RT_SVG_SYMBOL);
		}
	}

	private void checkHRef(XMLElement e, String attrNS, String attr) {
		String href = e.getAttributeNS(attrNS, attr);
		//System.out.println("HREF: '" + href +"'");
		if(href == null) {
			return;
		}
		
		if(href.contains("#epubcfi")) { 
			return; //temp until cfi implemented
		}
		
		href = href.trim();
		
		if (href.length() < 1) {
			// if href="" then selfreference, no need to check
			// but as per issue 225, issue w warning
			//change: issue this when we have a compatiblity hint 
			// level, not as a generic warning
//			report.warning(path, parser.getLineNumber(),
//					parser.getColumnNumber(), Messages.EMPTY_HREF);
			return;
		}
		
		if (".".equals(href)) {
			//selfreference, no need to check
		}
		
 
        if (href.startsWith("http")) {
            report.info(path, FeatureEnum.REFERENCE, href);
        }
        
        /* 
		 * mgy 20120417 adding check for base to initial if clause as part
		 * of solution to issue 155
		 */
		if (isRegisteredSchemeType(href) || (null != base && isRegisteredSchemeType(base))) {
			return;
		}					
		
		// This if statement is needed to make sure XML Fragment identifiers
		// are not reported as non-registered URI scheme types
		else if (href.indexOf(':') > 0) {			
				report.warning(path, parser.getLineNumber(),
						parser.getColumnNumber(),
						"use of non-registered URI scheme type in href: "
								+ href);
				return;			
		}
		
		try {
			href = PathUtil.resolveRelativeReference(path, href, base);
		} catch (IllegalArgumentException err) {
			report.error(path, parser.getLineNumber(),
					parser.getColumnNumber(), err.getMessage());
			return;
		}
		if (xrefChecker != null)
			xrefChecker.registerReference(path, parser.getLineNumber(),
					parser.getColumnNumber(), href,
					XRefChecker.RT_HYPERLINK);
		
	}

	public static boolean isRegisteredSchemeType(String href) {
		int colonIndex = href.indexOf(':');
		if (colonIndex < 0)
			return false;
		else if (regURISchemes.contains(href.substring(0, colonIndex + 1)))
			return true;
		else if (href.length() > colonIndex + 2)
			if (href.substring(colonIndex + 1, colonIndex + 3).equals("//")
					&& regURISchemes
							.contains(href.substring(0, colonIndex + 3)))
				return true;
			else
				return false;
		else
			return false;
	}

	public void startElement() {
		openElements++;
		XMLElement e = parser.getCurrentElement();
		String id = e.getAttribute("id");
		
		String baseTest = e.getAttributeNS(XMLConstants.XML_NS_URI, "base"); 
		if(baseTest != null) {
			base = baseTest;
		}
				
		String ns = e.getNamespace();
		String name = e.getName();
		int resourceType = XRefChecker.RT_GENERIC;
		if (ns != null) {
			if (ns.equals("http://www.w3.org/2000/svg")) {
				if (name.equals("linearGradient")
						|| name.equals("radialGradient")
						|| name.equals("pattern"))
					resourceType = XRefChecker.RT_SVG_PAINT;
				else if (name.equals("clipPath"))
					resourceType = XRefChecker.RT_SVG_CLIP_PATH;
				else if (name.equals("symbol"))
					resourceType = XRefChecker.RT_SVG_SYMBOL;
				else if (name.equals("a"))
					checkHRef(e, "http://www.w3.org/1999/xlink", "href");
				else if (name.equals("use"))
					checkSymbol(e, "http://www.w3.org/1999/xlink", "href");
				else if (name.equals("image"))
					checkImage(e, "http://www.w3.org/1999/xlink", "href");
				checkPaint(e, "fill");
				checkPaint(e, "stroke");
				checkClip(e, "clip");
			} else if (ns.equals("http://www.w3.org/1999/xhtml")) {
				if (name.equals("a"))
					checkHRef(e, null, "href");
				else if (name.equals("img"))
					checkImage(e, null, "src");
				else if (name.equals("object"))
					checkObject(e, null, "data");
				else if (name.equals("link"))
					checkLink(e, null, "href");
				else if (name.equals("base"))
					base = e.getAttribute("href");				
				else if (name.equals("style"))
					textNode = new StringBuilder();	
				
				resourceType = XRefChecker.RT_HYPERLINK;
				
				String style = e.getAttribute("style"); 
				if(style!=null && style.length()>0) {
					CSSCheckerFactory.getInstance().newInstance(
							ocf, report, style, true, path, 
							parser.getLineNumber(), 
							parser.getColumnNumber(), xrefChecker, version).runChecks();
				}
				
				
			}
		}
		if (xrefChecker != null && id != null)
			xrefChecker.registerAnchor(path, parser.getLineNumber(),
					parser.getColumnNumber(), id, resourceType);
		
		
				
	}

	public void endElement() {
		openElements--;
		XMLElement e = parser.getCurrentElement();
        String ns = e.getNamespace();
        String name = e.getName();

		if (openElements == 0) {
		    report.info(path, FeatureEnum.CHARS_COUNT, Long.toString(charsCount));
		}
		
		if ("http://www.w3.org/1999/xhtml".equals(ns) && "script".equals(name)) {
		    String attr = e.getAttribute("type");
		    report.info(path, FeatureEnum.HAS_SCRIPTS, (attr==null)?"":attr);
		}
		
		if ("http://www.w3.org/1999/xhtml".equals(ns) && "style".equals(name)) {
		    String style = textNode.toString();		    
		    if(style.length()>0) {
				CSSCheckerFactory.getInstance().newInstance(
						ocf, report, style, false, path, 
						parser.getLineNumber(), 
						parser.getColumnNumber(), xrefChecker, version).runChecks();
			}
			textNode = null;
		}
	}

	public void ignorableWhitespace(char[] chars, int arg1, int arg2) {
	}

	public void characters(char[] chars, int start, int length) {
	    charsCount += length;
	    
	    if(textNode != null) {
	    	textNode.append(chars, start, length);
	    }
	}

	public void processingInstruction(String arg0, String arg1) {
	}
	
	private static HashSet<String> fillRegURISchemes() {
		InputStream schemaStream = null;
		BufferedReader schemaReader = null;
		try {
			HashSet<String> set = new HashSet<String>();
			schemaStream = OPSHandler.class
					.getResourceAsStream("registeredSchemas.txt");
			schemaReader = new BufferedReader(
					new InputStreamReader(schemaStream));
			String schema = schemaReader.readLine();
			while (schema != null) {
				set.add(schema);
				schema = schemaReader.readLine();
			}			
			return set;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				schemaReader.close();
				schemaStream.close();
			}catch (Exception e) {
				
			}
		}
		return null;
	}
}

/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

package com.adobe.epubcheck.css;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.PathUtil;

class CSSHandler implements DocumentHandler, ErrorHandler {

	String path;

	XRefChecker xrefChecker;

	Report report;

	boolean fontFace = false;
	String fontFamily;
	String fontStyle;
	String fontWeight;
	String fontUri;
	
	EPUBVersion version;

	public CSSHandler(String path, XRefChecker xrefChecker, Report report,
			EPUBVersion version) {
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.report = report;
		this.version = version;
	}
	
	public void property(String name, LexicalUnit value, boolean arg2)
			throws CSSException {
		//System.err.println("property: " + name /* + " " + value.getStringValue()*/ );
		if (name == null)
			return;
		if (name.equals("src")) {
			if (value != null
					&& value.getLexicalUnitType() == LexicalUnit.SAC_URI)
				if (value.getStringValue() != null) {
					
					String uri = value.getStringValue();
					//System.err.println(uri);
					uri = PathUtil.resolveRelativeReference(path, uri, null);
					fontUri = uri;
					xrefChecker.registerReference(path, -1, -1, uri,
							XRefChecker.RT_GENERIC);

					// OPS 2.0.1 Section 3.4 
					if (fontFace && version == EPUBVersion.VERSION_2) {
	                       String fontMimeType = xrefChecker.getMimeType(uri);
	                       if (fontMimeType != null && !isFontMimetype(fontMimeType)){
                               report.warning(path, -1, -1, "Font-face reference "
                                       + uri + " to non-standard font type "
                                       + fontMimeType);
	                       }
					}
					if (fontFace && version == EPUBVersion.VERSION_3) {								
						String fontMimeType = xrefChecker.getMimeType(uri);
						if(fontMimeType != null) {
							if (!OPFChecker30.isBlessedFontType(fontMimeType))
								report.error(path, -1, -1, "Font-face reference "
										+ uri + " to non-standard font type "
										+ fontMimeType);
						} else {
							//we should get error report elsewhere
						}
					}

				} else
					report.error(path, -1, -1, Messages.NULL_REF);
		} else if (name.equals("position") && value != null
				&& value.getStringValue() != null
				&& value.getStringValue().equals("fixed"))
			report.error(
					path,
					-1,
					-1,
					"The fixed value of the position property is not part of the EPUB 3 CSS Profile.");
		else if (name.equals("direction") || name.equals("unicode-bidi")) {
			report.error(
					path,
					-1,
					-1,
					"The direction and unicode-bidi properties must not be included in an EPUB Style Sheet.");
		} else if (fontFace && name.equals("font-family") && value != null && value.getStringValue() != null) {
		    fontFamily = value.getStringValue();;
        } else if (fontFace && name.equals("font-style") && value != null && value.getStringValue() != null) {
            fontStyle = value.getStringValue();;
        } else if (fontFace && name.equals("font-weight") && value != null && value.getStringValue() != null) {
            fontWeight = value.getStringValue();;
		}
	}
	
	public void comment(String text) throws CSSException {
	}

	public void endDocument(InputSource source) throws CSSException {
	}

	public void endFontFace() throws CSSException {
	    if (fontFamily != null) {
	        if (fontUri != null  && !fontUri.startsWith("http")) {
	            report.info(path, FeatureEnum.FONT_EMBEDDED, fontFamily + 
	                (((fontStyle!=null) && !"normal".equalsIgnoreCase(fontStyle))?","+fontStyle:"") +
                    (((fontWeight!=null) && !"normal".equalsIgnoreCase(fontWeight))?","+fontWeight:"")
	             );
	        } else {
                report.info(path, FeatureEnum.FONT_REFERENCE, fontFamily + 
                        (((fontStyle!=null) && !"normal".equalsIgnoreCase(fontStyle))?","+fontStyle:"") +
                        (((fontWeight!=null) && !"normal".equalsIgnoreCase(fontWeight))?","+fontWeight:"")
                     );
                report.info(path, FeatureEnum.REFERENCE, fontUri);
	        }
	    }
		fontFace = false;
	}

	public void endMedia(SACMediaList media) throws CSSException {
	}

	public void endPage(String name, String pseudo_page) throws CSSException {
	}

	public void endSelector(SelectorList selectors) throws CSSException {
	}

	public void ignorableAtRule(String atRule) throws CSSException {
	}

	public void importStyle(String uri, SACMediaList media,
			String defaultNamespaceURI) throws CSSException {
		//System.err.println("importStyle()");
		String ruri = PathUtil.resolveRelativeReference(path, uri, null);
		
		xrefChecker.registerReference(path, -1, -1, ruri,
				XRefChecker.RT_GENERIC);
		
	}

	public void namespaceDeclaration(String prefix, String uri)
			throws CSSException {
	}

	

	public void startDocument(InputSource source) throws CSSException {
	}

	public void startFontFace() throws CSSException {
		//System.err.println("startFontFace()");
		fontFace = true;
		fontFamily = null;
		fontStyle = null;
		fontWeight = null;
		fontUri = null;
	}

	public void startMedia(SACMediaList media) throws CSSException {
	}

	public void startPage(String name, String pseudo_page) throws CSSException {
	}

	public void startSelector(SelectorList selectors) throws CSSException {
	}

	/*
	 * Until we have a CSS3 compliant parser, things get wild in the
	 * errorhandler department. Just keep silent.
	 */
	@Override
	public void error(CSSParseException e) throws CSSException {		
		// System.err.println("CSSHandler#error: " + e.getMessage());
		
	}

	@Override
	public void fatalError(CSSParseException e) throws CSSException {
		// System.err.println("CSSHandler#fatalError: " + e.getMessage());
		
	}

	@Override
	public void warning(CSSParseException e) throws CSSException {
		// System.err.println("CSSHandler#warning: " + e.getMessage());
		
	}

	public static boolean isFontMimetype(String mime) {
        if (mime == null) return false;
        return (mime.startsWith("font/") || mime.startsWith("application/font") 
                || mime.startsWith("application/x-font") 
                || "application/vnd.ms-opentype".equals(mime));
    }

}

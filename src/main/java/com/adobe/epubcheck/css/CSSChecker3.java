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

import java.io.IOException;
import java.io.InputStream;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.PathUtil;
import com.phloc.commons.io.IInputStreamProvider;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSDeclaration;
import com.phloc.css.decl.CSSExpressionMemberTermSimple;
import com.phloc.css.decl.CSSExpressionMemberTermURI;
import com.phloc.css.decl.CSSFontFaceRule;
import com.phloc.css.decl.CSSImportRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSExpressionMember;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.decl.visit.CSSVisitor;
import com.phloc.css.decl.visit.DefaultCSSUrlVisitor;
import com.phloc.css.decl.visit.DefaultCSSVisitor;
import com.phloc.css.handler.ICSSParseExceptionHandler;
import com.phloc.css.parser.ParseException;
import com.phloc.css.reader.CSSReader;

public class CSSChecker3 implements ContentChecker {

	private OCFPackage ocf;
	private Report report;
	private String path;
	private XRefChecker xrefChecker;
	private EPUBVersion version;
	
	public CSSChecker3(OCFPackage ocf, Report report, String path,
			XRefChecker xrefChecker, EPUBVersion version) {
		this.ocf = ocf;
		this.report = report;
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.version = version;
	}

	public void runChecks() {
		
		try {
			if (!ocf.hasEntry(path)) {
				report.error(null, 0, 0,
						String.format(Messages.MISSING_FILE, path));
				return;
			}

			//phloc handles closing the returned inputstream
			IInputStreamProvider isp = new IInputStreamProvider() {
				@Override
				public InputStream getInputStream() {
					try {
						return ocf.getInputStream(path);
					} catch (IOException e) {	
						report.error(null, 0, 0,
								String.format(Messages.IO_ERROR, path));
						return null;
					}
				}
			};

			ICSSParseExceptionHandler err = new ICSSParseExceptionHandler() {
				@Override
				public void onException(ParseException e) {
					//phloc crashes out on namespace declarations
					//worth trying enabling the below once that is fixed
//					int line = -1;
//					int col = -1;
//					if(e.currentToken != null) {
//						line = e.currentToken.beginLine;
//						col = e.currentToken.beginColumn;
//					}
//					report.error(path, line, col, e.getMessage());
				}
			};

			final CascadingStyleSheet css = CSSReader.readFromStream(isp, "utf-8", ECSSVersion.CSS30, err);
									
			if(css == null) return;
						
			//get URLs
			CSSVisitor.visitCSSUrl(css, new DefaultCSSUrlVisitor() {
				@Override
				public void onImport(final CSSImportRule rule) {					
					handleRef(rule.getLocationString());					
				}
				
				@Override
				public void onUrlDeclaration(
						final ICSSTopLevelRule aTopLevelRule,
						final CSSDeclaration aDeclaration,
						final CSSExpressionMemberTermURI term) {					
					handleRef(term.getURIString());					
				}
								
				private void handleRef(String ref) {	
					//System.out.println("css ref: " + ref);
					if(ref != null && ref.trim().length() > 0) {
						String resolved = PathUtil.resolveRelativeReference(
							path, ref, null);					
						xrefChecker.registerReference(path, -1, -1, resolved,
								XRefChecker.RT_GENERIC);
					} else {
						report.error(path, -1, -1, Messages.NULL_REF);
					}					
				}
			});
			
			//get misc info
			CSSVisitor.visitCSS(css, new DefaultCSSVisitor(){
				@Override
				public void onBeginFontFaceRule(CSSFontFaceRule fontFaceRule) {
					handleFontInfo(fontFaceRule);					
				}

				@Override
				public void onDeclaration(CSSDeclaration decl) {
					if(version == EPUBVersion.VERSION_3) {
						String prop = decl.getProperty();
						if(prop == null) return;
						if(prop.equalsIgnoreCase("position")) {
							String value = getFirstSimpleMember(decl);
							if(value!=null && value.equalsIgnoreCase("fixed")) {
								report.error(path, -1, -1,
										"The fixed value of the position property is not part of the EPUB 3 CSS Profile.");
							}
						} else if (prop.equalsIgnoreCase("direction") || prop.equalsIgnoreCase("unicode-bidi")) {
							report.error(path, -1, -1,
									"The direction and unicode-bidi properties must not be included in an EPUB Style Sheet.");
						}
					}					
				}
			});
			

		} catch (Exception e) {
			report.error(path, -1, 0, e.getMessage());
		} 

	}
	
	private void handleFontInfo(CSSFontFaceRule fontFaceRule) {
		String fontFamily = null;
		String fontStyle = null;
		String fontWeight = null;
		String fontUri = null;
		
		for(CSSDeclaration decl : fontFaceRule.getAllDeclarations()) {
			String prop = decl.getProperty();
			if(prop != null) {
				if(prop.equalsIgnoreCase("font-family")) {					
					fontFamily = getFirstSimpleMember(decl);
					//strip leading and trailing quotes
					fontFamily = fontFamily.replaceAll("^\"|^\'|\"$|\'$", "");
				} else if (prop.equalsIgnoreCase("font-style")) {					
					fontStyle =  getFirstSimpleMember(decl);
					//System.out.println(fontStyle);	
				} else if (prop.equalsIgnoreCase("font-weight")) {
					fontWeight =  getFirstSimpleMember(decl);
					//System.out.println(fontWeight);
				} else if (prop.equalsIgnoreCase("src")) {					
					 for (ICSSExpressionMember o : decl.getExpression().getAllMembers()) {
						 if(o instanceof CSSExpressionMemberTermURI) {
							 CSSExpressionMemberTermURI uri = (CSSExpressionMemberTermURI)o;
							 fontUri = uri.getURIString();
							 //System.out.println(fontUri);
						 }
					 }
				}
			}
			
		}
		
		if (fontFamily != null) {
	        if (fontUri != null  && !fontUri.startsWith("http")) {
	            report.info(path, FeatureEnum.FONT_EMBEDED, fontFamily + 
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
	}

	private String getFirstSimpleMember(CSSDeclaration decl) {
		CSSExpressionMemberTermSimple member = decl.getExpression().getAllSimpleMembers().get(0);
		return (member != null) ? member.getValue() : null;			
	}

}

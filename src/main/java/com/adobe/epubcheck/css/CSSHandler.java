package com.adobe.epubcheck.css;

import java.util.List;

import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssConstruct;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssGrammar.CssURI;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.PathUtil;
import com.google.common.base.CharMatcher;

public class CSSHandler implements CssContentHandler, CssErrorHandler {
	private final String path;
	private final XRefChecker xrefChecker;
	private final Report report;
	private final EPUBVersion version;
	private int lineOffset = 0; //append to line info from css parser
	private CharMatcher SPACE_AND_QUOTES = CharMatcher.anyOf(" \t\n\r\f\"'").precomputed();
			
	//vars for font-face info
	String fontFamily;
	String fontStyle;
	String fontWeight;
	String fontUri;
	boolean inFontFace = false;
	
	
	public CSSHandler(String path, XRefChecker xrefChecker, Report report,
			EPUBVersion version) {
		this.path = path;
		this.xrefChecker = xrefChecker;
		this.report = report;
		this.version = version;
	}

	@Override
	public void error(CssException e) throws CssException {
		report.warning(path, e.getLocation().getLine() + lineOffset, -1, e.getMessage());		
	}

	@Override
	public void startDocument() {
				
	}

	@Override
	public void endDocument() {
		
		
	}

	@Override
	public void startAtRule(CssAtRule atRule) {
		
		if(atRule.getName().get() == "@import") {
			CssConstruct uriOrString = atRule.getComponents().get(0);
			if(uriOrString != null) {
				int line = uriOrString.getLocation().getLine();
				int col = uriOrString.getLocation().getColumn();
				
				if(uriOrString.getType() == CssConstruct.Type.URI) {
					resolveAndRegister(((CssURI)uriOrString).toUriString(), line, col);
				} else if(uriOrString.getType() == CssConstruct.Type.STRING) {					
					String uri = CharMatcher.anyOf("\"'").trimFrom(uriOrString.toCssString());
					resolveAndRegister(uri, line, col);
				} else {
					//syntax error, url must be first parameter
				}
			}
		} else {
			//check generically for urls in other atrules
			registerURIs(atRule.getComponents(), 
					atRule.getLocation().getLine(), 
					atRule.getLocation().getColumn());
		}
		
		if(atRule.getName().get() == "@font-face") {
			inFontFace = true;
		}
		
	}

	@Override
	public void endAtRule(String name) {
		if(name == "@font-face") {
			inFontFace = false;
			handleFontFaceInfo();
		}
	}

	@Override
	public void selectors(List<CssSelector> selectors) {
		
	}

	@Override
	public void declaration(CssDeclaration declaration) {
		registerURIs(declaration.getComponents(), 
			declaration.getLocation().getLine(), 
			declaration.getLocation().getColumn());
		
		String propertyName = declaration.getName().get();
		if(propertyName == null) return;
		
		if(version == EPUBVersion.VERSION_3) {			
			int line = declaration.getLocation().getLine()+lineOffset;
			int col = declaration.getLocation().getColumn();
			
			if(propertyName == "position") {
				CssConstruct cns = declaration.getComponents().get(0);
				if(cns != null) {
					String value = cns.toCssString();
					if(value!=null && value.equalsIgnoreCase("fixed")) {
						report.warning(path, line , col, Messages.POSITION_FIXED);								
					}	
				}
				
			} else if (propertyName == "direction" || propertyName == "unicode-bidi") {
				report.error(path, line, col, String.format(Messages.CSS_PROPERTY_NOT_ALLOWED, propertyName));						
			}
		}
		
		if(inFontFace) {
			//collect for info 
			if(propertyName == "font-family") {
				CssConstruct cc = declaration.getComponents().get(0);
				if(cc != null) {
					fontFamily = SPACE_AND_QUOTES.trimFrom(cc.toCssString());
				}				
			} else if (propertyName == "font-style") {	
				CssConstruct cc = declaration.getComponents().get(0);
				fontStyle =  cc.toCssString();	
			} else if (propertyName == "font-weight") {
				CssConstruct cc = declaration.getComponents().get(0);
				fontWeight =  cc.toCssString();
			} else if (propertyName == "src") {			
				for(CssConstruct construct : declaration.getComponents()) {
					if(construct.getType() == CssConstruct.Type.URI) {
						fontUri = ((CssURI)construct).toUriString();	
						
						//check font mimetypes
						String fontMimeType = xrefChecker.getMimeType(fontUri);
						if(fontMimeType != null) {
							boolean blessed = true;
							if (version == EPUBVersion.VERSION_2) {	
								blessed = OPFChecker.isBlessedFontMimetype20(fontMimeType);
							} else if (version == EPUBVersion.VERSION_3) {
								blessed = OPFChecker30.isBlessedFontType(fontMimeType);
							}							
							if(!blessed) {
								report.warning(path, declaration.getLocation().getLine(), 
				                		   declaration.getLocation().getColumn(), 
				                		   String.format(Messages.CSS_FONT_MIMETYPE, 
				                				   fontUri, fontMimeType));
							}						
						} else {
							//errors sb reported elsewhere
						}
					}
				}				 
			}
		}
	}
	
	private void registerURIs(List<CssConstruct> constructs, int line, int col) {
		for(CssConstruct construct : constructs) {
			if(construct.getType() == CssConstruct.Type.URI) {
				resolveAndRegister(((CssURI) construct).toUriString(), line, col);																			
			}
		}		
	}

	private void resolveAndRegister(String relativeRef, int line, int col) {				
		if(relativeRef != null && relativeRef.trim().length() > 0) {
			String resolved = PathUtil.resolveRelativeReference(path, relativeRef, null);									
			xrefChecker.registerReference(path, line + lineOffset, col, resolved, XRefChecker.RT_GENERIC);			
		} else {
			report.error(path, line + lineOffset, col, Messages.NULL_REF);
		}

	}
	
	private void handleFontFaceInfo() {				
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
	}

	public void setLineOffset(int offset) {
		this.lineOffset = offset - 1;
		if(this.lineOffset < 0) this.lineOffset = 0;
	}
	
	
}

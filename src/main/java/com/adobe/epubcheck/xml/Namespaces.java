package com.adobe.epubcheck.xml;

import javax.xml.XMLConstants;

public class Namespaces {

	public static final String PACKAGE = "http://www.idpf.org/2007/opf"; //$NON-NLS-1$
	public static final String MEDIA_OVERLAYS = "http://www.w3.org/ns/SMIL"; //$NON-NLS-1$
	public static final String OPS = "http://www.idpf.org/2007/ops"; //$NON-NLS-1$
	public static final String CONTAINER = "urn:oasis:names:tc:opendocument:xmlns:container"; //$NON-NLS-1$
	public static final String XINCLUDE = "http://www.w3.org/2001/XInclude"; //$NON-NLS-1$
	public static final String XLINK = "http://www.w3.org/1999/xlink"; //$NON-NLS-1$
	public static final String XHTML = "http://www.w3.org/1999/xhtml"; //$NON-NLS-1$
	public static final String MATHML = "http://www.w3.org/1998/Math/MathML"; //$NON-NLS-1$
	public static final String SVG = "http://www.w3.org/2000/svg"; //$NON-NLS-1$
	public static final String ISOSCH = "http://purl.oclc.org/dsdl/schematron"; //$NON-NLS-1$
	public static final String SCH = "http://www.ascc.net/xml/schematron"; //$NON-NLS-1$
	public static final String NCX = "http://www.daisy.org/z3986/2005/ncx/"; //$NON-NLS-1$
	public static final String PLS = "http://www.w3.org/2005/01/pronunciation-lexicon"; //$NON-NLS-1$
	
	public static final String DSIG_DS = "http://www.w3.org/2000/09/xmldsig#"; //$NON-NLS-1$
	public static final String DSIG_DS11 = "http://www.w3.org/2009/xmldsig11#"; //$NON-NLS-1$
	public static final String DSIG_DSP = "http://www.w3.org/2009/xmldsig-properties"; //$NON-NLS-1$
	public static final String DSIG_EC = "http://www.w3.org/2001/10/xml-exc-c14n#"; //$NON-NLS-1$
	
	public static final String XENC = "http://www.w3.org/2001/04/xmlenc#"; //$NON-NLS-1$
	public static final String XENC11 = "http://www.w3.org/2009/xmlenc11#"; //$NON-NLS-1$
	
	public static final String XMLEVENTS = "http://www.w3.org/2001/xml-events"; //$NON-NLS-1$
	public static final String SSML = "http://www.w3.org/2001/10/synthesis"; //$NON-NLS-1$
	public static final String XML = XMLConstants.XML_NS_URI; //$NON-NLS-1$
	
	
	private Namespaces() {}
	
}

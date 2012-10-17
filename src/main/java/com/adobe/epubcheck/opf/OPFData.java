package com.adobe.epubcheck.opf;

import com.adobe.epubcheck.util.EPUBVersion;

public interface OPFData
{
	
	static String OPF_MIME_TYPE = "application/oebps-package+xml";

    EPUBVersion getVersion();
    
    public String getUniqueIdentifier();
}

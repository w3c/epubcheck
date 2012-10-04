package com.adobe.epubcheck.opf;

import com.adobe.epubcheck.util.EPUBVersion;

public interface OPFData
{

    EPUBVersion getVersion();
    
    public String getUniqueIdentifier();
}

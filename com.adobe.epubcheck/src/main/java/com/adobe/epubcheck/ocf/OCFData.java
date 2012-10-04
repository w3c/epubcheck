package com.adobe.epubcheck.ocf;

import java.util.HashSet;

public interface OCFData
{
    static final String containerEntry = "META-INF/container.xml";

    static final String encryptionEntry = "META-INF/encryption.xml";

    static final String signatureEntry = "META-INF/signatures.xml";
    
    /**
     * @return the full-path of the last <root-file> element in the container
     * which has a media-type of "application/oebps-package+xml"
     * TODO: change all implementations to return the first oebps package, not the last.
     */
    public String getRootPath();

    /**
     * @return a set of all the full-paths listed as <rootfiles> in the container.
     */
    public HashSet<String> getContainerEntries();
}

package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OCFZipPackage extends OCFPackage {

	ZipFile zip;

	public OCFZipPackage(ZipFile zip) {
	    super();
		this.zip = zip;
	}

	/* (non-Javadoc)
     * @see com.adobe.epubcheck.ocf.OCFPackage#hasEntry(java.lang.String)
     */
	public boolean hasEntry(String name) {
		return zip.getEntry(name) != null;
	}

	/* (non-Javadoc)
	 * @see com.adobe.epubcheck.ocf.OCFPackage#getTimeEntry(java.lang.String)
	 */
	public long getTimeEntry(String name) {
	    ZipEntry entry = zip.getEntry(name);
        if (entry == null)
            return 0L;
        return entry.getTime();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.adobe.epubcheck.ocf.OCFPackage#getInputStream(java.lang.String)
	 */
	@Override
	public InputStream getInputStream(String name) throws IOException {
	        ZipEntry entry = zip.getEntry(name);
	        if (entry == null)
	            return null;
	        InputStream in = zip.getInputStream(entry);
	        EncryptionFilter filter = (EncryptionFilter) enc.get(name);
	        if (filter == null)
	            return in;
	        if (filter.canDecrypt())
	            return filter.decrypt(in);
	        return null;
	    }

	/* (non-Javadoc)
     * @see com.adobe.epubcheck.ocf.OCFPackage#getFileEntries()
     */
	@Override
	public HashSet<String> getFileEntries() throws IOException {
        HashSet<String> entryNames = new HashSet<String>();

        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (!entry.isDirectory()) {
                entryNames.add(entry.getName());
            }
        }

        return entryNames;
    }


	/* (non-Javadoc)
     * @see com.adobe.epubcheck.ocf.OCFPackage#getDirectoryEntries()
     */
	@Override
	public HashSet<String> getDirectoryEntries() throws IOException  {
        HashSet<String> entryNames = new HashSet<String>();

        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                entryNames.add(entry.getName());
            }
        }
        return entryNames;
    }

}

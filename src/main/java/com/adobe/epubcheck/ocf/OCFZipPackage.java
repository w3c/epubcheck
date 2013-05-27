package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OCFZipPackage extends OCFPackage {

	private ZipFile zip;
	private List<String> allEntries = null;
	private Set<String> fileEntries;
	private Set<String> dirEntries;

	public OCFZipPackage(ZipFile zip) {
	    super();
		this.zip = zip;
	}
	
	private void listEntries() throws IOException {
		synchronized (zip) {
			allEntries = new LinkedList<String>();
			fileEntries = new HashSet<String>();
			dirEntries = new HashSet<String>();
			for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
				ZipEntry entry = entries.nextElement();
				allEntries.add(entry.getName());
				if (entry.isDirectory()) {
					dirEntries.add(entry.getName());
				} else {
					fileEntries.add(entry.getName());
				}
			}
		}
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
	
	@Override
	public List<String> getEntries() throws IOException {
		synchronized (zip) {
			if (allEntries==null) listEntries();
		}
		return Collections.unmodifiableList(allEntries);
	}

	/* (non-Javadoc)
     * @see com.adobe.epubcheck.ocf.OCFPackage#getFileEntries()
     */
	@Override
	public Set<String> getFileEntries() throws IOException {
		synchronized (zip) {
			if (allEntries==null) listEntries();
		}
		return Collections.unmodifiableSet(fileEntries);
    }


	/* (non-Javadoc)
     * @see com.adobe.epubcheck.ocf.OCFPackage#getDirectoryEntries()
     */
	@Override
	public Set<String> getDirectoryEntries() throws IOException  {
		synchronized (zip) {
			if (allEntries==null) listEntries();
		}
		return Collections.unmodifiableSet(dirEntries);
    }

}

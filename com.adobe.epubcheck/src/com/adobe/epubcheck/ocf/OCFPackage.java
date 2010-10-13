package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OCFPackage {

        ZipFile zip;
        Hashtable enc;
        String uniqueIdentifier;

        public OCFPackage(ZipFile zip) {
                this.zip = zip;
                this.enc = new Hashtable();
        }

        public void setEncryption(String name, EncryptionFilter encryptionFilter) {
                enc.put(name, encryptionFilter);
        }

        public void setUniqueIdentifier(String idval) {
                uniqueIdentifier = idval;
        }

        public String getUniqueIdentifier() {
                return uniqueIdentifier;
        }

        public boolean hasEntry(String name) {
                return zip.getEntry(name) != null;
        }

        public boolean canDecrypt(String name) {
                EncryptionFilter filter = (EncryptionFilter) enc.get(name);
                if (filter == null)
                        return true;
                return filter.canDecrypt();
        }

        public InputStream getInputStream(String name) throws IOException {
                ZipEntry entry = zip.getEntry(name);
                if (entry == null)
                        return null;
                InputStream in = zip.getInputStream(entry);
                EncryptionFilter filter = (EncryptionFilter) enc.get(name);
                if (filter == null)
                        return in;
                if( filter.canDecrypt() )
                        return filter.decrypt(in);
                return null;
        }

        public HashSet getFileEntries() throws IOException {
            HashSet entryNames = new HashSet();

            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if (! entry.isDirectory()) {
                    entryNames.add(entry.getName());
                }
            }

            return entryNames;
        }

        public HashSet getDirectoryEntries() throws IOException {
            HashSet entryNames = new HashSet();

            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if (entry.isDirectory()) {
                    entryNames.add(entry.getName());
                }
            }

            return entryNames;
        }
}

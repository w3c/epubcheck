package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import com.adobe.epubcheck.util.EPUBVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
abstract class TextSearch {
    private final Hashtable<String, EncryptionFilter> enc;
    final ZipFile zip;
    final Report report;
    final EPUBVersion version;


    public TextSearch(EPUBVersion version, ZipFile zip, Report report)
    {
        this.zip = zip;
        this.enc = new Hashtable<String, EncryptionFilter>();
        this.report = report;
        this.version = version;
    }

    InputStream getInputStream(String name) throws IOException
    {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null)
        {
            return null;
        }
        InputStream in = zip.getInputStream(entry);
        EncryptionFilter filter = enc.get(name);
        if (filter == null)
        {
            return in;
        }
        if (filter.canDecrypt())
        {
            return filter.decrypt(in);
        }
        return null;
    }

    abstract Vector<String> Search(String entry);
}

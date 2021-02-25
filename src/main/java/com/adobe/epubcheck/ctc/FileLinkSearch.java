package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.EPUBVersion;

import java.io.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class FileLinkSearch extends TextSearch {
    private static final Pattern fileLinkPattern = Pattern.compile("<a\\s([^<>]*\\s)?href=[\"']file://");

    public FileLinkSearch(EPUBVersion version, ZipFile zip, Report report)
    {
        super(version, zip, report);
    }

    @Override
    Vector<String> Search(String entry)
    {
        String fileName = new File(zip.getName()).getName();
        InputStream is = null;
        BufferedReader br = null;
        try
        {
            is = getInputStream(entry);
            br = new BufferedReader(new InputStreamReader(is));

            int lineCounter = 1;
            String line;
            while ((line = br.readLine()) != null)
            {
                Matcher matcher = fileLinkPattern.matcher(line);
                int position = 0;
                while (matcher.find(position))
                {
                    int contextStart = Math.max(0, matcher.start() - 20);
                    int contextEnd = Math.min(contextStart + 40, line.length() - 1);
                    String context = line.substring(contextStart, contextEnd);

                    report.message(MessageId.HTM_053, EPUBLocation.create(entry, lineCounter, matcher.start(), context.trim()), context.trim());
                    position = matcher.end();
                }
                lineCounter++;
            }
        }
        catch (FileNotFoundException e1)
        {
            report.message(MessageId.RSC_001, EPUBLocation.create(fileName), entry);
        }
        catch (IOException e1)
        {
            report.message(MessageId.PKG_008, EPUBLocation.create(fileName), entry);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            report.message(MessageId.RSC_005, EPUBLocation.create(entry), e.getMessage());
        }
        finally
        {
            silentlyClose(br);
            silentlyClose(is);
        }
        return new Vector<String>();
    }

    private void silentlyClose(Closeable c) {
        try
        {
            c.close();
        }
        catch (IOException ignored)
        {
        }
    }
}

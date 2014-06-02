package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.TextSearchDictionaryEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class DictionarySearch
{
  private final ZipFile zip;
  private final Hashtable<String, EncryptionFilter> enc;
  private final Report report;

  public DictionarySearch(ZipFile zip, Report report)
  {
    this.zip = zip;
    this.enc = new Hashtable<String, EncryptionFilter>();
    this.report = report;
  }

  InputStream getInputStream(String name) throws
      IOException
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

  public Vector<String> find(String entry, SearchDictionary tds)
  {
    Vector<String> result = new Vector<String>();
    InputStream is = null;
    try
    {
      is = getInputStream(entry);
      Scanner in = new Scanner(is);
      int lineCounter = 1;

      while (in.hasNextLine())
      {
        String line = in.nextLine();

        for (int i = 0; i < tds.getDictEntries().size(); i++)
        {
          TextSearchDictionaryEntry de = tds.getDictEntries().get(i);
          Pattern p = de.getPattern();
          Matcher matcher = p.matcher(line);
          int position = 0;
          while (matcher.find(position))
          {
            MessageId messageCode = de.getErrorCode();
            position = matcher.end();
            String matchedText = line.substring(matcher.start(), matcher.end());

            if (!matchesException(matchedText,  messageCode, tds))
            {
              int contextStart = Math.max(0, matcher.start() - 20);
              int contextEnd = Math.min(contextStart + 40, line.length() - 1);
              String context = line.substring(contextStart, contextEnd);
              report.message(messageCode, new MessageLocation(entry, lineCounter, matcher.start(), context.trim()));
            }
          }
        }
        lineCounter++;
      }
    }
    catch (FileNotFoundException e1)
    {
      String fileName = new File(zip.getName()).getName();
      report.message(MessageId.RSC_001, new MessageLocation(fileName, -1, -1), entry);
    }
    catch (IOException e1)
    {
      String fileName = new File(zip.getName()).getName();
      report.message(MessageId.PKG_008, new MessageLocation(fileName, -1, -1), entry);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      report.message(MessageId.RSC_005, new MessageLocation(entry, -1, -1), e.getMessage());
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception ignored)
        {
        }
      }
    }
    return result;
  }

  boolean matchesException(String matchedText, MessageId messageId, SearchDictionary tds)
  {
    for (int s = 0; s < tds.getExceptionEntries().size(); s++)
    {
      TextSearchDictionaryEntry de = tds.getExceptionEntries().get(s);
      if (de.getErrorCode() == messageId)
      {
        Pattern p = de.getPattern();
        Matcher matcher = p.matcher(matchedText);
        if (matcher.matches())
        {
          return true;
        }
      }
    }
    return false;
  }
}

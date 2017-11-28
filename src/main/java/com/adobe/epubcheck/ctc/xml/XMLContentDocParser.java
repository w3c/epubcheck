package com.adobe.epubcheck.ctc.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.EncryptionFilter;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class XMLContentDocParser
{
  private final ZipFile zip;
  private final Hashtable<String, EncryptionFilter> enc;
  private final Report report;

  public XMLContentDocParser(ZipFile zip, Report report)
  {
    this.zip = zip;
    this.enc = new Hashtable<String, EncryptionFilter>();
    this.report = report;
  }

  public void parseDoc(String fileEntry, DefaultHandler handler)
  {
    InputStream is = null;
    try
    {
      is = getInputStream(fileEntry);

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      //factory.setValidating(false);
      //factory.setFeature("resolve-dtd-uris", false);


      SAXParser saxParser = factory.newSAXParser();
      final XMLReader parser = saxParser.getXMLReader();
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setDTDHandler(handler);
      saxParser.parse(is, handler);

    }
    catch (FileNotFoundException e)
    {
      String message = e.getMessage();
      message = new File(message).getName();
      int p = message.indexOf("(");
      if (p > 0)
      {
        message = message.substring(0, message.indexOf("("));
      }
      message = message.trim();
      report.message(MessageId.RSC_001, EPUBLocation.create(fileEntry), message);
    }
    catch (IOException e)
    {
      // Ignore, should have been reported earlier
      // report.message(MessageId.PKG_008, EPUBLocation.create(fileEntry),
      // fileEntry);
    }
    catch (SAXException e)
    {
      // Ignore, should have been reported earlier
      // report.message(MessageId.RSC_005, EPUBLocation.create(fileEntry),
      // e.getMessage());
    }
    catch (ParserConfigurationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception ignore)
        {
        }
      }
    }

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
}

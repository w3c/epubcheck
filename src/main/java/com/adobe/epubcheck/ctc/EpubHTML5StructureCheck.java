package com.adobe.epubcheck.ctc;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.epubpackage.SpineItem;
import com.adobe.epubcheck.ctc.xml.HTMLTagsAnalyseHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubHTML5StructureCheck implements DocumentValidator
{
  static final int hasHtml = 1;
  static final int hasPublic = 2;
  static final int hasW3C = 4;
  static final int hasXhtml = 8;
  static final int hasHTML5 = hasHtml;
  static final int hasHTML4 = hasPublic | hasW3C | hasXhtml;

  final ZipFile zip;
  final Report report;
  final EpubPackage epubPackage;
  final Hashtable<String, EncryptionFilter> enc;

  public EpubHTML5StructureCheck(EpubPackage epack, Report report)
  {
    this.zip = epack.getZip();
    this.report = report;
    this.epubPackage = epack;
    this.enc = new Hashtable<String, EncryptionFilter>();
  }

  @Override
  public boolean validate()
  {
    boolean result = false;
    SearchDictionary vtsd = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);
    int landmarkNavCount = 0;
    boolean isGlobalFixed = EpubPackage.isGlobalFixed(this.epubPackage);

    Hashtable<String, SpineItem> spineItems = new Hashtable<String, SpineItem>();
    for (int i = 0; i < epubPackage.getSpine().itemsLength(); ++i)
    {
      SpineItem si = epubPackage.getSpine().getItem(i);
      spineItems.put(si.getIdref(), si);
    }

    for (int i = 0; i < epubPackage.getManifest().itemsLength(); i++)
    {
      ManifestItem mi = epubPackage.getManifest().getItem(i);
      if (vtsd.isValidMediaType(mi.getMediaType()))
      {
        XMLContentDocParser parser = new XMLContentDocParser(epubPackage.getZip(), report);
        HTMLTagsAnalyseHandler sh = new HTMLTagsAnalyseHandler();
        sh.setReport(report);
        SpineItem si = spineItems.get(mi.getId());
        boolean itemIsFixedFormat = isGlobalFixed;
        if (si != null)
        {

          String properties = si.getProperties();
          if (properties != null)
          {
            if (properties != null && !properties.equals(""))
            {
              properties = properties.replaceAll("[\\s]+", " ");
              String propertyArray[] = properties.split(" ");
              for (String prop : propertyArray)
              {
                if (prop.equals("rendition:layout-pre-paginated"))
                {
                  itemIsFixedFormat = true;
                }
                else if (prop.equals("rendition:layout-reflowable"))
                {
                  itemIsFixedFormat = false;
                }
              }
            }
          }
          sh.setIsFixed(itemIsFixedFormat);
        }

        String fileToParse;
        if (epubPackage.getPackageMainPath() != null && epubPackage.getPackageMainPath().length() > 0)
        {
          fileToParse = PathUtil.resolveRelativeReference(epubPackage.getPackageMainFile(), mi.getHref(), null);
        }
        else
        {
          fileToParse = mi.getHref();
        }

        ZipEntry entry = zip.getEntry(fileToParse);
        if (entry == null)
        {
          String fileName = new File(zip.getName()).getName();
          report.message(MessageId.RSC_001, new MessageLocation(fileName, -1, -1), fileToParse);
          continue;
        }

        sh.setFileName(fileToParse);
        //parser.parseDoc(fileToParse, sh);
        /***VALIDATE FILE EXTENSION***/

        String fileExtension = mi.getHref().substring(mi.getHref().lastIndexOf('.') + 1, mi.getHref().length());
        if (!(fileExtension.compareToIgnoreCase("html") == 0 || fileExtension.compareToIgnoreCase("htm") == 0 || fileExtension.compareToIgnoreCase("xhtml") == 0))
        {
          report.message(MessageId.HTM_014, new MessageLocation(mi.getHref(), -1, -1));
        }

        /***VALIDATE DOCTYPE***/
        int docTypeMatches = findMatchingDocumentTypePatterns(fileToParse);

        if ((0 != (docTypeMatches & hasHTML4)) && (epubPackage.getVersion() == EPUBVersion.VERSION_3))
        {
          report.message(MessageId.HTM_015, new MessageLocation(mi.getHref(), -1, -1));
        }
        else if ((0 != (docTypeMatches & hasHTML5)) && ((hasXhtml != (docTypeMatches & hasXhtml)))  &&  (epubPackage.getVersion() == EPUBVersion.VERSION_2))
        {
          report.message(MessageId.HTM_016, new MessageLocation(mi.getHref(), -1, -1));
        }
        parser.parseDoc(fileToParse, sh);

        if (sh.getHtml5SpecTagsCounter() > 0)
        {
          report.info(fileToParse, FeatureEnum.HAS_HTML5, "true");
          if (epubPackage.isSpineItem(mi.getId()))
          {
            // Report that there is HTML5 for the entire publication only if it is in a spine item.
            // This is used for the 'is backward compatible' check.
            // This is so the HTML5 (nav tag) in a toc document will be ignored for backwards compatibility testing.
            report.info(null, FeatureEnum.HAS_HTML5, "true");
          }
        }
        landmarkNavCount += sh.getLandmarkNavCount();
      }
    }
    if (landmarkNavCount != 1)
    {
      File zipFile = new File(zip.getName());
      report.message(MessageId.ACC_008, new MessageLocation(zipFile.getName(), -1, -1));
    }

    return result;
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

  static final Pattern patternDocTypeElement = Pattern.compile("<*!*[Dd][Oo][Cc][Tt][Yy][Pp][Ee]");
  static final Pattern patternHtmlElement = Pattern.compile("([^Xx][Hh][Tt][Mm][Ll])");
  static final Pattern patternPublicElement = Pattern.compile("[Pp][Uu][Bb][Ll][Ii][Cc]");
  static final Pattern patternW3CElement = Pattern.compile("[Ww][3][Cc]//[Dd][Tt][Dd]");
  static final Pattern patternXhtmlElement = Pattern.compile("[Xx][Hh][Tt][Mm][Ll]");

  int findMatchingDocumentTypePatterns(String entry)
  {
    InputStream is = null;
    int matchingPatterns = 0;
    try
    {
      is = getInputStream(entry);
      if (is == null)
      {
        throw new IOException("Input Stream not found: '" + entry + "'");
      }

      Scanner in = new Scanner(is);
      StringBuilder sb = new StringBuilder();
      int numBracketsToClose = 0;
      String line = null;

      // skip over every line until we find the !DOCTYPE
      while (in.hasNextLine())
      {
        line = in.nextLine();
        Matcher matcher = patternDocTypeElement.matcher(line);
        if (matcher.find())
        {
          numBracketsToClose = 1;
          int i = matcher.start();
          if (i >= 0)
          {
            // prime it here so we can enter the loop below
            sb.append("<");
            line = line.substring(i + 1);
            break;
          }
        }
      }

      // now start appending characters until we close all nested '<' with matching '>'
      while ((numBracketsToClose > 0) && (line != null))
      {
        int i = 0;
        while ((numBracketsToClose > 0) && (i < line.length()))
        {
          Character ch = line.charAt(i);
          if (ch == '<')
          {
            ++numBracketsToClose;
          }
          else if (ch == '>')
          {
            --numBracketsToClose;
          }
          sb.append(ch);
          ++i;
        }
        if (in.hasNextLine())
        {
          sb.append(" ");
          line = in.nextLine();
        }
        else
        {
          line = null;
        }
      }

      if (numBracketsToClose > 0)
      {
        // There's an error.  We ran out of characters before finding the matching '>'
        return -1;
      }

      line = sb.toString();

      matchingPatterns |= checkPattern(line, patternHtmlElement, hasHtml);
      matchingPatterns |= checkPattern(line, patternPublicElement, hasPublic);
      matchingPatterns |= checkPattern(line, patternW3CElement, hasW3C);
      matchingPatterns |= checkPattern(line, patternXhtmlElement, hasXhtml);
    }
    catch (IOException e1)
    {
      report.message(MessageId.RSC_005, new MessageLocation(entry, -1, -1), e1.getMessage());
    }
    catch (Exception e)
    {
      e.printStackTrace();
      report.message(MessageId.PKG_008, new MessageLocation(entry, -1, -1), e.getMessage());
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
    return matchingPatterns;
  }

  int checkPattern(String line, Pattern patternElement, int mask)
  {
    Matcher matcherElement = patternElement.matcher(line);
    return (matcherElement.find()) ? mask : 0;
  }
}

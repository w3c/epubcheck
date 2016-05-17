package com.adobe.epubcheck.ctc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.EncryptionFilter;
import com.adobe.epubcheck.util.EPUBVersion;

class EntitySearch
{
  private final ZipFile zip;
  private final Hashtable<String, EncryptionFilter> enc;
  private final Report report;
  private final EPUBVersion version;

  static final Pattern entityPattern = Pattern.compile("&([A-Za-z0-9]+)([;|\\s])");
  static final HashSet<String> legalEntities2_0;
  static final HashSet<String> legalEntities3_0;

  MessageId ENTITY_INVALID = MessageId.HTM_023;
  MessageId ENTITY_OK = MessageId.HTM_006;
  MessageId ENTITY_IMPROPER = MessageId.HTM_024;

  static
  {
    legalEntities3_0 = new HashSet<String>();
    Collections.addAll(legalEntities3_0, "&amp;", "&apos;", "&quot;", "&lt;", "&gt;");

    legalEntities2_0 = new HashSet<String>();
    Collections.addAll(legalEntities2_0, "&nbsp;", "&iexcl;", "&cent;", "&pound;", "&curren;",
        "&yen;", "&brvbar;", "&sect;", "&uml;", "&copy;", "&ordf;", "&laquo;", "&not;", "&shy;", "&reg;",
        "&macr;", "&deg;", "&plusmn;", "&sup2;", "&sup3;", "&acute;", "&micro;", "&para;", "&middot;", "&cedil;",
        "&sup1;", "&ordm;", "&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&Agrave;", "&Aacute;",
        "&Acirc;", "&Atilde;", "&Auml;", "&Aring;", "&AElig;", "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;",
        "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;", "&Ograve;", "&Oacute;", "&Ocirc;", "&Otilde;",
        "&Ouml;", "&times;", "&Oslash;", "&Ugrave;", "&Uacute;", "&Ucirc;", "&Uuml;", "&Yacute;", "&THORN;", "&szlig;",
        "&agrave;", "&aacute;", "&acirc;", "&atilde;", "&auml;", "&aring;", "&aelig;", "&ccedil;", "&egrave;", "&eacute;",
        "&ecirc;", "&euml;", "&igrave;", "&iacute;", "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;", "&oacute;",
        "&ocirc;", "&otilde;", "&ouml;", "&divide;", "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;", "&yacute;",
        "&thorn;", "&yuml;", "&OElig;", "&oelig;", "&Scaron;", "&scaron;", "&Yuml;", "&fnof;", "&circ;", "&tilde;",
        "&Alpha;", "&Beta;", "&Gamma;", "&Delta;", "&Epsilon;", "&Zeta;", "&Eta;", "&Theta;", "&Iota;", "&Kappa;",
        "&Lambda;", "&Mu;", "&Nu;", "&Xi;", "&Omicron;", "&Pi;", "&Rho;", "&Sigma;", "&Tau;", "&Upsilon;", "&Phi;", "&Chi;",
        "&Psi;", "&Omega;", "&alpha;", "&beta;", "&gamma;", "&delta;", "&epsilon;", "&zeta;", "&eta;", "&theta;", "&iota;",
        "&kappa;", "&lambda;", "&mu;", "&nu;", "&xi;", "&omicron;", "&pi;", "&rho;", "&sigmaf;", "&sigma;", "&tau;",
        "&upsilon;", "&phi;", "&chi;", "&psi;", "&omega;", "&thetasym;", "&upsih;", "&piv;", "&ensp;", "&emsp;", "&thinsp;",
        "&zwnj;", "&zwj;", "&lrm;", "&rlm;", "&ndash;", "&mdash;", "&lsquo;", "&rsquo;", "&sbquo;", "&ldquo;", "&rdquo;",
        "&bdquo;", "&dagger;", "&Dagger;", "&bull;", "&hellip;", "&permil;", "&prime;", "&Prime;", "&lsaquo;", "&rsaquo;",
        "&oline;", "&frasl;", "&euro;", "&image;", "&weierp;", "&real;", "&trade;", "&alefsym;", "&larr;", "&uarr;",
        "&rarr;", "&darr;", "&harr;", "&crarr;", "&lArr;", "&uArr;", "&rArr;", "&dArr;", "&hArr;", "&forall;", "&part;",
        "&exist;", "&empty;", "&nabla;", "&isin;", "&notin;", "&ni;", "&prod;", "&sum;", "&minus;", "&lowast;", "&radic;",
        "&prop;", "&infin;", "&ang;", "&and;", "&or;", "&cap;", "&cup;", "&int;", "&there4;", "&sim;", "&cong;", "&asymp;",
        "&ne;", "&equiv;", "&le;", "&ge;", "&sub;", "&sup;", "&nsub;", "&sube;", "&supe;", "&oplus;", "&otimes;", "&perp;",
        "&sdot;", "&vellip;", "&lceil;", "&rceil;", "&lfloor;", "&rfloor;", "&lang;", "&rang;", "&loz;", "&spades;", "&clubs;",
        "&hearts;", "&diams;");

  }

  public EntitySearch(EPUBVersion version, ZipFile zip, Report report)
  {
    this.zip = zip;
    this.enc = new Hashtable<String, EncryptionFilter>();
    this.report = report;
    this.version = version;
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

  public Vector<String> Search(String entry)
  {
    Vector<String> result = new Vector<String>();
    InputStream is = null;
    Scanner in = null;
    try
    {
      is = getInputStream(entry);
      in = new Scanner(is);
      int lineCounter = 1;

      while (in.hasNextLine())
      {
        String line = in.nextLine();
        Matcher matcher = entityPattern.matcher(line);
        int position = 0;

        while (matcher.find(position))
        {
          MessageId messageCode = ENTITY_INVALID;
          position = matcher.end();
          String matchedText = line.substring(matcher.start(), matcher.end());
          if (version == EPUBVersion.VERSION_2)
          {
            if (legalEntities3_0.contains(matchedText) || legalEntities2_0.contains(matchedText))
            {
              // its in either the legal 2.0 list or the 3.0 list. Simply emit a usage message
              messageCode = ENTITY_OK;
            }
          }
          else if (version == EPUBVersion.VERSION_3)
          {
            if (legalEntities3_0.contains(matchedText))
            {
              // its in the 3.0 list.  just emit a usage message
              messageCode = ENTITY_OK;
            }
            else if (legalEntities2_0.contains(matchedText))
            {
              // its in the 2.0 list.  Emit a usage message saying that only &amp; &apos; etc. are allowed
              messageCode = ENTITY_IMPROPER;
            }
          }

          int contextStart = Math.max(0, matcher.start() - 20);
          int contextEnd = Math.min(contextStart + 40, line.length() - 1);
          String context = line.substring(contextStart, contextEnd);

          if (messageCode == ENTITY_INVALID)
          {
            // emit the erroneous text along with the message
            report.message(messageCode, EPUBLocation.create(entry, lineCounter, matcher.start(), context.trim()), matchedText);
          }
          else
          {
            report.message(messageCode, EPUBLocation.create(entry, lineCounter, matcher.start(), context.trim()));
          }
        }
        lineCounter++;
      }
    }
    catch (FileNotFoundException e1)
    {
      String fileName = new File(zip.getName()).getName();
      report.message(MessageId.RSC_001, EPUBLocation.create(fileName), entry);
    }
    catch (IOException e1)
    {
      String fileName = new File(zip.getName()).getName();
      report.message(MessageId.PKG_008, EPUBLocation.create(fileName), entry);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      report.message(MessageId.RSC_005, EPUBLocation.create(entry), e.getMessage());
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
      if (in != null) {
	    in.close();
      }
    }
    return result;
  }
}

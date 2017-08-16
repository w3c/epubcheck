/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.adobe.epubcheck.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.ctc.CheckManager;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFChecker;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ocf.OCFZipPackage;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.util.ResourceUtil;
import com.adobe.epubcheck.util.WriterReportImpl;

/**
 * Public interface to epub validator.
 */
public class EpubCheck implements DocumentValidator
{
  private static String VERSION = null;
  private static String BUILD_DATE = null;
  final private File epubFile;
  final private EPUBProfile profile;
  final private Report report;

  public static String version()
  {
    if (VERSION == null)
    {
      Properties prop = new Properties();
      InputStream in = EpubCheck.class.getResourceAsStream("project.properties");
      try
      {
        prop.load(in);
      } catch (Exception e)
      {
        System.err.println("Couldn't read project properties: " + e.getMessage());
      } finally
      {
        if (in != null)
        {
          try
          {
            in.close();
          } catch (IOException ignored)
          {
          }
        }
      }
      VERSION = prop.getProperty("version");
      BUILD_DATE = prop.getProperty("buildDate");
    }
    return VERSION;
  }

  public static String buildDate()
  {
    return BUILD_DATE;
  }

  /**
   * Create an epub validator to validate the given file. Issues will be
   * reported to standard error.
   */
  public EpubCheck(File epubFile)
  {
    this(epubFile, new DefaultReportImpl(epubFile.getName()));
  }

  /**
   * Create an epub validator to validate the given file. Issues will be
   * reported to the given PrintWriter.
   */
  public EpubCheck(File epubFile, PrintWriter out)
  {
    this(epubFile, new WriterReportImpl(out));
  }

  /**
   * Create an epub validator to validate the given file and report issues to a
   * given Report object.
   */
  public EpubCheck(File epubFile, Report report)
  {
    this(epubFile, report, null);
  }

  /**
   * Create an epub validator to validate the given file and report issues to a
   * given Report object. Can validate a specific EPUB profile (e.g. EDUPUB,
   * DICT, IDX, etc).
   * 
   */
  public EpubCheck(File epubFile, Report report, EPUBProfile profile)
  {
    this.epubFile = epubFile;
    this.report = report;
    this.profile = profile == null ? EPUBProfile.DEFAULT : profile;
  }

  public EpubCheck(InputStream inputStream, Report report, String uri)
  {
    this(inputStream, report, uri, EPUBProfile.DEFAULT);
  }

  public EpubCheck(InputStream inputStream, Report report, String uri, EPUBProfile profile)
  {
    File epubFile;
    OutputStream out = null;
    try
    {
      epubFile = File.createTempFile("epub", "." + ResourceUtil.getExtension(uri));
      epubFile.deleteOnExit();
      out = new FileOutputStream(epubFile);

      byte[] bytes = new byte[1024];
      int read;
      while ((read = inputStream.read(bytes)) != -1)
      {
        out.write(bytes, 0, read);
      }

      this.epubFile = epubFile;
      this.profile = profile == null ? EPUBProfile.DEFAULT : profile;
      this.report = report;
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    } finally
    {
      if (inputStream != null)
      {
        try
        {
          inputStream.close();
        } catch (IOException ignored)
        {
        }
      }
      if (out != null)
      {
        try
        {
          out.flush();
          out.close();
        } catch (IOException ignored)
        {
        }
      }
    }
  }

  /**
   * Validate the file. Return true if no errors or warnings found.
   */
  public boolean validate()
  {
    int validateResult = doValidate();
    return validateResult == 0;
  }

  public int doValidate()
  {
    ZipFile zip = null;
    FileInputStream epubIn = null;
    try
    {
      String extension = ResourceUtil.getExtension(epubFile.getName());
      checkExtension(extension);

      if (!epubFile.exists())
      {
        report.message(MessageId.PKG_018, EPUBLocation.create(epubFile.getName()));
        return 2;
      }

      epubIn = new FileInputStream(epubFile);
      checkEpubHeader(epubIn);
      zip = new ZipFile(epubFile);

      OCFPackage ocf = new OCFZipPackage(zip);
      OCFChecker checker = new OCFChecker(new ValidationContextBuilder().ocf(ocf).report(report)
          .profile(profile).build());
      checker.runChecks();

      /*** Here are called custom checks (CTC Package) **/
      CheckManager c = new CheckManager(zip, report);
      c.checkPackage();
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.create(epubFile.getName(), ""),
          e.getMessage());
    } finally
    {
      try
      {
        if (epubIn != null)
        {
          epubIn.close();
        }
        if (zip != null)
        {
          zip.close();
        }
      } catch (IOException ignored)
      {
      }
    }

    int returnValue = 0;
    if (report.getFatalErrorCount() != 0) returnValue |= 4;
    if (report.getErrorCount() != 0) returnValue |= 2;
    if (report.getWarningCount() != 0) returnValue |= 1;
    return returnValue;
  }

  void checkExtension(String extension)
  {
    if (extension != null)
    {
      if (!extension.equals("epub"))
      {
        if (extension.matches("[Ee][Pp][Uu][Bb]"))
        {
          report.message(MessageId.PKG_016, EPUBLocation.create(epubFile.getName()));
        }
        else
        {
          report.message(MessageId.PKG_017, EPUBLocation.create(epubFile.getName(), extension));
        }
      }
    }
  }

  void checkEpubHeader(FileInputStream epubIn)
    throws IOException
  {
    byte[] header = new byte[58];

    int readCount = epubIn.read(header);
    if (readCount != -1)
    {
      while (readCount < header.length)
      {
        int read = epubIn.read(header, readCount, header.length - readCount);
        // break on eof
        if (read == -1)
        {
          break;
        }
        readCount += read;
      }
    }

    if (readCount != header.length)
    {
      report.message(MessageId.PKG_003, EPUBLocation.create(epubFile.getName(), ""));
    }
    else
    {
      int fnsize = getIntFromBytes(header, 26);
      int extsize = getIntFromBytes(header, 28);

      if (header[0] != 'P' && header[1] != 'K')
      {
        report.message(MessageId.PKG_004, EPUBLocation.create(epubFile.getName()));
      }
      else if (fnsize != 8)
      {
        report.message(MessageId.PKG_006, EPUBLocation.create(epubFile.getName()));
      }
      else if (extsize != 0)
      {
        report.message(MessageId.PKG_005, EPUBLocation.create(epubFile.getName()), extsize);
      }
      else if (!CheckUtil.checkString(header, 30, "mimetype"))
      {
        report.message(MessageId.PKG_006, EPUBLocation.create(epubFile.getName()));
      }
      else if (!CheckUtil.checkString(header, 38, "application/epub+zip"))
      {
        report.message(MessageId.PKG_007, EPUBLocation.create("mimetype"));
      }
    }
  }

  private int getIntFromBytes(byte[] bytes, int offset)
  {
    int hi = 0xFF & bytes[offset + 1];
    int lo = 0xFF & bytes[offset];
    return hi << 8 | lo;
  }
}

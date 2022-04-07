package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.CheckUtil;

public class OCFZipChecker implements Checker
{
  private final ValidationContext context;
  private final Report report;
  private final File zip;

  public OCFZipChecker(ValidationContext context)
  {
    // FIXME 2022 - check preconditions
    this.context = context;
    this.report = context.report;
    this.zip = new File(context.path);
  }

  @Override
  public void check()
  {
    FileInputStream epubIn = null;
    try
    {

      epubIn = new FileInputStream(zip);

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
        report.message(MessageId.PKG_003, EPUBLocation.of(context));
      }
      else
      {
        int fnsize = getIntFromBytes(header, 26);
        int extsize = getIntFromBytes(header, 28);

        if (header[0] != 'P' && header[1] != 'K')
        {
          report.message(MessageId.PKG_004, EPUBLocation.of(context));
        }
        else if (fnsize != 8)
        {
          report.message(MessageId.PKG_006, EPUBLocation.of(context));
        }
        else if (extsize != 0)
        {
          report.message(MessageId.PKG_005, EPUBLocation.of(context), extsize);
        }
        else if (!CheckUtil.checkString(header, 30, "mimetype"))
        {
          report.message(MessageId.PKG_006, EPUBLocation.of(context));
        }
        else if (!CheckUtil.checkString(header, 38, "application/epub+zip"))
        {
          // FIXME next get proper mimetype file location
          // FIXME next check mimetype file content in OCFChecker (not only ZIP)
//          report.message(MessageId.PKG_007, EPUBLocation.fromPath("mimetype"));
          report.message(MessageId.PKG_007, EPUBLocation.of(context));
        }
      }
    } catch (IOException e)
    {
      report.message(MessageId.PKG_008, EPUBLocation.of(context), e.getMessage());
    } finally
    {
      try
      {
        if (epubIn != null)
        {
          epubIn.close();
        }
      } catch (IOException ignored)
      {
      }
    }
  }

  private static int getIntFromBytes(byte[] bytes, int offset)
  {
    int hi = 0xFF & bytes[offset + 1];
    int lo = 0xFF & bytes[offset];
    return hi << 8 | lo;
  }

}

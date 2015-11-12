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
package com.adobe.epubcheck.util;

import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.Severity;

public class DefaultReportImpl extends MasterReport
{
  static boolean DEBUG = false;
  boolean quiet;
  boolean saveQuiet;

  public static String ePubVersion;

  public DefaultReportImpl(String ePubName)
  {
    this(ePubName, null, false);
  }

  public DefaultReportImpl(String ePubName, String info, boolean quiet)
  {
    this.quiet = quiet;
    String adjustedPath = PathUtil.removeWorkingDirectory(ePubName);
    this.setEpubFileName(adjustedPath);
    if (info != null)
    {
      //warning("", 0, 0, info);
    }
  }

  String fixMessage(String message)
  {
    if (message == null)
    {
      return "";
    }
    return message.replaceAll("[\\s]+", " ");
  }
  boolean pushQuiet()
  {
    saveQuiet = outWriter.isQuiet();
    outWriter.setQuiet(quiet);
    return saveQuiet;
  }

  void popQuiet()
  {
    outWriter.setQuiet(saveQuiet);
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {
    Severity severity = message.getSeverity();
    String text = formatMessage(message, location, args);
    if (severity.equals(Severity.USAGE))
    {
      pushQuiet();
      outWriter.println(text);
      popQuiet();
    }
    else
    {
      System.err.println(text);
    }
  }

  String formatMessage(Message message, EPUBLocation location, Object... args)
  {
    String epubFileName = PathUtil.removeWorkingDirectory(this.getEpubFileName());
    String fileName = PathUtil.removeWorkingDirectory(location.getPath());
    // remove duplicate epub name from path and empty fileName variable
    fileName = epubFileName.endsWith(fileName) ? "" : "/" + fileName;
    return String.format("%1$s(%2$s): %3$s%4$s(%5$s,%6$s): %7$s",
        message.getSeverity(),
        message.getID(),
        epubFileName,
        fileName,
        location.getLine(),
        location.getColumn(),
        fixMessage(args != null && args.length > 0 ? message.getMessage(args) : message.getMessage()));
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    if (ReportingLevel.Info >= getReportingLevel())
    {
       switch (feature)
      {
        case FORMAT_VERSION:
          if (!quiet)
          {
            outWriter.println(String.format(getMessages().get("validating_version_message"), value));
          }
          break;
        default:
          if (DEBUG && !quiet)
          {
            if (resource == null)
            {
              outWriter.println("INFO: [" + feature + "]=" + value);
            }
            else
            {
              outWriter.println("INFO: [" + feature + " (" +
                  resource + ")]=" + value);
            }
          }
          break;
      }
    }
  }
    
  public int generate()
  {
    return 0;
  }

  public void initialize()
  {
  }
}

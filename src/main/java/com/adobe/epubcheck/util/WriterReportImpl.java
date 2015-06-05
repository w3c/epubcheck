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

import java.io.PrintWriter;

public class WriterReportImpl extends MasterReport
{
  static boolean DEBUG = false;
	boolean quiet;
  final PrintWriter out;

  public WriterReportImpl(PrintWriter out)
  {
    this(out, "", false);
  }

  public WriterReportImpl(PrintWriter out, String info)
  {
    this(out, info, false);
	}
	
	public WriterReportImpl(PrintWriter out, String info, boolean quiet)
  {
    this.out = out;
    warning("", 0, 0, info);
		this.quiet = quiet;
  }

  String fixMessage(String message)
  {
	if (message == null) return "";
    return message.replaceAll("[\\s]+", " ");
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {
    if (message.getSeverity().equals(Severity.ERROR))
    {
      error(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args));
    }
    else if (message.getSeverity().equals(Severity.WARNING))
    {
      warning(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args));
    }
    else if (message.getSeverity().equals(Severity.FATAL))
    {
      fatalError(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args));
    }
  }

  void error(String resource, int line, int column, String message)
  {
    message = fixMessage(message);
    out.println("ERROR: "
        + (resource == null ? "[top level]" : resource)
        + (line <= 0 ? "" : "(" + line
        + (column <= 0 ? "" : "," + column) + ")") + ": "
        + message);
  }

  void fatalError(String resource, int line, int column, String message)
  {
    message = fixMessage(message);
    out.println("ERROR: "
        + (resource == null ? "[top level]" : resource)
        + (line <= 0 ? "" : "(" + line
        + (column <= 0 ? "" : "," + column) + ")") + ": "
        + message);
  }

  void warning(String resource, int line, int column, String message)
  {
    message = fixMessage(message);
    out.println("WARNING: "
        + (resource == null ? "[top level]" : resource)
        + (line <= 0 ? "" : "(" + line
        + (column <= 0 ? "" : "," + column) + ")") + ": "
        + message);
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    if (ReportingLevel.Info >= getReportingLevel())
    {
      switch (feature)
      {
        case FORMAT_VERSION:
          if (DEBUG && !quiet)
          {
            outWriter.println(String.format(Messages.get("validating_version_message"), value));
          }
          break;
        default:
          if (!quiet)
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

  public void initialize()
  {
  }
	
	public void hint(String resource, int line, int column, String message)
  {
		if (!quiet)
    {
          out.println("HINT: " + (resource == null ? "[top level]" : resource)
          + (line <= 0 ? "" : "(" + line
              + (column <= 0 ? "" : "," + column) + ")") + ": "
          + message);
      }

	}

  public int generate()
  {
    out.flush();
    return 0;
  }
}

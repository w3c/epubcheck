/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;

public class ValidationReport extends MasterReport
{
  String info = "";
  public ArrayList<ItemReport> errorList, warningList, exceptionList, infoList, fatalErrorList, hintList, usageList;
  public String fileName;

  public class ItemReport
  {
		public final String resource;
		public final int line;
		public final int column;
		public final String message;
    MessageId id;

    public ItemReport(String resource, int line, int column, String message, MessageId id)
    {
      this.resource = resource;
      this.line = line;
      this.column = column;
      this.message = message;
      this.id = id;
    }
  }

  public ValidationReport(String file)
  {
    fileName = PathUtil.removeWorkingDirectory(file);
    errorList = new ArrayList<ItemReport>();
    warningList = new ArrayList<ItemReport>();
    exceptionList = new ArrayList<ItemReport>();
    infoList = new ArrayList<ItemReport>();
    usageList = new ArrayList<ItemReport>();
    fatalErrorList = new ArrayList<ItemReport>();
    hintList = new ArrayList<ItemReport>();
  }

  public ValidationReport(String file, String info)
  {
    fileName = file;
    if (!info.equals(""))
    {
      info = info + "\n";
    }
    this.info = info;
    errorList = new ArrayList<ItemReport>();
    warningList = new ArrayList<ItemReport>();
    exceptionList = new ArrayList<ItemReport>();
    infoList = new ArrayList<ItemReport>();
    fatalErrorList = new ArrayList<ItemReport>();
    hintList = new ArrayList<ItemReport>();
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {
    if (message.getSeverity().equals(Severity.ERROR))
    {
      error(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args), message.getID());
    }
    else if (message.getSeverity().equals(Severity.WARNING))
    {
      warning(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args), message.getID());
    }
    else if (message.getSeverity().equals(Severity.FATAL))
    {
      fatalError(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args), message.getID());
    }
    else if (message.getSeverity().equals(Severity.INFO))
    {
      info(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args), message.getID());
    }
    else if (message.getSeverity().equals(Severity.USAGE))
    {
      usage(PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(), location.getColumn(), message.getMessage(args), message.getID());
    }
  }

  private void error(String resource, int line, int column, String message, MessageId id)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), id);
    errorList.add(item);
  }

  private void fatalError(String resource, int line, int column, String message, MessageId id)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), id);
    fatalErrorList.add(item);
  }

  private void warning(String resource, int line, int column, String message, MessageId id)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), id);
    warningList.add(item);
  }

  public void info(String resource, int line, int column, String message, MessageId id)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), id);
    getInfoList().add(item);
  }

  public void usage(String resource, int line, int column, String message, MessageId id)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), id);
    usageList.add(item);
  }

  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append(fileName);
    buffer.append(": ");
    buffer.append(info);

    buffer.append("Errors: ");
    buffer.append(this.getErrorCount());

    buffer.append("; Warnings: ");
    buffer.append(this.getWarningCount());

    buffer.append("\n");


    for (ItemReport exception : exceptionList)
    {
      buffer.append("FATAL: ");
      buffer.append(fileName);
      buffer.append(exception.resource != null ? ":" + exception.resource : "");
      buffer.append(exception.message);
      buffer.append("\n");
        for (int i = 0; i < hintList.size(); i++) {
            ItemReport item = (ItemReport) hintList.get(i);
            buffer.append("HINT: " + fileName
                    + (item.resource != null ? ":" + item.resource : "")
                    + item.message + "\n");
        }
    }

    for (ItemReport error : errorList)
    {
      buffer.append("ERROR: ");
      buffer.append(fileName);
      buffer.append(error.resource != null ? ":" + error.resource : "");
      buffer.append(error.line > 0 ? "(" + error.line + (error.column > 0 ? "," + error.column : "") + ")" : "");
      buffer.append(": ");
      buffer.append(error.message);
      buffer.append("\n");
    }

    for (ItemReport warning : warningList)
    {
      buffer.append("WARNING: ");
      buffer.append(fileName);
      buffer.append(warning.resource != null ? ":" + warning.resource : "");
      buffer.append(warning.line > 0 ? "(" + warning.line + (warning.column > 0 ? "," + warning.column : "") + ")" : "");
      buffer.append(": ");
      buffer.append(warning.message);
      buffer.append("\n");
    }

    for (ItemReport info : getInfoList())
    {
      buffer.append("INFO: ");
      buffer.append(fileName);
      buffer.append(info.resource != null ? ":" + info.resource : "");
      buffer.append(info.message);
      buffer.append("\n");
    }
    return buffer.toString();
  }

  private String fixMessage(String message)
  {
    if (message == null)
    {
      return "No message";
    }
    return message.replaceAll("[\\s]+", " ");
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    ItemReport item = new ItemReport(resource, 0, 0, fixMessage("[" + feature + "] " + value), null);
    getInfoList().add(item);
  }

  public void hint(String resource, int line, int column, String message)
  {
    ItemReport item = new ItemReport(resource, line, column, fixMessage(message), null);
    hintList.add(item);
  }
    
  public ArrayList<ItemReport> getInfoList()
  {
    return infoList;
  }

  public boolean hasInfoMessage(String msg)
  {
    for (ItemReport it : getInfoList())
    {
      if (it.message.equals(msg))
      {
        return true;
      }
    }
    return false;
  }

  public void initialize()
  {
  }

  public int generate()
  {
    return 0;
  }

  public List<MessageId> getUsageIds()
  {
    List<MessageId> result = new ArrayList<MessageId>();
    for (ItemReport it : usageList)
    {
      if(it.id != null) {
        result.add(it.id);
      }
    }
    return result;
  }

  public List<MessageId> getInfoIds()
  {
    List<MessageId> result = new ArrayList<MessageId>();
    for (ItemReport it : infoList)
    {
      if(it.id != null) {
        result.add(it.id);
      }
    }
    return result;
  }

  public List<MessageId> getWarningIds()
  {
    List<MessageId> result = new ArrayList<MessageId>();
    for (ItemReport it : warningList)
    {
      result.add(it.id);
    }
    return result;
  }

  public List<MessageId> getErrorIds()
  {
    List<MessageId> result = new ArrayList<MessageId>();
    for (ItemReport it : errorList)
    {
      result.add(it.id);
    }
    return result;
  }

  public List<MessageId> getFatalErrorIds()
  {
    List<MessageId> result = new ArrayList<MessageId>();
    for (ItemReport it : fatalErrorList)
    {
      result.add(it.id);
    }
    return result;
  }

}

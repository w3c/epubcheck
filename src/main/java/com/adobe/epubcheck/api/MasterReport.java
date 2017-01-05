package com.adobe.epubcheck.api;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.ReportingLevel;

/**
 * Reports are derived from this so that we can test for message Id coverage as well as have a centralized location for
 * severity reporting level testing.
 */
public abstract class MasterReport implements Report
{
  public static Set<MessageId> allReportedMessageIds = new HashSet<MessageId>();
  int errorCount, warningCount, fatalErrorCount, usageCount, infoCount = 0;
  int reportingLevel = ReportingLevel.Info;
  private String ePubName;
  private MessageDictionary dictionary = new MessageDictionary(null, this);

  @Override
  public MessageDictionary getDictionary()
  {
    return dictionary;
  }

  protected MasterReport()
  {
  }

  @Override
  public void setOverrideFile(File overrideFile)
  {
    getDictionary().setOverrideFile(overrideFile);
  }

  @JsonProperty
  String customMessageFileName = null;

  private void reportMessageId(MessageId id)
  {
    allReportedMessageIds.add(id);
  }

  @Override
  public void message(MessageId id, EPUBLocation location, Object... args)
  {
    Message message = getDictionary().getMessage(id);
    assert (message != null);
    Severity severity = message.getSeverity();
    if (ReportingLevel.getReportingLevel(severity) >= getReportingLevel())
    {
      if (severity.equals(Severity.ERROR))
      {
        errorCount++;
      }
      else if (severity.equals(Severity.WARNING))
      {
        warningCount++;
      }
      else if (severity.equals(Severity.FATAL))
      {
        fatalErrorCount++;
      }
      else if (severity.equals(Severity.USAGE))
      {
        usageCount++;
      }
      else if (severity.equals(Severity.INFO))
      {
        infoCount++;
      }
      this.message(message, location, args);
    }
    reportMessageId(id);
  }
  
  @Override
  public void setCustomMessageFile(String customMessageFileName)
  {
    this.customMessageFileName = customMessageFileName;
  }

  @Override
  public String getCustomMessageFile()
  {
    return this.customMessageFileName;
  }

  @Override
  public int getReportingLevel()
  {
    return reportingLevel;
  }

  @Override
  public void setReportingLevel(int reportingLevel)
  {
    if (reportingLevel >= ReportingLevel.Usage && reportingLevel <= ReportingLevel.Fatal)
    {
      this.reportingLevel = reportingLevel;
    }
    else
    {
      System.err.printf("Attempted to set invalid reporting level: %1d", reportingLevel);
    }
  }

  @Override
  public String getEpubFileName()
  {
    return this.ePubName;
  }

  @Override
  public void setEpubFileName(String value)
  {
    this.ePubName = value;
  }

  @Override
  public int getErrorCount()
  {
    return errorCount;
  }

  @Override
  public int getWarningCount()
  {
    return warningCount;
  }

  @Override
  public int getFatalErrorCount()
  {
    return fatalErrorCount;
  }

  @Override
  public int getInfoCount()
  {
    return infoCount;
  }

  @Override
  public int getUsageCount()
  {
    return usageCount;
  }

  @Override
  public void close()
  {
  }
}

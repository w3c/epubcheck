package com.adobe.epubcheck.api;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.LocaleHolder;
import com.adobe.epubcheck.messages.LocalizedMessageDictionary;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.OverriddenMessageDictionary;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.ReportingLevel;
import java.util.Locale;

/**
 * Reports are derived from this so that we can test for message Id coverage as
 * well as have a centralized location for severity reporting level testing.
 */
public abstract class MasterReport implements LocalizableReport
{
  public static Set<MessageId> allReportedMessageIds = new HashSet<MessageId>();
  private int errorCount, warningCount, fatalErrorCount, usageCount, infoCount = 0;
  private int reportingLevel = ReportingLevel.Info;
  private String ePubName;
  private MessageDictionary dictionary = new LocalizedMessageDictionary();
  private Messages messages;

  @Override
  public MessageDictionary getDictionary()
  {
    return dictionary;
  }

  /**
   * Creates a report with a new {@code Messages} instance and sets the locale
   * held in {@code LocaleHolder} to the default locale.
   */
  protected MasterReport()
  {
    this(true);
  }
  
  /**
   * Creates a report with a new {@code Messages} instance and sets the locale
   * held in {@code LocaleHolder} to the default locale only if the given flag is
   * <code>true</code>.
   * 
   * @param setLocale
   *          whether to update the locale held in {@code LocaleHolder}
   */
  protected MasterReport(boolean setLocale)
  {
      messages = Messages.getInstance();
    if (setLocale)
    {
      LocaleHolder.set(Locale.getDefault());
    }
  }

  @Override
  public void setLocale(Locale locale)
  {
      dictionary = new LocalizedMessageDictionary(locale);
      messages = Messages.getInstance(locale);
      // Note: we also store the locale statically (thread local) for libraries
      // which are not locale-context aware (like Jing).
      LocaleHolder.set(locale);
  }
  
  @Override
  public Locale getLocale()
  {
    return messages.getLocale();
  }
  
  public Messages getMessages()
  {
      return messages;
  }
  
  @Override
  public void setOverrideFile(File overrideFile)
  {
    dictionary = new OverriddenMessageDictionary(overrideFile, this);
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

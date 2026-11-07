package com.adobe.epubcheck.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.LocalizableReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;

/**
 * A {@link Report} that buffers messages and features until {@link #flush()}
 * is called.
 */
public final class BufferedReport implements LocalizableReport
{

  private final Report delegate;
  private final List<Runnable> pending = new ArrayList<>();

  public BufferedReport(Report delegate)
  {
    this.delegate = delegate;
  }

  /**
   * Replays the buffered calls on the wrapped report.
   */
  public void flush()
  {
    for (Runnable call : pending)
    {
      call.run();
    }
    pending.clear();
  }

  @Override
  public void message(final MessageId id, final EPUBLocation location, final Object... args)
  {
    pending.add(new Runnable()
    {
      @Override
      public void run()
      {
        delegate.message(id, location, args);
      }
    });
  }

  @Override
  public void info(final String resource, final FeatureEnum feature, final String value)
  {
    pending.add(new Runnable()
    {
      @Override
      public void run()
      {
        delegate.info(resource, feature, value);
      }
    });
  }

  @Override
  public int getErrorCount()
  {
    return delegate.getErrorCount();
  }

  @Override
  public int getWarningCount()
  {
    return delegate.getWarningCount();
  }

  @Override
  public int getFatalErrorCount()
  {
    return delegate.getFatalErrorCount();
  }

  @Override
  public int getInfoCount()
  {
    return delegate.getInfoCount();
  }

  @Override
  public int getUsageCount()
  {
    return delegate.getUsageCount();
  }

  @Override
  public int generate()
  {
    return delegate.generate();
  }

  @Override
  public void initialize()
  {
    delegate.initialize();
  }

  @Override
  public void setEpubFileName(String value)
  {
    delegate.setEpubFileName(value);
  }

  @Override
  public String getEpubFileName()
  {
    return delegate.getEpubFileName();
  }

  @Override
  public void setCustomMessageFile(String customMessageFileName)
  {
    delegate.setCustomMessageFile(customMessageFileName);
  }

  @Override
  public String getCustomMessageFile()
  {
    return delegate.getCustomMessageFile();
  }

  @Override
  public int getReportingLevel()
  {
    return delegate.getReportingLevel();
  }

  @Override
  public void setReportingLevel(int level)
  {
    delegate.setReportingLevel(level);
  }

  @Override
  public int getMaxOfEachMessage()
  {
    return delegate.getMaxOfEachMessage();
  }

  @Override
  public void setMaxOfEachMessage(int max)
  {
    delegate.setMaxOfEachMessage(max);
  }

  @Override
  public void close()
  {
    delegate.close();
  }

  @Override
  public void setLocale(Locale locale)
  {
    if (delegate instanceof LocalizableReport)
    {
      ((LocalizableReport) delegate).setLocale(locale);
    }
  }

  @Override
  public Locale getLocale()
  {
    return (delegate instanceof LocalizableReport)
        ? ((LocalizableReport) delegate).getLocale()
        : Locale.getDefault();
  }

  @Override
  public void setOverrideFile(File customMessageFile)
  {
    delegate.setOverrideFile(customMessageFile);
  }

  @Override
  public MessageDictionary getDictionary()
  {
    return delegate.getDictionary();
  }
}

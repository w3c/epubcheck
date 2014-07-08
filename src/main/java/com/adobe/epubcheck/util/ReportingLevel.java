package com.adobe.epubcheck.util;

import com.adobe.epubcheck.messages.Severity;

public class ReportingLevel
{
  static public final int Fatal = 5;
  static public final int Error = 4;
  static public final int Warning = 3;
  static public final int Info = 2;
  static public final int Usage = 1;
  static public final int Suppressed = 0;

  public static int getReportingLevel(Severity severity)
  {
    if (severity == Severity.FATAL)
    {
      return Fatal;
    }
    else if (severity == Severity.ERROR)
    {
      return Error;
    }
    else if (severity == Severity.WARNING)
    {
      return Warning;
    }
    else if (severity == Severity.INFO)
    {
      return Info;
    }
    else if (severity == Severity.USAGE)
    {
      return Usage;
    }
    else if (severity == Severity.SUPPRESSED)
    {
      return Suppressed;
    }
    else
    {
      return -1;
    }
  }
}

package com.adobe.epubcheck.util;

public class outWriter
{
  static boolean isQuiet = false;
  public outWriter(){}

  public static void setQuiet(boolean isQuiet)
  {
    outWriter.isQuiet = isQuiet;
  }

  public static boolean isQuiet()
  {
    return isQuiet;
  }

  public static void printf(String format, Object...args)
  {
    if (!isQuiet())
    {
      System.out.printf(format, args);
    }
  }
  public static void println(Object x)
  {
    if (!isQuiet())
    {
      System.out.println(x);
    }
  }
  public static void println(String x)
  {
    if (!isQuiet())
    {
      System.out.println(x);
    }
  }

  public static void println()
  {
    System.out.println();
  }

  public static void print(String s)
  {
    System.out.print(s);
  }

  public static void flush()
  {
    System.out.flush();
  }
}


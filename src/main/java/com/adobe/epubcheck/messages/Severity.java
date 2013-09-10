package com.adobe.epubcheck.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum Severity
{
  SUPPRESSED("SUPPRESSED"),
  USAGE("USAGE"),
  INFO("INFO"),
  WARNING("WARNING"),
  ERROR("ERROR"),
  FATAL("FATAL");

  private final String name;

  Severity(String feature)
  {
    this.name = feature;
  }

  public String toString()
  {
    return name;
  }

  private static final Map<String, Severity> map = new HashMap<String, Severity>();

  static
  {
    for (Severity type : Severity.values())
    {
      map.put(type.name, type);
    }
  }

  public static Severity fromString(String name)
  {
    if (map.containsKey(name))
    {
      return map.get(name);
    }
    throw new NoSuchElementException(name + " not found");
  }

  public int toInt()
  {
    if (this.equals(SUPPRESSED)) return 0;
    if (this.equals(USAGE)) return 1;
    if (this.equals(INFO)) return 2;
    if (this.equals(WARNING)) return 3;
    if (this.equals(ERROR)) return 4;
    if (this.equals(FATAL)) return 5;
    return -1;
  }
}


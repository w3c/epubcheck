package com.adobe.epubcheck.api;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.adobe.epubcheck.util.JsonWriter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public final class EPUBLocation implements Comparable<EPUBLocation>
{

  public static EPUBLocation create(String fileName)
  {
    return new EPUBLocation(fileName, -1, -1, null);
  }

  public static EPUBLocation create(String fileName, String context)
  {
    return new EPUBLocation(fileName, -1, -1, context);
  }

  public static EPUBLocation create(String fileName, int lineNumber, int column)
  {
    return new EPUBLocation(fileName, lineNumber, column, null);
  }

  public static EPUBLocation create(String fileName, int lineNumber, int column, String context)
  {
    return new EPUBLocation(fileName, lineNumber, column, context);
  }

  @JsonProperty
  private final String path;
  @JsonProperty
  private final int line;
  @JsonProperty
  private final int column;
  @JsonProperty
  @JsonSerialize(using = JsonWriter.OptionalJsonSerializer.class)
  private final Optional<String> context;

  private EPUBLocation(String path, int lineNumber, int column, String context)
  {
    Preconditions.checkNotNull(path);
    this.path = path;
    this.line = lineNumber;
    this.column = column;
    this.context = Optional.fromNullable(context);
  }

  public String getPath()
  {
    return this.path;
  }

  public int getLine()
  {
    return this.line;
  }

  public int getColumn()
  {
    return this.column;
  }

  public Optional<String> getContext()
  {
    return this.context;
  }
  
  

  @Override
  public String toString()
  {
    return path + "[" + line + "," + column + "]";
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass())
    {
      return false;
    }

    EPUBLocation other = (EPUBLocation) obj;
    return !(this.getContext() == null && other.getContext() != null)
        && this.getPath().equals(other.getPath()) && this.getLine() == other.getLine()
        && this.getColumn() == other.getColumn()
        && (this.getContext() == null || this.getContext().equals(other.getContext()));
  }

  int safeCompare(String a, String b)
  {
    if (a == null && b != null) return -1;
    if (a != null && b == null) return 1;
    if (a == null /* && b == null */) return 0;
    return a.compareTo(b);
  }

  @Override
  public int compareTo(EPUBLocation o)
  {
    int comp = safeCompare(this.path, o.path);
    if (comp != 0)
    {
      return comp;
    }

    comp = line - o.line;
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }

    comp = column - o.column;
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }
    comp = safeCompare(context.orNull(), o.context.orNull());
    if (comp != 0)
    {
      return comp;
    }

    return 0;
  }
}

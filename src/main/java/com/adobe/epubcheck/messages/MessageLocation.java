package com.adobe.epubcheck.messages;

import org.codehaus.jackson.annotate.JsonProperty;

public class MessageLocation implements Comparable<MessageLocation>
{
  @JsonProperty
  private String fileName;
  @JsonProperty
  private final int line;
  @JsonProperty
  private final int column;
  @JsonProperty
  private final String context;

  public MessageLocation(String fileName, int lineNumber, int column)
  {
    this(fileName, lineNumber, column, null);
  }

  public MessageLocation(String fileName, int lineNumber, int column, String context)
  {
    this.fileName = fileName;
    this.line = lineNumber;
    this.column = column;
    this.context = context;
  }

  public String getFileName()
  {
    return this.fileName == null ? "" : this.fileName;
  }

  public void setFileName(String value)
  {
    this.fileName = value;
  }

  public int getLine()
  {
    return this.line;
  }

  public int getColumn()
  {
    return this.column;
  }

  public String getContext()
  {
    return this.context;
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

    MessageLocation other = (MessageLocation) obj;
    return !(this.getContext() == null && other.getContext() != null) && this.getFileName().equals(other.getFileName()) && this.getLine() == other.getLine() && this.getColumn() == other.getColumn() && (this.getContext() == null || this.getContext().equals(other.getContext()));
  }

  int safeCompare(String a, String b)
  {
    if (a == null && b != null) return -1;
    if (a != null && b == null) return 1;
    if (a == null /* && b == null */) return 0;
    return a.compareTo(b);
  }


  @Override
  public int compareTo(MessageLocation o)
  {
    int comp = safeCompare(this.fileName, o.fileName);
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
    comp = safeCompare(context, o.context);
    if (comp != 0)
    {
      return comp;
    }

    return 0;
  }
}

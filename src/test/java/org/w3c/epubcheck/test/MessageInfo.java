package org.w3c.epubcheck.test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;

public class MessageInfo
{
  private final MessageId id;
  private final Severity severity;
  private final String path;
  private final int line;
  private final int column;
  private final String message;

  public MessageInfo(MessageId id, String message)
  {
    this(null, id, null, -1, -1, message);
  }

  public MessageInfo(Severity severity, MessageId id, String path, int line, int column,
      String message)
  {
    this.severity = severity;
    this.id = id;
    this.path = path;
    this.line = line;
    this.column = column;
    this.message = message;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(severity.name())
    .append(' ').append(id)    
    .append(": ")
    .append(message);
    if (path != null) sb.append(" | in ").append(path);
    if (line > 0)
    {
      sb.append('(').append(line);
      if (column > 0) sb.append(',').append(column);
      sb.append(')');
    }
    return sb.toString();
  }

  public MessageId getId()
  {
    return id;
  }

  public Severity getSeverity()
  {
    return severity;
  }

  public String getMessage()
  {
    return message;
  }

}
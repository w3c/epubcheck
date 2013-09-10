package com.adobe.epubcheck.messages;

import java.util.IllegalFormatException;

/**
 * This is information associated with a check message.
 */
public class Message
{
  private final MessageId ID;
  private final Severity severity;
  private final Severity originalSeverity;
  private final String message;
  private final String suggestion;

  public Message(MessageId messageId, Severity severity, String message, String suggestion)
  {
    this.ID = messageId;
    this.originalSeverity = this.severity = severity;
    this.message = message;
    this.suggestion = suggestion;
  }

  public Message(MessageId messageId, Severity severity, Severity originalSeverity, String message, String suggestion)
  {
    this.ID = messageId;
    this.severity = severity;
    this.originalSeverity = originalSeverity;
    this.message = message;
    this.suggestion = suggestion;
  }

  public MessageId getID()
  {
    return this.ID;
  }

  public Severity getSeverity()
  {
    return this.severity;
  }

  public Severity getOriginalSeverity()
  {
    return this.originalSeverity;
  }

  public String getSuggestion()
  {
    return this.suggestion;
  }

  public String getMessage(Object... args)
  {
    String result = this.getMessage();
    try
    {
      result = String.format(result, args);
    }
    catch (IllegalFormatException e)
    {
      result = this.getMessage() + " :" + e.getMessage();
    }
    return result;
  }

  public String getMessage()
  {
    return this.message;
  }
}

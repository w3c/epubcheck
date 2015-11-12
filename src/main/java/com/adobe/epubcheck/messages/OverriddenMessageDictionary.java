package com.adobe.epubcheck.messages;

import com.adobe.epubcheck.api.Report;
import java.io.File;

/**
 * Maps a message to a severity using overrides provided in a file. Falls back
 * to default messages and severities when an override isn't available.
 */
public class OverriddenMessageDictionary implements MessageDictionary
{
  private final OverriddenMessages messages;

  public OverriddenMessageDictionary(File overrideFile, Report report )
  {
    messages = new OverriddenMessages(overrideFile, report);
  }
  
  @Override
  public Message getMessage(MessageId id)
  {
     Message message = messages.getMessage(id);
     if( message == null ) 
     {
       // Failure to find the message is a programmer error.
       throw new IllegalArgumentException(String.format("MessageId %s is not valid.", id.name()));
     }
     
     return message;
  }
  
}

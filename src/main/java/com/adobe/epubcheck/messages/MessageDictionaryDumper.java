package com.adobe.epubcheck.messages;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Helper class to handle file output of a MessageDictionary.
 */
public class MessageDictionaryDumper
{
  private final MessageDictionary dictionary;
  
  public MessageDictionaryDumper(MessageDictionary dictionary)
  {
    this.dictionary = dictionary;
  }
  
  public void dump(OutputStreamWriter outputStream) throws IOException
  {
    // Output the messages in a tab separated format
    outputStream.write("ID\tSeverity\tMessage\tSuggestion\n");
    for (MessageId id : MessageId.values())
    {
      StringBuilder sb = new StringBuilder();
      sb.append(id.toString());
      sb.append("\t");
      Message message = dictionary.getMessage(id);
      if (message != null)
      {
        sb.append(message.getSeverity());
        sb.append("\t");
        sb.append(message.getMessage());
        sb.append("\t");
        sb.append(message.getSuggestion());
      }
      else
      {
        sb.append("null\tnull\tnull\tnull");
      }
      sb.append("\n");
      outputStream.write(sb.toString());
    }
  }
}

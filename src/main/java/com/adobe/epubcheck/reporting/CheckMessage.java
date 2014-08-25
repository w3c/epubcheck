package com.adobe.epubcheck.reporting;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.messages.Severity;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class CheckMessage implements Comparable<CheckMessage>
{
  private static final int MAX_LOCATIONS = 25;
  @JsonProperty
  private final String ID;
  @JsonProperty
  private final Severity severity;
  @JsonProperty
  private String message;
  @JsonProperty
  private int additionalLocations = 0;
  @JsonProperty
  private final List<MessageLocation> locations = new ArrayList<MessageLocation>();
  @JsonProperty
  private final String suggestion;

  private CheckMessage(Message message, MessageLocation location, Object... args)
  {
    this.ID = message.getID().toString();
    this.message = message.getMessage(args);
    this.locations.add(location);
    this.severity = message.getSeverity();
    this.suggestion = ("".equals(message.getSuggestion())) ? null : message.getSuggestion();
  }

  public static CheckMessage addCheckMessage(List<CheckMessage> checkMessages, Message message, MessageLocation location, Object... args)
  {
    CheckMessage result = findCheckMessage(checkMessages, message.getID().toString(), message.getMessage(args));
    if (result == null)
    {
      result = new CheckMessage(message, location, args);
      checkMessages.add(result);
    }
    else
    {
      result.addLocation(location);
    }
    return result;
  }

  void addLocation(MessageLocation location)
  {
    if (this.findLocation(location) == null)
    {
      if (this.locations.size() == CheckMessage.MAX_LOCATIONS)
      {
        ++additionalLocations;
        this.locations.add(new MessageLocation("There is 1 additional location for this message.", -1, -1));
      }
      else if (this.locations.size() < CheckMessage.MAX_LOCATIONS)
      {
        this.locations.add(location);
      }
      else
      {
        ++additionalLocations;
        MessageLocation infoLocation = this.locations.get(this.locations.size() - 1);
        infoLocation.setFileName(String.format("There are %1$s additional locations for this message.", additionalLocations));
      }
    }
  }

  private static CheckMessage findCheckMessage(List<CheckMessage> checkMessages, String id, String text)
  {
    for (CheckMessage message : checkMessages)
    {
      if (message.ID.equals(id))
      {
        if (message.message.equals(text))
        {
          return message;
        }
      }
    }
    return null;
  }

  public Severity getSeverity()
  {
    return this.severity;
  }

  private MessageLocation findLocation(MessageLocation location)
  {
    for (MessageLocation l : this.locations)
    {
      if (l.equals(location))
      {
        return l;
      }
    }
    return null;
  }

  public String toString()
  {
    MessageLocation location = this.locations.get(this.locations.size() - 1);
    String lineSeparator = System.getProperty("line.separator");
    String text;
    text = "ID: " + ID + lineSeparator
        + "SEVERITY: " + (severity != null ? severity : "-UNDEFINED-")
        + lineSeparator
        + lineSeparator
        + "ERRONEOUS FILE NAME: " + location.getFileName()
        + lineSeparator;
    if (location.getLine() > 0
        && location.getColumn() > 0)
    {
      text += "LINE NUMBER: " + location.getLine() + lineSeparator;
    }
    text += "COLUMN NUMBER: " + location.getColumn() + lineSeparator;
    text += "DESCRIPTION (long): " + this.message + lineSeparator;
    text += "=========================================================================================================================="
        + lineSeparator;

    return text;
  }

  int safeCompare(String a, String b)
  {
    if (a == null && b != null) return -1;
    if (a != null && b == null) return 1;
    if (a == null /* && b == null */) return 0;
    return a.compareTo(b);
  }

  @Override
  public int compareTo(CheckMessage o)
  {
    int comp = safeCompare(this.ID, o.ID);
    if (comp != 0)
    {
      return comp;
    }

    comp = severity.toInt() - o.severity.toInt();
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }

    comp = safeCompare(message, o.message);
    if (comp != 0)
    {
      return comp;
    }

    comp = safeCompare(suggestion, o.suggestion);
    if (comp != 0)
    {
      return comp;
    }

    comp = additionalLocations - o.additionalLocations;
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }

    comp = (locations.size() - o.locations.size());
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }

    return 0;
  }

  public void sortLocations()
  {
    Collections.sort(locations);
  }

public String getID() {
	return ID;
}

public String getMessage() {
	return message;
}

public int getAdditionalLocations() {
	return additionalLocations;
}

public List<MessageLocation> getLocations() {
	return locations;
}

public String getSuggestion() {
	return suggestion;
}
}

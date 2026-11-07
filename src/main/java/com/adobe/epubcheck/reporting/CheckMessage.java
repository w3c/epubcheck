package com.adobe.epubcheck.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckMessage implements Comparable<CheckMessage>
{

  public static CheckMessage addCheckMessage(List<CheckMessage> messages, int maxCount,
      Message message, EPUBLocation location, Object... args)
  {
    return addCheckMessage(messages, null, maxCount, message, location, args);
  }

  /**
   * Adds a message to the given list, or a location to the matching message
   * found in the (optional) index.
   */
  public static CheckMessage addCheckMessage(List<CheckMessage> messages,
      Map<String, CheckMessage> index, int maxCount, Message message, EPUBLocation location,
      Object... args)
  {
    // format only once
    String messageString = message.getMessage(args);
    String key = (index != null) ? message.getID() + "\u0000" + messageString : null;

    CheckMessage found = (index != null) ? index.get(key) : null;
    if (found == null && index == null)
    {
      for (CheckMessage candidate : messages)
      {
        if (candidate.getID().equals(message.getID().toString())
            && candidate.getMessage().equals(messageString))
        {
          found = candidate;
          break;
        }
      }
    }

    if (found != null)
    {
      return found.addLocation(location);
    }

    CheckMessage newCheckMessage = new CheckMessage(message, messageString, maxCount)
        .addLocation(location);
    messages.add(newCheckMessage);
    if (index != null)
    {
      index.put(key, newCheckMessage);
    }
    return newCheckMessage;
  }


  @JsonProperty
  private final String ID;
  @JsonProperty
  private final Severity severity;
  @JsonProperty
  private String message;
  @JsonProperty
  private int additionalLocations = 0;
  @JsonProperty
  private final List<EPUBLocation> locations = new ArrayList<EPUBLocation>();
  @JsonProperty
  private final String suggestion;

  // The maximum number of locations to report
  private final int maxCount;

  private CheckMessage(Message message, String messageString, int maxCount)
  {
    this.ID = message.getID().toString();
    this.message = messageString;
    this.severity = message.getSeverity();
    this.suggestion = ("".equals(message.getSuggestion())) ? null : message.getSuggestion();
    this.maxCount = maxCount;
  }

  private CheckMessage addLocation(EPUBLocation location)
  {
    if (!locations.contains(location)) {
      if (maxCount < 0 || locations.size() < maxCount) {
        locations.add(location);
      } else {
        additionalLocations++;
      }
    }
    return this;
  }

  public Severity getSeverity()
  {
    return this.severity;
  }

  public String toString()
  {
    EPUBLocation location = this.locations.get(this.locations.size() - 1);
    String lineSeparator = System.getProperty("line.separator");
    String text;
    text = "ID: " + ID + lineSeparator
        + "SEVERITY: " + (severity != null ? severity : "-UNDEFINED-")
        + lineSeparator
        + lineSeparator
        + "ERRONEOUS FILE NAME: " + location.getPath()
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

  public String getID()
  {
    return ID;
  }

  public String getMessage()
  {
    return message;
  }

  public int getAdditionalLocations()
  {
    return additionalLocations;
  }

  public List<EPUBLocation> getLocations()
  {
    return locations;
  }

  public String getSuggestion()
  {
    return suggestion;
  }
}

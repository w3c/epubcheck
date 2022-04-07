package com.adobe.epubcheck.messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.PathUtil;

/**
 * Loads a list of messages from an override file and manages logic to choose
 * between an override or default message based on which is available.
 */
public class OverriddenMessages
{

  private final DefaultSeverities defaultSeverities = new DefaultSeverities();
  private final Map<MessageId, Message> overridenMessages = new EnumMap<MessageId, Message>(
      MessageId.class);
  // We could provide other localizations here as well, but it's probably better
  // to keep this simple.
  private final LocalizedMessages defaultMessages = LocalizedMessages.getInstance();
  private final Pattern parameterPattern = Pattern.compile("%(\\d+)\\$s");
  private final File overrideFile;
  private final Report report;

  public OverriddenMessages(File overrideFile, Report report)
  {
    this.overrideFile = overrideFile;
    this.report = report;
    loadOverriddenMessageSeverities();
  }

  public Message getMessage(MessageId id)
  {
    // First, check for an overridden message
    Message m = overridenMessages.get(id);
    if (m == null)
    {
      // If not overridden, fall back to the default
      m = defaultMessages.getMessage(id);

      if (m == null)
      {
        // Indicates a programmer error
        throw new IllegalArgumentException("MessageId " + id.name() + " is invalid.");
      }
    }
    return m;
  }

  private void loadOverriddenMessageSeverities()
  {
    // Method lifted directly from the old MessageDictionary class. I've avoided
    // making any changes, but this method deserves a refactor. -mm
    if (overrideFile != null)
    {
      int lineNumber = -1;
      int columnNumber = -1;
      String line;

      FileInputStream fis = null;
      BufferedReader br = null;
      try
      {
        fis = new FileInputStream(overrideFile);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        lineNumber = 1;

        while (null != (line = br.readLine()))
        {
          if (1 == lineNumber)
          {
            if (line.toLowerCase(Locale.ROOT).startsWith("id"))
            {
              // optionally eat the first line
              continue;
            }
          }
          columnNumber = 0;
          String[] fields = line.split("\t");
          if (fields.length >= 2)
          {
            MessageId id;
            try
            {
              id = MessageId.fromString(fields[0]);
            } catch (NoSuchElementException unused)
            {
              report.message(MessageId.CHK_002, EPUBLocation.of(overrideFile).at(lineNumber, 0),
                  fields[0], PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
              continue;
            }

            Severity newSeverity;

            try
            {
              columnNumber += 1 + fields[0].length();
              newSeverity = Severity.fromString(fields[1]);
            } catch (NoSuchElementException ignored)
            {
              report.message(MessageId.CHK_003,
                  EPUBLocation.of(overrideFile).at(lineNumber, columnNumber), fields[1],
                  PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
              continue;
            }

            Message message = defaultMessages.getMessage(id);
            String messageText = message.getMessage();
            if (fields.length >= 3 && fields[2] != null && fields[2].length() > 0)
            {
              columnNumber += 1 + fields[1].length();
              messageText = checkMessageForParameterCount(lineNumber, columnNumber,
                  message.getMessage(), fields[2]);
              if (messageText == null)
              {
                report.message(MessageId.CHK_004,
                    EPUBLocation.of(overrideFile).at(lineNumber, 0).context(fields[2]),
                    PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
                continue;
              }
            }
            if (messageText != null)
            {
              Severity oldSeverity = defaultSeverities.get(message.getID());
              if (newSeverity != oldSeverity)
              {
                messageText = String.format(" (severity overridden from %1$s) %2$s", oldSeverity,
                    messageText);
              }
            }

            String suggestionText = message.getSuggestion();
            if (fields.length >= 4 && fields[3] != null && fields[3].length() > 0)
            {
              columnNumber += 1 + fields[1].length();
              suggestionText = checkMessageForParameterCount(lineNumber, columnNumber,
                  message.getSuggestion(), fields[3]);
              if (suggestionText == null)
              {
                report.message(MessageId.CHK_005,
                    EPUBLocation.of(overrideFile).at(lineNumber, 0).context(fields[3]),
                    PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
                continue;
              }
            }

            if (message != null && ((newSeverity != message.getSeverity())
                || (messageText.compareTo(message.getMessage()) != 0)
                || (suggestionText.compareTo(message.getSuggestion()) != 0)))
            {
              overridenMessages.put(id, new Message(message.getID(), newSeverity,
                  message.getSeverity(), messageText, suggestionText));
            }
          }
          ++lineNumber;
        }
      } catch (FileNotFoundException fnf)
      {
        report.message(MessageId.CHK_001, EPUBLocation.of(overrideFile));
      } catch (IOException ex)
      {
        report.message(MessageId.CHK_007,
            EPUBLocation.of(overrideFile).at(lineNumber, columnNumber),
            PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()), ex.getMessage());
      } finally
      {
        try
        {
          if (br != null)
          {
            br.close();
          }
          if (fis != null)
          {
            fis.close();
          }
        } catch (IOException ignored)
        {
        }
      }
    }
  }

  private String checkMessageForParameterCount(int lineNumber, int columnNumber,
      String originalText, String newText)
  {
    if (newText != null)
    {
      int maxOriginal = getParameterCount(lineNumber, columnNumber, originalText);
      int maxNew = getParameterCount(lineNumber, columnNumber, newText);

      if (maxNew <= maxOriginal)
      {
        return newText;
      }
      return null;
    }
    return originalText;
  }

  private int getParameterCount(int lineNumber, int columnNumber, String text)
  {
    int max = 0;
    {
      Matcher m = parameterPattern.matcher(text);
      while (m.find())
      {
        int absoluteColumnNumber = columnNumber + m.start();
        String s = m.group(1);
        try
        {
          Integer number = Integer.parseInt(s);
          if (number > max)
          {
            max = number;
          }
        } catch (NumberFormatException ex)
        {
          String pathAdjustedFileName = PathUtil
              .removeWorkingDirectory(overrideFile.getAbsolutePath());
          report.message(MessageId.CHK_006,
              EPUBLocation.of(overrideFile).at(lineNumber, absoluteColumnNumber).context(text),
              pathAdjustedFileName);
        }
      }
    }
    return max;
  }

}

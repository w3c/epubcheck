package com.adobe.epubcheck.messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.outWriter;
import com.google.common.base.Charsets;

/**
 * This is a dictionary that maps the text of a message to a severity.
 */
public class MessageDictionary
{
  File overrideFile;
  Report report;
  static Map<MessageId, Severity> defaultSeverityMap = null;
  static Pattern parameterPattern = Pattern.compile("%(\\d+)\\$s");

  public void setOverrideFile(File value)
  {
    overrideFile = value;
    initMessageMap();
  }

  public MessageDictionary(File overrideFile, Report report)
  {
    this.report = report;
    this.overrideFile = overrideFile;
    initMessageMap();
  }

  Map<MessageId, Message> messages = new HashMap<MessageId, Message>();
  static final ResourceBundle labels = ResourceBundle.getBundle(
      "com.adobe.epubcheck.messages.MessageBundle", Locale.getDefault(), new UTF8Control());

  public Message getMessage(MessageId id)
  {
    return this.messages.get(id);
  }

  static Map<MessageId, Severity> getDefaultSeverities()
  {
    if (defaultSeverityMap == null)
    {
      Map<MessageId, Severity> map = new HashMap<MessageId, Severity>(MessageId.values().length);

      // Accessibility
      map.put(MessageId.ACC_001, Severity.USAGE);
      map.put(MessageId.ACC_002, Severity.USAGE);
      map.put(MessageId.ACC_003, Severity.SUPPRESSED);
      map.put(MessageId.ACC_004, Severity.SUPPRESSED);
      map.put(MessageId.ACC_005, Severity.SUPPRESSED);
      map.put(MessageId.ACC_006, Severity.SUPPRESSED);
      map.put(MessageId.ACC_007, Severity.USAGE);
      map.put(MessageId.ACC_008, Severity.USAGE);
      map.put(MessageId.ACC_009, Severity.WARNING);
      map.put(MessageId.ACC_010, Severity.SUPPRESSED);
      map.put(MessageId.ACC_011, Severity.WARNING);
      map.put(MessageId.ACC_012, Severity.SUPPRESSED);
      map.put(MessageId.ACC_013, Severity.USAGE);
      map.put(MessageId.ACC_014, Severity.USAGE);
      map.put(MessageId.ACC_015, Severity.USAGE);
      map.put(MessageId.ACC_016, Severity.USAGE);
      map.put(MessageId.ACC_017, Severity.USAGE);

      // CHK
      map.put(MessageId.CHK_001, Severity.ERROR);
      map.put(MessageId.CHK_002, Severity.ERROR);
      map.put(MessageId.CHK_003, Severity.ERROR);
      map.put(MessageId.CHK_004, Severity.ERROR);
      map.put(MessageId.CHK_005, Severity.ERROR);
      map.put(MessageId.CHK_006, Severity.ERROR);
      map.put(MessageId.CHK_007, Severity.ERROR);
      map.put(MessageId.CHK_008, Severity.ERROR);

      // CSS
      map.put(MessageId.CSS_001, Severity.ERROR);
      map.put(MessageId.CSS_002, Severity.ERROR);
      map.put(MessageId.CSS_003, Severity.ERROR);
      map.put(MessageId.CSS_004, Severity.ERROR);
      map.put(MessageId.CSS_005, Severity.ERROR);
      map.put(MessageId.CSS_006, Severity.WARNING);
      map.put(MessageId.CSS_007, Severity.INFO);
      map.put(MessageId.CSS_008, Severity.ERROR);
      map.put(MessageId.CSS_009, Severity.USAGE);
      map.put(MessageId.CSS_010, Severity.ERROR);
      map.put(MessageId.CSS_011, Severity.SUPPRESSED);
      map.put(MessageId.CSS_012, Severity.USAGE);
      map.put(MessageId.CSS_013, Severity.USAGE);
      map.put(MessageId.CSS_015, Severity.ERROR);
      map.put(MessageId.CSS_016, Severity.SUPPRESSED);
      map.put(MessageId.CSS_017, Severity.WARNING);
      map.put(MessageId.CSS_019, Severity.WARNING);
      map.put(MessageId.CSS_020, Severity.USAGE);
      map.put(MessageId.CSS_021, Severity.USAGE);
      map.put(MessageId.CSS_022, Severity.USAGE);
      map.put(MessageId.CSS_023, Severity.USAGE);
      map.put(MessageId.CSS_024, Severity.USAGE);
      map.put(MessageId.CSS_025, Severity.USAGE);
      map.put(MessageId.CSS_027, Severity.USAGE);
      map.put(MessageId.CSS_028, Severity.USAGE);

      // HTML
      map.put(MessageId.HTM_001, Severity.ERROR);
      map.put(MessageId.HTM_002, Severity.WARNING);
      map.put(MessageId.HTM_003, Severity.ERROR);
      map.put(MessageId.HTM_004, Severity.ERROR);
      map.put(MessageId.HTM_005, Severity.USAGE);
      map.put(MessageId.HTM_006, Severity.USAGE);
      map.put(MessageId.HTM_007, Severity.WARNING);
      map.put(MessageId.HTM_008, Severity.ERROR);
      map.put(MessageId.HTM_009, Severity.ERROR);
      map.put(MessageId.HTM_010, Severity.USAGE);
      map.put(MessageId.HTM_011, Severity.ERROR);
      map.put(MessageId.HTM_012, Severity.USAGE);
      map.put(MessageId.HTM_013, Severity.USAGE);
      map.put(MessageId.HTM_014, Severity.WARNING);
      map.put(MessageId.HTM_014a, Severity.WARNING);
      map.put(MessageId.HTM_015, Severity.SUPPRESSED);
      map.put(MessageId.HTM_016, Severity.SUPPRESSED);
      map.put(MessageId.HTM_017, Severity.ERROR);
      map.put(MessageId.HTM_018, Severity.USAGE);
      map.put(MessageId.HTM_019, Severity.USAGE);
      map.put(MessageId.HTM_020, Severity.USAGE);
      map.put(MessageId.HTM_021, Severity.USAGE);
      map.put(MessageId.HTM_022, Severity.USAGE);
      map.put(MessageId.HTM_023, Severity.WARNING);
      map.put(MessageId.HTM_024, Severity.USAGE);
      map.put(MessageId.HTM_025, Severity.WARNING);
      map.put(MessageId.HTM_027, Severity.USAGE);
      map.put(MessageId.HTM_028, Severity.USAGE);
      map.put(MessageId.HTM_029, Severity.USAGE);
      map.put(MessageId.HTM_033, Severity.USAGE);
      map.put(MessageId.HTM_036, Severity.SUPPRESSED);
      map.put(MessageId.HTM_038, Severity.USAGE);
      map.put(MessageId.HTM_043, Severity.USAGE);
      map.put(MessageId.HTM_044, Severity.USAGE);
      map.put(MessageId.HTM_045, Severity.USAGE);
      map.put(MessageId.HTM_046, Severity.ERROR);
      map.put(MessageId.HTM_047, Severity.ERROR);
      map.put(MessageId.HTM_048, Severity.ERROR);
      map.put(MessageId.HTM_049, Severity.ERROR);
      map.put(MessageId.HTM_050, Severity.USAGE);
      map.put(MessageId.HTM_051, Severity.WARNING);
      map.put(MessageId.HTM_052, Severity.ERROR);

      // Media
      map.put(MessageId.MED_001, Severity.ERROR);
      map.put(MessageId.MED_002, Severity.ERROR);
      map.put(MessageId.MED_003, Severity.ERROR);
      map.put(MessageId.MED_004, Severity.ERROR);
      map.put(MessageId.MED_005, Severity.ERROR);
      map.put(MessageId.MED_006, Severity.USAGE);

      // NAV
      map.put(MessageId.NAV_001, Severity.ERROR);
      map.put(MessageId.NAV_002, Severity.USAGE);
      map.put(MessageId.NAV_003, Severity.ERROR);
      map.put(MessageId.NAV_004, Severity.USAGE);
      map.put(MessageId.NAV_005, Severity.USAGE);
      map.put(MessageId.NAV_006, Severity.USAGE);
      map.put(MessageId.NAV_007, Severity.USAGE);
      map.put(MessageId.NAV_008, Severity.USAGE);
      map.put(MessageId.NAV_009, Severity.ERROR);

      // NCX
      map.put(MessageId.NCX_001, Severity.USAGE);
      map.put(MessageId.NCX_002, Severity.ERROR);
      map.put(MessageId.NCX_003, Severity.USAGE);
      map.put(MessageId.NCX_004, Severity.USAGE);
      map.put(MessageId.NCX_005, Severity.USAGE);
      map.put(MessageId.NCX_006, Severity.USAGE);

      // OPF
      map.put(MessageId.OPF_001, Severity.ERROR);
      map.put(MessageId.OPF_002, Severity.FATAL);
      map.put(MessageId.OPF_003, Severity.WARNING);
      map.put(MessageId.OPF_004, Severity.WARNING);
      map.put(MessageId.OPF_004a, Severity.ERROR);
      map.put(MessageId.OPF_004b, Severity.ERROR);
      map.put(MessageId.OPF_004c, Severity.ERROR);
      map.put(MessageId.OPF_004d, Severity.ERROR);
      map.put(MessageId.OPF_004e, Severity.WARNING);
      map.put(MessageId.OPF_004f, Severity.WARNING);
      map.put(MessageId.OPF_005, Severity.ERROR);
      map.put(MessageId.OPF_006, Severity.ERROR);
      map.put(MessageId.OPF_007, Severity.WARNING);
      map.put(MessageId.OPF_007a, Severity.ERROR);
      map.put(MessageId.OPF_007b, Severity.WARNING);
      map.put(MessageId.OPF_008, Severity.ERROR);
      map.put(MessageId.OPF_009, Severity.ERROR);
      map.put(MessageId.OPF_010, Severity.ERROR);
      map.put(MessageId.OPF_011, Severity.ERROR);
      map.put(MessageId.OPF_012, Severity.ERROR);
      map.put(MessageId.OPF_013, Severity.ERROR);
      map.put(MessageId.OPF_014, Severity.ERROR);
      map.put(MessageId.OPF_015, Severity.ERROR);
      map.put(MessageId.OPF_016, Severity.ERROR);
      map.put(MessageId.OPF_017, Severity.ERROR);
      map.put(MessageId.OPF_018, Severity.WARNING);
      map.put(MessageId.OPF_019, Severity.FATAL);
      map.put(MessageId.OPF_020, Severity.SUPPRESSED);
      map.put(MessageId.OPF_021, Severity.WARNING);
      map.put(MessageId.OPF_025, Severity.ERROR);
      map.put(MessageId.OPF_026, Severity.ERROR);
      map.put(MessageId.OPF_027, Severity.ERROR);
      map.put(MessageId.OPF_028, Severity.ERROR);
      map.put(MessageId.OPF_029, Severity.ERROR);
      map.put(MessageId.OPF_030, Severity.ERROR);
      map.put(MessageId.OPF_031, Severity.ERROR);
      map.put(MessageId.OPF_032, Severity.ERROR);
      map.put(MessageId.OPF_033, Severity.ERROR);
      map.put(MessageId.OPF_034, Severity.ERROR);
      map.put(MessageId.OPF_035, Severity.WARNING);
      map.put(MessageId.OPF_036, Severity.USAGE);
      map.put(MessageId.OPF_037, Severity.WARNING);
      map.put(MessageId.OPF_038, Severity.WARNING);
      map.put(MessageId.OPF_039, Severity.WARNING);
      map.put(MessageId.OPF_040, Severity.ERROR);
      map.put(MessageId.OPF_041, Severity.ERROR);
      map.put(MessageId.OPF_042, Severity.ERROR);
      map.put(MessageId.OPF_043, Severity.ERROR);
      map.put(MessageId.OPF_044, Severity.ERROR);
      map.put(MessageId.OPF_045, Severity.ERROR);
      map.put(MessageId.OPF_046, Severity.ERROR);
      map.put(MessageId.OPF_047, Severity.USAGE);
      map.put(MessageId.OPF_048, Severity.ERROR);
      map.put(MessageId.OPF_049, Severity.ERROR);
      map.put(MessageId.OPF_050, Severity.ERROR);
      map.put(MessageId.OPF_051, Severity.SUPPRESSED);
      map.put(MessageId.OPF_052, Severity.ERROR);
      map.put(MessageId.OPF_053, Severity.WARNING);
      map.put(MessageId.OPF_054, Severity.ERROR);
      map.put(MessageId.OPF_055, Severity.WARNING);
      map.put(MessageId.OPF_056, Severity.USAGE);
      map.put(MessageId.OPF_057, Severity.SUPPRESSED);
      map.put(MessageId.OPF_058, Severity.USAGE);
      map.put(MessageId.OPF_059, Severity.USAGE);
      map.put(MessageId.OPF_060, Severity.ERROR);
      map.put(MessageId.OPF_061, Severity.WARNING);
      map.put(MessageId.OPF_062, Severity.USAGE);
      map.put(MessageId.OPF_063, Severity.WARNING);
      map.put(MessageId.OPF_064, Severity.INFO);
      map.put(MessageId.OPF_065, Severity.ERROR);
      map.put(MessageId.OPF_066, Severity.ERROR);
      map.put(MessageId.OPF_067, Severity.ERROR);
      map.put(MessageId.OPF_068, Severity.ERROR);
      map.put(MessageId.OPF_069, Severity.ERROR);
      map.put(MessageId.OPF_070, Severity.WARNING);
      map.put(MessageId.OPF_071, Severity.ERROR);
      map.put(MessageId.OPF_072, Severity.USAGE);
      map.put(MessageId.OPF_073, Severity.ERROR);
      map.put(MessageId.OPF_074, Severity.ERROR);
      map.put(MessageId.OPF_075, Severity.ERROR);
      map.put(MessageId.OPF_076, Severity.ERROR);
      map.put(MessageId.OPF_077, Severity.WARNING);
      map.put(MessageId.OPF_078, Severity.ERROR);
      map.put(MessageId.OPF_079, Severity.WARNING);
      map.put(MessageId.OPF_080, Severity.WARNING);
      map.put(MessageId.OPF_081, Severity.ERROR);
      map.put(MessageId.OPF_082, Severity.ERROR);
      map.put(MessageId.OPF_083, Severity.ERROR);
      map.put(MessageId.OPF_084, Severity.ERROR);

      // PKG
      map.put(MessageId.PKG_001, Severity.WARNING);
      map.put(MessageId.PKG_003, Severity.ERROR);
      map.put(MessageId.PKG_004, Severity.FATAL);
      map.put(MessageId.PKG_005, Severity.ERROR);
      map.put(MessageId.PKG_006, Severity.ERROR);
      map.put(MessageId.PKG_007, Severity.ERROR);
      map.put(MessageId.PKG_008, Severity.FATAL);
      map.put(MessageId.PKG_009, Severity.ERROR);
      map.put(MessageId.PKG_010, Severity.WARNING);
      map.put(MessageId.PKG_011, Severity.ERROR);
      map.put(MessageId.PKG_012, Severity.WARNING);
      map.put(MessageId.PKG_013, Severity.ERROR);
      map.put(MessageId.PKG_014, Severity.WARNING);
      map.put(MessageId.PKG_015, Severity.FATAL);
      map.put(MessageId.PKG_016, Severity.WARNING);
      map.put(MessageId.PKG_017, Severity.WARNING);
      map.put(MessageId.PKG_018, Severity.FATAL);
      map.put(MessageId.PKG_020, Severity.ERROR);
      map.put(MessageId.PKG_021, Severity.ERROR);
      map.put(MessageId.PKG_022, Severity.WARNING);
      map.put(MessageId.PKG_023, Severity.USAGE);

      // Resources
      map.put(MessageId.RSC_001, Severity.ERROR);
      map.put(MessageId.RSC_002, Severity.FATAL);
      map.put(MessageId.RSC_003, Severity.ERROR);
      map.put(MessageId.RSC_004, Severity.ERROR);
      map.put(MessageId.RSC_005, Severity.ERROR);
      map.put(MessageId.RSC_006, Severity.ERROR);
      map.put(MessageId.RSC_007, Severity.ERROR);
      map.put(MessageId.RSC_007w, Severity.WARNING);
      map.put(MessageId.RSC_008, Severity.ERROR);
      map.put(MessageId.RSC_009, Severity.ERROR);
      map.put(MessageId.RSC_010, Severity.ERROR);
      map.put(MessageId.RSC_011, Severity.ERROR);
      map.put(MessageId.RSC_012, Severity.ERROR);
      map.put(MessageId.RSC_013, Severity.ERROR);
      map.put(MessageId.RSC_014, Severity.ERROR);
      map.put(MessageId.RSC_015, Severity.ERROR);
      map.put(MessageId.RSC_016, Severity.FATAL);
      map.put(MessageId.RSC_017, Severity.WARNING);
      map.put(MessageId.RSC_018, Severity.WARNING);
      map.put(MessageId.RSC_019, Severity.WARNING);
      map.put(MessageId.RSC_020, Severity.ERROR);
      map.put(MessageId.RSC_021, Severity.ERROR);
      map.put(MessageId.RSC_022, Severity.INFO);
      map.put(MessageId.RSC_023, Severity.WARNING);

      // Scripting
      map.put(MessageId.SCP_001, Severity.USAGE);
      map.put(MessageId.SCP_002, Severity.USAGE);
      map.put(MessageId.SCP_003, Severity.USAGE);
      map.put(MessageId.SCP_004, Severity.ERROR);
      map.put(MessageId.SCP_005, Severity.ERROR);
      map.put(MessageId.SCP_006, Severity.USAGE);
      map.put(MessageId.SCP_007, Severity.USAGE);
      map.put(MessageId.SCP_008, Severity.USAGE);
      map.put(MessageId.SCP_009, Severity.USAGE);
      map.put(MessageId.SCP_010, Severity.USAGE);

      defaultSeverityMap = map;
    }
    return defaultSeverityMap;
  }

  void initDefaultMessageMap()
  {
    messages.clear();
    for (Map.Entry<MessageId, Severity> entry : getDefaultSeverities().entrySet())
    {
      this.addMessage(entry.getKey(), entry.getValue());
    }
  }

  void initMessageMap()
  {
    initDefaultMessageMap();
    loadOverriddenMessageSeverities();
  }

  void loadOverriddenMessageSeverities()
  {
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
              report.message(MessageId.CHK_002, EPUBLocation.create("", lineNumber, 0), fields[0],
                  PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
              continue;
            }

            Severity newSeverity;

            try
            {
              columnNumber += 1 + fields[0].length();
              newSeverity = Severity.fromString(fields[1]);
            } catch (NoSuchElementException ignored)
            {
              report.message(MessageId.CHK_003, EPUBLocation.create("", lineNumber, columnNumber),
                  fields[1], PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
              continue;
            }

            Message message = messages.get(id);
            String messageText = message.getMessage();
            if (fields.length >= 3 && fields[2] != null && fields[2].length() > 0)
            {
              columnNumber += 1 + fields[1].length();
              messageText = checkMessageForParameterCount(lineNumber, columnNumber,
                  message.getMessage(), fields[2]);
              if (messageText == null)
              {
                report.message(MessageId.CHK_004, EPUBLocation.create("", lineNumber, 0, fields[2]),
                    PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
                continue;
              }
            }
            if (messageText != null)
            {
              Severity oldSeverity = getDefaultSeverities().get(message.getID());
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
                report.message(MessageId.CHK_005, EPUBLocation.create("", lineNumber, 0, fields[3]),
                    PathUtil.removeWorkingDirectory(overrideFile.getAbsolutePath()));
                continue;
              }
            }

            if (message != null && ((newSeverity != message.getSeverity())
                || (messageText.compareTo(message.getMessage()) != 0)
                || (suggestionText.compareTo(message.getSuggestion()) != 0)))
            {
              messages.put(id, new Message(message.getID(), newSeverity, message.getSeverity(),
                  messageText, suggestionText));
            }
          }
          ++lineNumber;
        }
      } catch (FileNotFoundException fnf)
      {
        report.message(MessageId.CHK_001, EPUBLocation.create(overrideFile.getAbsolutePath()));
      } catch (IOException ex)
      {
        report.message(MessageId.CHK_007, EPUBLocation.create("", lineNumber, columnNumber),
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

  String checkMessageForParameterCount(int lineNumber, int columnNumber, String originalText,
      String newText)
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

  int getParameterCount(int lineNumber, int columnNumber, String text)
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
              EPUBLocation.create("", lineNumber, absoluteColumnNumber, text),
              pathAdjustedFileName);
        }
      }
    }
    return max;
  }

  void addMessage(MessageId messageId, Severity severity)
  {
    try
    {
      messages.put(messageId, new Message(messageId, severity, labels.getString(messageId.name()),
          getSuggestion(messageId)));
    } catch (Exception e)
    {
      outWriter.println("Couldn't locate message " + messageId.name());
    }
  }

  String getSuggestion(MessageId messageId)
  {
    String result;
    try
    {
      result = labels.getString(messageId.name() + "_SUG");
    } catch (Exception ignore)
    {
      result = "";
    }
    return result;
  }

  public void dumpMessages(OutputStreamWriter outputStream)
    throws IOException
  {
    // Output the messages in a tab separated format
    outputStream.write("ID\tSeverity\tMessage\tSuggestion\n");
    for (MessageId id : MessageId.values())
    {
      StringBuilder sb = new StringBuilder();
      sb.append(id.toString());
      sb.append("\t");
      Message message = this.getMessage(id);
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

  private static class UTF8Control extends Control
  {
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
        ClassLoader loader, boolean reload)
          throws IllegalAccessException,
          InstantiationException,
          IOException
    {
      // The below is a copy of the default implementation.
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, "properties"); //$NON-NLS-1$
      ResourceBundle bundle = null;
      InputStream stream = null;
      if (reload)
      {
        URL url = loader.getResource(resourceName);
        if (url != null)
        {
          URLConnection connection = url.openConnection();
          if (connection != null)
          {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      }
      else
      {
        stream = loader.getResourceAsStream(resourceName);
      }
      if (stream != null)
      {
        try
        {
          // Only this line is changed to make it to read properties files as
          // UTF-8.
          bundle = new PropertyResourceBundle(
              new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8)));
        } finally
        {
          stream.close();
        }
      }
      return bundle;
    }
  }
}

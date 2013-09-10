package com.adobe.epubcheck.util;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;

import java.util.HashSet;
import java.util.Set;

public class MetaUtils
{

  public static Set<String> validateProperties(String propertyValue,
      Set<String> recognizedUnprefixedValues,
      Set<String> recognizedPrefixes, String path, int line, int column,
      Report report, boolean singleValue)
  {

    if (propertyValue == null)
    {
      return null;
    }

    Set<String> unprefixedValues = new HashSet<String>();

    propertyValue = propertyValue.trim();
    propertyValue = propertyValue.replaceAll("[\\s]+", " ");
    String propertyArray[] = propertyValue.split(" ");

    if (singleValue && propertyArray.length > 1)
    {
      report.message(MessageId.OPF_025, new MessageLocation(path, line, column), propertyValue);
    }

    for (String aPropertyArray : propertyArray)
    {
      if (aPropertyArray.endsWith(":"))
      {
        report.message(MessageId.OPF_026, new MessageLocation(path, line, column), aPropertyArray);
      }
      else if (aPropertyArray.contains(":"))
      {
        checkPrefix(
            recognizedPrefixes,
            aPropertyArray.substring(0,
                aPropertyArray.indexOf(':')), path, line,
            column, report);
      }
      else if (recognizedUnprefixedValues != null
          && recognizedUnprefixedValues.contains(aPropertyArray))
      {
        unprefixedValues.add(aPropertyArray);
      }
      else
      {
        report.message(MessageId.OPF_027, new MessageLocation(path, line, column), aPropertyArray);
      }
    }

    return unprefixedValues;
  }

  private static boolean checkPrefix(Set<String> prefixSet, String prefix,
      String path, int line, int column, Report report)
  {

    if (!prefixSet.contains(prefix))
    {
      report.message(MessageId.OPF_028, new MessageLocation(path, line, column), prefix);
      return false;
    }
    return true;
  }
}

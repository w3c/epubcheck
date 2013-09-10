package com.adobe.epubcheck.util;

import com.adobe.epubcheck.messages.MessageId;

import java.util.regex.Pattern;

public class TextSearchDictionaryEntry
{

  private String searchedValue;
  private String regexExp;
  private MessageId errorCode;
  private Pattern pattern;

  public TextSearchDictionaryEntry(String searchedValue, String regex, MessageId errorCode)
  {
    this.searchedValue = searchedValue;
    this.regexExp = regex;
    this.errorCode = errorCode;
    this.pattern = null;
  }

  public String getSearchedValue()
  {
    return searchedValue;
  }

  public String getRegexExp()
  {
    return regexExp;
  }

  public MessageId getErrorCode()
  {
    return errorCode;
  }

  public Pattern getPattern()
  {
    if (pattern == null)
    {
      pattern = Pattern.compile(this.getRegexExp());
    }
    return pattern;
  }

}
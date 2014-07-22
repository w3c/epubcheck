package com.adobe.epubcheck.util;

import com.adobe.epubcheck.messages.MessageId;

import java.util.Vector;

public class SearchDictionary
{

  public enum DictionaryType
  {
    VALID_TEXT_MEDIA_TYPES, CSS_FILES, CSS_VALUES, LINK_VALUES, SVG_MEDIA_TYPES
  }


  public SearchDictionary(DictionaryType dt)
  {
    if (dt.equals(DictionaryType.VALID_TEXT_MEDIA_TYPES))
    {
      buildValidTypesDictionary();
    }
    if (dt.equals(DictionaryType.CSS_VALUES))
    {
      buildCssSearchDictionary();
    }
    if (dt.equals(DictionaryType.CSS_FILES))
    {
      buildCSSTypesDictionary();
    }
    if (dt.equals(DictionaryType.LINK_VALUES))
    {
      buildLinkSearchDictionary();
    }
    if (dt.equals(DictionaryType.SVG_MEDIA_TYPES))
    {
      buildSVGSearchDictionary();
    }
  }

  private final Vector<TextSearchDictionaryEntry> v = new Vector<TextSearchDictionaryEntry>();
  private final Vector<TextSearchDictionaryEntry> e = new Vector<TextSearchDictionaryEntry>();
/*
	String[] validTypes = new String[] 
	    { "application/xhtml+xml",
			"application/x-dtbncx+xml", "text/css" };
	
	*/

  void buildCSSTypesDictionary()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;


    //search eval() expression
    description = "text/css";
    value = "text/css";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);


  }


  void buildCssSearchDictionary()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;


    //search eval() expression
    description = "rotateX()";
    value = "rotateX()";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

    description = "rotateY()";
    value = "rotateY()";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);


    description = "column-count";
    value = "column-count";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

    description = "column-gap";
    value = "column-gap";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

    description = "column-rule";
    value = "column-rule";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

    description = "keyframes";
    value = "keyframes";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

    description = "transition";
    value = "transition";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    v.add(de);

  }

  void buildValidTypesDictionary()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;

    description = "application/xhtml+xml";
    value = "application/xhtml+xml";
    de = new TextSearchDictionaryEntry(description, value, null);
    v.add(de);
  }

  void buildLinkSearchDictionary()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;


    description = "Http:";
    value = "[Hh][Tt][Tt][Pp]*\\:";
    de = new TextSearchDictionaryEntry(description, value, MessageId.HTM_005);
    v.add(de);

    description = "Ftp:";
    value = "[Ff][Tt][Pp]*\\:";
    de = new TextSearchDictionaryEntry(description, value, MessageId.HTM_005);
    v.add(de);

    description = "File:";
    value = "[Ff][Ii][Ll][Ee]*\\:";
    de = new TextSearchDictionaryEntry(description, value, MessageId.HTM_005);
    v.add(de);

  }

  public Vector<TextSearchDictionaryEntry> getDictEntries()
  {
    return v;
  }

  public Vector<TextSearchDictionaryEntry> getExceptionEntries()
  {
    return e;
  }

  void buildSVGSearchDictionary()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;

    description = "image/svg+xml";
    value = "image/svg+xml";
    de = new TextSearchDictionaryEntry(description, value, null);
    v.add(de);
  }

  public boolean isValidMediaType(String typeToCheck)
  {
    if (typeToCheck == null)
    {
      return false;
    }

    for (int i = 0; i < getDictEntries().size(); i++)
    {
      if ((getDictEntries().get(i).getRegexExp()).compareToIgnoreCase(typeToCheck) == 0)
      {
        return true;
      }
    }
    return false;
  }


}

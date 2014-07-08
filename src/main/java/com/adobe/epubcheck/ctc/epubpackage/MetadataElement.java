package com.adobe.epubcheck.ctc.epubpackage;

import java.util.HashMap;

public class MetadataElement
{
  private final HashMap<String, String> attributes = new HashMap<String, String>();
  private String value = "";
  private String name = "";

  public void addAttribute(String attrName, String attrValue)
  {
    attributes.put(attrName, attrValue);
  }

  public HashMap getAllAttributes()
  {
    return attributes;
  }

  public String getAttribute(String attrName)
  {
    return attributes.get(attrName);
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }
}

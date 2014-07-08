package com.adobe.epubcheck.ctc.css;

import javax.xml.stream.Location;
import java.util.HashMap;

public class CSSSelector
{
  private final String name;
  private final Location location;
  private final boolean isClass;
  private final HashMap<String, CSSSelectorAttribute> attributes = new HashMap<String, CSSSelectorAttribute>();

  public CSSSelector(String name, Location location, boolean isClass)
  {
    this.name = name;
    this.location = location;
    this.isClass = isClass;
  }

  public String getName()
  {
    return name;
  }

  public boolean isClass()
  {
    return isClass;
  }


  public Location getLocation()
  {
    return location;
  }

  public HashMap<String, CSSSelectorAttribute> getAttributes()
  {
    return attributes;
  }

  public void addAttribute(CSSSelectorAttribute attribute)
  {
    attributes.put(attribute.getName(), attribute);
  }
}

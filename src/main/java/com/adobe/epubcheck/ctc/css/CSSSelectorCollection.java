package com.adobe.epubcheck.ctc.css;

import javax.xml.stream.Location;
import java.util.HashMap;

public class CSSSelectorCollection
{
  private final String name;
  private final int scopeId;
  private final Location location;
  private final HashMap<String, CSSSelector> selectors = new HashMap<String, CSSSelector>();

  public CSSSelectorCollection(String name, Location location, int scopeId)
  {
    this.name = name;
    this.location = location;
    this.scopeId = scopeId;
  }

  public HashMap<String, CSSSelector> getSelectors()
  {
    return selectors;
  }

  public void addSelector(CSSSelector selector)
  {
    CSSSelector existing = selectors.get(selector.getName());
    if (existing != null)
    {
      for (CSSSelectorAttribute attribute : selector.getAttributes().values())
      {
        existing.addAttribute(attribute);
      }
      selector = existing;
    }
    selectors.put(selector.getName(), selector);
  }
}

package com.adobe.epubcheck.ctc.xml;

import java.util.HashMap;

/*  ===  WARNING  ==========================================
 *  This class is scheduled to be refactored and integrated
 *  in another package.
 *  Please keep changes minimal (bug fixes only) until then.
 *  ========================================================
 */
public class ScriptElement
{
  private final HashMap<String, String> attrs = new HashMap<String, String>();
  private boolean isExternal = false;

  public void addAttribute(String name, String value)
  {
    attrs.put(name, value);
  }

  public String getAttribute(String name)
  {
    return attrs.get(name);
  }
}
package com.adobe.epubcheck.ctc.css;

/*  ===  WARNING  ==========================================
 *  This class is scheduled to be refactored and integrated
 *  in another package.
 *  Please keep changes minimal (bug fixes only) until then.
 *  ========================================================
 */
class CSSSelectorAttribute
{
  private final String name;
  private final String value;
  private final boolean isImportant;
  private final CSSSelector originatingSelector;

  public CSSSelectorAttribute(String name, String value, boolean isImportant, CSSSelector originatingSelector)
  {
    this.name = name;
    this.value = value;
    this.isImportant = isImportant;
    this.originatingSelector = originatingSelector;
  }

  public String getName()
  {
    return name;
  }

  public String getValue()
  {
    return value;
  }

  public boolean isImportant()
  {
    return isImportant;
  }

  public CSSSelector getOriginatingSelector()
  {
    return originatingSelector;
  }
}

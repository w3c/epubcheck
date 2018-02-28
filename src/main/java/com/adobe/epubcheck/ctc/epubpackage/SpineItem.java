package com.adobe.epubcheck.ctc.epubpackage;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class SpineItem
{
  private String idref;
  private String id;
  private String linear;
  private String properties;

  public String getIdref()
  {
    return idref;
  }

  public void setIdref(String idref)
  {
    this.idref = idref;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getLinear()
  {
    return linear;
  }

  public void setLinear(String linear)
  {
    this.linear = linear;
  }

  public String getProperties()
  {
    return properties;
  }

  public void setProperties(String properties)
  {
    this.properties = properties;
  }
}

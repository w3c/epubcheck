package com.adobe.epubcheck.ctc.epubpackage;

public class ManifestItem
{
  private String id;
  private String href;
  private String mediaType;
  private String properties;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getHref()
  {
    return href;
  }

  public void setHref(String href)
  {
    this.href = href;
  }

  public String getMediaType()
  {
    return mediaType;
  }

  public void setMediaType(String mediaType)
  {
    this.mediaType = mediaType;
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

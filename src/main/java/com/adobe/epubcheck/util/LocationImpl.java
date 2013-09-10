package com.adobe.epubcheck.util;

import javax.xml.stream.Location;

public class LocationImpl implements Location
{
  private final int lineNumber;
  private final int columnNumber;
  private final int characterOffset;
  private final String publicId;
  private final String systemId;


  public LocationImpl(int lineNumber, int columnNumber, int characterOffset, String publicId, String systemId)
  {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.characterOffset = characterOffset;
    this.publicId = publicId;
    this.systemId = systemId;
  }

  @Override
  public int getLineNumber()
  {
    return lineNumber;
  }

  @Override
  public int getColumnNumber()
  {
    return columnNumber;
  }

  @Override
  public int getCharacterOffset()
  {
    return characterOffset;
  }

  @Override
  public String getPublicId()
  {
    return publicId;
  }

  @Override
  public String getSystemId()
  {
    return systemId;
  }
}

package com.adobe.epubcheck.opf;

import java.util.Locale;

public enum PublicationType
{

  EPUB,
  DICTIONARY,
  EDUPUB,
  INDEX,
  PREVIEW;
  
  @Override
  public String toString()
  {
    return super.name().toLowerCase(Locale.ROOT);
  }
}

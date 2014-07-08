package com.adobe.epubcheck.ctc.epubpackage;

import java.util.Vector;

public class PackageMetadata
{
  private final Vector<MetadataElement> m = new Vector<MetadataElement>();

  public Vector<MetadataElement> getMetaElements()
  {
    return m;
  }

  public void addMetaElement(MetadataElement meta)
  {
    m.add(meta);
  }
}

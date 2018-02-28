package com.adobe.epubcheck.ctc.epubpackage;

import java.util.Vector;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
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

package com.adobe.epubcheck.ctc.epubpackage;

import java.util.Vector;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class PackageManifest
{
  private final Vector<ManifestItem> items = new Vector<ManifestItem>();

  public Vector getItems()
  {
    return items;
  }

  public int itemsLength()
  {
    return items.size();
  }

  public void addItem(ManifestItem mi)
  {
    items.add(mi);
  }

  public ManifestItem getItem(int i)
  {
    return items.get(i);
  }

  public ManifestItem getItem(String id)
  {
    if (id == null || id.trim().equals(""))
    {
      return null;
    }
    for (ManifestItem item : items)
    {
      if (id.trim().equals(item.getId().trim()))
      {
        return item;
      }
    }
    return null;
  }
}

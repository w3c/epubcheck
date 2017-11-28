package com.adobe.epubcheck.ctc.epubpackage;

import java.util.Vector;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class PackageSpine
{
  private String toc;
  private String id;
  private String pageProgressionDirection;
  private final Vector<SpineItem> items = new Vector<SpineItem>();

  public Vector<SpineItem> getItems()
  {
    return items;
  }

  public int itemsLength()
  {
    return items.size();
  }

  public void addItem(SpineItem mi)
  {
    items.add(mi);
  }

  public SpineItem getItem(int i)
  {
    return items.get(i);
  }

  public String getToc()
  {
    return toc;
  }

  public void setToc(String toc)
  {
    this.toc = toc;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getPageProgressionDirection()
  {
    return pageProgressionDirection;
  }

  public void setPageProgressionDirection(String pageProgressionDirection)
  {
    this.pageProgressionDirection = pageProgressionDirection;
  }
}

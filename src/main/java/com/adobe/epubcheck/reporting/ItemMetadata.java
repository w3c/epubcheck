package com.adobe.epubcheck.reporting;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.jackson.annotate.JsonProperty;

import com.adobe.epubcheck.util.FeatureEnum;

@SuppressWarnings("FieldCanBeLocal")
public class ItemMetadata implements Comparable<ItemMetadata>
{
  @JsonProperty
  private String id = "";
  @JsonProperty
  private String fileName;
  @JsonProperty
  private String media_type;
  @JsonProperty
  private long compressedSize;
  @JsonProperty
  private long uncompressedSize;
  @JsonProperty
  private String compressionMethod;
  @JsonProperty
  private String checkSum;
  @JsonProperty
  private boolean isSpineItem;
  @JsonProperty
  private Integer spineIndex;
  @JsonProperty
  private boolean isLinear;
  @JsonProperty
  private Integer navigationOrder = null;
  @JsonProperty
  private boolean isHTML5;
  @JsonProperty
  private Boolean isFixedFormat = null;
  @JsonProperty
  private boolean isScripted;
  @JsonProperty
  private boolean scriptSrc;
  @JsonProperty
  private boolean scriptTag;
  @JsonProperty
  private boolean scriptInline;
  @JsonProperty
  private String renditionLayout;
  @JsonProperty
  private String renditionOrientation;
  @JsonProperty
  private String renditionSpread;
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @JsonProperty
  private final SortedSet<String> referencedItems = new TreeSet<String>();

  public static ItemMetadata getItemByName(Map<String, ItemMetadata> metadata, String fileName)
  {
    ItemMetadata result = metadata.get(fileName);
    if (result == null)
    {
      result = new ItemMetadata();
      result.fileName = fileName;
      metadata.put(fileName, result);
    }
    return result;
  }

  public String getId()
  {
    return this.id;
  }

  public void setId(String value)
  {
    this.id = value;
  }

  public String getFileName()
  {
    return this.fileName;
  }

  public String getRenditionLayout()
  {
    return this.renditionLayout;
  }

  public void setRenditionLayout(String value)
  {
    this.renditionLayout = value;
  }

  public String getRenditionOrientation()
  {
    return this.renditionOrientation;
  }

  public void setRenditionOrientation(String value)
  {
    this.renditionOrientation = value;
  }

  public String getRenditionSpread()
  {
    return this.renditionSpread;
  }

  public void setRenditionSpread(String value)
  {
    this.renditionSpread = value;
  }


  public Boolean getIsFixedFormat()
  {
    return this.isFixedFormat;
  }

  public void setIsFixedFormat(Boolean value)
  {
    this.isFixedFormat = value;
  }

  public boolean getIsSpineItem()
  {
    return this.isSpineItem;
  }

  public void handleInfo(FeatureEnum feature, String value)
  {
    switch (feature)
    {
      case DECLARED_MIMETYPE:
        this.media_type = value;
        break;
      case HAS_SCRIPTS:
        this.isScripted = true;
        break;
      case HAS_FIXED_LAYOUT:
        this.isFixedFormat = true;
        break;
      case IS_SPINEITEM:
        this.isSpineItem = true;
        break;
      case UNIQUE_IDENT:
        this.id = value != null ? value : "";
        break;
      case IS_LINEAR:
        this.isLinear = Boolean.parseBoolean(value.trim());
        break;
      case RESOURCE:
        if (!value.equals(this.fileName))
        {
          this.referencedItems.add(value);
        }
        break;
      case SIZE:
        this.uncompressedSize = Long.parseLong(value.trim());
        break;
      case COMPRESSED_SIZE:
        this.compressedSize = Long.parseLong(value.trim());
        break;
      case COMPRESSION_METHOD:
        this.compressionMethod = value;
        break;
      case SHA_256:
        this.checkSum = value;
        break;
      case SPINE_INDEX:
        this.spineIndex = Integer.parseInt(value.trim());
        break;
      case HAS_HTML5:
        this.isHTML5 = true;
        break;
      case SCRIPT:
        if (value.equals("inline"))
        {
          this.scriptInline = true;
        }
        else if (value.equals("external"))
        {
          this.scriptSrc = true;
        }
        else if (value.equals("javascript"))
        {
          this.scriptSrc = true;
        }
        else if (value.equals("tag"))
        {
          this.scriptTag = true;
        }
        break;
      case RENDITION_LAYOUT:
        this.renditionLayout = value;
        break;
      case RENDITION_ORIENTATION:
        this.renditionOrientation = value;
        break;
      case RENDITION_SPREAD:
        this.renditionSpread = value;
        break;
      case NAVIGATION_ORDER:
        this.navigationOrder = Integer.parseInt(value.trim());
        break;
      default:
        //System.err.printf("unhandled info message feature: found '%s' with value '%s'", feature.toString(), value != null ? value : "null");
        break;
    }
  }

  public int compareTo(ItemMetadata item)
  {
    return this.id.compareTo(item.id);
  }
}

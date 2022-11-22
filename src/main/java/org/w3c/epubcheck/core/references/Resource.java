package org.w3c.epubcheck.core.references;

import com.adobe.epubcheck.opf.OPFItem;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

public class Resource
{
  private static final class Builder
  {

    private URL url;
    private OPFItem item = null;
    public String mimetype;

    public Resource.Builder url(URL url)
    {
      this.url = url;
      return this;
    }

    public Resource.Builder item(OPFItem item)
    {
      this.url = item.getURL();
      this.item = item;
      this.mimetype = item.getMimeType();
      return this;
    }

    public Resource.Builder mimetype(String mimetype)
    {
      this.mimetype = mimetype;
      return this;
    }

    public Resource build()
    {
      return new Resource(this);
    }
  }

  public static Resource fromItem(OPFItem item)
  {
    return new Builder().item(item).build();
  }

  public static Resource fromURL(URL url, String mimetype)
  {
    return new Builder().url(url).mimetype(mimetype).build();
  }

  private final URL url;
  private final String mimetype;
  private final Optional<OPFItem> item;

  private Resource(Resource.Builder builder)
  {
    Preconditions.checkState(builder.url != null, "A URL or OPF Item must be provided");
    Preconditions.checkState(builder.mimetype != null, "A MIME type must be provided");
    Preconditions
        .checkState(builder.item == null || builder.item.getMimeType().equals(builder.mimetype));
    this.url = builder.url;
    this.item = Optional.fromNullable(builder.item);
    this.mimetype = builder.mimetype;

  }

  public String getMimeType()
  {
    return mimetype;
  }

  public boolean hasItem()
  {
    return item.isPresent();
  }

  public OPFItem getItem()
  {
    return item.orNull();
  }

  public URL getURL()
  {
    return url;
  }

  public boolean hasCoreMediaTypeFallback()
  {
    return item.isPresent() && item.get().hasCoreMediaTypeFallback();
  }

  public boolean hasContentDocumentFallback()
  {
    // TODO Auto-generated method stub
    return item.isPresent() && item.get().hasContentDocumentFallback();
  }

  public boolean isInSpine()
  {
    return item.isPresent() && item.get().isInSpine();
  }

  @Override
  public String toString()
  {
    return url.toString();
  }
}
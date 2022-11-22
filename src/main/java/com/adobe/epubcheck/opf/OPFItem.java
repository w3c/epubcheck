/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.opf;

import java.util.Set;

import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

/**
 * Immutable representation of an item in a Package Document (OPF). Can
 * represent a <code>item</code> element or <code>link</code> elements pointing
 * to a container resource.
 */
public class OPFItem
{
  private final String id;
  private final URL url;
  private final EPUBLocation location;
  private final String path;
  private final String mimetype;
  private final boolean hasFallback;
  private final boolean hasCoreMediaTypeFallback;
  private final boolean hasContentDocumentFallback;
  private final Set<Property> properties;
  private final boolean ncx;
  private final boolean inSpine;
  private final int spinePosition;
  private final boolean nav;
  private final boolean scripted;
  private final boolean linear;
  private final boolean fixedLayout;
  private final boolean remote;
  private final String mediaOverlay;

  private OPFItem(Builder builder)
  {
    Preconditions.checkState(builder.id != null, "item ID is null");
    Preconditions.checkState(builder.url != null, "item path is null");
    Preconditions.checkState(builder.location != null, "item location is null");

    if (builder.spinePosition < 0 || !builder.linear)
    {
      builder.propertiesBuilder.add(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR));
    }
    if (builder.fxl)
    {
      builder.propertiesBuilder
          .add(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.FIXED_LAYOUT));
    }

    this.id = builder.id.trim();
    this.url = builder.url;
    this.mimetype = builder.mimetype();
    this.location = builder.location;
    this.hasFallback = builder.hasFallback();
    this.hasCoreMediaTypeFallback = builder.hasCoreMediaTypeFallback();
    this.hasContentDocumentFallback = builder.hasContentDocumentFallback();
    this.properties = builder.propertiesBuilder.build();
    this.ncx = builder.ncx;
    this.inSpine = builder.spinePosition > -1;
    this.spinePosition = builder.spinePosition;
    this.nav = properties.contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.NAV));
    this.scripted = properties
        .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.SCRIPTED));
    this.linear = builder.linear;
    this.fixedLayout = builder.fxl;
    this.mediaOverlay = builder.mediaOverlay;
    this.remote = builder.remote;

    // Compute a relative item path
    try
    {
      // If the item is a remote resource, return the
      // full URL string (decoded)
      if (remote)
      {
        this.path = url.toHumanString();
      }
      // If the item is defined with a data URL, return
      // the URL string truncated arbitrarily to 30 chars
      else if ("data".equals(url.scheme()))
      {
        String urlString = url.toString();
        this.path = url.toString().substring(0, Math.min(urlString.length(), 30)) + "…";
      }
      // If a container is present (full-publication check)
      // the item path is relative to the root of the container
      else if (builder.container.isPresent())
      {
        this.path = URLUtils.decode(builder.container.get().relativize(url));
      }
      // If a container is not present (single-file check)
      // we try to relativize the path from the package document path
      else
      {
        this.path = URLUtils.decode(location.url.resolve(".").relativize(URLUtils.docURL(url)));
      }
    } catch (GalimatiasParseException impossible)
    {
      throw new AssertionError(impossible);
    }
  }

  /**
   * Returns the ID of this item.
   * 
   * @return the ID of this item, guaranteed non-null.
   */
  public String getId()
  {
    return id;
  }

  /**
   * The URL of this item (cannot be <code>null</code>).
   * 
   * @return the container URL of this item
   */
  public URL getURL()
  {
    return url;
  }

  /**
   * The path of this item (cannot be <code>null</code>).
   * 
   * @return the path of this item
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Returns the media type of this item.
   * 
   * @return the media type of this item, guaranteed non-null.
   */
  public String getMimeType()
  {
    return mimetype;
  }

  /**
   * @return the location in the package document where this item is declared
   */
  public EPUBLocation getLocation()
  {
    return location;
  }

  /**
   * Returns whether this package document item defines a fallback to another
   * item.
   * 
   * @return <code>true</code> iff this item has a fallback item.
   */
  public boolean hasFallback()
  {
    return hasFallback;
  }

  /**
   * Returns whether this item is a core media type resource, or has a core
   * media type resource in its fallback chain.
   * 
   * @return <code>true</code> iff a core media type resource was found in the
   *           fallback chain (can be itself)
   */
  public boolean hasCoreMediaTypeFallback()
  {
    return hasCoreMediaTypeFallback;
  }

  /**
   * Returns whether this item is itself an EPUB content document, or has an
   * EPUB content document in its fallback chain.
   * 
   * @return <code>true</code> iff an EPUB content document was found in the
   *           fallback chain (can be itself)
   */
  public boolean hasContentDocumentFallback()
  {
    return hasContentDocumentFallback;
  }

  /**
   * Returns the set of {@link Property} declared on this item or any
   * <code>itemref</code> pointing to this item.
   * 
   * @return the properties of this item, or an empty set if none is declared.
   */
  public Set<Property> getProperties()
  {
    return properties;
  }

  /**
   * Returns the zero-based position of this item in the spine, or {@code -1} if
   * this item is not in the spine.
   * 
   * @return the position of this item in the spine, or {@code -1} if this item
   *           is not in the spine.
   */
  public int getSpinePosition()
  {
    return spinePosition;
  }

  /**
   * Returns <code>true</code> iff this item is an NCX document.
   * 
   * @return <code>true</code> iff this item is an NCX document.
   */
  public boolean isNcx()
  {
    return ncx;
  }

  /**
   * Returns <code>true</code> iff this item is a scripted document.
   * 
   * @return <code>true</code> iff this item is a scripted document.
   */
  public boolean isScripted()
  {
    return scripted;
  }

  /**
   * Returns <code>true</code> iff this item is a Navigation Document.
   * 
   * @return <code>true</code> iff this item is an Navigation Document.
   */
  public boolean isNav()
  {
    return nav;
  }

  /**
   * Returns <code>true</code> iff this item is in the spine.
   * 
   * @return <code>true</code> iff this item is in the spine.
   */
  public boolean isInSpine()
  {
    return inSpine;
  }

  /**
   * Returns <code>true</code> iff this item is a spine item part of the linear
   * reading order, as declared by the <code>itemref/@linear</code> attribute.
   * 
   * @return <code>true</code> iff this item is in the spine and is linear.
   * @throws IllegalStateException
   *         if this item is not in the spine.
   */
  public boolean isLinear()
  {
    if (!inSpine)
    {
      throw new IllegalStateException("linear");
    }
    return linear;
  }

  /**
   * Returns <code>true</code> iff this item is a Fixed-Layout Document.
   * 
   * @return <code>true</code> iff this item is a Fixed-Layout Document.
   */
  public boolean isFixedLayout()
  {
    return fixedLayout;
  }

  /**
   * Returns <code>true</code> iff this item is a remote resource.
   * 
   * @return <code>true</code> iff this item is a remote resource.
   */
  public boolean hasDataURL()
  {
    return "data".equals(url.scheme());
  }

  /**
   * Returns <code>true</code> iff this item is a remote resource.
   * 
   * @return <code>true</code> iff this item is a remote resource.
   */
  public boolean isRemote()
  {
    return remote;
  }

  public String getMediaOverlay()
  {
    return mediaOverlay;
  }

  @Override
  public String toString()
  {
    return path + "[" + id + "]";
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    OPFItem other = (OPFItem) obj;
    if (id == null)
    {
      if (other.id != null) return false;
    }
    else if (!id.equals(other.id)) return false;
    if (url == null)
    {
      if (other.url != null) return false;
    }
    else if (!url.equals(other.url)) return false;
    return true;
  }

  /**
   * A builder for {@link OPFItem}
   */
  public static final class Builder
  {

    private String id;
    private URL url;
    private EPUBLocation location;
    private Optional<OCFContainer> container;
    private boolean remote = false;
    private String mimetype;
    private String fallback;
    private String fallbackStyle;
    private boolean hasContentDocumentFallback = false;
    private boolean hasCoreMediaTypeFallback = false;
    private boolean isFallbackResolved = false;
    private boolean ncx = false;
    private boolean linear = true;
    private int spinePosition = -1;
    private boolean fxl = false;
    private String mediaOverlay;
    private ImmutableSet.Builder<Property> propertiesBuilder = new ImmutableSet.Builder<Property>();

    public Builder id(String id)
    {
      this.id = id;
      return this;
    }

    public String id()
    {
      return id;
    }

    public Builder url(URL url)
    {
      this.url = url;
      return this;
    }

    public Builder location(EPUBLocation location)
    {
      this.location = location;
      return this;
    }

    public EPUBLocation location()
    {
      return location;
    }

    public Builder container(Optional<OCFContainer> container)
    {
      this.container = container;
      return this;
    }

    public Builder remote(boolean remote)
    {
      this.remote = remote;
      return this;
    }

    public Builder mimetype(String mimetype)
    {
      this.mimetype = Optional.fromNullable(mimetype).or("undefined").trim();
      return this;
    }

    public String mimetype()
    {
      return mimetype;
    }

    public Builder fallback(String fallback)
    {
      this.fallback = Strings.nullToEmpty(fallback).trim();
      return this;
    }

    public String fallback()
    {
      return fallback;
    }

    public boolean hasFallback()
    {
      return !fallback.isEmpty();
    }

    public Builder fallbackStyle(String fallbackStyle)
    {
      this.fallbackStyle = Strings.nullToEmpty(fallbackStyle).trim();
      return this;
    }

    public String fallbackStyle()
    {
      return fallbackStyle;
    }

    public boolean hasFallbackStyle()
    {
      return !fallbackStyle.isEmpty();
    }

    public Builder hasCoreMediaTypeFallback(boolean hasCoreMediaTypeFallback)
    {
      this.hasCoreMediaTypeFallback = hasCoreMediaTypeFallback;
      return this;
    }

    public boolean hasCoreMediaTypeFallback()
    {
      return this.hasCoreMediaTypeFallback;
    }

    public Builder hasContentDocumentFallback(boolean hasContentDocumentFallback)
    {
      this.hasContentDocumentFallback = hasContentDocumentFallback;
      return this;
    }

    public boolean hasContentDocumentFallback()
    {
      return hasContentDocumentFallback;
    }

    public Builder markResolved()
    {
      this.isFallbackResolved = true;
      return this;
    }

    public boolean isResolved()
    {
      return isFallbackResolved;
    }

    public Builder fixedLayout()
    {
      this.fxl = true;
      return this;

    }

    public Builder mediaOverlay(String path)
    {
      this.mediaOverlay = path;
      return this;
    }

    public Builder ncx()
    {
      this.ncx = true;
      return this;
    }

    public Builder nonlinear()
    {
      this.linear = false;
      return this;
    }

    public Builder inSpine(int position)
    {
      this.spinePosition = Preconditions.checkNotNull(position);
      return this;
    }

    public Builder properties(Set<Property> properties)
    {
      if (properties != null)
      {
        this.propertiesBuilder.addAll(properties);
      }
      return this;
    }
    
    public String toString() {
      return id;
    }

    /**
     * Builds a new immutable {@link OPFItem} from this builder.
     */
    public OPFItem build()
    {
      return new OPFItem(this);
    }
  }
}

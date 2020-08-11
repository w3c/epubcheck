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

import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Immutable representation of an item in a Package Document (OPF). Can
 * represent a <code>item</code> element or <code>link</code> elements pointing
 * to a container resource.
 */
public class OPFItem
{
  private final String id;
  private final String path;
  private final String mimetype;
  private final int lineNumber;
  private final int columnNumber;
  private final Optional<String> fallback;
  private final Optional<String> fallbackStyle;
  private final Set<Property> properties;
  private final boolean ncx;
  private final boolean inSpine;
  private final int spinePosition;
  private final boolean nav;
  private final boolean scripted;
  private final boolean linear;
  private final boolean fixedLayout;
  private final String mediaOverlay;

  private OPFItem(String id, String path, String mimetype, int lineNumber, int columnNumber,
      Optional<String> fallback, Optional<String> fallbackStyle, Set<Property> properties,
      boolean ncx, int spinePosition, boolean nav, boolean scripted, boolean linear, boolean fxl, String mediaOverlay)
  {
    this.id = id;
    this.path = path;
    this.mimetype = mimetype;
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.fallback = fallback;
    this.fallbackStyle = fallbackStyle;
    this.properties = properties;
    this.ncx = ncx;
    this.inSpine = spinePosition > -1;
    this.spinePosition = spinePosition;
    this.nav = nav;
    this.scripted = scripted;
    this.linear = linear;
    this.fixedLayout = fxl;
    this.mediaOverlay = mediaOverlay;
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
   * The path of this item (cannot be <code>null</code>).
   * 
   * @return the path of this item, relative to the container.
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
   * The line where this item is declared in the OPF.
   * 
   * @return
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * The column where this item is declared in the OPF.
   * 
   * @return
   */
  public int getColumnNumber()
  {
    return columnNumber;
  }

  /**
   * Returns an {@link Optional} containing the ID of the fallback item for this
   * item, if it has one.
   * 
   * @return An optional containing the ID of the fallback item for this item if
   *         it has one, or {@link Optional#absent()} otherwise.
   */
  public Optional<String> getFallback()
  {
    return fallback;
  }

  /**
   * Returns An {@link Optional} containing the ID of the fallback stylesheet for
   * this item, if it has one.
   * 
   * @return An optional containing the ID of the fallback stylesheet for this
   *         item if it has one, or {@link Optional#absent()} otherwise.
   */
  public Optional<String> getFallbackStyle()
  {
    return fallbackStyle;
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
   * @return the position of this item in the spine, or {@code -1} if this item is
   *         not in the spine.
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
   *           if this item is not in the spine.
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
    result = prime * result + ((path == null) ? 0 : path.hashCode());
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
    if (path == null)
    {
      if (other.path != null) return false;
    }
    else if (!path.equals(other.path)) return false;
    return true;
  }

  /**
   * A builder for {@link OPFItem}
   */
  public static final class Builder
  {

    private String id;
    private String path;
    private String mimeType;
    private int lineNumber;
    private int columnNumber;
    private String fallback = null;
    private String fallbackStyle = null;
    private boolean ncx = false;
    private boolean linear = true;
    private int spinePosition = -1;
    private boolean fxl = false;
    private String mediaOverlay;
    private ImmutableSet.Builder<Property> propertiesBuilder = new ImmutableSet.Builder<Property>();

    /**
     * Creates a new builder
     * 
     * @param id
     *          the item ID, can be <code>null</code>
     * @param path
     *          the item path,, cannot be <code>null</code>
     * @param mimeType
     *          the item media type, can be <code>null</code>
     * @param lineNumber
     *          the line number of the corresponding <code>item</code> or
     *          <code>link</code> element
     * @param columnNumber
     *          the column number of the corresponding <code>item</code> or
     *          <code>link</code> element
     */
    public Builder(String id, String path, String mimeType, int lineNumber, int columnNumber)
    {
      this.id = Preconditions.checkNotNull(id).trim();
      this.path = Preconditions.checkNotNull(path).trim();
      this.mimeType = Optional.fromNullable(mimeType).or("undefined").trim();
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
    }

    public Builder fallback(String fallback)
    {
      this.fallback = fallback;
      return this;
    }

    public Builder fallbackStyle(String fallbackStyle)
    {
      this.fallbackStyle = fallbackStyle;
      return this;
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

    /**
     * Builds a new immutable {@link OPFItem} from this builder.
     */
    public OPFItem build()
    {
      if (spinePosition < 0 || !linear)
      {
        this.propertiesBuilder.add(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR));
      }
      if (fxl)
      {
        this.propertiesBuilder
            .add(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.FIXED_LAYOUT));
      }
      Set<Property> properties = propertiesBuilder.build();

      return new OPFItem(id, path, mimeType, lineNumber, columnNumber,
          Optional.fromNullable(Strings.emptyToNull(Strings.nullToEmpty(fallback).trim())),
          Optional.fromNullable(Strings.emptyToNull(Strings.nullToEmpty(fallbackStyle).trim())),
          properties, ncx, spinePosition,
          properties.contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.NAV)),
          properties.contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.SCRIPTED)),
          linear, fxl, mediaOverlay);
    }
  }
}

package com.adobe.epubcheck.opf;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

/**
 * Represents a linked resource in a Package Document, i.e. a resource
 * referenced from a <code>link</code> element (either at the package level or
 * in collections).
 */
public final class LinkedResource
{

  private final Optional<String> id;
  private final URL url;
  private final Set<Property> rel;
  private final Optional<String> refines;
  private final Optional<String> mimetype;

  /**
   * Returns an {@link Optional} containing the value of the ID of the
   * <code>link</code> , or {@link Optional#absent()} if the element has no ID.
   */
  public Optional<String> getId()
  {
    return id;
  }

  /**
   * Returns the URL of the linked resource as defined in the <code>href</code>
   * attribute of the <code>link</code> element. Guaranteed non-null.
   */
  public URL getURL()
  {
    return url;
  }

  /**
   * Returns the URL of the linked resource document, i.e. the linked URL minus
   * a possible fragment. Guaranteed non-null.
   */
  public URL getDocumentURL()
  {
    try
    {
      return url.withFragment(null);
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError();
    }
  }

  /**
   * Returns the set of properties defining the nature of the resource, as
   * defined in the <code>rel</code> attribute.
   * 
   * @return the set of rel properties (may be empty for collection resources).
   *         Guaranteed non-null.
   */
  public Set<Property> getRel()
  {
    return rel;
  }

  /**
   * Returns an {@link Optional} containing the value of the
   * <code>refines</code> attribute of the <code>link</code> , or
   * {@link Optional#absent()} if the element has no such attribute.
   */
  public Optional<String> getRefines()
  {
    return refines;
  }

  /**
   * Returns an {@link Optional} containing the declared media type of this
   * resource as specified in the <code>media-type</code> attribute, or
   * {@link Optional#absent()} if the media type is not declared.
   */
  public Optional<String> getMimeType()
  {
    return mimetype;
  }

  private LinkedResource(Builder builder)
  {
    Preconditions.checkState(builder.url != null);

    this.url = builder.url;
    this.id = optional(builder.id);
    this.rel = builder.rel == null ? ImmutableSet.<Property> of()
        : ImmutableSet.copyOf(builder.rel);
    this.refines = optional(builder.refines);
    this.mimetype = optional(builder.mimetype);
  }

  // Returns an optional containing the given string
  // or absent if the string is null or empty or space-only
  private static Optional<String> optional(String string)
  {
    if (string == null || string.trim().isEmpty())
    {
      return Optional.absent();
    }
    else
    {
      return Optional.of(string.trim());
    }
  }

  /**
   * A builder for {@link LinkedResource}.
   */
  public static final class Builder
  {

    private final URL url;
    private String id = null;
    private Set<Property> rel = null;
    private String refines = null;
    private String mimetype = null;

    /**
     * Creates a new builder for a resource of the given URI (must not be null).
     */
    public Builder(URL url)
    {
      this.url = checkNotNull(url);
    }

    public Builder id(String id)
    {
      this.id = id;
      return this;
    }

    public Builder rel(Set<Property> rel)
    {
      this.rel = rel;
      return this;
    }

    public Builder refines(String refines)
    {
      this.refines = refines;
      return this;
    }

    public Builder mimetype(String mimetype)
    {
      this.mimetype = mimetype;
      return this;
    }

    /**
     * Returns a newly created {@link LinkedResource}.
     */
    public LinkedResource build()
    {
      return new LinkedResource(this);
    }
  }

}

package com.adobe.epubcheck.opf;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a linked resource in a Package Document, i.e. a resource
 * referenced from a <code>link</code> element (either at the package level or
 * in collections).
 */
public final class LinkedResource
{

  private final Optional<String> id;
  private final String uri;
  private final String path;
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
   * Returns the URI of the linked resource as defined in the <code>href</code>
   * attribute of the <code>link</code> element. Guaranteed non-null.
   */
  public String getURI()
  {
    return uri;
  }

  /**
   * Returns the "path" of the linked resource, i.e. its URI minus a possible
   * fragment. Guaranteed non-null.
   */
  public String getPath()
  {
    return path;
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

  private LinkedResource(Optional<String> id, String uri, String resource, Set<Property> rel,
      Optional<String> refines, Optional<String> mimetype)
  {
    this.id = checkNotNull(id);
    this.uri = checkNotNull(uri);
    this.path = checkNotNull(resource);
    this.rel = checkNotNull(rel);
    this.refines = checkNotNull(refines);
    this.mimetype = checkNotNull(mimetype);
  }

  /**
   * A builder for {@link LinkedResource}.
   */
  public static final class Builder
  {

    private final String uri;
    private String id = null;
    private Set<Property> rel = null;
    private String refines = null;
    private String mimetype = null;

    /**
     * Creates a new builder for a resource of the given URI (must not be null).
     */
    public Builder(String uri)
    {
      this.uri = checkNotNull(uri).trim();
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
      return new LinkedResource(optional(id), uri, uri.replaceFirst("#.*$", ""),
          rel == null ? ImmutableSet.<Property> of() : ImmutableSet.copyOf(rel), optional(refines),
          optional(mimetype));
    }

    // Returns an optional containing the given string
    // or absent if the string is null or empty or space-only
    private Optional<String> optional(String string)
    {
      return Optional.fromNullable(Strings.emptyToNull(Strings.nullToEmpty(id).trim()));
    }
  }

}

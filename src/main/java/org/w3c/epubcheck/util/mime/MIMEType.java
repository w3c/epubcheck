package org.w3c.epubcheck.util.mime;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * Represents a MIME type as define in MIME Sniffing
 * See https://mimesniff.spec.whatwg.org/#mime-type
 * 
 * Conforming to living standard update: 17 July 2026
 */
public final class MIMEType
{

  /**
   * Returns a MIME type for the given type and subtype.
   *
   * This is a direct builder method (no parsing id done). The caller is
   * responsible for the conformity of the specified arguments.
   *
   * If `type` and `subtype` are conforming (i.e. solely contains HTTP token
   * code points), then the returned MIME type is equal to the result of
   * `MIMEType.parse(type+"/"+subtype)`.
   */
  public static MIMEType of(String type, String subtype)
  {
    return new Builder().type(type).subtype(subtype).build();
  }

  /**
   * Returns the MIME type result of parsing the string.
   */
  public static MIMEType parse(String string)
  {
    return new MIMETypeParser().parse(string);
  }

  /**
   * Normalizes a MIME type string, using parsing and serialization.
   *
   * @return a normalized valid MIME type string, or the empty string if parsing
   *           the given string was a failure.
   */
  public static String normalize(String string)
  {
    MIMEType mimetype = parse(string);
    return (mimetype != null) ? mimetype.toString() : "";
  }

  protected final static class Builder
  {

    private String type = null;
    private String subtype = null;
    private final LinkedHashMap<String, String> params = new LinkedHashMap<>(5);

    public Builder type(String type)
    {
      this.type = type;
      return this;
    }

    public Builder subtype(String subtype)
    {
      this.subtype = Preconditions.checkNotNull(subtype);
      return this;
    }

    public Builder param(String name, String value)
    {
      params.putIfAbsent(Preconditions.checkNotNull(name), Preconditions.checkNotNull(value));
      return this;
    }

    @SuppressWarnings("null")
    public MIMEType build()
    {
      Preconditions.checkState(type != null);
      Preconditions.checkState(subtype != null);
      return new MIMEType(type, subtype, ImmutableMap.copyOf(params));
    }
  }

  private final String type;
  private final String subtype;
  private final Map<String, String> params;

  private String essence;
  private String serialization;

  private MIMEType(String type, String subtype, Map<String, String> parameters)
  {
    Preconditions.checkArgument(Preconditions.checkNotNull(type).length() > 0);
    Preconditions.checkArgument(Preconditions.checkNotNull(subtype).length() > 0);
    this.type = type;
    this.subtype = subtype;
    this.params = Preconditions.checkNotNull(parameters);
  }

  public String type()
  {
    return type;
  }

  public String subtype()
  {
    return subtype;
  }

  public String essence()
  {
    return (essence != null) ? essence : (essence = type + '/' + subtype);
  }

  public Map<String, String> parameters()
  {
    return params;
  }

  /**
   * Returns the MIME type serialization
   */
  @Override
  public String toString()
  {
    return (serialization != null) ? serialization
        : (serialization = MIMETypeSerializer.serialize(this));
  }

  public MIMEType filter(String... paramNames)
  {
    if (params.isEmpty()) return this;

    Set<String> keys = Arrays.stream(paramNames).filter(p -> !Strings.isNullOrEmpty(p))
        .collect(Collectors.toSet());

    if (keys.isEmpty()) return this;

    Builder builder = new Builder().type(type).subtype(subtype);
    params.entrySet().stream()
        .filter(p -> keys.contains(p.getKey()))
        .forEachOrdered(p -> builder.param(p.getKey(), p.getValue()));
    return builder.build();
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(type, subtype, params);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MIMEType other = (MIMEType) obj;
    return Objects.equals(type, other.type)
        && Objects.equals(subtype, other.subtype)
        && Objects.equals(params, other.params);
  }
}

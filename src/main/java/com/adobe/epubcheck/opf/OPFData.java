package com.adobe.epubcheck.opf;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.collect.Sets;

public final class OPFData
{

  public static class OPFDataBuilder
  {

    private EPUBVersion version;
    private Set<PublicationType> types = EnumSet.noneOf(PublicationType.class);
    private String uniqueId;

    public OPFData build()
    {
      return new OPFData(this);
    }

    public OPFDataBuilder withUniqueId(String uniqueId)
    {
      this.uniqueId = uniqueId;
      return this;
    }

    public OPFDataBuilder withVersion(EPUBVersion version)
    {
      this.version = version;
      return this;
    }

    public OPFDataBuilder withType(String type)
    {
      try
      {
        if (type != null) {
          this.types.add(PublicationType.valueOf(type.toUpperCase(Locale.ROOT)));
        }
      } catch (Exception e)
      {
        // ignore, the type is not added if not known
      }
      return this;
    }

  }

  private final EPUBVersion version;
  private final Set<PublicationType> types;
  private final String uniqueId;

  private OPFData(OPFDataBuilder builder)
  {
    this.version = builder.version;
    this.uniqueId = builder.uniqueId;
    this.types = Sets.immutableEnumSet(builder.types);
  }

  public EPUBVersion getVersion()
  {
    return version;
  }

  public Set<PublicationType> getTypes()
  {
    return types;
  }

  public String getUniqueIdentifier()
  {
    // Note: can be null, correctness is checked in OPFHandler
    return uniqueId;
  }
}

package com.adobe.epubcheck.opf;

import java.util.Set;

import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class OPFData
{
  public static final String DC_TYPE_DICT = "dictionary";
  public static final String DC_TYPE_EDUPUB = "edupub";
  public static final String DC_TYPE_INDEX = "index";
  public static final String DC_TYPE_PREVIEW = "preview";

  public static class OPFDataBuilder
  {

    private EPUBVersion version;
    private Set<String> types = Sets.newHashSet();
    private String uniqueId;

    public OPFData build()
    {
      return new OPFData(version, uniqueId, types);
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
      this.types.add(type);
      return this;
    }

  }

  private final EPUBVersion version;
  private final Set<String> types;
  private final String uniqueId;

  private OPFData(EPUBVersion version, String uniqueId, Set<String> types)
  {
    this.version = version;
    this.uniqueId = uniqueId;
    this.types = ImmutableSet.copyOf(types);
  }

  public EPUBVersion getVersion()
  {
    return version;
  }

  public Set<String> getTypes()
  {
    return types;
  }

  public String getUniqueIdentifier()
  {
    // Note: can be null, correctness is checked in OPFHandler
    return uniqueId;
  }
}

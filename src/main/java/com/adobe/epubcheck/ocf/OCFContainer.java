package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.w3c.epubcheck.url.URLUtils;

import com.adobe.epubcheck.ocf.encryption.EncryptionFilter;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public final class OCFContainer implements GenericResourceProvider
{
  static final class Builder
  {

    private final URL rootURL;
    private Map<URL, OCFResource> resources = new LinkedHashMap<>();
    private ImmutableMap.Builder<URL, EncryptionFilter> encryptionFilters = ImmutableMap.builder();

    public Builder()
    {
      try
      {
        this.rootURL = URL.parse("https://" + UUID.randomUUID() + ".epubcheck.w3c.org");
      } catch (GalimatiasParseException e)
      {
        throw new AssertionError("Could not create root URL");
      }
    }

    public Builder addResource(OCFResource resource)
    {
      Preconditions.checkArgument(resource != null, "resource must not be null");
      try
      {
        resources.put(rootURL.resolve(URLUtils.encodePath(resource.getPath())), resource);
      } catch (GalimatiasParseException e)
      {
        throw new IllegalArgumentException(
            "Could not create container URL of " + resource.getPath(), e);
      }
      return this;
    }

    public OCFContainer build()
    {
      return new OCFContainer(this);
    }

    public void addEncryption(URL resource, EncryptionFilter filter)
    {
      Preconditions.checkArgument(resource != null, "resource must not be null");
      Preconditions.checkArgument(filter != null, "filter must not be null");
      Preconditions.checkArgument(resources.containsKey(resource),
          resource + " was not found in the container");
      encryptionFilters.put(resource, filter);
    }
  }

  private final URL rootURL;
  private final ImmutableMap<URL, OCFResource> resources;
  private final ImmutableMap<URL, EncryptionFilter> encryptionFilters;

  public OCFContainer(Builder builder)
  {
    this.rootURL = builder.rootURL;
    this.resources = ImmutableMap.copyOf(builder.resources);
    this.encryptionFilters = builder.encryptionFilters.build();
  }

  public boolean contains(URL resource)
  {
    return resources.containsKey(resource);
  }

  @Override
  public InputStream openStream(URL url)
    throws IOException
  {
    OCFResource resource = resources.get(url);
    if (resource == null)
    {
      throw new IllegalArgumentException("Resource not found: " + url);
    }
    // FIXME 2022 filter with encryption
    return resource.openStream();
  }

  public boolean canDecrypt(URL url)
  {
    OCFResource resource = resources.get(url);
    if (resource == null)
    {
      throw new IllegalArgumentException("Resource not found: " + url);
    }
    EncryptionFilter filter = encryptionFilters.get(url);
    return (filter == null || filter.canDecrypt());
  }

  public Set<URL> getResources()
  {
    return resources.keySet();
  }

  public URL getRootURL()
  {
    return rootURL;
  }

  public String relativize(URL url)
  {
    return rootURL.relativize(url);
  }

  public boolean isRemote(URL url)
  {
    Preconditions.checkArgument(url != null, "URL is null");
    if (contains(url))
    {
      return false;
    }
    else
    {
      return !(URLUtils.isSameOrigin(url, rootURL));
    }
  }

}

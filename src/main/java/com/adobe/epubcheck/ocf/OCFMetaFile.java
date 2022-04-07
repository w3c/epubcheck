package com.adobe.epubcheck.ocf;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

enum OCFMetaFile
{
  MIMETYPE("mimetype"),
  CONTAINER("META-INF/container.xml"),
  ENCRYPTION("META-INF/encryption.xml"),
  METADATA("META-INF/metadata.xml"),
  SIGNATURES("META-INF/signatures.xml");

  private final String path;

  private OCFMetaFile(String path)
  {
    this.path = path;
  }

  public URL asURL(OCFContainer container)
  {
    Preconditions.checkArgument(container != null, "container must not be null");
    assert container != null;
    try
    {
      return container.getRootURL().resolve(path);
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError("Could not build meta file URL", e);
    }
  }

  public EPUBLocation asLocation(OCFContainer container)
  {
    Preconditions.checkArgument(container != null, "container must not be null");
    return EPUBLocation.of(asURL(container), container);
  }

  public boolean isPresent(OCFContainer container)
  {
    Preconditions.checkArgument(container != null, "container must not be null");
    return container.contains(asURL(container));
  }

  public String asPath()
  {
    return path;
  }

}
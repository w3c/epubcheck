package org.w3c.epubcheck.core.references;

import java.util.List;

import org.w3c.epubcheck.core.references.Reference.Type;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public final class ReferenceRegistry
{

  private final OCFContainer container;
  private final ImmutableList.Builder<Reference> references = ImmutableList.builder();
  private final ResourceRegistry resourceRegistry;

  public ReferenceRegistry(OCFContainer container, ResourceRegistry resourceRegistry)
  {
    Preconditions.checkArgument(container != null);
    Preconditions.checkArgument(resourceRegistry != null);
    this.container = container;
    this.resourceRegistry = resourceRegistry;
  }

  public List<Reference> asList()
  {
    return references.build();
  }

  public boolean isReferenced(URL resource)
  {
    return references.build().stream().anyMatch(ref -> ref.url.equals(resource));
  }

  public void registerReference(URL url, Type type, EPUBLocation location)
  {
    registerReference(url, type, location, false);
  }

  public void registerReference(URL url, Type type, EPUBLocation location,
      boolean hasIntrinsicFallback)
  {
    if (url == null) return;

    // Remove query component of local URLs
    if (url.query() != null && !container.isRemote(url))
    {
      try
      {
        url = url.withQuery(null);
      } catch (GalimatiasParseException e)
      {
        new AssertionError("could not remove URL query");
      }
    }

    // Create and register a new reference
    Reference xref = new Reference(url, type, location, hasIntrinsicFallback);
    references.add(xref);

    // If it is a data URL, also register a new resource
    // as the URL may not have been listed in the manifest
    if ("data".equals(url.scheme()))
    {
      resourceRegistry.registerResource(url, URLUtils.getDataURLType(url));
    }
  }

  /**
   * Returns if any references to the given resources were registered.
   * 
   * @param resousrce
   *        the URL to a publication resource
   * @return <code>true</code> iff a reference to the given resource was found
   */
  public boolean hasReferencesTo(URL resource)
  {
    return (resource != null)
        && references.build().stream().anyMatch(ref -> resource.equals(ref.targetResource));
  }

}

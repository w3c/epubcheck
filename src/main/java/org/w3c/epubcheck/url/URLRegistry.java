package org.w3c.epubcheck.url;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.opf.OPFItem;

import io.mola.galimatias.URL;

// TODO decide:
// - where to create this
// - where to store this (in validation context? in Publication?)
public class URLRegistry
{

  // private final Set<Reference> references = new LinkedHashSet<>();
  //
  // public void registerReference(String string, EPUBLocation location, Type
  // type)
  // {
  // // parse URL
  // // TODO configure URL error handler (see Galimatias doc)
  // URL url = URL.parse(location.getURL(), string);
  //
  // // check data URL
  // if ("data".equals(url.scheme()))
  // {
  // // TODO check data URLs have fallback, issue #1239
  // }
  //
  // // TODO remove query string for in-container URLs
  //
  // // else register reference
  // references.add(new Reference(url, location, type));
  //
  // }
  //
  // Set<Reference> getReferences()
  // {
  // return ImmutableSet.copyOf(references);
  // }

  public boolean isReferenced(URL resource)
  {
    return false;
  }

  public void registerID(String id, Reference.Type refType, URL documentURL, EPUBLocation location)
  {

  }

  public void registerReference(String reference, Reference.Type refType, URL documentURL,
      EPUBLocation location)
  {

  }

  public void registerResource(OPFItem item)
  {

  }
}

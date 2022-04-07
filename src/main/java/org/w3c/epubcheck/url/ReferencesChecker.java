package org.w3c.epubcheck.url;

import java.util.HashSet;
import java.util.Set;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.opf.ValidationContext;

import io.mola.galimatias.URL;

public class ReferencesChecker implements Checker
{


  private final Iterable<Reference> references;
  private final ValidationContext context;

  public ReferencesChecker(ValidationContext context, Iterable<Reference> references)
  {
    this.context = context;
    this.references = references;
  }

  @Override
  public void check()
  {
    // TODO init
    Set<URL> undeclared = new HashSet<>();
    for (Reference reference : references)
    {
      // TODO add type-specific checks
      checkReference(reference, undeclared);
    }
    // TODO post-loop checks
  }

  private void checkReference(Reference reference, Set<URL> undeclared)
  {
//    // TODO get item matching reference
//    OPFItem item = null;
//
//    // TODO check remote resources
//    if (reference.resource.isRemote())
//    // or if (container.isRemote(reference.url)) ???
//    {
//      // if (allowed condition 1) {
//      // // do nothing
//      // } else if allowed condition 2() {
//      // // do nothing
//      // } else {
//      //// report RSC_006
//      // }
//    }
//
//    // Check undeclared reference
//    if (item == null)
//    {
//      // TODO introduce "notfound" set to check which resources have been tested
//      if (!container.contains(reference.resource) && !reference.resource.isRemote())
//      {
//        // if EPUB 3 and reference.type = link, report RSC_007w
//        // else report RSC_007
//      }
//      // TODO not sure "else" is required:
//      // We can report both RSC-007 and RSC-008 for the same reference
//      else if (true
//      // if resource has not aleady been checked
//      // and ref is not a link
//      // and ref is not a remote hyperlink
//      )
//      {
//        // TODO report RSC-008
//      }
//      return;
//    }
//
//    // Type-specific checks
//
//    // Check fragment
//    Preconditions.checkState(item != null);
//    if (reference.url.fragment() != null)
//    {
//
//    }
  }
}

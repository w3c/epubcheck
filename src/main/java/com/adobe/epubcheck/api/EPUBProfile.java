package com.adobe.epubcheck.api;

import java.util.Set;

import com.adobe.epubcheck.opf.PublicationType;

public enum EPUBProfile
{
  DEFAULT,
  IDX,
  DICT,
  EDUPUB,
  PREVIEW;

  /**
   * Checks this validation profile against the dc:type(s) declared in an OPF
   * and returns a possibly overriden profile.
   * <p>
   * For instance, if the publication has a 'edupub' dc:type and the DEFAULT
   * validation profile is given, the EDUPUB profile will be returned instead.
   * </p>
   * <p>
   * If the given validation profile is modified, report an INFO message
   * OPF_064.
   * </p>
   * 
   * @param The publication's dc:type(s).
   * @return This profile if it's compatible with the OPF dc:type(s), or
   *         else a compatible non-null validation profile.
   */
  public EPUBProfile makeTypeCompatible(Set<PublicationType> pubTypes)
  {
    if (pubTypes.contains(PublicationType.DICTIONARY))
    {
      return EPUBProfile.DICT;
    }
    else if (pubTypes.contains(PublicationType.EDUPUB))
    {
      return EPUBProfile.EDUPUB;
    }
    else if (pubTypes.contains(PublicationType.INDEX))
    {
      return EPUBProfile.IDX;
    }
    else if (pubTypes.contains(PublicationType.PREVIEW))
    {
      return EPUBProfile.PREVIEW;
    }
    else
    {
      return this;
    }
  }

  public PublicationType matchingType()
  {
    switch (this)
    {
    case DICT:
      return PublicationType.DICTIONARY;
    case EDUPUB:
      return PublicationType.EDUPUB;
    case IDX:
      return PublicationType.INDEX;
    case PREVIEW:
      return PublicationType.PREVIEW;
    default:
      return PublicationType.EPUB;
    }
  }
}

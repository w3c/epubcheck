package com.adobe.epubcheck.api;

import java.util.Set;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFData;
import com.google.common.collect.ImmutableSet;

public enum EPUBProfile
{
  DEFAULT,
  IDX,
  DICT,
  EDUPUB,
  PREVIEW;

  /**
   * Checks a given validation profile against the dc:type(s) declared in an OPF
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
   * @param profile
   *          a validation profile.
   * @param opfData
   *          the parsed OPF data (contains the publication's dc:type(s)).
   * @param path
   *          the path to use for reporting messages.
   * @param report
   *          the message report.
   * @return The given profile if it's compatible with the OPF dc:type(s), or
   *         else a compatible non-null validation profile.
   */
  public static EPUBProfile makeOPFCompatible(EPUBProfile profile, OPFData opfData, String path,
      Report report)
  {

    Set<String> pubTypes = opfData != null ? opfData.getTypes() : ImmutableSet.<String> of();
    if (pubTypes.contains(OPFData.DC_TYPE_DICT) && profile != EPUBProfile.DICT)
    {
      report.message(MessageId.OPF_064, EPUBLocation.create(path), OPFData.DC_TYPE_DICT,
          EPUBProfile.DICT);
      return EPUBProfile.DICT;
    }
    else if (pubTypes.contains(OPFData.DC_TYPE_EDUPUB) && profile != EPUBProfile.EDUPUB)
    {
      report.message(MessageId.OPF_064, EPUBLocation.create(path), OPFData.DC_TYPE_EDUPUB,
          EPUBProfile.EDUPUB);
      return EPUBProfile.EDUPUB;
    }
    else if (pubTypes.contains(OPFData.DC_TYPE_INDEX) && profile != EPUBProfile.IDX)
    {
      report.message(MessageId.OPF_064, EPUBLocation.create(path), OPFData.DC_TYPE_INDEX,
          EPUBProfile.IDX);
      return EPUBProfile.IDX;
    }
    else if (pubTypes.contains(OPFData.DC_TYPE_PREVIEW) && profile != EPUBProfile.PREVIEW)
    {
      report.message(MessageId.OPF_064, EPUBLocation.create(path), OPFData.DC_TYPE_PREVIEW,
          EPUBProfile.PREVIEW);
      return EPUBProfile.PREVIEW;
    }
    else
    {
      return profile != null ? profile : EPUBProfile.DEFAULT;
    }
  }
}

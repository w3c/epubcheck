package org.w3c.epubcheck.util.text;

import com.google.common.base.Preconditions;
import com.ibm.icu.text.CaseMap;
import com.ibm.icu.text.Normalizer2;

public final class UnicodeUtils
{

  private static final Normalizer2 NFD_NORMALIZER = Normalizer2.getNFCInstance();
  private static final CaseMap.Fold CASE_FOLDER = CaseMap.fold();

  private UnicodeUtils()
  {
    // static utility class
  }

  /**
   * Applies Unicode Canonical Case Fold Normalization as defined in
   * https://www.w3.org/TR/charmod-norm/#CanonicalFoldNormalizationStep
   * 
   * This applies, in sequence: - canonical decomposition (NFD) - case folding
   * 
   * Note that the result is **not** recomposed (NFC), i.e. the optional
   * post-folding NFC normalization is not applied.
   * 
   * In other words, the result is suitable for string comparison for
   * case-insensitive string comparison, but not for display.
   * 
   * @param string
   *          the string to normalize
   * @return the string normalized by applying NFD then case folding
   */
  public static String canonicalCaseFold(String string)
  {
    Preconditions.checkArgument(string != null);
    return CASE_FOLDER.apply(NFD_NORMALIZER.normalize(string));
  }
}

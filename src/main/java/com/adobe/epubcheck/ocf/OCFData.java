package com.adobe.epubcheck.ocf;

import java.util.List;

import com.google.common.base.Optional;

public interface OCFData
{
  static final String containerEntry = "META-INF/container.xml";
  static final String encryptionEntry = "META-INF/encryption.xml";
  static final String metadataEntry = "META-INF/metadata.xml";
  static final String signatureEntry = "META-INF/signatures.xml";

  /**
   * @return the full paths of the root files of the container for the given
   *         media type.
   */
  public List<String> getEntries(String type);

  /**
   * @return the full paths of all the root files of the container
   */
  public List<String> getEntries();

  /**
   * @return the full path of the Rendition Mapping Document, or
   *         {@link Optional#absent()} is no such document is declared.
   */
  public Optional<String> getMapping();
}

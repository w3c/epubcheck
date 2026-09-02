package com.adobe.epubcheck.vocab;

/**
 * Holds info about an EPUB vocabulary property, specifically whether it's
 * disallowed (ERROR) or deprecate (WARNING).
 */
public enum PropertyStatus
{
  /**
   * The default status of properties defined in EPUB 3.
   */
  ALLOWED,

  /**
   * The status of properties that are allowed but deprecated.
   */
  DEPRECATED,

  /**
   * The status of properties that are allowed but outdated.
   */
  OUTDATED,

  /**
   * The status of properties that are not allowed in Content Documents
   * (documents of type 'application/xhtml+xml')
   */
  DISALLOWED_IN_XHTML;

  interface Holder
  {
    public PropertyStatus getStatus();
  }

}

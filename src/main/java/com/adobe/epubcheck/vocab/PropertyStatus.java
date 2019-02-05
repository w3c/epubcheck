package com.adobe.epubcheck.vocab;

import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;

/**
 * Holds info about an EPUB vocabulary property, specifically whether it's
 * disallowed (ERROR) or deprecate (WARNING).
 */
public interface PropertyStatus
{
  public boolean isAllowed(ValidationContext context);

  public boolean isDeprecated();

  /**
   * The 'allowed' status (for properties that are neither disallowed or
   * deprecated).
   */
  public static final PropertyStatus ALLOWED = new PropertyStatus()
  {
    @Override
    /**
     * Always returns <code>false</code>.
     */
    public boolean isDeprecated()
    {
      return false;
    }

    /**
     * Always returns <code>true</code>.
     */
    @Override
    public boolean isAllowed(ValidationContext context)
    {
      return true;
    }
  };

  /**
   * The 'deprecated' status (for properties that are allowed but deprecated).
   */
  public static final PropertyStatus DEPRECATED = new PropertyStatus()
  {
    @Override
    /**
     * Always returns <code>true</code>.
     */
    public boolean isDeprecated()
    {
      return true;
    }

    @Override
    /**
     * Always returns <code>true</code>.
     */
    public boolean isAllowed(ValidationContext context)
    {
      return true;
    }
  };

  /**
   * The status of properties that are disallowed on Content Documents (documents
   * of type 'application/xhtml+xml')
   */
  public static final PropertyStatus DISALLOWED_ON_CONTENT_DOCS = new PropertyStatus()
  {
    @Override
    /**
     * Always returns <code>false</code>.
     */
    public boolean isDeprecated()
    {
      return false;
    }

    @Override
    /**
     * Returns <code>false</code> iff the context is an XHTML document.
     */
    public boolean isAllowed(ValidationContext context)
    {
      return !"application/xhtml+xml".equals(Preconditions.checkNotNull(context).mimeType);

    }
  };

}

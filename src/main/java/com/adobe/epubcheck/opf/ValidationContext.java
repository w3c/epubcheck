package com.adobe.epubcheck.opf;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * Holds various contextual objects used during validation. This validation
 * context is used by both {@link ContentChecker} and {@link DocumentValidator}
 * instances.
 * 
 */
public final class ValidationContext
{
  /**
   * The path to the validated resource. Guaranteed non-null.
   */
  public final String path;
  /**
   * The media type of the validated resource. Guaranteed non-null.
   */
  public final String mimeType;
  /**
   * The EPUB version being used for validation. Guaranteed non-null.
   */
  public final EPUBVersion version;
  /**
   * The validation profile in use (e.g. DEFAULT, EDUPUB, etc.). Guaranteed
   * non-null.
   */
  public final EPUBProfile profile;
  /**
   * The report object used to log validation messages. Guaranteed non-null.
   */
  public final Report report;

  /**
   * Used to open resource streams. Guaranteed non-null.
   */
  public final GenericResourceProvider resourceProvider;
  /**
   * The OCF Package the resource being validated belongs to. Is absent for
   * single-file validations.
   */
  public final Optional<OCFPackage> ocf;
  /**
   * The cross-reference checker, absent for single-file validation.
   */
  public final Optional<XRefChecker> xrefChecker;
  /**
   * The set of 'dc:type' values declared at the OPF level. Guaranteed non-null,
   * can be empty.
   */
  public final Set<String> pubTypes;
  /**
   * A space-separate list of properties associated to the resource being
   * validated.
   */
  public final String properties;

  private ValidationContext(String path, String mimeType, EPUBVersion version, EPUBProfile profile,
      Report report, GenericResourceProvider resourceProvider, Optional<OCFPackage> ocf,
      Optional<XRefChecker> xrefChecker, Set<String> pubTypes, String properties)
  {
    super();
    this.path = path;
    this.mimeType = mimeType;
    this.version = version;
    this.profile = profile;
    this.report = report;
    this.resourceProvider = resourceProvider;
    this.ocf = ocf;
    this.xrefChecker = xrefChecker;
    this.pubTypes = pubTypes;
    this.properties = properties;
  }

  /**
   * Builds a new {@link ValidationContext} from various member objects or by
   * copying a parent context.
   *
   */
  public static final class ValidationContextBuilder
  {
    private String path = null;
    private String mimeType = null;
    private EPUBVersion version = null;
    private EPUBProfile profile = null;
    private Report report = null;

    private GenericResourceProvider resourceProvider = null;
    private OCFPackage ocf = null;
    private XRefChecker xrefChecker = null;
    private Set<String> pubTypes = null;
    private String properties = null;

    public ValidationContextBuilder()
    {
    }

    public ValidationContextBuilder(ValidationContext context)
    {
      copy(context);
    }

    public ValidationContextBuilder copy(ValidationContext context)
    {
      path = context.path;
      mimeType = context.mimeType;
      version = context.version;
      profile = context.profile;
      report = context.report;
      resourceProvider = context.resourceProvider;
      ocf = context.ocf.orNull();
      xrefChecker = context.xrefChecker.orNull();
      pubTypes = context.pubTypes;
      properties = context.properties;
      return this;
    }

    public ValidationContextBuilder path(String path)
    {
      this.path = path;
      return this;
    }

    public ValidationContextBuilder mimetype(String mimetype)
    {
      this.mimeType = mimetype;
      return this;
    }

    public ValidationContextBuilder version(EPUBVersion version)
    {
      this.version = version;
      return this;
    }

    public ValidationContextBuilder profile(EPUBProfile profile)
    {
      this.profile = profile;
      return this;
    }

    public ValidationContextBuilder report(Report report)
    {
      this.report = report;
      return this;
    }

    public ValidationContextBuilder resourceProvider(GenericResourceProvider resourceProvider)
    {
      this.resourceProvider = resourceProvider;
      return this;
    }

    public ValidationContextBuilder ocf(OCFPackage ocf)
    {
      this.ocf = ocf;
      return this;
    }

    public ValidationContextBuilder xrefChecker(XRefChecker xrefChecker)
    {
      this.xrefChecker = xrefChecker;
      return this;
    }

    public ValidationContextBuilder pubTypes(Set<String> pubTypes)
    {
      this.pubTypes = pubTypes;
      return this;
    }

    public ValidationContextBuilder properties(String properties)
    {
      this.properties = properties;
      return this;
    }

    public ValidationContext build()
    {
      resourceProvider = (resourceProvider == null && ocf != null) ? ocf : resourceProvider;
      checkNotNull(resourceProvider);
      checkNotNull(report);
      return new ValidationContext(Strings.nullToEmpty(path), Strings.nullToEmpty(mimeType),
          version != null ? version : EPUBVersion.Unknown, profile != null ? profile
              : EPUBProfile.DEFAULT, report, resourceProvider, Optional.fromNullable(ocf),
          Optional.fromNullable(xrefChecker), pubTypes != null ? ImmutableSet.copyOf(pubTypes)
              : ImmutableSet.<String> of(), Strings.nullToEmpty(properties));
    }
  }

}

package com.adobe.epubcheck.opf;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.core.references.ReferenceRegistry;
import org.w3c.epubcheck.core.references.ResourceRegistry;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport;
import com.adobe.epubcheck.api.LocalizableReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.overlay.OverlayTextChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

/**
 * Holds various contextual objects used during validation. This validation
 * context is notalby used by {@link Checker} instances.
 * 
 */
public final class ValidationContext
{

  /**
   * The URL of the validated resource. Guaranteed non-null.
   */
  public final URL url;
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
   * The locale used to log validation messages. Guaranteed non-null.
   */
  public final Locale locale;
  /**
   * Used to report some features of the validated resource, without logging.
   * Guaranteed non-null.
   */
  public final FeatureReport featureReport;

  /**
   * Used to open resource streams. Guaranteed non-null.
   */
  public final GenericResourceProvider resourceProvider;
  /**
   * The Package Document item for the validated resource. Is absent if there is
   * no item representing this resource in the Package Document.
   */
  public final Optional<OPFItem> opfItem;
  /**
   * The OCF Package the resource being validated belongs to. Is absent for
   * single-file validations.
   */
  public final Optional<OCFContainer> container;
  /**
   * The publication resource registry, absent for single-file validation.
   */
  public final Optional<ResourceRegistry> resourceRegistry;
  /**
   * The references registry, absent for single-file validation
   */
  public final Optional<ReferenceRegistry> referenceRegistry;
  /**
   * The src checker for media overlay text elements, absent for single-file
   * validation
   */
  public final Optional<OverlayTextChecker> overlayTextChecker;
  /**
   * The set of 'dc:type' values declared at the OPF level. Guaranteed non-null,
   * can be empty.
   */
  public final Set<PublicationType> pubTypes;
  /**
   * The set of properties associated to the resource being validated.
   */
  public final Set<Property> properties;

  private ValidationContext(ValidationContextBuilder builder)
  {
    // FIXME 2022 add a default report if not provided
    // FIXME 2022 add a better default resource provider

    Preconditions.checkState(builder.url != null, "URL must be set");
    Preconditions.checkState(builder.report != null, "report must be set");
    this.url = builder.url;
    this.container = Optional.fromNullable(builder.container);
    this.resourceRegistry = Optional.fromNullable(builder.resourceRegistry);
    this.referenceRegistry = Optional.fromNullable(builder.referenceRegistry);
    this.mimeType = Strings.nullToEmpty(builder.mimeType);
    this.version = Optional.fromNullable(builder.version).or(EPUBVersion.Unknown);
    this.profile = Optional.fromNullable(builder.profile).or(EPUBProfile.DEFAULT);
    this.report = builder.report;
    this.locale = MoreObjects.firstNonNull(
        (report instanceof LocalizableReport) ? ((LocalizableReport) report).getLocale() : null,
        Locale.getDefault());
    this.featureReport = Optional.fromNullable(builder.featureReport).or(new FeatureReport());
    this.resourceProvider = Iterables.find(
        Arrays.asList(builder.container, builder.resourceProvider, new URLResourceProvider()),
        Predicates.notNull());
    this.opfItem = Optional.fromNullable((builder.resourceRegistry != null)
        ? builder.resourceRegistry.getOPFItem(builder.url).orElse(null)
        : null);
    this.overlayTextChecker = Optional.fromNullable(builder.overlayTextChecker);
    this.pubTypes = (builder.pubTypes != null) ? Sets.immutableEnumSet(builder.pubTypes)
        : EnumSet.noneOf(PublicationType.class);
    this.properties = builder.properties.build();
    this.path = computePath();
  }

  // FIXME 2022 document and test
  private String computePath()
  {
    if (container.isPresent() && !container.get().isRemote(url))
    {
      if (url.path() != null && !url.path().isEmpty())
      {
        return url.path().substring(1);
      }
      else
      {
        return "";
      }
    }
    else if ("file".equals(url.scheme()))
    {
      return URLUtils.toFilePath(url);
    }
    else
    {
      return url.toHumanString();
    }
  }

  public ValidationContextBuilder copy()
  {
    return new ValidationContextBuilder().copy(this);
  }

  // FIXME 2022 document
  public boolean isRemote(URL url)
  {
    Preconditions.checkArgument(url != null, "URL is null");
    if (container.isPresent())
    {
      return container.get().isRemote(url);
    }
    else
    {
      return URLUtils.isRemote(url, this.url);
    }
  }

  public String relativize(URL url)
  {
    Preconditions.checkArgument(url != null, "URL is null");
    if (container.isPresent())
    {
      return container.get().relativize(url);
    }
    else
    {
      return this.url.relativize(url);
    }
  }

  public static final ValidationContextBuilder of(URL url)
  {
    return new ValidationContextBuilder().url(url);
  }

  public static final ValidationContextBuilder test()
  {
    try
    {
      return new ValidationContextBuilder().url(URL.parse("https://test.example.org"));
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError();
    }
  }

  /**
   * Builds a new {@link ValidationContext} from various member objects or by
   * copying a parent context.
   *
   */
  public static final class ValidationContextBuilder
  {
    private URL url = null;
    private String mimeType = null;
    private EPUBVersion version = null;
    private EPUBProfile profile = null;
    private Report report = null;
    private FeatureReport featureReport = null;

    private GenericResourceProvider resourceProvider = null;
    private OCFContainer container = null;
    private ResourceRegistry resourceRegistry = null;
    private ReferenceRegistry referenceRegistry = null;
    private OverlayTextChecker overlayTextChecker = null;
    private Set<PublicationType> pubTypes = null;
    private ImmutableSet.Builder<Property> properties = ImmutableSet.<Property> builder();

    public ValidationContextBuilder()
    {
    }

    public ValidationContextBuilder(ValidationContext context)
    {
      copy(context);
    }

    public ValidationContextBuilder copy(ValidationContext context)
    {
      url = context.url;
      mimeType = context.mimeType;
      version = context.version;
      profile = context.profile;
      report = context.report;
      featureReport = context.featureReport;
      resourceProvider = context.resourceProvider;
      container = context.container.orNull();
      resourceRegistry = context.resourceRegistry.orNull();
      referenceRegistry = context.referenceRegistry.orNull();
      overlayTextChecker = context.overlayTextChecker.orNull();
      pubTypes = context.pubTypes;
      properties = ImmutableSet.<Property> builder().addAll(context.properties);
      return this;
    }

    public ValidationContextBuilder url(URL url)
    {
      this.url = url;
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

    public ValidationContextBuilder featureReport(FeatureReport featureReport)
    {
      this.featureReport = featureReport;
      return this;
    }

    public ValidationContextBuilder resourceProvider(GenericResourceProvider resourceProvider)
    {
      this.resourceProvider = resourceProvider;
      return this;
    }

    public ValidationContextBuilder container(OCFContainer container)
    {
      this.container = container;
      if (container != null)
      {
        this.resourceRegistry = new ResourceRegistry();
        this.referenceRegistry = new ReferenceRegistry(container, resourceRegistry);
        this.overlayTextChecker = new OverlayTextChecker();
      }
      return this;
    }

    public ValidationContextBuilder pubTypes(Set<PublicationType> pubTypes)
    {
      this.pubTypes = pubTypes;
      return this;
    }

    public ValidationContextBuilder properties(Set<Property> properties)
    {
      this.properties = ImmutableSet.builder();
      if (properties != null)
      {
        this.properties.addAll(properties);
      }
      return this;
    }

    public ValidationContextBuilder addProperty(Property property)
    {
      properties.add(Preconditions.checkNotNull(property));
      return this;
    }

    public ValidationContext build()
    {
      return new ValidationContext(this);
    }
  }

  /**
   * Utility to create {@link Predicate}s applying to {@link ValidationContext}
   * instances.
   *
   */
  public static final class ValidationContextPredicates
  {

    /**
     * Returns a predicate that evaluates to <code>true</code> if the given
     * property is declared in the context being tested.
     */
    public static Predicate<ValidationContext> hasProp(final Property property)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.properties.contains(property);
        }
      };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the given
     * publication <code>dc:type</code> is declared in the context being tested.
     */
    public static Predicate<ValidationContext> hasPubType(final PublicationType type)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.pubTypes.contains(type);
        }
      };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the context
     * being tested has the given media type.
     */
    public static Predicate<ValidationContext> mimetype(final String mimetype)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.mimeType.equals(mimetype);
        }
      };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the context
     * being tested has the given path.
     */
    public static Predicate<ValidationContext> path(final String path)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.path.equals(path);
        }
      };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the context
     * being tested declares the given validation profile.
     */
    public static Predicate<ValidationContext> profile(final EPUBProfile profile)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.profile.equals(profile);
        }
      };
    }

    /**
     * Returns a predicate that evaluates to <code>true</code> if the context
     * being tested declares the given EPUB version.
     */
    public static Predicate<ValidationContext> version(final EPUBVersion version)
    {
      return new Predicate<ValidationContext>()
      {
        @Override
        public boolean apply(ValidationContext input)
        {
          return input.version.equals(version);
        }
      };
    }

    private ValidationContextPredicates()
    {
    }
  }

  public String getMimeType(URL url)
  {
    if (url == null) return null;
    if ("data".equals(url.scheme()))
    {
      return URLUtils.getDataURLType(url);
    }
    else if (resourceRegistry.isPresent())
    {
      return resourceRegistry.get().getMimeType(url);
    }
    else
    {
      return null;
    }
  }

}

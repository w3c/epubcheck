/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.opf;

import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.hasPubType;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.profile;
import static com.adobe.epubcheck.opf.ValidationContext.ValidationContextPredicates.version;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.w3c.epubcheck.core.AbstractChecker;
import org.w3c.epubcheck.core.Checker;
import org.w3c.epubcheck.core.CheckerFactory;
import org.w3c.epubcheck.core.references.ReferenceRegistry;
import org.w3c.epubcheck.core.references.ResourceReferencesChecker;
import org.w3c.epubcheck.util.url.URLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.FeatureReport;
import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.nav.NavChecker;
import com.adobe.epubcheck.ncx.NCXChecker;
import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.ocf.OCFFilenameChecker;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.BufferedReport;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.ValidatorMap;
import com.adobe.epubcheck.xml.Namespaces;
import com.adobe.epubcheck.xml.SAXAbortException;
import com.adobe.epubcheck.xml.XMLParser;
import com.adobe.epubcheck.xml.XMLValidator;
import com.adobe.epubcheck.xml.XMLValidators;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class OPFChecker extends AbstractChecker
{

  private final static ValidatorMap validatorMap = ValidatorMap.builder()
      .put(version(EPUBVersion.VERSION_2), XMLValidators.OPF_20_RNG)
      .put(version(EPUBVersion.VERSION_2), XMLValidators.OPF_20_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_RNC)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_DO_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_DICT_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_IDX_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_MANIFEST_SCH)
      .put(version(EPUBVersion.VERSION_3), XMLValidators.OPF_30_COLLECTION_PREVIEW_SCH)
      .put(Predicates.or(profile(EPUBProfile.DICT), hasPubType(PublicationType.DICTIONARY)),
          XMLValidators.OPF_DICT_SCH)
      .put(Predicates.or(profile(EPUBProfile.EDUPUB), hasPubType(PublicationType.EDUPUB)),
          XMLValidators.OPF_EDUPUB_SCH)
      .put(Predicates.or(profile(EPUBProfile.PREVIEW), hasPubType(PublicationType.PREVIEW)),
          XMLValidators.OPF_PREVIEW_SCH)
      .build();

  // schemas that only report on `opf:collection` elements
  private static final Set<XMLValidators> COLLECTION_VALIDATORS = EnumSet
      .of(XMLValidators.OPF_30_COLLECTION_DO_SCH, XMLValidators.OPF_30_COLLECTION_DICT_SCH,
          XMLValidators.OPF_30_COLLECTION_IDX_SCH, XMLValidators.OPF_30_COLLECTION_MANIFEST_SCH,
          XMLValidators.OPF_30_COLLECTION_PREVIEW_SCH);

  protected final String path;
  protected final EPUBVersion version;
  protected OPFHandler opfHandler = null;

  public OPFChecker(ValidationContext context)
  {
    super(context);
    this.path = context.path;
    this.version = context.version;
  }

  public void check()
  {
    if (context.container.isPresent())
    {
      checkPackage();
    }
    else
    {
      checkContent();
    }
  }

  protected boolean checkPackage()
  {
    Preconditions.checkState(context.container.isPresent());
    OCFContainer container = context.container.get();
    if (!container.contains(context.url))
    {
      report.message(MessageId.PKG_020, EPUBLocation.of(context), path);
      return false;
    }
    checkContent();

    if (!opfHandler.checkUniqueIdentExists())
    {
      report.message(MessageId.OPF_030, EPUBLocation.of(context), opfHandler.getIdentId());
    }

    List<OPFItem> items = opfHandler.getItems();
    report.info(null, FeatureEnum.ITEMS_COUNT, Integer.toString(items.size()));

    // Register package doc and items to the XRefChecker
    context.resourceRegistry.get().registerResource(context.url, context.mimeType);
    for (OPFItem item : items)
    {
      context.resourceRegistry.get().registerResource(item);
      report.info(item.getPath(), FeatureEnum.DECLARED_MIMETYPE, item.getMimeType());
    }

    checkGuide();

    // Check items content (publication resources)
    checkItemContents(items);

    // Checks items after the content-validation pass
    // This allows to run checks depending on info collected in publication
    // resources
    for (OPFItem item : items)
    {
      checkItemAfterResourceValidation(item);
    }

    // Check references
    new ResourceReferencesChecker(context).check();
    return false;
  }

  protected void checkItemAfterResourceValidation(OPFItem item)
  {
  }

  protected void checkGuide()
  {
    int refCount = opfHandler.getReferenceCount();
    for (int i = 0; i < refCount; i++)
    {
      OPFReference ref = opfHandler.getReference(i);
      Optional<OPFItem> item = opfHandler.getItemByURL(ref.getDocumentURL());
      if (!item.isPresent())
      {
        report.message(MessageId.OPF_031,
            EPUBLocation.of(context).at(ref.getLineNumber(), ref.getColumnNumber()),
            context.relativize(ref.getURL()));
      }
      else
      {
        if (!isBlessedItemType(item.get().getMimeType(), version)
            && !isDeprecatedBlessedItemType(item.get().getMimeType()))
        {
          report.message(MessageId.OPF_032,
              EPUBLocation.of(context).at(ref.getLineNumber(), ref.getColumnNumber()),
              context.relativize(ref.getURL()));
        }
      }
    }
  }

  protected void initHandler()
  {
    opfHandler = new OPFHandler(context);
  }

  public OPFHandler getOPFHandler()
  {
    return opfHandler;
  }

  private boolean hasCollections()
  {
    if (!context.container.isPresent())
    {
      return true;
    }
    try
    {
      XMLParser parser = new XMLParser(new ValidationContextBuilder(context).mimetype("opf")
          .report(QuietReport.INSTANCE).build());
      final boolean[] found = { false };
      final boolean[] completed = { false };
      parser.addContentHandler(new DefaultHandler()
      {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException
        {
          if (Namespaces.PACKAGE.equals(uri) && "collection".equals(localName))
          {
            found[0] = true;
            throw new SAXAbortException();
          }
        }

        @Override
        public void endDocument()
        {
          completed[0] = true;
        }
      });
      parser.process();
      return found[0] || !completed[0];
    } catch (Exception e)
    {
      return true;
    }
  }

  private Predicate<XMLValidators> getValidatorFilter()
  {
    if (context.version != EPUBVersion.VERSION_3 || hasCollections())
    {
      return Predicates.alwaysTrue();
    }
    return new Predicate<XMLValidators>()
    {
      @Override
      public boolean apply(XMLValidators validator)
      {
        return !COLLECTION_VALIDATORS.contains(validator);
      }
    };
  }

  protected boolean checkContent()
  {
    XMLParser parser = new XMLParser(new ValidationContextBuilder(context).mimetype("opf").build());
    initHandler();
    for (XMLValidator validator : validatorMap.getValidators(context, getValidatorFilter()))
    {
      parser.addValidator(validator);
    }
    parser.addContentHandler(opfHandler);
    parser.process();

    for (OPFItem item : opfHandler.getItems())
    {
      // only check the filename in single-file mode
      // (it is checked by the container checker in full-publication mode)
      // and for local resources (i.e. computed to a file URL)
      if (!context.container.isPresent() && !item.isRemote() && !item.hasDataURL())
      {
        new OCFFilenameChecker(item.getPath(), context, item.getLocation()).check();
      }
      if (!item.equals(opfHandler.getItemByURL(item.getURL()).orNull()))
      {
        // FIXME 2022 check duplicates at build time (in OPFHandler)
        report.message(MessageId.OPF_074, item.getLocation(), item.getPath());
      }
      // check that the manifest does not include the package document itself
      else if (item.getURL().equals(context.url))
      {
        report.message(MessageId.OPF_099, item.getLocation());
      }
      else
      {
        checkItem(item, opfHandler);
      }
    }

    if (!opfHandler.getSpineItems().isEmpty())
    {
      boolean linearFound = false;
      int spineIndex = 0;
      for (OPFItem item : opfHandler.getSpineItems())
      {
        checkSpineItem(item, opfHandler);
        if (item.isLinear())
        {
          linearFound = true;
        }
        report.info(item.getPath(), FeatureEnum.SPINE_INDEX, Integer.toString(spineIndex++));
      }
      if (!linearFound)
      {
        report.message(MessageId.OPF_033, EPUBLocation.of(context));
      }
    }

    if (version == EPUBVersion.VERSION_2)
    {
      // check for >1 itemrefs to any given spine item
      // http://code.google.com/p/epubcheck/issues/detail?id=182
      Set<OPFItem> seen = new HashSet<OPFItem>();
      for (OPFItem item : opfHandler.getSpineItems())
      {
        if (seen.contains(item))
        {
          report.message(MessageId.OPF_034, item.getLocation(), item.getId());
        }
        else
        {
          seen.add(item);
        }
      }
    }
    return true;
  }

  public static boolean isBlessedItemType(String type, EPUBVersion version)
  {
    if (version == EPUBVersion.VERSION_2)
    {
      return type.equals("application/xhtml+xml") || type.equals("application/x-dtbook+xml");
    }
    else
    {
      return type.equals("application/xhtml+xml") || type.equals("image/svg+xml");
    }
  }

  public static boolean isDeprecatedBlessedItemType(String type)
  {
    return type.equals("text/x-oeb1-document") || type.equals("text/html");
  }

  public static boolean isBlessedStyleType(String type)
  {
    return type.equals("text/css");
  }

  public static boolean isDeprecatedBlessedStyleType(String type)
  {
    return type.equals("text/x-oeb1-css");
  }

  public static boolean isBlessedImageType(String type, EPUBVersion version)
  {
    return type.equals("image/gif") || type.equals("image/png") || type.equals("image/jpeg")
        || type.equals("image/svg+xml")
        || version == EPUBVersion.VERSION_3 && type.equals("image/webp")
        || version == EPUBVersion.VERSION_3 && type.equals("image/avif")
        || version == EPUBVersion.VERSION_3 && type.equals("image/jxl");
  }

  public static boolean isBlessedFontMimetype20(String mime)
  {
    return mime != null && (mime.startsWith("font/") || mime.startsWith("application/font")
        || mime.startsWith("application/x-font") || "application/vnd.ms-opentype".equals(mime));
  }

  public static boolean isScriptType(String type)
  {
    type = (type == null) ? null : type.toLowerCase(Locale.ENGLISH);
    return "application/javascript".equals(type) || "text/javascript".equals(type)
        || "application/ecmascript".equals(type) || "application/x-ecmascript".equals(type)
        || "application/x-javascript".equals(type) || "text/ecmascript".equals(type)
        || "text/javascript1.0".equals(type) || "text/javascript1.1".equals(type)
        || "text/javascript1.2".equals(type) || "text/javascript1.3".equals(type)
        || "text/javascript1.4".equals(type) || "text/javascript1.5".equals(type)
        || "text/jscript".equals(type) || "text/livescript".equals(type)
        || "text/x-ecmascript".equals(type) || "text/x-javascript".equals(type);
  }

  protected void checkItem(OPFItem item, OPFHandler opfHandler)
  {
    String mimeType = item.getMimeType();
    if (!mimeType.matches("[a-zA-Z0-9!#$&+-^_]+/[a-zA-Z0-9!#$&+-^_]+"))
    {
      /*
       * Ensures that media-type attribute has correct content. The media-type
       * must have a type and a sub-type divided by '/' The allowable content
       * for the media-type attribute is defined in RFC4288 section 4.2
       */
      // report.error(path, item.getLineNumber(), item.getColumnNumber(),
      // "invalid content for media-type attribute");
    }
    else if (isDeprecatedBlessedItemType(mimeType) || isDeprecatedBlessedStyleType(mimeType))
    {
      if (opfHandler.getOpf20PackageFile() && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_035, item.getLocation().context(item.getId()));
      }
      else if (opfHandler.getOpf12PackageFile() && mimeType.equals("text/html"))
      {
        report.message(MessageId.OPF_038, item.getLocation().context(item.getId()), mimeType);
      }
      else if (opfHandler.getOpf20PackageFile())
      {
        report.message(MessageId.OPF_037, item.getLocation().context(item.getId()), mimeType);
      }
    }
    if (opfHandler.getOpf12PackageFile() && !item.hasFallback())
    {
      if (isBlessedItemType(mimeType, version))
      {
        report.message(MessageId.OPF_038, item.getLocation().context(item.getId()), mimeType);
      }
      else if (isBlessedStyleType(mimeType))
      {
        report.message(MessageId.OPF_039, item.getLocation().context(item.getId()), mimeType);
      }
    }
  }

  protected boolean isParallelCheckingEnabled()
  {
    return true;
  }

  protected void checkItemContent(OPFItem item)
  {
    if (!isCheckable(item))
    {
      return;
    }
    // Create a new validation context for the OPF item
    // FIXME 2022 set context OPFItem here
    // (instead of from XRefChecker in the builder code)
    ValidationContext itemContext = new ValidationContextBuilder(context).url(item.getURL())
        .mimetype(item.getMimeType()).properties(item.getProperties()).build();
    checkItemContent(item, itemContext, report);
  }

  protected void checkItemContent(OPFItem item, ValidationContext itemContext, Report itemReport)
  {
    runItemChecker(item, itemContext, itemReport);
  }

  private boolean isCheckable(OPFItem item)
  {
    // We do not currently support checking resources defined as data URLs
    if (item.hasDataURL())
    {
      return false;
    }
    // Abort if the item is this package document
    // (this is disallowed, and reported elsewhere)
    return !URLUtils.docURL(item.getURL()).equals(context.url);
  }

  private void runItemChecker(OPFItem item, ValidationContext itemContext, Report itemReport)
  {
    // Create an appropriate checker
    try
    {
      Checker checker;
      if (item.isNcx())
      {
        checker = new NCXChecker(itemContext);
      }
      else if (item.isNav())
      {
        checker = new NavChecker(itemContext);
      }
      else
      {
        checker = CheckerFactory.newChecker(itemContext);
      }
      // Check the item
      checker.check();
    } catch (IllegalStateException e)
    {
      itemReport.message(MessageId.CHK_008, EPUBLocation.of(context), item.getPath());
    }
  }

  private void checkItemContents(List<OPFItem> items)
  {
    List<OPFItem> localItems = new ArrayList<>(items.size());
    for (OPFItem item : items)
    {
      if (!item.isRemote())
      {
        localItems.add(item);
      }
    }

    int jobs = getJobs(localItems, context.jobs);
    if (jobs > 1 && !isParallelCheckingEnabled())
    {
      jobs = 1;
    }
    if (jobs <= 1 || localItems.size() < 2)
    {
      for (OPFItem item : localItems)
      {
        checkItemContent(item);
      }
      return;
    }

    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    ExecutorService executor = Executors.newFixedThreadPool(jobs, new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable runnable)
      {
        Thread thread = new Thread(runnable, "epubcheck-worker");
        thread.setDaemon(true);
        thread.setContextClassLoader(contextClassLoader);
        return thread;
      }
    });
    boolean interrupted = false;
    try
    {
      // workers fork from a snapshot taken before any result is merged
      final FeatureReport featuresSnapshot = context.featureReport.snapshot();
      List<Future<ItemResult>> futures = new ArrayList<>(localItems.size());
      for (final OPFItem item : localItems)
      {
        futures.add(executor.submit(new Callable<ItemResult>()
        {
          @Override
          public ItemResult call()
          {
            return checkItemContentBuffered(item, featuresSnapshot);
          }
        }));
      }
      // replay results in manifest order
      for (Future<ItemResult> future : futures)
      {
        ItemResult result;
        try
        {
          result = future.get();
        } catch (ExecutionException e)
        {
          Throwable cause = e.getCause();
          if (cause instanceof RuntimeException) throw (RuntimeException) cause;
          if (cause instanceof Error) throw (Error) cause;
          throw new IllegalStateException(cause);
        } catch (InterruptedException e)
        {
          interrupted = true;
          throw new IllegalStateException(e);
        }
        if (result != null)
        {
          // replay the partial report before rethrowing
          result.flush();
          result.rethrowFailure();
        }
      }
    } finally
    {
      // wait for all workers: the caller closes the container on return
      executor.shutdownNow();
      while (!executor.isTerminated())
      {
        try
        {
          executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e)
        {
          interrupted = true;
        }
      }
      if (interrupted)
      {
        Thread.currentThread().interrupt();
      }
    }
  }

  private ItemResult checkItemContentBuffered(OPFItem item, FeatureReport featuresSnapshot)
  {
    if (!isCheckable(item))
    {
      return null;
    }
    BufferedReport bufferedReport = new BufferedReport(context.report);
    FeatureReport bufferedFeatures = featuresSnapshot.fork();
    ReferenceRegistry bufferedReferences = null;
    try
    {
      if (context.referenceRegistry.isPresent())
      {
        bufferedReferences = new ReferenceRegistry(context.container.get(),
            context.resourceRegistry.get());
      }
      ValidationContextBuilder builder = new ValidationContextBuilder(context).url(item.getURL())
          .mimetype(item.getMimeType()).properties(item.getProperties()).report(bufferedReport)
          .featureReport(bufferedFeatures);
      if (bufferedReferences != null)
      {
        builder.referenceRegistry(bufferedReferences);
      }
      checkItemContent(item, builder.build(), bufferedReport);
      return new ItemResult(bufferedReport, bufferedReferences, context.referenceRegistry.orNull(),
          bufferedFeatures, context.featureReport, null);
    } catch (RuntimeException | Error e)
    {
      // keep the partial report, the failure is rethrown on replay
      return new ItemResult(bufferedReport, bufferedReferences, context.referenceRegistry.orNull(),
          bufferedFeatures, context.featureReport, e);
    }
  }

  private static int getJobs(List<OPFItem> items, int configuredJobs)
  {
    int minItems = Integer.getInteger("epubcheck.minItems", 8);
    // overlay registration is order-dependent
    if (items.size() < minItems || hasMediaOverlays(items))
    {
      return 1;
    }
    int jobs = (configuredJobs > 0) ? configuredJobs : Integer.getInteger("epubcheck.jobs", -1);
    if (jobs > 0)
    {
      return Math.min(jobs, items.size());
    }
    return defaultJobs();
  }

  private static int defaultJobs()
  {
    return defaultJobs(Runtime.getRuntime().availableProcessors());
  }

  static int defaultJobs(int processors)
  {
    return Math.min(8, Math.max(Math.min(4, processors), processors / 2));
  }

  private static boolean hasMediaOverlays(List<OPFItem> items)
  {
    for (OPFItem item : items)
    {
      String overlay = item.getMediaOverlay();
      if ((overlay != null && !overlay.trim().isEmpty()) || "application/smil+xml".equals(item.getMimeType()))
      {
        return true;
      }
    }
    return false;
  }

  private static final class ItemResult
  {
    private final BufferedReport report;
    private final ReferenceRegistry references;
    private final ReferenceRegistry target;
    private final FeatureReport features;
    private final FeatureReport targetFeatures;
    private final Throwable failure;

    ItemResult(BufferedReport report, ReferenceRegistry references, ReferenceRegistry target,
        FeatureReport features, FeatureReport targetFeatures, Throwable failure)
    {
      this.report = report;
      this.references = references;
      this.target = target;
      this.features = features;
      this.targetFeatures = targetFeatures;
      this.failure = failure;
    }

    void flush()
    {
      if (references != null && target != null)
      {
        target.addAll(references.asList());
      }
      targetFeatures.mergeSnapshot(features);
      report.flush();
    }

    void rethrowFailure()
    {
      if (failure instanceof RuntimeException) throw (RuntimeException) failure;
      if (failure instanceof Error) throw (Error) failure;
      if (failure != null) throw new IllegalStateException(failure);
    }
  }

  protected void checkSpineItem(OPFItem item, OPFHandler opfHandler)
  {
    // These checks are okay to be done on <spine> items, but they really
    // should be done on all
    // <manifest> items instead. I am avoiding making this change now
    // pending a few issue
    // resolutions in the EPUB Maint Working Group (e.g. embedded fonts not
    // needing fallbacks).
    // [GC 11/15/09]
    String mimeType = item.getMimeType();
    if (isBlessedStyleType(mimeType) || isDeprecatedBlessedStyleType(mimeType)
        || isBlessedImageType(mimeType, version))
    {
      report.message(MessageId.OPF_042, item.getLocation(), mimeType);
    }
    else if (!isBlessedItemType(mimeType, version) && !isDeprecatedBlessedItemType(mimeType))
    {
      if (!item.hasFallback())
      {
        report.message(MessageId.OPF_043, item.getLocation(), mimeType);
      }
      else if (!item.hasContentDocumentFallback())
      {
        report.message(MessageId.OPF_044, item.getLocation(), mimeType);
      }
    }
  }
}

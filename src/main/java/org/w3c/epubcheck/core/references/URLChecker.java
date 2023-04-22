package org.w3c.epubcheck.core.references;

import java.net.URI;

import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.StrictErrorHandler;
import io.mola.galimatias.URL;
import io.mola.galimatias.URLParsingSettings;

public class URLChecker
{
  private static final String TEST_BASE_A_FULL = "https://a.example.org/A/";
  private static final String TEST_BASE_A_START = "https://a.example.org/";
  private static final URL TEST_BASE_A_URL = URL.fromJavaURI(URI.create(TEST_BASE_A_FULL));
  private static final String TEST_BASE_B_FULL = "https://b.example.org/B/";
  private static final String TEST_BASE_B_START = "https://b.example.org/";
  private static final URL TEST_BASE_B_URL = URL.fromJavaURI(URI.create(TEST_BASE_B_FULL));
  private static final URLParsingSettings STRICT_PARSING_SETTINGS = URLParsingSettings.create()
      .withErrorHandler(StrictErrorHandler.getInstance());

  private URL baseURL;
  private URL baseURLTestA;
  private URL baseURLTestB;
  private boolean isRemoteBase;
  private final Report report;
  private final ValidationContext context;

  public URLChecker(ValidationContext context)
  {
    this(Preconditions.checkNotNull(context), context.url);
  }

  public URLChecker(ValidationContext context, URL baseURL)
  {
    this.context = Preconditions.checkNotNull(context);
    this.report = context.report;
    this.baseURL = Preconditions.checkNotNull(baseURL);
    this.isRemoteBase = false;
    try
    {
      this.baseURLTestA = TEST_BASE_A_URL.resolve(context.relativize(baseURL));
      this.baseURLTestB = TEST_BASE_B_URL.resolve(context.relativize(baseURL));
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError(e);
    }
  }

  public URL setBase(String newBase, EPUBLocation location)
  {
    URL newBaseURL = resolveURL(newBase, true, location);
    if (newBaseURL != null)
    {
      baseURL = newBaseURL;
    }
    return baseURL;
  }

  public URL checkURL(String string, EPUBLocation location)
  {
    URL url = URLUtils.normalize(resolveURL(string, false, location));
    return url;
  }

  private URL resolveURL(String string, boolean isBase, EPUBLocation location)
  {
    assert baseURL != null;
    if (string == null) return null;
    try
    {
      // Report file URLs
      if (string.startsWith("file:"))
      {
        report.message(MessageId.RSC_030, location, string);
      }

      // Collapse formatting whitespace in data URLs
      if (string.startsWith("data:"))
      {
        string = string.replaceAll("\\s", "");
      }
      URL url = URL.parse(baseURL, string); // non-strict parsing

      // Also try to parse the URL in strict mode, to report any invalid URL,
      // but continue the processing if an exception is thrown
      try
      {
        URL.parse(STRICT_PARSING_SETTINGS, baseURL, string);
      } catch (GalimatiasParseException e)
      {
        report.message(MessageId.RSC_020, location, string, e.getLocalizedMessage());
      }

      // if we are resolving a new base URL, also resolve the test URLs
      // it will not throw any error if the earlier parsing has not already
      URL testA = baseURLTestA.resolve(string);
      URL testB = baseURLTestB.resolve(string);
      if (isBase)
      {
        baseURLTestA = testA;
        baseURLTestB = testB;
      }

      // if the base URL is remote, no further checks are required
      if (isRemoteBase)
      {
        return url;
      }
      // if URL is absolute, return it as-is
      if (!testA.toString().startsWith(TEST_BASE_A_START)
          || !testB.toString().startsWith(TEST_BASE_B_START))
      {
        isRemoteBase = true;
        return url;
      }
      else
      {
        // if URL has a query string, report and continue
        if (url.query() != null)
        {
          report.message(MessageId.RSC_033, location, string);
          url = url.withQuery(null);
        }
        // if relative URL "leaks" outside the container, report and continue
        // this check only make sense when the container is present
        if (context.container.isPresent() && !isBase
            && (!testA.toString().startsWith(TEST_BASE_A_FULL)
                || !testB.toString().startsWith(TEST_BASE_B_FULL)))
        {
          report.message(MessageId.RSC_026, location, string);
        }
      }
      return url;
    } catch (GalimatiasParseException e)
    {
      // URL parsing error thrown during non-strict parsing
      report.message(MessageId.RSC_020, location, string, e.getLocalizedMessage());
      return null;
    }
  }

}

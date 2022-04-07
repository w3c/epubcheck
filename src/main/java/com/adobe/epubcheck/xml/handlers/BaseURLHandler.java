package com.adobe.epubcheck.xml.handlers;

import java.net.URI;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EpubConstants;
import com.google.common.base.Preconditions;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.StrictErrorHandler;
import io.mola.galimatias.URL;
import io.mola.galimatias.URLParsingSettings;

public abstract class BaseURLHandler extends LocationHandler
{
  private static final String TEST_BASE_A_FULL = "https://a.example.org/A/";
  private static final String TEST_BASE_A_START = "https://a.example.org/";
  private static final URL TEST_BASE_A_URL = URL.fromJavaURI(URI.create(TEST_BASE_A_FULL));
  private static final String TEST_BASE_B_FULL = "https://b.example.org/B/";
  private static final String TEST_BASE_B_START = "https://b.example.org/";
  private static final URL TEST_BASE_B_URL = URL.fromJavaURI(URI.create(TEST_BASE_B_FULL));

  private URL baseURL;
  private URL baseURLTestA;
  private URL baseURLTestB;
  private boolean isRemoteBase;
  private final Report report;

  public BaseURLHandler(ValidationContext context)
  {
    this(context, context.url);
  }

  public BaseURLHandler(ValidationContext context, URL baseURL)
  {
    super(context);
    this.report = context.report;
    this.isRemoteBase = false;
    this.baseURL = Preconditions.checkNotNull(baseURL);
    try
    {
      this.baseURLTestA = TEST_BASE_A_URL.resolve(context.relativize(baseURL));
      this.baseURLTestB = TEST_BASE_B_URL.resolve(context.relativize(baseURL));
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError(e);
    }
  }

  protected final URL baseURL()
  {
    return baseURL;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
  {
    // In HTML, `base` element sets a new base URL
    if (EpubConstants.HtmlNamespaceUri.equals(uri) && "base".equals(localName))
    {
      setBase(attributes.getValue("", "href"));
    }
    // In XML, `xml:base` attribute sets a new base URL
    else
    {
      setBase(attributes.getValue(XMLConstants.XML_NS_URI, "base"));
    }

  }

  private void setBase(String newBase)
  {
    URL newBaseURL = resolveURL(newBase, true);
    if (newBaseURL != null)
    {
      baseURL = newBaseURL;
    }
  }

  protected final URL checkURL(String string)
  {
    URL url = resolveURL(string, false);

    // FIXME 2022 can we check resource existence here instead of XRefChecker?
    // if in a container, check that the resource exists, or return null
    // try
    // {
    // if (url != null && context.container.isPresent()
    // && !context.container.get().contains(url.withFragment(null)))
    // {
    // report.message(MessageId.RSC_007, location(), string);
    // return null;
    // }
    // } catch (GalimatiasParseException e)
    // {
    // new AssertionError(); // setting null fragment shouldn't throw
    // }
    return url;
  }

  private static final URLParsingSettings STRICT_PARSING_SETTINGS = URLParsingSettings.create()
      .withErrorHandler(StrictErrorHandler.getInstance());

  private URL resolveURL(String string, boolean isBase)
  {
    // FIXME next report disallowed URL schemes (e.g. file URLs).
    Preconditions.checkNotNull(baseURL);
    if (string == null) return null;
    try
    {
      URL url = URL.parse(baseURL, string); // non-strict parsing

      // Also try to parse the URL in strict mode, to report any invalid URL
      // Except for 'data:' URLs (formatting whitespace is a common practice)
      if (!"data".equals(url.scheme()))
      {
        try
        {
          URL.parse(STRICT_PARSING_SETTINGS, baseURL, string);
        } catch (GalimatiasParseException e)
        {
          // TODO should this be a mere warning?
          report.message(MessageId.RSC_020, location(), string, e.getLocalizedMessage());
        }
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
      // if relative URL "leaks" outside the container, report and continue
      else if (!isBase && !testA.toString().startsWith(TEST_BASE_A_FULL)
          || !testB.toString().startsWith(TEST_BASE_B_FULL))
      {
        // FIXME !!! this is broken, base s/b taken into account
        report.message(MessageId.RSC_026, location(), string);
      }
      return url;
    } catch (GalimatiasParseException e)
    {
      // the non-strict parsing
      report.message(MessageId.RSC_020, location(), string, e.getLocalizedMessage());
      return null;
    }
  }
}

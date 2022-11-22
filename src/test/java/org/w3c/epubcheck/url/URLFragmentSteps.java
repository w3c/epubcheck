package org.w3c.epubcheck.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.util.url.URLFragment;

import com.google.common.base.Enums;

import io.cucumber.java.en.Then;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class URLFragmentSteps
{

  private static final URL BASE_URL = URL.fromJavaURI(URI.create("https://example.org"));

  private URLFragment result;

  @Then("{string} is a {} {} fragment")
  public void testSVGFragment(String fragment, String validity, String type)
  {
    result = parse(fragment,
        Enums.getIfPresent(MIMEType.class, type).or(MIMEType.OTHER).toString());
    assertThat((result.isValid()) ? "valid" : "invalid", is(validity));
  }

  @Then("it indicates an element with ID {string}")
  public void assertID(String id)
  {
    assertThat(result.getId(), is(id));
  }

  @Then("it does not indicate an element")
  public void assertIDIsEmpty()
  {
    assertThat(result.getId(), is(emptyString()));
  }

  @Then("it is a media fragment")
  public void assertMediaFragment()
  {
    assertTrue(result.isMediaFragment());
  }

  @Then("it has scheme {string}")
  public void assertScheme(String scheme)
  {
    assertThat(result.getScheme(), is(scheme));
  }

  private URLFragment parse(String fragment, String mimetype)
  {
    try
    {
      return URLFragment.parse(BASE_URL.withFragment(fragment), mimetype);
    } catch (GalimatiasParseException e)
    {
      throw new AssertionError("Could not create URL with fragment " + fragment, e);
    }
  }
}

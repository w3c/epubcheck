package org.w3c.epubcheck.util.microsyntax;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.LinkedList;
import java.util.List;

import org.w3c.epubcheck.util.microsyntax.ViewportMeta.ErrorHandler;
import org.w3c.epubcheck.util.microsyntax.ViewportMeta.ParseError;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ViewportSteps
{

  private ViewportMeta viewport;

  public static final class TestErrorHandler implements ErrorHandler
  {
    public final List<ParseError> errors = new LinkedList<>();

    @Override
    public void error(ParseError error, int position)
    {
      errors.add(error);
    }

    public List<ParseError> errors()
    {
      return ImmutableList.copyOf(errors);
    }
  };

  private final TestErrorHandler handler;

  public ViewportSteps(TestErrorHandler handler)
  {
    this.handler = handler;
  }

  @ParameterType(".*")
  public ParseError error(String error)
  {
    try
    {
      return ParseError.valueOf(error);
    } catch (Exception e)
    {
      throw new IllegalArgumentException("unknown error code '" + error + "'");
    }
  }

  @When("parsing viewport {string}")
  public void parseViewport(String content)
  {
    viewport = ViewportMeta.parse(content, handler);
  }

  @Then("no error is returned")
  public void assertValid()
  {
    assertThat("Unexpected errors", handler.errors(), is(empty()));
  }

  @Then("the parsed viewport equals {multimap}")
  public void assertResult(ImmutableListMultimap<String, String> multimap)
  {
    assertThat(viewport.asMultimap(), is(equalTo(multimap)));
  }

  @Then("error {error} is returned")
  public void assertError(ParseError error)
  {
    assertThat(handler.errors(), contains(error));
  }
}

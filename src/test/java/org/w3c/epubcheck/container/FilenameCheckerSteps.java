package org.w3c.epubcheck.container;

import org.w3c.epubcheck.test.TestConfiguration;

import com.adobe.epubcheck.ocf.OCFFilenameChecker;
import com.adobe.epubcheck.opf.ValidationContext;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;

public class FilenameCheckerSteps
{

  private final ValidationContext context;

  public FilenameCheckerSteps(TestConfiguration configuration)
  {
    this.context = configuration.getContextBuilder().build();
  }

  @ParameterType("U\\+[0-9a-zA-Z]{4,6}")
  public int codepoint(String codepoint)
  {
    return Integer.valueOf(codepoint.substring(2), 16);
  }

  @When("checking file name {string}")
  public void checkFilename(String string)
  {
    new OCFFilenameChecker(string, context).check();
  }

  @When("checking file name containing code point {codepoint}")
  public void checkFilenameWithCodepoint(int codepoint)
  {
    new OCFFilenameChecker(
        new StringBuilder().append("a").appendCodePoint(codepoint).append("name").toString(),
        context).check();
  }

}

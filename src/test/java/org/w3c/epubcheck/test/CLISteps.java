package org.w3c.epubcheck.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.tool.EpubChecker;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CLISteps
{

  private static final Map<String, String> REPLACEMENTS = ImmutableMap.<String, String> builder()
      .put("{{VERSION}}", EpubCheck.version()).build();

  private static Pattern TEST_FILE_PATTERN = Pattern.compile("^\\{\\{([\\S&&[^{}]]+)\\}\\}$");

  private static String replace(String template)
  {
    String result = template;
    for (Entry<String, String> replacement : REPLACEMENTS.entrySet())
    {
      result = result.replace(replacement.getKey(), replacement.getValue());
    }
    return result;
  }

  private final Locale defaultLocale = Locale.getDefault();
  private final PrintStream systemOut = System.out;
  private final PrintStream systemErr = System.err;
  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
  private final TestEnvironment environment;
  private int exitCode;

  public CLISteps(TestEnvironment environment)
  {
    this.environment = environment;
  }

  @ParameterType("stdout|stderr")
  public String outputStream(String name)
  {
    switch (name)
    {
    case "stdout":
      return stdout.toString();
    case "stderr":
      return stderr.toString();
    default:
      throw new IllegalArgumentException("Unknown output stream: " + name);
    }
  }

  @Before
  public void before()
  {
    Locale.setDefault(Locale.ENGLISH);
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
  }

  @After
  public void after()
  {
    Locale.setDefault(defaultLocale);
    System.setOut(systemOut);
    System.setErr(systemErr);
  }

  @After
  public void after(Scenario scenario)
  {
    scenario.getSourceTagNames();
  }

  @After("@debug")
  public void afterDebug()
  {
    if (!stdout.toString().isEmpty())
    {
      System.out.println("\n==== Output Stream ===");
      System.out.print(stdout);
      System.out.println("======================");
    }
    if (!stderr.toString().isEmpty())
    {
      System.err.println("\n==== Error Stream ====");
      System.err.print(stderr);
      System.err.println("======================");
    }
  }
  
  @Given("stderr is redirected to stdout")
  public void redirectErrorStream() {
    System.setErr(new PrintStream(stdout));
  }

  @Then("the return code is {int}")
  public void assertReturnCode(int code)
  {
    assertThat(exitCode, equalTo(code));
  }

  @Then("{outputStream} contains")
  @Then("{outputStream} contains {string}")
  public void assertStreamContains(String stream, String string)
  {

    assertThat(stream, containsString(string));
  }

  @Then("{outputStream} does not contain")
  @Then("{outputStream} does not contain {string}")
  public void assertStreamDoesNotContain(String stream, String string)
  {

    assertThat(stream, not(containsString(string)));
  }

  @Then("{outputStream} is empty")
  public void assertStreamIsEmpty(String stream)
  {
    assertThat(stream, is(emptyString()));
  }

  @Then("{outputStream} is")
  @Then("{outputStream} is {string}")
  public void assertStreamIs(String stream, String expected)
  {
    assertThat(stream.replaceAll("\r\n$", "\n"), equalTo(replace(expected)));
  }

  @Then("{outputStream} starts with")
  @Then("{outputStream} starts with {string}")
  public void assertStreamStartsWith(String stream, String string)
  {
    assertThat(stream, startsWith(string));
  }

  @Then("file {string} does not exist")
  public void assertFileDoesNotExist(String path)
  {
    URL url = this.getClass().getResource(environment.getBasepath());
    File file = new File(url.getPath() + path);
    assertThat(file.exists(), is(false));
  }

  @Then("file {string} was created")
  public void assertFileWasCreated(String path)
  {
    URL url = this.getClass().getResource(environment.getBasepath());
    File file = new File(url.getPath() + path);
    assertThat(file.exists(), is(true));
    if (!file.delete())
    {
      throw new AssertionError("Could not delete file " + file);
    }
  }

  @When("running `{}`")
  public void run(String command)
  {
    // Set the configured locale
    Locale.setDefault(environment.getDefaultLocale());

    // Filter the command spec
    List<String> args = Splitter.on(' ').omitEmptyStrings()
        .splitToStream(command)
        // Remove the (optional) `epubcheck` command name
        .skip(1)
        // Map the {{file.ext}} test files to actual file path
        .map(arg -> {
          String mapped = arg;
          Matcher matcher = TEST_FILE_PATTERN.matcher(arg);
          if (matcher.matches())
          {
            String path = matcher.toMatchResult().group(1);
            URL url = this.getClass().getResource(environment.getBasepath());
            mapped = url.getPath() + path;
          }
          return mapped;
        }).collect(Collectors.toList());

    exitCode = new EpubChecker().run(args.toArray(new String[0]));

  }
}

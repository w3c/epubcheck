package org.w3c.epubcheck.test;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import io.cucumber.java.en.Then;

public class JSONReportAssertionSteps
{

  private TestReport report;

  public JSONReportAssertionSteps(TestConfiguration configuration)
  {
    this.report = configuration.getReport();
  }

  @Then("the JSON report is valid")
  public void jsonIsValid()
  {
    assertThat(report.getOutput(), isJson());
  }

  @Then("JSON at {string} contains {int} items")
  public void jsonArrayHasSize(String path, int size)
  {
    assertThat(report.getOutput(), hasJsonPath(path, hasSize(size)));
  }

  @Then("JSON at {string} is {string}")
  public void jsonValueIs(String path, String value)
  {
    assertThat(report.getOutput(), hasJsonPath(path, equalTo(value)));
  }

  @Then("JSON at {string} is {bool}")
  public void jsonValueIs(String path, Boolean value)
  {
    assertThat(report.getOutput(), hasJsonPath(path, equalTo(value)));
  }

  @Then("JSON at {string} is:")
  public void jsonValueIs(String path, List<String> values)
  {
    assertThat(report.getOutput(), hasJsonPath(path, equalTo(values)));
  }

  @Then("JSON at {string} is empty")
  public void jsonValueIs(String path)
  {
    assertThat(report.getOutput(), hasJsonPath(path, is(empty())));
  }

  @Then("JSON at {string} is not null")
  public void jsonValueIsNotNull(String path)
  {
    assertThat(report.getOutput(), hasJsonPath(path, not(nullValue())));
  }

  @Then("JSON at {string} has no null values")
  public void jsonValuesAreNotNull(String path)
  {
    assertThat(report.getOutput(), hasJsonPath(path, everyItem(notNullValue())));
  }

  @Then("JSON at {string} are all {string}")
  public void jsonValuesAreAll(String path, String value)
  {
    assertThat(report.getOutput(), hasJsonPath(path, everyItem(equalTo(value))));
  }

  @Then("JSON at {string} are all {bool}")
  public void jsonValuesAreAll(String path, Boolean value)
  {
    assertThat(report.getOutput(), hasJsonPath(path, everyItem(equalTo(value))));
  }
}

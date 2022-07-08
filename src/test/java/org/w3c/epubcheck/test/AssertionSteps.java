package org.w3c.epubcheck.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.hamcrest.Matcher;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.google.common.collect.Iterables;

import io.cucumber.java.en.Then;

public class AssertionSteps
{

  private TestReport report;
  private MessageInfo lastAssertedMessage;

  public AssertionSteps(TestReport report)
  {
    this.report = report;
  }

  @Then("no( other) warning(s)/error(s) or error(s)/warning(s) are/is reported")
  public void assertNoErrorsOrWarning()
  {
    assertThat("Unexpected fatal errors", report.getAll(Severity.FATAL), is(emptyIterable()));
    assertThat("Unexpected error", report.getAll(Severity.ERROR), is(emptyIterable()));
    assertThat("Unexpected warning", report.getAll(Severity.WARNING), is(emptyIterable()));
  }

  @Then("(the ){severity} {messageId} is reported( \\(.*)")
  public void assertMessageOnce(Severity severity, MessageId id)
  {
    lastAssertedMessage = report.consume(id);
    assertThat("No message found with ID " + id, lastAssertedMessage, is(notNullValue()));
    assertThat(lastAssertedMessage.getSeverity(), equalTo(severity));
  }

  @Then("(the ){severity} {messageId} is reported {int} time(s)( \\(.*)")
  public void assertMessageNTimes(Severity severity, MessageId id, int times)
  {
    List<MessageInfo> actual = report.consumeAll(id);
    lastAssertedMessage = Iterables.getLast(actual, null);
    assertThat(actual, hasSize(times));
    assertThat(actual, everyItem(hasProperty("severity", equalTo(severity))));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Then("(the )following {severity}( ID)(s) are/is reported( \\(.*)")
  public void assertMessageList(Severity severity, List<Matcher> matchers)
  {
    List<Matcher<? super MessageInfo>> expected = (List<Matcher<? super MessageInfo>>) (Object) matchers;
    List<MessageInfo> actual = report.consumeAll(severity);
    assertThat(actual, contains(expected));
  }

  @Then("(the )message is {string}")
  public void assertMessageText(String expected)
  {
    assertThat(lastAssertedMessage, is(notNullValue()));
    assertThat(lastAssertedMessage.getMessage(), is(equalTo(expected)));
  }

  @Then("(the )message contains {string}")
  public void assertMessageTextContains(String expected)
  {
    assertThat(lastAssertedMessage, is(notNullValue()));
    assertThat(lastAssertedMessage.getMessage(), containsString(expected));
  }
}

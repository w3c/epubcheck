/*
 * Copyright (c) 2011 Adobe Systems Incorporated
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

package com.adobe.epubcheck.vocab;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.ReportingLevel;
import com.adobe.epubcheck.util.ThrowingResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class VocabTest
{

  private static final EPUBLocation loc = EPUBLocation.create("file", 42, 42);

  private static enum FOOBAR
  {
    FOO,
    BAR,
    FOO_BAR
  }

  private static final Vocab FOOBAR_VOCAB = new EnumVocab<FOOBAR>(FOOBAR.class,
      "http://example.org/foobar#");

  private static enum NUMBERS
  {
    ONE,
    TWO,
    THREE
  }

  private static final Vocab NUMBERS_VOCAB = new EnumVocab<NUMBERS>(NUMBERS.class,
      "http://example.org/number#", "num");

  private static enum CAMEL
  {
    FOO_BAR
  }

  private static final Vocab CAMEL_VOCAB = new EnumVocab<CAMEL>(CAMEL.class, CaseFormat.LOWER_CAMEL,
      "http://example.org/camel#", "camel");

  private static enum DEPRECATED implements PropertyStatus
  {
    PROP;

    @Override
    public boolean isAllowed(ValidationContext context)
    {
      return true;
    }

    @Override
    public boolean isDeprecated()
    {
      return true;
    }
  }

  private static final Vocab DEPRECATED_VOCAB = new EnumVocab<DEPRECATED>(DEPRECATED.class,
      "http://example.org/deprecated#", "deprecated");

  private static enum DISALLOWED implements PropertyStatus
  {
    PROP;

    @Override
    public boolean isAllowed(ValidationContext context)
    {
      return false;
    }

    @Override
    public boolean isDeprecated()
    {
      return false;
    }
  }

  private static final Vocab DISALLOWED_VOCAB = new EnumVocab<DISALLOWED>(DISALLOWED.class,
      "http://example.org/disallowed#", "disallowed");

  private static final Vocab BAZ_UNCHECKED_VOCAB = new UncheckedVocab(
      "http://example.org/number#baz", "baz");

  private static final Map<String, Vocab> PREDEF_VOCABS = ImmutableMap.<String, Vocab> builder()
      .put("", FOOBAR_VOCAB).put("num", NUMBERS_VOCAB).put("baz", BAZ_UNCHECKED_VOCAB)
      .put("camel", CAMEL_VOCAB).put("deprecated", DEPRECATED_VOCAB)
      .put("disallowed", DISALLOWED_VOCAB).build();
  private static final Map<String, Vocab> KNOWN_VOCABS = ImmutableMap.of(
      "http://example.org/foobar#", FOOBAR_VOCAB, "http://example.org/number#", NUMBERS_VOCAB,
      "http://example.org/number#baz", BAZ_UNCHECKED_VOCAB);
  private static final Set<String> FORBIDDEN_URIS = ImmutableSet.of("http://example.org/default#",
      "http://example.org/forbidden#");

  private List<MessageId> expectedErrors = Lists.newLinkedList();
  private List<MessageId> expectedWarnings = Lists.newLinkedList();
  private List<MessageId> expectedUsages = Lists.newLinkedList();
  private List<MessageId> expectedFatals = Lists.newLinkedList();
  private ValidationContext context;
  private ValidationReport report;

  @Before
  public void before()
  {
    report = new ValidationReport(VocabTest.class.getSimpleName());
    report.setReportingLevel(ReportingLevel.Usage);
    context = new ValidationContextBuilder().resourceProvider(new ThrowingResourceProvider())
        .report(report).build();
  }

  private Set<Property> testPropertyList(String value, Map<String, Vocab> vocabs)
  {
    return testPropertyList(value, vocabs, false);
  }

  private Set<Property> testPropertyList(String value, Map<String, Vocab> vocabs, boolean verbose)
  {
    Set<Property> props = VocabUtil.parsePropertyList(value, vocabs, context, loc);

    if (verbose)
    {
      outWriter.println(report);
    }

    assertEquals("The fatal error results do not match", expectedFatals, report.getFatalErrorIds());
    assertEquals("The error results do not match", expectedErrors, report.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, report.getWarningIds());
    assertEquals("The usage results do not match", expectedUsages, report.getUsageIds());

    return props;
  }

  private Optional<Property> testProperty(String value, Map<String, Vocab> vocabs)
  {
    return testProperty(value, vocabs, false);
  }

  private Optional<Property> testProperty(String value, Map<String, Vocab> vocabs, boolean verbose)
  {

    Optional<Property> prop = VocabUtil.parseProperty(value, vocabs, context, loc);

    if (verbose)
    {
      outWriter.println(report);
    }

    assertEquals("The fatal error results do not match", expectedFatals, report.getFatalErrorIds());
    assertEquals("The error results do not match", expectedErrors, report.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, report.getWarningIds());
    assertEquals("The usages results do not match", expectedUsages, report.getUsageIds());

    return prop;
  }

  private Map<String, Vocab> testVocabs(String value)
  {
    return testVocabs(value, false);
  }

  private Map<String, Vocab> testVocabs(String value, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    Map<String, Vocab> result = VocabUtil.parsePrefixDeclaration(value, PREDEF_VOCABS, KNOWN_VOCABS,
        FORBIDDEN_URIS, testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());
    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The usages results do not match", expectedUsages, testReport.getUsageIds());

    return result;
  }

  @Before
  public void setup()
  {
    expectedFatals.clear();
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedUsages.clear();
  }

  @Test(expected = NullPointerException.class)
  public void testVocabsRequired()
  {
    testPropertyList("prop1 prop2", null);
  }

  @Test
  public void testSingle()
  {
    Optional<Property> prop = testProperty("foo", PREDEF_VOCABS);
    assertTrue(prop.isPresent());
  }

  @Test
  public void testSingleHypenated()
  {
    Optional<Property> prop = testProperty("foo-bar", PREDEF_VOCABS);
    assertTrue(prop.isPresent());
  }

  @Test
  public void testSingleCamelCase()
  {
    Optional<Property> prop = testProperty("camel:fooBar", PREDEF_VOCABS);
    assertTrue(prop.isPresent());
  }

  @Test
  public void testSingleInvalid()
  {
    expectedErrors.add(MessageId.OPF_025);
    Optional<Property> prop = testProperty("foo bar", PREDEF_VOCABS);
    assertFalse(prop.isPresent());
  }

  @Test
  public void testDeprecated()
  {
    expectedWarnings.add(MessageId.OPF_086);
    testProperty("deprecated:prop", PREDEF_VOCABS);
  }

  @Test
  public void testDisallowed()
  {
    expectedUsages.add(MessageId.OPF_087);
    testProperty("disallowed:prop", PREDEF_VOCABS);
  }

  @Test
  public void testList()
  {
    testPropertyList("  foo bar  ", PREDEF_VOCABS);
  }

  @Test
  public void testMalformed()
  {
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    testPropertyList(":world :world :", PREDEF_VOCABS);
  }

  @Test
  public void testUndefined()
  {
    expectedErrors.add(MessageId.OPF_027);
    expectedErrors.add(MessageId.OPF_027);
    testPropertyList("foo num:one num:foo baz foo-bar", PREDEF_VOCABS);
  }

  @Test
  public void testNotDeclared()
  {
    expectedErrors.add(MessageId.OPF_028);
    expectedErrors.add(MessageId.OPF_028);
    testPropertyList("foo foo:bar bar:bar num:one", PREDEF_VOCABS);
  }

  @Test
  public void testUncheckedVocab()
  {
    testPropertyList("foo baz:foo baz:bar num:one", PREDEF_VOCABS);
  }

  @Test
  public void testNullPrefix()
  {
    Map<String, Vocab> actual = testVocabs(null);
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size()));
  }

  @Test
  public void testPrefix()
  {
    Map<String, Vocab> actual = testVocabs(
        "hello: http://example.org/hello# world: http://example.org/world#");
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size() + 2));
    assertThat(actual.keySet(), hasItems("hello", "world"));
    assertThat(actual.get("hello"), instanceOf(UncheckedVocab.class));
    assertThat(actual.get("world"), instanceOf(UncheckedVocab.class));
  }

  @Test
  public void testRedeclaredPrefix()
  {
    expectedWarnings.add(MessageId.OPF_007);
    Map<String, Vocab> actual = testVocabs("num: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size()));
    assertThat(actual.keySet(), hasItems("num"));
    assertThat(actual.get("num"), instanceOf(UncheckedVocab.class));
  }

  @Test
  public void testRedeclaredKnownVocab()
  {
    Map<String, Vocab> actual = testVocabs("int: http://example.org/number#");
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size() + 1));
    assertThat(actual.keySet(), hasItems("num", "int"));
    assertThat(actual.get("int"), instanceOf(EnumVocab.class));
  }

  @Test
  public void testUnderscorePrefix()
  {
    expectedErrors.add(MessageId.OPF_007a);
    Map<String, Vocab> actual = testVocabs(
        "_: http://example.org/hello# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size() + 1));
    assertThat(actual.keySet(), not(hasItems("_")));
    assertThat(actual.keySet(), hasItems("hello"));
  }

  @Test
  public void testDefaultDeclaredPrefix()
  {
    expectedWarnings.add(MessageId.OPF_007b);
    Map<String, Vocab> actual = testVocabs(
        "default: http://example.org/default# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(PREDEF_VOCABS.keySet().size() + 1));
    assertThat(actual.keySet(), not(hasItems("default")));
    assertThat(actual.keySet(), hasItems("hello"));
  }

}

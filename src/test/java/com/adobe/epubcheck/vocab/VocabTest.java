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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
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
  private static final Vocab BAZ_UNCHECKED_VOCAB = new UncheckedVocab(
      "http://example.org/number#baz", "baz");

  private static final Map<String, Vocab> PREDEF_VOCABS = ImmutableMap.of("", FOOBAR_VOCAB, "num",
      NUMBERS_VOCAB, "baz", BAZ_UNCHECKED_VOCAB);
  private static final Map<String, Vocab> KNOWN_VOCABS = ImmutableMap.of(
      "http://example.org/foobar#", FOOBAR_VOCAB, "http://example.org/number#", NUMBERS_VOCAB,
      "http://example.org/number#baz", BAZ_UNCHECKED_VOCAB);
  private static final Set<String> FORBIDDEN_URIS = ImmutableSet.of("http://example.org/default#",
      "http://example.org/forbidden#");

  private List<MessageId> expectedErrors = Lists.newLinkedList();
  private List<MessageId> expectedWarnings = Lists.newLinkedList();
  private List<MessageId> expectedFatals = Lists.newLinkedList();

  private Set<Property> testPropertyList(String value, Map<String, Vocab> vocabs)
  {
    return testPropertyList(value, vocabs, false);
  }

  private Set<Property> testPropertyList(String value, Map<String, Vocab> vocabs, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    Set<Property> props = VocabUtil.parsePropertyList(value, vocabs, testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());

    return props;
  }

  private Optional<Property> testProperty(String value, Map<String, Vocab> vocabs)
  {
    return testProperty(value, vocabs, false);
  }

  private Optional<Property> testProperty(String value, Map<String, Vocab> vocabs, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    Optional<Property> prop = VocabUtil.parseProperty(value, vocabs, testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());

    return prop;
  }

  private Map<String, Vocab> testVocabs(String value)
  {
    return testVocabs(value, false);
  }

  private Map<String, Vocab> testVocabs(String value, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    Map<String, Vocab> result = VocabUtil.parsePrefixDeclaration(value, PREDEF_VOCABS,
        KNOWN_VOCABS, FORBIDDEN_URIS, testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatals,
        testReport.getFatalErrorIds());

    return result;
  }

  @Before
  public void setup()
  {
    expectedErrors.clear();
    expectedWarnings.clear();
    expectedFatals.clear();
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
  public void testSingleInvalid()
  {
    expectedErrors.add(MessageId.OPF_025);
    Optional<Property> prop = testProperty("foo bar", PREDEF_VOCABS);
    assertFalse(prop.isPresent());
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
  public void testNotAllowed()
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
    assertThat(actual.entrySet().size(), is(3));
  }

  @Test
  public void testPrefix()
  {
    Map<String, Vocab> actual = testVocabs("hello: http://example.org/hello# world: http://example.org/world#");
    assertThat(actual.entrySet().size(), is(5));
    assertThat(actual.keySet(), hasItems("hello", "world"));
    assertThat(actual.get("hello"), is(UncheckedVocab.class));
    assertThat(actual.get("world"), is(UncheckedVocab.class));
  }

  @Test
  public void testRedeclaredPrefix()
  {
    expectedWarnings.add(MessageId.OPF_007);
    Map<String, Vocab> actual = testVocabs("num: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(3));
    assertThat(actual.keySet(), hasItems("num"));
    assertThat(actual.get("num"), is(UncheckedVocab.class));
  }

  @Test
  public void testRedeclaredKnownVocab()
  {
    Map<String, Vocab> actual = testVocabs("int: http://example.org/number#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), hasItems("num", "int"));
    assertThat(actual.get("int"), is(EnumVocab.class));
  }

  @Test
  public void testUnderscorePrefix()
  {
    expectedErrors.add(MessageId.OPF_007a);
    Map<String, Vocab> actual = testVocabs("_: http://example.org/hello# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), not(hasItems("_")));
    assertThat(actual.keySet(), hasItems("hello"));
  }

  @Test
  public void testDefaultDeclaredPrefix()
  {
    expectedWarnings.add(MessageId.OPF_007b);
    Map<String, Vocab> actual = testVocabs("default: http://example.org/default# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), not(hasItems("default")));
    assertThat(actual.keySet(), hasItems("hello"));
  }

}

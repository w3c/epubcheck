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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class VocabTest
{

  private static final MessageLocation loc = new MessageLocation("file", 42, 42);

  private static enum FOOBAR
  {
    FOO,
    BAR,
    FOO_BAR
  }

  private static final Vocab FOOBAR_VOCAB = new EnumVocab(FOOBAR.class, "http://example.org/foobar#");

  private static enum NUMBERS
  {
    ONE,
    TWO,
    THREE
  }

  private static final Vocab NUMBERS_VOCAB = new EnumVocab(NUMBERS.class, "http://example.org/number#", "num");
  private static final Vocab BAZ_UNCHECKED_VOCAB = new UncheckedVocab("http://example.org/number#baz", "baz");

  private static final Map<String, Vocab> PREDEF_VOCABS = ImmutableMap.of("", FOOBAR_VOCAB, "num", NUMBERS_VOCAB,
      "baz", BAZ_UNCHECKED_VOCAB);
  private static final Map<String, Vocab> KNOWN_VOCABS = ImmutableMap.of("http://example.org/foobar#", FOOBAR_VOCAB,
      "http://example.org/number#", NUMBERS_VOCAB, "http://example.org/number#baz", BAZ_UNCHECKED_VOCAB);
  private static final Set<String> FORBIDDEN_URIS = ImmutableSet.of("http://example.org/default#",
      "http://example.org/forbidden#");

  private List<MessageId> expectedErrors;
  private List<MessageId> expectedWarnings;
  private List<MessageId> expectedFatalErrors;

  private void testProperties(String value, Map<String, Vocab> vocabs, boolean isList)
  {
    testProperties(value, vocabs, isList, false);
  }

  private void testProperties(String value, Map<String, Vocab> vocabs, boolean isList, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    VocabUtil.parseProperties(value, vocabs, isList, testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatalErrors, testReport.getFatalErrorIds());
  }

  private Map<String, Vocab> testVocabs(String value)
  {
    return testVocabs(value, false);
  }

  private Map<String, Vocab> testVocabs(String value, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(VocabTest.class.getSimpleName());

    Map<String, Vocab> result = VocabUtil.parsePrefixDeclaration(value, PREDEF_VOCABS, KNOWN_VOCABS, FORBIDDEN_URIS,
        testReport, loc);

    if (verbose)
    {
      outWriter.println(testReport);
    }

    assertEquals("The error results do not match", expectedErrors, testReport.getErrorIds());
    assertEquals("The warning results do not match", expectedWarnings, testReport.getWarningIds());
    assertEquals("The fatal error results do not match", expectedFatalErrors, testReport.getFatalErrorIds());

    return result;
  }

  @Before
  public void setup()
  {
    expectedErrors = new ArrayList<MessageId>();
    expectedWarnings = new ArrayList<MessageId>();
    expectedFatalErrors = new ArrayList<MessageId>();
  }

  @Test(expected = NullPointerException.class)
  public void testVocabsRequired()
  {
    testProperties("prop1 prop2", null, true);
  }

  @Test
  public void testListAllowed()
  {
    testProperties("  foo bar  ", PREDEF_VOCABS, true);
  }

  @Test
  public void testListDisallowed()
  {
    expectedErrors.add(MessageId.OPF_025);
    testProperties("foo bar", PREDEF_VOCABS, false);
  }

  @Test
  public void testMalformed()
  {
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    testProperties(":world :world :", PREDEF_VOCABS, true);
  }

  @Test
  public void testNotAllowed()
  {
    expectedErrors.add(MessageId.OPF_027);
    expectedErrors.add(MessageId.OPF_027);
    testProperties("foo num:one num:foo baz foo-bar", PREDEF_VOCABS, true);
  }

  @Test
  public void testNotDeclared()
  {
    expectedErrors.add(MessageId.OPF_028);
    expectedErrors.add(MessageId.OPF_028);
    testProperties("foo foo:bar bar:bar num:one", PREDEF_VOCABS, true);
  }

  @Test
  public void testUncheckedVocab()
  {
    testProperties("foo baz:foo baz:bar num:one", PREDEF_VOCABS, true);
  }

  @Test
  public void testNull()
  {
    Map<String,Vocab> actual = testVocabs(null);
    assertThat(actual.entrySet().size(), is(3));
  }
  
  @Test
  public void testPrefix()
  {
    Map<String,Vocab> actual = testVocabs("hello: http://example.org/hello# world: http://example.org/world#");
    assertThat(actual.entrySet().size(), is(5));
    assertThat(actual.keySet(), hasItems("hello","world"));
    assertThat(actual.get("hello"),is(UncheckedVocab.class));
    assertThat(actual.get("world"),is(UncheckedVocab.class));
  }
  
  @Test
  public void testRedeclaredPrefix()
  {
    expectedWarnings.add(MessageId.OPF_007);
    Map<String,Vocab> actual = testVocabs("num: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(3));
    assertThat(actual.keySet(), hasItems("num"));
    assertThat(actual.get("num"),is(UncheckedVocab.class));
  }
  
  @Test
  public void testRedeclaredKnownVocab()
  {
    Map<String,Vocab> actual = testVocabs("int: http://example.org/number#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), hasItems("num","int"));
    assertThat(actual.get("int"),is(EnumVocab.class));
  }
  
  @Test
  public void testUnderscorePrefix()
  {
    expectedErrors.add(MessageId.OPF_007a);
    Map<String,Vocab> actual = testVocabs("_: http://example.org/hello# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), not(hasItems("_")));
    assertThat(actual.keySet(), hasItems("hello"));
  }
  
  @Test
  public void testDefaultDeclaredPrefix()
  {
    expectedErrors.add(MessageId.OPF_007b);
    Map<String,Vocab> actual = testVocabs("default: http://example.org/default# hello: http://example.org/hello#");
    assertThat(actual.entrySet().size(), is(4));
    assertThat(actual.keySet(), not(hasItems("default")));
    assertThat(actual.keySet(), hasItems("hello"));
  }
  

}

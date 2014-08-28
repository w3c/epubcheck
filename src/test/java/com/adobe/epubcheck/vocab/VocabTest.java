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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
import com.google.common.collect.ImmutableMap;

public class VocabTest
{

  private static final MessageLocation loc = new MessageLocation("file", 42, 42);

  private static enum FOOBAR
  {
    FOO, BAR, FOO_BAR
  }

  private static final Vocab FOOBAR_VOCAB = new EnumVocab(FOOBAR.class, "http://example.org/foobar#");

  private static enum NUMBERS
  {
    ONE, TWO, THREE
  }

  private static final Vocab NUMBERS_VOCAB = new EnumVocab(NUMBERS.class, "http://example.org/number#", "num");
  private static final Vocab BAZ_VOCAB = new UncheckedVocab("http://example.org/number#baz", "baz");

  private static final Map<String, Vocab> VOCABS = ImmutableMap.of("", FOOBAR_VOCAB, "num", NUMBERS_VOCAB, "baz", BAZ_VOCAB);

  private List<MessageId> expectedErrors;
  private List<MessageId> expectedWarnings;
  private List<MessageId> expectedFatalErrors;

  private void test(String value, Map<String, Vocab> vocabs, boolean isList)
  {
    test(value, vocabs, isList, false);
  }

  private void test(String value, Map<String, Vocab> vocabs, boolean isList, boolean verbose)
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
    test("prop1 prop2", null, true);
  }

  @Test
  public void testListAllowed()
  {
    test("  foo bar  ", VOCABS, true);
  }

  @Test
  public void testListDisallowed()
  {
    expectedErrors.add(MessageId.OPF_025);
    test("foo bar", VOCABS, false);
  }

  @Test
  public void testMalformed()
  {
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    expectedErrors.add(MessageId.OPF_026);
    test(":world :world :", VOCABS, true);
  }
  

  @Test
  public void testNotAllowed()
  {
    expectedErrors.add(MessageId.OPF_027);
    expectedErrors.add(MessageId.OPF_027);
    test("foo num:one num:foo baz foo-bar", VOCABS, true);
  }
  
  @Test
  public void testNotDeclared()
  {
    expectedErrors.add(MessageId.OPF_028);
    expectedErrors.add(MessageId.OPF_028);
    test("foo foo:bar bar:bar num:one", VOCABS, true);
  }

  @Test
  public void testUncheckedVocab()
  {
    test("foo baz:foo baz:bar num:one", VOCABS, true);
  }

}

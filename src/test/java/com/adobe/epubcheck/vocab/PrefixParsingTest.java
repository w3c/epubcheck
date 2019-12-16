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

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.ValidationReport;
import com.adobe.epubcheck.util.outWriter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class PrefixParsingTest
{

  private static final EPUBLocation loc = EPUBLocation.create("file", 42, 42);
  private static final Map<String, String> foobarMap = ImmutableMap.of("foo",
      "http://example.org/foo#", "bar", "http://example.org/bar#");
  private static final Map<String, String> emptyMap = ImmutableMap.of();
  private List<MessageId> expectedErrors = Lists.newLinkedList();
  private List<MessageId> expectedWarnings = Lists.newLinkedList();;
  private List<MessageId> expectedFatals = Lists.newLinkedList();;
  private Map<String, String> actual;

  private Map<String, String> test(String value)
  {
    return test(value, false);
  }

  private Map<String, String> test(String value, boolean verbose)
  {
    ValidationReport testReport = new ValidationReport(PrefixParsingTest.class.getSimpleName());

    Map<String, String> result = PrefixDeclarationParser
        .parsePrefixMappings(value, testReport, loc);

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

  @Test
  public void testValidPrefixDeclaration()
  {
    actual = test("foo: http://example.org/foo# bar: http://example.org/bar#");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testEmptyPrefixDeclaration()
  {
    actual = test("");
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testNullPrefixDeclaration()
  {
    actual = test(null);
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testTrailingWhitespace()
  {
    expectedWarnings.add(MessageId.OPF_004);
    expectedWarnings.add(MessageId.OPF_004);
    actual = test("  foo:   http://example.org/foo# \n\t\r bar:   http://example.org/bar#   ");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testEmptyPrefix()
  {
    expectedErrors.add(MessageId.OPF_004a);
    expectedErrors.add(MessageId.OPF_004a);
    actual = test(": http://example.org/foo# : http://example.org/bar#");
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testInvalidPrefixName()
  {
    expectedErrors.add(MessageId.OPF_004b);
    actual = test("123: http://example.org/foo#");
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testInvalidColonAfterPrefix()
  {
    expectedErrors.add(MessageId.OPF_004c);
    expectedErrors.add(MessageId.OPF_004c);
    actual = test("foo http://example.org/foo# bar  : http://example.org/bar#");
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testNoSpaceAfterColon()
  {
    expectedErrors.add(MessageId.OPF_004d);
    actual = test("foo:http://example.org/foo#");
    assertEquals(emptyMap, actual);
  }

  @Test
  public void testIllegalWhitespace()
  {
    expectedWarnings.add(MessageId.OPF_004e);
    expectedWarnings.add(MessageId.OPF_004f);
    actual = test("foo:\t http://example.org/foo# \u2003 bar: http://example.org/bar#");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testNoURIForPrefix()
  {
    expectedErrors.add(MessageId.OPF_005);
    actual = test("foo: http://example.org/foo# bar: http://example.org/bar# baz");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testNoURIForPrefix2()
  {
    expectedErrors.add(MessageId.OPF_005);
    actual = test("foo: http://example.org/foo# bar: http://example.org/bar# baz:");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testNoURIForPrefix3()
  {
    expectedErrors.add(MessageId.OPF_005);
    actual = test("foo: http://example.org/foo# bar: http://example.org/bar# baz: ");
    assertEquals(foobarMap, actual);
  }

  @Test
  public void testInvalidURI()
  {
    expectedErrors.add(MessageId.OPF_006);
    actual = test("bad: [bad] foo: http://example.org/foo# bar: http://example.org/bar#");
    assertEquals(foobarMap, actual);
  }

}

package com.adobe.epubcheck.util;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test for the ISO8601 parser.
 *
 * Date grammar:
 * Year:
 * YYYY (eg 1997)
 * Year and month:
 * YYYY-MM (eg 1997-07)
 * Complete date:
 * YYYY-MM-DD (eg 1997-07-16)
 * Complete date plus hours and minutes:
 * YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
 * Complete date plus hours, minutes and seconds:
 * YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
 * Complete date plus hours, minutes, seconds and a decimal fraction of a second
 * YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
 * where:
 *
 * YYYY = four-digit year
 * MM = two-digit month (01=January, etc.)
 * DD = two-digit day of month (01 through 31)
 * hh = two digits of hour (00 through 23) (am/pm NOT allowed)
 * mm = two digits of minute (00 through 59)
 * ss = two digits of second (00 through 59)
 * s = one or more digits representing a decimal fraction of a second
 * TZD = time zone designator (Z or +hh:mm or -hh:mm)
 *
 */
public class DateParserTest
{

  private DateParser p = new DateParser();

  @Test
  public void testisISO8601Date()
    throws Exception
  {
    assertValidDate("2011");
    assertValidDate("2011-02");
    assertValidDate("2011-02-12");
    assertValidDate("2011-03-01T13");
    assertValidDate("2011-02-01T13:00");
    assertValidDate("2011-02-01T13:00:00");
    assertValidDate("2011-02-01T13:00:00Z");
    assertValidDate("2011-02-01T13:00:00+01:00");
    assertValidDate("2011-02-01T13:00:00-03:00");

    assertInvalidDate("");
    assertInvalidDate("2011-");
    assertInvalidDate("2011-02-");
    assertInvalidDate("2011-02-01T");
    assertInvalidDate("2011-02-01T13:");
    assertInvalidDate("2011-02-01T13:00:");
    assertInvalidDate("2011-02-01T13:00:00T");
    assertInvalidDate("2011-02-01T13:00:00+01");
    assertInvalidDate("2011-02-01T13:00:00+01:");
    assertInvalidDate("2011-02-01T13:00:00-03");
    assertInvalidDate("2011-02-01T13:00:00-03:");
    assertInvalidDate("2011-02-01T13:00:00-03:AA");
    assertInvalidDate("20a1");
    assertInvalidDate(" 2");
    assertInvalidDate("2011-02-29");
    assertInvalidDate("2011-02-01T13:00:00.123aqb");
    assertInvalidDate("1994-11-05T13:15:30Zab");
  }

  private void assertValidDate(String string)
  {
    try
    {
      p.parse(string);
    } catch (InvalidDateException e)
    {
      fail(e.getMessage());
    }
  }

  private void assertInvalidDate(String string)
  {
    try
    {
      p.parse(string);
      fail("Expected parsing failure");
    } catch (InvalidDateException e)
    {
      // System.err.println(e.getMessage());
    }
  }
}

package com.adobe.epubcheck.util;

import java.util.*;

/**
 * Date parser for the ISO 8601 format.
 *
 * Initial code taken from the jigsaw project (W3C license [1]) and modified consistently to
 * apply further checks that were missing, for example the initial code reported 
 * <code>2011-</code> as valid date. 
 * See also:
 * http://www.w3.org/TR/1998/NOTE-datetime-19980827
 *
 * @author mircea@oxygenxml.com Initial version and fixes.
 * @author mihaela@sync.ro Initial version and fixes.
 *
 * @author george@oxygenxml.com Additional fixes.
 */

/**
 * **** [1] W3C license (jigsaw license) *****
 * <p/>
 * Jigsaw Copying Conditions
 * <p/>
 * W3C IPR SOFTWARE NOTICE
 * <p/>
 * Copyright \u00a9 1995-1998 World Wide Web Consortium, (Massachusetts Institute of
 * Technology, Institut National de Recherche en Informatique et en
 * Automatique, Keio University). All Rights Reserved.
 * http://www.w3.org/Consortium/Legal/
 * <p/>
 * This W3C work (including software, documents, or other related items) is
 * being provided by the copyright holders under the following license. By
 * obtaining, using and/or copying this work, you (the licensee) agree that you
 * have read, understood, and will comply with the following terms and
 * conditions:
 * <p/>
 * Permission to use, copy, and modify this software and its documentation,
 * with or without modification,  for any purpose and without fee or royalty is
 * hereby granted, provided that you include the following on ALL copies of the
 * software and documentation or portions thereof, including modifications,
 * that you make:
 * <p/>
 * 1. The full text of this NOTICE in a location viewable to users of the
 * redistributed or derivative work.
 * 2. Any pre-existing intellectual property disclaimers, notices, or terms
 * and conditions. If none exist, a short notice of the following form
 * (hypertext is preferred, text is permitted) should be used within the
 * body of any redistributed or derivative code: "Copyright \u00a9 World Wide
 * Web Consortium, (Massachusetts Institute of Technology, Institut
 * National de Recherche en Informatique et en Automatique, Keio
 * University). All Rights Reserved. http://www.w3.org/Consortium/Legal/"
 * 3. Notice of any changes or modifications to the W3C files, including the
 * date changes were made. (We recommend you provide URIs to the location
 * from which the code is derived).
 * <p/>
 * In addition, creators of derivitive works must include the full text of this
 * NOTICE in a location viewable to users of the derivitive work.
 * <p/>
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS
 * MAKE NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR
 * PURPOSE OR THAT THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE
 * ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 * <p/>
 * COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 * CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR
 * DOCUMENTATION.
 * <p/>
 * The name and trademarks of copyright holders may NOT be used in advertising
 * or publicity pertaining to the software without specific, written prior
 * permission. Title to copyright in this software and any associated
 * documentation will at all times remain with copyright holders.
 * <p/>
 * ____________________________________
 * <p/>
 * This formulation of W3C's notice and license became active on August 14
 * 1998. See the older formulation for the policy prior to this date. Please
 * see our Copyright FAQ for common questions about using materials from our
 * site, including specific terms and conditions for packages like libwww,
 * Amaya, and Jigsaw. Other questions about this notice can be directed to
 * site-policy@w3.org .
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * webmaster
 * (last updated 14-Aug-1998)
 * **** end W3C license (jigsaw license) *****
 */
public class DateParser
{

  /**
   * Check if the next token, if exists, has a given value and that the
   * provided string tokenizer has more tokens after that. It consumes
   * the token checked against the expected value from the string tokenizer.
   *
   * @param st    The StringTokenizer to check.
   * @param token The value expected for the next token.
   * @return <code>true</code> if the token matches the value and there are more tokens.
   *         <code>false</code> if there are no more tokens and we do not have a token to check.
   * @throws InvalidDateException If the token does not match the value or if there are no
   *                              more tokens after the token that matches the expected value.
   */
  private boolean checkValueAndNext(StringTokenizer st, String token) throws
      InvalidDateException
  {
    if (!st.hasMoreTokens())
    {
      return false;
    }
    String t = st.nextToken();
    if (!t.equals(token))
    {
      throw new InvalidDateException("Unexpected: " + t);
    }
    if (!st.hasMoreTokens())
    {
      throw new InvalidDateException("Incomplete date.");
    }
    return true;
  }

  /**
   * Check if a given date is an iso8601 date.
   *
   * @param iso8601Date The date to be checked.
   * @return <code>true</code> if the date is an iso8601 date.
   * @throws InvalidDateException
   */
  private Calendar getCalendar(String iso8601Date) throws
      InvalidDateException
  {
    // YYYY-MM-DDThh:mm:ss.sTZD
    StringTokenizer st = new StringTokenizer(iso8601Date, "-T:.+Z", true);
    if (!st.hasMoreTokens())
    {
      throw new InvalidDateException("Empty Date");
    }
    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    try
    {
      // Year
      if (st.hasMoreTokens())
      {
        int year = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.YEAR, year);
      }
      else
      {
        return calendar;
      }
      // Month
      if (checkValueAndNext(st, "-"))
      {
        int month = Integer.parseInt(st.nextToken()) - 1;
        calendar.set(Calendar.MONTH, month);
      }
      else
      {
        return calendar;
      }
      // Day
      if (checkValueAndNext(st, "-"))
      {
        int day = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.DAY_OF_MONTH, day);
      }
      else
      {
        return calendar;
      }
      // Hour
      if (checkValueAndNext(st, "T"))
      {
        int hour = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
      }
      else
      {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
      }
      // Minutes
      if (checkValueAndNext(st, ":"))
      {
        int minutes = Integer.parseInt(st.nextToken());
        calendar.set(Calendar.MINUTE, minutes);
      }
      else
      {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
      }
      if (!st.hasMoreTokens())
      {
        return calendar;
      }
      // Not mandatory now
      // Seconds
      String tok = st.nextToken();
      if (tok.equals(":"))
      { // seconds
        if (st.hasMoreTokens())
        {
          int secondes = Integer.parseInt(st.nextToken());
          calendar.set(Calendar.SECOND, secondes);
          if (!st.hasMoreTokens())
          {
            return calendar;
          }
          // decimal fraction of a second
          tok = st.nextToken();
          if (tok.equals("."))
          {
            String nt = st.nextToken();
            while (nt.length() < 3)
            {
              nt += "0";
            }
            if (nt.length() > 3)
            {
              // check the other part from the decimal fraction to be formed only from digits
              for (int i = 3; i < nt.length(); i++)
              {
                if (!Character.isDigit(nt.charAt(i)))
                {
                  throw new InvalidDateException("Invalid digit in the decimal fraction of a second: " + nt.charAt(i));
                }
              }
            }
            nt = nt.substring(0, 3); //Cut trailing chars..
            int millisec = Integer.parseInt(nt);
            calendar.set(Calendar.MILLISECOND, millisec);
            if (!st.hasMoreTokens())
            {
              return calendar;
            }
            tok = st.nextToken();
          }
          else
          {
            calendar.set(Calendar.MILLISECOND, 0);
          }
        }
        else
        {
          throw new InvalidDateException("No secondes specified");
        }
      }
      else
      {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
      }
      // Time zone
      if (!tok.equals("Z"))
      { // UTC
        if (!(tok.equals("+") || tok.equals("-")))
        {
          throw new InvalidDateException("only Z, + or - allowed");
        }
        boolean plus = tok.equals("+");
        if (!st.hasMoreTokens())
        {
          throw new InvalidDateException("Missing hour field");
        }
        int tzhour = Integer.parseInt(st.nextToken());
        int tzmin;
        if (checkValueAndNext(st, ":"))
        {
          tzmin = Integer.parseInt(st.nextToken());
        }
        else
        {
          throw new InvalidDateException("Missing minute field");
        }
        if (plus)
        {
          calendar.add(Calendar.HOUR, -tzhour);
          calendar.add(Calendar.MINUTE, -tzmin);
        }
        else
        {
          calendar.add(Calendar.HOUR, tzhour);
          calendar.add(Calendar.MINUTE, tzmin);
        }
      }
      else
      {
        if (st.hasMoreTokens())
        {
          throw new InvalidDateException("Unexpected field at the end of the date field: " + st.nextToken());
        }
      }
    }
    catch (NumberFormatException ex)
    {
      throw new InvalidDateException("[" + ex.getMessage() + "] is not an integer");
    }
    return calendar;
  }

  /**
   * @param iso8601DateAsString The date parameter as a String.
   * @return The corresponding Date object representing the result of parsing the date parameter.
   * @throws InvalidDateException In case of an invalid date.
   */
  public Date parse(String iso8601DateAsString) throws
      InvalidDateException
  {
    Calendar calendar = getCalendar(iso8601DateAsString);
    try
    {
      calendar.setLenient(false);
      return calendar.getTime();
    }
    catch (Exception e)
    {
      throw new InvalidDateException(iso8601DateAsString + " " + e.getClass().toString() + " " + e.getMessage());
    }
  }
}

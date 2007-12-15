package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class DateTimeDatatype extends RegexDatatype implements OrderRelation {
  static private final String YEAR_PATTERN = "-?([1-9][0-9]*)?[0-9]{4}";
  static private final String MONTH_PATTERN = "[0-9]{2}";
  static private final String DAY_OF_MONTH_PATTERN = "[0-9]{2}";
  static private final String TIME_PATTERN = "[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]*)?";
  static private final String TZ_PATTERN = "(Z|[+\\-][0-9][0-9]:[0-5][0-9])?";

  private final String template;

  /**
   * The argument specifies the lexical representation accepted:
   * Y specifies a year with optional preceding minus
   * M specifies a two digit month
   * D specifies a two digit day of month
   * t specifies a time (hh:mm:ss.sss)
   * any other character stands for itself.
   * All lexical representations are implicitly followed by an optional time zone.
   */
  DateTimeDatatype(String template) {
    super(makePattern(template));
    this.template = template;
  }

  static private String makePattern(String template) {
    StringBuffer pattern = new StringBuffer();
    for (int i = 0, len = template.length(); i < len; i++) {
      char c = template.charAt(i);
      switch (c) {
      case 'Y':
        pattern.append(YEAR_PATTERN);
        break;
      case 'M':
        pattern.append(MONTH_PATTERN);
        break;
      case 'D':
        pattern.append(DAY_OF_MONTH_PATTERN);
        break;
      case 't':
        pattern.append(TIME_PATTERN);
        break;
      default:
        pattern.append(c);
        break;
      }
    }
    pattern.append(TZ_PATTERN);
    return pattern.toString();
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return getValue(str, vc) != null;
  }

  static private class DateTime {
    private final Date date;
    private final int leapMilliseconds;
    private final boolean hasTimeZone;

    DateTime(Date date, int leapMilliseconds, boolean hasTimeZone) {
      this.date = date;
      this.leapMilliseconds = leapMilliseconds;
      this.hasTimeZone = hasTimeZone;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof DateTime))
        return false;
      DateTime other = (DateTime)obj;
      return (this.date.equals(other.date)
              && this.leapMilliseconds == other.leapMilliseconds
              && this.hasTimeZone == other.hasTimeZone);
    }

    public int hashCode() {
      return date.hashCode();
    }

    Date getDate() {
      return date;
    }

    int getLeapMilliseconds() {
      return leapMilliseconds;
    }

    boolean getHasTimeZone() {
      return hasTimeZone;
    }
  }

  // XXX Check leap second validity?
  // XXX Allow 24:00:00?
  Object getValue(String str, ValidationContext vc) {
    boolean negative = false;
    int year = 2000; // any leap year will do
    int month = 1;
    int day = 1;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;
    int milliseconds = 0;
    int pos = 0;
    int len = str.length();
    for (int templateIndex = 0, templateLength = template.length();
         templateIndex < templateLength;
         templateIndex++) {
      char templateChar = template.charAt(templateIndex);
      switch (templateChar) {
      case 'Y':
        negative = str.charAt(pos) == '-';
        int yearStartIndex = negative ? pos + 1 : pos;
        pos = skipDigits(str, yearStartIndex);
        try {
          year = Integer.parseInt(str.substring(yearStartIndex, pos));
        }
        catch (NumberFormatException e) {
          return null;
        }
        break;
      case 'M':
        month = parse2Digits(str, pos);
        pos += 2;
        break;
      case 'D':
        day = parse2Digits(str, pos);
        pos += 2;
        break;
      case 't':
        hours = parse2Digits(str, pos);
        pos += 3;
        minutes = parse2Digits(str, pos);
        pos += 3;
        seconds = parse2Digits(str, pos);
        pos += 2;
        if (pos < len && str.charAt(pos) == '.') {
          int end = skipDigits(str, ++pos);
          for (int j = 0; j < 3; j++) {
            milliseconds *= 10;
            if (pos < end)
              milliseconds += str.charAt(pos++) - '0';
          }
          pos = end;
        }
        break;
      default:
        pos++;
        break;
      }
    }
    boolean hasTimeZone = pos < len;
    int tzOffset;
    if (hasTimeZone && str.charAt(pos) != 'Z')
      tzOffset = parseTimeZone(str, pos);
    else
      tzOffset = 0;
    int leapMilliseconds;
    if (seconds == 60) {
      leapMilliseconds = milliseconds + 1;
      milliseconds = 999;
      seconds = 59;
    }
    else
      leapMilliseconds = 0;
    try {
      GregorianCalendar cal = CalendarFactory.getCalendar();
      Date date;
      if (cal == CalendarFactory.cal) {
        synchronized (cal) {
          date = createDate(cal, tzOffset, negative, year, month, day, hours, minutes, seconds, milliseconds);
        }
      }
      else
        date = createDate(cal, tzOffset, negative, year, month, day, hours, minutes, seconds, milliseconds);
      return new DateTime(date, leapMilliseconds, hasTimeZone);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  // The GregorianCalendar constructor is incredibly slow with some
  // versions of GCJ (specifically the version shipped with RedHat 9).
  // This code attempts to detect when construction is slow.
  // When it is, we synchronize access to a single
  // object; otherwise, we create a new object each time we need it
  // so as to avoid thread lock contention.

  static class CalendarFactory {
    static private final int UNKNOWN = -1;
    static private final int SLOW = 0;
    static private final int FAST = 1;
    static private final int LIMIT = 10;
    static private int speed = UNKNOWN;
    static GregorianCalendar cal = new GregorianCalendar();

    static GregorianCalendar getCalendar() {
      // Don't need to synchronize this because speed is atomic.
      switch (speed) {
      case SLOW:
        return cal;
      case FAST:
        return new GregorianCalendar();
      }
      // Note that we are not timing the first construction (which happens
      // at class initialization), since that may involve one-time cache
      // initialization.
      long start = System.currentTimeMillis();
      GregorianCalendar tem = new GregorianCalendar();
      long time = System.currentTimeMillis() - start;
      speed =  time > LIMIT ? SLOW : FAST;
      return tem;
    }
  }

  private static Date createDate(GregorianCalendar cal, int tzOffset, boolean negative,
                                 int year, int month, int day,
                                 int hours, int minutes, int seconds, int milliseconds) {
    cal.setLenient(false);
    cal.setGregorianChange(new Date(Long.MIN_VALUE));
    cal.clear();
    // Using a time zone of "GMT+XX:YY" doesn't work with JDK 1.1, so we have to do it like this.
    cal.set(Calendar.ZONE_OFFSET, tzOffset);
    cal.set(Calendar.DST_OFFSET, 0);
    cal.set(Calendar.ERA, negative ? GregorianCalendar.BC : GregorianCalendar.AD);
    // months in ISO8601 start with 1; months in Java start with 0
    month -= 1;
    cal.set(year, month, day, hours, minutes, seconds);
    cal.set(Calendar.MILLISECOND, milliseconds);
    checkDate(cal.isLeapYear(year), month, day); // for GCJ
    return cal.getTime();
  }

  static private void checkDate(boolean isLeapYear, int month, int day) {
    if (month < 0 || month > 11 || day < 1)
      throw new IllegalArgumentException();
    int dayMax;
    switch (month) {
    // Thirty days have September, April, June and November...
    case Calendar.SEPTEMBER:
    case Calendar.APRIL:
    case Calendar.JUNE:
    case Calendar.NOVEMBER:
      dayMax = 30;
      break;
    case Calendar.FEBRUARY:
      dayMax = isLeapYear ? 29 : 28;
      break;
    default:
      dayMax = 31;
      break;
    }
    if (day > dayMax)
      throw new IllegalArgumentException();
  }

  static private int parseTimeZone(String str, int i) {
    int sign = str.charAt(i) == '-' ? -1 : 1;
    return (Integer.parseInt(str.substring(i + 1, i + 3))*60 + Integer.parseInt(str.substring(i + 4)))*60*1000*sign;
  }

  static private int parse2Digits(String str, int i) {
    return (str.charAt(i) - '0')*10 + (str.charAt(i + 1) - '0');
  }

  static private int skipDigits(String str, int i) {
    for (int len = str.length(); i < len; i++) {
      if ("0123456789".indexOf(str.charAt(i)) < 0)
        break;
    }
    return i;
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  static private final int TIME_ZONE_MAX = 14*60*60*1000;

  public boolean isLessThan(Object obj1, Object obj2) {
    DateTime dt1 = (DateTime)obj1;
    DateTime dt2 = (DateTime)obj2;
    long t1 = dt1.getDate().getTime();
    long t2 = dt2.getDate().getTime();
    if (dt1.getHasTimeZone() == dt2.getHasTimeZone())
      return isLessThan(t1,
                        dt1.getLeapMilliseconds(),
                        t2,
                        dt2.getLeapMilliseconds());
    else if (!dt2.getHasTimeZone())
      return isLessThan(t1, dt1.getLeapMilliseconds(), t2 - TIME_ZONE_MAX, dt2.getLeapMilliseconds());
    else
      return isLessThan(t1 + TIME_ZONE_MAX, dt1.getLeapMilliseconds(), t2, dt2.getLeapMilliseconds());
  }

  static private boolean isLessThan(long t1, int leapMillis1, long t2, int leapMillis2) {
    if (t1 < t2)
      return true;
    if (t1 > t2)
      return false;
    if (leapMillis1 < leapMillis2)
      return true;
    return false;
  }
}

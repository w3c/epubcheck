package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

class DurationDatatype extends RegexDatatype implements OrderRelation {
  static private final String PATTERN =
    "-?P([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T([0-9]+H)?([0-9]+M)?(([0-9]+(\\.[0-9]*)?|\\.[0-9]+)S)?)?";

  DurationDatatype() {
    super(PATTERN);
  }

  public boolean lexicallyAllows(String str) {
    if (!super.lexicallyAllows(str))
      return false;
    char last = str.charAt(str.length()-1);
    // This enforces that there must be at least one component
    // and that T is omitted if all time components are omitted
    return last != 'P' && last != 'T';
  }

  static private class Duration {
    private final BigInteger years;
    private final BigInteger months;
    private final BigInteger days;
    private final BigInteger hours;
    private final BigInteger minutes;
    private final BigDecimal seconds;

    Duration(boolean negative,
             BigInteger years, BigInteger months, BigInteger days,
             BigInteger hours, BigInteger minutes, BigDecimal seconds) {
      if (negative) {
        this.years = years.negate();
        this.months = months.negate();
        this.days = days.negate();
        this.hours = hours.negate();
        this.minutes = minutes.negate();
        this.seconds = seconds.negate();
      }
      else {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
      }
    }

    BigInteger getYears() {
      return years;
    }

    BigInteger getMonths() {
      return months;
    }

    BigInteger getDays() {
      return days;
    }

    BigInteger getHours() {
      return hours;
    }

    BigInteger getMinutes() {
      return minutes;
    }

    BigDecimal getSeconds() {
      return seconds;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Duration))
        return false;
      Duration other = (Duration)obj;
      return (this.years.equals(other.years)
              && this.months.equals(other.months)
              && this.days.equals(other.days)
              && this.hours.equals(other.hours)
              && this.minutes.equals(other.minutes)
              && this.seconds.compareTo(other.seconds) == 0);
    }

    public int hashCode() {
      return (years.hashCode()
              ^ months.hashCode()
              ^ days.hashCode()
              ^ hours.hashCode()
              ^ minutes.hashCode()
              ^ seconds.hashCode());
    }
  }

  Object getValue(String str, ValidationContext vc) {
    int t = str.indexOf('T');
    if (t < 0)
      t = str.length();
    String date = str.substring(0, t);
    String time = str.substring(t);
    return new Duration(str.charAt(0) == '-',
                        getIntegerField(date, 'Y'),
                        getIntegerField(date, 'M'),
                        getIntegerField(date, 'D'),
                        getIntegerField(time, 'H'),
                        getIntegerField(time, 'M'),
                        getDecimalField(time, 'S'));

  }

  static private BigInteger getIntegerField(String str, char code) {
    int end = str.indexOf(code);
    if (end < 0)
      return BigInteger.valueOf(0);
    int start = end;
    while (Character.isDigit(str.charAt(start - 1)))
      --start;
    return new BigInteger(str.substring(start, end));
  }

  static private BigDecimal getDecimalField(String str, char code) {
    int end = str.indexOf(code);
    if (end < 0)
      return BigDecimal.valueOf(0);
    int start = end;
    while (!Character.isLetter(str.charAt(start - 1)))
      --start;
    return new BigDecimal(str.substring(start, end));
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  private static final int[] REF_YEAR_MONTHS = { 1696, 9, 1697, 2, 1903, 3, 1903, 7 };

  public boolean isLessThan(Object obj1, Object obj2) {
    Duration d1 = (Duration)obj1;
    Duration d2 = (Duration)obj2;
    BigInteger months1 = computeMonths(d1);
    BigInteger months2 = computeMonths(d2);
    BigDecimal seconds1 = computeSeconds(d1);
    BigDecimal seconds2 = computeSeconds(d2);
    switch (months1.compareTo(months2)) {
    case -1:
      if (seconds1.compareTo(seconds2) <= 0)
        return true;
      break;
    case 0:
      return seconds1.compareTo(seconds2) < 0;
    case 1:
      if (seconds1.compareTo(seconds2) >= 0)
        return false;
      break;
    }
    for (int i = 0; i < REF_YEAR_MONTHS.length; i += 2) {
      BigDecimal total1 = daysPlusSeconds(computeDays(months1, REF_YEAR_MONTHS[i], REF_YEAR_MONTHS[i + 1]), seconds1);
      BigDecimal total2 = daysPlusSeconds(computeDays(months2, REF_YEAR_MONTHS[i], REF_YEAR_MONTHS[i + 1]), seconds2);
      if (total1.compareTo(total2) >= 0)
        return false;
    }
    return true;
  }

  /**
   * Returns the number of days spanned by a period of months starting with a particular
   * reference year and month.
   */
  private static BigInteger computeDays(BigInteger months, int refYear, int refMonth) {
    switch (months.signum()) {
    case 0:
      return BigInteger.valueOf(0);
    case -1:
      return computeDays(months.negate(), refYear, refMonth).negate();
    }
    // Complete cycle of Gregorian calendar is 400 years
    BigInteger[] tem = months.divideAndRemainder(BigInteger.valueOf(400*12));
    --refMonth; // use 0 base to match Java
    int total = 0;
    for (int rem = tem[1].intValue(); rem > 0; rem--) {
      total += daysInMonth(refYear, refMonth);
      if (++refMonth == 12) {
        refMonth = 0;
        refYear++;
      }
    }
    // In the Gregorian calendar, there are 97 (= 100 + 4 - 1) leap years every 400 years.
    return tem[0].multiply(BigInteger.valueOf(365*400 + 97)).add(BigInteger.valueOf(total));
  }

  private static int daysInMonth(int year, int month) {
    switch (month) {
    case Calendar.SEPTEMBER:
    case Calendar.APRIL:
    case Calendar.JUNE:
    case Calendar.NOVEMBER:
      return 30;
    case Calendar.FEBRUARY:
      return isLeapYear(year) ? 29 : 28;
    }
    return 31;
  }

  private static boolean isLeapYear(int year) {
    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
  }

  /**
   * Returns the total number of seconds from a specified number of days and seconds.
   */
  private static BigDecimal daysPlusSeconds(BigInteger days, BigDecimal seconds) {
    return seconds.add(new BigDecimal(days.multiply(BigInteger.valueOf(24*60*60))));
  }

  /**
   * Returns the total number of months specified by the year and month fields of the duration
   */
  private static BigInteger computeMonths(Duration d) {
    return d.getYears().multiply(BigInteger.valueOf(12)).add(d.getMonths());
  }

  /**
   * Returns the total number of seconds specified by the days, hours, minuts and seconds fields of
   * the duration.
   */
  private static BigDecimal computeSeconds(Duration d) {
    return d.getSeconds().add(new BigDecimal(d.getDays().multiply(BigInteger.valueOf(24))
                                             .add(d.getHours()).multiply(BigInteger.valueOf(60))
                                             .add(d.getMinutes()).multiply(BigInteger.valueOf(60))));
  }

  public static void main(String[] args) {
    DurationDatatype dt = new DurationDatatype();
    System.err.println(dt.isLessThan(dt.getValue(args[0], null), dt.getValue(args[1], null)));
  }
}

/*
 * This class was originally created by the DAISY Consortium
 * for another project, licensed under LGPL v2.1.
 * It is now integrated in EPUBCheck and relicensed under
 * EPUBCheck’s primary license.
 * See https://github.com/w3c/epubcheck/pull/1173
 */
package com.adobe.epubcheck.overlay;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>SmilClock</code> object is a wrapper for a SMIL clock value (time)
 * 
 * <pre>
 * Versions:
 * 0.1.0 (09/02/2003)
 * - Implemented string parsing
 * - Implemented both toString() methods
 * 0.1.1 (10/02/2003)
 * - Added static method to get/set tolerance for equals() and compareTo() methods
 * - Modified equals() and compareTo() to take tolerance value into account
 * 0.2.0 (10/04/2003)
 * - Added support for npt= formats
 * - Fixed bug in SmilClock(double) constructor
 * - Fixed nasty bug in SmilClock(String) constructor
 * 1.0.1 (11/01/2004)
 * - Fixed bug in milliseconds parsing in SmilClock(String s); now handles values with more/less than 3 digits
 * - Fixed bug in toString(int format) that caused milliseconds to lose leading zeroes
 * 1.0.2 (11/06/2005) Markus
 * - Added optimization: patterns compiled and static
 * 1.0.3 (21/06/2005) Markus 
 * - Added secondsValueRounded
 * 1.0.4 (10/02/2006) Linus
 * - Fixed locale bug in toString: now using DecimalFormat instead of NumberFormat
 * 1.0.5 (20/06/2006 Laurie
 * - Added HUMAN_READABLE static int toString(int)
 * 1.1.0 (14/11/2006) Linus
 * - Use BigDecimal instead of double to avoid rounding errors
 * </pre>
 * 
 * @author James Pritchett
 */
public class SmilClock {
    // TODO move this to a more appropriate package
    private static Pattern fullClockPattern = Pattern
            .compile("(npt=)?(\\d+):([0-5]\\d):([0-5]\\d)([.](\\d+))?");
    private static Pattern partialClockPattern = Pattern
            .compile("(npt=)?([0-5]\\d):([0-5]\\d)([.](\\d+))?");
    private static Pattern timecountClockPattern = Pattern
            .compile("(npt=)?(\\d+([.]\\d+)?)(h|min|s|ms)?");

    /**
     * @param s A string representation of the SMIL clock value in any accepted
     *            format
     * @throws NumberFormatException if the string is not a legal SMIL clock
     *             value format
     */
    public SmilClock(String s) throws NumberFormatException {
        Matcher m;
        BigDecimal bd;

        /*
         * This uses regular expressions to parse the given string. It tries
         * each of the three formats (full, partial, timecount) and throws an
         * exception if none of them match. It uses regular expression groupings
         * to capture the various numeric portions of the string at parse-time,
         * which it then uses to calculate the milliseconds value.
         */

        // test for timecount clock value
        m = timecountClockPattern.matcher(s.trim());
        if (m.matches()) {
            bd = new BigDecimal(m.group(2)); // Save the number (with
            // fraction)
            if (m.group(4) == null) {
                // this.msecValue = (long)(bd.longValue() * 1000);
                // //(28/11/2006)Piotr: this one truncates fraction
                this.msecValue = new ClipTime(bd.multiply(BigDecimal.valueOf((long) 1000))
                        .longValue());
            } else if (m.group(4).equals("ms")) {
                this.msecValue = new ClipTime(bd.doubleValue()); // NOTE: This will NOT truncate fraction
            } else if (m.group(4).equals("s")) {
                // this.msecValue = bd.multiply(new
                // BigDecimal((long)1000)).longValue(); //(28/11/2006)Piotr: the
                // construcor BigDecimal(long l) missing in java 1.4; ZedVal
                // feature
                this.msecValue = new ClipTime(bd.multiply(BigDecimal.valueOf((long) 1000))
                        .longValue());
            } else if (m.group(4).equals("min")) {
                // this.msecValue = bd.multiply(new
                // BigDecimal((long)60000)).longValue(); //(28/11/2006)Piotr: as
                // above
                this.msecValue = new ClipTime(bd.multiply(BigDecimal.valueOf((long) (60*1000))).doubleValue());
            } else if (m.group(4).equals("h")) {
                // this.msecValue = bd.multiply(new
                // BigDecimal((long)3600000)).longValue(); //(28/11/2006)Piotr:
                // as above
                this.msecValue = new ClipTime(bd.multiply(BigDecimal.valueOf((long) 60*60*1000)).longValue());
            } else {
            	this.msecValue = new ClipTime();
            }
            return;
        }

        // test for a full clock value
        m = fullClockPattern.matcher(s.trim());
        if (m.matches()) {
            this.msecValue = new ClipTime((Long.parseLong(m.group(2)) * 60*60*1000)
                    + (Long.parseLong(m.group(3)) * 60* 1000)
                    + (Long.parseLong(m.group(4)) * 1000)
                    + ((m.group(6) != null) ? new BigDecimal(m.group(5)).multiply(BigDecimal.valueOf(1000)).doubleValue() : 0));
            return;
        }

        // test for partial clock value
        m = partialClockPattern.matcher(s.trim());
        if (m.matches()) {
            this.msecValue = new ClipTime((Long.parseLong(m.group(2)) * 60*1000)
                    + (Long.parseLong(m.group(3)) * 1000)
                    + ((m.group(5) != null) ? new BigDecimal(m.group(4)).multiply(BigDecimal.valueOf(1000)).doubleValue() : 0));
            return;
        }

        // If we got this far, s is not a legal SMIL clock value
        throw new NumberFormatException("Invalid SMIL clock value format: "
                + s.trim());
    }

    public SmilClock() {
    	this.msecValue = new ClipTime();
    }
    
    /**
     * @param msec Time value in milliseconds
     */
//    public SmilClock(long msec) {
//        this.msecValue = new ClipTime(msec);
//    }
    
    private SmilClock(ClipTime clipTime) {
        this.msecValue = clipTime;
    }

//    public SmilClock(SmilClock toCopy) {
//    	this.msecValue = toCopy.getTimeWOPrecisionLoss();
//    }
//    
    /**
     * @param sec Time value in seconds
     */
    public SmilClock(double sec) {
        this.msecValue = new ClipTime(sec * 1000);
    }
    
    public SmilClock addTime(SmilClock addTime) {
    	return new SmilClock(this.getTimeWOPrecisionLoss().add(addTime.getTimeWOPrecisionLoss()));
    }

    public SmilClock subtractTime(SmilClock subtractTime) {
    	return new SmilClock(this.getTimeWOPrecisionLoss().subtract(subtractTime.getTimeWOPrecisionLoss()));
    }

    /**
     * 
     * Just for compability, broken by design really
     * 
     * The SmilClock should only be initialized by values of "seconds", another basic type,
     * implying another unit type, milliseconds is way to dangerous!
     * @param msec Time value in milliseconds
     */
    @Deprecated
    public SmilClock(long msec) {
        this.msecValue = new ClipTime(msec);
    }

    //public void setToMiliseconds(double msec) {
    //	this.msecValue = new ClipTime(msec);
    //}
    
    public boolean notSet() {
    	return this.msecValue.notSet();
    }

    
    /**
     * Returns clock value in full clock value format (default)
     * 
     * @return String in full clock value format (HH:MM:SS.mmm)
     */
    @Override
    public String toString() {
        return this.toString(SmilClock.FULL);
    }

    /**
     * Returns clock value in specified format
     * 
     * @param format Format code (FULL, PARTIAL, TIMECOUNT)
     * @return String with value in named format
     */
    public String toString(int format) {
        long hr;
        long min;
        long sec;
        double msec;
        long tmp;

        String s;

        NumberFormat nfInt = NumberFormat.getIntegerInstance();
        nfInt.setMinimumIntegerDigits(2);
        NumberFormat nfMsec = NumberFormat.getIntegerInstance();
        nfMsec.setMinimumIntegerDigits(3);
        DecimalFormatSymbols dfSymbols = new DecimalFormatSymbols();
        dfSymbols.setDecimalSeparator('.');
        DecimalFormat dfDouble = new DecimalFormat("0.000", dfSymbols);
        dfDouble.setMaximumFractionDigits(3);
        dfDouble.setGroupingUsed(false);

        // Break out all the pieces ...
        msec = this.msecValue.roundedToMilliSeconds().getTimeInMs() % 1000;
        tmp = (Math.round(this.msecValue.getTimeInMs() - msec)) / 1000;
        sec = tmp % 60;
        tmp = (tmp - sec) / 60;
        min = tmp % 60;
        hr = (tmp - min) / 60;

        switch (format) {
        case FULL:
            if (msec > 0) {
                s = hr + ":" + nfInt.format(min) + ":" + nfInt.format(sec)
                        + "." + nfMsec.format(msec);
            } else {
                s = hr + ":" + nfInt.format(min) + ":" + nfInt.format(sec);
            }
            break;
        case PARTIAL:
            // TODO : Comment probably wrong! (Comment older than "previous" code..??
        	// KNOWN BUG: This will return misleading results for clock values >
            // 59:59.999
            // WORK AROUND: Caller is responsible for testing that this is an
            // appropriate format
            if (msec > 0) {
                s = nfInt.format(min) + ":" + nfInt.format(sec) + "."
                        + nfMsec.format(msec);
            } else {
                s = nfInt.format(min) + ":" + nfInt.format(sec);
            }
            break;
        case TIMECOUNT:
            s = dfDouble.format(BigDecimal.valueOf(this.msecValue.getTimeInMs() / 1000));
            break;
        case TIMECOUNT_MSEC:
            s = dfDouble.format(BigDecimal.valueOf(this.msecValue.getTimeInMs())) + "ms";
            break;
        case RAW_TIMECOUNT_TRUNCATED_MSC:
        	s =  Long.toString((long) Math.ceil(this.msecValue.getTimeInMs()));
        	break;
        case TIMECOUNT_SEC:
            s = dfDouble.format(BigDecimal.valueOf(this.msecValue.getTimeInMs() / 1000)) + "s";
            break;
        case TIMECOUNT_MIN:
            s = dfDouble.format(BigDecimal.valueOf(this.msecValue.getTimeInMs() / (1000*60))) + "min";
            break;
        case TIMECOUNT_HR:
            s = dfDouble.format(BigDecimal.valueOf(this.msecValue.getTimeInMs() / (1000*60*60))) + "h";
            break;
        case HUMAN_READABLE:
            if (hr > 0) {
                s = hr + " h " + nfInt.format(min) + " min ";
            } else if (min > 0) {
                s = nfInt.format(min) + " min " + nfInt.format(sec) + " s";
            } else if (sec > 0) {
                s = nfInt.format(sec) + " s " + nfMsec.format(msec) + " ms";
            } else {
                s = nfMsec.format(msec) + " ms";
            }
            break;
        default:
            throw new NumberFormatException("Unknown SMIL clock format code: "
                    + format);
        }
        return s;
    }

    /**
     * Returns clock value in milliseconds
     */
    private ClipTime getTimeWOPrecisionLoss() {
        return this.msecValue;
    }

	/**
     * 
     * Just for compability, broken by design really
     * 
     * @return clock value in milliseconds
     */
    @Deprecated
    public long millisecondsValue() {
        return (long) millisecondsValueAsLong();
    }

    public long millisecondsValueAsLong() {
        return Math.round(this.msecValue.roundedToMilliSeconds().getTimeInMs());
    }

    /**
     * 
     * Just for compability, broken by design really
     * 
     * Enhance type system even further, get rid of log/double altogheter and use some class "Seconds" instead!
     * @return
     */
    @Deprecated
    public long secondsValueRounded() {
        return Math.round(this.secondsValue());
    }

    /**
     * Returns clock value in seconds
     * 
     * @return clock value in seconds
     */
    public double secondsValue() {
        // return new
        // BigDecimal(this.msecValue).divide(BigDecimal.valueOf(1000)).doubleValue();
        // //(28/11/2006)PK: BigDecimal#divide(BigDecimal bd) not in java 1.4;
        // ZedVal feature
        return (double) this.msecValue.getTimeInMs() / 1000;
    }

    /**
     * Returns clock value in seconds, rounded to full seconds
     * 
     * @return clock value in seconds, rounded to full seconds
     */
//    public long secondsValueRounded() {
//        return Math.round(this.secondsValue());
//    }
 
    public double secondsValueRoundedDouble() {
        return Math.round(this.secondsValue());
    }
    
    public SmilClock roundToMSPrecision() {
    	return new SmilClock(this.getTimeWOPrecisionLoss().roundedToMilliSeconds());
    }

    public SmilClock floorToMSPrecision() {
    	return new SmilClock(this.getTimeWOPrecisionLoss().floorToMilliSeconds());
    }
    
    // FIXME Hashcode not implemented, should come in pair with "equals"
    
    // implement equals() so we can test values for equality
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject)
            return true; // Objects are identical
        if (otherObject == null)
            return false; // There ain't nuthin' like a null ...
        if (getClass() != otherObject.getClass())
            return false; // No class-mixing, either
        try {
            SmilClock other = (SmilClock) otherObject; // Cast it, then
            // compare, using
            // tolerance
            return eqWithinTolerance(other, msecTolerance);
        } catch (ClassCastException cce) {
            // do nothing
        }
        return false;
    }
    
    public boolean eqWithinTolerance(SmilClock other, long msecTolerance) {
        if (compareTo(other, msecTolerance) == 0) {
            return true;
        } else {
        	return false;
        }
    }

    // implement Comparable interface so we can sort and compare values
    public int compareTo(Object otherObject) throws ClassCastException {
    	return compareTo(otherObject, getTolerance());
    }
    
    public int compareTo(Object otherObject, long msecTolerance) throws ClassCastException {
        SmilClock other = (SmilClock) otherObject; // Hope for the best!
        if (Math.abs(other.msecValue.getTimeInMs() - this.msecValue.getTimeInMs()) <= msecTolerance) {        	
            return 0;
        }
        if (this.msecValue.getTimeInMs() < other.msecValue.getTimeInMs()) {
            return -1;
        }
        return 1;
    }

    // Static methods

    /**
     * Sets tolerance for comparisons and equality testing.
     * <p>
     * When comparing two values, if they differ by less than the given
     * tolerance, they will be evaluated as equal to one another.
     * </p>
     * 
     * @param msec Tolerance value in milliseconds
     */
    public static void setTolerance(long msec) {
        msecTolerance = msec;
    }

    /**
     * Returns tolerance setting
     * 
     * @return Current tolerance value in milliseconds
     */
    public static long getTolerance() {
        return msecTolerance;
    }

    // Type codes for the different SMIL clock value formats
    public static final int FULL = 1;
    public static final int PARTIAL = 2;
    public static final int TIMECOUNT = 3; // Default version (no metric)
    public static final int TIMECOUNT_MSEC = 4;
    public static final int TIMECOUNT_SEC = 5;
    public static final int TIMECOUNT_MIN = 6;
    public static final int TIMECOUNT_HR = 7;
    public static final int HUMAN_READABLE = 8;
    public static final int RAW_TIMECOUNT_TRUNCATED_MSC = 9;

    private final ClipTime msecValue; // All values stored in milliseconds
    private static long msecTolerance;
}

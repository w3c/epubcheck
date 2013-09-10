package com.adobe.epubcheck.util;

/**
 * Test for the DateParser class.
 */
public class DateParserTest {
	/**
	 * Test the ISO8601 date. 
	 * Date grammar: 
	 * 	Year: 
	 * 		YYYY (eg 1997) 
	 * 	Year and month: 
	 * 		YYYY-MM (eg 1997-07) 
	 * 	Complete date: 
	 * 		YYYY-MM-DD (eg 1997-07-16) 
	 * 	Complete date plus hours and minutes: 
	 * 		YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00) 
	 * 	Complete date plus hours, minutes and seconds:
	 * 		YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00) 
	 * 	Complete date plus hours, minutes, seconds and a decimal fraction of a second
	 * 		YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00) 
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
	 * @throws Exception
	 */
	public void testisISO8601Date() throws Exception {
		DateParser p = new DateParser();
		p.parse(	"2011"						);
		p.parse(	"2011-02"					);
		p.parse(	"2011-02-12"				);
		p.parse(	"2011-03-01T13"				);
		p.parse(	"2011-02-01T13:00"			);
		p.parse(	"2011-02-01T13:00:00"		);
		p.parse(	"2011-02-01T13:00:00Z"		);
		p.parse(	"2011-02-01T13:00:00+01:00"	);
		p.parse(	"2011-02-01T13:00:00-03:00"	);

		try {	
			p.parse(	""							);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-"						);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-"					);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	"2011-02-01T"				);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:"			);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:"			);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:00T"		);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:00+01"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:00+01:"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:00-03"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-01T13:00:00-03:"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	"2011-02-01T13:00:00-03:AA"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	"20a1"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	" 2"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		try {	
			p.parse(	"2011-02-29"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	"2011-02-01T13:00:00.123aqb"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		try {	
			p.parse(	"1994-11-05T13:15:30Zab"	);
			throw new Exception("Invalid date passed!");
		} catch (InvalidDateException e) {}
		
		
	}

	public static void main(String[] args) {
		try {
			new DateParserTest().testisISO8601Date();
			outWriter.println("Passed all tests!");
		} catch (Exception e) {
			outWriter.println("Fail:");
			e.printStackTrace();
		}
	}
}

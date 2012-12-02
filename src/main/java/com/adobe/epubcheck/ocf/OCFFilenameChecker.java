package com.adobe.epubcheck.ocf;

import java.util.HashSet;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.Messages;

public final class OCFFilenameChecker {
	
	public static HashSet<String> restricted30CharacterSet;

	static {
		HashSet<String> set = new HashSet<String>();
		set.add("PRIVATE_USE_AREA");
		set.add("ARABIC_PRESENTATION_FORMS_A");
		set.add("SPECIALS");
		set.add("SUPPLEMENTARY_PRIVATE_USE_AREA_A");
		set.add("SUPPLEMENTARY_PRIVATE_USE_AREA_B");
		set.add("VARIATION_SELECTORS_SUPPLEMENT");
		set.add("TAGS");
		restricted30CharacterSet = set;
	}
	
	private OCFFilenameChecker() {
		// static util
	}
	
	
	public static String checkCompatiblyEscaped(final String str, Report report, EPUBVersion version) {
		if (str.startsWith("http://"))
			return "";

		// the test string will be used to compare test result
		String test = checkNonAsciiFilename(str, report);

		if (str.endsWith(".")) {
			report.error(str, 0, 0, Messages.FILENAME_ENDS_IN_DOT);
			test += ".";
		}

		boolean spaces = false;
		final char[] ascciGraphic = new char[] { '<', '>', '"', '{', '}', '|',
				'^', '`', '*', '?' /* , ':','/', '\\' */};
		String result = "";
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			for (char a : ascciGraphic) {
				if (c == a) {
					result += "\"" + Character.toString(c) + "\",";
					test += Character.toString(c);
				}
			}
			if (Character.isSpaceChar(c)) {
				spaces = true;
				test += Character.toString(c);
			}
		}
		if (result.length() > 1) {
			result = result.substring(0, result.length() - 1);
			report.error(str, 0, 0, Messages.FILENAME_DISALLOWED_CHARACTERS
					+ result);
		}
		if (spaces)
			report.warning(str, 0, 0, Messages.SPACES_IN_FILENAME);
		
		if (version == EPUBVersion.VERSION_3) {
			checkCompatiblyEscaped30(str, test, report);
		}
		return test;
	}
	
	private static String checkNonAsciiFilename(final String str, Report report) {
		// TODO change this from warning to a compatibility hint message level
	
		String nonAscii = str.replaceAll("[\\p{ASCII}]", "");
		if (nonAscii.length() > 0)
			report.warning(str, 0, 0,
					String.format(Messages.FILENAME_NON_ASCII, nonAscii));
		return nonAscii; 
		
	}
	
	private static String checkCompatiblyEscaped30(String str, String test, Report report) {
		String result = "";

		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];

			if (Character.isISOControl(c)) {
				result += "\"" + Character.toString(c) + "\",";
				test += Character.toString(c);
			}

			// DEL (U+007F)
			if (c == '\u007F') {
				result += "\"" + Character.toString(c) + "\",";
				test += Character.toString(c);
			}
			String unicodeType = Character.UnicodeBlock.of(c).toString();
			if (restricted30CharacterSet.contains(unicodeType))
				result += "\"" + Character.toString(c) + "\",";
		}
		if (result.length() > 1) {
			result = result.substring(0, result.length() - 1);
			report.error(str, 0, 0, Messages.FILENAME_DISALLOWED_CHARACTERS
					+ result);
		}
		return test;
	}
}

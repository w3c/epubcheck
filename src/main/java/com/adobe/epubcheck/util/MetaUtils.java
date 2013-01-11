package com.adobe.epubcheck.util;

import java.util.HashSet;
import java.util.Set;

import com.adobe.epubcheck.api.Report;

public class MetaUtils {

	public static Set<String> validateProperties(String propertyValue,
			Set<String> recognizedUnprefixedValues,
			Set<String> recognizedPrefixes, String path, int line, int column,
			Report report, boolean singleValue) {

		if (propertyValue == null)
			return null;

		Set<String> unprefixedValues = new HashSet<String>();

		propertyValue = propertyValue.trim();
		propertyValue = propertyValue.replaceAll("[\\s]+", " ");
		String propertyArray[] = propertyValue.split(" ");

		if (singleValue && propertyArray.length > 1)
			report.error(path, line, column, "Property can take only one value");

		for (int i = 0; i < propertyArray.length; i++)
			if (propertyArray[i].endsWith(":"))
				report.error(
						path,
						line,
						column,
						propertyArray[i]
								+ " value is not allowed to be composed only by a prefix");
			else if (propertyArray[i].contains(":"))
				checkPrefix(
						recognizedPrefixes,
						propertyArray[i].substring(0,
								propertyArray[i].indexOf(':')), path, line,
						column, report);
			else if (recognizedUnprefixedValues != null
					&& recognizedUnprefixedValues.contains(propertyArray[i]))
				unprefixedValues.add(propertyArray[i]);
			else
				report.error(path, line, column, "Undefined property: "
						+ propertyArray[i]);

		return unprefixedValues;
	}

	static boolean checkPrefix(Set<String> prefixSet, String prefix,
			String path, int line, int column, Report report) {

		if (!prefixSet.contains(prefix)) {
			report.error(path, line, column, "Undecleared prefix: " + prefix);
			return false;
		}
		return true;
	}
}

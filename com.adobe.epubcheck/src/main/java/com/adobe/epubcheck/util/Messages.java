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

package com.adobe.epubcheck.util;

public class Messages {

	public static String FULL_EPUB = "File is validated as a complete epub archive.";

	public static String SINGLE_FILE = "File is validated as a single file of type %1$s and version %2$s. Only a subset of the available tests is run.";

	public static String MISSING_FILE = "File %1$s is missing in the package.";

	public static String INVALID_OPF_Version = "Failed obtaining OPF version: %1$s .";

	public static String NAV_NOT_SUPPORTED = "The nav file is not supported for epub versions other than 3.0.";

	public static String OPV_VERSION_TEST = "Tests are done only for the OPF version.";

	public static String MODE_VERSION_NOT_SUPPORTED = "The checker doesn't validate type %1$s and version %2$s.";
	
	public static String VERSION_MISMATCH = "Validating the EPUB against version %1$s but detected version %2$s.";

	public static String VERSION_NOT_FOUND = "Could not detect the EPUB version. Validating against version %1$s.";

	public static String NO_ERRORS__OR_WARNINGS = "No errors or warnings detected.";

	public static String THERE_WERE_ERRORS = "\nCheck finished with warnings or errors\n";
	
	public static String DELETING_ARCHIVE = "\nDeleting requested epub archive as it is not valid.\n";
	
	public static String DISPLAY_HELP = "-help displays help";

	public static String ARGUMENT_NEEDED = "At least one argument expected";

	public static String AFTER_ARGUMENT_EXPECTED = "After the argument %1$s , the %2$s of the file to be checked is expected";

	public static String NO_FILE_SPECIFIED = "No file to check was specified in arguments";

	public static String END_OF_EXECUTION = "The tool will EXIT";

	public static String MODE_VERSION_IGNORED = "The mode and version arguments are ignored for epubs"
			+ "(They are retrieved from the files.)";

	public static String MODE_REQUIRED = "For files other than epubs, mode must be specified! Default version is 3.0.";

	public static String CANNOT_READ_HEADER = "Cannot read header";

	public static String CORRUPTED_ZIP_HEADER = "Corrupted ZIP header";

	public static String LENGTH_FIRST_FILENAME = "Length of the first filename in archive must be 8, but was %1$s";

	public static String EXTRA_FIELD_LENGTH = "Extra field length for first filename must be 0, but was %1$s";

	public static String MIMETYPE_ENTRY_MISSING = "Mimetype entry missing or not the first in archive";

	public static String MIMETYPE_WRONG_TYPE = "Mimetype contains wrong type (%1$s expected).";

	public static String IO_ERROR = "I/O error: %1$s ";

	public static String NULL_REF = "NULL value reference";
	
	public static String RESOURCE_NOT_AVAILABLE = "Could not find resource %1$s";

	public static String UTF_NOT_SUPPORTED = "Only UTF-8 and UTF-16 encodings are allowed, detected %1$s";
	
	public static String UTF_NOT_SUPPORTED_BOM = "Only UTF-8 and UTF-16 encodings are allowed, detected %1$s BOM";

	public static String MALFORMED_BYTE_SEQUENCE = "Malformed byte sequence: %1$s .  Check encoding";

	public static String FAILED_PERFORMING_SCHEMATRON_TESTS = "Failed performing OPF Schematron tests: %1$s";

	public static String FILENAME_DISALLOWED_CHARACTERS = "File name contains characters disallowed in OCF file names: ";

	public static String SPACES_IN_FILENAME = "Filename contains spaces. Consider changing filename such that URI escaping is not necessary";

	public static String FILENAME_ENDS_IN_DOT = "Filename is not allowed to end with \'.\'.";

	public static String FILENAME_NON_ASCII = "File name contains non-ascii characters: %1$s. Consider changing filename";

	public static String CONFLICTING_ATTRIBUTES = "Conflicting attributes found: ";

	public static String INVALID_XML_VERSION = "Any Publication Resource that is an XML-Based Media Type must be a conformant XML 1.0 Document. XML Version retrieved: ";

	public static String XML_VERSION_NOT_SUPPORTED = "Your system doesn't support xml version verification. Make sure your xml files are conformant to XML 1.0";

	public static String EXTERNAL_ENTITIES_NOT_ALLOWED = "External entities are not allowed. External entity declaration found: ";
	
	public static String CSS_STRING_PARSE_ERROR = "An error occurred while parsing CSS for %1$s.";
	
	public static String VALIDATING_VERSION_MESSAGE = "Validating against EPUB version %1$s";
	
	public static String IRREGULAR_DOCTYPE = "Irregular DOCTYPE: found \'%1$s\', expecting \'%2$s\'.";
	
	public static String CSS_PROPERTY_NOT_ALLOWED = "The \'%1$s\' property must not be included in an EPUB Style Sheet.";
	
	public static String MULTIPLE_OPS_RENDITIONS = "The container includes multiple OPS renditions";

	public static String POSITION_FIXED = "The fixed value of the position property is not part of the EPUB 3 CSS Profile.";
	
	public static String CSS_FONT_MIMETYPE = "Font-face reference %1$s to non-standard font type %2$s";
	
	public static String EMPTY_HREF = "Link attribute with no value";

}

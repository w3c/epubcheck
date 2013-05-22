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

	public static String EXTRA_FIELD_LENGTH = "Mimetype entry must not have an extra field in its ZIP header";

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
	
	public static String MISSING_RESOURCE = "resource \'%1$s\' is missing";
	
	public static String RESOURCE_CANNOT_BE_DECRYPTED = "resource \'%1$s\' cannot be decrypted";
	
	public static String OPF_FILE_MISSING = "OPF file %1$s is missing";
	
	public static String OPF_UNIQUE_ID_REF_NOT_EXISTING = "unique-identifier attribute in package element must reference an existing identifier element id";
	
	public static String OPF_GUIDE_REF_UNMANIFESTED = "File listed in reference element in guide was not declared in OPF manifest: ";
	
	public static String OPF_GUIDE_REF_IS_NO_CONTENT_DOCUMENT = "Guide reference to an item that is not a Content Document: ";
	
	public static String OPF_SPINE_ONLY_NON_LINEAR = "spine contains only non-linear resources";
	
	public static String OPF_SPINE_MULTI_REFS_TO_SAME_ID = "spine contains multiple references to the manifest item with id %1$s";
	
	public static String OPF_MIMETYPE_TEXTHTML_WRONG_FOR_XHTMLOPS = "text/html is not appropriate for XHTML/OPS, use application/xhtml+xml instead";
	
	public static String OPF_MIMETYPE_TEXTHTML_WRONG_FOR_OEBPS_12 = "text/html is not appropriate for OEBPS 1.2, use text/x-oeb1-document instead";
	
	public static String OPF_MIMETYPE_DEPRECATED = "deprecated media-type \'%1$s\'";
	
	public static String OPF_MIMETYPE_IS_BLESSED_ITEM_TYPE = "use of OPS media-type \'%1$s\' in OEBPS 1.2 context; use text/x-oeb1-document instead";
	
	public static String OPF_MIMETYPE_IS_BLESSED_STYLE_TYPE = "use of OPS media-type \'%1$s\' in OEBPS 1.2 context; use text/x-oeb1-css instead";
	
	public static String OPF_FALLBACK_ITEM_NOT_FOUND = "fallback item could not be found";
	
	public static String OPF_FALLBACK_STYLE_ITEM_NOT_FOUND = "fallback-style item could not be found";
	
	public static String OPF_MIMETYPE_NOT_PERMISSIBLE_IN_SPINE = "\'%1$s\' is not a permissible spine media-type";
	
	public static String OPF_SPINE_NONSTANDARD_MIMETYPE_WITHOUT_FALLBACK = "Spine item with non-standard media-type \'%1$s\' with no fallback";
	
	public static String OPF_SPINE_NONSTANDARD_MIMETYPE_WITH_NOTALLOWED_FALLBACK = "Spine item with non-standard media-type \'%1$s\' with fallback to non-spine-allowed media-type";
	
	public static String OPF_FALLBACK_CIRCULAR_REF = "circular reference in fallback chain";
	
	public static String OPF_MANIFEST_SCRIPTED_PROPERTY_MISSING = "Item should have the scripted property set in order to be a valid mediaType handler.";
	
	public static String OPF_USING_OEBPS12 = "OPF file is using OEBPS 1.2 syntax allowing backwards compatibility";

	public static String OPF_MISSING_OR_EMPTY_UNIQUE_ID_ATTRIBUTE = "unique-identifier attribute in package element must be present and have a value";

	public static String OPF_ONLY_AUDIO_VIDEO = "Only audio and video remote resources are permitted";

	public static String OPF_ITEM_WITH_ID_NOT_FOUND = "item with id \'%1$s\' not found";

	public static String OPF_TOC_ITEM_WRONG_MIMETYPE = "toc attribute references resource with non-NCX mime type; \"application/x-dtbncx+xml\" is expected";

	public static String OPF_DEPRECATED_ELEMENT = "use of deprecated element \'%1$s\'";

	public static String OPF_DC_ROLE_VALUE_INVALID = "role value \'%1$s\' is not valid";

	public static String OPF_EPUB3_META_DATE_INVALID = "date value \'%1$s\' does not follow recommended syntax as per http://www.w3.org/TR/NOTE-datetime: %2$s";

	public static String OPF_EPUB2_META_DATE_INVALID  = "date value \'%1$s\' is not valid as per http://www.w3.org/TR/NOTE-datetime: %2$s";

	public static String OPF_EMPTY_ELEMENT = "%1$s element is empty";

	public static String OPF_MIMETYPE_IS_CORE_MEDIATYPE = "The media-type \'%1$s\' is a core media type";

	public static String OPF_MIMETYPE_HANDLER_ALREADY_ASSIGNED = "The media-type \'%1$s\' has already been assigned a handler";

	public static String OPF_ITEMREF_WITH_INVALID_PAGESPREAD = "itemref can't have both page-spread-right and page-spread-left properties";

	public static String OPF_ITEM_PROPERTY_NOT_DEFINED = "Item property: \'%1$s\' is not defined for media type: %1$s";

	public static String OPF_REMOTE_RESOURCE_NOT_ALLOWED = "\'%1$s\': remote resource reference not allowed; resource must be placed in the OCF";

	public static String OPF_REF_RESOURCE_MISSING = "\'%1$s\': referenced resource missing in the package.";

	public static String OPF_REF_RESOURCE_NOT_DECLARED = "\'%1$s\': referenced resource is not declared in the OPF manifest.";

	public static String OPF_FRAGMENT_ID_MISSING = "fragment identifier missing in reference to \'%1$s\'";

	public static String OPF_HYPERLINK_TO_NONSTANDARD_RES = "hyperlink to non-standard resource \'%1$s\' of type \'%2$s\'";

	public static String OPF_HYPERLINK_RES_OUTSIDE_SPINE = "hyperlink to resource outside spine \'%1$s\'";

	public static String OPF_NONSTANDARD_IMAGE = "non-standard image resource \'%1$s\' of type \'%2$s\'";
	
	public static String OPF_NONSTANDARD_STYLESHEET = "non-standard stylesheet resource \'%1$s\' of type \'%2$s\'. A fallback must be specified.";

	public static String OPF_FRAGMENT_ID_FOR_IMG = "fragment identifier used for image resource \'%1$s\'";

	public static String OPF_FRAGMENT_ID_FOR_STYLE = "fragment identifier used for stylesheet resource \'%1$s\'";

	public static String OPF_FRAGMENT_ID_NOT_DEFINED_IN = "\'%1$s\': fragment identifier is not defined in \'%2$s\'";

	public static String OPF_FRAGMENT_ID_DEFINES_INCOMPATIBLE_RES = "fragment identifier \'%1$s\' defines incompatible resource type in \'%2$s\'";

	public static String OCF_CONTAINERXML_FULLPATH_ATTR_MISSING = "element \"rootfile\" missing required attribute \"full-path\"";

	public static String OCF_CONTAINERXML_FULLPATH_ATTR_EMPTY = "attribute \"full-path\" on element \"rootfile\" must not be empty";

	public static String CLI_OUTPUT_XML = "Assessment XML document was saved in: ";
	
	public static String NCX_BAD_UID = "meta@dtb:uid content \'%1$s\' should conform to unique-identifier in content.opf: \'%2$s\'";

}

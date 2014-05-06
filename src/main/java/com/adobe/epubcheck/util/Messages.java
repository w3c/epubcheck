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

import com.adobe.epubcheck.api.EpubCheck;

public class Messages {

  public static final String SINGLE_FILE = "File is validated as a single file of type %1$s and version %2$s. Only a subset of the available tests is run.";

  public static String OPV_VERSION_TEST = "*** Candidate for msg deletion *** Tests are performed only for the OPF version.";

  public static final String MODE_VERSION_NOT_SUPPORTED = "The checker doesn't validate type %1$s and version %2$s.";

  public static final String NO_ERRORS__OR_WARNINGS = "No errors or warnings detected.";

  public static final String THERE_WERE_ERRORS = "\nCheck finished with errors\n";

  public static final String THERE_WERE_WARNINGS = "\nCheck finished with warnings\n";

  public static final String ERROR_PROCESSING_UNEXPANDED_EPUB = "\nThis check cannot process expanded epubs\n";

  public static final String DELETING_ARCHIVE = "\nEpub creation cancelled due to detected errors.\n";

  public static final String DISPLAY_HELP = "-help displays help";

  public static final String ARGUMENT_NEEDED = "At least one argument expected";

  public static final String VERSION_ARGUMENT_EXPECTED = "Version number omitted from the -version argument.";

  public static final String MODE_ARGUMENT_EXPECTED = "Type omitted from the -mode argument.";

  public static final String NO_FILE_SPECIFIED = "No file specified in the arguments. Exiting.";

  public static final String MODE_VERSION_IGNORED = "The mode and version arguments are ignored for epubs. "
      + "They are retrieved from the files.";

  public static final String MODE_REQUIRED = "Mode required for non-epub files. Default version is 3.0.";

  public static final String VALIDATING_VERSION_MESSAGE = "Validating using EPUB version %1$s rules.";

  public static final String OUTPUT_TYPE_CONFLICT = "Only one output format can be specified at a time.";

  public static final String HELP_TEXT =
          "nookepubcheck v." + EpubCheck.version() + "\n" +
          "When running this tool, the first argument should be the name (with the path)\n" +
          " of the file to check.\n" +
          "\n" +
          "If checking a non-epub file, the epub version of the file must\n" +
          " be specified using -v and the type of the file using -mode.\n" +
          " The default version is: 3.0.\n" +
          "\n" +
          "Modes and versions supported: \n" +
          "--mode opf -v 2.0\n" +
          "--mode opf -v 3.0\n" +
          "--mode xhtml -v 2.0\n" +
          "--mode xhtml -v 3.0\n" +
          "--mode svg -v 2.0\n" +
          "--mode svg -v 3.0\n" +
          "--mode nav -v 3.0\n" +
          "--mode mo  -v 3.0 // For Media Overlays validation\n" +                    // \  the bar is 100
          "--mode exp  // For expanded EPUB archives\n" +
          "\n" +
          "This tool also accepts the following options:\n" +
          "--save 	         = saves the epub created from the expanded epub\n" +
          "--out <file>     = output an assessment XML document file.\n" +
          "--json <file>    = output an assessment JSON document file\n" +
          "-m <file>        = same as --mode\n" +
          "-o <file>        = same as --out\n" +
          "-j <file>        = same as --json\n" +
          "--failonwarnings[+|-] = By default, the tool returns a 1 if errors are found in the file or 0 if no errors\n" +
          "                        are found.  Using --failonwarnings will cause the process to exit with a status of\n" +
          "                        1 if either warnings or errors are present and 0 only when there are no errors or warnings.\n" +
          "-f, --fatal      = include only fatal errors in the output\n" +
          "-e, --error      = include only error and fatal severity messages in ouput\n" +
          "-w, --warn       = include fatal, error, and warn severity messages in output\n" +
          "-u, --usage      = include ePub feature usage information in output\n" +
          "                    (default is OFF); if enabled, usage information will\n" +
          "                    always be included in the output file\n" +
          "\n" +
          "-l, --listChecks [<file>] = list message ids and severity levels to the custom message file named <file>\n" +
          "                          or the console\n" +
          "-c, --customMessages [<file>] = override message severity levels as defined in the custom message file named <file>\n" +
          "\n" +
          "-h, -? or --help = displays this help message\n";


}

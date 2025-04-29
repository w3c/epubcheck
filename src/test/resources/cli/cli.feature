Feature: EPUBCheck Command Line

	Tests the `epubcheck` command line interface.



	Background:
		Given EPUB test files located at '/cli/files/'



	Rule: Display help

		Example: Option `-h` is used to display the usage message 
			When running `epubcheck -h`
			Then the return code is 0
			And stdout is
				```
				EPUBCheck v{{VERSION}}
				
				When running this tool, the first argument should be the name (with the path)
				of the file to check.
				
				To specify a validation profile (to run checks against a specific EPUB 3 profile
				or extension specification), use the -profile option:
				
				Validation profiles supported:
				--profile default        = the default validation profile
				--profile dict           = validates against the EPUB Dictionaries and Glossaries specification
				--profile edupub         = validates against the EDUPUB Profile
				--profile idx            = validates against the EPUB Indexes specification
				--profile preview        = validates against the EPUB Previews specification
				
				If checking a non-epub file, the epub version of the file must
				be specified using -v and the type of the file using -mode.
				The default version is: 3.0.
				
				Modes and versions supported:
				--mode opf -v 2.0
				--mode opf -v 3.0
				--mode xhtml -v 2.0
				--mode xhtml -v 3.0
				--mode svg -v 2.0
				--mode svg -v 3.0
				--mode nav -v 3.0
				--mode mo  -v 3.0        = for Media Overlays validation
				--mode exp               = for expanded EPUB archives
				
				This tool also accepts the following options:
				--save                   = saves the epub created from the expanded epub
				--out <file>             = output an assessment XML document file (use - to output to console)
				--xmp <file>             = output an assessment XMP document file (use - to output to console)
				--json <file>            = output an assessment JSON document file (use - to output to console)
				-m <file>                = same as --mode
				-p <profile>             = same as --profile
				-o <file>                = same as --out
				-x <file>                = same as --xmp
				-j <file>                = same as --json
				--failonwarnings         = By default, the tool returns a 1 if errors are found in the file or 0 if no errors
				                           are found.  Using --failonwarnings will cause the process to exit with a status of
				                           1 if either warnings or errors are present and 0 only when there are no errors or warnings.
				-q, --quiet              = no message on console, except errors, only in the output
				-f, --fatal              = include only fatal errors in the output
				-e, --error              = include only error and fatal severity messages in ouput
				-w, --warn               = include fatal, error, and warn severity messages in output
				-u, --usage              = include ePub feature usage information in output
				                           (default is OFF); if enabled, usage information will
				                           always be included in the output file
				--locale <locale>        = output localized messages according to the provided IETF BCP 47 language tag string.
				
				-l, --listChecks [<file>]       = list message ids and severity levels to the custom message file named <file>
				                                  or the console
				-c, --customMessages [<file>]   = override message severity levels as defined in the custom message file named <file>
				
				--version                = displays the EPUBCheck version
				
				-h, -? or --help         = displays this help message


				```

		Example: Option `-?` is used to display the usage message 
			When running `epubcheck -?`
			Then the return code is 0
			And stdout contains "displays this help message"

		Example: Option `-help` is used to display the usage message 
			When running `epubcheck -?`
			Then the return code is 0
			And stdout contains "displays this help message"

		Example: Option `--help` is used to display the usage message 
			When running `epubcheck -?`
			Then the return code is 0
			And stdout contains "displays this help message"



	Rule: Display version information

		Example: Option `-version` is used to display the version
			When running `epubcheck -version`
			Then the return code is 0
			Then stdout is
				```
				EPUBCheck v{{VERSION}}

				```

		Example: Option `--version` is used to display the version
			When running `epubcheck --version`
			Then the return code is 0
			Then stdout is
				```
				EPUBCheck v{{VERSION}}

				```

		Example: Option `--version` is used when checking file
			When running `epubcheck --version {{valid.epub}}`
			Then the return code is 0
			Then stdout starts with "EPUBCheck v"
			And stdout contains "No errors or warnings detected."



	Rule: Reject invalid commands

		Example: no command argument
			When running `epubcheck`
			Then the return code is 1
			And stderr contains 'At least one argument expected'

		Example: unknown option
			When running `epubcheck --unknown {{valid.epub}}`
			Then the return code is 1
			And stderr contains 'Unrecognized argument: "--unknown"'



	Rule: Check EPUB publication

		Example: check a valid packaged EPUB
			When running `epubcheck {{valid.epub}}`
			Then the return code is 0
			And stderr is empty

		Example: check a packaged EPUB with warnings
			When running `epubcheck -mode exp {{20-warning-tester}}`
			Then the return code is 0
			And stderr contains 'WARNING(PKG-010)'

		Example: information about the EPUB version comes first
			Given stderr is redirected to stdout
			When running `epubcheck -mode exp {{30-mimetype-invalid}}`
			# FIXME 2022 ensure all messages are printed after the versioning info.
			# Then stdout starts with "Validating using EPUB version"

		Example: check a packaged EPUB with poorly cased extension
			# TODO this should be tested outside CLI tests
			When running `epubcheck {{wrong_extension.ePub}}`
			Then the return code is 0
			And stderr contains 'WARNING(PKG-016)'

		Example: check a packaged EPUB with non-epub extension
			# TODO this should be tested outside CLI tests
			When running `epubcheck -u --profile default {{wrong_extension.zip}}`
			And stdout contains 'USAGE(PKG-024)'
			Then the return code is 0

		Example: check a packaged EPUB with no extension
			# TODO this should probably raise a usage
			When running `epubcheck --profile default {{wrong_extension}}`
			Then the return code is 0



	Rule: Check single EPUB files

		Example: check a single valid navigation document
			When running `epubcheck -mode nav {{30-valid-test/OPS/nav.xhtml}}`
			Then the return code is 0

		Example: check a single invalid navigation document
			When running `epubcheck -mode nav {{nav-no-toc.xhtml}}`
			Then the return code is 1

		Example: check an unreadable file
			# TODO this should be tested outside CLI tests
			When running `epubcheck --mode nav http://localhost/notfound`
			Then the return code is 1



	Rule: Save an EPUB archive

		Example: save the resulting EPUB
			Given file '30-valid-test.epub' does not exist
			When running `epubcheck --mode exp {{30-valid-test}} --save`
			Then the return code is 0
			And file '30-valid-test.epub' was created



	Rule: Configure the verbosity

		Example: quiet mode prevents extra output
			When running `epubcheck -mode exp {{20-warning-tester}} --quiet`
			Then stdout is empty

		Example: quiet mode does not conflict with saving a report
			When running `epubcheck {{valid.epub}} --quiet -out {{report.xml}}`
			Then the return code is 0
			And stdout is empty
			But file 'report.xml' was created

		Example: USAGE messages are not reported by default
			When running `epubcheck -mode exp {{20-severity-tester}}`
			And stdout does not contain 'USAGE'

		Example: USAGE messages are reported with option `-u`
			When running `epubcheck -u -mode exp {{20-severity-tester}}`
			And stdout contains 'USAGE'

		Example: WARNING messages are reported with option `-w`
			When running `epubcheck -w -mode exp {{20-severity-tester}}`
			Then the return code is 1
			And stdout does not contain 'USAGE'
			And stderr contains 'WARNING'
			And stderr contains 'ERROR'

		Example: WARNING messages are silenced with option `-e`
			When running `epubcheck -e -mode exp {{20-severity-tester}}`
			Then the return code is 1
			And stdout does not contain 'USAGE'
			And stderr does not contain 'WARNING'
			And stderr contains 'ERROR'

		Example: ERROR messages are silenced with option `-f`
			When running `epubcheck -f -mode exp {{20-severity-tester}}`
			Then the return code is 0
			And stdout does not contain 'USAGE'
			And stderr does not contain 'WARNING'
			And stderr does not contain 'ERROR'



	Rule: Configure the rejection level

		Example: option `--faileonwarnings` make the command fail when a WARNING is reported
			When running `epubcheck --failonwarnings -mode exp {{20-warning-tester}}`
			Then the return code is 1
			And stderr contains 'WARNING(PKG-010)'



	Rule: Output reports

		Example: save an XML report with the `-out` option
			Given file 'report.xml' does not exist
			When running `epubcheck {{valid.epub}} -out {{report.xml}}`
			Then the return code is 0
			And file 'report.xml' was created

		Example: save an XML report with the `-o` option
			Given file 'report.xml' does not exist
			When running `epubcheck {{valid.epub}} -o {{report.xml}}`
			Then the return code is 0
			And file 'report.xml' was created

		Example: save an XMP report with the `-x` option
			Given file 'report.xmp' does not exist
			When running `epubcheck {{valid.epub}} -out {{report.xmp}}`
			Then the return code is 0
			And file 'report.xmp' was created

		Example: save a JSON report with the `-j` option
			Given file 'report.json' does not exist
			When running `epubcheck {{valid.epub}} -j {{report.json}}`
			Then the return code is 0
			And file 'report.json' was created

		Example: output a JSON report to the standard output
			Given file 'report.json' does not exist
			When running `epubcheck {{valid.epub}} -j -`
			Then the return code is 0
			And stdout contains '"title" : "Minimal EPUB 3.0"'
			But stdout does not contain 'No errors or warnings detected'

		Example: conflicting report formats are rejected
			When running `epubcheck {{valid.epub}} -o {{report.xml}} -j {{report.json}}`
			Then the return code is 1
			And stderr contains "Only one output format can be specified at a time."
			And file 'report.json' does not exist
			And file 'report.xml' does not exist



	Rule: Localize messages

		Example: `--locale` option can be used to localize messages
			Given the default locale is 'en-EN'
			When running `epubcheck {{valid.epub}} --locale fr-FR`
			Then stdout contains 'Aucune erreur ou avertissement détecté.'

		Example: unsupported locale falls back to the default locale
			Given the default locale is 'fr-FR'
			When running `epubcheck {{valid.epub}} --locale ar-eg`
			Then stdout contains 'Aucune erreur ou avertissement détecté.'

		Example: invalid locale falls back to the default locale
			Given the default locale is 'fr-FR'
			When running `epubcheck {{valid.epub}} --locale foobar`
			Then stdout contains 'Aucune erreur ou avertissement détecté.'

		Example: missing locale argument makes the command fail
			When running `epubcheck {{valid.epub}} --locale -h`
			Then the return code is 1
			And stderr contains 'Argument "-h" to the --locale option is incorrect.'

		Example: missing locale argument when last option makes the command fail
			When running `epubcheck {{valid.epub}} --locale`
			Then the return code is 1
			And stderr contains 'Argument to the --locale option is missing.'



	Rule: Override message severities

		Example: messages and severities are overridden with the `-c` option
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override.txt}}`
			Then the return code is 1
			And stdout does not contain 'USAGE('
			And stderr does not contain 'WARNING('
			And stderr contains 'ERROR('
			And stderr contains 'This is an overridden message'

		Example: messages and severities overridden with the `-c` option
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override_missing.txt}}`
			Then the return code is 1
			And stderr contains 'ERROR(CHK-001)'

		Example: report an error when overriding an unknown message ID
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override_bad_id.txt}}`
			Then the return code is 1
			And stderr contains 'ERROR(CHK-002)'

		Example: report an error when overriding to an unknown severity
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override_bad_severity.txt}}`
			Then the return code is 1
			And stderr contains 'ERROR(CHK-003)'

		Example: report an error when overriding to a message with parameters mismatch
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override_bad_message.txt}}`
			Then the return code is 1
			And stderr contains 'ERROR(CHK-004)'

		Example: report an error when overriding to a message suggestion with parameters mismatch
			When running `epubcheck -u -mode exp {{20-severity-tester}} -c {{severity_override_bad_suggestion.txt}}`
			Then the return code is 1
			And stderr contains 'ERROR(CHK-005)'


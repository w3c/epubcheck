---
title: Command Line Interface
---

This is the reference documentation for the EPUBCheck command line program.

:::note
If you prefer to run EPUBCheck with a graphical user interface, check the list of [third-party apps and tools](/docs/apps-and-tools/)!
:::

:::note important
This guide assumes the availability on [an `epubcheck` command](/docs/running/#the-epubcheck-command).
If you run the EPUBCheck program in any other way (for instance via the `java` command), please remember to replace the `epubcheck` command by your running method of choice when trying the examples in this guide!
:::


## Synopsis

```
epubcheck [<options>] [<path>]
```

The `epubcheck` command runs EPUBCheck to check the conformance of the EPUB file specified by the `<path>` argument.

## Options

`--version`
: Print the EPUBCheck version

`-?`, `-h`, `--help`
: Print the command line help

`-m <mode>`, `--mode <mode>`
: Set the file checking mode to `<mode>`

  The supported modes are:
  - `exp`: to check `<path>` as an expanded EPUB directory
  - `mo`: to check `<path>` as a single Media Overlays Document
  - `nav`: to check `<path>` as a single Navigation Document
  - `opf`: to check `<path>` as a single Package Document
  - `svg`: to check `<path>` as a single SVG Content Document
  - `xhtml`: to check `<path>` as a single XHTML Content Document

`--save`
: Create an EPUB package (`.epub`) after checking an expanded EPUB directory

  This option is ignored when not combined with the `--mode exp` option.

`-p <profile>`, `--profile <profile>`
: Use `<profile>` as the checking profile

  The supported profiles are:
  - `default`: check the rules defined in the EPUB specifications (default)
  - `dict`: run extra checks for the rules defined in the [EPUB Dictionaries and Glossaries](http://www.idpf.org/epub/dict/epub-dict.html) specifications
  - `edupub`: run extra checks for the rules defined in the [EPUB for Education](http://www.idpf.org/epub/profiles/edu/spec/) specification
  - `idx`: run extra checks for the rules defined in the [EPUB Indexes](http://www.idpf.org/epub/idx/epub-indexes.html) specification
  - `preview`: run extra checks for the rules defined in the [EPUB Previews](http://www.idpf.org/epub/previews/) specification

  When checking single files (as opposed to full EPUB publications), you can specify the targeted EPUB version with the `-v` option.
  When no version option is specified, version `3.0` is assumed by default.

`-v <version>`
: Target the `<version>` version of the EPUB specifications when checking a single file with the `--mode` option

  This option is ignored when not combined with the `--mode` option.

`-o <file>`, `--output <file>`
: Write an XML report to `<file>`

  Use the argument `-` instead of a file path to write the report to the standard output stream.

`-j <file>`, `--json <file>`
: Write a JSON report to `<file>`

  Use the argument `-` instead of a file path to write the report to the standard output stream.

`-x <file>`, `--xmp <file>`
: Write an XMP report to `<file>`

  Use the argument `-` instead of a file path to write the report to the standard output stream.

`--maxOfEachMessage <NUM>|unlimited`
: Limit the number of messages reported for each message-ID-and-text combination to <NUM>.
  
  This option only applies when producing a report in one of the JSON or XML formats.
  By default, if this option is not set, the report will only include a maximum of 25 messages for each message-ID-and-text combination.
  The keyword "`unlimited`" or a negative value can be used to unset the limit and include all the messages to the report.

`--failonwarnings`
: return `1` if either warnings or errors are present

  By default, EPUBCheck returns `1` if and only if it reports any errors, and returns `0` if only warnings were detected.

`-q`, `--quiet`
: Print nothing on the output stream.

  Error or warning messages are still printed on the error stream.

`-f`, `--fatal`
: Set the reported severity level to fatal 'fatal'

  Only fatal errors will be reported.

`-e`, `--error`
: Set the reported severity level to 'error'

  Only errors and fatal errors will be reported.

`-w`, `--warn`
: Set the reported severity level to 'warning'

  Only warnings, errors, and fatal errors will be reported.
: include fatal, error, and warn severity messages in output

`-u`, `--usage`
: Set the reported severity level to 'usage'

  All the messages will be reported, including informative messages.
  This option if off by default.

`--locale <locale>`
: Output messages in the given locale.

  `<locale>` must be an [IETF BCP 47](https://tools.ietf.org/html/bcp47) language tag string.

`-l [<file>]`, `--listChecks [<file>]`
: Write all the known message codes and severity levels to `<file>`, or to the standard output stream if the `<file>` argument is omitted

`-c <file>`, `--customMessages <file>`
: Override the message severity levels with the levels defined in `<file>`

## Usage

### Checking an EPUB publication

You can check a packaged EPUB publication with the command:

```shell-session
epubcheck publication.epub
```

Sometimes it is convenient to check an expanded (unpackaged) EPUB publication directory.
In such case you can use the command:

```shell-session
epubcheck --mode exp publication/
```

When using expanded mode, you can use the `--save` option to create a packaged EPUB (`.epub` file) after running the checks.

### Checking a single EPUB document

In addition to checking full EPUB publication, you can also check individual files contained in an EPUB, with the `--mode` option:

```shell-session
epubcheck <path to file> --mode <mode> -v <version>
```

The mode argument must be of the following:
  - **`opf`** to check a package document
  - **`nav`** to check a navigation document (available only for EPUB 3);
  - **`mo`** to check a media overlay document (available only for EPUB 3);
  - **`xhtml`** to check an XHTML content document
  - **`svg`** to check an SVG content document
  - **`exp`** to check an EPUB publication directory (see the previous section)

When using the mode option to check a single document, you need to tell EPUBCheck what version of EPUB you target with the `-v` option.
The supported versions are:
  - `2.0` for EPUB 2
  - `3.0` for EPUB 3

:::note
When checking a single file, only a subset of the available checks are run, as some checks depend on the inspection of a full publication.

When checking a full EPUB publication, the `--mode` and `-v` options are ignored.
:::

### Specifying a profile

You can specify a profile to enable the extra checks for the EPUB specification extensions, with the `--profile` option:

```shell-session
epubcheck <path to file> -profile <profile>
```

The supported profiles are:
  - `default`: check the rules defined in the EPUB specifications (default)
  - `dict`: run extra checks for the rules defined in the [EPUB Dictionaries and Glossaries](http://www.idpf.org/epub/dict/epub-dict.html) specifications
  - `edupub`: run extra checks for the rules defined in the [EPUB for Education](http://www.idpf.org/epub/profiles/edu/spec/) specification
  - `idx`: run extra checks for the rules defined in the [EPUB Indexes](http://www.idpf.org/epub/idx/epub-indexes.html) specification
  - `preview`: run extra checks for the rules defined in the [EPUB Previews](http://www.idpf.org/epub/previews/) specification

:::note
In most cases, it is not required to specify the profile explicitly, as it will automatically be set according to `dc:type` metadata in the Publication. Setting the profile explicitly can be useful to detect when such `dc:type` metadata is missing, or to check single files (see next section)
:::


### Getting a report in a structured format

By default EPUBCheck prints the reported messages to the standard output, in a textual format.
But you can alternatively write a report in a structured format.
This can be helpful for instance when you want to further process it within a production workflow.

The available report formats are:

- XML, enabled with the `--out` or `-o` option
- XMP, enabled with the `--xmp` or `-x` option
- JSON, enabled with the `--json` or `-j` option

The report can besaved to a file (specified as a path argument to the output format option), or printed to the standard output (when using `-` as the output format argument).

For example, to save a report in JSON, use the command:

```shell-session
epubcheck --json output.json file.epub
```

To print a JSON report to the standard output, use the command:

```shell-session
epubcheck --json - file.epub
```

You can read more about EPUBCheck reports in the [reporting guide](/docs/report/)!

## Any difficulty?

If you face any issue when using the EPUBCheck command line program, you can check our [troubleshooting guide](/docs/troubleshooting/), or try to [get help](/docs/support/)!

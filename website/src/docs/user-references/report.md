---
title: Reporting
---

This guide describes the reporting capabilities of EPUBCheck. It will show you the various output formats and how to enable them. You will also learn how you can programatically extend EPUBCheck to create a custom report format.

## Plain Text Report

By default, EPUBCheck writes the reported messages to the standard error stream, in a plain text format.

A message is usually printed on a single line, with the following structure:

```
<SEVERITY>(<CODE>): <PATH>(<LINE>,<COL>): <MESSAGE>
```

Where:

- `<SEVERITY>` is the severity of the message (one of `FATAL`, `ERROR`, `WARNING`, `USAGE`, or `INFO`)
- `<CODE>` is the code of the message (see the list in the [messages reference](/docs/messages/))
- `<PATH>` is the path of the file where the issue was detected
- `<LINE>` is the line number where the issue was detected, or `-1` if this information is not relevant
- `<COL>` is the column number where the issue was detected, or `-1` if this information is not relevant
- `<MESSAGE>` is the description of the issue


## XML Report

You can use the `--out` option to create a report in XML.

The XML report is based on the [JHOVE format](http://jhove.openpreservation.org).
This report will notably contain:
- the EPUB version in a `<version>` element
- a conformance status ("Not well-formed" or "Well-formed) in a `<status>` element
- the list of messages produced by EPUBCheck in a `<messages>` element
- further metadata information in a `<properties>` element

{# TODO link to full XML report samples? or wrap them in a details/summary 
Examples:
- https://github.com/w3c/epubcheck/blob/v4.2.2/src/test/resources/com/adobe/epubcheck/test/opf/Missing_Spine_epub3_expected_results.xml
- https://github.com/IDPF/epubcheck/blob/v4.2.2/src/test/resources/com/adobe/epubcheck/test/opf/Publication_Metadata_epub3_expected_results.xml
#}

This is an example of a list of messages included in a JHOVE XML report:

```xml
…
<messages>
  <message severity="error" subMessage="OPF-019">OPF-019, FATAL, [Spine tag was not found in the OPF file.], OPS/content.opf</message>
  <message severity="error" subMessage="RSC-005">RSC-005, ERROR, [Error while parsing file 'element "package" incomplete; missing required element "spine"'.], OPS/content.opf (13-11)</message>
  <message severity="error" subMessage="RSC-011">RSC-011, ERROR, [Found a reference to a resource that is not a spine item.], OPS/toc.xhtml (11-36)</message>
  <message severity="info" subMessage="ACC-007">ACC-007, HINT, [Content Documents do not use 'epub:type' attributes for semantic inflection.], OPS/page01.xhtml</message>
  <message severity="info" subMessage="ACC-008">ACC-008, HINT, [Navigation Document has no 'landmarks nav' element.], Missing_Spine_epub3.epub</message>
</messages>
…
```

Like any XML document, the XML report can be processed with XML processing tools.
For instance, you could use XSLT to convert it in another format of choice.

If you prefer to directly output another format, you can see how to [create a custom report](#custom-report).

## JSON Report

You can use the `--json` option to create a report in JSON.

The JSON report is in a proprietary format. It is an object with:
- a `checker` property containing metadata on the EPUBCheck run (path of the checked file, EPUBCheck version, number of errors/warnings, duration, etc)
- a `publication` property containing metadata on the checked publication
- an `items` property containing an array of the EPUB items, along with their associated properties
- and `messages` property containing an array of the messages reported by EPUBCheck

{# TODO add an example of a JSON report after the format is refactored #}

## Custom Report

If the existing report formats do not suit your needs, you can extend EPUBCheck to create a report in a custom format.
To do that, you need to write a Java class implementing the `com.adobe.epubcheck.api.Report` interface.

In particular, the `info(String resource, FeatureEnum feature, String value)` method will be called during the parsing process each time a particular feature has been detected in the EPUB file.
The list of the currently detected features can be found in the `com.adobe.epubcheck.util.FeatureEnum` enumeration.

As a starting point, you can use the `com.adobe.epubcheck.util.XmlReportImpl` class or the `com.adobe.epubcheck.util.ValidationReport` test class.
You might also want to take a look in the default Report implementation in `com.adobe.epubcheck.util.DefaultReportImpl`

Once your new report class is implemented, you can tell EPUBCheck to use it by [calling EPUBCheck as a Java library](/docs//java-library/).

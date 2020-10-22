---
title: Running EPUBCheck
nav:
  section: getting-started
---

This guide will explain you how to check a first EPUB publication with the EPUBCheck command line program.
It assumes you have a functional [installation](/docs/installation/) of EPUBCheck.

## The `epubcheck` command

The command you'll use to run EPUBCheck depends on [how you installed EPUBCheck](/docs/installation/#installing-epubcheck).

If you installed the official distribution by unzipping a ZIP archive,
you will likely run EPUBCheck via the `java` command:

```shell-session
java -jar /path/to/epubcheck/epubcheck.jar --version
```

Alternatively, if you manually created a command shortcut, or if you installed EPUBCheck via a package manager,
you will likely run EPUBCheck directly via an `epubcheck` command:

```shell-session
epubcheck --version
```

In the sections below, **we will assume the existence of an `epubcheck` command**.
Please remember to replace this command by your running method of choice when trying the examples in this guide!

## Checking an EPUB file

To check an EPUB file, use the file path as the argument to the `epubcheck` command.

For instance, if you're in a directory containing the EPUB file `moby-dick.epub`, run the following command:

```shell-session
epubcheck moby-dick.epub
```

If the file is a conforming EPUB 3 publication, the command will return the following message:

```
Validating using EPUB version 3.4 rules.
No errors or warnings detected.
Messages: 0 fatals / 0 errors / 0 warnings / 0 infos

EPUBCheck completed
```

On the first line, EPUBCheck tells you the version of the EPUB specification it is checking the conformance to.
Then it tells you if errors were found (in this case, no).
Finally, it gives you a summary of the number of errors, warnings, and informative messages reported about your EPUB.

## Checking an expanded EPUB

Sometimes during the development of your EPUB publication, you will prefer to work with an expanded (unzipped) EPUB directory.
In such case, you can check the expanded EPUB directory by specifuing the `-mode exp` option:

```shell-session
epubcheck -mode exp moby-dick/
```

## Reviewing the message log

When your EPUB is not conforming, EPUBCheck will produce a list of errors or warnings to the standard error stream.

For instance, let's assume you're checking an EPUB which is declaring a manifest item which does not corresponds to any file in the EPUB.
EPUBCheck will produce the following output:

```text/2
Validating using EPUB version 3.4 rules.
ERROR(RSC-001): ./manifest-item-missing-error.epub(-1,-1): File "EPUB/missing.xhtml" could not be found.

Check finished with errors
Messages: 0 fatals / 1 error / 0 warnings / 0 infos

EPUBCheck completed
```

In the sample output above, you can see the structure of the messages reported by EPUBCheck:

- `ERROR` tells you the severity of the issue
- `RSC-001` is the code of the issue (for reference)
- `./package-manifest-item-missing-error.epub(-1,-1)` is the path of the file where the error was detected, possibly with a line and column location.
  In this case it is the EPUB itself (as the error is about a missing resource), which a line and column location of `-1` meaning "no specific location".
- `File "EPUB/missing.xhtml" could not be found` is the actual message text.

From this list of reported errors or warnings, you can now try to fix the EPUB accordingly!

## What next?

In this getting started guide, you've learned how to install the EPUBCheck command line program and check your first EPUB publications. Well done!

To learn more about EPUBCheck, you can:

- explore all the command line options and usage details in the [command line reference](/docs/cli/)
- understand the meaning of EPUBCheck messages by looking them up in the [messages reference](/docs/messages/)
- check out more advanced usage of EPUBCheck like [reporting options](/docs/report/) or as a [Java library](/docs/java-library/)

And do not forget:

- you can [get help](/docs/support/) if you face a difficulty when using EPUBCheck
- do not hesitate to [file an issue](/docs/creating-issues/) if you think you found a bug or have a feature request
- you're welcome to check the [contributing guides](/docs/contributing/) if you want to help the project!

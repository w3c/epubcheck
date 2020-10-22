---
title: Installation
---

This guide will explain you how to setup your system and install the EPUBcheck command line program.

::: note
These docs assume that you know how to run a program in a command line shell.
If you need to read up on how to do that, check the web for [command line primers](https://duckduckgo.com/?q=command+line+primer)!
:::

## Installing Java

To use EPUBCheck on the command line, you'll need Java 7 or or a later version.

You can check if Java is already installed on your computer by running the following command:

```shell-session
java -version
```

If Java is available, the command will return the version of Java called with the `java` command, for instance:

```
openjdk version "11.0.9.1" 2020-11-04
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.9.1+1)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.9.1+1, mixed mode)
```

If you get an error message saying the `java` command could not be found, it means you need to install Java.

There are several providers of Java virtual machines (JVM), and several ways to install Java, depending on your operating system.
If you're new to Java or do not know what to install, we recommend downloading a [pre-built binary from AdoptOpenJDK](https://adoptopenjdk.net), with the default options.

:::note important
EPUBCheck requires Java 7 or a later version.
{# TODO get CI Java versions in data from travis.yml
It is regularly tested on OpenJDK versions {{java.versions}} and Oracle JDK versions {{java.version}}
#}
:::

## Installing EPUBCheck

### Using the official distribution

We distribute EPUBCheck as a ZIP archive (`.zip` file) containing the EPUBCheck binaries and all its dependencies.

To install EPUBCheck, you need to follow these steps:

1. download the [latest version of EPUBCheck]({{githubRelease.latest.url}})
2. unzip the downloaded ZIP file

:::note
You can move the unpacked archive content wherever you want in your file system.
But please be careful to always keep together all the files, especially the `epubcheck.jar` file (the EPUBCheck binaries) and the `lib/` directory (the dependencies binaries).
:::

:::note quicktip
On macOS and Linux, you can use the following command to directly download and unzip the latest version of EPUBCheck:
```shell-session
curl -SL {{ githubRelease.latest.url }} | bsdtar -xf -
```
or, if `bsdtar` is not available on your system, you can use the following commands:
```shell-session
curl -SL {{ githubRelease.latest.url }} -o epubcheck.zip
unzip epubcheck.zip
```
:::

To run EPUBCheck and verify that your installation is functional, go to the unpacked directory {# TODO get the version from global data (for instance `epubcheck-{{version}}`) #} and run the following command:

```shell-session
java -jar epubcheck.jar --version
```

The command should return the version of EPUBCheck you just installed. For instance:

```
EPUBCheck v4.2.4 {# TODO get the version from gobal data #}
```

:::note
You can run EPUBCheck from anywhere in your file system by specifying the absolute path to the `epubcheck.jar` binary, like so:

```shell-session
java -jar /path/to/epubcheck/epubcheck.jar
```
:::

{# TODO add a note on how to create an `epubcheck` command? #}

### Using a package manager

Alternatively to the previous method, you can possibly download and install EPUBcheck from a package manager.
This method can be easier as it will typically create an `epubcheck` command that you can run anywhere in your file system (without having to call it through the `java` command).

:::note
We do not officially maintain the distribution of EPUBCheck in package managers.
But kind volunteers are regularly keeping the following popular options up-to-date!
:::

#### On macOS

You can install EPUBCheck via [Homebrew](https://brew.sh/) with the command:

```shell-session
brew install epubcheck
```

#### On Ubuntu or Debian

You can install EPUBCheck via [APT](https://en.wikipedia.org/wiki/APT_(software)) with the command:

```shell-session
apt-get install epubcheck
```

## Updating EPUBCheck

EPUBCheck does not come with a built-in updater tool.
To update EPUBcheck to a newer version (or similarly revert to an older version),
you'll need to download the new version and replace the content of your EPUBCheck directory with the content of the newly downloaded ZIP archive. You can follow the steps above to [install the latest EPUBCheck version](#using-the-official-distribution).

Alternatively, if you installed EPUBCheck with a [package manager](#using-a-package-manager),
you should be able to use the same package manager to update EPUBCheck. Please refer to the documentation of your package manager of choice to know the udpate procedure.

## Ready to run?

If you successfully went through the steps described in this guide, you now have a functional installation of EPUBCheck… congratulations! You can now try to [check a first EPUB publication](/docs/running/).

If anything went wrong and you could not install EPUBCheck, do not worry! We do not want to let you down… You can probably [get help](/docs/support/), or maybe try using EPUBCheck with a [third-party graphical user interface](/docs/apps-and-tools/)?
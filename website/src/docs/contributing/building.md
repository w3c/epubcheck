---
title: Building and Testing
---

This guide describes how to build EPUBCheck from the source code, and how to run the tests.

## Building EPUBCheck

EPUBCheck is built with [Apache Maven](http://maven.apache.org/) (version 3 or higher is recommended). Building requires a Java Development Kit (JDK) version 7 or higher.

To build the EPUBCheck `jar` or `ZIP` distribution, use the command:

```shell-session
mvn clean package
```

To install the `jar` in your local Maven repository, use the command:

```shell-session
mvn clean install
```

## Running Tests

### Running tests from the command line

To run the whole EPUBCheck test suite, use the command:
```
mvn clean verify
```

:::note
The `mvn verify` command will run all the functional, unit, and integration tests.
:::

To run all the tests except the integration tests, use the command:

```
mvn clean test
```

You can also run a single test class using the `test` property.
For instance:

```
mvn -Dtest=VocabTest test
```

{# TODO describe the test results, how to read them. #}

### Running tests from the development environment

Some development environments support running tests directly from their user interface, either natively or with plugins.

In Eclipse, you can use the default JUnit plugin to run all the tests.

## Setting Up the project in Eclipse

The Eclipse IDE is a popular development environment among Java developers.
To configure EPUBCheck as a project in Eclipse, the recommened appraoch is to import it as an "Existing Maven Project".
Select `File > Import... > Maven > Existing Maven Projects`, then browse for the directory containing the `pom.xml` file (where you checked out the git repository) and select `Finish`.

## Continuous Integration

The EPUBCheck project uses [Travis CI](https://travis-ci.com/github/w3c/epubcheck) to automatically run the test suite on the `master` branch and for any pull request.
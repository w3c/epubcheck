---
title: Writing Tests
---

{# TODO simplify language, avoid passive voice #}

### Overview

EPUBCheck’s _functional tests_ form the biggest and most crucial part of the test suite.
Their role is to verify that EPUBCheck correctly evaluates the conformance criteria defined in the EPUB specifications.

These tests follow a “black box” testing approach:
the principle is to run EPUBCheck on an input file (or set of files), and then to inspect the resulting output, without having to know exactly how the logic is implemented internally.

To make the functional tests as readable as possible,
and also understandable and maintainable by people who aren’t necessarily familiar with the Java language,
the functional tests are written in the [_Gherkin_ language](https://cucumber.io/docs/guides/overview/#what-is-gherkin) and execute with the [_Cucumber_ tool](https://cucumber.io/docs/guides/overview/#what-is-cucumber).
- The Gherkin language allows you to describe test cases in a natural language syntax, in plain text files (`.feature` files).
- Cucumber is the tool that parses the test descriptions and executes the associated Java code, to evaluate if the tests pass or fail.

The following section describes the structure of a Cucumber test.

### A Cucumber Test

Test cases, also known as _scenarios_ in Cucumber lingo, are defined in _feature files_ (files with a `.feature` extension) as a set of readable instructions, called _steps_.
Here is an example of the content of a simple feature file:

```
Feature: Validate EPUB 3
  
  EPUBCheck should check conformance to the latest EPUB 3 specifications,
  available at: https://www.w3.org/publishing/epub3/

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings

  Scenario: valid EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  Scenario: invalid EPUB
    When checking EPUB 'schema-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
```

This feature file contains two tests, one for a valid EPUB, the other for an invalid EPUB (due to a schema error).
It has a specific structure, and some lines start with a _keyword_ (`Feature`, `Scenario`, `Given`, `When`, `Then`, `And`, etc), but is generally fairly readable.
Let’s take it apart and describe what it means, piece by piece.

The first line of a feature file consists of the keyword `Feature`, followed by a colon and a title:

```
Feature: Validate EPUB 3
```

The title (here, "Validate EPUB 3") should briefly identify the topic or feature being tested.
It will be present in the test report produced by Cucumber, and can be used by the evaluator to know what the tests are about.

The next lines contain a longer description of the topic or feature being tested:

```
  EPUBCheck should check conformance to the latest EPUB 3 specifications,
  available at: https://www.w3.org/publishing/epub3/
```

This longer description is optional.
It can be of any lengh, and ends when the next line starts with a keyword (typically `Background` or `Scenario`).
In the EPUBCheck test suite, the feature description is used to explain what part of the EPUB specifications is tested.

After the feature description comes a `Background` section, here containing two steps:

```
  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
```

A _step_ is any line starting with any of the keywords `Given`, `When`, `Then`, `And`, or `But`.
Each step is associated with some Java code (see the section [Underlying Java Code](#underlying-java-code));
Cucumber will execute this code in sequence, one step at a time, when running the tests.
The steps included in a `Background` section will be run before each scenario.
This section is therefore useful to define the initial steps shared by all the tests in the feature file.

In the EPUBCheck test suite, `Background` section is used to define the location where to find the test files, and to specify the EPUBCheck configuration shared by all the tests in the feature.
These steps and their syntax are described at length in the [Steps Reference](#steps-reference).

The rest of the feature file basically consists of `Scenario` sections.
Each scenario describes one test case, as a sequence of steps.
It also has a title, consisting of the text string after the colon character following the `Scenario` keyword.
The scenario titles are arbitrary; they are used to identify the different scenarios in the test report.

For instance, the first `Scenario` in our example feature file is named "valid EPUB" and checks that no errors or warnings are reported when EPUBCheck is run on a minimalistic valid EPUB:

```
  Scenario: valid EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported
```

Because the feature file contains a `Background` section (as seen earlier), the sequence of steps that are actually executed when evaluating this test case are the following:

```
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
    When checking EPUB 'minimal'
    Then no errors or warnings are reported
```

This sequence of steps follows a typical pattern that is found in all the scenarios:

1. description of the initial context (steps `Given` and `And` above)
2. description of an action (step `When`)
3. description of the expected result (step `Then`)

Note that a scenario can have any number of steps.
The keywords `And` and `But` can be used to improve the reading flow.
In fact, the step keywords are not actually differentiated in the underlying implementation, they just improve the readability.
A sequence "_given_ a context, _when_ this happens _and_ this also happens, _then_ this is expected _but_ this isn't expected"
is more readable than the equivalent sequence "_given_ a context, _when_ this happens _when_ this also happens, _then_ this is expected _then_ this isn't expected",
or even the also equivalent but illegible sequence "_then_ a context, _then_ this happens _then_ this also happens, _then_ this is expected _then_ this isn't expected".

The second scenario in our sample feature file describes a test for when EPUBCheck is expected to report an error for an invalid EPUB:

```
  Scenario: invalid EPUB
    When checking EPUB 'schema-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
```

At this point, it is important to note that the text of the steps (for instance "checking EPUB 'schema-error'" or "error RSC-005 is reported") is what is used by Cucumber to identify what piece of Java code to execute in order to run the test.
Each step should correspond to a matching _step definition_, i.e. a Java method that implements the step’s logic.

However, test authors typically do not have to write any line of Java code themselves, as the EPUBCheck test suite already includes a collection of step definitions which intend to cover the variety of EPUBCheck test cases.
Writing a new test case for EPUBCheck consists of describing a scenario in a plain text feature file, using any of the readily available steps listed in the [Steps reference](#steps-reference) below.

{# TODO explain how to run and debug a single test (using the @debug tag #}

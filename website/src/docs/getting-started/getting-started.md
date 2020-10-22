---
title: Getting Started
nav:
  section: docs
  tocTitle: Introduction
---

This getting started guide will walk you through installing EPUBCheck and running it to check a first EPUB publication.
It assumes you are prepared to use a command line terminal program.
After finishing this guide, you will have a functional setup to run on many more EPUB files!

Before diving into the practical details, the following sections will help you understand the scope of the EPUBCheck tool.

## What is EPUBCheck?

EPUBCheck is **the official conformance checker for EPUB publications**. This short statement includes a lot of information already:

- _official_: EPUBCheck is a project owned by [W3C](https://w3.org/publishing).
  It is under the responsibility of the same group of people who define the EPUB format.
- _conformance checker_: EPUBCheck evaluates if your EPUB publications conform to the requirements defined in the EPUB specifications.
- _for EPUB publications_: EPUBCheck supports the latest stable versions of either [EPUB 2](http://idpf.org/epub/201) or [EPUB 3](https://www.w3.org/publishing/epub3/).

EPUBCheck is distributed as a command line program.
It is also available as a Java software library, to be integrated as a component in larger systems.

## Who is it for?

EPUBCheck is made for both content creators and content distributors.
In fact, most of the digital publishing actors rely on EPUBCheck as a critical step in their workflows.

Using the official distribution of EPUBCheck will require you to be familiar with running programs in a command-line shell.
But if you prefer to use a graphical user interface, it is absolutely fine!
Good people created [third-party apps and tools](/docs/apps-and-tools/) which can provide a friendlier user experience.

## Why check your EPUBs?

EPUBCheck can detect mistakes or defects in EPUB publications.
These flaws may cause interoperability issues and degrade the reading experience.

Content distributors will typically use EPUBCheck to decide if digital publications can be accepted in their catalogs.
Most retailers will not accept an EPUB file unless it successfully passes EPUBCheck scrutiny.
Content producers can use EPUBCheck to catch unintentional mistakes early on, so they can fix them.

## What can it detect?

EPUBCheck will scan technical aspects of your EPUB, including:

- the structure of the container
- the package description file and associated metadata
- the content markup
- the consistency of internal references

But EPUBCheck cannot play an editorial role. It will not detect typos, mispellings, off-topic images, etc.

## Let’s start!

When you’re ready to get started, move on to the next section to learn [how to install EPUBCheck](/docs/installation/).
---
title: EPUBCheck
layout: 'layouts/home.html'
intro:
  description: The conformance checker for EPUB publications
  downloadText: "Download"
  downloadURL: "<see elventyComputed below>"
  getStartedText: "Get Started"
  getStartedURL: "https://example.org"
quickstart:
  text: EPUBCheck is a command line tool. Unzip the downloaded package, open a terminal program, and start checking your EPUB files!
  command: |
    epubcheck moby-dick.epub
    Validating using EPUB version 3.4 rules.
    No errors or warnings detected!
  link: Learn more on [how to get started](docs/getting-started/).
features:
  - title: Open Source.
    icon: open.svg
    text: |
      EPUBCheck is open source, licensed under MIT.

      EPUBCheck is a W3C project maintained by the DAISY Consortium.
  - title: Standard.
    icon: open.svg
    text: |
      EPUBCheck evaluates EPUB publications against the official EPUB specifications.

      It supports both [EPUB 2](http://idpf.org/epub/201) and [EPUB 3](https://www.w3.org/publishing/epub3/).
  - title: Internationalized.
    icon: open.svg
    text: |
      EPUBCheck is translated in a varierty of languages.
      
      You can help to [contribute to the localization effort](docs/translating/)!
  - title: Looking for an app?
    icon: open.svg
    text: |
      EPUBCheck is distributed as a command line tool or a Java library.

      If you prefer using an app, see this list of [third-party tools integrating EPUBCheck](docs/apps-and-tools/)!
eleventyComputed:
  intro:
    downloadURL: "{{ githubRelease.latest.url }}"
---
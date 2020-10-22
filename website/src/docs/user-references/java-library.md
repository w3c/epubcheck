---
title: Java Library
---

This guide will briefly describe how to use EPUBCheck as a Java library.

As an alternative to using it as a command line program, you can call EPUBCheck programmatically with its Java API.
You will find the EPUBCheck public interfaces in the `com.adobe.epubcheck.api` package.

## The `EPUBCheck` class

The `EPUBCheck` class implements the EPUBCheck processing engine.
You will typically call EPUBCheck by calling the `validate()` method on an instance of this `EPUBCheck` class.

Here is a basic example of how to call EPUBCheck programmatically:

```java
File epubFile = new File("/path/to/your/epub/file.epub");

// simple constructor; errors are printed on stderr stream
EPUBCheck epubcheck = new EPUBCheck(epubFile);

// validate() returns true if no errors or warnings are found
boolean result = epubcheck.validate();
```

The official ZIP distribution contains all you need to use EPUBCheck as a library: the `epubcheck.jar` Java archive for EPUBCheck, and all its dependencies in the `lib` directory.


## Using EPUBCheck in your build

In addition to being released as a ZIP archive on GitHub, EPUBCheck is also officially published to the [Maven Central Repository](https://search.maven.org/artifact/org.w3c/epubcheck).

EPUBCheck’s Maven group ID is `org.w3c` and its artifact ID is `epubcheck`. You can copy the snippets below to add a dependency to EPUBCheck in your build system of choice:

{# todo get artifact+version information from global data #}

### Maven

```xml
<dependency>
    <groupId>org.w3c</groupId>
    <artifactId>epubcheck</artifactId>
    <version>4.2.4</version>
</dependency>
```

### Gradle

```groovy
compile group: 'org.w3c', name: 'epubcheck', version: '4.2.4'
```

### Ivy

```xml
<dependency org="org.w3c" name="epubcheck" rev="4.2.4" />
```

### Buildr

```ruby
compile.with 'org.w3c:epubcheck:jar:4.2.4'
```

### SBT

```scala
libraryDependencies += "org.w3c" % "epubcheck" % "4.2.4"
```
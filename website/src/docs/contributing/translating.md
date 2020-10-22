---
title: Translating EPUBCheck
---

This guide describes how you can help us translate EPUBCheck in your own language. This will make us happy, and will likely be helpful to your local community!


## Contribute new languages

### via Transifex *(preferred way)* 

We use [Transifex](https://www.transifex.com) as *translation management system* and we encourage you to join Transifex if you'd like to contribute a new language.

To do so, or to improve an existing one, please join Transifex and the [EPUBCheck project on Transifex](https://www.transifex.com/idpf/epubcheck/), request a new language and start translating the messages in the Transifex web app.

#### Testing

To test your changes in a development environment you need to set up the EPUBCheck Java repository (plus install Apache Maven, see [Build](../build) for details) and [install](https://docs.transifex.com/client/installing-the-client) the [Transifex commandline client](https://docs.transifex.com/client/introduction/).

Since the transifex file format and the commandline client have some issues (see 'TODOs' in [#479](https://github.com/w3c/epubcheck/pull/479)) we have [a separate Bash script](https://github.com/w3c/epubcheck/pull/479#issuecomment-146994023) which wraps the Transifex Commandline Client and normalizes the message properties files upon a transifex pull.

To run this script you need to install the Transifex Commandline Client and have a Transifex account.

Next you need to set up your [Transifex API key](https://www.transifex.com/user/settings/api/) in `~/.transifexrc`:

```
[https://www.transifex.com]
api_hostname=https://api.transifex.com
hostname=https://www.transifex.com
password=FILL_IN_YOUR_API_KEY_HERE
username=api
```

Then you need to run the wrapper script from EPUBCheck repo root directory!
e.g. `./src/build/transifex-pull.sh --all`

```
usage: transifex-pull.sh [--all | <locale>]
examples:
  transifex-pull.sh --all
  transifex-pull.sh de
  transifex-pull.sh ko_KR
```

*Note for Windows users: You need a Bash Console like Cygwin, GitBash, etc. installed in order to run the EPUBCheck transifex client!*

#### Pull Request

When you're finished with translation, please open a new [Issue(!)](https://github.com/w3c/epubcheck/issues/new) on GitHub and assign `@tofi86`.

`@tofi86` also coordinates the Transifex localization project and can answer your questions about it.


### Alternative: manual translation

If you don't want to join Transifex you can manually translate EPUBCheck messages. Simply duplicate the original (english) message files (`MessageBundle.properties` or `messages.properties`) located in:

* [src/main/resources/com/adobe/epubcheck/messages/](https://github.com/w3c/epubcheck/tree/master/src/main/resources/com/adobe/epubcheck/messages/)
* [src/main/resources/com/adobe/epubcheck/util](https://github.com/w3c/epubcheck/tree/master/src/main/resources/com/adobe/epubcheck/util)
* [src/main/resources/com/thaiopensource/datatype/xsd/resources](https://github.com/w3c/epubcheck/tree/master/src/main/resources/com/thaiopensource/datatype/xsd/resources)
* [src/main/resources/com/thaiopensource/relaxng/pattern/resources](https://github.com/w3c/epubcheck/tree/master/src/main/resources/com/thaiopensource/relaxng/pattern/resources)
* [src/main/resources/com/thaiopensource/validate/schematron/resources](https://github.com/w3c/epubcheck/tree/master/src/main/resources/com/thaiopensource/validate/schematron/resources)
* [src/main/resources/org/idpf/epubcheck/util/css](https://github.com/w3c/epubcheck/tree/master/src/main/resources/org/idpf/epubcheck/util/css)

to `MessageBundle_XX.properties` or `messages_XX.properties` in its respective folder and start translating.

#### Testing

When testing your translation on a system whose locale doesn't match the translation locale (e.g. translating *pt_BR* on a *en_US* system) add the `--locale xx_YY` argument added with EPUBCheck 4.1.0  to your `java` or `mvn` commandline call – e.g. `java -jar epubcheck.jar --locale pt_BR test.epub` to test your new 'brasilian portuguese' translation.

#### Pull Request

When you're finished with translation, please send us a PullRequest with your changes and assign `@tofi86`.

## Report typos and fixes

If you think the EPUBCheck messages in your language need some improvement, please open a new [Issue](https://github.com/w3c/epubcheck/issues/new) on GitHub and contact the localization maintainer (see list below).

## Current translations and their maintainers:

* `en` - English
  * *(default)*
* `ja` - Japanese
  * *Masayoshi Takahashi ([@takahashim](https://www.github.com/takahashim))*
  * *Satoshi KOJIMA ([@skoji](https://www.github.com/skoji))*
* `de` - German
  * *Tobias Fischer ([@tofi86](https://www.github.com/tofi86))*
* `es` - Spanish
  * *Emiliano Molina ([@Cuadratin](https://www.github.com/Cuadratin))*
* `fr` - French
  * *Vincent Gros ([@vincent-gros](https://www.github.com/vincent-gros))*
* `it` - Italian
  * *Alberto Pettarin ([@pettarin](https://www.github.com/pettarin))*
  * *Fondazione LIA (esp. Elisa Molinari, Gregorio Pellegrino)*
* `nl` - Dutch
  * *Merijn de Haen*
* `ko_KR` - Korean
  * *Woongyoung Park*
* `pt_BR` - Portuguese (Brazil)
  * *Thiago de Oliveira Pereira ([@thiagoeec](https://www.github.com/thiagoeec))*
* `da` - Danish
  * *Marianne Gulstad ([@MyDK](https://www.github.com/MyDK))*
* `zh_TW` - Traditional Chinese (Taiwan)
  * *Bobby Tung ([@bobbytung](https://github.com/bobbytung))*
---
title: Troubleshooting
---

This troubleshooting guide lists a few common errors that you may encounter when using EPUBCheck. If you'd like to suggest another entry, or have a better solution for one of these, feel free to [file an issue to GitHub](/docs/creating-issues/)!

### "File not found" error

If EPUBCheck fails because of Unicode characters in file paths, the following command line parameters can be used:

For example ("Mały Książę" = "Le Petit Prince" in Polish):
```
java -Dsun.jnu.encoding=UTF8 -Dfile.encoding=UTF8 -jar epubcheck.jar "Mały Książę - Antoine de Saint-Exupéry.epub"
```


### "Stack overflow" error

If EPUBCheck crashes with a `StackOverflowError`, try adjusting the thread stack size of your Java Virtual Machine. With most Java distributions, this can be done by using the `-Xss` option of the `java` command, like in the following example:

```
java -Xss1024k -jar epubcheck.jar moby-dick.epub
```
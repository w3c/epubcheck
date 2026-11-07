package com.adobe.epubcheck.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArchiveTest
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testExpandedArchiveTimestamps() throws Exception
  {
    Path book = folder.newFolder("book").toPath();
    String[] names = {"mimetype", "chapter.xhtml", "caf\u00e9.css"};
    String[] contents = {"application/epub+zip", "<html/>", "body {}"};
    // An even second that can be stored exactly in the standard ZIP timestamp.
    long modified = 1705320000000L;
    for (int i = 0; i < names.length; i++)
    {
      Path file = Files.write(book.resolve(names[i]), contents[i].getBytes(UTF_8));
      Files.setLastModifiedTime(file, FileTime.fromMillis(modified));
    }

    File epub = folder.newFile("output.epub");
    new Archive(book.toString(), true).createArchive(epub);
    Set<String> remaining = new HashSet<>(Arrays.asList(names));
    try (ZipInputStream local = new ZipInputStream(new FileInputStream(epub));
        ZipFile central = new ZipFile(epub))
    {
      assertEquals(names.length, central.size());
      ZipEntry entry = local.getNextEntry();
      assertEquals("mimetype", entry.getName());
      while (entry != null)
      {
        String name = entry.getName();
        assertTrue(name, remaining.remove(name));
        ZipEntry directoryEntry = central.getEntry(name);
        assertEquals(modified, directoryEntry.getTime());
        assertEquals(name.equals("mimetype") ? ZipEntry.STORED : ZipEntry.DEFLATED, entry.getMethod());
        assertNull("Local extra fields for " + name, entry.getExtra());
        assertNull("Central extra fields for " + name, directoryEntry.getExtra());
        entry = local.getNextEntry();
      }
      assertTrue(remaining.isEmpty());
    }
  }
}

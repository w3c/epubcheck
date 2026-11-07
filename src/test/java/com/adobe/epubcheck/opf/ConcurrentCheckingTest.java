package com.adobe.epubcheck.opf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.adobe.epubcheck.tool.EpubChecker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConcurrentCheckingTest
{

  private static final Pattern UUID = Pattern
      .compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  private static final int CHAPTERS = 9;
  private static final int MALFORMED_CHAPTER = 5;

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void fatalParseErrorIsReportedOnce() throws Exception
  {
    File epub = buildEPUB("fatal", true, false, false);

    List<JsonNode> sequential = messages(epub, 1);
    List<JsonNode> concurrent = messages(epub, 4);

    assertEquals("expected exactly one fatal error", 1, count(sequential, "RSC-016"));
    assertEquals("expected exactly one fatal error", 1, count(concurrent, "RSC-016"));

    JsonNode fatal = find(concurrent, "RSC-016");
    assertEquals("the fatal error must keep its parse location", 1, fatal.get("locations").size());
    assertTrue(fatal.get("locations").get(0).get("line").asInt() > 0);

    assertEquals("concurrent checking must not change the report", normalize(sequential),
        normalize(concurrent));
  }

  @Test
  public void everyMalformedDocumentReportsItsOwnFatalError() throws Exception
  {
    File epub = buildEPUB("multiple-fatal", new HashSet<>(Arrays.asList(2, 5)), false, false);

    List<JsonNode> sequential = messages(epub, 1);
    List<JsonNode> concurrent = messages(epub, 4);

    assertEquals("each malformed document must report its own fatal error", 2,
        locations(sequential, "RSC-016").size());
    assertEquals("each malformed document must report its own fatal error", 2,
        locations(concurrent, "RSC-016").size());
    assertEquals(normalize(sequential), normalize(concurrent));
  }

  @Test
  public void featureLocationsAreDeterministic() throws Exception
  {
    File epub = buildEPUB("microdata", false, true, false);

    String expected = "OEBPS/ch1.xhtml";
    for (int run = 0; run < 3; run++)
    {
      JsonNode message = find(messages(epub, 4, "--profile", "edupub"), "HTM-051");
      assertNotNull("expected a microdata warning", message);
      assertEquals(expected, message.get("locations").get(0).get("path").asText());
    }
  }

  @Test
  public void dataURLsInPictureAreCheckedIndependentlyOfScheduling() throws Exception
  {
    File epub = buildEPUB("data-urls", false, false, true);

    List<JsonNode> sequential = messages(epub, 1);
    List<JsonNode> concurrent = messages(epub, 4);

    assertEquals("every picture must be checked", CHAPTERS,
        locations(sequential, "MED-003").size());
    assertEquals("every picture must be checked", CHAPTERS,
        locations(concurrent, "MED-003").size());
    assertEquals(normalize(sequential), normalize(concurrent));
  }

  private File buildEPUB(String name, boolean withMalformedChapter, boolean withMicrodata,
      boolean withDataURLPicture)
    throws Exception
  {
    return buildEPUB(name,
        withMalformedChapter ? Collections.singleton(MALFORMED_CHAPTER)
            : Collections.<Integer>emptySet(),
        withMicrodata, withDataURLPicture);
  }

  private File buildEPUB(String name, Set<Integer> malformedChapters, boolean withMicrodata,
      boolean withDataURLPicture)
    throws Exception
  {
    File dir = tmp.newFolder(name);
    File epub = new File(dir, name + ".epub");
    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(epub)))
    {
      // mimetype must be the first entry, uncompressed
      byte[] mimetype = "application/epub+zip".getBytes(StandardCharsets.UTF_8);
      ZipEntry entry = new ZipEntry("mimetype");
      entry.setMethod(ZipEntry.STORED);
      entry.setSize(mimetype.length);
      entry.setCompressedSize(mimetype.length);
      CRC32 crc = new CRC32();
      crc.update(mimetype);
      entry.setCrc(crc.getValue());
      zip.putNextEntry(entry);
      zip.write(mimetype);
      zip.closeEntry();

      write(zip, "META-INF/container.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
          + "  <rootfiles><rootfile full-path=\"OEBPS/content.opf\" "
          + "media-type=\"application/oebps-package+xml\"/></rootfiles>\n" + "</container>");

      write(zip, "OEBPS/content.opf", packageDocument());
      write(zip, "OEBPS/nav.xhtml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
          + "xmlns:epub=\"http://www.idpf.org/2007/ops\"><head><title>nav</title></head>"
          + "<body><nav epub:type=\"toc\"><ol><li><a href=\"ch1.xhtml\">1</a></li></ol>"
          + "</nav></body></html>");

      for (int i = 1; i <= CHAPTERS; i++)
      {
        String body;
        if (malformedChapters.contains(i))
        {
          body = "<p>unclosed";
        }
        else if (withMicrodata)
        {
          body = "<p itemscope=\"itemscope\" itemtype=\"http://schema.org/Chapter\">"
              + "<span itemprop=\"name\">" + i + "</span></p>";
          if (i == 1)
          {
            // make the first chapter the slowest to check
            StringBuilder filler = new StringBuilder();
            for (int j = 0; j < 500; j++)
            {
              filler.append("<p>filler</p>");
            }
            body = body + filler;
          }
        }
        else if (withDataURLPicture)
        {
          // typeless, foreign, and unparseable media types
          String image;
          switch (i % 3)
          {
          case 0:
            image = "data:,hello";
            break;
          case 1:
            image = "data:image/vnd.xyz;base64,AAAA";
            break;
          default:
            image = "data:!!!,hello";
            break;
          }
          body = "<picture><img src=\"" + image + "\" alt=\"x\"/></picture>";
        }
        else
        {
          body = "<p>chapter " + i + "</p>";
        }
        write(zip, "OEBPS/ch" + i + ".xhtml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>ch" + i
                + "</title></head><body>" + body + "</body></html>");
      }
    }
    return epub;
  }

  private String packageDocument()
  {
    StringBuilder manifest = new StringBuilder();
    StringBuilder spine = new StringBuilder();
    for (int i = 1; i <= CHAPTERS; i++)
    {
      manifest.append("    <item id=\"c").append(i).append("\" href=\"ch").append(i)
          .append(".xhtml\" media-type=\"application/xhtml+xml\"/>\n");
      spine.append("    <itemref idref=\"c").append(i).append("\"/>\n");
    }
    manifest.append(
        "    <item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" "
            + "properties=\"nav\"/>\n");
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\" "
        + "unique-identifier=\"uid\">\n"
        + "  <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n"
        + "    <dc:identifier id=\"uid\">urn:uuid:12345678-1234-1234-1234-123456789012</dc:identifier>\n"
        + "    <dc:title>Concurrent checking</dc:title>\n" + "    <dc:language>en</dc:language>\n"
        + "    <meta property=\"dcterms:modified\">2020-01-01T00:00:00Z</meta>\n"
        + "  </metadata>\n" + "  <manifest>\n" + manifest + "  </manifest>\n" + "  <spine>\n"
        + spine + "  </spine>\n" + "</package>";
  }

  private static void write(ZipOutputStream zip, String name, String content) throws Exception
  {
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content.getBytes(StandardCharsets.UTF_8));
    zip.closeEntry();
  }

  private List<JsonNode> messages(File epub, int jobs, String... extraArgs) throws Exception
  {
    File report = new File(epub.getParentFile(), epub.getName() + ".jobs" + jobs + "."
        + System.nanoTime() + ".json");
    List<String> args = new ArrayList<>(Arrays.asList("-j", report.getPath(), "-q"));
    args.addAll(Arrays.asList(extraArgs));
    args.add(epub.getPath());

    PrintStream out = System.out;
    PrintStream err = System.err;
    System.setProperty("epubcheck.jobs", Integer.toString(jobs));
    try
    {
      // silence the checker's progress output
      System.setOut(new PrintStream(new ByteArrayOutputStream()));
      System.setErr(new PrintStream(new ByteArrayOutputStream()));
      new EpubChecker().run(args.toArray(new String[0]));
    } finally
    {
      System.setOut(out);
      System.setErr(err);
      System.clearProperty("epubcheck.jobs");
    }

    JsonNode messages = new ObjectMapper().readTree(report).get("messages");
    assertNotNull(messages);
    List<JsonNode> result = new ArrayList<>();
    messages.forEach(result::add);
    return result;
  }

  private static String normalize(List<JsonNode> messages)
  {
    return UUID.matcher(messages.toString()).replaceAll("UUID");
  }

  private static int count(List<JsonNode> messages, String id)
  {
    int count = 0;
    for (JsonNode message : messages)
    {
      if (id.equals(message.get("ID").asText())) count++;
    }
    return count;
  }

  private static List<JsonNode> locations(List<JsonNode> messages, String id)
  {
    List<JsonNode> result = new ArrayList<>();
    for (JsonNode message : messages)
    {
      if (id.equals(message.get("ID").asText()))
      {
        message.get("locations").forEach(result::add);
      }
    }
    return result;
  }

  private static JsonNode find(List<JsonNode> messages, String id)
  {
    for (JsonNode message : messages)
    {
      if (id.equals(message.get("ID").asText())) return message;
    }
    return null;
  }
}

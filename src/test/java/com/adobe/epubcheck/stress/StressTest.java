package com.adobe.epubcheck.stress;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.ValidationReport;
import com.google.common.io.Files;

@Deprecated
public class StressTest
{

  /*
    * Keeping the test method commented out to not accidentally become part of
    * "run all..." efforts. To run, - set the TEMP_DIR_PATH and GROW
    * parameters, make sure the temp directory exists and is empty - uncomment
    * and run.
    */
  String TEMP_DIR_PATH = "/Users/mgylling/ec_temp/";
  int GROW = 2000;
  String STRING_FMT = "%05d";

  XPathFactory factory = XPathFactory.newInstance();
  XPath xpath = factory.newXPath();

  String OPF_NS = "http://www.idpf.org/2007/opf";
  String XHT_NS = "http://www.w3.org/1999/xhtml";
  File TEMP_DIR;
  File SOURCE_DIR = new File(StressTest.class.getResource("/stress/")
      .getFile());
  File opfFile;
  Document opfDoc;
  Element manifest;
  Element spine;
  File navFile;
  Document navDoc;
  Element navOlElem;
  File TEMP_CONTENT_DIR;
  int counter = 0;

  /*
  @Ignore
  @Test
  public void testHighFileCount() throws Exception
  {
    setup();
    try
    {
      outWriter.println("Building high file count stress test epub...");
      build();
      outWriter.println("Created epub has " +
          manifest.getChildNodes().getLength() + " manifest items.");
      outWriter.println("Validating...");
      validate();
      outWriter.println("done.");
    }
    finally
    {
      cleanDir(TEMP_DIR);
      File[] files = TEMP_DIR.listFiles();
      if (files != null && files.length > 0)
      {
        System.err.println("temp dir clean failed");
      }
    }
  }
  */
  private void validate() throws Exception
  {
    Archive epub = new Archive(TEMP_DIR.getPath());
    epub.createArchive();
    ValidationReport report = new ValidationReport(TEMP_DIR.getName());
    EpubCheck epubCheck = new EpubCheck(epub.getEpubFile(), report);
    epubCheck.check();
    assertTrue(report.getErrorCount() < 1);
    assertTrue(report.getWarningCount() < 1);
    epub.deleteEpubFile();
    // outWriter.println(report);
  }

  private void build() throws Exception
  {
    cleanDir(TEMP_DIR);
    TEMP_DIR.delete();

    doCopyDirectory(SOURCE_DIR, TEMP_DIR);
    TEMP_CONTENT_DIR = new File(TEMP_DIR, "EPUB");
    // new File(TEMP_CONTENT_DIR, "META-INF").delete(); // bug in phloc copy

    opfFile = new File(TEMP_CONTENT_DIR, "package.opf");
    opfDoc = build(opfFile);
    manifest = (Element) opfDoc.getDocumentElement()
        .getElementsByTagName("manifest").item(0);
    spine = (Element) opfDoc.getDocumentElement()
        .getElementsByTagName("spine").item(0);

    navFile = new File(TEMP_CONTENT_DIR, "nav.xhtml");
    navDoc = build(navFile);
    navOlElem = (Element) xpath.evaluate("//xht:nav[1]/xht:ol",
        navDoc.getDocumentElement(), XPathConstants.NODE);

    for (int i = 0; i < GROW; i++)
    {
      counter++;

      String newImageName = "image_" + String.format(STRING_FMT, counter)
          + ".png";
      Files.copy(new File(TEMP_CONTENT_DIR,
          "image_00000.png"),
          new File(TEMP_CONTENT_DIR, newImageName));
      appendManifestItem(newImageName, "image/png", "img_");

      String newCssName = "style_" + String.format(STRING_FMT, counter)
          + ".css";
      Files.copy(new File(TEMP_CONTENT_DIR,
          "style_00000.css"), new File(TEMP_CONTENT_DIR, newCssName));
      appendManifestItem(newCssName, "text/css", "css_");

      String newXhtName = "content_" + String.format(STRING_FMT, counter)
          + ".xhtml";
      Document xhtDoc = build(new File(TEMP_CONTENT_DIR,
          "content_00000.xhtml"));
      xhtDoc = tweakXht(xhtDoc);
      appendManifestItem(newXhtName, "application/xhtml+xml", "t_");
      appendSpineItem("t_" + String.format(STRING_FMT, counter));
      File xhtOut = new File(TEMP_CONTENT_DIR, newXhtName);
      save(xhtDoc, xhtOut);

      // add new content doc to nav doc
      Element li = navDoc.createElementNS(XHT_NS, "li");
      Element a = navDoc.createElementNS(XHT_NS, "a");
      Attr href = navDoc.createAttribute("href");
      href.appendChild(navDoc.createTextNode(newXhtName));
      a.getAttributes().setNamedItem(href);
      a.appendChild(navDoc.createTextNode("Lorem Ipsum "
          + String.format(STRING_FMT, counter)));
      li.appendChild(a);
      navOlElem.appendChild(li);

    }

    save(navDoc, navFile);
    save(opfDoc, opfFile);
  }

  private void doCopyDirectory(File srcDir, File destDir) throws Exception
  {
    File[] srcFiles = srcDir.listFiles();
    if (srcFiles == null)
    {
      throw new IOException("Failed to list contents of " + srcDir);
    }
    if (destDir.exists())
    {
      if (!destDir.isDirectory())
      {
        throw new IOException("Destination '" + destDir + "' exists but is not a directory");
      }
    }
    else
    {
      if (!destDir.mkdirs() && !destDir.isDirectory())
      {
        throw new IOException("Destination '" + destDir + "' directory cannot be created");
      }
    }
    if (!destDir.canWrite())
    {
      throw new IOException("Destination '" + destDir + "' cannot be written to");
    }

    for (File srcFile : srcFiles)
    {
      File dstFile = new File(destDir, srcFile.getName());
      if (srcFile.isDirectory())
      {
        doCopyDirectory(srcFile, dstFile);
      }
      else
      {
        Files.copy(srcFile, dstFile);
      }

    }

  }

  private Document tweakXht(Document xhtDoc) throws Exception
  {
    // <link type="text/css" rel="stylesheet" href="style_00000.css" />
    Element link = (Element) xpath.evaluate("//xht:link[1]",
        xhtDoc.getDocumentElement(), XPathConstants.NODE);
    Attr href = link.getAttributeNode("href");
    href.setNodeValue("style_" + String.format(STRING_FMT, counter)
        + ".css");

    // <h1>Lorem Ipsum 00000</h1>
    Element h1 = (Element) xpath.evaluate("//xht:h1[1]",
        xhtDoc.getDocumentElement(), XPathConstants.NODE);
    h1.removeChild(h1.getFirstChild());
    h1.appendChild(xhtDoc.createTextNode("Lorem Ipsum "
        + String.format(STRING_FMT, counter)));

    // <img src="image_00000.png"/>
    Element img = (Element) xpath.evaluate("//xht:img[1]",
        xhtDoc.getDocumentElement(), XPathConstants.NODE);
    Attr src = img.getAttributeNode("src");
    src.setNodeValue("image_" + String.format(STRING_FMT, counter) + ".png");

    return xhtDoc;

  }

  private void appendSpineItem(String id)
  {
    Element itemref = opfDoc.createElementNS(OPF_NS, "itemref");
    Attr idref = opfDoc.createAttribute("idref");
    idref.appendChild(opfDoc.createTextNode(id));
    itemref.getAttributes().setNamedItem(idref);

    spine.appendChild(itemref);
  }

  private void appendManifestItem(String ref, String mimeType, String idPfx)
  {
    Element item = opfDoc.createElementNS(OPF_NS, "item");

    Attr id = opfDoc.createAttribute("id");
    id.appendChild(opfDoc.createTextNode(idPfx
        + String.format(STRING_FMT, counter)));
    item.getAttributes().setNamedItem(id);

    Attr mt = opfDoc.createAttribute("media-type");
    mt.appendChild(opfDoc.createTextNode(mimeType));
    item.getAttributes().setNamedItem(mt);

    Attr href = opfDoc.createAttribute("href");
    href.appendChild(opfDoc.createTextNode(ref));
    item.getAttributes().setNamedItem(href);

    manifest.appendChild(item);

  }

  private void save(Document doc, File out) throws Exception
  {
    // some error with saxon on the classpath
    System.setProperty("javax.xml.transform.TransformerFactory",
        "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    TransformerFactory transformerFactory = TransformerFactory
        .newInstance();
    Transformer transformer = transformerFactory.newTransformer();

    // TransformerFactory transformerFactory =
    // com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl.newInstance();
    // Transformer transformer = new
    // com.sun.org.apache.xalan.internal.xsltc.trax.Transformer();
    DOMSource source = new DOMSource(doc);
    StreamResult streamResult = new StreamResult(out);
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, streamResult);
  }

  private Document build(File f) throws Exception
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setValidating(false);
    return dbf.newDocumentBuilder().parse(f);
  }

  private void setup()
  {

    TEMP_DIR = new File(TEMP_DIR_PATH);
    if (!TEMP_DIR.exists())
    {
      throw new IllegalArgumentException("Path " + TEMP_DIR_PATH
          + " does not exist");
    }
    if (!TEMP_DIR.isDirectory())
    {
      throw new IllegalArgumentException("Path " + TEMP_DIR_PATH
          + " is not a directory");
    }
    if (TEMP_DIR.listFiles().length > 0)
    {
      throw new IllegalArgumentException("Directory " + TEMP_DIR_PATH
          + " is not empty");
    }
    if (!SOURCE_DIR.exists() || !SOURCE_DIR.isDirectory())
    {
      throw new IllegalArgumentException("Source directory not found");
    }

    xpath.setNamespaceContext(new NamespaceContext()
    {

      @Override
      public String getNamespaceURI(String prefix)
      {
        if (prefix.equals("xht"))
        {
          return XHT_NS;
        }
        return null;
      }

      @Override
      public String getPrefix(String namespaceURI)
      {
        if (namespaceURI.equals(XHT_NS))
        {
          return "xht";
        }
        return null;
      }

      @Override
      public Iterator getPrefixes(String namespaceURI)
      {
        throw new UnsupportedOperationException();
      }
    });

  }

  private void cleanDir(File path)
  {
    File[] files = path.listFiles();
    if (files != null)
    {
      for (File f : files)
      {
        if (f.isDirectory())
        {
          cleanDir(f);
          f.delete();
        }
        else
        {
          f.delete();
        }
      }
      if (!path.equals(TEMP_DIR))
      {
        path.delete();
      }
    }
  }
}

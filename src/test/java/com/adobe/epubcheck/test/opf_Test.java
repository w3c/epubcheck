package com.adobe.epubcheck.test;

import org.junit.*;

import com.adobe.epubcheck.test.common.TestOutputType;

public class opf_Test
{
  private SecurityManager originalManager;

  @Before
  public void setUp() throws Exception
  {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() throws Exception
  {
    System.setSecurityManager(this.originalManager);
  }

  @Test
  public void Missing_NAV_epub3Test() throws Exception
  {
    runOpfJsonTest("Missing_NAV_epub3", 1);
  }

  @Test
  public void Missing_NAV_epub2Test() throws Exception
  {
    runOpfJsonTest("Missing_NAV_epub2", 0);
  }

  @Test
  public void Missing_NCX_epub3Test() throws Exception
  {
    runOpfJsonTest("Missing_NCX_epub3", 0);
  }

  @Test
  public void Missing_NCX_epub2Test() throws Exception
  {
    runOpfJsonTest("Missing_NCX_epub2", 1);
  }

  @Test
  public void Missing_Spine_epub3_json_Test() throws Exception
  {
    runOpfJsonTest("Missing_Spine_epub3", 1);
  }

  @Test
  public void Missing_Spine_epub3_xml_Test() throws Exception
  {
    runOpfXmlTest("Missing_Spine_epub3", 1);
  }

  @Test
  public void Missing_Spine_epub2Test() throws Exception
  {
    runOpfJsonTest("Missing_Spine_epub2", 1);
  }

  @Test
  public void Epub3_marked_v2_json_Test() throws Exception
  {
    runOpfJsonTest("Epub3_marked_v2", 1);
  }

  @Test
  public void Epub3_marked_v2_xml_Test() throws Exception
  {
    runOpfXmlTest("Epub3_marked_v2", 1);
  }

  @Test
  public void Epub2_marked_v3_json_Test() throws Exception
  {
    runOpfJsonTest("Epub2_marked_v3", 1);
  }

  @Test
  public void Epub2_marked_v3_xmlTest() throws Exception
  {
    runOpfXmlTest("Epub2_marked_v3", 1);
  }

  @Test
  public void rendition_valid_Test() throws Exception
  {
    runOpfJsonTest("rendition_valid", 1);
  }

  @Test
  public void viewport_Test() throws Exception
  {
    // viewport test is an epub where pre-paginated is set for the entire document
    runOpfJsonTest("viewport", 1);
  }

  @Test
  public void viewport2_Test() throws Exception
  {
    // viewport test is an epub where reflowable is set for the entire document
    // and some spine items are set to fixed individually
    runOpfJsonTest("viewport2", 1);
  }

  @Test
  public void Missing_metadata_epub3Test() throws Exception
  {
    runOpfJsonTest("Missing_metadata_epub3", 1);
  }

  @Test
  public void Missing_metadata_epub2Test() throws Exception
  {
    runOpfJsonTest("Missing_metadata_epub2", 1);
  }


  @Test
  public void Excessive_Spine_Items_epub3_Test() throws Exception
  {
    runOpfJsonTest("Excessive_Spine_Items_epub3", 0);
  }

  @Test
  public void Excessive_Spine_Items_epub2_Test() throws Exception
  {
    runOpfJsonTest("Excessive_Spine_Items_epub2", 0);
  }

  @Test
  public void Excessive_Spine_Items_epub3_fixed_format_Test() throws Exception
  {
    runOpfJsonTest("Excessive_Spine_Items_epub3_fixed_format", 1);
  }

  @Test
  public void Excessive_Spine_Items_epub3_fixed_format_properties_Test() throws Exception
  {
    runOpfJsonTest("Excessive_Spine_Items_epub3_fixed_format_properties", 1);
  }

  @Test
  public void Publication_Metadata_epub3_json_Test()
  {
    runOpfJsonTest("Publication_Metadata_epub3", 0);
  }

  @Test
  public void Publication_Metadata_epub3_xml_Test()
  {
    runOpfXmlTest("Publication_Metadata_epub3", 0);
  }

  @Test
  public void Media_type_handler_Test()
  {
    runOpfJsonTest("Media-type_handler", 1);
  }

  @Test
  public void Mismatched_mimetypes_epub3_Test()
  {
    runOpfJsonTest("Mismatched_mimetypes_epub3", 1);
  }

  @Test
  public void Mismatched_mimetypes_epub2_Test()
  {
    runOpfJsonTest("Mismatched_mimetypes_epub2", 1);
  }

  @Test
  public void Properties_Test()
  {
    runOpfJsonTest("Properties", 1);
  }

  @Test
  public void oebps12_Test()
  {
    runOpfJsonTest("oebps12", 1);
  }

  @Test
  public void Fallbacks_epub3_Test()
  {
    runOpfJsonTest("Fallbacks_epub3", 1);
  }

  @Test
  public void Fallbacks_epub2_Test()
  {
    runOpfJsonTest("Fallbacks_epub2", 1);
  }

  @Test
  public void Missing_unique_id_Test()
  {
    runOpfJsonTest("Missing_unique_id", 1);
  }

  private void runOpfJsonTest(String testName, int expectedReturnCode)
  {
    common.runExpTest("opf", testName, expectedReturnCode, TestOutputType.JSON);
  }

  private void runOpfXmlTest(String testName, int expectedReturnCode)
  {
    common.runExpTest("opf", testName, expectedReturnCode, TestOutputType.XML);
  }
}

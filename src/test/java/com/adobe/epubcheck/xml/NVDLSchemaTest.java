package com.adobe.epubcheck.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.thaiopensource.validate.Schema;

public class NVDLSchemaTest
{

  private static final XMLValidators[] NVDL_VALIDATORS = { XMLValidators.SVG_20_NVDL,
      XMLValidators.SVG_30_NVDL, XMLValidators.SVG_30_INFORMATIVE_NVDL,
      XMLValidators.XHTML_20_NVDL, XMLValidators.XHTML_30_NVDL };

  @Test
  public void modesAndModeUsagesAreInitializedWhenTheSchemaIsCompiled() throws Exception
  {
    for (XMLValidators validators : NVDL_VALIDATORS)
    {
      Schema schema = validators.get().getSchema();
      assertEquals("com.thaiopensource.validate.nvdl.SchemaImpl", schema.getClass().getName());

      XMLValidator.NvdlState state = XMLValidator.collectNvdlState(schema);
      assertFalse("no NVDL mode found in " + validators.name(), state.modes.isEmpty());
      assertFalse("no NVDL mode usage found in " + validators.name(), state.usages.isEmpty());

      for (Object mode : state.modes)
      {
        assertInitialized(mode, "mode", validators);
      }
      for (Object usage : state.usages)
      {
        assertInitialized(usage, "mode usage", validators);
      }
    }
  }

  private static void assertInitialized(Object node, String kind, XMLValidators validators)
    throws Exception
  {
    Field attributeProcessing = node.getClass().getDeclaredField("attributeProcessing");
    attributeProcessing.setAccessible(true);
    assertTrue("an NVDL " + kind + " of " + validators.name() + " is still lazily initialized",
        attributeProcessing.getInt(node) != -1);
  }
}

package com.adobe.epubcheck.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class XMLValidatorsTest
{

  private static Object compiledValue(XMLValidators validator) throws Exception
  {
    Field val = XMLValidators.class.getDeclaredField("val");
    val.setAccessible(true);
    return val.get(validator);
  }

  // must run before any other test compiles schemas
  @Test
  public void a_schemasAreNotCompiledEagerly() throws Exception
  {
    assertNull("schemas must not be compiled before their first use",
        compiledValue(XMLValidators.ENC_20_RNG));
  }

  @Test
  public void b_allSchemasCompileAndAreCached()
  {
    for (XMLValidators validator : XMLValidators.values())
    {
      XMLValidator compiled = validator.get();
      assertNotNull("no validator compiled for " + validator, compiled);
      assertNotNull("no schema available for " + validator, compiled.getSchema());
      assertSame("validator not cached for " + validator, compiled, validator.get());
    }
  }
}

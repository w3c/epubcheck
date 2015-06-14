package com.adobe.epubcheck.opf;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.opf.MetadataSet.Metadata;
import com.adobe.epubcheck.opf.MetadataSet.Builder;
import com.adobe.epubcheck.vocab.Property;

public class MetadataSetTest
{

  private Builder builder;
  private MetadataSet set;
  private static Property PROP_FOO = Property.newFrom("foo", "base#", "px");
  private static Property PROP_FOZ = Property.newFrom("foz", "base#", "px");
  private static Property PROP_BAR = Property.newFrom("bar", "base#", "px");
  private static Property PROP_BAZ = Property.newFrom("baz", "base#", "px");

  @Before
  public void setup()
  {
    builder = new Builder();
  }

  @Test
  public void testBuilderEmpty()
  {
    set = builder.build();
    assertTrue(set.getAll().isEmpty());
  }

  @Test
  public void testBuilderBasicProp()
  {
    builder.meta(null, PROP_FOO, "value", null);
    set = builder.build();
    assertEquals(1, set.getAll().size());
    assertEquals(1, set.getPrimary(PROP_FOO).size());
    assertEquals(1, set.getAny(PROP_FOO).size());
    assertEquals("value", set.getAll().iterator().next().getValue());
  }

  @Test
  public void testBuilderRefineResource()
  {
    builder.meta("id", PROP_FOO, "value", "bar");
    set = builder.build();
    assertNotNull(set.getRefining("bar"));
    assertEquals(1, set.getRefining("bar").size());
  }

  @Test
  public void testBuilderRefineWithHash()
  {
    builder.meta("id", PROP_FOO, "value", "#bar");
    set = builder.build();
    assertNotNull(set.getRefining("bar"));
    assertEquals(1, set.getRefining("bar").size());
  }

  @Test
  public void testBuilderRefinesEmpty()
  {
    builder.meta("id", PROP_FOO, "value", "#");
    set = builder.build();
    assertNotNull(set.getRefining("bar"));
    assertTrue(set.getRefining("bar").isEmpty());
  }

  @Test
  public void testBuilderRefineSingleProp()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", null);
    set = builder.build();
    assertEquals(2, set.getAll().size());
    assertEquals(1, set.getPrimary().size());
    assertEquals(1, set.getPrimary(PROP_BAR).size());
    assertEquals(1, set.getAny(PROP_FOO).size());
    Metadata bar = set.getAny(PROP_BAR).iterator().next();
    Metadata foo = set.getAny(PROP_FOO).iterator().next();
    assertEquals(1, bar.getRefiners().size());
    assertEquals(foo, bar.getRefiners().iterator().next());
    assertTrue(foo.getRefiners().isEmpty());
    assertEquals(bar, set.getRefinedBy(foo).orNull());
    assertEquals(foo, set.getRefining("idbar").iterator().next());
  }
  
  @Test
  public void testBuilderRefineSinglePropWithNoId()
  {
    builder.meta(null, PROP_FOO, "valuefoo", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", null);
    set = builder.build();
    assertEquals(2, set.getAll().size());
    assertEquals(1, set.getPrimary().size());
    assertEquals(1, set.getPrimary(PROP_BAR).size());
    assertEquals(1, set.getAny(PROP_FOO).size());
    Metadata bar = set.getAny(PROP_BAR).iterator().next();
    Metadata foo = set.getAny(PROP_FOO).iterator().next();
    assertEquals(1, bar.getRefiners().size());
    assertEquals(foo, bar.getRefiners().iterator().next());
    assertTrue(foo.getRefiners().isEmpty());
    assertEquals(bar, set.getRefinedBy(foo).orNull());
    assertEquals(foo, set.getRefining("idbar").iterator().next());
  }

  @Test
  public void testBuilderMultipleRefines()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idbar");
    builder.meta("idfoz", PROP_FOZ, "valuefoz", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", null);
    set = builder.build();
    assertEquals(3, set.getAll().size());
    assertEquals(1, set.getPrimary().size());
    Metadata bar = set.getAny(PROP_BAR).iterator().next();
    Metadata foo = set.getAny(PROP_FOO).iterator().next();
    Metadata foz = set.getAny(PROP_FOZ).iterator().next();
    assertEquals(2, bar.getRefiners().size());
    assertTrue(bar.getRefiners().contains(foo));
    assertTrue(bar.getRefiners().contains(foz));
  }

  @Test
  public void testBuilderChainedRefines()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idfoz");
    builder.meta("idfoz", PROP_FOZ, "valuefoz", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", null);
    set = builder.build();
    assertEquals(3, set.getAll().size());
    assertEquals(1, set.getPrimary().size());
    Metadata bar = set.getAny(PROP_BAR).iterator().next();
    Metadata foo = set.getAny(PROP_FOO).iterator().next();
    Metadata foz = set.getAny(PROP_FOZ).iterator().next();
    assertEquals(1, bar.getRefiners().size());
    assertTrue(bar.getRefiners().contains(foz));
    assertEquals(1, foz.getRefiners().size());
    assertTrue(foz.getRefiners().contains(foo));
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderRefinesCycle()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", "idfoo");
    set = builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderRefinesCycleSelf()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idfoo");
    set = builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderRefinesCycleIndirect()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", "idbaz");
    builder.meta("idbaz", PROP_BAZ, "valuebaz", "idfoo");
    set = builder.build();
  }
  

  @Test(expected = IllegalStateException.class)
  public void testBuilderRefines()
  {
    builder.meta("idfoo", PROP_FOO, "valuefoo", "#idbar");
    builder.meta("idbar", PROP_BAR, "valuebar", "#idbaz");
    builder.meta("idbaz", PROP_BAZ, "valuebaz", "#idfoo");
    set = builder.build();
  }
}

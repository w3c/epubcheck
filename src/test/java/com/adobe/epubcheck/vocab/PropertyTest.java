package com.adobe.epubcheck.vocab;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class PropertyTest
{

  private enum LETTERS
  {
    A,
    B,
    C
  }

  private enum NUMBERS
  {
    ONE,
    TWO,
    THREE
  }

  private static String FOO_URI = "http://example.org/foo#";
  private static String LETTERS_URI = "http://example.org/letters#";
  private static String NUMBERS_URI = "http://example.org/numbers#";

  @Test
  public void testFilter()
  {
    Set<LETTERS> letters = Property.filter(
        new ImmutableSet.Builder<Property>().add(Property.newFrom("foo", FOO_URI, "foo"))
            .add(Property.newFrom("foo", FOO_URI, "foo"))
            .add(Property.newFrom("a", LETTERS_URI, "", LETTERS.A))
            .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.ONE))
            .add(Property.newFrom("bar", FOO_URI, "foo"))
            .add(Property.newFrom("c", LETTERS_URI, "", LETTERS.C))
            .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.TWO))
            .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.THREE))
            .add(Property.newFrom("baz", FOO_URI, "foo")).build(), LETTERS.class);
    assertThat(letters.size(), is(2));
    assertThat(letters, hasItems(LETTERS.A, LETTERS.C));
  }
  @Test
  public void testFilterNoMatch()
  {
    Set<LETTERS> letters = Property.filter(
        new ImmutableSet.Builder<Property>().add(Property.newFrom("foo", FOO_URI, "foo"))
        .add(Property.newFrom("foo", FOO_URI, "foo"))
        .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.ONE))
        .add(Property.newFrom("bar", FOO_URI, "foo"))
        .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.TWO))
        .add(Property.newFrom("one", NUMBERS_URI, "", NUMBERS.THREE))
        .add(Property.newFrom("baz", FOO_URI, "foo")).build(), LETTERS.class);
    assertEquals(letters, ImmutableSet.of());
  }

  @Test
  public void testFilterNull()
  {
    assertEquals(Property.filter(null, NUMBERS.class), ImmutableSet.of());
  }
}

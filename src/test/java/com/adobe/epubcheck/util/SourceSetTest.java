package com.adobe.epubcheck.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.util.SourceSet.ErrorHandler;
import com.adobe.epubcheck.util.SourceSet.ParseError;

public class SourceSetTest
{

  private static class TestErrorHandler implements ErrorHandler
  {
    private List<ParseError> errors = new LinkedList<>();

    @Override
    public void error(ParseError error, int position)
    {
      errors.add(error);
    }
  }

  private TestErrorHandler errorHandler;

  @Before
  public void before()
  {
    errorHandler = new TestErrorHandler();
  }

  @Test
  public void testURLSingleChar()
  {
    SourceSet sources = SourceSet.parse("a", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a"), sources.getImageURLs());
  }

  @Test
  public void testURLMoreChars()
  {
    SourceSet sources = SourceSet.parse("abc", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("abc"), sources.getImageURLs());
  }

  @Test
  public void testURLWithInnerCommas()
  {
    SourceSet sources = SourceSet.parse("a,,b", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a,,b"), sources.getImageURLs());

  }

  @Test
  public void testURLTrailingComma()
  {
    // NOTE: this doesn't currently raise a parse error in the srcset
    // parsing algorithm (2019-03-14)
    SourceSet sources = SourceSet.parse("a,", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a"), sources.getImageURLs());
  }

  @Test
  public void testURLTrailingCommaFollowedByWhitespace()
  {
    // NOTE: this doesn't currently raise a parse error in the srcset
    // parsing algorithm (2019-03-14)
    SourceSet sources = SourceSet.parse("a,  ", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a"), sources.getImageURLs());
  }

  @Test
  public void testURLList()
  {
    SourceSet sources = SourceSet.parse("a, b, c", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a", "b", "c"), sources.getImageURLs());
  }

  @Test
  public void testURLListWithWidthDescriptors()
  {
    SourceSet sources = SourceSet.parse("a, b 100w, c 200w", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a", "b", "c"), sources.getImageURLs());
  }

  @Test
  public void testURLListWithDensityDescriptors()
  {
    SourceSet sources = SourceSet.parse("a, b 1.5x, c 2x", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a", "b", "c"), sources.getImageURLs());
  }

  @Test
  public void testURLListWithDensityInVariousSyntax()
  {
    SourceSet.parse("u 1x, u 1.5x, u .2x, u 1e12x, u .1e-12x, u 1.1e+12x, u 1E1x", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
  }

  @Test
  public void testURLListWithHeightDescriptors()
  {
    SourceSet sources = SourceSet.parse("a, b 100w 100h, c 200w", errorHandler);
    assertTrue(errorHandler.errors.isEmpty());
    assertEquals(Arrays.asList("a", "b", "c"), sources.getImageURLs());
  }

  @Test
  public void testErrorNull()
  {
    SourceSet sources = SourceSet.parse(null, errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.NULL_OR_EMPTY));
    assertTrue(sources.isEmpty());
  }

  @Test
  public void testErrorEmpty()
  {
    SourceSet sources = SourceSet.parse("", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.NULL_OR_EMPTY));
    assertTrue(sources.isEmpty());
  }

  @Test
  public void testErrorEmptyStartNoSource()
  {
    SourceSet sources = SourceSet.parse(",", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_START));
    assertTrue(sources.isEmpty());
  }

  @Test
  public void testErrorEmptyStartFollowedBySource()
  {
    SourceSet sources = SourceSet.parse(", image.jpeg", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_START));
    assertTrue(sources.getImageURLs().contains("image.jpeg"));
  }

  @Test
  public void testErrorEmptyStartMultipleCommasFollowedBySource()
  {
    SourceSet sources = SourceSet.parse(",,, a", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_START));
    assertTrue(sources.getImageURLs().contains("a"));
  }

  @Test
  public void testErrorEmptyMiddle()
  {
    SourceSet sources = SourceSet.parse("a,, b", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_MIDDLE));
    assertEquals(Arrays.asList("a", "b"), sources.getImageURLs());

  }

  @Test
  public void testErrorEmptyMiddleMoreThanTwoCommas()
  {
    SourceSet sources = SourceSet.parse("a,,,, b", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_MIDDLE));
    assertEquals(Arrays.asList("a", "b"), sources.getImageURLs());

  }

  @Test
  public void testErrorEmptyMiddleWithWhitespace()
  {
    SourceSet sources = SourceSet.parse("a, , b", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_MIDDLE));
    assertEquals(Arrays.asList("a", "b"), sources.getImageURLs());
  }

  @Test
  public void testErrorEmptyMiddleWithWhitespaceAndNoFollowingSpace()
  {
    SourceSet sources = SourceSet.parse("a, ,b", errorHandler);
    assertTrue(errorHandler.errors.contains(ParseError.EMPTY_MIDDLE));
    assertEquals(Arrays.asList("a", "b"), sources.getImageURLs());
  }

  @Test
  public void testErrorDescriptorWidthSigned()
  {
    SourceSet sources = SourceSet.parse("a +100w, b -100w", errorHandler);
    assertEquals(Collections.nCopies(2, ParseError.DESCRIPTOR_WIDTH_SIGNED), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorWidthWithDensity()
  {
    SourceSet sources = SourceSet.parse("a 100w 1x", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_MIX_WIDTH_DENSITY), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorWidthNotInteger()
  {
    SourceSet sources = SourceSet.parse("a NaNw", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_WIDTH_NOT_INT), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorWidthZero()
  {
    SourceSet sources = SourceSet.parse("a 0w", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_WIDTH_ZERO), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorWidthTwice()
  {
    SourceSet sources = SourceSet.parse("a 100w 200w", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_WIDTH_MORE_THAN_ONCE), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorWidthAndOther()
  {
    SourceSet sources = SourceSet.parse("a 100w 2z", errorHandler);

    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_UNKNOWN_SUFFIX), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorDensityWithWidth()
  {
    SourceSet sources = SourceSet.parse("a 1x 100w", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_MIX_WIDTH_DENSITY), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorDensityNotFloat()
  {
    SourceSet sources = SourceSet.parse("a NaNx", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_DENSITY_NOT_FLOAT), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorDensityTwice()
  {
    SourceSet sources = SourceSet.parse("a 1x 2x", errorHandler);
    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_DENSITY_MORE_THAN_ONCE), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorDensityAndOther()
  {
    SourceSet sources = SourceSet.parse("a 1x 2z", errorHandler);

    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_UNKNOWN_SUFFIX), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

  @Test
  public void testErrorDescriptorUnknownSuffix()
  {
    SourceSet sources = SourceSet.parse("a 2z", errorHandler);

    assertEquals(Arrays.asList(ParseError.DESCRIPTOR_UNKNOWN_SUFFIX), errorHandler.errors);
    assertTrue(sources.getImageURLs().isEmpty());
  }

}

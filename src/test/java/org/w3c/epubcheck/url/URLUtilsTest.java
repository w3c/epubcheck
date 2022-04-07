package org.w3c.epubcheck.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.nio.file.FileSystems;

import org.junit.Test;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

public class URLUtilsTest
{
  @Test
  public void testEncodeNullString()
  {
    assertThat(URLUtils.encodePath(null), is(nullValue()));
  }

  @Test
  public void testEncodeEmptyString()
  {
    assertThat(URLUtils.encodePath(""), is(emptyString()));
  }

  @Test
  public void testEncodeBasicString()
  {
    assertThat(URLUtils.encodePath("hello"), equalTo("hello"));
  }

  @Test
  public void testEncode0x0020()
  {
    assertThat(URLUtils.encodePath("a space"), equalTo("a%20space"));
  }

  @Test
  public void testEncode0x0023()
  {
    assertThat(URLUtils.encodePath("a#hash"), equalTo("a%23hash"));
  }

  @Test
  public void testEncode0x0025()
  {
    assertThat(URLUtils.encodePath("a%20space"), equalTo("a%2520space"));
  }

  @Test
  public void testEncode0x002F()
  {
    assertThat(URLUtils.encodePath("a/path"), equalTo("a/path"));
  }

  @Test
  public void testEncode0x003F()
  {
    assertThat(URLUtils.encodePath("a?path"), equalTo("a%3Fpath"));
  }

  public void testToHumanLocation()
    throws GalimatiasParseException
  {
    URL.parse("file:///C:/path");
    URL.parse("file:///C|/path");
    URL.parse("file:///C|a/path");
    URL.parse("file:///path/C|/path");

  }


}

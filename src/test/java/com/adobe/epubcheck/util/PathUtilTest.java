package com.adobe.epubcheck.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PathUtilTest
{

  @Test(expected = NullPointerException.class)
  public void testIsRemoteNull()
  {
    PathUtil.isRemote(null);
  }

  @Test
  public void testIsRemoteTrue()
  {
    assertTrue(PathUtil.isRemote("https://example.org"));
  }

  @Test
  public void testIsRemoteFalse()
  {
    assertFalse(PathUtil.isRemote("OCF/path"));
  }

  @Test(expected = NullPointerException.class)
  public void testNormalizePathNull()
  {
    PathUtil.normalizePath(null);
  }

  @Test
  public void testNormalizePathEmpty()
  {
    assertEquals("", PathUtil.normalizePath(""));
  }

  @Test
  public void testNormalizePathDot()
  {
    assertEquals("", PathUtil.normalizePath("."));
    assertEquals("", PathUtil.normalizePath("./"));
  }

  @Test
  public void testNormalizePathDotBegin()
  {
    assertEquals("foo", PathUtil.normalizePath("./foo"));
  }

  @Test
  public void testNormalizePathDotsBegin()
  {
    assertEquals("foo", PathUtil.normalizePath("././foo"));
  }

  @Test
  public void testNormalizePathDotMiddle()
  {
    assertEquals("foo/bar", PathUtil.normalizePath("foo/./bar"));
  }

  @Test
  public void testNormalizePathDotsMiddle()
  {
    assertEquals("foo/bar", PathUtil.normalizePath("foo/././bar"));
  }

  @Test
  public void testNormalizePathDotEnd()
  {
    assertEquals("foo/bar/", PathUtil.normalizePath("foo/bar/."));
  }

  @Test
  public void testNormalizePathDotsEnd()
  {
    assertEquals("foo/bar/", PathUtil.normalizePath("foo/bar/./."));
  }

  @Test
  public void testNormalizePathParent()
  {
    assertEquals("../", PathUtil.normalizePath(".."));
    assertEquals("../", PathUtil.normalizePath("../"));
  }

  @Test
  public void testNormalizePathParentBegin()
  {
    assertEquals("../foo", PathUtil.normalizePath("../foo"));
    assertEquals("../../foo", PathUtil.normalizePath("../../foo"));
  }

  @Test
  public void testNormalizePathParentEnd()
  {
    assertEquals("", PathUtil.normalizePath("foo/.."));
    assertEquals("", PathUtil.normalizePath("foo/../"));
    assertEquals("", PathUtil.normalizePath("foo/bar/../.."));
    assertEquals("foo/", PathUtil.normalizePath("foo/bar/.."));
    assertEquals("foo/", PathUtil.normalizePath("foo/bar/../"));
  }

  @Test
  public void testNormalizePathParentMiddle()
  {
    assertEquals("bar", PathUtil.normalizePath("foo/../bar"));
    assertEquals("bar/", PathUtil.normalizePath("foo/../bar/"));
    assertEquals("foo", PathUtil.normalizePath("foo/bar/../../foo"));
  }

  @Test
  public void testNormalizePathTrailingSlash()
  {
    assertEquals("foo/", PathUtil.normalizePath("foo/"));
    assertEquals("foo/", PathUtil.normalizePath("foo///"));
  }

  @Test
  public void testNormalizePathLeadingSlash()
  {
    assertEquals("/", PathUtil.normalizePath("/"));
    assertEquals("/foo", PathUtil.normalizePath("/foo"));
    assertEquals("/foo", PathUtil.normalizePath("/./foo"));
    assertEquals("/../foo", PathUtil.normalizePath("/../foo"));
  }

  @Test
  public void testNormalizePathAbsoluteURI()
  {
    assertEquals("http://example.org/foo", PathUtil.normalizePath("http://example.org/foo"));
    assertEquals("http://foo/../bar", PathUtil.normalizePath("http://foo/../bar"));
  }

  @Test
  public void testNormalizePathMiddleSlash()
  {
    assertEquals("foo/bar", PathUtil.normalizePath("foo////bar"));
  }

  @Test
  public void testNormalizePathNothingToNormalize()
  {
    assertEquals("foo/bar", PathUtil.normalizePath("foo/bar"));
  }

  @Test(expected = NullPointerException.class)
  public void testRelativizeNull()
  {
    PathUtil.resolveRelativeReference(null, null);
  }

  @Test
  public void testRelativizeWithNullBase()
  {
    assertEquals("foo", PathUtil.resolveRelativeReference(null, "foo"));
  }

  @Test
  public void testRelativizeWithNullBaseIsNormalized()
  {
    assertEquals("foo", PathUtil.resolveRelativeReference(null, "bar/../foo"));
  }

  @Test
  public void testRelativizeAbsoluteWithNullBaseIsReturnedAsIs()
  {
    assertEquals("http://foo", PathUtil.resolveRelativeReference(null, "http://foo"));
  }

  @Test
  public void testRelativizeAbsoluteWithNonNullBaseIsReturnedAsIs()
  {
    assertEquals("http://foo", PathUtil.resolveRelativeReference("/bar/", "http://foo"));
  }

  @Test
  public void testRelativizeAbsoluteSchemes()
  {
    assertEquals("http://foo", PathUtil.resolveRelativeReference(null, "http://foo"));
    assertEquals("https://foo?q#f", PathUtil.resolveRelativeReference(null, "https://foo?q#f"));
    assertEquals("data:foo", PathUtil.resolveRelativeReference(null, "data:foo"));
  }

  @Test
  public void testRelativizeWithAbsoluteBase()
  {
    assertEquals("http://example.org/foo",
        PathUtil.resolveRelativeReference("http://example.org/", "foo"));
  }

  @Test
  public void testRelativizeWithAbsoluteBaseAndFragment()
  {
    assertEquals("http://example.org/foo",
        PathUtil.resolveRelativeReference("http://example.org/#bar", "foo"));
  }

  @Test
  public void testRelativizeWithAbsoluteBaseAndQuery()
  {
    assertEquals("http://example.org/foo",
        PathUtil.resolveRelativeReference("http://example.org/?test#bar", "foo"));
  }

  @Test
  public void testRelativizeWithAbsoluteBaseIsNormalized()
  {
    assertEquals("http://example.org/foo",
        PathUtil.resolveRelativeReference("http://example.org/foo/../bar", "bar/../foo"));
  }

  @Test
  public void testRelativizeWithRelBase()
  {
    assertEquals("foo/foo", PathUtil.resolveRelativeReference("foo/", "foo"));
  }

  @Test
  public void testRelativizeWithRelBaseIsNormalized()
  {
    assertEquals("foo", PathUtil.resolveRelativeReference("foo", "foo"));
    assertEquals("foo", PathUtil.resolveRelativeReference(".", "foo"));
    assertEquals("../foo", PathUtil.resolveRelativeReference("../", "foo"));
    assertEquals("../foo", PathUtil.resolveRelativeReference("..", "foo"));
    assertEquals("foo/foo/", PathUtil.resolveRelativeReference("foo/", "foo/"));
    assertEquals("bar/foo", PathUtil.resolveRelativeReference("foo/..", "bar/foo"));
  }

  @Test
  public void testRelativizeFragment()
  {
    assertEquals("foo#bar", PathUtil.resolveRelativeReference("foo", "#bar"));
    assertEquals("foo/#bar", PathUtil.resolveRelativeReference("foo/", "#bar"));
    assertEquals("#bar", PathUtil.resolveRelativeReference(".", "#bar"));
  }

  @Test
  public void testRelativizeDecodes()
  {
    assertEquals("base/fo o", PathUtil.resolveRelativeReference("base/", "fo%20o"));
    assertEquals("base/fo+o", PathUtil.resolveRelativeReference("base/", "fo%2Bo"));
    assertEquals("base/fo+o", PathUtil.resolveRelativeReference("base/", "fo+o"));
  }

  @Test
  public void testRemoveAnchor()
  {
    String urlWithoutAnchor = "a/b";
    String urlWithAnchor = urlWithoutAnchor + "#c";
    assertEquals(urlWithoutAnchor, PathUtil.removeFragment(urlWithAnchor));
    assertEquals(urlWithoutAnchor, PathUtil.removeFragment(urlWithoutAnchor));
  }

  @Test
  public void testRemoveWorkingDirectory()
  {
    String OLD_USER_DIR = System.getProperty("user.dir");

    assertEquals(null, PathUtil.removeWorkingDirectory(null));
    assertEquals("", PathUtil.removeWorkingDirectory(""));

    System.setProperty("user.dir", "/user");
    assertEquals("./epub", PathUtil.removeWorkingDirectory("/user/epub"));

    assertEquals("/prefix/user/epub", PathUtil.removeWorkingDirectory("/prefix/user/epub"));

    System.setProperty("user.dir", "/");
    assertEquals("/dir/epub", PathUtil.removeWorkingDirectory("/dir/epub"));

    System.setProperty("user.dir", OLD_USER_DIR);
  }

}

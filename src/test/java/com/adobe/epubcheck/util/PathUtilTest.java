package com.adobe.epubcheck.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PathUtilTest
{

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

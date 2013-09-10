package com.adobe.epubcheck.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathUtilTest {

	@Test
	public void testNormalizePath() {
		// Test nothing to do
		String url1 = "foo/bar";
		assertEquals("foo/bar", PathUtil.normalizePath(url1));
		
		// Test remove of .
		String url21 = "foo/./bar";
		assertEquals("foo/bar", PathUtil.normalizePath(url21));
		String url22 = "./bar";
		assertEquals("bar", PathUtil.normalizePath(url22));

		// Test jump of ..
		String url31 = "foo/../bar";
		assertEquals("bar", PathUtil.normalizePath(url31));

		String url32 = "../bar";
		try {
			PathUtil.normalizePath(url32);
			fail("Should raise an exception with " + url32);
		} catch (IllegalArgumentException e) {
		
		}


	}

	@Test
	public void testRemoveAnchor() {
		String urlWithoutAnchor = "a/b";
		String urlWithAnchor = urlWithoutAnchor + "#c";
		assertEquals(urlWithoutAnchor, PathUtil.removeAnchor(urlWithAnchor));
		assertEquals(urlWithoutAnchor, PathUtil.removeAnchor(urlWithoutAnchor));
	}

}

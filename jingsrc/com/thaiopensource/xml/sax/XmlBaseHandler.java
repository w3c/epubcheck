package com.thaiopensource.xml.sax;

import com.thaiopensource.util.Uri;
import org.xml.sax.Locator;

public class XmlBaseHandler {
  private int depth = 0;
  private Locator loc;
  private Entry stack = null;

  private static class Entry {
    private Entry parent;
    private String attValue;
    private String systemId;
    private int depth;
  }

  public void setLocator(Locator loc) {
    this.loc = loc;
  }

  public void startElement() {
    ++depth;
  }

  public void endElement() {
    if (stack != null && stack.depth == depth)
      stack = stack.parent;
    --depth;
  }

  public void xmlBaseAttribute(String value) {
    Entry entry = new Entry();
    entry.parent = stack;
    stack = entry;
    entry.attValue = Uri.escapeDisallowedChars(value);
    entry.systemId = getSystemId();
    entry.depth = depth;
  }

  private String getSystemId() {
    return loc == null ? null : loc.getSystemId();
  }

  public String getBaseUri() {
    return getBaseUri1(getSystemId(), stack);
  }

  private static String getBaseUri1(String baseUri, Entry stack) {
    if (stack == null
	|| (baseUri != null && !baseUri.equals(stack.systemId)))
      return baseUri;
    baseUri = stack.attValue;
    if (Uri.isAbsolute(baseUri))
      return baseUri;
    return Uri.resolve(getBaseUri1(stack.systemId, stack.parent), baseUri);
  }
}

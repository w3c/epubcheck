package com.adobe.epubcheck.xml;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Utilities for HTML-specific logic.
 * 
 */
public final class HTMLUtils
{

  private static final Set<String> KNOWN_XHTML_NAMESPACES = ImmutableSet.of(Namespaces.XHTML,
      Namespaces.XML, Namespaces.OPS, Namespaces.SVG, Namespaces.MATHML, Namespaces.SSML,
      Namespaces.XMLEVENTS, Namespaces.XLINK);

  private static final Set<String> CASE_INSENSITIVE_ATTRIBUTES = ImmutableSet.<String> builder()
      .add("align").add("allowfullscreen").add("allowpaymentrequest").add("allowusermedia")
      .add("async").add("autocapitalize").add("autocomplete").add("autofocus").add("autoplay")
      .add("checked").add("contenteditable").add("controls").add("crossorigin").add("default")
      .add("defer").add("dir").add("disabled").add("draggable").add("formnovalidate").add("hidden")
      .add("http-equiv").add("ismap").add("itemscope").add("kind").add("loop").add("multiple")
      .add("muted").add("nomodule").add("novalidate").add("open").add("playsinline").add("preload")
      .add("readonly").add("required").add("reversed").add("scope").add("selected").add("shape")
      .add("sizes").add("spellcheck").add("step").add("translate").add("type").add("typemustmatch")
      .add("valign").add("value").add("wrap").build();

  public static boolean isCustomElement(String namespace, String name)
  {
    return Namespaces.XHTML.equals(namespace) && Preconditions.checkNotNull(name).contains("-");
  }

  public static boolean isCustomNamespace(String namespace)
  {
    return !(namespace == null || namespace.trim().isEmpty()
        || KNOWN_XHTML_NAMESPACES.contains(namespace.trim()));
  }

  /**
   * Returns whether an attribute is defined as having a case-insensitive value
   * in HTML. This is notably the case of boolean attributes and enumerated
   * attributes.
   * 
   * @param name
   *          the name of an attribute defined in the HTML specification
   * @return <code>true</code> iff the attribute value is case-insensitive
   */
  public static boolean isCaseInsensitiveAttribute(String namespace, String name)
  {
    return namespace.isEmpty() && CASE_INSENSITIVE_ATTRIBUTES.contains(name);
  }

  public static boolean isDataAttribute(String namespace, String name)
  {
    return namespace.isEmpty() && name.startsWith("data-");
  }

  private HTMLUtils()
  {
    // Not instanciable.
  }

}

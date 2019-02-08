package com.adobe.epubcheck.xml;

import com.google.common.base.Preconditions;

/**
 * Utilities for HTML-specific logic.
 * 
 */
public class HTMLUtils
{
  /**
   * Returns whether an attribute is defined as having a case-insensitive value in
   * HTML. This is notably the case of boolean attributes and enumerated
   * attributes.
   * 
   * @param name
   *          the name of an attribute defined in the HTML specification
   * @return <code>true</code> iff the attribute value is case-insensitive
   */
  public static boolean isCaseInsensitiveAttribute(String name)
  {
    switch (Preconditions.checkNotNull(name))
    {
    case "align":
    case "allowfullscreen":
    case "allowpaymentrequest":
    case "allowusermedia":
    case "async":
    case "autocapitalize":
    case "autocomplete":
    case "autofocus":
    case "autoplay":
    case "checked":
    case "contenteditable":
    case "controls":
    case "crossorigin":
    case "default":
    case "defer":
    case "dir":
    case "disabled":
    case "draggable":
    case "formnovalidate":
    case "hidden":
    case "http-equiv":
    case "ismap":
    case "itemscope":
    case "kind":
    case "loop":
    case "multiple":
    case "muted":
    case "nomodule":
    case "novalidate":
    case "open":
    case "playsinline":
    case "preload":
    case "readonly":
    case "required":
    case "reversed":
    case "scope":
    case "selected":
    case "shape":
    case "sizes":
    case "spellcheck":
    case "step":
    case "translate":
    case "type":
    case "typemustmatch":
    case "valign":
    case "value":
    case "wrap":
      return true;
    default:
      return false;
    }

  }

  public static boolean isCustomElement(String namespace, String name)
  {
    return Namespaces.XHTML.equals(namespace) && Preconditions.checkNotNull(name).contains("-");
  }

}

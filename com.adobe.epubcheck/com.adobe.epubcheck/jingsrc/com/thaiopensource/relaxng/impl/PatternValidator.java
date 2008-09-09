package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.Name;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;

import java.util.Hashtable;

public class PatternValidator extends DtdContext implements Validator, ContentHandler, DTDHandler {
  private final ValidatorPatternBuilder builder;
  private final Pattern start;
  private final ErrorHandler eh;
  private Hashtable recoverPatternTable;
  private PatternMemo memo;
  private boolean hadError;
  private boolean collectingCharacters;
  private final StringBuffer charBuf = new StringBuffer();
  private PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
  private Locator locator;

  private static final class PrefixMapping {
    private final String prefix;
    private final String namespaceURI;
    private final PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PrefixMapping getPrevious() {
      return previous;
    }
  }

  private void startCollectingCharacters() {
    if (!collectingCharacters) {
      collectingCharacters = true;
      charBuf.setLength(0);
    }
  }

  private void flushCharacters() throws SAXException {
    collectingCharacters = false;
    int len = charBuf.length();
    for (int i = 0; i < len; i++) {
      switch (charBuf.charAt(i)) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	text();
	return;
      }
    }
  }

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (collectingCharacters)
      flushCharacters();

    Name name = new Name(namespaceURI, localName);
    if (!setMemo(memo.startTagOpenDeriv(name))) {
      PatternMemo next = memo.startTagOpenRecoverDeriv(name);
      if (!next.isNotAllowed())
        error("required_elements_missing");
      else {
        next = builder.getPatternMemo(builder.makeAfter(findElement(name), memo.getPattern()));
        error(next.isNotAllowed() ? "unknown_element" : "out_of_context_element", name);
      }
      memo = next;
    }
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));

      if (!setMemo(memo.startAttributeDeriv(attName)))
	error("impossible_attribute_ignored", attName);
      else if (!setMemo(memo.dataDeriv(atts.getValue(i), this))) {
        error("bad_attribute_value", attName);
        memo = memo.recoverAfter();
      }
    }
    if (!setMemo(memo.endAttributes())) {
      // XXX should specify which attributes
      error("required_attributes_missing");
      memo = memo.ignoreMissingAttributes();
    }
    if (memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE)
      startCollectingCharacters();
  }

  private PatternMemo fixAfter(PatternMemo p) {
    return builder.getPatternMemo(p.getPattern().applyForPattern(new ApplyAfterFunction(builder) {
      Pattern apply(Pattern p) {
        return builder.makeEmpty();
      }
    }));
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (collectingCharacters) {
      collectingCharacters = false;
      if (!setMemo(memo.textOnly())) {
	error("only_text_not_allowed");
	memo = memo.recoverAfter();
	return;
      }
      final String data = charBuf.toString();
      if (!setMemo(memo.dataDeriv(data, this))) {
        PatternMemo next = memo.recoverAfter();
        if (!memo.isNotAllowed()) {
          if (!next.isNotAllowed()
              || fixAfter(memo).dataDeriv(data, this).isNotAllowed())
            error("string_not_allowed");
        }
        memo = next;
      }
    }
    else if (!setMemo(memo.endTagDeriv())) {
      PatternMemo next = memo.recoverAfter();
      if (!memo.isNotAllowed()) {
        if (!next.isNotAllowed()
            || fixAfter(memo).endTagDeriv().isNotAllowed())
          error("unfinished_element");
      }
      memo = next;
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (collectingCharacters) {
      charBuf.append(ch, start, length);
      return;
    }
    for (int i = 0; i < length; i++) {
      switch (ch[start + i]) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	text();
	return;
      }
    }
  }

  private void text() throws SAXException {
    if (!setMemo(memo.mixedTextDeriv()))
      error("text_not_allowed");
  }

  public void endDocument() {
    // XXX maybe check that memo.isNullable if !hadError
  }

  public void setDocumentLocator(Locator loc) {
    locator = loc;
  }

  public void startDocument() throws SAXException {
    if (memo.isNotAllowed())
      error("schema_allows_nothing");
  }
  public void processingInstruction(String target, String date) { }
  public void skippedEntity(String name) { }
  public void ignorableWhitespace(char[] ch, int start, int len) { }
  public void startPrefixMapping(String prefix, String uri) {
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }
  public void endPrefixMapping(String prefix) {
    prefixMapping = prefixMapping.getPrevious();
  }

  public PatternValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.start = pattern;
    this.builder = builder;
    this.eh = eh;
    reset();
  }

  public void reset() {
    hadError = false;
    collectingCharacters = false;
    locator = null;
    memo = builder.getPatternMemo(start);
    prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
    clearDtdContext();
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  private void error(String key) throws SAXException {
    if (hadError && memo.isNotAllowed())
      return;
    hadError = true;
    eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key), locator));
  }

  private void error(String key, Name arg) throws SAXException {
    error(key, NameFormatter.format(arg));
  }

  private void error(String key, String arg) throws SAXException {
    if (hadError && memo.isNotAllowed())
      return;
    hadError = true;
    eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key, arg), locator));
  }

  /* Return false if m is notAllowed. */
  private boolean setMemo(PatternMemo m) {
    if (m.isNotAllowed())
      return false;
    else {
      memo = m;
      return true;
    }
  }

  private Pattern findElement(Name name) {
    if (recoverPatternTable == null)
     recoverPatternTable = new Hashtable();
    Pattern p = (Pattern)recoverPatternTable.get(name);
    if (p == null) {
      p = FindElementFunction.findElement(builder, name, start);
      recoverPatternTable.put(name, p);
    }
    return p;
  }

  public String resolveNamespacePrefix(String prefix) {
    PrefixMapping tem = prefixMapping;
    do {
      if (tem.prefix.equals(prefix))
        return tem.namespaceURI;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public String getBaseUri() {
    return null;
  }
}

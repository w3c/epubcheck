package com.thaiopensource.validate.nrl;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.DTDHandler;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.nrl.ActionSet;
import com.thaiopensource.validate.nrl.AttributeActionSet;
import com.thaiopensource.validate.nrl.FilteredAttributes;
import com.thaiopensource.validate.nrl.Hashset;
import com.thaiopensource.validate.nrl.IntSet;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import com.thaiopensource.validate.nrl.NoResultAction;
import com.thaiopensource.validate.nrl.ResultAction;
import com.thaiopensource.validate.nrl.SectionState;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;

import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

class ValidatorImpl extends DefaultHandler implements Validator {
  private static final String BEARER_URI = "";
  private static final String BEARER_LOCAL_NAME = "#bearer";
  private static final String NO_NS = "\0";
  private final ErrorHandler eh;
  private final PropertyMap properties;
  private Locator locator;
  private Section currentSection;
  private PrefixMapping prefixMapping = null;
  private final Hashtable validatorHandlerCache = new Hashtable();
  private final Localizer localizer = new Localizer(ValidatorImpl.class);
  private final Hashset noResultActions = new Hashset();
  private final Hashtable attributeNamespaceIndexSets = new Hashtable();
  private final Vector activeHandlersAttributeIndexSets = new Vector();
  private final Hashset attributeSchemas = new Hashset();
  private boolean attributeNamespaceRejected;
  private Attributes filteredAttributes;
  private final Mode startMode;

  static private class PrefixMapping {
    final String prefix;
    final String uri;
    final PrefixMapping parent;

    PrefixMapping(String prefix, String uri, PrefixMapping parent) {
      this.prefix = prefix;
      this.uri = uri;
      this.parent = parent;
    }
  }

  private class Section implements SectionState {
    final Section parent;
    /**
     * Namespace of this section.  Empty string for absent.
     */
    final String ns;
    /**
     * Number of open elements in this section.
     */
    int depth = 0;
    /**
     * List of the Validators rooted in this section
     */
    final Vector validators = new Vector();
    final Vector schemas = new Vector();
    /**
     * List of the ContentHandlers that want to see the elements in this section
     */
    final Vector activeHandlers = new Vector();
    final Vector activeHandlersAttributeModeUsage = new Vector();
    final Vector attributeValidationModeUsages = new Vector();
    /**
     * List of Programs saying what to do with child sections
     */
    final Vector childPrograms = new Vector();
    final Stack context = new Stack();
    boolean contextDependent = false;
    int attributeProcessing = Mode.ATTRIBUTE_PROCESSING_NONE;

    Section(String ns, Section parent) {
      this.ns = ns;
      this.parent = parent;
    }

    public void addChildMode(ModeUsage modeUsage, ContentHandler handler) {
      childPrograms.addElement(new Program(modeUsage, handler));
      if (modeUsage.isContextDependent())
        contextDependent = true;
    }

    public void addValidator(Schema schema, ModeUsage modeUsage) {
      schemas.addElement(schema);
      Validator validator = createValidator(schema);
      validators.addElement(validator);
      activeHandlers.addElement(validator.getContentHandler());
      activeHandlersAttributeModeUsage.addElement(modeUsage);
      attributeProcessing = Math.max(attributeProcessing,
                                     modeUsage.getAttributeProcessing());
      childPrograms.addElement(new Program(modeUsage, validator.getContentHandler()));
      if (modeUsage.isContextDependent())
        contextDependent = true;
    }

    public void addActiveHandler(ContentHandler handler, ModeUsage attributeModeUsage) {
      activeHandlers.addElement(handler);
      activeHandlersAttributeModeUsage.addElement(attributeModeUsage);
      attributeProcessing = Math.max(attributeProcessing,
                                     attributeModeUsage.getAttributeProcessing());
      if (attributeModeUsage.isContextDependent())
        contextDependent = true;
    }

    public void addAttributeValidationModeUsage(ModeUsage modeUsage) {
      int ap = modeUsage.getAttributeProcessing();
      if (ap != Mode.ATTRIBUTE_PROCESSING_NONE) {
        attributeValidationModeUsages.addElement(modeUsage);
        attributeProcessing = Math.max(ap, attributeProcessing);
        if (modeUsage.isContextDependent())
          contextDependent = true;
      }
    }

    public void reject() throws SAXException {
      if (eh != null)
        eh.error(new SAXParseException(localizer.message("reject_element", ns),
                                       locator));
    }

  }

  static private class Program {
    final ModeUsage modeUsage;
    final ContentHandler handler;

    Program(ModeUsage modeUsage, ContentHandler handler) {
      this.modeUsage = modeUsage;
      this.handler = handler;
    }
  }

  ValidatorImpl(Mode mode, PropertyMap properties) {
    this.properties = properties;
    this.eh = ValidateProperty.ERROR_HANDLER.get(properties);
    this.startMode = mode;
    initCurrentSection();
  }

  private void initCurrentSection() {
    currentSection = new Section(NO_NS, null);
    currentSection.addChildMode(new ModeUsage(startMode, startMode), null);
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).characters(ch, start, length);

  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).ignorableWhitespace(ch, start, length);
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (!uri.equals(currentSection.ns))
      startSection(uri);
    currentSection.depth++;
    if (currentSection.contextDependent)
      currentSection.context.push(localName);
    boolean transformAttributes = processAttributes(attributes);
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++) {
      ContentHandler handler = (ContentHandler)(currentSection.activeHandlers.elementAt(i));
      handler.startElement(uri, localName, qName,
                           transformAttributes
                           ? filterAttributes((IntSet)activeHandlersAttributeIndexSets.elementAt(i),
                                              attributes)
                           : attributes);
    }
  }

  private static Attributes filterAttributes(IntSet indexSet, Attributes attributes) {
    if (indexSet.size() == attributes.getLength())
      return attributes;
    return new FilteredAttributes(indexSet, attributes);
  }

  private boolean processAttributes(Attributes attributes) throws SAXException {
    if (currentSection.attributeProcessing == Mode.ATTRIBUTE_PROCESSING_NONE
        || attributes.getLength() == 0)
      return false;
    attributeNamespaceIndexSets.clear();
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      String ns = attributes.getURI(i);
      IntSet indexSet = (IntSet)attributeNamespaceIndexSets.get(ns);
      if (indexSet == null) {
        indexSet = new IntSet();
        attributeNamespaceIndexSets.put(ns, indexSet);
      }
      indexSet.add(i);
    }
    if (currentSection.attributeProcessing == Mode.ATTRIBUTE_PROCESSING_QUALIFIED
        && attributeNamespaceIndexSets.size() == 1
        && attributeNamespaceIndexSets.get("") != null)
      return false;
    Vector handlerModes = currentSection.activeHandlersAttributeModeUsage;
    activeHandlersAttributeIndexSets.setSize(handlerModes.size());
    for (int i = 0, len = handlerModes.size(); i < len; i++)
      activeHandlersAttributeIndexSets.setElementAt(new IntSet(), i);
    boolean transform = false;
    Vector validationModes = currentSection.attributeValidationModeUsages;
    for (Enumeration e = attributeNamespaceIndexSets.keys(); e.hasMoreElements();) {
      String ns = (String)e.nextElement();
      IntSet indexSet = (IntSet)attributeNamespaceIndexSets.get(ns);
      attributeSchemas.clear();
      filteredAttributes = null;
      attributeNamespaceRejected = false;
      for (int i = 0, len = handlerModes.size(); i < len; i++) {
        ModeUsage modeUsage = (ModeUsage)handlerModes.elementAt(i);
        AttributeActionSet actions = processAttributeSection(modeUsage, ns, indexSet, attributes);
        if (actions.getAttach())
          ((IntSet)activeHandlersAttributeIndexSets.get(i)).addAll(indexSet);
        else
          transform = true;
      }
      for (int i = 0, len = validationModes.size(); i < len; i++) {
        ModeUsage modeUsage = (ModeUsage)validationModes.elementAt(i);
        processAttributeSection(modeUsage, ns, indexSet, attributes);
      }
    }
    return transform;
  }

  private AttributeActionSet processAttributeSection(ModeUsage modeUsage,
                                                     String ns,
                                                     IntSet indexSet,
                                                     Attributes attributes)
          throws SAXException {
    Mode mode = modeUsage.getMode(currentSection.context);
    AttributeActionSet actions = mode.getAttributeActions(ns);
    if (actions.getReject() && !attributeNamespaceRejected) {
      attributeNamespaceRejected = true;
      if (eh != null)
        eh.error(new SAXParseException(localizer.message("reject_attribute", ns),
                                       locator));
    }
    Schema[] schemas = actions.getSchemas();
    for (int j = 0; j < schemas.length; j++) {
      if (attributeSchemas.contains(schemas[j]))
        continue;
      attributeSchemas.add(schemas[j]);
      if (filteredAttributes == null)
        filteredAttributes = filterAttributes(indexSet, attributes);
      validateAttributes(schemas[j], filteredAttributes);
    }
    return actions;
  }

  private void validateAttributes(Schema schema, Attributes attributes) throws SAXException {
    Validator validator = createValidator(schema);
    ContentHandler ch = validator.getContentHandler();
    initHandler(ch);
    ch.startElement(BEARER_URI, BEARER_LOCAL_NAME, BEARER_LOCAL_NAME, attributes);
    ch.endElement(BEARER_URI, BEARER_LOCAL_NAME, BEARER_LOCAL_NAME);
    cleanupHandler(ch);
    releaseValidator(schema, validator);
  }

  private void startSection(String uri) throws SAXException {
    Section section = new Section(uri, currentSection);
    Vector childPrograms = currentSection.childPrograms;
    noResultActions.clear();
    for (int i = 0, len = childPrograms.size(); i < len; i++) {
      Program program = (Program)childPrograms.elementAt(i);
      ActionSet actions = program.modeUsage.getMode(currentSection.context).getElementActions(uri);
      ResultAction resultAction = actions.getResultAction();
      if (resultAction != null)
        resultAction.perform(program.handler, section);
      NoResultAction[] nra = actions.getNoResultActions();
      for (int j = 0; j < nra.length; j++) {
        NoResultAction tem = nra[j];
        if (!noResultActions.contains(tem)) {
          nra[j].perform(section);
          noResultActions.add(tem);
        }
      }
    }
    for (int i = 0, len = section.validators.size(); i < len; i++)
      initHandler(((Validator)section.validators.elementAt(i)).getContentHandler());
    currentSection = section;
  }

  private void initHandler(ContentHandler ch) throws SAXException {
    if (locator != null)
      ch.setDocumentLocator(locator);
    ch.startDocument();
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      ch.startPrefixMapping(pm.prefix, pm.uri);
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).endElement(uri, localName, qName);
    currentSection.depth--;
    if (currentSection.contextDependent)
      currentSection.context.pop();
    if (currentSection.depth == 0)
      endSection();
  }

  private void endSection() throws SAXException {
    for (int i = 0, len = currentSection.validators.size(); i < len; i++) {
      Validator validator = (Validator)currentSection.validators.elementAt(i);
      cleanupHandler(validator.getContentHandler());
      releaseValidator((Schema)currentSection.schemas.elementAt(i), validator);
      // endDocument() on one of the validators may throw an exception
      // in this case we don't want to release the validator twice
      currentSection.validators.setElementAt(null, i);
    }
    currentSection = currentSection.parent;
  }

  private void cleanupHandler(ContentHandler vh) throws SAXException {
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      vh.endPrefixMapping(pm.prefix);
    vh.endDocument();
  }

  public void endDocument()
          throws SAXException {
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    super.startPrefixMapping(prefix, uri);
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    super.endPrefixMapping(prefix);
    prefixMapping = prefixMapping.parent;
  }

  private Validator createValidator(Schema schema) {
    Stack stack = (Stack)validatorHandlerCache.get(schema);
    if (stack == null) {
      stack = new Stack();
      validatorHandlerCache.put(schema, stack);
    }
    if (stack.empty())
      return schema.createValidator(properties);
    return (Validator)stack.pop();
  }

  private void releaseValidator(Schema schema, Validator vh) {
    if (vh == null)
      return;
    vh.reset();
    ((Stack)validatorHandlerCache.get(schema)).push(vh);
  }

  public void reset() {
    for (; currentSection != null; currentSection = currentSection.parent) {
      for (int i = 0, len = currentSection.validators.size(); i < len; i++)
        releaseValidator((Schema)currentSection.schemas.elementAt(i),
                         (Validator)currentSection.validators.elementAt(i));
    }
    initCurrentSection();
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }
}

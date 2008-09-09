package com.thaiopensource.validate.mns;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.Uri;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.xml.sax.XmlBaseHandler;
import com.thaiopensource.xml.sax.DelegatingContentHandler;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.StringSplitter;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

class SchemaImpl extends AbstractSchema {
  static final String MNS_URI = "http://www.thaiopensource.com/ns/mns";
  private final Hashtable modeMap = new Hashtable();
  private Mode startMode;
  private static final String DEFAULT_MODE_NAME = "#default";
  private final boolean attributesSchema;

  static private final class WrappedIOException extends RuntimeException {
    private final IOException exception;

    private WrappedIOException(IOException exception) {
      this.exception = exception;
    }

    private IOException getException() {
      return exception;
    }
  }

  static class ElementAction {
    private final Schema schema;
    private final Mode mode;
    private final ContextMap contextMap;
    private final ElementsOrAttributes prune;
    private final Hashset covered = new Hashset();

    ElementAction(String ns, Schema schema, Mode mode, ContextMap contextMap, ElementsOrAttributes prune) {
      this.schema = schema;
      this.mode = mode;
      this.contextMap = contextMap;
      this.prune = prune;
      covered.add(ns);
    }

    Mode getMode() {
      return mode;
    }

    ContextMap getContextMap() {
      return contextMap;
    }

    Schema getSchema() {
      return schema;
    }

    ElementsOrAttributes getPrune() {
      return prune;
    }

    Hashset getCoveredNamespaces() {
      return covered;
    }
  }

  static class Mode {
    private Locator whereDefined;
    private boolean defined = false;
    private ElementsOrAttributes lax;
    private boolean strictDefined = false;
    private final Hashtable elementMap = new Hashtable();
    private final Hashtable attributesMap = new Hashtable();

    Mode(ElementsOrAttributes lax) {
      this.lax = lax;
    }

    ElementsOrAttributes getLax() {
      return lax;
    }

    Schema getAttributesSchema(String ns) {
      return (Schema)attributesMap.get(ns);
    }

    ElementAction getElementAction(String ns) {
      return (ElementAction)elementMap.get(ns);
    }
  }

  private class Handler extends DelegatingContentHandler implements SchemaFuture {
    private final SchemaReceiverImpl sr;
    private ElementAction currentElementAction;
    private boolean hadError = false;
    private final ErrorHandler eh;
    private final CountingErrorHandler ceh;
    private final Localizer localizer = new Localizer(SchemaImpl.class);
    private Locator locator;
    private final XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();
    private int foreignDepth = 0;
    private String contextNs;
    private Mode contextMode;
    private String elementNs;
    private String defaultSchemaType;
    private final Stack nameStack = new Stack();
    private boolean isRoot;
    private int pathDepth = 0;
    private Validator validator;


    Handler(SchemaReceiverImpl sr) {
      this.sr = sr;
      this.eh = ValidateProperty.ERROR_HANDLER.get(sr.getProperties());
      this.ceh = new CountingErrorHandler(eh);
    }

    public void setDocumentLocator(Locator locator) {
      xmlBaseHandler.setLocator(locator);
      this.locator = locator;
    }

    public void startDocument() throws SAXException {
      try {
        PropertyMapBuilder builder = new PropertyMapBuilder(sr.getProperties());
        ValidateProperty.ERROR_HANDLER.put(builder, ceh);
        validator = sr.getMnsSchema().createValidator(builder.toPropertyMap());
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
      catch (IncorrectSchemaException e) {
        throw new RuntimeException("internal error in RNG schema for MNS");
      }
      setDelegate(validator.getContentHandler());
      if (locator != null)
        super.setDocumentLocator(locator);
      super.startDocument();
    }

    public Schema getSchema() throws IncorrectSchemaException, SAXException {
      if (validator == null || ceh.getHadErrorOrFatalError())
        throw new IncorrectSchemaException();
      for (Enumeration enumList = modeMap.keys(); enumList.hasMoreElements();) {
        String modeName = (String)enumList.nextElement();
        Mode mode = (Mode)modeMap.get(modeName);
        if (!mode.defined && !modeName.equals(DEFAULT_MODE_NAME))
          error("undefined_mode", modeName, mode.whereDefined);
      }
      if (hadError)
        throw new IncorrectSchemaException();
      return SchemaImpl.this;
    }

    public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
      if (e instanceof WrappedIOException)
        throw ((WrappedIOException)e).getException();
      return e;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      xmlBaseHandler.startElement();
      String xmlBase = attributes.getValue(WellKnownNamespaces.XML, "base");
      if (xmlBase != null)
        xmlBaseHandler.xmlBaseAttribute(xmlBase);
      if (!MNS_URI.equals(uri) || foreignDepth > 0) {
        foreignDepth++;
        return;
      }
      if (ceh.getHadErrorOrFatalError())
        return;
      if (localName.equals("rules"))
        parseRules(attributes);
      else if (localName.equals("cover"))
        parseCover(attributes);
      else if (localName.equals("context"))
        parseContext(attributes);
      else if (localName.equals("root"))
        parseRoot(attributes);
      else if (localName.equals("element"))
        parseElement(attributes);
      else if (localName.equals("lax"))
        parseLax(attributes);
      else
        parseValidate(localName.equals("validateAttributes"), attributes);
    }

    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {
      super.endElement(namespaceURI, localName, qName);
      xmlBaseHandler.endElement();
      if (foreignDepth > 0) {
        foreignDepth--;
        return;
      }
     if (pathDepth > 0) {
        pathDepth--;
        if (pathDepth == 0)
          endPath();
      }
    }


    private void parseRules(Attributes attributes) {
      String modeName = attributes.getValue("", "startMode");
      if (modeName == null)
        modeName = DEFAULT_MODE_NAME;
      defaultSchemaType = getSchemaType(attributes);
      startMode = lookupCreateMode(modeName);
    }

    private void parseCover(Attributes attributes) throws SAXException {
      String ns = getNs(attributes, false);
      currentElementAction.covered.add(ns);
    }

    private void parseLax(Attributes attributes) throws SAXException {
      String[] modeNames = getInModes(attributes);
      Mode[] modes = getModes(modeNames);
      ElementsOrAttributes lax = toElementsOrAttributes(attributes.getValue("", "allow"),
                                                        ElementsOrAttributes.BOTH);
      for (int i = 0; i < modes.length; i++) {
        if (modes[i].strictDefined)
          error("lax_multiply_defined", modeNames[i]);
        else {
          modes[i].lax = lax;
          modes[i].strictDefined = true;
        }
      }
    }

    private void parseValidate(boolean isAttribute, Attributes attributes) throws SAXException {
      String[] modeNames = getInModes(attributes);
      Mode[] modes = getModes(modeNames);
      String ns = getNs(attributes, isAttribute);
      String schemaUri = getSchema(attributes);
      String schemaType = getSchemaType(attributes);
      if (schemaType == null)
        schemaType = defaultSchemaType;
      try {
        if (isAttribute) {
          Schema schema = sr.createChildSchema(new InputSource(schemaUri), schemaType, true);
          for (int i = 0; i < modes.length; i++) {
            if (modes[i].attributesMap.get(ns) != null)
              error("validate_attributes_multiply_defined", modeNames[i], ns);
            else
              modes[i].attributesMap.put(ns, schema);
          }
        }
        else {
          Schema schema = sr.createChildSchema(new InputSource(schemaUri), schemaType, false);
          currentElementAction = new ElementAction(ns,
                                                   schema,
                                                   getUseMode(attributes),
                                                   new ContextMap(),
                                                   getPrune(attributes));
          contextNs = ns;
          for (int i = 0; i < modes.length; i++) {
            if (modes[i].elementMap.get(ns) != null)
              error("validate_element_multiply_defined", modeNames[i], ns);
            else
              modes[i].elementMap.put(ns, currentElementAction);
          }
        }
      }
      catch (IncorrectSchemaException e) {
        hadError = true;
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
    }

    private void parseContext(Attributes attributes) throws SAXException {
      String ns = getNs(attributes, false);
      if (ns != null)
        contextNs = ns;
      elementNs = contextNs;
      contextMode = getUseMode(attributes);
    }

    private void parseRoot(Attributes attributes) throws SAXException {
      String ns = getNs(attributes, false);
      if (ns != null)
        elementNs = ns;
      isRoot = true;
      pathDepth++;
    }

    private void parseElement(Attributes attributes) throws SAXException {
      String ns = getNs(attributes, false);
      if (ns != null)
        elementNs = ns;
      if (!currentElementAction.covered.contains(elementNs))
        error("context_ns_not_covered", elementNs);
      nameStack.push(new Name(elementNs, attributes.getValue("", "name").trim()));
      pathDepth++;
    }

    private void endPath() throws SAXException {
      if (!currentElementAction.contextMap.put(isRoot, nameStack, contextMode))
        error("path_multiply_defined", displayPath(isRoot, nameStack));
      elementNs = contextNs;
      isRoot = false;
      nameStack.setSize(0);
    }

    private String displayPath(boolean isRoot, Stack nameStack) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0, len = nameStack.size(); i < len; i++) {
        if (i > 0 || isRoot)
          buf.append('/');
        Name name = (Name)nameStack.elementAt(i);
        if (name.getNamespaceUri().length() > 0) {
          buf.append('{');
          buf.append(name.getNamespaceUri());
          buf.append('}');
        }
        buf.append(name.getLocalName());
      }
      return buf.toString();
    }

    private String getSchema(Attributes attributes) throws SAXException {
      String schemaUri = attributes.getValue("", "schema");
      if (Uri.hasFragmentId(schemaUri))
        error("schema_fragment_id");
      return Uri.resolve(xmlBaseHandler.getBaseUri(),
                         Uri.escapeDisallowedChars(schemaUri));
    }

    private String getSchemaType(Attributes attributes) {
      return attributes.getValue("", "schemaType");
    }

    private ElementsOrAttributes getPrune(Attributes attributes) {
      return toElementsOrAttributes(attributes.getValue("", "prune"),
                                    ElementsOrAttributes.NEITHER);
    }

    private ElementsOrAttributes toElementsOrAttributes(String value, ElementsOrAttributes defaultValue) {
      if (value == null)
        return defaultValue;
      ElementsOrAttributes eoa = ElementsOrAttributes.NEITHER;
      if (value.indexOf("elements") >= 0)
        eoa = eoa.addElements();
      if (value.indexOf("attributes") >= 0)
        eoa = eoa.addAttributes();
      return eoa;
    }

    private Mode getUseMode(Attributes attributes) {
      String modeName = attributes.getValue("", "useMode");
      if (modeName == null)
        modeName = DEFAULT_MODE_NAME;
      Mode mode = lookupCreateMode(modeName);
      if (mode.whereDefined == null && locator != null)
        mode.whereDefined = new LocatorImpl(locator);
      return mode;
    }

    private String getNs(Attributes attributes, boolean forbidEmpty) throws SAXException {
      String ns = attributes.getValue("", "ns");
      if (ns != null && !Uri.isAbsolute(ns) && (forbidEmpty || !ns.equals("")))
        error("ns_absolute");
      return ns;
    }

    private Mode[] getModes(String[] modeNames) {
      Mode[] modes = new Mode[modeNames.length];
      for (int i = 0; i < modes.length; i++) {
        modes[i] = lookupCreateMode(modeNames[i]);
        modes[i].defined = true;
      }
      return modes;
    }

    private String[] getInModes(Attributes attributes) {
      String inModes = attributes.getValue("", "inModes");
      if (inModes == null)
        return new String[] { DEFAULT_MODE_NAME };
      return StringSplitter.split(inModes);
    }


    void error(String key) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key), locator));
    }

    void error(String key, String arg) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg), locator));
    }

    void error(String key, String arg, Locator locator) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg), locator));
    }

    void error(String key, String arg1, String arg2) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg1, arg2), locator));
    }

  }

  SchemaImpl(boolean attributesSchema) {
    this.attributesSchema = attributesSchema;
  }

  SchemaFuture installHandlers(XMLReader in, SchemaReceiverImpl sr) {
    Handler h = new Handler(sr);
    in.setContentHandler(h);
    return h;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(startMode, properties);
  }

  private Mode lookupCreateMode(String name) {
    Mode mode = (Mode)modeMap.get(name);
    if (mode == null) {
      mode = new Mode(attributesSchema ? ElementsOrAttributes.ELEMENTS : ElementsOrAttributes.NEITHER);
      modeMap.put(name, mode);
    }
    return mode;
  }

}

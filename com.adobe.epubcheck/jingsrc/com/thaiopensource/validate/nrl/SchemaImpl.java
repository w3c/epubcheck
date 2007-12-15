package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.Uri;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.OptionArgumentException;
import com.thaiopensource.validate.OptionArgumentPresenceException;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.XmlBaseHandler;
import com.thaiopensource.xml.sax.DelegatingContentHandler;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
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
import java.util.Vector;

class SchemaImpl extends AbstractSchema {
  static private final String IMPLICIT_MODE_NAME = "#implicit";
  static private final String WRAPPER_MODE_NAME = "#wrapper";
  static final String NRL_URI = SchemaReader.BASE_URI + "nrl";
  private final Hashtable modeMap = new Hashtable();
  private Mode startMode;
  private final Mode defaultBaseMode;
  private final boolean attributesSchema;
  private final XMLReaderCreator xrc;   // added for ePubCheck

  static private final class WrappedIOException extends RuntimeException {
    private final IOException exception;

    private WrappedIOException(IOException exception) {
      this.exception = exception;
    }

    private IOException getException() {
      return exception;
    }
  }

  static private class MustSupportOption {
    private final String name;
    private final PropertyId pid;
    private final Locator locator;

    MustSupportOption(String name, PropertyId pid, Locator locator) {
      this.name = name;
      this.pid = pid;
      this.locator = locator;
    }
  }

  private class Handler extends DelegatingContentHandler implements SchemaFuture {
    private final SchemaReceiverImpl sr;
    private boolean hadError = false;
    private final ErrorHandler eh;
    private final CountingErrorHandler ceh;
    private final Localizer localizer = new Localizer(SchemaImpl.class);
    private Locator locator;
    private final XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();
    private int foreignDepth = 0;
    private Mode currentMode = null;
    private String defaultSchemaType;
    private Validator validator;
    private ElementsOrAttributes match;
    private ActionSet actions;
    private AttributeActionSet attributeActions;
    private String schemaUri;
    private String schemaType;
    private PropertyMapBuilder options;
    private final Vector mustSupportOptions = new Vector();
    private ModeUsage modeUsage;
    private boolean anyNamespace;

    Handler(SchemaReceiverImpl sr) {
      this.sr = sr;
      this.eh = ValidateProperty.ERROR_HANDLER.get(sr.getProperties());
      this.ceh = new CountingErrorHandler(this.eh);
    }

    public void setDocumentLocator(Locator locator) {
      xmlBaseHandler.setLocator(locator);
      this.locator = locator;
    }

    public void startDocument() throws SAXException {
      try {
        PropertyMapBuilder builder = new PropertyMapBuilder(sr.getProperties());
        ValidateProperty.ERROR_HANDLER.put(builder, ceh);
        validator = sr.getNrlSchema().createValidator(builder.toPropertyMap());
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
      catch (IncorrectSchemaException e) {
        throw new RuntimeException("internal error in RNG schema for NRL");
      }
      setDelegate(validator.getContentHandler());
      if (locator != null)
        super.setDocumentLocator(locator);
      super.startDocument();
    }

    public Schema getSchema() throws IncorrectSchemaException, SAXException {
      if (validator == null || ceh.getHadErrorOrFatalError())
        throw new IncorrectSchemaException();
      Hashset openModes = new Hashset();
      Hashset checkedModes = new Hashset();
      for (Enumeration enumList = modeMap.keys(); enumList.hasMoreElements();) {
        String modeName = (String)enumList.nextElement();
        Mode mode = (Mode)modeMap.get(modeName);
        if (!mode.isDefined())
          error("undefined_mode", modeName, mode.getWhereUsed());
        for (Mode tem = mode; tem != null; tem = tem.getBaseMode()) {
          if (checkedModes.contains(tem))
            break;
          if (openModes.contains(tem)) {
            error("mode_cycle", tem.getName(), tem.getWhereDefined());
            break;
          }
          openModes.add(tem);
        }
        checkedModes.addAll(openModes);
        openModes.clear();
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
      if (!NRL_URI.equals(uri) || foreignDepth > 0) {
        foreignDepth++;
        return;
      }
      if (ceh.getHadErrorOrFatalError())
        return;
      if (localName.equals("rules"))
        parseRules(attributes);
      else if (localName.equals("mode"))
        parseMode(attributes);
      else if (localName.equals("namespace"))
        parseNamespace(attributes);
      else if (localName.equals("anyNamespace"))
        parseAnyNamespace(attributes);
      else if (localName.equals("validate"))
        parseValidate(attributes);
      else if (localName.equals("reject"))
        parseReject(attributes);
      else if (localName.equals("attach"))
        parseAttach(attributes);
      else if (localName.equals("unwrap"))
        parseUnwrap(attributes);
      else if (localName.equals("allow"))
        parseAllow(attributes);
      else if (localName.equals("context"))
        parseContext(attributes);
      else if (localName.equals("option"))
        parseOption(attributes);
      else
        throw new RuntimeException("unexpected element \"" + localName + "\"");
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
      if (ceh.getHadErrorOrFatalError())
        return;
      if (localName.equals("validate"))
        finishValidate();
    }

    private void parseRules(Attributes attributes) {
      startMode = getModeAttribute(attributes, "startMode");
      if (startMode == null) {
        startMode = lookupCreateMode(IMPLICIT_MODE_NAME);
        currentMode = startMode;
        startMode.noteDefined(null);
      }
      startMode.noteUsed(locator);
      if (attributesSchema) {
        Mode wrapper = lookupCreateMode(WRAPPER_MODE_NAME);
        ActionSet actions = new ActionSet();
        actions.addNoResultAction(new AllowAction(new ModeUsage(startMode, startMode)));
        wrapper.bindElement(Mode.ANY_NAMESPACE, actions);
        wrapper.noteDefined(null);
        startMode = wrapper;
      }
      defaultSchemaType = getSchemaType(attributes);
    }

    private void parseMode(Attributes attributes) throws SAXException {
      currentMode = getModeAttribute(attributes, "name");
      if (currentMode.isDefined()) {
        error("duplicate_mode", currentMode.getName());
        error("first_mode", currentMode.getName(), currentMode.getWhereDefined());
      }
      else {
        Mode base = getModeAttribute(attributes, "extends");
        if (base != null)
          currentMode.setBaseMode(base);
        currentMode.noteDefined(locator);
      }
    }

    private void parseNamespace(Attributes attributes) throws SAXException {
      anyNamespace = false;
      parseRule(getNs(attributes), attributes);
    }

    private void parseAnyNamespace(Attributes attributes) throws SAXException {
      anyNamespace = true;
      parseRule(Mode.ANY_NAMESPACE, attributes);
    }

    private void parseRule(String ns, Attributes attributes) throws SAXException {
      match = toElementsOrAttributes(attributes.getValue("", "match"),
                                     ElementsOrAttributes.ELEMENTS);
      if (match.containsAttributes()) {
        attributeActions = new AttributeActionSet();
        if (!currentMode.bindAttribute(ns, attributeActions)) {
          if (ns.equals(Mode.ANY_NAMESPACE))
            error("duplicate_attribute_action_any_namespace");
          else
            error("duplicate_attribute_action", ns);
        }
      }
      if (match.containsElements()) {
        actions = new ActionSet();
        if (!currentMode.bindElement(ns, actions)) {
          if (ns.equals(Mode.ANY_NAMESPACE))
            error("duplicate_element_action_any_namespace");
          else
            error("duplicate_element_action", ns);
        }
      }
      else
        actions = null;
    }

    private void parseValidate(Attributes attributes) throws SAXException {
      schemaUri = getSchema(attributes);
      schemaType = getSchemaType(attributes);
      if (schemaType == null)
        schemaType = defaultSchemaType;
      if (actions != null)
        modeUsage = getModeUsage(attributes);
      else
        modeUsage = null;
      options = new PropertyMapBuilder();
      mustSupportOptions.clear();
    }

    private void finishValidate() throws SAXException {
      try {
        if (attributeActions != null) {
          Schema schema = createSubSchema(true);
          attributeActions.addSchema(schema);
        }
        if (actions != null) {
          Schema schema = createSubSchema(false);
          actions.addNoResultAction(new ValidateAction(modeUsage, schema));
        }
      }
      catch (IncorrectSchemaException e) {
        hadError = true;
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
    }

    //  Added for ePubCheck
    private InputSource makeInputSource(XMLReader xr, String systemId) throws IOException, SAXException {
        EntityResolver er = xr.getEntityResolver();
        if (er != null) {
          InputSource inputSource = er.resolveEntity(null, systemId);
          if (inputSource != null)
    	return inputSource;
        }
        return new InputSource(systemId);
      }
    
    private Schema createSubSchema(boolean isAttributesSchema) throws IOException, IncorrectSchemaException, SAXException {
      PropertyMap requestedProperties = options.toPropertyMap();
      Schema schema = sr.createChildSchema(makeInputSource(xrc.createXMLReader(), schemaUri), // Modified for ePubCheck
                                           schemaType,
                                           requestedProperties,
                                           isAttributesSchema);
      PropertyMap actualProperties = schema.getProperties();
      for (Enumeration enumList = mustSupportOptions.elements(); enumList.hasMoreElements();) {
        MustSupportOption mso = (MustSupportOption)enumList.nextElement();
        Object actualValue = actualProperties.get(mso.pid);
        if (actualValue == null)
          error("unsupported_option", mso.name, mso.locator);
        else if (!actualValue.equals(requestedProperties.get(mso.pid)))
          error("unsupported_option_arg", mso.name, mso.locator);
      }
      return schema;
    }

    private void parseOption(Attributes attributes) throws SAXException {
      boolean mustSupport;
      String mustSupportValue = attributes.getValue("", "mustSupport");
      if (mustSupportValue != null) {
        mustSupportValue = mustSupportValue.trim();
        mustSupport = mustSupportValue.equals("1") || mustSupportValue.equals("true");
      }
      else
        mustSupport = false;
      String name = Uri.resolve(NRL_URI, attributes.getValue("", "name"));
      Option option = sr.getOption(name);
      if (option == null) {
        if (mustSupport)
          error("unknown_option", name);
      }
      else {
        String arg = attributes.getValue("", "arg");
        try {
          PropertyId pid = option.getPropertyId();
          Object value = option.valueOf(arg);
          Object oldValue = options.get(pid);
          if (oldValue != null) {
            value = option.combine(new Object[]{oldValue, value});
            if (value == null)
              error("duplicate_option", name);
            else
              options.put(pid, value);
          }
          else {
            options.put(pid, value);
            mustSupportOptions.addElement(new MustSupportOption(name, pid,
                                                                locator == null
                                                                ? null
                                                                : new LocatorImpl(locator)));
          }
        }
        catch (OptionArgumentPresenceException e) {
          error(arg == null ? "option_requires_argument" : "option_unexpected_argument", name);
        }
        catch (OptionArgumentException e) {
          if (arg == null)
            error("option_requires_argument", name);
          else
            error("option_bad_argument", name, arg);
        }
      }
    }

    private void parseAttach(Attributes attributes) {
      if (attributeActions != null)
        attributeActions.setAttach(true);
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.setResultAction(new AttachAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseUnwrap(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.setResultAction(new UnwrapAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseAllow(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.addNoResultAction(new AllowAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseReject(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.addNoResultAction(new RejectAction(modeUsage));
      }
      else
        modeUsage = null;
      if (attributeActions != null)
        attributeActions.setReject(true);
    }

    private void parseContext(Attributes attributes) throws SAXException {
      if (anyNamespace) {
        error("context_any_namespace");
        return;
      }
      Mode mode = getUseMode(attributes);
      try {
        Vector paths = Path.parse(attributes.getValue("", "path"));
        // XXX warning if modeUsage is null
        if (modeUsage != null) {
          for (int i = 0, len = paths.size(); i < len; i++) {
            Path path = (Path)paths.elementAt(i);
            if (!modeUsage.addContext(path.isRoot(), path.getNames(), mode))
              error("duplicate_path", path.toString());
          }
        }
      }
      catch (Path.ParseException e) {
        error(e.getMessageKey());
      }
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

    private ModeUsage getModeUsage(Attributes attributes) {
      return new ModeUsage(getUseMode(attributes), currentMode);
    }

    private Mode getUseMode(Attributes attributes) {
      Mode mode = getModeAttribute(attributes, "useMode");
      if (mode == null)
        return Mode.CURRENT;
      mode.noteUsed(locator);
      return mode;
    }

    private String getNs(Attributes attributes) throws SAXException {
      String ns = attributes.getValue("", "ns");
      if (ns != null && !Uri.isAbsolute(ns) && !ns.equals(""))
        error("ns_absolute");
      return ns;
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

  // modified for ePubCheck
  SchemaImpl(PropertyMap properties, XMLReaderCreator xrc) {
    super(properties);
    this.xrc = xrc;
    this.attributesSchema = properties.contains(NrlProperty.ATTRIBUTES_SCHEMA);
    makeBuiltinMode("#allow", AllowAction.class);
    makeBuiltinMode("#attach", AttachAction.class);
    makeBuiltinMode("#unwrap", UnwrapAction.class);
    defaultBaseMode = makeBuiltinMode("#reject", RejectAction.class);
  }

  private Mode makeBuiltinMode(String name, Class cls) {
    Mode mode = lookupCreateMode(name);
    ActionSet actions = new ActionSet();
    ModeUsage modeUsage = new ModeUsage(Mode.CURRENT, mode);
    if (cls == AttachAction.class)
      actions.setResultAction(new AttachAction(modeUsage));
    else if (cls == AllowAction.class)
      actions.addNoResultAction(new AllowAction(modeUsage));
    else if (cls == UnwrapAction.class)
      actions.setResultAction(new UnwrapAction(modeUsage));
    else
      actions.addNoResultAction(new RejectAction(modeUsage));
    mode.bindElement(Mode.ANY_NAMESPACE, actions);
    mode.noteDefined(null);
    AttributeActionSet attributeActions = new AttributeActionSet();
    if (attributesSchema)
      attributeActions.setReject(true);
    else
      attributeActions.setAttach(true);
    mode.bindAttribute(Mode.ANY_NAMESPACE, attributeActions);
    return mode;
  }

  SchemaFuture installHandlers(XMLReader in, SchemaReceiverImpl sr) {
    Handler h = new Handler(sr);
    in.setContentHandler(h);
    return h;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(startMode, properties);
  }

  private Mode getModeAttribute(Attributes attributes, String localName) {
    return lookupCreateMode(attributes.getValue("", localName));
  }

  private Mode lookupCreateMode(String name) {
    if (name == null)
      return null;
    name = name.trim();
    Mode mode = (Mode)modeMap.get(name);
    if (mode == null) {
      mode = new Mode(name, defaultBaseMode);
      modeMap.put(name, mode);
    }
    return mode;
  }

}

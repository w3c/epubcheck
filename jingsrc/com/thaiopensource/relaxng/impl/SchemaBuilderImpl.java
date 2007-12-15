package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Div;
import com.thaiopensource.relaxng.parse.ParsedElementAnnotation;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.relaxng.parse.SubParser;
import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.ParsedPatternFuture;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Name;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeBuilder;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;

public class SchemaBuilderImpl implements SchemaBuilder, ElementAnnotationBuilder, CommentList {
  private final SchemaBuilderImpl parent;
  private boolean hadError = false;
  private final SubParser subParser;
  private final SchemaPatternBuilder pb;
  private final DatatypeLibraryFactory datatypeLibraryFactory;
  private final String inheritNs;
  private final ErrorHandler eh;
  private final OpenIncludes openIncludes;
  private final AttributeNameClassChecker attributeNameClassChecker = new AttributeNameClassChecker();
  static final Localizer localizer = new Localizer(SchemaBuilderImpl.class);

  static class OpenIncludes {
    final String uri;
    final OpenIncludes parent;

    OpenIncludes(String uri, OpenIncludes parent) {
      this.uri = uri;
      this.parent = parent;
    }
  }

  static public Pattern parse(Parseable parseable,
                              ErrorHandler eh,
                              DatatypeLibraryFactory datatypeLibraryFactory,
                              SchemaPatternBuilder pb,
                              boolean isAttributesPattern)
          throws IncorrectSchemaException, IOException, SAXException {
    try {
      SchemaBuilderImpl sb = new SchemaBuilderImpl(parseable,
                                                   eh,
                                                   new BuiltinDatatypeLibraryFactory(datatypeLibraryFactory),
                                                   pb);
      ParsedPattern pp = parseable.parse(sb, new RootScope(sb));
      if (isAttributesPattern)
        pp = sb.wrapAttributesPattern(pp);
      return sb.expandPattern((Pattern)pp);
    }
    catch (IllegalSchemaException e) {
      throw new IncorrectSchemaException();
    }
    catch (BuildException e) {
      throw unwrapBuildException(e);
    }
  }


  static public PatternFuture installHandlers(ParseReceiver parser, XMLReader xr, ErrorHandler eh, DatatypeLibraryFactory dlf,
                                              SchemaPatternBuilder pb)
          throws SAXException {
    final SchemaBuilderImpl sb = new SchemaBuilderImpl(parser,
                                                       eh,
                                                       new BuiltinDatatypeLibraryFactory(dlf),
                                                       pb);
    final ParsedPatternFuture pf = parser.installHandlers(xr, sb, new RootScope(sb));
    return new PatternFuture() {
      public Pattern getPattern(boolean isAttributesPattern) throws IncorrectSchemaException, SAXException, IOException {
        try {
          ParsedPattern pp = pf.getParsedPattern();
          if (isAttributesPattern)
            pp = sb.wrapAttributesPattern(pp);
          return sb.expandPattern((Pattern)pp);
        }
        catch (IllegalSchemaException e) {
          throw new IncorrectSchemaException();
        }
        catch (BuildException e) {
          throw unwrapBuildException(e);
        }
      }
    };
  }

  static RuntimeException unwrapBuildException(BuildException e) throws SAXException, IncorrectSchemaException, IOException {
    Throwable t = e.getCause();
    if (t instanceof IOException)
      throw (IOException)t;
    if (t instanceof RuntimeException)
      return (RuntimeException)t;
    if (t instanceof IllegalSchemaException)
      throw new IncorrectSchemaException();
    if (t instanceof SAXException)
      throw (SAXException)t;
    if (t instanceof Exception)
      throw new SAXException((Exception)t);
    throw new SAXException(t.getClass().getName() + " thrown");
  }

  private ParsedPattern wrapAttributesPattern(ParsedPattern pattern) {
    // XXX where can we get a locator from?
    return makeElement(makeAnyName(null, null), pattern, null, null);
  }

  private Pattern expandPattern(Pattern pattern) throws IllegalSchemaException, BuildException {
    if (!hadError) {
      try {
        pattern.checkRecursion(0);
        pattern = pattern.expand(pb);
        pattern.checkRestrictions(Pattern.START_CONTEXT, null, null);
        if (!hadError)
          return pattern;
      }
      catch (SAXParseException e) {
        error(e);
      }
      catch (SAXException e) {
        throw new BuildException(e);
      }
      catch (RestrictionViolationException e) {
        if (e.getName() != null)
          error(e.getMessageId(), NameFormatter.format(e.getName()), e.getLocator());
        else
          error(e.getMessageId(), e.getLocator());
      }
    }
    throw new IllegalSchemaException();
  }

  private SchemaBuilderImpl(SubParser subParser,
                            ErrorHandler eh,
                            DatatypeLibraryFactory datatypeLibraryFactory,
                            SchemaPatternBuilder pb) {
    this.parent = null;
    this.subParser = subParser;
    this.eh = eh;
    this.datatypeLibraryFactory = datatypeLibraryFactory;
    this.pb = pb;
    this.inheritNs = "";
    this.openIncludes = null;
  }

  private SchemaBuilderImpl(String inheritNs,
                            String uri,
                            SchemaBuilderImpl parent) {
    this.parent = parent;
    this.subParser = parent.subParser;
    this.eh = parent.eh;
    this.datatypeLibraryFactory = parent.datatypeLibraryFactory;
    this.pb = parent.pb;
    this.inheritNs = parent.resolveInherit(inheritNs);
    this.openIncludes = new OpenIncludes(uri, parent.openIncludes);
  }

  public ParsedPattern makeChoice(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno)
          throws BuildException {
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = (Pattern)patterns[0];
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeChoice(result, (Pattern)patterns[i]);
    return result;
  }

  public ParsedPattern makeInterleave(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno)
          throws BuildException {
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = (Pattern)patterns[0];
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeInterleave(result, (Pattern)patterns[i]);
    return result;
  }

  public ParsedPattern makeGroup(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno)
          throws BuildException {
    if (nPatterns <= 0)
      throw new IllegalArgumentException();
    Pattern result = (Pattern)patterns[0];
    for (int i = 1; i < nPatterns; i++)
      result = pb.makeGroup(result, (Pattern)patterns[i]);
    return result;
  }

  public ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeOneOrMore((Pattern)p);
  }

  public ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeZeroOrMore((Pattern)p);
  }

  public ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeOptional((Pattern)p);
  }

  public ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeList((Pattern)p, (Locator)loc);
  }

  public ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeMixed((Pattern)p);
  }

  public ParsedPattern makeEmpty(Location loc, Annotations anno) {
    return pb.makeEmpty();
  }

  public ParsedPattern makeNotAllowed(Location loc, Annotations anno) {
    return pb.makeUnexpandedNotAllowed();
  }

  public ParsedPattern makeText(Location loc, Annotations anno) {
    return pb.makeText();
  }

  public ParsedPattern makeErrorPattern() {
    return pb.makeError();
  }

  public ParsedNameClass makeErrorNameClass() {
    return new ErrorNameClass();
  }

  public ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    String messageId = attributeNameClassChecker.checkNameClass((NameClass)nc);
    if (messageId != null)
      error(messageId, (Locator)loc);
    return pb.makeAttribute((NameClass)nc, (Pattern)p, (Locator)loc);
  }

  public ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeElement((NameClass)nc, (Pattern)p, (Locator)loc);
  }

  private class DummyDataPatternBuilder implements DataPatternBuilder {
    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
            throws BuildException {
    }

    public void annotation(ParsedElementAnnotation ea)
            throws BuildException {
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      return pb.makeError();
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      return pb.makeError();
    }
  }

  private class ValidationContextImpl implements ValidationContext {
    private final ValidationContext vc;
    private final String ns;

    ValidationContextImpl(ValidationContext vc, String ns) {
      this.vc = vc;
      this.ns = ns.length() == 0 ? null : ns;
    }

    public String resolveNamespacePrefix(String prefix) {
      String result = prefix.length() == 0 ? ns : vc.resolveNamespacePrefix(prefix);
      if (result == INHERIT_NS) {
        if (inheritNs.length() == 0)
          return null;
        return inheritNs;
      }
      return result;
    }

    public String getBaseUri() {
      return vc.getBaseUri();
    }

    public boolean isUnparsedEntity(String entityName) {
      return vc.isUnparsedEntity(entityName);
    }

    public boolean isNotation(String notationName) {
      return vc.isNotation(notationName);
    }
  }

  private class DataPatternBuilderImpl implements DataPatternBuilder {
    private final DatatypeBuilder dtb;
    DataPatternBuilderImpl(DatatypeBuilder dtb) {
      this.dtb = dtb;
    }

    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
            throws BuildException {
      try {
        dtb.addParameter(name, value, new ValidationContextImpl(context, ns));
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
        int pos = e.getIndex();
        String displayedParam;
        if (pos == DatatypeException.UNKNOWN)
          displayedParam = null;
        else
          displayedParam = displayParam(value, pos);
        if (displayedParam != null) {
          if (detail != null)
            error("invalid_param_detail_display", detail, displayedParam, (Locator)loc);
          else
            error("invalid_param_display", displayedParam, (Locator)loc);
        }
	else if (detail != null)
	  error("invalid_param_detail", detail, (Locator)loc);
	else
	  error("invalid_param", (Locator)loc);
      }
    }

    public void annotation(ParsedElementAnnotation ea)
            throws BuildException {
    }

    String displayParam(String value, int pos) {
      if (pos < 0)
        pos = 0;
      else if (pos > value.length())
        pos = value.length();
      return localizer.message("display_param", value.substring(0, pos), value.substring(pos));
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      try {
        return pb.makeData(dtb.createDatatype());
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, (Locator)loc);
	else
	  error("invalid_params", (Locator)loc);
        return pb.makeError();
      }
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      try {
        return pb.makeDataExcept(dtb.createDatatype(), (Pattern)except, (Locator)loc);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, (Locator)loc);
	else
	  error("invalid_params", (Locator)loc);
        return pb.makeError();
      }
    }
  }

  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc)
          throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    else {
      try {
        return new DataPatternBuilderImpl(dl.createDatatypeBuilder(type));
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("unsupported_datatype_detail", datatypeLibrary, type, detail, (Locator)loc);
	else
	  error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
      }
    }
    return new DummyDataPatternBuilder();
  }

  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, Context context, String ns,
                                 Location loc, Annotations anno) throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    else {
      try {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try {
          Datatype dt = dtb.createDatatype();
          Object obj = dt.createValue(value, new ValidationContextImpl(context, ns));
          if (obj != null)
            return pb.makeValue(dt, obj);
          error("invalid_value", value, (Locator)loc);
        }
        catch (DatatypeException e) {
          String detail = e.getMessage();
          if (detail != null)
            error("datatype_requires_param_detail", detail, (Locator)loc);
          else
            error("datatype_requires_param", (Locator)loc);
        }
      }
      catch (DatatypeException e) {
        error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
      }
    }
    return pb.makeError();
  }

  static class GrammarImpl implements Grammar, Div, IncludedGrammar {
    private final SchemaBuilderImpl sb;
    private final Hashtable defines;
    private final RefPattern startRef;
    private final Scope parent;

    private GrammarImpl(SchemaBuilderImpl sb, Scope parent) {
      this.sb = sb;
      this.parent = parent;
      this.defines = new Hashtable();
      this.startRef = new RefPattern(null);
    }

    protected GrammarImpl(SchemaBuilderImpl sb, GrammarImpl g) {
      this.sb = sb;
      parent = g.parent;
      startRef = g.startRef;
      defines = g.defines;
    }

    public ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException {
      for (Enumeration enumList = defines.keys();
           enumList.hasMoreElements();) {
        String name = (String)enumList.nextElement();
        RefPattern rp = (RefPattern)defines.get(name);
        if (rp.getPattern() == null) {
          sb.error("reference_to_undefined", name, rp.getRefLocator());
          rp.setPattern(sb.pb.makeError());
        }
      }
      Pattern start = startRef.getPattern();
      if (start == null) {
        sb.error("missing_start_element", (Locator)loc);
        start = sb.pb.makeError();
      }
      return start;
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      // nothing to do
    }

    public ParsedPattern endIncludedGrammar(Location loc, Annotations anno) throws BuildException {
      return null;
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      define(lookup(name), combine, pattern, loc);
    }

    private void define(RefPattern rp, GrammarSection.Combine combine, ParsedPattern pattern, Location loc)
            throws BuildException {
      switch (rp.getReplacementStatus()) {
      case RefPattern.REPLACEMENT_KEEP:
        if (combine == null) {
          if (rp.isCombineImplicit()) {
            if (rp.getName() == null)
              sb.error("duplicate_start", (Locator)loc);
            else
              sb.error("duplicate_define", rp.getName(), (Locator)loc);
          }
          else
            rp.setCombineImplicit();
        }
        else {
          byte combineType = (combine == COMBINE_CHOICE ? RefPattern.COMBINE_CHOICE : RefPattern.COMBINE_INTERLEAVE);
          if (rp.getCombineType() != RefPattern.COMBINE_NONE
              && rp.getCombineType() != combineType) {
            if (rp.getName() == null)
              sb.error("conflict_combine_start", (Locator)loc);
            else
              sb.error("conflict_combine_define", rp.getName(), (Locator)loc);
          }
          rp.setCombineType(combineType);
        }
        Pattern p = (Pattern)pattern;
        if (rp.getPattern() == null)
          rp.setPattern(p);
        else if (rp.getCombineType() == RefPattern.COMBINE_INTERLEAVE)
          rp.setPattern(sb.pb.makeInterleave(rp.getPattern(), p));
        else
          rp.setPattern(sb.pb.makeChoice(rp.getPattern(), p));
        break;
      case RefPattern.REPLACEMENT_REQUIRE:
        rp.setReplacementStatus(RefPattern.REPLACEMENT_IGNORE);
        break;
      case RefPattern.REPLACEMENT_IGNORE:
        break;
      }
    }

    public void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException {
    }

    public void topLevelComment(CommentList comments) throws BuildException {
    }

    private RefPattern lookup(String name) {
      if (name == START)
        return startRef;
      return lookup1(name);
    }

    private RefPattern lookup1(String name) {
      RefPattern p = (RefPattern)defines.get(name);
      if (p == null) {
        p = new RefPattern(name);
        defines.put(name, p);
      }
      return p;
    }

    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      RefPattern p = lookup1(name);
      if (p.getRefLocator() == null && loc != null)
        p.setRefLocator((Locator)loc);
      return p;
    }

    public ParsedPattern makeParentRef(String name, Location loc, Annotations anno) throws BuildException {
      if (parent == null) {
        sb.error("parent_ref_outside_grammar", (Locator)loc);
        return sb.makeErrorPattern();
      }
      return parent.makeRef(name, loc, anno);
    }

    public Div makeDiv() {
      return this;
    }

    public Include makeInclude() {
      return new IncludeImpl(sb, this);
    }

  }

  static class RootScope implements Scope {
    private final SchemaBuilderImpl sb;
    RootScope(SchemaBuilderImpl sb) {
      this.sb = sb;
    }

    public ParsedPattern makeParentRef(String name, Location loc, Annotations anno) throws BuildException {
      sb.error("parent_ref_outside_grammar", (Locator)loc);
      return sb.makeErrorPattern();
    }
    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      sb.error("ref_outside_grammar", (Locator)loc);
      return sb.makeErrorPattern();
    }

  }

  static class Override {
    Override(RefPattern prp, Override next) {
      this.prp = prp;
      this.next = next;
    }

    final RefPattern prp;
    final Override next;
    byte replacementStatus;
  }


  private static class IncludeImpl implements Include, Div {
    private final SchemaBuilderImpl sb;
    private Override overrides;
    private final GrammarImpl grammar;

    private IncludeImpl(SchemaBuilderImpl sb, GrammarImpl grammar) {
      this.sb = sb;
      this.grammar = grammar;
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      RefPattern rp = grammar.lookup(name);
      overrides = new Override(rp, overrides);
      grammar.define(rp, combine, pattern, loc);
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      // nothing to do
    }

    public void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException {
      // nothing to do
    }

    public void topLevelComment(CommentList comments) throws BuildException {
    }

    public Div makeDiv() {
      return this;
    }

    public void endInclude(String uri, String ns,
                           Location loc, Annotations anno) throws BuildException {
      for (OpenIncludes inc = sb.openIncludes;
           inc != null;
           inc = inc.parent) {
        if (inc.uri.equals(uri)) {
          sb.error("recursive_include", uri, (Locator)loc);
          return;
        }
      }

      for (Override o = overrides; o != null; o = o.next) {
        o.replacementStatus = o.prp.getReplacementStatus();
        o.prp.setReplacementStatus(RefPattern.REPLACEMENT_REQUIRE);
      }
      try {
        SchemaBuilderImpl isb = new SchemaBuilderImpl(ns, uri, sb);
        sb.subParser.parseInclude(uri, isb, new GrammarImpl(isb, grammar));
        for (Override o = overrides; o != null; o = o.next) {
          if (o.prp.getReplacementStatus() == RefPattern.REPLACEMENT_REQUIRE) {
            if (o.prp.getName() == null)
              sb.error("missing_start_replacement", (Locator)loc);
            else
              sb.error("missing_define_replacement", o.prp.getName(), (Locator)loc);
          }
        }
      }
      catch (IllegalSchemaException e) {
        sb.noteError();
      }
      finally {
        for (Override o = overrides; o != null; o = o.next)
          o.prp.setReplacementStatus(o.replacementStatus);
      }
    }

    public Include makeInclude() {
      return null;
    }
  }

  public Grammar makeGrammar(Scope parent) {
    return new GrammarImpl(this, parent);
  }

  public ParsedPattern annotate(ParsedPattern p, Annotations anno) throws BuildException {
    return p;
  }

  public ParsedNameClass annotate(ParsedNameClass nc, Annotations anno) throws BuildException {
    return nc;
  }

  public ParsedPattern annotateAfter(ParsedPattern p, ParsedElementAnnotation e) throws BuildException {
    return p;
  }

  public ParsedNameClass annotateAfter(ParsedNameClass nc, ParsedElementAnnotation e) throws BuildException {
    return nc;
  }

  public ParsedPattern commentAfter(ParsedPattern p, CommentList comments) throws BuildException {
    return p;
  }

  public ParsedNameClass commentAfter(ParsedNameClass nc, CommentList comments) throws BuildException {
    return nc;
  }

  public ParsedPattern makeExternalRef(String uri, String ns, Scope scope,
                                       Location loc, Annotations anno)
          throws BuildException {
    for (OpenIncludes inc = openIncludes;
         inc != null;
         inc = inc.parent) {
      if (inc.uri.equals(uri)) {
        error("recursive_include", uri, (Locator)loc);
        return pb.makeError();
      }
    }
    try {
      return subParser.parseExternal(uri, new SchemaBuilderImpl(ns, uri, this), scope);
    }
    catch (IllegalSchemaException e) {
      noteError();
      return pb.makeError();
    }
  }

  public ParsedNameClass makeChoice(ParsedNameClass[] nameClasses, int nNameClasses, Location loc, Annotations anno) {
    if (nNameClasses <= 0)
      throw new IllegalArgumentException();
    NameClass result = (NameClass)nameClasses[0];
    for (int i = 1; i < nNameClasses; i++)
      result = new ChoiceNameClass(result, (NameClass)nameClasses[i]);
    return result;
  }

  public ParsedNameClass makeName(String ns, String localName, String prefix, Location loc, Annotations anno) {
    return new SimpleNameClass(new Name(resolveInherit(ns), localName));
  }

  public ParsedNameClass makeNsName(String ns, Location loc, Annotations anno) {
    return new NsNameClass(resolveInherit(ns));
  }

  public ParsedNameClass makeNsName(String ns, ParsedNameClass except, Location loc, Annotations anno) {
    return new NsNameExceptNameClass(resolveInherit(ns), (NameClass)except);
  }

  public ParsedNameClass makeAnyName(Location loc, Annotations anno) {
    return new AnyNameClass();
  }

  public ParsedNameClass makeAnyName(ParsedNameClass except, Location loc, Annotations anno) {
    return new AnyNameExceptNameClass((NameClass)except);
  }

  private final String resolveInherit(String ns) {
    if (ns == INHERIT_NS)
      return inheritNs;
    return ns;
  }

  private class LocatorImpl implements Locator, Location {
    private final String systemId;
    private final int lineNumber;
    private final int columnNumber;

    private LocatorImpl(String systemId, int lineNumber, int columnNumber) {
      this.systemId = systemId;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
    }

    public String getPublicId() {
      return null;
    }

    public String getSystemId() {
      return systemId;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public int getColumnNumber() {
      return columnNumber;
    }
  }

  public Location makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }

  public Annotations makeAnnotations(CommentList comments, Context context) {
    return this;
  }

  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix,
                                                               Location loc, CommentList comments, Context context) {
    return this;
  }

  public CommentList makeCommentList() {
    return this;
  }

  public void addComment(String value, Location loc) throws BuildException {
  }

  public void addAttribute(String ns, String localName, String prefix, String value, Location loc) {
    // nothing needed
  }

  public void addElement(ParsedElementAnnotation ea) {
    // nothing needed
  }

  public void addComment(CommentList comments) throws BuildException {
    // nothing needed
  }

  public void addLeadingComment(CommentList comments) throws BuildException {
    // nothing needed
  }

  public void addText(String value, Location loc, CommentList comments) {
    // nothing needed
  }

  public ParsedElementAnnotation makeElementAnnotation() {
    return null;
  }

  public boolean usesComments() {
    return false;
  }

  private void error(SAXParseException message) throws BuildException {
    noteError();
    try {
      if (eh != null)
        eh.error(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  /*
  private void warning(SAXParseException message) throws BuildException {
    try {
      if (eh != null)
        eh.warning(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }
  */

  private void error(String key, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key), loc));
  }

  private void error(String key, String arg, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg), loc));
  }

  private void error(String key, String arg1, String arg2, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }

  private void error(String key, String arg1, String arg2, String arg3, Locator loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, new Object[]{arg1, arg2, arg3}), loc));
  }
  private void noteError() {
    if (!hadError && parent != null)
      parent.noteError();
    hadError = true;
  }
}

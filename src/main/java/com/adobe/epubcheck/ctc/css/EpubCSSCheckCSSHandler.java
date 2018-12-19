package com.adobe.epubcheck.ctc.css;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssExceptions;
import org.idpf.epubcheck.util.css.CssGrammar;
import org.idpf.epubcheck.util.css.CssLocation;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.LocationImpl;
import com.adobe.epubcheck.util.TextSearchDictionaryEntry;
import com.google.common.base.Optional;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubCSSCheckCSSHandler implements CssContentHandler, CssErrorHandler
{
  String path;
  Report report;
  boolean isGlobalFixedFormat;
  boolean hasIndividualFixedFormatDocuments;
  int scopeId;
  boolean isBody;
  boolean inKeyFrames;
  CSSSelectorCollection currentFileSelectorCollection;
  CSSSelectorCollection inProgressSelectorCollection;
  final Vector<TextSearchDictionaryEntry> declarationComponentItems = new Vector<TextSearchDictionaryEntry>();
  final Vector<TextSearchDictionaryEntry> functionComponentItems = new Vector<TextSearchDictionaryEntry>();
  final Vector<TextSearchDictionaryEntry> atRuleComponentItems = new Vector<TextSearchDictionaryEntry>();
  int startingLineNumber;
  int startingColumnNumber;
  boolean inFontFace;
  boolean hasFontFaceDeclarations;
  CssGrammar.CssAtRule atRule;

  public HashMap<String, ClassUsage> getClassMap()
  {
    return classMap;
  }

  public boolean IncrementGlobalCssClassCount(String attrValue)
  {

    ClassUsage val = getClassMap().get(attrValue);
    if (val != null)
    {
      ++val.Count;
      return true;
    }
    return false;
  }

  public void CheckUnusedCSSClassSelectors(Report report)
  {
    HashMap<String, ClassUsage> map = getClassMap();
    for (ClassUsage cu : map.values())
    {
      if (cu.Count == 0)
      {
        assert (cu.Name != null && !cu.Name.isEmpty());
        report.message(MessageId.CSS_024, getCorrectedEPUBLocation(cu.FileName, cu.Location.getLineNumber(), cu.Location.getColumnNumber(), cu.Name));
      }
    }
  }

  int correctedLineNumber(int lineNumber)
  {
    return startingLineNumber + lineNumber;
  }

  int correctedColumnNumber(int lineNumber, int columnNumber)
  {
    if (lineNumber != 0)
    {
      return columnNumber;
    }
    return startingColumnNumber + columnNumber;
  }

  public class ClassUsage
  {
    public String Name;
    public Location Location;
    public String FileName;
    public int Count;
  }

  HashMap<String, ClassUsage> classMap = new LinkedHashMap<String, ClassUsage>();

  public EpubCSSCheckCSSHandler(Report report, boolean isGlobalFixedFormat, boolean hasIndividualFixedFormatDocuments)
  {
    startingLineNumber = 0;
    startingColumnNumber = 0;
    this.isGlobalFixedFormat = isGlobalFixedFormat;
    this.hasIndividualFixedFormatDocuments = hasIndividualFixedFormatDocuments;
    setReport(report);
    setScopeId(0);
    buildCssSearchDictionaries();
  }

  public EpubCSSCheckCSSHandler(Report report, int startingLineNumber, int startingColumnNumber, boolean isGlobalFixedFormat, boolean hasIndividualFixedFormatDocuments)
  {
    this.startingLineNumber = startingLineNumber - 1;   // To get the right final Line number need to be added using 0 based starting point instead of 1 based.
    this.startingColumnNumber = startingColumnNumber;
    this.isGlobalFixedFormat = isGlobalFixedFormat;
    this.hasIndividualFixedFormatDocuments = hasIndividualFixedFormatDocuments;
    setReport(report);
    setScopeId(0);
    buildCssSearchDictionaries();
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  Report getReport()
  {
    return report;
  }

  void setReport(Report report)
  {
    this.report = report;
  }

  void setScopeId(int scopeId)
  {
    this.scopeId = scopeId;
  }

  void buildCssSearchDictionaries()
  {
    String description;
    String value;
    TextSearchDictionaryEntry de;

    //search eval() expression
    description = "rotateX()";
    value = "rotatex";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    functionComponentItems.add(de);

    description = "rotateY()";
    value = "rotatey";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    functionComponentItems.add(de);

    description = "columns";
    value = "columns";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);

    description = "column-count";
    value = "column-count";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);

    description = "column-gap";
    value = "column-gap";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);

    description = "column-rule";
    value = "column-rule";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);

    description = "keyframes";
    value = "keyframes";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    atRuleComponentItems.add(de);

    description = "transition";
    value = "transition";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);

    description = "box-sizing";
    value = "box-sizing";
    de = new TextSearchDictionaryEntry(description, value, MessageId.CSS_009);
    declarationComponentItems.add(de);
  }

  @Override
  public void startDocument()
  {
    currentFileSelectorCollection = new CSSSelectorCollection(path, new LocationImpl(-1, -1, -1, path, path), scopeId);
  }

  @Override
  public void endDocument()
  {
    currentFileSelectorCollection = null;
    ++scopeId;
  }

  static final Pattern keyframesPattern = Pattern.compile("@((keyframes)|(-moz-keyframes)|(-webkit-keyframes)|(-o-keyframes))");

  @Override
  public void startAtRule(CssGrammar.CssAtRule atRule)
  {
    this.atRule = atRule;
    if (atRule.getName() != Optional.<String>absent())
    {
      String ruleName = atRule.getName().get().toLowerCase(Locale.ROOT);
      CssLocation location = atRule.getLocation();
      if (ruleName.startsWith("@media"))
      {
        getReport().message(MessageId.CSS_023, getCorrectedEPUBLocation(path, location.getLine(), location.getColumn(), atRule.toCssString()));
      }
      else if (keyframesPattern.matcher(ruleName).matches())
      {
        inKeyFrames=true;
      }
      else if (ruleName.equals("@font-face"))
      {
        inFontFace = true;
      }
      searchInsideValue(ruleName, atRule.getLocation().getLine(), location.getColumn(), atRuleComponentItems, path, atRule.toCssString());
    }
  }

  EPUBLocation getCorrectedEPUBLocation(String fileName, int lineNumber, int columnNumber, String context)
  {
    lineNumber = correctedLineNumber(lineNumber);
    columnNumber = correctedColumnNumber(lineNumber, columnNumber);
    return EPUBLocation.create(fileName, lineNumber, columnNumber, context);
  }

  @Override
  public void endAtRule(String ruleName)
  {
    if (ruleName.equals("@font-face"))
    {
      if (!hasFontFaceDeclarations)
      {
        getReport().message(MessageId.CSS_019, getCorrectedEPUBLocation(path, atRule.getLocation().getLine(), atRule.getLocation().getColumn(), atRule.toCssString()), atRule.toCssString());
      }
    }
    inKeyFrames = false;
    inFontFace = false;
    hasFontFaceDeclarations=false;
    atRule = null;
  }

  @Override
  public void selectors(List<CssGrammar.CssSelector> selectors)
  {
    inProgressSelectorCollection = new CSSSelectorCollection(path, getCorrectedLocationFromCssLocation(selectors.get(0).getLocation()), scopeId);
    Queue<CssGrammar.CssConstruct> selectorQueue = new LinkedList<CssGrammar.CssConstruct>();

    isBody = getIsBody(selectors, selectorQueue);

    while (!selectorQueue.isEmpty())
    {
      CssGrammar.CssConstruct construct = selectorQueue.remove();
      CssGrammar.CssConstruct.Type type = construct.getType();
      switch (type)
      {
        case CLASSNAME:
        {//.ident
          CssGrammar.CssClassName className = ((CssGrammar.CssClassName) construct);
          Location location = getCorrectedLocationFromCssLocation(className.getLocation());
          CSSSelector newSelector = new CSSSelector(className.toCssString(), location, true);
          inProgressSelectorCollection.addSelector(newSelector);
          addClassSelector(newSelector, path, location);
          break;
        }

        case STRING:
        case KEYWORD:
        case COMBINATOR:         //space, plus, gt, tilde
          break;
        case ATTRIBUTE_MATCH:       // "~=", "|=", "^=","$=" "*="
        {
          CssGrammar.CssAttributeMatchSelector attributeMatchSelector = ((CssGrammar.CssAttributeMatchSelector) construct);
          Location location = getCorrectedLocationFromCssLocation(attributeMatchSelector.getLocation());
          CSSSelector newSelector = new CSSSelector(attributeMatchSelector.toCssString(), location, false);
          inProgressSelectorCollection.addSelector(newSelector);
          break;
        }
        case HASHNAME:          //#ident
        {//.ident
          CssGrammar.CssHashName hashName = ((CssGrammar.CssHashName) construct);
          Location location = getCorrectedLocationFromCssLocation(hashName.getLocation());
          CSSSelector newSelector = new CSSSelector(hashName.toCssString(), location, false);
          inProgressSelectorCollection.addSelector(newSelector);
          break;
        }
        case ATRULE:
        case QUANTITY:
        case URANGE:
        case URI:
        case SYMBOL:            //a single char, eg operators
        case FUNCTION:
        case DECLARATION:
          break;
        case PSEUDO:            //element and class
        {
          CssGrammar.CssPseudoSelector psuedoSelector = ((CssGrammar.CssPseudoSelector) construct);
          Location location = getCorrectedLocationFromCssLocation(psuedoSelector.getLocation());
          CSSSelector newSelector = new CSSSelector(psuedoSelector.toCssString(), location, false);
          inProgressSelectorCollection.addSelector(newSelector);
          break;
        }
        case TYPE_SELECTOR:        //ns|E, *|E, |E, E, *
        {
          CssGrammar.CssTypeSelector typeSelector = ((CssGrammar.CssTypeSelector) construct);
          Location location = getCorrectedLocationFromCssLocation(typeSelector.getLocation());
          CSSSelector newSelector = new CSSSelector(typeSelector.toCssString(), location, false);
          inProgressSelectorCollection.addSelector(newSelector);
          break;
        }
        case SELECTOR:
        {
          CssGrammar.CssSelector selector = ((CssGrammar.CssSelector) construct);
          for (CssGrammar.CssConstruct c : selector.getComponents())
          {
            selectorQueue.add(c);
          }
          break;
        }
        case SIMPLE_SELECTOR_SEQ:
        {
          CssGrammar.CssSimpleSelectorSequence sequence = ((CssGrammar.CssSimpleSelectorSequence) construct);
          for (CssGrammar.CssConstruct c : sequence.getComponents())
          {
            selectorQueue.add(c);
          }
          break;
        }
        case ATTRIBUTE_SELECTOR:      //[...]
        {
          CssGrammar.CssAttributeSelector attributeSelector = ((CssGrammar.CssAttributeSelector) construct);
          Location location = getCorrectedLocationFromCssLocation(attributeSelector.getLocation());
          CSSSelector newSelector = new CSSSelector(attributeSelector.toCssString(), location, false);
          inProgressSelectorCollection.addSelector(newSelector);
          break;
        }
        case SCOPEDGROUP:        //(...) and [...], the latter when not an attr selector segment
          break;
      }
    }
  }

  static boolean getIsBody(List<CssGrammar.CssSelector> selectors, Queue<CssGrammar.CssConstruct> selectorQueue)
  {
    boolean isBody = false;
    for (CssGrammar.CssSelector selector : selectors)
    {
      selectorQueue.add(selector);
      if (!isBody)
      {
        String selectorName = selector.toCssString();
        if ("body".equalsIgnoreCase(selectorName))
        {
          isBody = true;
        }
      }
    }
    return isBody;
  }

  void addClassSelector(CSSSelector selector, String path, Location location)
  {
    if (selector.isClass())
    {
      String name = selector.getName();
      ClassUsage c = getClassMap().get(name);

      if (c == null)
      {
        c = new ClassUsage();
        c.Count = 0;
        c.FileName = path;
        c.Location = location;
        c.Name = selector.getName();
        getClassMap().put(name, c);
      }
    }
  }

  Location getCorrectedLocationFromCssLocation(CssLocation location)
  {
    return new LocationImpl(correctedLineNumber(location.getLine()), correctedColumnNumber(location.getLine(), location.getColumn()), location.getCharOffset(), location.getSystemID(), location.getSystemID());
  }

  @Override
  public void endSelectors(List<CssGrammar.CssSelector> selectors)
  {
    for (CSSSelector selector : inProgressSelectorCollection.getSelectors().values())
    {
      currentFileSelectorCollection.addSelector(selector);
    }
    inProgressSelectorCollection = null;
    isBody = false;
  }


  @Override
  public void declaration(CssGrammar.CssDeclaration declaration)
  {
    if (declaration.getName() != Optional.<String>absent())
    {
      String text = declaration.getName().get();
      searchInsideValue(text, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declarationComponentItems, path, declaration.toCssString());
      checkTermsAndValues(declaration);
      boolean isImportant = CheckImportant(declaration);
      boolean added = false;
      boolean isFontSize = ("font-size".equalsIgnoreCase(text));
      boolean isFont = ("font".equalsIgnoreCase(text));
      StringBuilder sb = new StringBuilder();

      if (isFont)
      {
        CheckFont(declaration);
      }
      if (isBody)
      {
        CheckBody(declaration);
      }

      for (CssGrammar.CssConstruct construct : declaration.getComponents())
      {
        Vector<TextSearchDictionaryEntry> searchItems;
        String searchText;
        if (isFontSize)
        {
          CheckFontSize(construct, declaration);
        }

        switch (construct.getType())
        {
          case FUNCTION:
          {
            CssGrammar.CssFunction function = (CssGrammar.CssFunction) construct;
            searchItems = functionComponentItems;
            searchText = function.getName().isPresent() ? function.getName().get() : null;
            break;
          }
          default:
          {
            searchItems = declarationComponentItems;
            searchText = construct.toCssString();
            if (inProgressSelectorCollection != null && searchText != null)
            {
              if (added)
              {
                sb.append(" ");
              }
              sb.append(searchText);
              added = true;
            }
            break;
          }
        }
        if (searchText != null)
        {
          searchInsideValue(searchText, construct.getLocation().getLine(), construct.getLocation().getColumn(), searchItems, path, declaration.toCssString());
        }
      }
      if (inProgressSelectorCollection != null)
      {
        String value = sb.toString();
        for (CSSSelector selector : inProgressSelectorCollection.getSelectors().values())
        {
          CSSSelectorAttribute attribute = new CSSSelectorAttribute(text, value, isImportant, selector);
          selector.addAttribute(attribute);
        }
      }
      if (inFontFace)
      {
        hasFontFaceDeclarations = true;
      }
    }
  }

  void checkTermsAndValues(CssGrammar.CssDeclaration declaration)
  {
    if (!isGlobalFixedFormat || hasIndividualFixedFormatDocuments)
    {
      MessageId id = hasIndividualFixedFormatDocuments ? MessageId.CSS_027 : MessageId.CSS_017;
      if ("position".compareToIgnoreCase(declaration.getName().get()) == 0)
      {
        for (CssGrammar.CssConstruct construct : declaration.getComponents())
        {
          if (construct.getType() == CssGrammar.CssConstruct.Type.KEYWORD &&
              "absolute".compareToIgnoreCase(construct.toCssString()) == 0)
          {
            getReport().message(id, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), declaration.getName().get());
            break;
          }
        }
      }
    }
  }

  boolean CheckImportant(CssGrammar.CssDeclaration declaration)
  {
    boolean isImportant = declaration.getImportant();
    if (isImportant)
    {
      getReport().message(MessageId.CSS_013, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()));
    }
    return isImportant;
  }

  void CheckBody(CssGrammar.CssDeclaration declaration)
  {
    String declarationName = declaration.toCssString().toLowerCase(Locale.ROOT);
    if (declarationName.startsWith("margin-") || declarationName.equals("margin"))
    {
      getReport().message(MessageId.CSS_022, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), declarationName);
    }
  }


  void CheckFontSize(CssGrammar.CssConstruct construct, CssGrammar.CssDeclaration declaration)
  {
    MessageId id = hasIndividualFixedFormatDocuments ? MessageId.ACC_016 : MessageId.ACC_014;
    switch (construct.getType())
    {
      case KEYWORD:
      {
        if (!isGlobalFixedFormat || hasIndividualFixedFormatDocuments)
        {
          // report non-relative font-size keyword as ACC USAGE message
          String value = construct.toCssString().toLowerCase(Locale.ROOT);

          // report not allowed font-size keyword as ERROR message
          if (!isFontSize(construct))
          {
            getReport().message(MessageId.CSS_020, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
          }
          else if (("smaller".compareTo(value) != 0) && ("larger".compareTo(value) != 0) && ("inherit".compareTo(value) != 0))
          {
            getReport().message(id, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
          }
          
        }
        break;
      }
      case QUANTITY:
        if (!isGlobalFixedFormat || hasIndividualFixedFormatDocuments)
        {
          CssGrammar.CssQuantity quantity = (CssGrammar.CssQuantity) construct;
          switch (quantity.getUnit())
          {
            case EMS:
            case EXS:
            case REMS:
            case PERCENTAGE:
              break;
            case LENGTH:
              // report absolute font-size as ACC USAGE message
              getReport().message(id, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
              break;
            case INTEGER:
              // issue #922: "0" should be allowed as font-size
              if (!quantity.toCssString().equals("0"))
              {
                // report unsupported font-size as ERROR message
                getReport().message(MessageId.CSS_020, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
              }
              break;
            default:
              // report unsupported font-size as ERROR message
              getReport().message(MessageId.CSS_020, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
              break;
          }
        }
        break;
      default:
        break;
    }
  }

  void CheckFont(CssGrammar.CssDeclaration declaration)
  {
    boolean assignedFontStyle = false;
    boolean assignedFontVariant = false;
    boolean assignedFontWeight = false;

    int i = 0;

    List<CssGrammar.CssConstruct> components = declaration.getComponents();
    CssGrammar.CssConstruct construct;

    if (!matchesSystemFont(declaration))
    {
      while (i < components.size())
      {
        construct = components.get(i);
        if (!assignedFontStyle && isFontStyle(construct))
        {
          assignedFontStyle = true;
          ++i;
          continue;
        }
        if (!assignedFontVariant && isFontVariant(construct))
        {
          assignedFontVariant = true;
          ++i;
          continue;
        }
        if (!assignedFontWeight && isFontWeight(construct))
        {
          assignedFontWeight = true;
          ++i;
          continue;
        }
        if (!assignedFontStyle || !assignedFontVariant || !assignedFontWeight)
        {
          String value = construct.toCssString();
          if (isNormal(value) || isInherit(value))
          {
            if (!assignedFontStyle)
            {
              assignedFontStyle = true;
              ++i;
              continue;
            }
            else if (!assignedFontVariant)
            {
              assignedFontVariant = true;
              ++i;
              continue;
            }
            else
            {
              assignedFontWeight = true;
              ++i;
              continue;
            }
          }
        }
        if (isFontSize(construct))
        {
          CheckFontSize(construct, declaration);
          if (i <= components.size() - 2)
          {
            construct = components.get(++i);
            if (construct.getType() == CssGrammar.CssConstruct.Type.SYMBOL && construct.toCssString().equals("/"))
            {
              construct = components.get(++i);
              CheckLineHeight(construct, declaration);
            }
          }
          return;
        }
        else
        {
          // we got into a state where we didn't recognize the token as a font-size, but it didn't match style/variant/weight either.
          getReport().message(MessageId.CSS_020, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), construct.toCssString());
          return;
        }
      }
    }
  }

  HashSet<String> fontSizes;

  boolean isFontSize(CssGrammar.CssConstruct construct)
  {
    if (null == fontSizes)
    {
      String[] fontSizeStrings = {
          "xx-small",
          "x-small",
          "small",
          "medium",
          "large",
          "x-large",
          "xx-large",
          "larger",
          "smaller",
          "inherit"
      };
      fontSizes = getHashSetFromStrings(fontSizeStrings);
    }

    if (valueMatchesLowercaseLegalValues(construct, fontSizes))
    {
      return true;
    }
    if (construct.getType() == CssGrammar.CssConstruct.Type.QUANTITY)
    {
      CssGrammar.CssQuantity quantity = (CssGrammar.CssQuantity) (construct);
      CssGrammar.CssQuantity.Unit unit = quantity.getUnit();
      switch (unit)
      {
        case PERCENTAGE:
        case EMS:
        case EXS:
        case LENGTH:
          return true;
        case INTEGER:
          if ("0".equals(quantity.toCssString()))
          {
            return true;
          }
      }
    }
    return false;
  }

  void CheckLineHeight(CssGrammar.CssConstruct construct, CssGrammar.CssDeclaration declaration)
  {
    if (!isGlobalFixedFormat || hasIndividualFixedFormatDocuments)
    {
      MessageId id = hasIndividualFixedFormatDocuments ? MessageId.ACC_017 : MessageId.ACC_015;
      if (construct.getType() == CssGrammar.CssConstruct.Type.QUANTITY)
      {
        CssGrammar.CssQuantity quantity = (CssGrammar.CssQuantity) (construct);
        CssGrammar.CssQuantity.Unit unit = quantity.getUnit();
        switch (unit)
        {
          case PERCENTAGE:
          case EMS:
            break;
          case NUMBER:
            break;
          case LENGTH:
            getReport().message(id, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()));
            break;
        }
      }
    }
  }

  HashSet<String> fontVariants;

  boolean isFontVariant(CssGrammar.CssConstruct construct)
  {
    if (null == fontVariants)
    {
      final String[] fontVariantsStrings =
          {
              "small-caps",
          };
      fontVariants = getHashSetFromStrings(fontVariantsStrings);
    }
    return valueMatchesLowercaseLegalValues(construct, fontVariants);
  }

  HashSet<String> fontStyles;

  boolean isFontStyle(CssGrammar.CssConstruct construct)
  {
    if (null == fontStyles)
    {
      final String[] fontStylesStrings = {
          "italic",
          "oblique",
      };
      fontStyles = getHashSetFromStrings(fontStylesStrings);
    }
    return valueMatchesLowercaseLegalValues(construct, fontStyles);
  }

  boolean isNormal(String value)
  {
    return "normal".compareToIgnoreCase(value) == 0;
  }

  boolean isInherit(String value)
  {
    return "inherit".compareToIgnoreCase(value) == 0;
  }

  HashSet<String> fontWeightStrings;

  boolean isFontWeightString(String value)
  {
    if (fontWeightStrings == null)
    {
      final String[] fontWeights = {
          "bold",
          "bolder",
          "lighter",
          "100", "200", "300", "400", "500", "600", "700", "800", "900",
      };
      fontWeightStrings = getHashSetFromStrings(fontWeights);
    }
    return valueMatchesLowercaseLegalValues(value, fontWeightStrings);
  }

  HashSet<String> getHashSetFromStrings(String[] strings)
  {
    HashSet<String> hashSet = new HashSet<String>();
    Collections.addAll(hashSet, strings);
    return hashSet;
  }

  boolean valueMatchesLowercaseLegalValues(CssGrammar.CssConstruct construct, HashSet<String> legalValues)
  {
    return valueMatchesLowercaseLegalValues(construct.toCssString(), legalValues);
  }

  boolean valueMatchesLowercaseLegalValues(String value, HashSet<String> legalValues)
  {
    if (null == value || value.isEmpty())
    {
      return false;
    }

    return legalValues.contains(value.toLowerCase(Locale.ROOT));
  }

  boolean isFontWeight(CssGrammar.CssConstruct construct)
  {
    switch (construct.getType())
    {
      case KEYWORD:
      case STRING:
        return isFontWeightString(construct.toCssString());
      case QUANTITY:
        CssGrammar.CssQuantity quantity = (CssGrammar.CssQuantity) construct;

        if (quantity.getUnit() == CssGrammar.CssQuantity.Unit.INTEGER)
        {
          return isFontWeightString(quantity.toCssString());
        }
        break;
    }
    return false;
  }

  HashSet<String> validSystemFontNames;

  boolean matchesSystemFont(CssGrammar.CssDeclaration declaration)
  {
    List<CssGrammar.CssConstruct> components = declaration.getComponents();
    if (components.size() == 1 && components.get(0).getType() == CssGrammar.CssConstruct.Type.KEYWORD)
    {
      String name = components.get(0).toCssString();
      if (!isValidSystemFontName(name))
      {
        // report error here  = missing size or font family
        getReport().message(MessageId.CSS_021, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()));
      }
      return true; // return true here because we have handled the case of only 1 attribute in the list
    }
    return false;
  }

  boolean isValidSystemFontName(String name)
  {
    if (validSystemFontNames == null)
    {
      String[] validSystemFontNamesStrings = {"caption", "icon", "menu", "message-box", "small-caption", "status-bar", "fantasy", "inherit"};
      validSystemFontNames = getHashSetFromStrings(validSystemFontNamesStrings);
    }
    return valueMatchesLowercaseLegalValues(name, validSystemFontNames);
  }

  static final Pattern invalidTokenStringFinder = Pattern.compile("Token '[0-9]+%' not allowed here");

  @Override
  public void error(CssExceptions.CssException e) throws CssExceptions.CssException
  {
    // Already handled in com.adobe.epubcheck.css.CSSHandler
//    String message = e.getMessage();
//
//    if (inKeyFrames)
//    {
//      Matcher m = invalidTokenStringFinder.matcher(message);
//      if (m.matches())
//      {
//        return;
//      }
//    }
//    CssLocation location = e.getLocation();
//    report.message(MessageId.CSS_008, new EPUBLocation(path, location.getLine(), location.getColumn()), message);
  }

  void searchInsideValue(String entry, int line, int column, Vector<TextSearchDictionaryEntry> tds, String file, String context)
  {
    for (TextSearchDictionaryEntry de : tds)
    {
      Pattern p = de.getPattern();
      Matcher matcher = p.matcher(entry);
      int position = 0;
      while (matcher.find(position))
      {
        position = matcher.end();
        report.message(de.getErrorCode(), getCorrectedEPUBLocation(file, line, column, context));
      }
    }
  }
}

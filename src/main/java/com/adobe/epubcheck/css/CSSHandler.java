package com.adobe.epubcheck.css;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssConstruct;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssGrammar.CssURI;
import org.idpf.epubcheck.util.css.CssLocation;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker;
import com.adobe.epubcheck.opf.XRefChecker.Type;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Sets;

public class CSSHandler implements CssContentHandler, CssErrorHandler
{
  final ValidationContext context;
  final String path;
  final XRefChecker xrefChecker;
  final Report report;
  final EPUBVersion version;
  int startingLineNumber = 0; //append to line info from css parser
  int startingColumnNumber = 0;
  static final CharMatcher SPACE_AND_QUOTES = CharMatcher.anyOf(" \t\n\r\f\"'").precomputed();

  //vars for font-face info
  String fontFamily;
  String fontStyle;
  String fontWeight;
  String fontUri;
  boolean inFontFace = false;
  boolean hasFontFaceDeclarations = false;
  boolean inKeyFrames = false;
  CssAtRule atRule = null;
  
  // properties the must be declared on the related OPF item
  final Set<ITEM_PROPERTIES> detectedProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);

  public CSSHandler(ValidationContext context)
  {
    this.context = context;
    this.path = context.path;
    this.xrefChecker = context.xrefChecker.orNull();
    this.report = context.report;
    this.version = context.version;
  }

  private EPUBLocation getCorrectedEPUBLocation(String fileName, int lineNumber, int columnNumber, String context)
  {
    lineNumber = correctedLineNumber(lineNumber);
    columnNumber = correctedColumnNumber(lineNumber, columnNumber);
    return EPUBLocation.create(fileName, lineNumber, columnNumber, context);
  }

  private int correctedLineNumber(int lineNumber)
  {
    return startingLineNumber + lineNumber;
  }

  private int correctedColumnNumber(int lineNumber, int columnNumber)
  {
    if (lineNumber != 0)
    {
      return columnNumber;
    }
    return startingColumnNumber + columnNumber;
  }


  static final Pattern invalidTokenStringFinder = Pattern.compile("Token '[0-9]+%' not allowed here");

  @Override
  public void error(CssException e) throws
      CssException
  {
    String message = e.getMessage();

    if (inKeyFrames)
    {
      Matcher m = invalidTokenStringFinder.matcher(message);
      if (m.matches())
      {
        return;
      }
    }
    CssLocation location = e.getLocation();
    report.message(MessageId.CSS_008, getCorrectedEPUBLocation(path, location.getLine(), location.getColumn(), null), e.getMessage());
  }

  @Override
  public void startDocument()
  {
  }

  @Override
  public void endDocument()
  {
    checkProperties();
  }

  static final Pattern keyframesPattern = Pattern.compile("@((keyframes)|(-moz-keyframes)|(-webkit-keyframes)|(-o-keyframes))");

  @Override
  public void startAtRule(CssAtRule atRule)
  {
    String ruleName = atRule.getName().get();
    this.atRule = atRule;
    if (ruleName.equals("@import"))
    {
      CssConstruct uriOrString = atRule.getComponents().get(0);
      if (uriOrString != null)
      {
        int line = uriOrString.getLocation().getLine();
        int col = uriOrString.getLocation().getColumn();
        String uri = null;
        if (uriOrString.getType() == CssConstruct.Type.URI)
        {
          uri = ((CssURI) uriOrString).toUriString();
        }
        else if (uriOrString.getType() == CssConstruct.Type.STRING)
        {
          uri = CharMatcher.anyOf("\"'").trimFrom(uriOrString.toCssString());
        }
        else
        {
          //syntax error, url must be first parameter
        }
        if (uri != null)
        {
          resolveAndRegister(uri, line, col, atRule.toCssString(), Type.GENERIC);
        }
      }
    }
    else if(atRule.getName().get().equals("@namespace"))
    {
	    //do not register namespace URIs as resources...
    }
    else
    {
      //check generically for urls in other atrules
      registerURIs(atRule.getComponents(),
          atRule.getLocation().getLine(),
          atRule.getLocation().getColumn());
    }

    if (ruleName.equals("@font-face"))
    {
      inFontFace = true;
    }
    else if (keyframesPattern.matcher(ruleName).matches())
    {
      inKeyFrames=true;
    }
  }

  @Override
  public void endAtRule(String name)
  {
    if (inFontFace)
    {
      inFontFace = false;
      handleFontFaceInfo();
      if (!hasFontFaceDeclarations)
      {
        report.message(MessageId.CSS_019, EPUBLocation.create(path, atRule.getLocation().getLine(), atRule.getLocation().getColumn(), atRule.toCssString()));
      }
      hasFontFaceDeclarations = false;
    }
    if (inKeyFrames)
    {
      inKeyFrames = false;
    }
    atRule = null;
  }

  @Override
  public void selectors(List<CssSelector> selectors)
  {
  }

  @Override
  public void endSelectors(List<CssSelector> selectors)
  {
  }

  @Override
  public void declaration(CssDeclaration declaration)
  {
    registerURIs(declaration.getComponents(),
        declaration.getLocation().getLine(),
        declaration.getLocation().getColumn());

    String propertyName = declaration.getName().get();
    if (propertyName == null)
    {
      return;
    }

    if (propertyName.equals("position"))
    {
      CssConstruct cns = declaration.getComponents().get(0);
      if (cns != null)
      {
        String value = cns.toCssString();
        if (value != null && value.equalsIgnoreCase("fixed"))
        {
          report.message(MessageId.CSS_006, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()));
        }
      }
    }
    if (version == EPUBVersion.VERSION_3)
    {
      if (propertyName.equals("direction") || propertyName.equals("unicode-bidi"))
      {
        report.message(MessageId.CSS_001, getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()), propertyName);
      }
    }

    if (inFontFace)
    {
      hasFontFaceDeclarations = true;

      //collect for info
      if (propertyName.equals("font-family"))
      {
        CssConstruct cc = declaration.getComponents().get(0);
        if (cc != null)
        {
          fontFamily = SPACE_AND_QUOTES.trimFrom(cc.toCssString());
        }
      }
      else if (propertyName.equals("font-style"))
      {
        CssConstruct cc = declaration.getComponents().get(0);
        fontStyle = cc.toCssString();
      }
      else if (propertyName.equals("font-weight"))
      {
        CssConstruct cc = declaration.getComponents().get(0);
        fontWeight = cc.toCssString();
      }
      else if (propertyName.equals("src"))
      {
        for (CssConstruct construct : declaration.getComponents())
        {
          if (construct.getType() == CssConstruct.Type.URI)
          {
            fontUri = ((CssURI) construct).toUriString();
            fontUri = PathUtil.resolveRelativeReference(path, fontUri);
            // check font mimetypes
            String fontMimeType = xrefChecker.getMimeType(fontUri);
            if (fontMimeType != null)
            {
              boolean blessed = true;
              if (version == EPUBVersion.VERSION_2)
              {
                blessed = OPFChecker.isBlessedFontMimetype20(fontMimeType);
              }
              else if (version == EPUBVersion.VERSION_3)
              {
                blessed = OPFChecker30.isBlessedFontType(fontMimeType);
              }
              if (!blessed)
              {
                report.message(MessageId.CSS_007,
                    getCorrectedEPUBLocation(path, declaration.getLocation().getLine(), declaration.getLocation().getColumn(), declaration.toCssString()),
                    fontUri,
                    fontMimeType);
              }
            }
            else
            {
              //errors sb reported elsewhere
            }
          }
        }
      }
      report.message(MessageId.CSS_028,
          getCorrectedEPUBLocation(path,
                                      declaration.getLocation().getLine(),
                                      declaration.getLocation().getColumn(),
              fontUri  != null ? fontUri : "null")
      );
    }
  }

  private void registerURIs(List<CssConstruct> constructs, int line, int col)
  {
    for (CssConstruct construct : constructs)
    {
      if (construct.getType() == CssConstruct.Type.URI)
      {
        resolveAndRegister(((CssURI) construct).toUriString(), line, col, construct.toCssString(), inFontFace?Type.FONT:Type.GENERIC);
      }
    }
  }

  private void resolveAndRegister(String uri, int line, int col, String context, Type type)
  {
    if (uri != null && uri.trim().length() > 0)
    {
      // Fragment-only URLs should be resolved relative to the host document
      // Since we don't have access to the path of the host document(s) here,
      // we ignore this case 
      if (!uri.startsWith("#")) {
        String resolved = PathUtil.resolveRelativeReference(path, uri);
        xrefChecker.registerReference(path, correctedLineNumber(line), correctedColumnNumber(line, col), resolved, type);
        if (PathUtil.isRemote(resolved)) {
          detectedProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
        }
      }
    }
    else
    {
      report.message(MessageId.CSS_002,getCorrectedEPUBLocation(path, line, col, context));
    }
  }

  private void handleFontFaceInfo()
  {
    if (fontFamily != null)
    {
      if (fontUri != null && !fontUri.startsWith("http"))
      {
        report.info(path, FeatureEnum.FONT_EMBEDDED, fontFamily +
            (((fontStyle != null) && !"normal".equalsIgnoreCase(fontStyle)) ? "," + fontStyle : "") +
            (((fontWeight != null) && !"normal".equalsIgnoreCase(fontWeight)) ? "," + fontWeight : "")
        );
      }
      else
      {
        report.info(path, FeatureEnum.FONT_REFERENCE, fontFamily +
            (((fontStyle != null) && !"normal".equalsIgnoreCase(fontStyle)) ? "," + fontStyle : "") +
            (((fontWeight != null) && !"normal".equalsIgnoreCase(fontWeight)) ? "," + fontWeight : "")
        );
        if (fontUri != null) {
        	report.info(path, FeatureEnum.REFERENCE, fontUri);
        }
      }
    }
  }
  
  protected void checkProperties() {

    // Exit early if we don't have container-level info (single file validation)
    if (!context.ocf.isPresent()) // single file validation
    {
      return;
    }
    
    Set<ITEM_PROPERTIES> declaredProperties = Property.filter(context.properties, ITEM_PROPERTIES.class);

     // Check that all properties found in the doc are declared on the OPF item
    for (ITEM_PROPERTIES property : Sets.difference(detectedProperties, declaredProperties))
    {
      report.message(MessageId.OPF_014, EPUBLocation.create(path, startingLineNumber, startingLineNumber),
          PackageVocabs.ITEM_VOCAB.getName(property));
    }
    
    // Check that properties declared in the OPF item were found in the content 
    Set<ITEM_PROPERTIES> uncheckedProperties = Sets.difference(declaredProperties, detectedProperties)
        .copyInto(EnumSet.noneOf(ITEM_PROPERTIES.class));
    if (uncheckedProperties.contains(ITEM_PROPERTIES.REMOTE_RESOURCES))
    {
      uncheckedProperties.remove(ITEM_PROPERTIES.REMOTE_RESOURCES);
      report.message(MessageId.OPF_018, EPUBLocation.create(path, startingLineNumber, startingLineNumber));
    }
  }

  public void setStartingLineNumber(int offset)
  {
    this.startingLineNumber = offset - 1;
    if (this.startingLineNumber < 0)
    {
      this.startingLineNumber = 0;
    }
  }
}

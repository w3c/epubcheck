package com.adobe.epubcheck.ops;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.w3c.epubcheck.constants.MIMEType;
import org.w3c.epubcheck.core.references.Reference;
import org.w3c.epubcheck.core.references.Reference.Type;
import org.w3c.epubcheck.core.references.Resource;
import org.w3c.epubcheck.util.microsyntax.ViewportMeta;
import org.w3c.epubcheck.util.microsyntax.ViewportMeta.ParseError;
import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.EpubConstants;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.SourceSet;
import com.adobe.epubcheck.vocab.AggregateVocab;
import com.adobe.epubcheck.vocab.AltStylesheetVocab;
import com.adobe.epubcheck.vocab.ComicsVocab;
import com.adobe.epubcheck.vocab.DataNavVocab;
import com.adobe.epubcheck.vocab.DictVocab;
import com.adobe.epubcheck.vocab.EpubCheckVocab;
import com.adobe.epubcheck.vocab.ForeignVocabs;
import com.adobe.epubcheck.vocab.IndexVocab;
import com.adobe.epubcheck.vocab.MagazineNavigationVocab;
import com.adobe.epubcheck.vocab.PackageVocabs;
import com.adobe.epubcheck.vocab.PackageVocabs.ITEM_PROPERTIES;
import com.adobe.epubcheck.vocab.Property;
import com.adobe.epubcheck.vocab.StagingEdupubVocab;
import com.adobe.epubcheck.vocab.StructureVocab;
import com.adobe.epubcheck.vocab.StructureVocab.EPUB_TYPES;
import com.adobe.epubcheck.vocab.UncheckedVocab;
import com.adobe.epubcheck.vocab.Vocab;
import com.adobe.epubcheck.vocab.VocabUtil;
import com.adobe.epubcheck.xml.model.XMLAttribute;
import com.adobe.epubcheck.xml.model.XMLElement;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import io.mola.galimatias.URL;

public class OPSHandler30 extends OPSHandler
{
  private static final String HAS_PALPABLE_CONTENT = "IS_PALPABLE";

  private static Map<String, Vocab> RESERVED_VOCABS = ImmutableMap.<String, Vocab> of("",
      AggregateVocab.of(StructureVocab.VOCAB, StagingEdupubVocab.VOCAB, DataNavVocab.VOCAB,
          DictVocab.VOCAB, IndexVocab.VOCAB, ComicsVocab.VOCAB, StructureVocab.UNCHECKED_VOCAB),
      MagazineNavigationVocab.PREFIX, MagazineNavigationVocab.VOCAB, ForeignVocabs.PRISM_PREFIX,
      ForeignVocabs.PRISM_VOCAB);
  private static Map<String, Vocab> ALTCSS_VOCABS = ImmutableMap.<String, Vocab> of("",
      AggregateVocab.of(AltStylesheetVocab.VOCAB, new UncheckedVocab("", "")));
  private static Map<String, Vocab> KNOWN_VOCAB_URIS = ImmutableMap.of(MagazineNavigationVocab.URI,
      MagazineNavigationVocab.VOCAB, ForeignVocabs.PRISM_URI, ForeignVocabs.PRISM_VOCAB);
  private static Set<String> DEFAULT_VOCAB_URIS = ImmutableSet.of(StructureVocab.URI);

  private static final Splitter TOKENIZER = Splitter.onPattern("\\s+").omitEmptyStrings();

  private Map<String, Vocab> vocabs = RESERVED_VOCABS;

  private final Set<ITEM_PROPERTIES> requiredProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);
  private final Set<ITEM_PROPERTIES> allowedProperties = EnumSet.noneOf(ITEM_PROPERTIES.class);

  private final boolean isLinear;

  protected boolean inPicture = false;

  protected boolean anchorNeedsText = false;
  protected boolean inMathML = false;
  protected boolean inSvg = false;
  protected boolean inBody = false;
  protected boolean inRegionBasedNav = false;
  protected boolean isOutermostSVGAlreadyProcessed = false;
  protected boolean hasAltorAnnotation = false;
  protected boolean hasLabel = false;
  protected boolean hasListItem = false;
  protected boolean hasViewport = false;
  private Map<URL, String> mediaSources;

  static protected final String[] scriptEventsStrings = { "onafterprint", "onbeforeprint",
      "onbeforeunload", "onerror", "onhaschange", "onload", "onmessage", "onoffline", "onpagehide",
      "onpageshow", "onpopstate", "onredo", "onresize", "onstorage", "onundo", "onunload",

      "onblur", "onchange", "oncontextmenu", "onfocus", "onformchange", "onforminput", "oninput",
      "oninvalid", "onreset", "onselect", "onsubmit",

      "onkeydown", "onkeypress", "onkeyup",

      "onabort", "oncanplay", "oncanplaythrough", "ondurationchange", "onemptied", "onended",
      "onerror", "onloadeddata", "onloadedmetadata", "onloadstart", "onpause", "onplay",
      "onplaying", "onprogress", "onratechange", "onreadystatechange", "onseeked", "onseeking",
      "onstalled", "onsuspend", "ontimeupdate", "onvolumechange", "onwaiting" };

  static protected HashSet<String> scriptEvents;

  public static HashSet<String> getScriptEvents()
  {
    if (scriptEvents == null)
    {
      scriptEvents = new HashSet<String>();
      Collections.addAll(scriptEvents, scriptEventsStrings);
      Collections.addAll(scriptEvents, mouseEventsStrings);
    }
    return scriptEvents;
  }

  static protected final String[] mouseEventsStrings = { "onclick", "ondblclick", "ondrag",
      "ondragend", "ondragenter", "ondragleave", "ondragover", "ondragstart", "ondrop",
      "onmousedown", "onmousemove", "onmouseout", "onmouseover", "onmouseup", "onmousewheel",
      "onscroll" };
  static protected HashSet<String> mouseEvents;

  public static HashSet<String> getMouseEvents()
  {
    if (mouseEvents == null)
    {
      mouseEvents = new HashSet<String>();
      Collections.addAll(mouseEvents, mouseEventsStrings);
    }
    return mouseEvents;
  }

  public OPSHandler30(ValidationContext context)
  {
    super(context);
    isLinear = !context.properties
        .contains(EpubCheckVocab.VOCAB.get(EpubCheckVocab.PROPERTIES.NON_LINEAR));
  }

  @Override
  protected void checkImage(String attrNS, String attr)
  {
    XMLElement e = currentElement();

    // if it's an SVG image, just register the reference
    if ("http://www.w3.org/2000/svg".equals(e.getNamespace()))
    {
      URL url = checkResourceURL(e.getAttributeNS(attrNS, attr));
      registerReference(url, Reference.Type.IMAGE);
    }
    // else process image or image source sets in HTML
    else
    {
      String src = e.getAttribute("src");
      String srcset = e.getAttribute("srcset");

      // compute a list of image URLs to register
      Set<String> imageSources = new TreeSet<>();
      if (src != null) imageSources.add(src);
      imageSources.addAll(SourceSet.parse(srcset).getImageURLs());

      // register all the URLs
      for (String urlString : imageSources)
      {
        URL url = checkResourceURL(urlString);
        if (url != null && context.referenceRegistry.isPresent())
        {
          Resource imageResource = context.resourceRegistry.get()
              .getResource(URLUtils.docURL(url)).orElse(null);
          // check picture-specific fallback rules
          if (inPicture && imageResource != null)
          {
            String mimetype = imageResource.getMimeType();
            URL imageURL = imageResource.getURL();
            switch (e.getName())
            {
            case "img":
              // an `img` child of `picture` MUST be a core media type resource
              if (!OPFChecker.isBlessedImageType(mimetype, EPUBVersion.VERSION_3))
              {
                report.message(MessageId.MED_003, location(),
                    context.relativize(imageURL), mimetype);
              }
              break;
            case "source":
              // a `source` child of `picture` MUST be core media type resource
              // or have a `type` attribute
              String type = Strings.nullToEmpty(e.getAttribute("type")).trim();
              if (type.isEmpty() && !OPFChecker.isBlessedImageType(mimetype, EPUBVersion.VERSION_3))
              {
                report.message(MessageId.MED_007, location(),
                    context.relativize(imageURL), mimetype);
              }
              else
              {
                // warn about HTML-declared/EPUB-declared type mismatch
                checkMimetypeMatches(url, type);
              }
              break;
            }
          }
          // register the image resource
          // only check manifest fallback if the image is not in `picture`
          registerReference(url, Reference.Type.IMAGE, inPicture);
        }
      }
    }
  }

  protected void checkType(String type)
  {
    if (type == null)
    {
      return;
    }
    Set<Property> propList = VocabUtil.parsePropertyList(type, vocabs, context, location());
    checkTypes(Property.filter(propList, StructureVocab.EPUB_TYPES.class));

    // Check unrecognized properties from the structure vocab
    for (Property property : propList)
    {
      if (StructureVocab.URI.equals(property.getVocabURI())) try
      {
        property.toEnum();
      } catch (UnsupportedOperationException ex)
      {
        report.message(MessageId.OPF_088, location(), property.getName());
      }
    }

    // Check the 'region-based' property (Data Navigation Documents)
    if (propList.contains(DataNavVocab.VOCAB.get(DataNavVocab.EPUB_TYPES.REGION_BASED)))

    {
      if (!"nav".equals(currentElement().getName()) || !context.properties
          .contains(PackageVocabs.ITEM_VOCAB.get(PackageVocabs.ITEM_PROPERTIES.DATA_NAV)))
      {
        report.message(MessageId.HTM_052, location());
      }
      else
      {
        inRegionBasedNav = true;
      }
    }
    // Store whether the doc containt DICT content
    if (propList.contains(DictVocab.VOCAB.get(DictVocab.EPUB_TYPES.DICTIONARY)))
    {
      context.featureReport.report(FeatureEnum.DICTIONARY, location(), null);
    }
  }

  protected void checkTypes(Set<EPUB_TYPES> types)
  {
    if (types.contains(EPUB_TYPES.PAGEBREAK))
    {
      context.featureReport.report(FeatureEnum.PAGE_BREAK, location(), null);
    }
    if (types.contains(EPUB_TYPES.INDEX))
    {
      allowedProperties.add(ITEM_PROPERTIES.INDEX);
      context.featureReport.report(FeatureEnum.INDEX, location(), null);
    }
    if (types.contains(EPUB_TYPES.GLOSSARY))
    {
      allowedProperties.add(ITEM_PROPERTIES.GLOSSARY);
    }
  }

  @Override
  protected URL checkSVGFontFaceURI()
  {
    URL href = super.checkSVGFontFaceURI();
    if (href != null && context.isRemote(href))
    {
      requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
    }
    return href;
  }

  protected void checkSSMLPh(String ph)
  {
    // issue 139; enhancement is to add real syntax check for IPA and x-SAMPA
    if (ph == null)
    {
      return;
    }
    if (ph.trim().length() < 1)
    {
      report.message(MessageId.HTM_007, location());
    }
  }

  @Override
  public void characters(char[] chars, int arg1, int arg2)
  {
    super.characters(chars, arg1, arg2);

    // set the palpable state
    if (!new String(chars, arg1, arg2).trim().isEmpty())
    {
      // FIXME this should only be set for elements allowing flow/phrasing
      currentElement().setPrivateData(HAS_PALPABLE_CONTENT, true);
    }

    if (anchorNeedsText)
    {
      anchorNeedsText = false;
    }
  }

  @Override
  public void startElement()
  {
    super.startElement();

    XMLElement e = currentElement();

    // set this element's initial palpable state to false
    e.setPrivateData(HAS_PALPABLE_CONTENT, false);

    checkDiscouragedElements();
    processSemantics();
    processSectioning();

    String name = e.getName();
    if (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()))
    {
      if (name.equals("html"))
      {
        vocabs = VocabUtil.parsePrefixDeclaration(
            e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "prefix"), RESERVED_VOCABS,
            KNOWN_VOCAB_URIS, DEFAULT_VOCAB_URIS, report, location());
      }
      else if (name.equals("meta"))
      {
        processMeta();
      }
      else if (name.equals("form"))
      {
        requiredProperties.add(ITEM_PROPERTIES.SCRIPTED);
      }
      else if (name.equals("link"))
      {
        processLink();
      }

      else if (name.equals("audio"))
      {
        startMediaElement();
      }
      else if (name.equals("video"))
      {
        processVideo();
        startMediaElement();
      }
      else if (name.equals("figure"))
      {
        processFigure();
      }
      else if (name.equals("table"))
      {
        processTable();
      }
      else if (name.equals("track"))
      {
        startTrack();
      }
      else if (name.equals("a"))
      {
        anchorNeedsText = true;
        processAnchor(e);
      }
      else if (name.equals("input"))
      {
        startInput();
      }
      else if (name.equals("picture"))
      {
        inPicture = true;
      }
      else if (name.equals("source"))
      {
        if ("picture".equals(e.getParent().getName()))
        {
          checkImage(null, null);
        }
        else // audio or video source
        {
          startMediaSource();
        }
      }
      else if (name.equals("embed"))
      {
        startEmbed();
      }
      else if (name.equals("blockquote") || name.equals("q") || name.equals("ins")
          || name.equals("del"))
      {
        checkCiteAttribute();
      }
    }
    else if ("http://www.w3.org/1998/Math/MathML".equals(e.getNamespace()))
    {
      if (name.equals("math"))
      {
        requiredProperties.add(ITEM_PROPERTIES.MATHML);
        inMathML = true;
        hasAltorAnnotation = (null != e.getAttribute("alttext"));
        String altimg = e.getAttribute("altimg");
        if (altimg != null)
        {
          super.checkImage(null, "altimg");
        }

      }
      else if (name.equals("annotation-xml"))
      {
        hasAltorAnnotation = true;
      }
    }
    else if ("http://www.w3.org/2000/svg".equals(e.getNamespace()))
    {
      if (name.equals("svg"))
      {
        processSVG();
      }
      else if (name.equals("a"))
      {
        anchorNeedsText = true;
        processAnchor(e);
      }
      else if (name.equals("title"))
      {
        hasLabel = true;
      }
      else if (name.equals("text"))
      {
        hasLabel = true;
      }
    }
    else if (EpubConstants.EpubTypeNamespaceUri.equals(e.getNamespace()))
    {
      if (name.equals("switch"))
      {
        requiredProperties.add(ITEM_PROPERTIES.SWITCH);
      }
    }

    processInlineScripts();

    checkType(e.getAttributeNS(EpubConstants.EpubTypeNamespaceUri, "type"));

    checkSSMLPh(e.getAttributeNS("http://www.w3.org/2001/10/synthesis", "ph"));
  }

  private void checkCiteAttribute()
  {
    URL url = checkURL(currentElement().getAttribute("cite"));
    registerReference(url, Type.CITE);
  }

  private void startTrack()
  {
    URL url = checkResourceURL(currentElement().getAttribute("src"));
    registerReference(url, Type.TRACK);
  }

  private void startInput()
  {
    URL url = checkResourceURL(currentElement().getAttribute("src"));
    registerReference(url, Type.GENERIC);
  }

  private void startEmbed()
  {
    URL url = checkResourceURL(currentElement().getAttribute("src"));
    checkMimetypeMatches(url, currentElement().getAttribute("type"));
    registerReference(url, Type.GENERIC);

  }

  protected void checkDiscouragedElements()
  {
    XMLElement elem = currentElement();
    if (EpubConstants.HtmlNamespaceUri.equals(elem.getNamespace()))
    {
      switch (elem.getName())
      {
      case "base":
      case "embed":
      case "rp":
        report.message(MessageId.HTM_055, location(), elem.getName());
      }

    }
  }

  protected void processInlineScripts()
  {
    HashSet<String> scriptEvents = getScriptEvents();
    HashSet<String> mouseEvents = getMouseEvents();

    XMLElement e = currentElement();
    for (int i = 0; i < e.getAttributeCount(); ++i)
    {
      XMLAttribute attr = e.getAttribute(i);
      String name = attr.getName().toLowerCase(Locale.ROOT);
      if (scriptEvents.contains(name) || mouseEvents.contains(name))
      {
        processJavascript();
        return;
      }
    }
  }

  @Override
  protected void checkScript()
  {
    super.checkScript();
    URL url = checkResourceURL(currentElement().getAttribute("src"));
    registerReference(url, Type.GENERIC);
  }

  @Override
  protected void processJavascript()
  {
    super.processJavascript();
    requiredProperties.add(ITEM_PROPERTIES.SCRIPTED);
  }

  protected void processLink()
  {
    String classAttribute = currentElement().getAttribute("class");
    if (classAttribute == null)
    {
      return;
    }

    Set<Property> properties = VocabUtil.parsePropertyList(classAttribute, ALTCSS_VOCABS, context,
        location());
    Set<AltStylesheetVocab.PROPERTIES> altClasses = Property.filter(properties,
        AltStylesheetVocab.PROPERTIES.class);

    if (properties.size() == 1)
    {
      return;
    }

    boolean vertical = altClasses.contains(AltStylesheetVocab.PROPERTIES.VERTICAL);
    boolean horizontal = altClasses.contains(AltStylesheetVocab.PROPERTIES.HORIZONTAL);
    boolean day = altClasses.contains(AltStylesheetVocab.PROPERTIES.DAY);
    boolean night = altClasses.contains(AltStylesheetVocab.PROPERTIES.NIGHT);

    if (vertical && horizontal || day && night)
    {
      report.message(MessageId.CSS_005, location(), classAttribute);
    }
  }

  protected void processAnchor(XMLElement e)
  {
    if (e.getAttribute("href") == null)
    {
      anchorNeedsText = false;
    }
    if (inSvg || context.mimeType.equals("image/svg+xml"))
    {
      String title = e.getAttributeNS(EpubConstants.XLinkNamespaceUri, "title");
      String ariaLabel = e.getAttribute("aria-label");
      hasLabel = !Strings.isNullOrEmpty(title) || !Strings.isNullOrEmpty(ariaLabel);
    }
  }

  protected void startMediaElement()
  {
    assert "audio".equals(currentElement().getName()) || "video".equals(currentElement().getName());

    mediaSources = new HashMap<>();

    // check the `src` attribute
    // note: schema ensures if `src` is set, the audio has no `source` children
    URL url = checkResourceURL(currentElement().getAttribute("src"));

    // register the reference (does nothing if URL is null)
    registerMediaResource(url, context.getMimeType(url), false);
  }

  protected void endMediaElement()
  {
    assert "audio".equals(currentElement().getName()) || "video".equals(currentElement().getName());

    // set fallback flag
    // the media element has an intrinsic fallback if any of its source children
    // represent a core media type resource
    boolean hasFallback = mediaSources.values().stream()
        .anyMatch(mimetype -> OPFChecker30.isCoreMediaType(mimetype));

    // register the list of audio sources with the fallback flag
    mediaSources.forEach((url, mimetype) -> registerMediaResource(url, mimetype, hasFallback));
  }

  protected void startMediaSource()
  {
    XMLElement elem = currentElement();
    assert "source".equals(elem.getName());
    if (!("audio".equals(elem.getParent().getName())
        || "video".equals(elem.getParent().getName())))
    {
      return; // schema error was reported
    }

    // check the `src` attribute
    URL url = checkResourceURL(elem.getAttribute("src"));

    // check full-publication rules
    if (context.container.isPresent())
    {
      // get the MIME type of the media resource
      String mimetype = checkMimetypeMatches(url, elem.getAttribute("type"));

      // record the type for fallback checking and resource registration,
      // when closing `audio` after all `source` elements are parsed
      mediaSources.put(url, mimetype);
    }
  }

  protected void registerMediaResource(URL url, String mimetype, boolean hasFallback)
  {
    if (url == null) return;
    if (OPFChecker30.isAudioType(mimetype))
    {
      context.featureReport.report(FeatureEnum.AUDIO, location());
      registerReference(url, Type.AUDIO, hasFallback);
    }
    else
    {
      context.featureReport.report(FeatureEnum.VIDEO, location());
      registerReference(url, Type.VIDEO);
    }
  }

  protected String checkMimetypeMatches(URL resource, String mimetype)
  {
    // get the MIME type of the resource declared in the package document
    String resourceMimetype = context.getMimeType(resource);

    if (mimetype == null)
    {
      return resourceMimetype;
    }
    else
    {
      // remove any params from the given MIME type string
      mimetype = MIMEType.removeParams(mimetype);

      // hack: remove the codecs parameter in the resource type for OPUS audio
      // so that the equality check works
      // TODO remove this when we implement proper MIME type parsing
      if (resourceMimetype != null && resourceMimetype.matches("audio/ogg\\s*;\\s*codecs=opus"))
      {
        resourceMimetype = "audio/ogg";
      }

      // report any MIME type mismatch as a warning
      if (resourceMimetype != null && !resourceMimetype.equals(mimetype))
      {
        report.message(MessageId.OPF_013, location(), context.relativize(resource), mimetype,
            resourceMimetype);
      }
      // return the given MIME type (without parameters)
      return mimetype;
    }

  }

  protected void processVideo()
  {

    URL posterURL = checkResourceURL(currentElement().getAttribute("poster"));
    registerReference(posterURL, Type.IMAGE);

  }

  @Override
  protected void processHyperlink(URL href)
  {
    super.processHyperlink(href);
    if ("data".equals(href.scheme()))
    {
      report.message(MessageId.RSC_029, location());
      return;
    }
    if (inRegionBasedNav)
    {
      registerReference(href, Reference.Type.REGION_BASED_NAV);
    }
  }

  protected URL checkResourceURL(String src)
  {
    if (src == null || src.trim().isEmpty()) return null;

    // parse and check the URL
    URL url = checkURL(src);

    // the `remote-resources` property MUST be set if a remote resource is
    // referenced
    if (context.isRemote(url))
    {
      requiredProperties.add(ITEM_PROPERTIES.REMOTE_RESOURCES);
    }

    // get the resource MIME type
    String mimeType = context.getMimeType(url);
    if (mimeType != null)
    {
      // the `svg` property MAY be set if an SVG resource is referenced in HTML
      if (MIMEType.SVG.is(mimeType) && !MIMEType.SVG.is(context.mimeType))
      {
        allowedProperties.add(ITEM_PROPERTIES.SVG);
      }
    }

    return url;
  }

  @Override
  protected void checkObject()
  {
    // do nothing, we check this in the closing tag
  }

  protected void endObject()
  {
    XMLElement elem = currentElement();

    // check the object resource
    URL url = checkResourceURL(elem.getAttribute("data"));

    // check the MIME type declared for this object
    checkMimetypeMatches(url, elem.getAttribute("type"));

    // the object has intrinsic fallback if it has palpable content
    boolean hasFallback = (boolean) elem.getPrivateData(HAS_PALPABLE_CONTENT);

    // register the reference
    registerReference(url, Type.GENERIC, hasFallback);

  }

  @Override
  protected void checkIFrame()
  {
    super.checkIFrame();
    URL url = checkResourceURL(currentElement().getAttribute("src"));
    registerReference(url, Type.GENERIC);
  }

  protected void processSVG()
  {
    inSvg = true;
    if (!context.mimeType.equals("image/svg+xml"))
    {
      requiredProperties.add(ITEM_PROPERTIES.SVG);
    }
    else if (!isOutermostSVGAlreadyProcessed)
    {
      isOutermostSVGAlreadyProcessed = true;
      if (context.opfItem.isPresent() && context.opfItem.get().isFixedLayout()
          && currentElement().getAttribute("viewBox") == null)
      {

        report.message(MessageId.HTM_048, location());
      }
    }
  }

  protected void processMeta()
  {
    XMLElement e = currentElement();
    if (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()))
    {
      String name = e.getAttribute("name");
      if ("viewport".equals(Strings.nullToEmpty(name).trim()))
      {
        String content = e.getAttribute("content");
        // For fixed-layout documents, check the first viewport meta element
        if (!hasViewport && context.opfItem.isPresent() && context.opfItem.get().isFixedLayout())
        {
          hasViewport = true;
          // parse viewport metadata
          List<ViewportMeta.ParseError> syntaxErrors = new LinkedList<>();
          ViewportMeta viewport = ViewportMeta.parse(content,
              new ViewportMeta.ErrorHandler()
              {
                @Override
                public void error(ParseError error, int position)
                {
                  syntaxErrors.add(error);
                }
              });
          if (!syntaxErrors.isEmpty())
          {
            // report any syntax error
            report.message(MessageId.HTM_047, location(), content);
          }
          else
          {
            for (String property : Arrays.asList("width", "height"))
            {
              // check that viewport metadata has a valid width value
              if (!viewport.hasProperty(property))
              {
                report.message(MessageId.HTM_056, location(), property);
              }
              else
              {
                List<String> values = viewport.getValues(property);
                if (values.size() > 1)
                {
                  report.message(MessageId.HTM_059, location(), property,
                      values.stream().map(v -> '"' + v + '"').collect(Collectors.joining(", ")));
                }
                if (!ViewportMeta.isValidProperty(property, values.get(0)))
                {
                  report.message(MessageId.HTM_057, location(), property);
                }
              }
            }

          }
        }
        else
        {
          // Report ignored secondary viewport meta in fixed-layout documents
          if (context.opfItem.isPresent() && context.opfItem.get().isFixedLayout())
          {
            report.message(MessageId.HTM_060a, location(), content);
          }
          // Report ignored viewport meta in reflowable documents
          else
          {
            report.message(MessageId.HTM_060b, location(), content);
          }
        }
      }
    }
  }

  protected void processTable()
  {
    context.featureReport.report(FeatureEnum.TABLE, location());
  }

  protected void processFigure()
  {
    context.featureReport.report(FeatureEnum.FIGURE, location());
  }

  private void processSemantics()
  {
    XMLElement e = currentElement();
    if (e.getAttribute("itemscope") != null
        && !context.featureReport.hasFeature(FeatureEnum.HAS_MICRODATA))
    {
      context.featureReport.report(FeatureEnum.HAS_MICRODATA, location());
    }
    if (e.getAttribute("property") != null
        && !context.featureReport.hasFeature(FeatureEnum.HAS_RDFA))
    {
      context.featureReport.report(FeatureEnum.HAS_RDFA, location());
    }
  }

  private void processSectioning()
  {
    XMLElement e = currentElement();
    if (isLinear && context.profile == EPUBProfile.EDUPUB
        && EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()))
    {
      if ("body".equals(e.getName()))
      {
        inBody = true;
      }
      else if (inBody && !"section".equals(e.getName()))
      {
        context.featureReport.report(FeatureEnum.SECTIONS, location());
        inBody = false;
      }
      else if ("section".equals(e.getName()))
      {
        inBody = false;
        context.featureReport.report(FeatureEnum.SECTIONS, location());
      }
    }
  }

  @Override
  public void endElement()
  {
    super.endElement();
    XMLElement e = currentElement();
    String name = e.getName();

    if (openElements == 0 && (name.equals("html") || name.equals("svg")))
    {
      checkOverlaysStyles();
      checkProperties();
    }
    else if (name.equals("object"))
    {
      endObject();
    }
    else if (name.equals("video"))
    {
      endMediaElement();
    }
    else if (name.equals("audio"))
    {
      endMediaElement();
    }
    else if (name.equals("a"))
    {
      if (anchorNeedsText)
      {
        report.message(MessageId.ACC_004, location().context("a"));
        anchorNeedsText = false;
      }
      if ((inSvg || context.mimeType.equals("image/svg+xml")) && !hasLabel)
      {
        report.message(MessageId.ACC_011, location().context(e.getName()));
      }
    }
    else if (name.equals("math"))
    {
      inMathML = false;
      if (!hasAltorAnnotation)
      {
        report.message(MessageId.ACC_009, location().context("math"));
      }
    }
    else if (name.equals("nav") && inRegionBasedNav)
    {
      inRegionBasedNav = false;
    }
    else if (name.equals("picture"))
    {
      inPicture = false;
    }
    else if (name.equals("svg"))
    {
      inSvg = false;
    }
    else if (EpubConstants.HtmlNamespaceUri.equals(e.getNamespace()) && name.equals("head"))
    {
      checkHead();
    }

    updatePalpableState();
  }

  protected boolean isPalpable()
  {
    XMLElement elem = currentElement();
    String name = elem.getName();

    if (elem.getAttribute("hidden") != null)
    {
      return false;
    }
    switch (elem.getNamespace())
    {
    case "http://www.w3.org/1999/xhtml":
      switch (name)
      {
      // Embedded Content
      case "audio":
      case "canvas":
      case "embed":
      case "iframe":
      case "img":
      case "object":
      case "picture":
      case "video":
        return true;
      // Special cases
      // case "input":
      // return !"hidden".equals(elem.getAttribute("type"));
      // case "dl":
      // // check dl has name-value group
      // break;
      // case "ul":
      // case "ol":
      // case "menu":
      // // check list has list items
      // break;
      default:
        // FIXME should exclude some elements (e.g. templates, script, etc)
        return (boolean) elem.getPrivateData(HAS_PALPABLE_CONTENT);
      }
    case "http://www.w3.org/2000/svg":
      return "svg".equals(name);
    case "http://www.w3.org/1998/Math/MathML":
      return "math".equals(name);
    default:
      return false;
    }
  }

  private void updatePalpableState()
  {
    XMLElement elem = currentElement();
    if (elem.getParent() != null)
    {
      elem.getParent().getPrivateData().compute(HAS_PALPABLE_CONTENT,
          (k, v) -> (boolean) v || isPalpable());
    }
  }

  protected void checkOverlaysStyles()
  {
    if (context.opfItem.isPresent() && context.opfItem.get().getMediaOverlay() != null
        && (context.featureReport.hasFeature(FeatureEnum.MEDIA_OVERLAYS_ACTIVE_CLASS)
            || context.featureReport.hasFeature(FeatureEnum.MEDIA_OVERLAYS_PLAYBACK_ACTIVE_CLASS))
        && !this.hasCSS)
    {
      report.message(MessageId.CSS_030, location());
    }
  }

  protected void checkProperties()
  {
    if (!context.container.isPresent()) // single file validation
    {
      return;
    }

    Set<ITEM_PROPERTIES> itemProps = Property.filter(context.properties, ITEM_PROPERTIES.class);

    for (ITEM_PROPERTIES requiredProperty : Sets.difference(requiredProperties, itemProps))
    {
      report.message(MessageId.OPF_014, EPUBLocation.of(context),
          PackageVocabs.ITEM_VOCAB.getName(requiredProperty));
    }

    Set<ITEM_PROPERTIES> uncheckedProperties = Sets.difference(itemProps, requiredProperties)
        .copyInto(EnumSet.noneOf(ITEM_PROPERTIES.class));
    uncheckedProperties.remove(ITEM_PROPERTIES.NAV);
    uncheckedProperties.remove(ITEM_PROPERTIES.DATA_NAV);
    uncheckedProperties.remove(ITEM_PROPERTIES.COVER_IMAGE);
    uncheckedProperties.removeAll(allowedProperties);
    if (uncheckedProperties.contains(ITEM_PROPERTIES.REMOTE_RESOURCES))
    {
      uncheckedProperties.remove(ITEM_PROPERTIES.REMOTE_RESOURCES);
      if (!requiredProperties.contains(ITEM_PROPERTIES.SCRIPTED))
      {
        report.message(MessageId.OPF_018, location());
      }
      else
      {
        report.message(MessageId.OPF_018b, location());
      }
    }

    if (!uncheckedProperties.isEmpty())
    {
      report.message(MessageId.OPF_015, EPUBLocation.of(context),
          Joiner.on(", ").join(PackageVocabs.ITEM_VOCAB.getNames(uncheckedProperties)));
    }
  }

  protected void checkHead()
  {
    if (context.opfItem.isPresent() && context.opfItem.get().isFixedLayout() && !hasViewport)
    {
      report.message(MessageId.HTM_046, location());
    }
  }

  @Override
  protected void checkLink()
  {
    super.checkLink();
    XMLElement e = currentElement();
    String rel = e.getAttribute("rel");
    if (rel != null)
    {
      String title = e.getAttribute("title");
      List<String> linkTypes = TOKENIZER.splitToList(rel);
      if (linkTypes.contains("alternate") && linkTypes.contains("stylesheet")
          && Strings.isNullOrEmpty(title))
      {
        report.message(MessageId.CSS_015, location());
      }
    }
  }

}

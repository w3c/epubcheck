package com.adobe.epubcheck.reporting;

import com.adobe.epubcheck.util.FeatureEnum;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is information about the publication in general.  It is intended to be serialized into json.
 */
@SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
class PublicationMetadata
{
  @JsonProperty
  private String publisher;
  @JsonProperty
  private String title;
  @JsonProperty
  private final List<String> creator = new ArrayList<String>();
  @JsonProperty
  private String date;
  @JsonProperty
  private final List<String> subject = new ArrayList<String>();
  @JsonProperty
  private String description;
  @JsonProperty
  private String rights;
  @JsonProperty
  private String identifier;
  @JsonProperty
  private String language;
  @JsonProperty
  private int nSpines;
  @JsonProperty
  private long checkSum;
  @JsonProperty
  private String renditionLayout = "reflowable";
  @JsonProperty
  private String renditionOrientation = "auto";
  @JsonProperty
  private String renditionSpread = "auto";
  @JsonProperty
  private String ePubVersion;
  @JsonProperty
  private boolean isScripted = false;
  @JsonProperty
  private boolean hasFixedFormat = false;
  @JsonProperty
  private boolean isBackwardCompatible = true;
  @JsonProperty
  private boolean hasAudio = false;
  @JsonProperty
  private boolean hasVideo = false;
  @JsonProperty
  private long charsCount = 0;
  @JsonProperty
  private final Set<String> embeddedFonts = new LinkedHashSet<String>();
  @JsonProperty
  private final Set<String> refFonts = new LinkedHashSet<String>();
  @JsonProperty
  private boolean hasEncryption;
  @JsonProperty
  private boolean hasSignatures;
  @JsonProperty
  private final Set<String> contributors = new LinkedHashSet<String>();

  public PublicationMetadata()
  {
  }

  public String getRenditionLayout()
  {
    return this.renditionLayout;
  }

  public String getRenditionOrientation()
  {
    return this.renditionOrientation;
  }

  public String getRenditionSpread()
  {
    return this.renditionSpread;
  }

  public void handleInfo(String resource, FeatureEnum feature, String value)
  {
    switch (feature)
    {
      case DC_TITLE:
        this.title = value;
        break;
      case DC_LANGUAGE:
        this.language = value;
        break;
      case DC_PUBLISHER:
        this.publisher = value;
        break;
      case DC_CREATOR:
        this.creator.add(value);
        break;
      case DC_RIGHTS:
        this.rights = value;
        break;
      case DC_SUBJECT:
        this.subject.add(value);
        break;
      case DC_DESCRIPTION:
        this.description = value;
        break;
      case MODIFIED_DATE:
        this.date = value;
        break;
      case UNIQUE_IDENT:
        if (resource == null)
        {
          this.identifier = value;
        }
        break;
      case FORMAT_VERSION:
        this.ePubVersion = value;
        break;
      case HAS_SCRIPTS:
        this.isScripted = true;
        this.isBackwardCompatible = false;
        break;
      case HAS_FIXED_LAYOUT:
        this.hasFixedFormat = true;
        this.isBackwardCompatible = false;
        break;
      case HAS_HTML5:
        if (resource == null)
        {
          this.isBackwardCompatible = false;
        }
        break;
      case IS_SPINEITEM:
        this.nSpines++;
        break;
      case HAS_NCX:
        if (!Boolean.parseBoolean(value))
        {
          this.isBackwardCompatible = false;
        }
        break;
      case RENDITION_LAYOUT:
        if (resource == null)
        {
          this.renditionLayout = value;
        }
        break;
      case RENDITION_ORIENTATION:
        if (resource == null)
        {
          this.renditionOrientation = value;
        }
        break;
      case RENDITION_SPREAD:
        if (resource == null)
        {
          this.renditionSpread = value;
        }
        break;
      case CHARS_COUNT:
        this.charsCount += Long.parseLong(value);
        break;
      case DECLARED_MIMETYPE:
        if (value != null && value.startsWith("audio/"))
        {
          this.hasAudio = true;
        }
        else if (value != null && value.startsWith("video/"))
        {
          this.hasVideo = true;
        }
        break;
      case FONT_EMBEDDED:
        this.embeddedFonts.add(value);
        break;
      case FONT_REFERENCE:
        this.refFonts.add(value);
        break;
      case HAS_SIGNATURES:
        this.hasSignatures = true;
        break;
      case HAS_ENCRYPTION:
        this.hasEncryption = true;
        break;
      case DC_CONTRIBUTOR:
        this.contributors.add(value);
        break;
    }
  }
}

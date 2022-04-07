package com.adobe.epubcheck.ocf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.w3c.epubcheck.constants.MIMEType;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.ocf.encryption.EncryptionFilter;
import com.adobe.epubcheck.opf.PublicationType;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.vocab.Property;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.mola.galimatias.URL;

//FIXME 2022 add state/preconditions checks
// FIXME 2022 document (note: say this class is not thread safe)
class OCFCheckerState
{
  private ValidationContextBuilder context;

  private final OCFContainer.Builder containerBuilder = new OCFContainer.Builder();
  private OCFContainer container;
  private boolean containerNeedsRebuild = true;

  private final ImmutableList.Builder<URL> packageDocuments = ImmutableList.builder();
  private URL mappingDocument;

  private final Map<URL, EPUBLocation> obfuscated = new HashMap<>();
  private final Map<URL, Set<PublicationType>> publicationTypes = new LinkedHashMap<>();
  private final Map<URL, String> publicationIDs = new LinkedHashMap<>();
  private final Map<URL, EPUBVersion> publicationVersions = new LinkedHashMap<>();
  private final Set<URL> declaredResources = new HashSet<>();
  private String error = "";

  public OCFCheckerState(ValidationContext context)
  {
    this.context = context.copy();
  }

  public void addEncryptedResource(URL resource, EncryptionFilter filter)
  {
    // FIXME 2022 check container contains resource
    containerBuilder.addEncryption(Preconditions.checkNotNull(resource),
        Preconditions.checkNotNull(filter));
    containerNeedsRebuild = true;
  }

  public void addError(String error)
  {
    this.error = Strings.nullToEmpty(error);
  }

  public void addMappingDocument(URL resource)
  {
    this.mappingDocument = resource;
    this.declaredResources.add(resource);
  }

  public void addObfuscatedResource(URL resource, EPUBLocation location)
  {
    // FIXME 2022 check container contains resource
    this.obfuscated.put(Preconditions.checkNotNull(resource), Preconditions.checkNotNull(location));
  }

  public void addResource(OCFResource resource)
  {
    containerBuilder.addResource(resource);
    containerNeedsRebuild = true;
  }

  public void addRootfile(String mediaType, URL resource)
  {
    Preconditions.checkNotNull(mediaType);
    Preconditions.checkNotNull(resource);
    if (MIMEType.PACKAGE_DOC.is(mediaType))
    {
      this.packageDocuments.add(resource);
    }
    this.declaredResources.add(resource);
  }

  public void addType(URL url, String typeString)
  {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(typeString);
    try
    {
      PublicationType type = PublicationType.valueOf(typeString.toUpperCase(Locale.ROOT));
      if (!publicationTypes.containsKey(url))
      {
        publicationTypes.put(url, new LinkedHashSet<>());
      }
      publicationTypes.get(url).add(type);
    } catch (IllegalArgumentException e)
    {
      // ignore, the type is not added if not known
    }
  }

  public void addUniqueId(URL url, String id)
  {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(id);
    publicationIDs.put(url, id);

  }

  public void addVersion(URL url, EPUBVersion version)
  {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(version);
    publicationVersions.put(url, version);
  }

  public void errorReset()
  {
    error = "";
  }

  public ValidationContextBuilder context()
  {
    return context.build().copy();
  }

  public OCFContainer getContainer()
  {
    if (containerNeedsRebuild)
    {
      container = containerBuilder.build();
      context = context.container(container);
      containerNeedsRebuild = false;
    }
    return container;
  }

  public String getError()
  {
    return error;
  }

  public Optional<URL> getMappingDocument()
  {
    return Optional.ofNullable(mappingDocument);
  }

  public EPUBLocation getObfuscationLocation(URL resource)
  {
    EPUBLocation location = obfuscated.get(resource);
    if (location == null)
    {
      throw new IllegalStateException();
    }
    return location;
  }

  public List<URL> getPackageDocuments()
  {
    return packageDocuments.build();
  }

  public Optional<String> getPublicationID()
  {
    Iterator<String> values = publicationIDs.values().iterator();
    return values.hasNext() ? Optional.of(values.next()) : Optional.empty();
  }

  public Set<PublicationType> getPublicationTypes()
  {
    Iterator<Set<PublicationType>> values = publicationTypes.values().iterator();
    return values.hasNext() ? ImmutableSet.copyOf(values.next()) : ImmutableSet.of();
  }

  public Set<PublicationType> getPublicationTypes(URL url)
  {
    Preconditions.checkArgument(url != null);
    Set<PublicationType> types = publicationTypes.get(url);
    return (types != null) ? ImmutableSet.copyOf(types) : ImmutableSet.of();
  }

  public Optional<EPUBVersion> getPublicationVersion()
  {
    Iterator<EPUBVersion> values = publicationVersions.values().iterator();
    return values.hasNext() ? Optional.of(values.next()) : Optional.empty();
  }

  public EPUBVersion getPublicationVersion(URL url)
  {
    Preconditions.checkArgument(url != null);
    EPUBVersion version = publicationVersions.get(url);
    return (version != null) ? version : EPUBVersion.Unknown;
  }

  public boolean isObfuscated(URL resource)
  {
    return obfuscated.containsKey(resource);
  }

  public void setVersion(EPUBVersion validationVersion)
  {
    context = context.version(validationVersion);
  }

  public void setProfile(EPUBProfile checkPublicationProfile)
  {
    context = context.profile(checkPublicationProfile);
  }

  public void addProperty(Property property)
  {
    context = context.addProperty(property);

  }

  public boolean isDeclared(URL resource)
  {
    return declaredResources.contains(resource);
  }

}

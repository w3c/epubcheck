package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.epubcheck.util.url.URLUtils;

import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.mola.galimatias.URL;

public class OCFZipResources implements OCFResources
{
  private final ZipFile zip;

  public OCFZipResources(URL url) throws IOException
  {
    File file = URLUtils.toFile(url);
    this.zip = new ZipFile(file, StandardCharsets.UTF_8);
  }

  @Override
  public Iterator<OCFResource> iterator()
  {
    return new Iterator<OCFResource>()
    {
      private final Enumeration<? extends ZipEntry> entries = zip.entries();

      @Override
      public boolean hasNext()
      {
        return entries.hasMoreElements();
      }

      @Override
      public OCFResource next()
        throws NoSuchElementException
      {
        final ZipEntry entry = entries.nextElement();

        final Map<FeatureEnum, String> properties = ImmutableMap.<FeatureEnum, String> builder()
            .put(FeatureEnum.SIZE, String.valueOf(entry.getSize()))
            .put(FeatureEnum.COMPRESSED_SIZE, String.valueOf(entry.getCompressedSize()))
            .put(FeatureEnum.COMPRESSION_METHOD, getCompressionMethod(entry))
            .put(FeatureEnum.SHA_256, getSHAHash(entry, zip))
            .put(FeatureEnum.CREATION_DATE,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(entry.getTime())))
            .build();
        
        return new OCFResource()
        {
          @Override
          public InputStream openStream()
            throws IOException
          {
            return zip.getInputStream(entry);
          }

          @Override
          public boolean isFile()
          {
            return !entry.isDirectory();
          }

          @Override
          public boolean isDirectory()
          {
            return entry.isDirectory();
          }

          @Override
          public Map<FeatureEnum, String> getProperties()
          {
            return properties;
          }

          @Override
          public String getPath()
          {
            return entry.getName();
          }

          @Override
          public String toString()
          {
            return getPath();
          }
        };
      }
    };
  }

  public void close()
    throws IOException
  {
    zip.close();
  }

  private static String getSHAHash(ZipEntry entry, ZipFile zip)
  {
    try (InputStream inputStream = zip.getInputStream(entry))
    {
      return OCFResources.getSHAHash(inputStream);
    } catch (IOException e)
    {
      return "";
    }
  }

  private static String getCompressionMethod(ZipEntry entry)
  {
    Preconditions.checkNotNull(entry);
    switch (entry.getMethod())
    {
    case ZipEntry.DEFLATED:
      return "Deflated";
    case ZipEntry.STORED:
      return "Stored";
    default:
      return "Unsupported";
    }
  }

}

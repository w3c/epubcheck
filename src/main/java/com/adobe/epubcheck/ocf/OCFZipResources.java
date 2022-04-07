package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.mola.galimatias.URL;

public class OCFZipResources implements Iterable<OCFResource>
{
  private final ZipFile zip;

  public OCFZipResources(URL url) throws IOException
  {
    File file = null;
    try
    {
      file = new File(url.toJavaURI());
    } catch (URISyntaxException e)
    {
      new IllegalArgumentException("Not a file URL: " + url);
    }
    this.zip = new ZipFile(file);
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

  private static String getSHAHash(ZipEntry entry, ZipFile zip)
  {
    try (InputStream inputStream = zip.getInputStream(entry))
    {

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dataBytes = new byte[1024];

      int nread;
      while ((nread = inputStream.read(dataBytes)) != -1)
      {
        md.update(dataBytes, 0, nread);
      }
      byte[] bytes = md.digest();

      // convert the byte to hex format method 1
      // StringBuilder sb = new StringBuilder();
      // for (int i = 0; i < bytes.length; i++)
      // {
      // sb.append(Integer.toString((bytes[i] & 0xff) + 0x100,
      // 16).substring(1));
      // }

      // convert the byte to hex format method 2
      StringBuilder hexString = new StringBuilder();
      for (byte aByte : bytes)
      {
        hexString.append(Integer.toHexString(0xFF & aByte));
      }

      return hexString.toString();
    } catch (Exception e)
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

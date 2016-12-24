package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.FeatureEnum;

public class OCFZipPackage extends OCFPackage
{

  private final ZipFile zip;
  private List<String> allEntries = null;
  private Set<String> fileEntries;
  private Set<String> dirEntries;

  public OCFZipPackage(ZipFile zip)
  {
    super();
    this.zip = zip;
  }

  private void listEntries() throws
      IOException
  {
    synchronized (zip)
    {
      allEntries = new LinkedList<String>();
      fileEntries = new TreeSet<String>();
      dirEntries = new TreeSet<String>();

      try
      {
        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); )
        {
          ZipEntry entry = entries.nextElement();
          allEntries.add(entry.getName());
          if (entry.isDirectory())
          {
            dirEntries.add(entry.getName());
          }
          else
          {
            fileEntries.add(entry.getName());
          }
        }
      }
      catch (IllegalArgumentException ex)
      {
        throw new IOException(ex.getMessage());
      }
    }
  }

  /* (non-Javadoc)
  * @see com.adobe.epubcheck.ocf.OCFPackage#hasEntry(java.lang.String)
  */
  public boolean hasEntry(String name)
  {
    return zip.getEntry(name) != null;
  }

  /* (non-Javadoc)
    * @see com.adobe.epubcheck.ocf.OCFPackage#getTimeEntry(java.lang.String)
    */
  public long getTimeEntry(String name)
  {
    ZipEntry entry = zip.getEntry(name);
    if (entry == null)
    {
      return 0L;
    }
    return entry.getTime();
  }

  /*
    * (non-Javadoc)
    * @see com.adobe.epubcheck.ocf.OCFPackage#getInputStream(java.lang.String)
    */
  @Override
  public InputStream getInputStream(String name) throws
      IOException
  {
    ZipEntry entry = zip.getEntry(name);
    if (entry == null)
    {
      return null;
    }
    InputStream in = zip.getInputStream(entry);
    EncryptionFilter filter = enc.get(name);
    if (filter == null)
    {
      return in;
    }
    if (filter.canDecrypt())
    {
      return filter.decrypt(in);
    }
    return null;
  }

  @Override
  public List<String> getEntries() throws
      IOException
  {
    synchronized (zip)
    {
      if (allEntries == null)
      {
        listEntries();
      }
    }
    return Collections.unmodifiableList(allEntries);
  }

  /* (non-Javadoc)
  * @see com.adobe.epubcheck.ocf.OCFPackage#getFileEntries()
  */
  @Override
  public Set<String> getFileEntries() throws
      IOException
  {
    synchronized (zip)
    {
      if (allEntries == null)
      {
        listEntries();
      }
      return Collections.unmodifiableSet(fileEntries);
    }
  }

  /* (non-Javadoc)
  * @see com.adobe.epubcheck.ocf.OCFPackage#getDirectoryEntries()
  */
  @Override
  public Set<String> getDirectoryEntries() throws
      IOException
  {
    synchronized (zip)
    {
      if (allEntries == null)
      {
        listEntries();
      }
      return Collections.unmodifiableSet(dirEntries);
    }
  }

  public void reportMetadata(String fileName, Report report)
  {
    ZipEntry entry = zip.getEntry(fileName);
    if (entry != null)
    {
      report.info(fileName, FeatureEnum.SIZE, String.valueOf(entry.getSize()));
      report.info(fileName, FeatureEnum.COMPRESSED_SIZE, String.valueOf(entry.getCompressedSize()));
      report.info(fileName, FeatureEnum.COMPRESSION_METHOD, this.getCompressionMethod(entry));
      InputStream inputStream = null;
      try
      {
        inputStream = zip.getInputStream(entry);
        if (inputStream != null)
        {
          report.info(fileName, FeatureEnum.SHA_256, getSHAHash(inputStream));
        }
      }
      catch (IOException e)
      {
        report.message(MessageId.PKG_008, EPUBLocation.create(fileName), fileName);
      }
      finally
      {
        if (inputStream != null)
        {
          try
          {
            inputStream.close();
          }
          catch (Exception ignore)
          {
          }
        }
      }
    }
  }

  private String getCompressionMethod(ZipEntry entry)
  {
    if (entry == null)
    {
      return "";
    }
    int method = entry.getMethod();
    if (method == ZipEntry.DEFLATED)
    {
      return "Deflated";
    }
    if (method == ZipEntry.STORED)
    {
      return "Stored";
    }
    return "Unsupported";
  }

  private static String getSHAHash(InputStream fis)
  {
    try
    {

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dataBytes = new byte[1024];

      int nread;
      while ((nread = fis.read(dataBytes)) != -1)
      {
        md.update(dataBytes, 0, nread);
      }
      byte[] bytes = md.digest();

      //convert the byte to hex format method 1
      //StringBuilder sb = new StringBuilder();
      //for (int i = 0; i < bytes.length; i++)
      //{
      //  sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
      //}

      //convert the byte to hex format method 2
      StringBuilder hexString = new StringBuilder();
      for (byte aByte : bytes)
      {
        hexString.append(Integer.toHexString(0xFF & aByte));
      }

      return hexString.toString();
    }
    catch (Exception e)
    {
      return "error!";
    }
    finally
    {
      if (fis != null)
      {
        try
        {
          fis.close();
        }
        catch (IOException ignored)
        {
        }
      }
    }
  }

  public String getName()
  {
    return new File(this.zip.getName()).getName();
  }

  @Override
  public String getPackagePath()
  {
    return zip.getName();
  }
}

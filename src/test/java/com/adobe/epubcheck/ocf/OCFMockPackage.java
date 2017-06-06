package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.adobe.epubcheck.api.Report;

public class OCFMockPackage extends OCFPackage
{
  HashSet<String> dirEntries, mockEntries;
  File containerFile;
  int offset;

  public OCFMockPackage(String containerPath)
  {
    try {
      URL containerURL = this.getClass().getResource(containerPath);
      String containerURI = containerURL != null ? new File(containerURL.toURI()).getAbsolutePath() : null;
      containerFile = new File(containerURI != null ? containerURI : containerPath);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Cannot find test file", e);
    }
    offset = containerFile.getPath().length() + 1;
    dirEntries = new HashSet<String>();
    mockEntries = new HashSet<String>();
    addEntries(containerFile);
  }


  private void addEntries(File parent)
  {
    File files[] = parent.listFiles();
    if (null != files)
    {
      for (File file : files)
      {
        if (file.isFile())
        {
          mockEntries.add(file.getPath().substring(offset).replace('\\', '/'));
        }
        else if (file.isDirectory() && !file.getName().startsWith("."))
        {
          dirEntries.add(file.getPath().substring(offset).replace('\\', '/'));
          addEntries(file);
        }
      }
    }
  }

  @Override
  public boolean hasEntry(String name)
  {
    File entry = new File(containerFile, name);
    return entry.exists();
  }

  @Override
  public long getTimeEntry(String name)
  {
    return 1348240407L;
  }

  @Override
  public InputStream getInputStream(String name) throws IOException
  {
    if (hasEntry(name))
    {
      return new FileInputStream(new File(containerFile, name));
    }
    else
    {
      throw new IOException("An unknown file, " + name + " was requested");
    }
  }

  @Override
  public Set<String> getFileEntries() throws IOException
  {
    return mockEntries;
  }

  @Override
  public Set<String> getDirectoryEntries() throws IOException
  {
    return dirEntries;
  }


  @Override
  public List<String> getEntries() throws IOException {
    List<String> result = new LinkedList<String>();
    result.addAll(mockEntries);
    result.addAll(dirEntries);
    return result;
  }
    

  public void reportMetadata(String fileName, Report report)
  {
  }

  public String getName()
  {
    return this.containerFile.getName();
  }

  @Override
  public String getPackagePath()
  {
    return containerFile.getPath();
  }
}

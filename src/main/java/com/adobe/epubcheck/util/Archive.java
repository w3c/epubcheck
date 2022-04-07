package com.adobe.epubcheck.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class Archive
{
  private ArrayList<String> paths;

  private ArrayList<String> names;

  private final File baseDir;

  private File epubFile;

  private String epubName;

  public Archive(String base, boolean save)
  {
    boolean deleteOnExit = !save;
    baseDir = makeCanonical(new File(base));

    if (!baseDir.exists() || !baseDir.isDirectory())
    {
      throw new RuntimeException("The path specified for the archive is invalid");
    }
    epubName = baseDir.getName() + ".epub";
    epubFile = new File(baseDir.getParent() + File.separator + epubName);
    if (deleteOnExit)
    {
      epubFile.deleteOnExit();
    }

    paths = new ArrayList<String>();
    names = new ArrayList<String>();
  }

  public Archive(String base)
  {
    this(base, false);
  }

  public String getEpubName()
  {
    return epubName;
  }

  public File getEpubFile()
  {
    return epubFile;
  }

  public void deleteEpubFile()
  {
    if (!epubFile.delete())
    {
      // TODO replace system.err
      System.err.println("Unable to delete generated archive.");
    }
  }

  public void createArchive(File absoluteEpubFilePath)
  {
    this.epubFile = absoluteEpubFilePath;
    createArchive();
  }

  public void createArchive()
  {
    // using commons compress to allow setting filename encoding pre java7
    ZipArchiveOutputStream out = null;
    try
    {
      collectFiles(baseDir, "");

      // make mimetype the first entry
      int mimetype = names.indexOf("mimetype");
      if (mimetype > -1)
      {
        String name = names.remove(mimetype);
        String path = paths.remove(mimetype);
        names.add(0, name);
        paths.add(0, path);
      }
      else
      {
        // TODO replace system.err
        System.err.println(
            "No mimetype file found in expanded publication, output archive will be invalid");
      }

      out = new ZipArchiveOutputStream(epubFile);
      out.setEncoding("UTF-8");

      for (int i = 0; i < paths.size(); i++)
      {
        ZipArchiveEntry entry = new ZipArchiveEntry(new File(paths.get(i)), names.get(i));
        if (i == 0 && mimetype > -1)
        {
          entry.setMethod(ZipArchiveEntry.STORED);
          entry.setSize(getSize(paths.get(i)));
          entry.setCrc(getCRC(paths.get(i)));
        }
        else
        {
          entry.setMethod(ZipArchiveEntry.DEFLATED);
        }
        out.putArchiveEntry(entry);
        FileInputStream in = null;
        try
        {
          in = new FileInputStream(paths.get(i));

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0)
          {
            out.write(buf, 0, len);
          }
          out.closeArchiveEntry();
        } finally
        {
          if (in != null)
          {
            in.close();
          }
        }
      }
    } catch (Exception e)
    {
      throw new RuntimeException(e.getMessage());
    } finally
    {
      try
      {
        if (out != null)
        {
          out.flush();
          out.finish();
          out.close();
        }
      } catch (IOException ignored)
      {
      }
    }
  }

  private File makeCanonical(File f)
  {
    if (f == null)
    {
      return null;
    }
    try
    {
      return f.getCanonicalFile();
    } catch (IOException ignored)
    {
      return f.getAbsoluteFile();
    }
  }

  private long getSize(String path)
    throws IOException
  {
    FileInputStream in = null;
    try
    {
      in = new FileInputStream(path);
      byte[] buf = new byte[1024];
      int len;
      int size = 0;
      while ((len = in.read(buf)) > 0)
      {
        size += len;
      }
      return size;
    } finally
    {
      if (in != null)
      {
        in.close();
      }
    }
  }

  private long getCRC(String path)
    throws IOException
  {
    CheckedInputStream cis = null;
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(path);
      cis = new CheckedInputStream(fis, new CRC32());
      byte[] buf = new byte[128];
      while (cis.read(buf) >= 0)
      {
        // TODO: why is this loop empty?
      }
    } finally
    {
      if (fis != null)
      {
        fis.close();
      }
      if (cis != null)
      {
        cis.close();
      }
    }
    return cis.getChecksum().getValue();
  }

  // public void createArchiveOld() {
  // collectFiles(baseDir, "");
  // byte[] buf = new byte[1024];
  // try {
  //
  // ZipOutputStream out = new ZipOutputStream((new FileOutputStream(
  // epubName)));
  //
  // int index = names.indexOf("mimetype");
  // if (index >= 0) {
  // FileInputStream in = new FileInputStream(paths.get(index));
  //
  // ZipEntry entry = new ZipEntry(names.get(index));
  // entry.setMethod(ZipEntry.STORED);
  // int len, size = 0;
  // while ((len = in.read(buf)) > 0)
  // size += len;
  //
  // in = new FileInputStream(paths.get(index));
  //
  // entry.setCompressedSize(size);
  // entry.setSize(size);
  //
  // CRC32 crc = new CRC32();
  // entry.setCrc(crc.getValue());
  // out.putNextEntry(entry);
  //
  // while ((len = in.read(buf)) > 0) {
  // crc.update(buf, 0, len);
  // out.write(buf, 0, len);
  // }
  //
  // entry.setCrc(crc.getValue());
  //
  // paths.remove(index);
  // names.remove(index);
  // }
  //
  // for (int i = 0; i < paths.size(); i++) {
  // FileInputStream in = new FileInputStream(paths.get(i));
  //
  // out.putNextEntry(new ZipEntry(names.get(i)));
  //
  // int len;
  // while ((len = in.read(buf)) > 0) {
  // out.write(buf, 0, len);
  // }
  //
  // out.closeEntry();
  // in.close();
  // }
  //
  // out.close();
  // } catch (IOException e) {
  // }
  // }

  private void collectFiles(File dir, String dirName)
  {
    if (!dirName.equals("") && !dirName.endsWith("/"))
    {
      dirName = dirName + "/";
    }

    File files[] = dir.listFiles();
    assert files != null;

    for (int i = 0; i < files.length; i++)
    {
      // issue 256: ignore '.DS_Store', '._DS_Store', 'Thumbs.db' and
      // 'ehthumbs.db' files
      if (files[i].isFile() && !files[i].getName().equals(".DS_Store")
          && !files[i].getName().equals("._DS_Store") && !files[i].getName().equals("Thumbs.db")
          && !files[i].getName().equals("ehthumbs.db"))
      {
        names.add(dirName + files[i].getName());
        paths.add(files[i].getAbsolutePath());

        // issue 256: ignore .git/ and .svn/ folders

      }
      else if (files[i].isDirectory() && !files[i].getName().equals(".svn")
          && !files[i].getName().equals(".git"))
      {
        collectFiles(files[i], dirName + files[i].getName() + "/");
      }
    }

  }

  public void listFiles()
  {
    for (String name : names)
    {
      outWriter.println(name);
    }
  }
}

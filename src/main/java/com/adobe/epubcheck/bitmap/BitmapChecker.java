/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.bitmap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.ocf.OCFPackage;
import com.adobe.epubcheck.ocf.OCFZipPackage;
import com.adobe.epubcheck.opf.ContentChecker;
import com.adobe.epubcheck.util.CheckUtil;

public class BitmapChecker implements ContentChecker
{
  private final OCFPackage ocf;
  private final Report report;
  private final String path;
  private final String mimeType;
  private static final int HEIGHT_MAX = 2 * 1080;
  private static final int WIDTH_MAX = 2 * 1920;
  private static final long IMAGESIZE_MAX = 4 * 1024 * 1024;

  BitmapChecker(OCFPackage ocf, Report report, String path, String mimeType)
  {
    this.ocf = ocf;
    this.report = report;
    this.path = path;
    this.mimeType = mimeType;
  }

  private void checkHeader(byte[] header)
  {
    boolean passed;
    if (mimeType.equals("image/jpeg"))
    {
      passed = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
    }
    else if (mimeType.equals("image/gif"))
    {
      passed = header[0] == (byte) 'G' && header[1] == (byte) 'I'
          && header[2] == (byte) 'F' && header[3] == (byte) '8';
    }
    else
    {
      passed = !mimeType.equals("image/png") || header[0] == (byte) 0x89 && header[1] == (byte) 'P' && header[2] == (byte) 'N' && header[3] == (byte) 'G';
    }
    if (!passed)
    {
      report.message(MessageId.OPF_029, EPUBLocation.create(this.ocf.getName()), path, mimeType);
    }
  }


  /**
   * Gets image dimensions for given file
   *
   * @param imgFileName image file
   * @return dimensions of image
   * @throws IOException if the file is not a known image
   */
  public ImageHeuristics getImageSizes(String imgFileName) throws
      IOException
  {
    int pos = imgFileName.lastIndexOf(".");
    if (pos == -1)
    {
      throw new IOException("No extension for file: " + imgFileName);
    }

    String suffix = imgFileName.substring(pos + 1);
    File tempFile = null;
    if ("svg".compareToIgnoreCase(suffix) == 0)
    {
      tempFile = getImageFile(ocf, imgFileName);
      if (tempFile != null)
      {
          long tempFileLength = tempFile.length();
          if (ocf.getClass() == OCFZipPackage.class)
          {
              tempFile.delete();
          }
        return new ImageHeuristics(0, 0, tempFileLength);
      }
      return null;
    }
    
    // Determine format by file extension and by inspecting the file
    tempFile = getImageFile(ocf, imgFileName);
    String formatFromInputStream = null;
    String formatFromSuffix = null;
    ImageInputStream imageInputStream = ImageIO.createImageInputStream(tempFile);
    Iterator<ImageReader> imageReaderIteratorFromInputStream = ImageIO.getImageReaders(imageInputStream);
    while (imageReaderIteratorFromInputStream.hasNext()) {
      ImageReader imageReaderFromInputStream = imageReaderIteratorFromInputStream.next();
      formatFromInputStream = imageReaderFromInputStream.getFormatName();
        
      Iterator<ImageReader> imageReaderIteratorFromSuffix = ImageIO.getImageReadersBySuffix(suffix);
        while (imageReaderIteratorFromSuffix.hasNext()) {
          ImageReader reader = imageReaderIteratorFromSuffix.next();
          formatFromSuffix = reader.getFormatName();
          
          if (formatFromSuffix != null && formatFromSuffix.equals(formatFromInputStream)) break;
        }
        if (formatFromSuffix != null && formatFromSuffix.equals(formatFromInputStream)) break;
    }
    imageInputStream.close();



      if (formatFromSuffix != null && formatFromSuffix.equals(formatFromInputStream)) {
      // file format and file extension matches; read image file
      
      try {
        BufferedImage image = ImageIO.read(tempFile);
        if (image == null) {
          report.message(MessageId.PKG_021, EPUBLocation.create(imgFileName));
          return null;
          
        } else {
          int width          = image.getWidth();
          int height         = image.getHeight();
          return new ImageHeuristics(width, height, tempFile.length());
        }
      }
      catch (IOException e)
      {
        report.message(MessageId.PKG_021, EPUBLocation.create(imgFileName));
        return null;
      }
      catch (IllegalArgumentException argex)
      {
        report.message(MessageId.PKG_021, EPUBLocation.create(imgFileName));
        return null;
      }
      finally
      {
          if (ocf.getClass() == OCFZipPackage.class)
          {
              tempFile.delete();
          }
      }
  
    } else
      {
          if (ocf.getClass() == OCFZipPackage.class)
          {
              tempFile.delete();
          }
          if (formatFromSuffix != null) {
              // file format and file extension differs

              report.message(MessageId.PKG_022, EPUBLocation.create(imgFileName), formatFromInputStream, suffix);
              return null;

          } else {
              // file format could not be determined

              throw new IOException("Not a known image file: " + imgFileName);
          }
      }
  }

  private File getImageFile(OCFPackage ocf, String imgFileName) throws IOException
  {
    if (ocf.getClass() == OCFZipPackage.class)
    {
      return getTempImageFile((OCFZipPackage) ocf, imgFileName);
    }
    else
    {
      return new File(ocf.getPackagePath() + File.separator + imgFileName);
    }
  }

  public class ImageHeuristics
  {
    public int width;
    public int height;
    public long length;

    public ImageHeuristics(int width, int height, long length)
    {
      this.width = width;
      this.height = height;
      this.length = length;
    }
  }

  private File getTempImageFile(OCFZipPackage ocf, String imgFileName) throws IOException
  {
    File file = null;
    FileOutputStream os = null;
    InputStream is = null;
    try
    {
      int pos = imgFileName.lastIndexOf(".");
      if (pos == -1)
      {
        throw new IOException("No extension for file: " + imgFileName);
      }
      String suffix = imgFileName.substring(pos);
      String prefix = "img";

      file = File.createTempFile(prefix, suffix);
      os = new FileOutputStream(file);

      is = ocf.getInputStream(imgFileName);
      if (is == null)
      {
        return null;
      }
      byte[] bytes = new byte[32 * 1024];
      int read;
      while ((read = is.read(bytes)) > 0)
      {
        os.write(bytes, 0, read);
      }
    }
    finally
    {
      if (os != null)
      {
        os.flush();
        os.close();
      }
      if (is != null)
      {
        is.close();
      }
    }
    return file;  //To change body of created methods use File | Settings | File Templates.
  }

  private void checkImageDimensions(String imageFileName)
  {
    try
    {
      ImageHeuristics h = getImageSizes(imageFileName);
      if (h != null)
      {
        if (h.height >= HEIGHT_MAX || h.width >= WIDTH_MAX)
        {
          report.message(MessageId.OPF_051, EPUBLocation.create(imageFileName));
        }
        if (h.length >= IMAGESIZE_MAX)
        {
          report.message(MessageId.OPF_057, EPUBLocation.create(imageFileName));
        }
      }
    }
    catch (IOException ex)
    {
      report.message(MessageId.PKG_021, EPUBLocation.create(imageFileName) );
    } catch (LinkageError error)
    {
      report.message(MessageId.RSC_022, EPUBLocation.create(imageFileName));
    }
  }

  public void runChecks()
  {
    if (!ocf.hasEntry(path))
    {
      report.message(MessageId.RSC_001, EPUBLocation.create(this.ocf.getName()), path);
    }
    else if (!ocf.canDecrypt(path))
    {
      report.message(MessageId.RSC_004, EPUBLocation.create(this.ocf.getName()), path);
    }
    else
    {
      InputStream in = null;
      try
      {
        in = ocf.getInputStream(path);
        if (in == null)
        {
          report.message(MessageId.RSC_001, EPUBLocation.create(this.ocf.getName()), path);
        }
        byte[] header = new byte[4];
        int rd = CheckUtil.readBytes(in, header, 0, 4);
        if (rd < 4)
        {
          report.message(MessageId.MED_004, EPUBLocation.create(path));
        }
        else
        {
          checkHeader(header);
        }
        checkImageDimensions(path);
      }
      catch (IOException e)
      {
        report.message(MessageId.PKG_021, EPUBLocation.create(path, path));
      }
      finally
      {
        try
        {
          if (in != null)
          {
            in.close();
          }
        }
        catch (IOException ignored)
        {
          // eat it
        }
      }
    }
  }
}

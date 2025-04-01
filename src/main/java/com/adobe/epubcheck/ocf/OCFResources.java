package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public interface OCFResources extends Iterable<OCFResource>
{
  public void close()
    throws IOException;

  static String getSHAHash(InputStream inputStream)
  {
    try
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
}

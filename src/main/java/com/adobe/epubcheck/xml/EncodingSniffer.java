package com.adobe.epubcheck.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class EncodingSniffer
{

  private static final byte[][] UTF16_MAGIC = { { (byte) 0xFE, (byte) 0xFF },
      { (byte) 0xFF, (byte) 0xFE }, { 0, 0x3C, 0, 0x3F }, { 0x3C, 0, 0x3F, 0 } };

  private static final byte[][] UCS4_MAGIC = { { 0, 0, (byte) 0xFE, (byte) 0xFF },
      { (byte) 0xFF, (byte) 0xFE, 0, 0 }, { 0, 0, (byte) 0xFF, (byte) 0xFE },
      { (byte) 0xFE, (byte) 0xFF, 0, 0 }, { 0, 0, 0, 0x3C }, { 0, 0, 0x3C, 0 }, { 0, 0x3C, 0, 0 },
      { 0x3C, 0, 0, 0 } };

  private static final byte[] UTF8_MAGIC = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

  private static final byte[] EBCDIC_MAGIC = { 0x4C, 0x6F, (byte) 0xA7, (byte) 0x94 };

  private static boolean matchesMagic(byte[] magic, byte[] buffer)
  {
    for (int i = 0; i < magic.length; i++)
    {
      if (buffer[i] != magic[i])
      {
        return false;
      }
    }
    return true;
  }

  public static String sniffEncoding(InputStream in)
    throws IOException
  {
    // see http://www.w3.org/TR/REC-xml/#sec-guessing
    byte[] buffer = new byte[256];
    in.mark(buffer.length);
    int len = in.read(buffer);
    in.reset();
    if (len < 4)
    {
      return null;
    }
    for (byte[] magic : UTF16_MAGIC)
    {
      if (matchesMagic(magic, buffer))
      {
        return "UTF-16";
      }
    }
    for (byte[] anUcs4magic : UCS4_MAGIC)
    {
      if (matchesMagic(anUcs4magic, buffer))
      {
        return "UCS-4";
      }
    }
    if (matchesMagic(UTF8_MAGIC, buffer))
    {
      return "UTF-8";
    }
    if (matchesMagic(EBCDIC_MAGIC, buffer))
    {
      return "EBCDIC";
    }

    // some ASCII-compatible encoding; read ASCII
    int asciiLen = 0;
    while (asciiLen < len)
    {
      int c = buffer[asciiLen] & 0xFF;
      if (c == 0 || c > 0x7F)
      {
        break;
      }
      asciiLen++;
    }

    // read it into a String
    String header = new String(buffer, 0, asciiLen, "ASCII");
    int encIndex = header.indexOf("encoding=");
    if (encIndex < 0)
    {
      return null; // probably UTF-8
    }

    encIndex += 9;
    if (encIndex >= header.length())
    {
      return null; // encoding did not fit!
    }

    char quote = header.charAt(encIndex);
    if (quote != '"' && quote != '\'')
    {
      return null; // confused...
    }

    int encEnd = header.indexOf(quote, encIndex + 1);
    if (encEnd < 0)
    {
      return null; // encoding did not fit!
    }

    String encoding = header.substring(encIndex + 1, encEnd);
    return encoding.toUpperCase(Locale.ROOT);
  }

  private EncodingSniffer()
  {
    // Not instanciable.
  }
}

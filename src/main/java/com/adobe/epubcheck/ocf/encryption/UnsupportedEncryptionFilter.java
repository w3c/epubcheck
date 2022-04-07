package com.adobe.epubcheck.ocf.encryption;

import java.io.InputStream;

public class UnsupportedEncryptionFilter implements EncryptionFilter
{
  public boolean canDecrypt()
  {
    return false;
  }

  public InputStream decrypt(InputStream in)
  {
    return null;
  }
}

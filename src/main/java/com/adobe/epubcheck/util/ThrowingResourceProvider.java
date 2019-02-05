package com.adobe.epubcheck.util;

import java.io.IOException;
import java.io.InputStream;

public final class ThrowingResourceProvider implements GenericResourceProvider
{
  
  @Override
  public InputStream getInputStream(String path)
    throws IOException
  {
    throw new IOException();
  }

}

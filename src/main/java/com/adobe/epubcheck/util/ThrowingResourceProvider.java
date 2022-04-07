package com.adobe.epubcheck.util;

import java.io.IOException;
import java.io.InputStream;

import io.mola.galimatias.URL;

public final class ThrowingResourceProvider implements GenericResourceProvider
{
  
  @Override
  public InputStream openStream(URL url)
    throws IOException
  {
    throw new IOException();
  }

}

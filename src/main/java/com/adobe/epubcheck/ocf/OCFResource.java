package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.adobe.epubcheck.util.FeatureEnum;

public interface OCFResource
{
  boolean isDirectory();

  boolean isFile();

  String getPath();

  Map<FeatureEnum, String> getProperties();
  
  InputStream openStream() throws IOException;
}

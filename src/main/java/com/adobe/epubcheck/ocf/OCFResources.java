package com.adobe.epubcheck.ocf;

import java.io.IOException;

public interface OCFResources extends Iterable<OCFResource>
{
  public void close()
    throws IOException;
}

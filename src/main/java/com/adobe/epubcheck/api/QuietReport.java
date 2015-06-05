package com.adobe.epubcheck.api;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.util.FeatureEnum;

public final class QuietReport extends MasterReport
{

  public static final Report INSTANCE = new QuietReport();

  private QuietReport()
  {
    super();
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {

  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
  }

  @Override
  public int generate()
  {
    return 0;
  }

  @Override
  public void initialize()
  {
  }

}

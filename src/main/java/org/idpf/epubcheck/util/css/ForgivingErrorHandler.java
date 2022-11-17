package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;

public final class ForgivingErrorHandler implements CssErrorHandler
{

  public static final ForgivingErrorHandler INSTANCE = new ForgivingErrorHandler();

  @Override
  public void error(CssException e)
    throws CssException
  {
    // do nothing
  }

}

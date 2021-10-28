package org.w3c.epubcheck.core;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.ValidationContext;
import com.google.common.base.Preconditions;

//FIXME 2021 Romain - document
public abstract class AbstractChecker implements Checker
{

  protected final ValidationContext context;
  protected final Report report;

  public AbstractChecker(ValidationContext context)
  {
    this.context = Preconditions.checkNotNull(context);
    this.report = context.report;
  }
}

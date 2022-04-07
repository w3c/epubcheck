package com.adobe.epubcheck.ocf;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ResourceUtil;

public class OCFExtensionChecker implements Checker
{
  private final ValidationContext context;
  private final Report report;
  private final String extension;

  public OCFExtensionChecker(ValidationContext context)
  {
    // FIXME 2022 - check preconditions
    this.context = context;
    this.report = context.report;
    this.extension = ResourceUtil.getExtension(context.path);
  }

  @Override
  public void check()
  {

    if (extension != null)
    {
      if (!extension.equals("epub"))
      {
        if (extension.matches("[Ee][Pp][Uu][Bb]"))
        {
          report.message(MessageId.PKG_016, EPUBLocation.of(context));
        }
        else
        {
          if (context.version == EPUBVersion.VERSION_3)
          {
            report.message(MessageId.PKG_024, EPUBLocation.of(context).context(extension));
          }
          else
          {
            report.message(MessageId.PKG_017, EPUBLocation.of(context).context(extension));
          }
        }
      }
    }
  }

}

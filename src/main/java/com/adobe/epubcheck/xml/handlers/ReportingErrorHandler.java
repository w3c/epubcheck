package com.adobe.epubcheck.xml.handlers;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;

public class ReportingErrorHandler implements ErrorHandler
{

  private final ValidationContext context;
  private final Report report;
  private final boolean normative;

  public ReportingErrorHandler(ValidationContext context)
  {
    this(context, true);
  }

  public ReportingErrorHandler(ValidationContext context, boolean normative)
  {
    this.context = context;
    this.report = context.report;
    this.normative = normative;
  }

  public void warning(SAXParseException ex)
    throws SAXException
  {
    report.message(normative ? MessageId.RSC_017 : MessageId.RSC_024,
        EPUBLocation.of(context).at(ex.getLineNumber(), ex.getColumnNumber()), ex.getMessage());
  }

  public void error(SAXParseException ex)
    throws SAXException
  {
    String message = ex.getMessage().trim();
    if (message != null && message.startsWith("WARNING:"))
    {
      report.message(normative ? MessageId.RSC_017 : MessageId.RSC_024,
          EPUBLocation.of(context).at(ex.getLineNumber(), ex.getColumnNumber()),
          message.substring(9, message.length()));
    }
    else
    {
      report.message(normative ? MessageId.RSC_005 : MessageId.RSC_025,
          EPUBLocation.of(context).at(ex.getLineNumber(), ex.getColumnNumber()), message);
    }
  }

  public void fatalError(SAXParseException ex)
    throws SAXException
  {
    report.message(MessageId.RSC_016,
        EPUBLocation.of(context).at(ex.getLineNumber(), ex.getColumnNumber()), ex.getMessage());
  }

}

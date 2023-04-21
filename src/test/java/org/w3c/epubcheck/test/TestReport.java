package org.w3c.epubcheck.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.api.QuietReport;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.reporting.CheckingReport;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.XmlReportImpl;
import com.adobe.epubcheck.util.XmpReportImpl;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

public class TestReport extends MasterReport
{

  private static Map<MessageId, Predicate<MessageInfo>> ID_FILTER_PREDICATES = Maps.toMap(
      EnumSet.<MessageId> allOf(MessageId.class), new Function<MessageId, Predicate<MessageInfo>>()
      {

        @Override
        public Predicate<MessageInfo> apply(final MessageId id)
        {
          return new Predicate<MessageInfo>()
          {
            @Override
            public boolean apply(MessageInfo message)
            {
              return message.getId() == id;
            }
          };
        }
      });
  private static Map<Severity, Predicate<MessageInfo>> SEVERITY_FILTER_PREDICATES = Maps.toMap(
      EnumSet.<Severity> allOf(Severity.class), new Function<Severity, Predicate<MessageInfo>>()
      {

        @Override
        public Predicate<MessageInfo> apply(final Severity severity)
        {
          return new Predicate<MessageInfo>()
          {
            @Override
            public boolean apply(MessageInfo message)
            {
              return message.getSeverity() == severity;
            }
          };
        }
      });

  /* Whether to output messages on System.out */
  private boolean verbose = false;
  /* Stores the messages to be queried by assertions */
  private List<MessageInfo> messages = new LinkedList<MessageInfo>();
  /* The output format (can be JSON/XML/XMP/text */
  private String format = "default";
  /* A delegate report, used mostly for output formatting */
  private Report delegate = null;
  /* a writer storing the report output */
  private final StringWriter output = new StringWriter();

  public TestReport()
  {
    this.setLocale(Locale.ENGLISH);
  }

  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {
    MessageInfo messageInfo = new MessageInfo(message.getSeverity(), message.getID(),
        location.getPath(), location.getLine(), location.getColumn(),
        fixMessage(message.getMessage(args)));
    if (verbose) System.out.println(messageInfo);
    messages.add(messageInfo);
    // delegate to the formatting report
    if (delegate != null)
    {
      delegate.message(message, location, args);
    }
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    MessageInfo messageInfo = new MessageInfo(Severity.INFO, null, resource, 0, 0,
        fixMessage("[" + feature + "] " + value));
    if (verbose) System.out.println(messageInfo);
    messages.add(messageInfo);
    // delegate to the formatting report
    if (delegate != null)
    {
      delegate.info(resource, feature, value);
    }
  }

  @Override
  public int generate()
  {
    int result = 0;
    if (delegate != null)
    {
      result = delegate.generate();
      if (verbose) System.out.println(output);
    }
    return result;
  }

  @Override
  public void initialize()
  {
    assert format != null;
    switch (format)
    {
    case "JSON":
      delegate = new CheckingReport(new PrintWriter(output), getEpubFileName());
      break;
    case "XML":
      delegate = new XmlReportImpl(new PrintWriter(output), getEpubFileName(), "test");
      break;
    case "XMP":
      delegate = new XmpReportImpl(new PrintWriter(output), getEpubFileName(), "test");
      break;
    default:
      delegate = QuietReport.INSTANCE;
      break;
    }
    delegate.initialize();
  }

  public Iterable<MessageInfo> getAll(Severity severity)
  {
    return Iterables.filter(messages, SEVERITY_FILTER_PREDICATES.get(severity));
  }

  public synchronized MessageInfo consume(final MessageId id)
  {
    ListMultimap<Boolean, MessageInfo> partition = Multimaps.index(messages,
        Functions.forPredicate(new Predicate<MessageInfo>()
        {
          private boolean found = false;

          @Override
          public boolean apply(MessageInfo message)
          {
            if (!found && message.getId() == id)
            {
              found = true;
              return true;
            }
            return false;
          }
        }));
    messages = partition.get(false);
    return Iterables.getOnlyElement(partition.get(true), null);
  }

  public synchronized List<MessageInfo> consumeAll(MessageId id)
  {
    ListMultimap<Boolean, MessageInfo> partition = Multimaps.index(messages,
        Functions.forPredicate(ID_FILTER_PREDICATES.get(id)));
    messages = partition.get(false);
    return partition.get(true);
  }

  public synchronized List<MessageInfo> consumeAll(Severity severity)
  {
    ListMultimap<Boolean, MessageInfo> partition = Multimaps.index(messages,
        Functions.forPredicate(SEVERITY_FILTER_PREDICATES.get(severity)));
    messages = partition.get(false);
    return partition.get(true);
  }

  public List<MessageInfo> getAllMessages()
  {
    return ImmutableList.copyOf(messages);
  }

  public String getOutput()
  {
    return output.toString();
  }

  private String fixMessage(String message)
  {
    if (message == null)
    {
      return "No message";
    }
    return message.replaceAll("[\\s]+", " ");
  }

  public void setReportingFormat(String format)
  {
    if (format != null)
    {
      this.format = format;
    }
  }

}

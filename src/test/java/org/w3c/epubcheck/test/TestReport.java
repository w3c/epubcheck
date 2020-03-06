package org.w3c.epubcheck.test;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.PathUtil;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
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

  private boolean verbose = false;
  private List<MessageInfo> messages = new LinkedList<MessageInfo>();

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
        PathUtil.removeWorkingDirectory(location.getPath()), location.getLine(),
        location.getColumn(), fixMessage(message.getMessage(args)));
    if (verbose) System.out.println(messageInfo);
    messages.add(messageInfo);
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    MessageInfo messageInfo = new MessageInfo(Severity.INFO, null, resource, 0, 0,
        fixMessage("[" + feature + "] " + value));
    if (verbose) System.out.println(messageInfo);
    messages.add(messageInfo);
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

  private String fixMessage(String message)
  {
    if (message == null)
    {
      return "No message";
    }
    return message.replaceAll("[\\s]+", " ");
  }

}

package com.adobe.epubcheck.test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.outWriter;

public class TestRunListener extends RunListener
{
  @Override
  public void testRunStarted(Description description)
  {
    File dumpFile = new File("ReportedMessages.txt");
    if (dumpFile.exists())
    {
      dumpFile.delete();
    }
    File allFile = new File("AllMessages.txt");
    if (!allFile.exists())
    {
      try
      {
        FileWriter fileWriter = new FileWriter(allFile);
        for (MessageId id : MessageId.values())
        {
          fileWriter.write(id.toString());
          fileWriter.write("\n");
        }
        fileWriter.close();
      }
      catch (Exception e)
      {
        outWriter.println(e.getMessage());
      }
    }
  }

  @Override
  public void testRunFinished(Result result)
  {
    File dumpFile = new File("ReportedMessages.txt");
    try
    {
      FileWriter fileWriter = new FileWriter(dumpFile);

      MessageId[] messageIds = new MessageId[MasterReport.allReportedMessageIds.size()];
      messageIds = MasterReport.allReportedMessageIds.toArray(messageIds);

      List<MessageId> list = new ArrayList<MessageId>();
      Collections.addAll(list, messageIds);
      Collections.sort(list);
      for (MessageId item : list)
      {
        fileWriter.write(item.toString());
        fileWriter.write("\n");
      }
      fileWriter.close();
    }
    catch (Exception e)
    {
      outWriter.println(e.getMessage());
    }
  }
}

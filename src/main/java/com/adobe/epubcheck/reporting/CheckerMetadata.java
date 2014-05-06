package com.adobe.epubcheck.reporting;

import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.outWriter;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This describes properties about the checker.  It is intended to be serialized into json.
 */
@SuppressWarnings("FieldCanBeLocal")
class CheckerMetadata
{
  @JsonProperty
  private String path;
  @JsonProperty
  private String filename;
  @JsonProperty
  private String checkerVersion;
  @JsonProperty
  private String checkDate;
  @JsonProperty
  private long elapsedTime = -1; // Elapsed Time in Seconds
  @JsonProperty
  private int nFatal = 0;
  @JsonProperty
  private int nError = 0;
  @JsonProperty
  private int nWarning = 0;
  @JsonProperty
  private int nUsage = 0;

  private final String workingDirectory  = System.getProperty("user.dir");

  public void setFileInfo(File epubFile)
  {
    this.path = PathUtil.removeWorkingDirectory(epubFile.getAbsolutePath());
    this.filename = epubFile.getName();
  }

  private Date processStartDateTime;
  private Date processEndDateTime;
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

  public CheckerMetadata()
  {

  }

  public void setCheckerVersion(String value)
  {
    this.checkerVersion = value;
    //this.checkerVersion = "${pom.version}";
  }

  public long getProcessDuration()
  {
    if (elapsedTime == -1)
    {
      setElapsedTime();
    }

    return this.elapsedTime;
  }

  public void setStartDate()
  {
    this.processStartDateTime = new Date();
    this.checkDate = CheckerMetadata.dateFormat.format(this.processStartDateTime);
  }

  public void setStopDate()
  {
    this.processEndDateTime = new Date();
    this.setElapsedTime();
  }

  private void setElapsedTime()
  {
    this.elapsedTime = this.processEndDateTime.getTime()
        - this.processStartDateTime.getTime();
  }

  public void setMessageTypes(List<CheckMessage> messages)
  {
    for (CheckMessage message : messages)
    {
      if (message.getSeverity() != null)
      {
        switch (message.getSeverity())
        {
          case FATAL:
            nFatal++;
            break;
          case ERROR:
            nError++;
            break;
          case WARNING:
            nWarning++;
            break;
          case USAGE:
            nUsage++;
            break;
        }
      }
      else
      {
        outWriter.print("message with no severity");
      }
    }
  }
}

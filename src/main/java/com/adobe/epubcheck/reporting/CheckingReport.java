package com.adobe.epubcheck.reporting;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.MasterReport;
import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.JsonWriter;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.util.outWriter;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class CheckingReport extends MasterReport
{
  @JsonProperty
  final CheckerMetadata checker;

  @JsonProperty
  final PublicationMetadata publication;

  Map<String, ItemMetadata> itemIndex = null;

  @JsonProperty
  List<ItemMetadata> items = null;

  @JsonProperty
  final List<CheckMessage> messages = new ArrayList<CheckMessage>();

  final String outputFile;

  public CheckingReport(String epubPath, String outFile)
  {
    this.checker = new CheckerMetadata();
    this.publication = new PublicationMetadata();
    this.outputFile = outFile;
    this.setEpubFileName(epubPath);
  }

  void setParameters()
  {
    outWriter.println("Epub Name: " + PathUtil.removeWorkingDirectory(this.getEpubFileName()));
    this.checker.setCheckerVersion(EpubCheck.version());
    File f = new File(this.getEpubFileName());
    this.checker.setFileInfo(f);
    this.checker.setMessageTypes(this.messages);
    boolean defaultFixedFormat = this.publication.getRenditionLayout().equals("pre-paginated");
    for (ItemMetadata item : this.getItems())
    {
      String id = item.getId();
      if (id == null || id.equals(""))
      {
        item.setId("ePubCheck.NoManifestRef:" + item.getFileName());
      }

      if (item.getIsSpineItem())
      {
        String layout = item.getRenditionLayout();
        if (layout == null || layout.equals(""))
        {
          item.setRenditionLayout(this.publication.getRenditionLayout());
        }

        String orientation = item.getRenditionOrientation();
        if (orientation == null || orientation.equals(""))
        {
          item.setRenditionOrientation(this.publication.getRenditionOrientation());
        }

        String spread = item.getRenditionSpread();
        if (spread == null || spread.equals(""))
        {
          item.setRenditionSpread(this.publication.getRenditionSpread());
        }

        Boolean isFixed = item.getIsFixedFormat();
        if (isFixed == null)
        {
          item.setIsFixedFormat(defaultFixedFormat);
        }
      }
    }
  }

  public int generate()
  {
    this.setStopDate();
    this.setParameters();
    try
    {
      this.getJsonReport(this.outputFile);
    }
    catch (IOException e)
    {
      outWriter.println("Incorrect path to save JsonFile.");
      return 1;
    }

    long duration = this.getProcessDuration();
    outWriter.println("Process duration: " + duration + "ms");
    return 0;
  }

  public void initialize()
  {
    this.setStartDate();
  }

  void getJsonReport(String path) throws
      IOException
  {
    sortCollections();
    OutputStream out = null;
    try
    {
      if (path == null) {
          out = new PrintStream(System.out);
      } else {
          out = new FileOutputStream(path);
      }
      JsonWriter jw = JsonWriter.createJsonWriter(true);
      jw.writeJson(this, out);
    }
    finally
    {
      if (out != null)
      {
        out.close();
      }
    }
  }

  void sortCollections()
  {
    Collections.sort(getItems());
    Collections.sort(messages);
    for (CheckMessage m : messages)
    {
      m.sortLocations();
    }

  }
  long getProcessDuration()
  {
    return this.checker.getProcessDuration();
  }

  void setStartDate()
  {
    this.checker.setStartDate();
  }

  void setStopDate()
  {
    this.checker.setStopDate();
  }

  @Override
  public void message(Message message, EPUBLocation location, Object... args)
  {
    CheckMessage.addCheckMessage(messages, message, location, args);
  }

  @Override
  public void info(String resource, FeatureEnum feature, String value)
  {
    this.publication.handleInfo(resource, feature, value);
    if (resource != null && !resource.equals(""))
    {
      ItemMetadata item = ItemMetadata.getItemByName(getItemIndex(), resource);
      item.handleInfo(feature, value);
    }
  }

  List<ItemMetadata> getItems()
  {
    if (this.items == null)
    {
      if (this.itemIndex != null)
      {
        this.items = new ArrayList<ItemMetadata>(itemIndex.values());
      }
      else
      {
        this.items = new ArrayList<ItemMetadata>();
      }
    }
    return items;
  }

  Map<String, ItemMetadata> getItemIndex()
  {
    if (this.itemIndex == null)
    {
      this.itemIndex = new HashMap<String, ItemMetadata>();
      if (this.items != null && this.items.size() > 0)
      {
        for (ItemMetadata item : items)
        {
          itemIndex.put(item.getFileName(), item);
        }
      }
    }
    return itemIndex;
  }
}

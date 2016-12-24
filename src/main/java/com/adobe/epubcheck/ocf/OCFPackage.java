package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.OPFPeeker;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.xml.XMLParser;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class OCFPackage implements GenericResourceProvider
{
  final Hashtable<String, EncryptionFilter> enc;
  String uniqueIdentifier;
  private Report reporter;
  private final Supplier<OCFData> ocfData = Suppliers.memoize(new Supplier<OCFData>()
  {
    @Override
    public OCFData get()
    {
      Preconditions.checkNotNull(reporter);
      XMLParser containerParser = new XMLParser(new ValidationContextBuilder()
          .path(OCFData.containerEntry).resourceProvider(OCFPackage.this).report(reporter)
          .mimetype("xml").build());
      OCFHandler containerHandler = new OCFHandler(containerParser);
      containerParser.addXMLHandler(containerHandler);
      containerParser.process();
      return containerHandler;
    }
  });

  private final Supplier<Map<String, OPFData>> opfData = Suppliers
      .memoize(new Supplier<Map<String, OPFData>>()
      {
        @Override
        public Map<String, OPFData> get()
        {
          Preconditions.checkNotNull(reporter);
          Map<String, OPFData> result = new HashMap<String, OPFData>();
          for (String opfPath : ocfData.get().getEntries(OPFData.OPF_MIME_TYPE))
          {
            OPFPeeker peeker = new OPFPeeker(opfPath, reporter, OCFPackage.this);
            try
            {
              result.put(opfPath, peeker.peek());
            } catch (InvalidVersionException e)
            {
              reporter.message(MessageId.OPF_001, EPUBLocation.create(opfPath),
                  e.getMessage());
            } catch (IOException ignored)
            {
              // missing file will be reported later
            }
          }
          return Collections.unmodifiableMap(result);
        }
      });

  public OCFPackage()
  {
    this.enc = new Hashtable<String, EncryptionFilter>();
  }

  public void setEncryption(String name, EncryptionFilter encryptionFilter)
  {
    enc.put(name, encryptionFilter);
  }

  /**
   * @param name
   *          the name of a relative file that is possibly in the container
   * @return true if the file is in the container, false otherwise
   */
  public abstract boolean hasEntry(String name);

  public abstract long getTimeEntry(String name);

  /**
   * @param name
   *          the name of a relative file to fetch from the container.
   * @return an InputStream representing the data from the named file, possibly
   *         decrypted if an appropriate encryption filter has been set
   */
  public abstract InputStream getInputStream(String name)
    throws IOException;

  /**
   * @return a list of all the entries in this container. May contain duplicate
   *         entries (which is invalid in EPUB).
   * @throws IOException
   */
  public abstract List<String> getEntries()
    throws IOException;

  /**
   * @return a set of relative file names of files in this container (cleaned from duplicates)
   * @throws IOException
   */
  public abstract Set<String> getFileEntries()
    throws IOException;

  /**
   * @return a set of relative directory entries in this container (cleaned from duplicates)
   * @throws IOException
   */
  public abstract Set<String> getDirectoryEntries()
    throws IOException;

  /**
   * @param fileName
   *          name of the file to test
   * @return true if I have an Encryption filter for this particular file.
   */
  public boolean canDecrypt(String fileName)
  {
    EncryptionFilter filter = enc.get(fileName);
    return filter == null || filter.canDecrypt();
  }

  /**
   * This method parses the container entry and stores important data, but does
   * /not/ validate the container against a schema definition.
   * <p>
   * The parsed OCFData objects are memoized.
   * </p>
   * <p>
   * This OCFPackage's reporter is used to report any error that may occur the
   * first time the OCFData is parsed.
   * </p>
   *
   */
  public OCFData getOcfData()
  {
    return ocfData.get();
  }

  /**
   * This method parses the OPF root files contained in an OCFContainer and
   * stores important data, but does /not/ validate the OPF file against a
   * schema definition.
   * <p>
   * The parsed OPFData objects are memoized.
   * </p>
   * <p>
   * This OCFPackage's reporter is used to report any error that may occur the
   * first time the OPFData is parsed.
   * </p>
   *
   * @return an map with the OPF root files as keys and the OPFData as values.
   */
  public Map<String, OPFData> getOpfData()
  {
    return opfData.get();
  }

  public abstract void reportMetadata(String fileName, Report report);

  public abstract String getName();

  public abstract String getPackagePath();

  public void setReport(Report reporter)
  {
    this.reporter = reporter;
  }
}

package com.adobe.epubcheck.ocf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.opf.OPFData;
import com.adobe.epubcheck.opf.OPFPeeker;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.xml.XMLParser;

public abstract class OCFPackage implements GenericResourceProvider
{
  final Hashtable<String, EncryptionFilter> enc;
  String uniqueIdentifier;

  public OCFPackage()
  {
    this.enc = new Hashtable<String, EncryptionFilter>();
  }

  public void setEncryption(String name, EncryptionFilter encryptionFilter)
  {
    enc.put(name, encryptionFilter);
  }

	public void setUniqueIdentifier(String idval) {
		uniqueIdentifier = idval;
	}
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
  /**
   * @param name the name of a relative file that is possibly in the container
   * @return true if the file is in the container, false otherwise
   */
  public abstract boolean hasEntry(String name);

  public abstract long getTimeEntry(String name);

  /**
   * @param name the name of a relative file to fetch from the container.
   * @return an InputStream representing the data from the named file, possibly
   *         decrypted if an appropriate encryption filter has been set
   */
  public abstract InputStream getInputStream(String name) throws
      IOException;

  /**
     * @return a list of all the entries in this container. May contain duplicate entries (which is invalid in EPUB).
     * @throws IOException
     */
    public abstract List<String> getEntries() throws IOException;
    
    /**
   * @return a set of relative file names of files in this container
   * @throws IOException
   */
  public abstract Set<String> getFileEntries() throws
      IOException;

  /**
   * @return a set of relative directory entries in this container
   * @throws IOException
   */
  public abstract Set<String> getDirectoryEntries() throws
      IOException;


  /**
   * @param fileName name of the file to test
   * @return true if I have an Encryption filter for this particular file.
   */
  public boolean canDecrypt(String fileName)
  {
    EncryptionFilter filter = enc.get(fileName);
    return filter == null || filter.canDecrypt();
  }

  /**
   * This method parses the container entry and stores important data, but does /not/
   * validate the container against a schema definition.
   *
   * @param reporter a Report instance where errors are reported
   * @return an OCFHandler instance, cast to the OCFData interface
   */
  public OCFData getOcfData(Report reporter)
  {
    XMLParser containerParser;
    InputStream in = null;
    try
    {
      in = getInputStream(OCFData.containerEntry);
      containerParser = new XMLParser(this, in,
          OCFData.containerEntry, "xml", reporter, null);
      OCFHandler containerHandler = new OCFHandler(containerParser);
      containerParser.addXMLHandler(containerHandler);
      containerParser.process();
      return containerHandler;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      try
      {
        if (in != null)
        {
          in.close();
        }
      }
      catch (Exception ignored)
      {

      }
    }
  }


  /**
   * This method parses the OPF root files contained in an OCFContainer and stores important data,
   * but does /not/ validate the OPF file against a schema definition.
   *
   * @param container the OCFData container which holds the container.xml data
   * @param reporter  a Report instance where errors are reported
   * @return an map with the OPF root files as keys and the OPFData as values.
   * @throws InvalidVersionException if the 'version' attribute in the <package> element
   *                                 is not "2.0" or "3.0"
   * @throws IOException             for any other io error.
   */
  public Map<String, OPFData> getOpfData(OCFData container, Report reporter)
      throws
      InvalidVersionException,
      IOException
  {
    Map<String, OPFData> result = new HashMap<String, OPFData>();
    for (String opfPath : container.getEntries(OPFData.OPF_MIME_TYPE))
    {
      InputStream inv = null;
      try
      {
        inv = getInputStream(opfPath);
        OPFPeeker peeker = new OPFPeeker(opfPath, reporter);
        result.put(opfPath, peeker.peek(getInputStream(opfPath)));
      }
      finally
      {
        try
        {
          if (inv != null)
          {
            inv.close();
          }
        }
        catch (Exception ignored)
        {
        }
      }
    }
    return Collections.unmodifiableMap(result);
  }

  public abstract void reportMetadata(String fileName, Report report);

  public abstract String getName();

  public abstract String getPackagePath();
}

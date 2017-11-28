package com.adobe.epubcheck.ctc.epubpackage;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.PathUtil;
import org.w3c.dom.Document;

import java.io.File;
import java.util.zip.ZipFile;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubPackage
{
  private String packageMainFilePath;
  private ZipFile zip;
  private Document packDoc;
  private String packageMainPath = "";
  private PackageManifest manifest = new PackageManifest();
  private PackageSpine spine = new PackageSpine();
  private PackageMetadata metadata = new PackageMetadata();
  private EPUBVersion version;
  private final String fileName;

  public String getFileName()
  {
    return this.fileName;
  }

  public Document getPackDoc()
  {
    return packDoc;
  }

  void setPackDoc(Document packDoc)
  {
    this.packDoc = packDoc;
  }

  public PackageMetadata getMetadata()
  {
    return metadata;
  }

  public void setMetadata(PackageMetadata metadata)
  {
    this.metadata = metadata;
  }

  public EPUBVersion getVersion()
  {
    return version;
  }

  public void setVersion(EPUBVersion version)
  {
    this.version = version;
  }

  public EpubPackage(String packageMainFile, ZipFile zip, Document doc)
  {
    setPackageMainFile(packageMainFile);
    setZip(zip);
    setPackDoc(doc);
    if (packageMainFile.lastIndexOf('/') > 0)
    {
      setPackageMainPath(packageMainFile.substring(0, packageMainFile.lastIndexOf('/')));
    }
    setManifest(new PackageManifest());
    setSpine(new PackageSpine());
    File file = new File(zip.getName());
    this.fileName = file.getName();
  }

  public String getPackageMainFile()
  {
    return packageMainFilePath;
  }

  public void setPackageMainFile(String packageMainFile)
  {
    this.packageMainFilePath = packageMainFile;
  }

  public String getPackageMainPath()
  {
    return packageMainPath;
  }

  void setPackageMainPath(String packageMainPath)
  {
    this.packageMainPath = packageMainPath;
  }

  public PackageManifest getManifest()
  {
    return manifest;
  }

  public void setManifest(PackageManifest manifest)
  {
    this.manifest = manifest;
  }

  public PackageSpine getSpine()
  {
    return spine;
  }

  public void setSpine(PackageSpine spine)
  {
    this.spine = spine;
  }

  public ZipFile getZip()
  {
    return zip;
  }

  void setZip(ZipFile zip)
  {
    this.zip = zip;
  }

  public boolean isSpineItem(String id)
  {
    if (id == null || id.equals(""))
    {
      return false;
    }

    for (SpineItem item : spine.getItems())
    {
      if (id.equals(item.getIdref()))
      {
        return true;
      }
    }

    return false;
  }

  public static boolean isGlobalFixed(EpubPackage epack)
  {
    for (MetadataElement me : epack.getMetadata().getMetaElements())
    {
      if ("rendition:layout".equals(me.getName()))
      {
        return ("pre-paginated".equals(me.getValue()));
      }
      else if ("meta".equals(me.getName()))
      {
        String property = me.getAttribute("property");
        if ("rendition:layout".equals(property))
        {
          return ("pre-paginated".equals(me.getValue()));
        }
      }
    }
    return false;
  }

  public String getManifestItemFileName(ManifestItem mi)
  {
    if (mi != null)
    {
      return getManifestItemFileName(mi.getHref());
    }
    return "";
  }

  public String getManifestItemFileName(String entryName)
  {
    if (entryName == null)
      return "";

    String fileToParse;

    if (this.getPackageMainPath() != null && this.getPackageMainPath().length() > 0)
    {
      fileToParse = PathUtil.resolveRelativeReference(this.getPackageMainFile(), entryName, null);
    }
    else
    {
      fileToParse = entryName;
    }
    int hash = fileToParse.lastIndexOf("#");
    if (hash > 0)
    {
      fileToParse = fileToParse.substring(0, hash);
    }
    return fileToParse;
  }


}

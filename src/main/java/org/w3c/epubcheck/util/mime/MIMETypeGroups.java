package org.w3c.epubcheck.util.mime;

// Work in progress - 2026
public final class MIMETypeGroups
{
  private MIMETypeGroups()
  {
  }

  public static boolean isImage(MIMEType mimetype)
  {
    return "image".equals(mimetype.type());
  }

  public static boolean isAudioOrVideo(MIMEType mimetype)
  {
    return "audio".equals(mimetype.type())
        || "video".equals(mimetype.type())
        || "application/ogg".equals(mimetype.essence());
  }

  public static boolean isFont(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isZIP(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isArchive(MIMEType mimetype)
  {
    switch (mimetype.essence())
    {
    case "application/x-rar-compressed":
    case "application/zip":
    case "application/x-gzip":
      return true;
    default:
      return false;
    }
  }

  public static boolean isXML(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isHTML(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isScriptable(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isJavascript(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static boolean isJSON(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }

  public static MIMEType minimize(MIMEType mimetype)
  {
    throw new UnsupportedOperationException();
  }
}

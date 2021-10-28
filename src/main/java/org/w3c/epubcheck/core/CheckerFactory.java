package org.w3c.epubcheck.core;

import org.w3c.epubcheck.constants.MIMEType;

import com.adobe.epubcheck.bitmap.BitmapChecker;
import com.adobe.epubcheck.css.CSSChecker;
import com.adobe.epubcheck.dict.SearchKeyMapChecker;
import com.adobe.epubcheck.dtbook.DTBookChecker;
import com.adobe.epubcheck.opf.PublicationResourceChecker;
import com.adobe.epubcheck.opf.OPFChecker;
import com.adobe.epubcheck.opf.OPFChecker30;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.ops.OPSChecker;
import com.adobe.epubcheck.overlay.OverlayChecker;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.base.Optional;

// FIXME 2021 Romain - document
public final class CheckerFactory
{
  public enum CheckerTypes
  {

  }

  public static Checker newChecker(ValidationContext context)
  {
    Optional<MIMEType> mimeType = MIMEType.get(context.mimeType);
    if (mimeType.isPresent())
    {
      switch (mimeType.get())
      {
      case CSS:
        return new CSSChecker(context);
      case DTBOOK:
        return new DTBookChecker(context);
      case EPUB:
        // FIXME 2021 Romain - ValidationContext-based EPUB checker will come later
        break;
      case HTML:
        if (context.version == EPUBVersion.VERSION_2) return new OPSChecker(context);
        break;
      case IMAGE_GIF:
      case IMAGE_JPEG:
      case IMAGE_PNG:
        return new BitmapChecker(context);
      case OEBPS:
        if (context.version == EPUBVersion.VERSION_2) return new OPSChecker(context);
        break;
      case PACKAGE_DOC:
        return (context.version == EPUBVersion.VERSION_2) ? new OPFChecker(context)
            : new OPFChecker30(context);
      case SEARCH_KEY_MAP:
        if (context.version == EPUBVersion.VERSION_3) return new SearchKeyMapChecker(context);
        break;
      case SVG:
        return new OPSChecker(context);
      case SMIL:
        if (context.version == EPUBVersion.VERSION_3) return new OverlayChecker(context);
        break;
      case XHTML:
        return new OPSChecker(context);
      default:
        break;
      }
    }
    return new PublicationResourceChecker(context);
  }
}

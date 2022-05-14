package com.adobe.epubcheck.xml.handlers;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.EPUBVersion;

public class DeclarationHandler extends LocationHandler
{

  private final Report report;
  private final String mimeType;
  private final EPUBVersion version;
  private boolean firstStartDTDInvocation = true;
  private final Set<String> entities = new HashSet<String>();

  public DeclarationHandler(ValidationContext context)
  {
    super(context);
    this.report = context.report;
    this.mimeType = context.mimeType;
    this.version = context.version;

    // XML predefined
    entities.add("gt");
    entities.add("lt");
    entities.add("amp");
    entities.add("quot");
    entities.add("apos");
  }

  @Override
  public void startDTD(String root, String publicId, String systemId)
    throws SAXException
  {
    // for modular DTDs etc, just issue a warning for the top level IDs.
    if (!firstStartDTDInvocation)
    {
      return;
    }

    handleDocTypeUserInfo(root, publicId, systemId);

    firstStartDTDInvocation = false;
  }

  private void handleDocTypeUserInfo(String root, String publicId, String systemId)
  {
    if (version == EPUBVersion.VERSION_2)
    {

      if ("application/xhtml+xml".equals(mimeType) && root.equals("html"))
      {
        // OPS 2.0(.1)
        String complete = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n"
            + "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";

        if (matchDoctypeId("-//W3C//DTD XHTML 1.1//EN", publicId, complete))
        {
          matchDoctypeId("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", systemId, complete);
        }

      }

      if ("opf".equals(mimeType) && (publicId != null || systemId != null))
      {

        // 1.2: <!DOCTYPE package PUBLIC
        // "+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN"
        // "http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd">
        // http://http://idpf.org/dtds/oeb-1.2/oebpkg12.dtd
        if ("package".equals(root)
            && (publicId == null
                || publicId.equals("+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN"))
            && (systemId == null
                || systemId.equals("http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd")))
        {
          // for heritage content collections, dont warn about this, as its not
          // explicitly forbidden by the spec
        }
        else
        {
          report.message(MessageId.HTM_009, location());
        }

      }

      if ("application/x-dtbncx+xml".equals(mimeType))
      {
        String complete = "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" "
            + "\n \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">";
        if (matchDoctypeId("-//NISO//DTD ncx 2005-1//EN", publicId, complete))
        {
          matchDoctypeId("http://www.daisy.org/z3986/2005/ncx-2005-1.dtd", systemId, complete);
        }
      }

    }
    else if (version == EPUBVersion.VERSION_3)
    {
      if ("application/xhtml+xml".equals(mimeType)
          && "html".equalsIgnoreCase(root))
      {
        String complete = "<!DOCTYPE html>";
        // warn for obsolete or unknown doctypes
        if (publicId == null && (systemId == null || systemId.equals("about:legacy-compat")))
        {
          // we assume to have have <!DOCTYPE html>
          // or <!DOCTYPE html SYSTEM "about:legacy-compat">
        }
        else
        {
          report.message(MessageId.HTM_004, location(), publicId, complete);
        }
      }
      else if (publicId != null || systemId != null)
      {
        // check if the declaration is allowed for the current media type
        boolean isAllowed;
        switch (mimeType)
        {
        case "image/svg+xml":
          isAllowed = "-//W3C//DTD SVG 1.1//EN".equals(publicId)
              && "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd".equals(systemId);
          break;
        case "application/mathml+xml":
        case "application/mathml-content+xml":
        case "application/mathml-presentation+xml":
          isAllowed = "-//W3C//DTD MathML 3.0//EN".equals(publicId)
              && "http://www.w3.org/Math/DTD/mathml3/mathml3.dtd".equals(systemId);
          break;
        default:
          isAllowed = false;
        }
        if (!isAllowed)
        {
          report.message(MessageId.OPF_073, location());
        }
      }
    }

  }

  private boolean matchDoctypeId(String expected, String given, String messageParam)
  {
    if (!expected.equals(given))
    {
      report.message(MessageId.HTM_004, location(), given == null ? "" : given, messageParam);
      return false;
    }
    return true;
  }

  @Override
  public void startEntity(String ent)
    throws SAXException
  {
    if (!entities.contains(ent) && !ent.equals("[dtd]"))
    {
      // This message may never be reported. Undeclared entities result in a Sax
      // Parser Error and message RSC_005.
      report.message(MessageId.HTM_011, location());
    }
  }

  @Override
  public void externalEntityDecl(String name, String publicId, String systemId)
    throws SAXException
  {

    if (version == EPUBVersion.VERSION_3)
    {
      report.message(MessageId.HTM_003, location(), name);
      return;
    }
    entities.add(name);
  }

  @Override
  public void internalEntityDecl(String name, String value)
    throws SAXException
  {
    entities.add(name);
  }
}

package com.adobe.epubcheck.xml.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ResourceUtil;
import com.google.common.collect.ImmutableMap;

public class DefaultResolver implements EntityResolver
{

  private static final Map<String, String> SYSTEM_ID_MAP = new ImmutableMap.Builder<String, String>()
      // OEB 1.2
      .put("http://openebook.org/dtds/oeb-1.2/oebpkg12.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/oebpkg12.dtd"))
      .put("http://http://idpf.org/dtds/oeb-1.2/oebpkg12.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/oebpkg12.dtd"))
      .put("http://openebook.org/dtds/oeb-1.2/oeb12.ent",
          ResourceUtil.getResourcePath("schema/20/dtd/oeb12.dtdinc"))
      .put("http://openebook.org/dtds/oeb-1.2/oebdoc12.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/oebdoc12.dtd"))

      // 2.0 dtd, probably never published
      .put("http://www.idpf.org/dtds/2007/opf.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/opf20.dtd"))
      // xhtml 1.0
      .put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml1-transitional.dtd"))
      .put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml1-strict.dtd"))
      .put("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml-lat1.dtdinc"))
      .put("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml-symbol.dtdinc"))
      .put("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml-special.dtdinc"))
      // svg 1.1
      .put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/svg11.dtd"))
      // dtbook
      .put("http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/dtbook-2005-2.dtd"))
      // ncx
      .put("http://www.daisy.org/z3986/2005/ncx-2005-1.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/ncx-2005-1.dtd"))

      // xhtml 1.1: just reference the character entities, as we validate with
      // rng
      .put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml11-ent.dtd"))
      .put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd",
          ResourceUtil.getResourcePath("schema/20/dtd/xhtml11-ent.dtd"))

      // non-resolved names; Saxon (which schematron requires and registers as
      // preferred parser, it seems) passes us those (bad, bad!), work around it
      .put("xhtml-lat1.ent", ResourceUtil.getResourcePath("dtd/xhtml-lat1.dtdinc"))
      .put("xhtml-symbol.ent", ResourceUtil.getResourcePath("dtd/xhtml-symbol.dtdinc"))
      .put("xhtml-special.ent", ResourceUtil.getResourcePath("dtd/xhtml-special.dtdinc")).build();

  private final EPUBVersion version;

  public DefaultResolver(EPUBVersion version)
  {
    this.version = version;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException,
    IOException
  {

    String resourcePath = SYSTEM_ID_MAP.get(systemId);

    // external entities are not resolved in EPUB 3
    if (version == EPUBVersion.VERSION_3 || systemId.equals("about:legacy-compat"))
    {
      return new InputSource(new StringReader(""));
    }
    else if (resourcePath != null)
    {
      InputStream resourceStream = ResourceUtil.getResourceStream(resourcePath);
      InputSource source = new InputSource(resourceStream);
      source.setPublicId(publicId);
      source.setSystemId(systemId);
      return source;
    }
    else
    {
      // check for a system prop that turns off online fetching
      // the default is to attempt online fetching, as this has been the default
      // forever
      boolean offline = Boolean.parseBoolean(System.getProperty("epubcheck.offline"));
      // TODO better test remote URLs
      if (systemId.startsWith("http") && offline)
      {
        return new InputSource(new StringReader(""));
      }
      // else return null and let the caller try to fetch the goods
      return null;
    }
  }

}

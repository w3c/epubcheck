package com.adobe.epubcheck.dict;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker.Type;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.XMLElement;
import com.adobe.epubcheck.xml.XMLHandler;
import com.adobe.epubcheck.xml.XMLParser;

public class SearchKeyMapHandler implements XMLHandler
{

  private final ValidationContext context;
  private final String path;
  private final XMLParser parser;

  public SearchKeyMapHandler(ValidationContext context, XMLParser parser)
  {
    this.context = context;
    this.path = context.path;
    this.parser = parser;
  }

  public void startElement()
  {

    XMLElement e = parser.getCurrentElement();
    String name = e.getName();

    if ("http://www.idpf.org/2007/ops".equals(e.getNamespace()))
    {

      if ("search-key-group".equals(name))
      {
        processRef(e.getAttribute("href"));
      }
      else if ("match".equals(name))
      {
        processRef(e.getAttribute("href"));
      }
    }
  }

  private void processRef(String ref)
  {
    if (ref != null && context.xrefChecker.isPresent())
    {
      ref = PathUtil.resolveRelativeReference(path, ref, null);
      context.xrefChecker.get().registerReference(path, parser.getLineNumber(),
          parser.getColumnNumber(), ref, Type.SEARCH_KEY);
    }
  }

  public void characters(char[] chars, int arg1, int arg2)
  {
  }

  public void endElement()
  {
  }

  public void ignorableWhitespace(char[] chars, int arg1, int arg2)
  {
  }

  public void processingInstruction(String arg0, String arg1)
  {
  }

}

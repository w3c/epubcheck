package com.adobe.epubcheck.dict;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.opf.XRefChecker.Type;
import com.adobe.epubcheck.util.PathUtil;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;

public class SearchKeyMapHandler extends XMLHandler
{

  public SearchKeyMapHandler(ValidationContext context)
  {
    super(context);
  }

  @Override
  public void startElement()
  {

    XMLElement e = currentElement();
    String name = e.getName();

    if ("http://www.idpf.org/2007/ops".equals(e.getNamespace()))
    {

      if ("search-key-group".equals(name))
      {
        processRef();
      }
      else if ("match".equals(name))
      {
        processRef();
      }
    }
  }

  private void processRef()
  {
    String ref = currentElement().getAttribute("href");
    if (ref != null && context.xrefChecker.isPresent())
    {
      ref = PathUtil.resolveRelativeReference(path, ref);
      context.xrefChecker.get().registerReference(path, location().getLine(),
          location().getColumn(), ref, Type.SEARCH_KEY);
    }
  }

}

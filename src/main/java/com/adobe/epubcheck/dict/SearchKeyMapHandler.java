package com.adobe.epubcheck.dict;

import org.w3c.epubcheck.core.references.Reference;

import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.xml.handlers.XMLHandler;
import com.adobe.epubcheck.xml.model.XMLElement;

import io.mola.galimatias.URL;

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
    URL ref = checkURL(currentElement().getAttribute("href"));
    registerReference(ref, Reference.Type.SEARCH_KEY);
  }

}

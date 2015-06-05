package com.adobe.epubcheck.util;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.MessageId;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceHelper
{
  private long id = 0;
  private static HashSet<String> expectedNamespaces = new HashSet<String>();
  static
  {
    expectedNamespaces.add(""); // don't report on the default (empty) namespace
    expectedNamespaces.add(EpubConstants.EpubTypeNamespaceUri);
    expectedNamespaces.add(EpubConstants.XmlNamespaceUri);
    expectedNamespaces.add(EpubConstants.HtmlNamespaceUri);
    expectedNamespaces.add(EpubConstants.OpfNamespaceUri);
    expectedNamespaces.add(EpubConstants.OpenDocumentContainerNamespaceUri);
    expectedNamespaces.add(EpubConstants.DCElements);
  }

  private class IdHashMap<K,V> extends HashMap<K, V>
  {
    final long id;
    public long getId()
    {
      return id;
    }

    public IdHashMap(long id)
    {
      super();
      this.id = id;
    }
    public IdHashMap(long id, Map<K, V> m)
    {
      super(m);
      this.id = id;
    }
  }

  private static boolean isExpectedNamespace(String uri)
  {
    return (uri != null) ? expectedNamespaces.contains(uri) : false;
  }
  private class NamespaceInstance
  {
    private long id;
    private String prefix;
    private String uri;
    private EPUBLocation location;
    private boolean inUse;
    private Pattern prefixPattern;

    public NamespaceInstance(long id, String prefix, String uri, EPUBLocation location)
    {
      setId(id);
      setPrefix(prefix);
      setUri(uri);
      setLocation(location);
      setInUse(false);
      setPrefixPattern(Pattern.compile("^" + prefix + ":.+"));
    }

    public long getId()
    {
      return id;
    }

    public void setId(long id)
    {
      this.id = id;
    }

    public String getPrefix()
    {
      return prefix;
    }

    public void setPrefix(String prefix)
    {
      this.prefix = prefix;
    }

    public String getUri()
    {
      return uri;
    }

    public void setUri(String uri)
    {
      this.uri = uri;
    }

    public EPUBLocation getLocation()
    {
      return location;
    }

    public void setLocation(EPUBLocation location)
    {
      this.location = location;
    }

    public boolean isInUse()
    {
      return inUse;
    }

    public void setInUse(boolean inUse)
    {
      this.inUse = inUse;
    }

    public Pattern getPrefixPattern()
    {
      return prefixPattern;
    }

    public void setPrefixPattern(Pattern prefixPattern)
    {
      this.prefixPattern = prefixPattern;
    }

    @Override
    public String toString()
    {
      if (getUri() != null && getUri().length() > 0)
      {
        return "xmlns" + this.getPrefix() + "=" + getUri();
      }
      else
      {
        return "xmlns" + this.getPrefix();
      }
    }
  }

  private class NamespaceContext
  {
    private long id;
    private int useCount;
    private IdHashMap<String, String> prefixMap;
    private IdHashMap<String, NamespaceInstance> uriMap;

    public NamespaceContext(long id)
    {
      this.useCount = 1;
      this.id = id;
      this.prefixMap = new IdHashMap<String, String>(id);
      this.uriMap = new IdHashMap<String, NamespaceInstance>(id);
    }

    public NamespaceContext(long id, NamespaceContext other)
    {
      other.decrementUseCount();
      this.useCount = 1;
      this.id = id;
      this.prefixMap = new IdHashMap<String, String>(id, other.getPrefixMap());
      this.uriMap = new IdHashMap<String, NamespaceInstance>(id, other.getUriMap());
    }

    public long getId()
    {
      return id;
    }
    public int getUseCount()
    {
      return useCount;
    }
    public int incrementUseCount()
    {
      return ++useCount;
    }
    public int decrementUseCount()
    {
      return --useCount;
    }
    public IdHashMap<String, String> getPrefixMap()
    {
      return prefixMap;
    }
    public IdHashMap<String, NamespaceInstance> getUriMap()
    {
      return  uriMap;
    }
  }

  private Stack<NamespaceContext> contexts = new Stack<NamespaceContext>();

  public NamespaceHelper()
  {
    contexts.push(new NamespaceContext(id++));
  }

  private void  pushContext()
  {
    ++id;
    NamespaceContext currentContext = contexts.peek();
    currentContext.incrementUseCount();
  }

  private String findMatchingPrefix(String qName)
  {
    NamespaceContext currentContext = contexts.peek();

    for (NamespaceInstance instance : currentContext.getUriMap().values())
    {
      Pattern p = instance.getPrefixPattern();
      Matcher m = p.matcher(qName);
      if (m.find())
      {
        return instance.getPrefix();
      }
    }

    return null;
  }

  public String findPrefixForUri(String uri)
  {
    String prefix = null;
    NamespaceContext currentContext = contexts.peek();
    if (uri != null)
    {
      NamespaceInstance instance = currentContext.getUriMap().get(uri);
      if (instance != null)
      {
        prefix = instance.getPrefix();
      }
    }
    return prefix;
  }

  private void recordPrefixUse(String prefix)
  {
    NamespaceContext currentContext = contexts.peek();
    String uri = currentContext.getPrefixMap().get(prefix);
    if (uri != null)
    {
      NamespaceInstance instance = currentContext.getUriMap().get(uri);
      if (instance != null && !instance.isInUse())
      {
        instance.setInUse(true);
      }
    }
  }

  private void recordUriUse(String uri)
  {
    NamespaceContext currentContext = contexts.peek();
    if (uri != null)
    {
      NamespaceInstance instance = currentContext.getUriMap().get(uri);
      if (instance != null && !instance.isInUse())
      {
        instance.setInUse(true);
      }
    }
  }

  private void popContext(Report report)
  {
    NamespaceContext currentContext = contexts.peek();
    if (0 == currentContext.decrementUseCount())
    {
      for (NamespaceInstance instance : currentContext.getUriMap().values())
      {
        // report on namespaces that are in this scope (ie, its id matches) and its not in use
        if (currentContext.getId() == instance.getId() && !instance.isInUse())
        {
          report.message(MessageId.HTM_044,  instance.getLocation(), instance.getUri());
        }
      }
      contexts.pop();
    }
  }

  public void declareNamespace(String prefix, String uri, EPUBLocation location, Report report)
  {
    NamespaceContext currentContext = contexts.peek();
    if (id != currentContext.getId())
    {
      NamespaceContext newContext = new NamespaceContext(id, currentContext);
      contexts.push(newContext);
      currentContext = newContext;
    }
    currentContext.getPrefixMap().put(prefix, uri);
    currentContext.getUriMap().put(uri, new NamespaceInstance(id, prefix, uri, location));
    if (!isExpectedNamespace(uri))
    {
      report.message(MessageId.HTM_010,  location, uri);
    }
  }

  static final Pattern xmlnsUriPattern = Pattern.compile("xmlns:([a-zA-Z]+)");
  public void onStartElement(String fileName, Locator locator, String uri, String qName, Attributes attributes, Report report)
  {

    pushContext();

    for (int i = 0; i < attributes.getLength(); ++i)
    {
      String aqName = attributes.getQName(i);
      Matcher m = xmlnsUriPattern.matcher(aqName);
      if (m.matches())
      {
        // the group holds the prefix, the value holds the uri
        declareNamespace(m.group(1), attributes.getValue(i), EPUBLocation.create(fileName, locator.getLineNumber(), locator.getColumnNumber(), aqName), report);
      }
      else
      {
        String foundPrefix = findMatchingPrefix(aqName);
        if (foundPrefix != null)
        {
          recordPrefixUse(foundPrefix);
        }
      }
    }

    String prefix = findMatchingPrefix(qName);
    if (prefix != null && prefix.length() > 0)
    {
      recordPrefixUse(prefix);
    }

    if (uri != null && uri.length() > 0)
    {
      recordUriUse(uri);
    }
  }

  public void onEndElement(Report report)
  {
    popContext(report);
  }
}

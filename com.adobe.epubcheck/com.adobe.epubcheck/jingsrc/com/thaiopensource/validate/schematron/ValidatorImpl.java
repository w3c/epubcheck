package com.thaiopensource.validate.schematron;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.sax.DelegatingContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

class ValidatorImpl extends DelegatingContentHandler implements Validator {
  private final Transformer transformer;
  private Locator locator;
  private TransformerException transformerException;

  private final Object contentHandlerMonitor = new Object();
  private final Object parseMonitor = new Object();
  private final Object transformMonitor = new Object();
  private Thread transformThread;
  private final ContentHandler outputHandler;

  class BlockingReader extends XMLReaderImpl {
    public void parse(InputSource in) throws SAXException {
      synchronized (parseMonitor) {
        synchronized (contentHandlerMonitor) {
          contentHandlerMonitor.notify();
        }
        try {
          parseMonitor.wait();
        }
        catch (InterruptedException e) {
          throw new SAXException(e);
        }
      }
    }

    public void setContentHandler(ContentHandler handler) {
      setDelegate(handler);
    }

    public ContentHandler getContentHandler() {
      return getDelegate();
    }

  }

  ValidatorImpl(Templates templates, PropertyMap properties) {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    outputHandler = new OutputHandler(eh);
    try {
      transformer = templates.newTransformer();
      // XXX set up transformer with a resolver that uses the resolver
      // XXX and XMLReaderCreator from properties
      // XXX set up transformer with an ErrorListener that just throws
    }
    catch (TransformerConfigurationException e) {
      throw new RuntimeException("could not create transformer");
    }
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }

  public void reset() {
    if (transformThread != null) {
      synchronized (transformMonitor) {
        transformThread.interrupt();
        try {
          transformMonitor.wait();
        }
        catch (InterruptedException e) { }
        transformThread = null;
      }
    }
    transformerException = null;
    locator = null;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument()
          throws SAXException {
    final SAXSource source = new SAXSource(new BlockingReader(),
                                           new InputSource(""));
    transformThread = new Thread(new Runnable() {
      public void run() {
        try {
          transformer.transform(source, new SAXResult(outputHandler));
        }
        catch (TransformerException e) {
          transformerException = e;
        }
        finally {
          synchronized (transformMonitor) {
            transformMonitor.notify();
          }
        }
      }
    }, "Transform");
    synchronized (contentHandlerMonitor) {
      transformThread.start();
      try {
        contentHandlerMonitor.wait();
      }
      catch (InterruptedException e) {
        throw new SAXException(e);
      }
    }
    if (locator != null)
      super.setDocumentLocator(locator);
    super.startDocument();
  }

  public void endDocument()
          throws SAXException {
    super.endDocument();
    synchronized (transformMonitor) {
      synchronized (parseMonitor) {
        parseMonitor.notify();
      }
      try {
        transformMonitor.wait();
      }
      catch (InterruptedException e) {
        throw new SAXException(e);
      }
      finally {
        transformThread = null;
      }
    }
    if (transformerException != null)
      throw toSAXException(transformerException);
  }

  static SAXException toSAXException(TransformerException transformerException) {
    // Unwrap where possible
    Throwable wrapped = transformerException.getException();
    if (wrapped instanceof SAXException)
      return (SAXException)wrapped;
    if (wrapped instanceof RuntimeException)
      throw (RuntimeException)wrapped;
    if (wrapped instanceof Exception)
      return new SAXException((Exception)wrapped);
    return new SAXException(transformerException);
  }
}

package com.thaiopensource.validate;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

/**
 * Validates an XML document with respect to a schema.  The schema is
 * determined when the <code>Validator</code> is created and cannot be
 * changed.  The XML document is provided to the <code>Validator</code>
 * by calling methods of the <code>ContentHandler</code> object returned
 * by <code>getContentHandler</code>; the methods must be called in
 * the sequence specified by the <code>ContentHandler</code>
 * interface.  If the <code>getDTDHandler</code> method returns
 * a non-null object, then method calls must be made on it
 * reporting DTD information.
 *
 * <p>Any errors will be reported to the <code>ErrorHandler</code>
 * specified when the <code>Validator</code> was created.  If, after the
 * call to the <code>endDocument</code> method, no errors have been
 * reported, then the XML document is valid.
 *
 * <p>A single <code>Validator</code> object is <em>not</em> safe for
 * concurrent access from multiple threads. A single
 * <code>ValidatorHandler</code> can be used to validate only a single
 * document at a time.
 *
 * <p>After completing validation of an XML document (i.e. after calling
 * the <code>endDocument</code> on the <code>ContentHandler</code>),
 * <code>reset</code> can be called to allow validation of another
 * document. The <code>reset</code> method may create new <code>ContentHandler</code>
 * and <code>DTDHandler</code> objects or may simply reinitialize the
 * state of the existing objects.  Therefore, <code>getContentHandler</code>
 * and <code>getDTDHandler</code> must be called after <code>reset</code>
 * to retrieve the objects to which the XML document to be validated
 * must be provided.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Validator {
  /**
   * Returns the ContentHandler that will receive the XML document.
   * Information about the XML document to be validated must be
   * reported by calling methods on the returned ContentHandler.
   * When validation of an XML document has been completed (either
   * endDocument() has been called or validation has been abandoned
   * prematurely), reset() must be called.  If no calls are made
   * on the ContentHandler, then reset() need not be called.
   * Implementations should allocate resources that require
   * cleanup (e.g. threads, open files) lazily, typically
   * in startDocument().
   *
   * This method does not change the state of the Validator: the same
   * object will always be returned unless <code>reset</code> is called.
   *
   * @see #reset()
   * @return a ContentHandler, never <code>null</code>
   */
  ContentHandler getContentHandler();

  /**
   * Returns a DTDHandler. Information about the DTD must be reported
   * by calling methods on the returned object, unless <code>null</code>
   * is returned.  The same object will always be returned unless
   * <code>reset</code> is called: this method does not change the state
   * of the Validator.
   *
   * @return a DTDHandler, maybe <code>null</code> if DTD information is
   * not significant to the <code>Validator</code>
   */
  DTDHandler getDTDHandler();

   /**
    * Cleans up after validating a document.  After completing validation
    * of a document, <code>reset</code> must be called. After calling
    * reset(), another document may be validated.  Calling this method
    * may create new ContentHandler and DTDHandler objects or may simply
    * reinitialize the state of the existing objects.
    */
   void reset();
}

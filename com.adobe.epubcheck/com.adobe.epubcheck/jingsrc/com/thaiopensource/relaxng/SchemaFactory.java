package com.thaiopensource.relaxng;

import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Flag;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A factory for RELAX NG schemas.  The factory creates <code>Schema</code> objects from their
 * XML representation.
 *
 * A single <code>SchemaFactory</code> is <em>not</em> safe for concurrent
 * access by multiple threads; it must be accessed by at most one thread at a time.
 * Schemas can be created concurrently by using a distinct <code>SchemaFactory</code> for each
 * thread.  However, the <code>Schema</code> objects created <em>are</em> safe for concurrent
 * access by multiple threads.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class SchemaFactory {
  private PropertyMapBuilder properties = new PropertyMapBuilder();
  private boolean compactSyntax = false;
  private SchemaReader autoSchemaLanguage = new AutoSchemaReader();

  /**
   * Constructs a schema factory.
   */
  public SchemaFactory() {
  }

  /**
   * Creates a schema by parsing an XML document.  A non-null <code>XMLReaderCreator</code> must be specified
   * with <code>setXMLReaderCreator</code> before calling <code>createSchema</code>.  The <code>ErrorHandler</code>
   * is allowed to be <code>null</code>. The <code>DatatypeLibraryFactory</code> is allowed to be <code>null</code>.
   *
   * <p>Normally, if a schema cannot be created, <code>createSchema</code> will throw
   * a <code>IncorrectSchemaException</code>; however,
   * before doing so, one or more errors will be reported using the <code>ErrorHandler</code> if it is non-null.  If the
   * <code>ErrorHandler</code> throws a <code>SAXException</code>, then <code>createSchema</code> will pass this
   * through rather than throwing a <code>IncorrectSchemaException</code>. Similarly, if <code>XMLReader.parse</code>
   * throws a <code>SAXException</code> or <code>IOException</code>, then <code>createSchema</code> will pass
   * this through rather than throwing a <code>IncorrectSchemaException</code>. Thus, if an error handler
   * is specified that reports errors to the user, there is no need to report any additional message to the
   * user if <code>createSchema</code> throws <code>IncorrectSchemaException</code>.
   *
   * @param in the <code>InputSource</code> containing the XML document to be parsed;
   * must not be <code>null</code>
   * @return the <code>Schema</code> constructed from the XML document;
   * never <code>null</code>.
   *
   * @throws IOException if an I/O error occurs
   * @throws SAXException if there is an XML parsing error and the XMLReader or ErrorHandler
   * throws a SAXException
   * @throws com.thaiopensource.validate.IncorrectSchemaException if the XML document was not a correct RELAX NG schema
   * @throws NullPointerException if the current XMLReaderCreator is <code>null</code>
   */
  public Schema createSchema(InputSource in) throws IOException, SAXException, IncorrectSchemaException {
    SchemaReader r = compactSyntax ? CompactSchemaReader.getInstance() : autoSchemaLanguage;
    return r.createSchema(in, properties.toPropertyMap());
  }

  /**
   * Specifies the XMLReaderCreator to be used for creating <code>XMLReader</code>s for parsing
   * the XML document.  Because of <code>include</code> and <code>externalRef</code> elements,
   * parsing a single RELAX NG may require the creation of multiple more than one <code>XMLReader</code>.
   * A non-null XMLReaderCreator must be specified before calling <code>createSchema</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for parsing the XML document containing
   * the schema; may be <code>null</code>
   * @see #getXMLReaderCreator
   */
  public void setXMLReaderCreator(XMLReaderCreator xrc) {
    properties.put(ValidateProperty.XML_READER_CREATOR, xrc);
  }

  /**
   * Returns the current <code>XMLReaderCreator</code> as specified by <code>setXMLReaderCreator</code>.
   * If <code>XMLReaderCreator</code> has never been called, then <code>getXMLReaderCreator</code>
   * returns null.
   *
   * @return the <code>XMLReaderCreator</code> that will be used for parsing the XML document containing
   * the schema; may be <code>null</code>
   *
   * @see #setXMLReaderCreator
   */
  public XMLReaderCreator getXMLReaderCreator() {
    return (XMLReaderCreator)properties.get(ValidateProperty.XML_READER_CREATOR);
  }

  /**
   * Specifies the <code>ErrorHandler</code> to be used for reporting errors while creating the schema.
   * This does not affect the error handler used for validation.
   *
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors while creating the schema;
   * may be <code>null</code>.
   * @see #getErrorHandler
   */
  public void setErrorHandler(ErrorHandler eh) {
    properties.put(ValidateProperty.ERROR_HANDLER, eh);
  }

  /**
   * Returns the <code>ErrorHandler</code> that will be used for reporting errors while creating the
   * schema. If <code>setErrorHandler</code> has not been called for this <code>SchemaFactory</code>,
   * then <code>getErrorHandler</code> returns <code>null</code>.
   *
   * @return the <code>ErrorHandler</code> to be used for reporting errors while creating the schema;
   * may be <code>null</code>.
   * @see #setErrorHandler
   */
  public ErrorHandler getErrorHandler() {
    return (ErrorHandler)properties.get(ValidateProperty.ERROR_HANDLER);
  }

  /**
   * Specifies the <code>DatatypeLibraryFactory</code> to be used for handling datatypes in the schema.
   * This also determines how datatypes are handled during validation.  If <code>null</code> is
   * specified then only the builtin datatypes will be supported.
   *
   * @param dlf the <code>DatatypeLibraryFactory</code> to be used for handling datatypes in the schema
   * @see #getDatatypeLibraryFactory
   */
  public void setDatatypeLibraryFactory(DatatypeLibraryFactory dlf) {
    properties.put(RngProperty.DATATYPE_LIBRARY_FACTORY, dlf);
  }

  /**
   * Returns the <code>DatatypeLibraryFactory</code> that will be used for handling datatypes in the
   * schema. If <code>setDatatypeLibraryFactory</code> has not been called for this <code>SchemaFactory</code>,
   * then <code>getDatatypeLibraryFactory</code> returns <code>null</code>.
   *
   * @return the <code>DatatypeLibraryFactory</code> to be used for handling datatypes in the schema;
   * may be null.
   * @see #setDatatypeLibraryFactory
   */
  public DatatypeLibraryFactory getDatatypeLibraryFactory() {
    return (DatatypeLibraryFactory)properties.get(RngProperty.DATATYPE_LIBRARY_FACTORY);
  }

  /**
   * Specifies whether to perform checking of ID/IDREF/IDREFS attributes in accordance with
   * RELAX NG DTD Compatibility.
   *
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS checking should be performed;
   * <code>false</code> otherwise
   *
   * @see #getCheckIdIdref
   * @see <a href="http://www.oasis-open.org/committees/relax-ng/compatibility.html#id">RELAX NG DTD Compatibility</a>
   */
  public void setCheckIdIdref(boolean checkIdIdref) {
    properties.put(RngProperty.CHECK_ID_IDREF, checkIdIdref ? Flag.PRESENT : null);
  }

  /**
   * Indicates whether ID/IDREF/IDREFS attributes will be checked in accordance RELAX NG DTD
   * Compatibility.  If <code>setCheckIdIdref</code> has not been called for this <code>SchemaFactory</code>,
   * then <code>getCheckIdref</code> will return <code>false</code>.
   *
   * @return <code>true</code> if ID/IDREF/IDREFS attributes will be checked;
   * <code>false</code> otherwise.
   *
   * @see #setCheckIdIdref
   * @see <a href="http://www.oasis-open.org/committees/relax-ng/compatibility.html#id">RELAX NG DTD Compatibility</a>
   */
  public boolean getCheckIdIdref() {
    return properties.contains(RngProperty.CHECK_ID_IDREF);
  }

  /**
   * Specifies whether to use the compact syntax to parse the RELAX NG schema rather than the normal XML syntax.
   *
   * @param compactSyntax <code>true</code> if the compact syntax should be used; <code>false</code>
   * if the XML syntax should be used
   * @see #getCompactSyntax
   */
  public void setCompactSyntax(boolean compactSyntax) {
    this.compactSyntax = compactSyntax;
  }

  /**
   * Indicates whether the compact syntax will be used to parse the RELAX NG schema rather than
   * the normal XML syntax.
   *
   * @return <code>true</code> if the compact syntax will be used; <code>false</code> if the XML
   * syntax will be used
   */
  public boolean getCompactSyntax() {
    return compactSyntax;
  }

  public void setFeasible(boolean feasible) {
    properties.put(RngProperty.FEASIBLE, feasible ? Flag.PRESENT : null);
  }

  public boolean getFeasible() {
    return properties.contains(RngProperty.FEASIBLE);
  }
}

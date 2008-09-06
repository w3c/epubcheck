package com.thaiopensource.relaxng.util;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import org.xml.sax.ErrorHandler;

/**
 * Provides a compatibility wrapper around ValidationDriver.  New applications
 * should use ValidationDriver directly.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 * @see ValidationDriver
 * @deprecated
 */
public class ValidationEngine extends ValidationDriver {

  /**
   * Flag indicating that ID/IDREF/IDREFS should be checked.
   * @see RngProperty#CHECK_ID_IDREF
   */
  public static final int CHECK_ID_IDREF = 01;
  /**
   * Flag indicating that the schema is in the RELAX NG compact syntax rather than the XML syntax.
   * @see CompactSchemaReader
   */
  public static final int COMPACT_SYNTAX = 02;
  /**
   * @see RngProperty#FEASIBLE
   */
  public static final int FEASIBLE = 04;

  /**
   * Default constructor.  Equivalent to <code>ValidationEngine(null, null, CHECK_ID_IDREF)</code>.
   */
  public ValidationEngine() {
    this(null, null, CHECK_ID_IDREF);
  }
  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param flags bitwise OR of flags selected from <code>CHECK_ID_IDREF</code>, <code>COMPACT_SYNTAX</code>,
   * <code>FEASIBLE</code>, <code>MNS</code>
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   * @see #CHECK_ID_IDREF
   * @see #COMPACT_SYNTAX
   * @see #FEASIBLE
   */
  public ValidationEngine(XMLReaderCreator xrc,
                          ErrorHandler eh,
                          int flags) {
    super(makePropertyMap(xrc, eh, flags),
          (flags & COMPACT_SYNTAX) == 0 ? null : CompactSchemaReader.getInstance());
  }

  private static PropertyMap makePropertyMap(XMLReaderCreator xrc, ErrorHandler eh, int flags) {
    PropertyMapBuilder builder = new PropertyMapBuilder();
    if (xrc == null)
      xrc = new Sax2XMLReaderCreator();
    ValidateProperty.XML_READER_CREATOR.put(builder, xrc);
    if (eh != null)
      ValidateProperty.ERROR_HANDLER.put(builder, eh);
    if ((flags & CHECK_ID_IDREF) != 0)
      RngProperty.CHECK_ID_IDREF.add(builder);
    if ((flags & FEASIBLE) != 0)
      RngProperty.FEASIBLE.add(builder);
    return builder.toPropertyMap();
  }

  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS should be checked; <code>false</code> otherwise
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   */
  public ValidationEngine(XMLReaderCreator xrc,
                          ErrorHandler eh,
                          boolean checkIdIdref) {
    this(xrc, eh, checkIdIdref ? CHECK_ID_IDREF : 0);
  }

  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS should be checked; <code>false</code> otherwise
   * @param compactSyntax <code>true</code> if the RELAX NG compact syntax should be used to parse the schema;
   * <code>false</code> if the XML syntax should be used
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   */
  public ValidationEngine(XMLReaderCreator xrc, ErrorHandler eh, boolean checkIdIdref, boolean compactSyntax) {
    this(xrc,
         eh,
         (checkIdIdref ? CHECK_ID_IDREF : 0)
         | (compactSyntax ? COMPACT_SYNTAX : 0));
  }


  public ValidationEngine(XMLReaderCreator xrc, ErrorHandler eh, boolean checkIdIdref, boolean compactSyntax,
                          boolean feasible) {
    this(xrc,
         eh,
         (checkIdIdref ? CHECK_ID_IDREF : 0)
         | (compactSyntax ? COMPACT_SYNTAX : 0)
         | (feasible ? FEASIBLE : 0));
  }

}

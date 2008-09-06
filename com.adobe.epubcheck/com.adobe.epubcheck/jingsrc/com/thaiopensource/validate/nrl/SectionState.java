package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.nrl.ModeUsage;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

interface SectionState {
  /**
   *
   * @param modeUsage
   * @param handler may be null
   */
  void addChildMode(ModeUsage modeUsage, ContentHandler handler);
  void addValidator(Schema schema, ModeUsage modeUsage);
  /**
   *
   * @param handler must not be null
   */
  void addActiveHandler(ContentHandler handler, ModeUsage attributeModeUsage);
  void addAttributeValidationModeUsage(ModeUsage modeUsage);
  void reject() throws SAXException;
}

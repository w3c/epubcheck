package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.nrl.Action;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

abstract class ResultAction extends Action {
  ResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(ContentHandler handler, SectionState state) throws SAXException;
  abstract ResultAction changeCurrentMode(Mode mode);
}

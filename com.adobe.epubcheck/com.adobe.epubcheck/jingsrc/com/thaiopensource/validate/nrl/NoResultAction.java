package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.nrl.Action;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import org.xml.sax.SAXException;

abstract class NoResultAction extends Action {
  NoResultAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  abstract void perform(SectionState state) throws SAXException;
  abstract NoResultAction changeCurrentMode(Mode mode);
}

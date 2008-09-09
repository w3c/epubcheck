package com.thaiopensource.validate.nrl;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.nrl.Mode;
import com.thaiopensource.validate.nrl.ModeUsage;
import com.thaiopensource.validate.nrl.NoResultAction;
import com.thaiopensource.validate.nrl.SectionState;
import org.xml.sax.SAXException;

class ValidateAction extends NoResultAction {
  private final Schema schema;

  ValidateAction(ModeUsage modeUsage, Schema schema) {
    super(modeUsage);
    this.schema = schema;
  }

  void perform(SectionState state) throws SAXException {
    state.addValidator(schema, getModeUsage());
  }

  NoResultAction changeCurrentMode(Mode mode) {
    return new ValidateAction(getModeUsage().changeCurrentMode(mode), schema);
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && schema.equals(((ValidateAction)obj).schema);
  }

  public int hashCode() {
    return super.hashCode() ^ schema.hashCode();
  }
}

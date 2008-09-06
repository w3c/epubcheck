package com.thaiopensource.validate.nrl;

class AllowAction extends NoResultAction {
  AllowAction(ModeUsage modeUsage) {
    super(modeUsage);
  }

  void perform(SectionState state) {
    state.addChildMode(getModeUsage(), null);
    state.addAttributeValidationModeUsage(getModeUsage());
  }

  NoResultAction changeCurrentMode(Mode mode) {
    return new AllowAction(getModeUsage().changeCurrentMode(mode));
  }
}

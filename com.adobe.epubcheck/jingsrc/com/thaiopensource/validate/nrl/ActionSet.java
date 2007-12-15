package com.thaiopensource.validate.nrl;

class ActionSet {
  private ResultAction resultAction;
  private NoResultAction[] noResultActions = new NoResultAction[0];

  ResultAction getResultAction() {
    return resultAction;
  }

  void setResultAction(ResultAction resultAction) {
    this.resultAction = resultAction;
  }

  void addNoResultAction(NoResultAction action) {
    NoResultAction[] actions = new NoResultAction[noResultActions.length + 1];
    System.arraycopy(noResultActions, 0, actions, 0, noResultActions.length);
    actions[noResultActions.length] = action;
    noResultActions = actions;
  }

  NoResultAction[] getNoResultActions() {
    return noResultActions;
  }

  ActionSet changeCurrentMode(Mode mode) {
    ActionSet actions = new ActionSet();
    if (this.resultAction != null)
      actions.resultAction = this.resultAction.changeCurrentMode(mode);
    actions.noResultActions = new NoResultAction[this.noResultActions.length];
    for (int i = 0; i < actions.noResultActions.length; i++)
      actions.noResultActions[i] = this.noResultActions[i].changeCurrentMode(mode);
    return actions;
  }
}

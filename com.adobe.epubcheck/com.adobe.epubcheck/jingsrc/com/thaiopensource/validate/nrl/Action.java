package com.thaiopensource.validate.nrl;

abstract class Action {
  private final ModeUsage modeUsage;

   Action(ModeUsage modeUsage) {
     this.modeUsage = modeUsage;
   }

   ModeUsage getModeUsage() {
     return modeUsage;
   }

   public boolean equals(Object obj) {
     return obj != null && obj.getClass() == getClass() && ((Action)obj).modeUsage.equals(modeUsage);
   }

  public int hashCode() {
    return getClass().hashCode() ^ modeUsage.hashCode();
  }
}

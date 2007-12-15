package com.thaiopensource.relaxng.impl;

import java.util.Vector;

class DuplicateAttributeDetector {
  private final Vector nameClasses = new Vector();
  private Alternative alternatives = null;

  private static class Alternative {
    private final int startIndex;
    private int endIndex;
    private final Alternative parent;

    private Alternative(int startIndex, Alternative parent) {
      this.startIndex = startIndex;
      this.endIndex = startIndex;
      this.parent = parent;
    }
  }

  boolean addAttribute(NameClass nc) {
    int lim = nameClasses.size();
    for (Alternative a = alternatives; a != null; a = a.parent) {
      for (int i = a.endIndex; i < lim; i++)
	if (OverlapDetector.overlap(nc, (NameClass)nameClasses.elementAt(i)))
	  return false;
      lim = a.startIndex;
    }
    for (int i = 0; i < lim; i++)
      if (OverlapDetector.overlap(nc, (NameClass)nameClasses.elementAt(i)))
	return false;
    nameClasses.addElement(nc);
    return true;
  }

  void startChoice() {
    alternatives = new Alternative(nameClasses.size(), alternatives);
  }

  void alternative() {
    alternatives.endIndex = nameClasses.size();
  }

  void endChoice() {
    alternatives = alternatives.parent;
  }

}

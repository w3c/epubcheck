package com.thaiopensource.relaxng.impl;


final class PatternInterner {
  private static final int INIT_SIZE = 256;
  private static final float LOAD_FACTOR = 0.3f;
  private Pattern[] table;
  private int used;
  private int usedLimit;

  PatternInterner() {
    table = null;
    used = 0;
    usedLimit = 0;
  }

  PatternInterner(PatternInterner parent) {
    table = parent.table;
    if (table != null)
      table = (Pattern[])table.clone();
    used = parent.used;
    usedLimit = parent.usedLimit;
  }

  Pattern intern(Pattern p) {
    int h;

    if (table == null) {
      table = new Pattern[INIT_SIZE];
      usedLimit = (int)(INIT_SIZE * LOAD_FACTOR);
      h = firstIndex(p);
    }
    else {
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h)) {
	if (p.samePattern(table[h]))
	  return table[h];
      }
    }
    if (used >= usedLimit) {
      // rehash
      Pattern[] oldTable = table;
      table = new Pattern[table.length << 1];
      for (int i = oldTable.length; i > 0;) {
	--i;
	if (oldTable[i] != null) {
	  int j;
	  for (j = firstIndex(oldTable[i]); table[j] != null; j = nextIndex(j))
	    ;
	  table[j] = oldTable[i];
	}
      }
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h))
	;
      usedLimit = (int)(table.length * LOAD_FACTOR);
    }
    used++;
    table[h] = p;
    return p;
  }

  private int firstIndex(Pattern p) {
    return p.patternHashCode() & (table.length - 1);
  }

  private int nextIndex(int i) {
    return i == 0 ? table.length - 1 : i - 1;
  }
}

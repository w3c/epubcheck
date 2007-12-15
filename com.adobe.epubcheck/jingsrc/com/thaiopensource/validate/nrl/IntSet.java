package com.thaiopensource.validate.nrl;

class IntSet {
  static private final int INIT_SIZE = 4;
  private int[] v = null;
  private int len = 0;

  void add(int n) {
    if (v == null) {
      v = new int[INIT_SIZE];
      v[0] = n;
      len = 1;
      return;
    }
    if (len == v.length) {
      int[] newv = new int[len*2];
      System.arraycopy(v, 0, newv, 0, len);
      v = newv;
    }
    if (n > v[len - 1]) {
      v[len++] = n;
      return;
    }
    int i = 0;
    for (; i < len; i++) {
      if (n <= v[i]) {
        if (n == v[i])
          return;
        break;
      }
    }
    for (int j = len; j >= i; j--)
      v[j + 1] = v[j];
    v[i] = n;
    ++len;
  }

  void addAll(IntSet is) {
    if (is.len == 0)
      return;
    int[] newv = new int[len + is.len];
    int i = 0, j = 0, k = 0;
    while (i < len && j < is.len) {
      if (v[i] < is.v[j])
        newv[k++] = v[i++];
      else if (is.v[j] < v[i])
        newv[k++] = is.v[j++];
      else {
        newv[k++] = v[i++];
        j++;
      }
    }
    while (i < len)
      newv[k++] = v[i++];
    while (j < is.len)
      newv[k++] = is.v[j++];
    v = newv;
    len = k;
  }

  int size() {
    return len;
  }

  int get(int i) {
   if (i >= len)
     throw new IndexOutOfBoundsException();
    try {
      return v[i];
    }
    catch (NullPointerException e) {
      throw new IndexOutOfBoundsException();
    }
  }
}

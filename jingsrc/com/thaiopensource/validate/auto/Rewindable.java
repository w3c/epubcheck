package com.thaiopensource.validate.auto;

public interface Rewindable {
  void willNotRewind();
  void rewind();
  boolean canRewind();
}

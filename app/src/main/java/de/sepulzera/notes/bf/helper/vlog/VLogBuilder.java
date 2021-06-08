package de.sepulzera.notes.bf.helper.vlog;

import java.util.ArrayList;
import java.util.List;

public class VLogBuilder {
  public VLogBuilder() {
    this.maxLen = 500;
  }

  public VLogBuilder(int maxLen) {
    this.maxLen = maxLen;
  }

  public void d(String tag, String msg) {
    if (lastEntry == maxLen - 1) {
      lastEntry = 0;
    } else {
      ++lastEntry;
    }

    if (entries.size() == maxLen) {
      entries.set(lastEntry, new VLogEntry(tag, msg));
    } else {
      entries.add(new VLogEntry(tag, msg));
    }
  }

  public List<VLogEntry> getLog() {
    return entries;
  }
  public int getLastEntry() { return lastEntry; }

  public void clear() {
    entries.clear();
    lastEntry = -1;
  }

  public static class VLogEntry {
    public String tag;
    public String msg;

    public VLogEntry(String tag, String msg) {
      this.tag = tag;
      this.msg = msg;
    }
  }

  private final ArrayList<VLogEntry> entries = new ArrayList<>();
  private int lastEntry = -1;
  private final int maxLen;
}

package de.sepulzera.notes.bf.helper.vlog;

import java.util.List;

public class VLog {
  public static void d(String tag, String msg) {
    builder.d(tag, msg);
  }

  public static List<VLogBuilder.VLogEntry> getLog() {
    return builder.getLog();
  }
  public static void clear() { builder.clear(); }

  private final static VLogBuilder builder = new VLogBuilder();
}

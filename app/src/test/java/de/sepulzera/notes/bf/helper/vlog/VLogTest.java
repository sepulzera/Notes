package de.sepulzera.notes.bf.helper.vlog;

import org.junit.Test;

import java.util.List;

import de.sepulzera.notes.bf.helper.DateUtil;

import static org.junit.Assert.assertEquals;

public class VLogTest {
  private static String TAG = "MyTag";

  @Test
  public void simpleTest() {
    String msg = "Some message.";
    VLog.d(TAG, msg);

    final List<VLogBuilder.VLogEntry> entries = VLog.getLog();
    assertEquals(entries.size(), 1);
    assertEquals(entries.get(0).tag, TAG);
    assertEquals(entries.get(0).msg, msg);
  }

  @Test
  public void clearTest() {
    VLog.d(TAG, "Some message 1.");
    VLog.d(TAG, "Some message 2.");
    VLog.d("another tag", "Some message 1.");
    VLog.d("another tag", "Some message 2.");

    final List<VLogBuilder.VLogEntry> entries = VLog.getLog();
    assertEquals(entries.size(), 4);

    VLog.clear();
    assertEquals(entries.size(), 0);
  }
}

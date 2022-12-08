package de.sepulzera.notes.bf.helper.vlog;

import org.junit.Test;

import java.util.List;

import de.sepulzera.notes.bf.helper.DateUtil;

import static org.junit.Assert.assertEquals;

public class VLogBuilderTest {
  private static String TAG = "MyTag";
  private static int MAX_LEN = 5;

  @Test
  public void simpleTest() {
    String msg = "Some message.";

    final VLogBuilder blder = new VLogBuilder(MAX_LEN);
    blder.d(TAG, msg);

    final List<VLogBuilder.VLogEntry> entries = blder.getLog();
    assertEquals(entries.size(), 1);
    assertEquals(blder.getLastEntry(), 0);
    assertEquals(entries.get(0).tag, TAG);
    assertEquals(entries.get(0).msg, msg);
  }

  @Test
  public void clearTest() {
    final VLogBuilder blder = new VLogBuilder(MAX_LEN);

    blder.d(TAG, "Some message 1.");
    blder.d(TAG, "Some message 2.");
    blder.d("another tag", "Some message 1.");
    blder.d("another tag", "Some message 2.");

    final List<VLogBuilder.VLogEntry> entries = blder.getLog();
    assertEquals(entries.size(), 4);
    assertEquals(blder.getLastEntry(), 3);

    blder.clear();
    assertEquals(entries.size(), 0);
    assertEquals(blder.getLastEntry(), -1);
  }

  @Test
  public void overflowTest() {
    final VLogBuilder blder = new VLogBuilder(MAX_LEN);

    blder.d(TAG, "1");
    blder.d(TAG, "2");
    blder.d(TAG, "3");
    blder.d(TAG, "4");
    blder.d(TAG, "5");

    List<VLogBuilder.VLogEntry> entries = blder.getLog();
    assertEquals(entries.size(), 5);
    assertEquals(blder.getLastEntry(), 4);
    assertEquals(entries.get(0).msg, "1");
    assertEquals(entries.get(1).msg, "2");
    assertEquals(entries.get(2).msg, "3");
    assertEquals(entries.get(3).msg, "4");
    assertEquals(entries.get(4).msg, "5");

    blder.d(TAG, "6");
    blder.d(TAG, "7");

    entries = blder.getLog();
    assertEquals(entries.size(), 5);
    assertEquals(blder.getLastEntry(), 1);
    assertEquals(entries.get(0).msg, "6");
    assertEquals(entries.get(1).msg, "7");
    assertEquals(entries.get(2).msg, "3");
    assertEquals(entries.get(3).msg, "4");
    assertEquals(entries.get(4).msg, "5");

    blder.d(TAG, "8");
    blder.d(TAG, "9");
    blder.d(TAG, "10");
    blder.d(TAG, "11");

    entries = blder.getLog();
    assertEquals(entries.size(), 5);
    assertEquals(blder.getLastEntry(), 0);
    assertEquals(entries.get(0).msg, "11");
    assertEquals(entries.get(1).msg, "7");
    assertEquals(entries.get(2).msg, "8");
    assertEquals(entries.get(3).msg, "9");
    assertEquals(entries.get(4).msg, "10");
  }
}

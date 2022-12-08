package de.sepulzera.notes.bf.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class StringUtilTest {

  @Test
  public void abbreviateTest() {
    assertNull(StringUtil.abbreviate(null, 4));
    assertEquals("", StringUtil.abbreviate("", 10));
    assertEquals("abc...", StringUtil.abbreviate("abcdefg", 6));
    assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 7));
    assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 8));
    assertEquals("a...", StringUtil.abbreviate("abcdefg", 4));
  }

  @Test
  public void defaultIfNullTest() {
    final String actualString  = "actual";
    final String defaultString = "default";
    assertEquals(defaultString, StringUtil.defaultIfNull(null, defaultString));
    assertEquals(actualString, StringUtil.defaultIfNull(actualString, defaultString));
    assertNull(StringUtil.defaultIfNull(null, null));
    assertEquals(actualString, StringUtil.defaultIfNull(actualString, null));
  }

  /*
  @Test
  public void getIndexOfLineStartTest() {
    assertEquals("empty string", 0, StringUtil.getIndexOfLineStart("", 0));
    assertEquals("one char", 0, StringUtil.getIndexOfLineStart("x", 0));

    assertEquals("no linebreak", 0, StringUtil.getIndexOfLineStart("Hello World!", 0));
    assertEquals("no linebreak", 0, StringUtil.getIndexOfLineStart("Hello World!", 5));
    assertEquals("no linebreak", 0, StringUtil.getIndexOfLineStart("Hello World!", 12));

    assertEquals("single linebreak, selection before", 0, StringUtil.getIndexOfLineStart("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", 13, StringUtil.getIndexOfLineStart("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection after2", 13, StringUtil.getIndexOfLineStart("Hello World!\n", 13));

    assertEquals("two linebreakes, selection first", 0, StringUtil.getIndexOfLineStart("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", 13, StringUtil.getIndexOfLineStart("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", 28, StringUtil.getIndexOfLineStart("Hello World!\nAnother World!\nA third World!", 35));
  }

  @Test
  public void getIndexOfLineEndTest() {
    assertEquals("empty string", 0, StringUtil.getIndexOfLineEnd("", 0));
    assertEquals("one char", 0, StringUtil.getIndexOfLineEnd("x", 0));

    assertEquals("no linebreak", 11, StringUtil.getIndexOfLineEnd("Hello World!", 0));
    assertEquals("no linebreak", 11, StringUtil.getIndexOfLineEnd("Hello World!", 5));
    assertEquals("no linebreak", 11, StringUtil.getIndexOfLineEnd("Hello World!", 12));

    assertEquals("single linebreak, selection before", 11, StringUtil.getIndexOfLineEnd("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", 26, StringUtil.getIndexOfLineEnd("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection after2", 12, StringUtil.getIndexOfLineEnd("Hello World!\n", 13));

    assertEquals("two linebreakes, selection first", 11, StringUtil.getIndexOfLineEnd("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", 26, StringUtil.getIndexOfLineEnd("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", 41, StringUtil.getIndexOfLineEnd("Hello World!\nAnother World!\nA third World!", 35));
  } */

  @Test
  public void deleteLinesTest() {
    assertEquals("empty string", "", StringUtil.deleteLines("", 0));
    assertEquals("one char", "", StringUtil.deleteLines("x", 0));

    assertEquals("no linebreak", "", StringUtil.deleteLines("Hello World!", 0));
    assertEquals("no linebreak", "", StringUtil.deleteLines("Hello World!", 5));
    assertEquals("no linebreak", "", StringUtil.deleteLines("Hello World!", 12));

    assertEquals("single linebreak, selection before", "Another World!", StringUtil.deleteLines("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", "Hello World!", StringUtil.deleteLines("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection after2", "Hello World!", StringUtil.deleteLines("Hello World!\n", 13));

    assertEquals("two linebreakes, selection first", "Another World!\nA third World!", StringUtil.deleteLines("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", "Hello World!\nA third World!", StringUtil.deleteLines("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", "Hello World!\nAnother World!", StringUtil.deleteLines("Hello World!\nAnother World!\nA third World!", 35));
  }

  @Test
  public void deleteLines_emptyLineTest() {
    assertEquals("first line empty", "Hello World!", StringUtil.deleteLines("\nHello World!", 0));
    assertEquals("middle line empty", "Hello World!\nAnother World!", StringUtil.deleteLines("Hello World!\n\nAnother World!", 13));
    assertEquals("last line empty", "Hello World!", StringUtil.deleteLines("Hello World!\n", 13));

    assertEquals("last line empty", "Hello World!\n\n", StringUtil.deleteLines("Hello World!\n\n\n", 14));
  }

  @Test
  public void deleteLines_span_selectionTest() {
    assertEquals("no linebreak", "", StringUtil.deleteLines("Hello World!", 0, 5));
    assertEquals("no linebreak", "", StringUtil.deleteLines("Hello World!", 5, 12));

    assertEquals("single linebreak, selection before", "Another World!", StringUtil.deleteLines("Hello World!\nAnother World!", 0, "Hello World!".length()));
    assertEquals("single linebreak, selection after", "Hello World!", StringUtil.deleteLines("Hello World!\nAnother World!", "Hello World!".length() + 1, "Hello World!\nAnother World!".length()));

    assertEquals("two linebreakes, selection first and middle", "A third World!", StringUtil.deleteLines("Hello World!\nAnother World!\nA third World!", 5, 20));
    assertEquals("two linebreakes, selection middle and last", "Hello World!", StringUtil.deleteLines("Hello World!\nAnother World!\nA third World!", 20, 35));
  }

  @Test
  public void duplicateLinesTest() {
    assertEquals("empty string", StringUtil.LINE_ENDING, StringUtil.duplicateLines("", 0));
    assertEquals("one char", "x" + StringUtil.LINE_ENDING + "x", StringUtil.duplicateLines("x", 0));

    assertEquals("no linebreak", "Hello World!" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.duplicateLines("Hello World!", 0));
    assertEquals("no linebreak", "Hello World!" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.duplicateLines("Hello World!", 5));
    assertEquals("no linebreak", "Hello World!" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.duplicateLines("Hello World!", 12));

    assertEquals("single linebreak, selection before", "Hello World!" + StringUtil.LINE_ENDING + "Hello World!" + StringUtil.LINE_ENDING + "Another World!", StringUtil.duplicateLines("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", "Hello World!" + StringUtil.LINE_ENDING + "Another World!" + StringUtil.LINE_ENDING + "Another World!", StringUtil.duplicateLines("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection after2", "Hello World!" + StringUtil.LINE_ENDING + StringUtil.LINE_ENDING, StringUtil.duplicateLines("Hello World!\n", 13));

    assertEquals("two linebreakes, selection first", "Hello World!" + StringUtil.LINE_ENDING + "Hello World!" + StringUtil.LINE_ENDING + "Another World!\nA third World!", StringUtil.duplicateLines("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", "Hello World!\nAnother World!" + StringUtil.LINE_ENDING + "Another World!" + "\nA third World!", StringUtil.duplicateLines("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", "Hello World!\nAnother World!\nA third World!" + StringUtil.LINE_ENDING + "A third World!", StringUtil.duplicateLines("Hello World!\nAnother World!\nA third World!", 35));
  }

  @Test
  public void moveLinesUpTest() {
    assertEquals("empty string", "", StringUtil.moveLinesUp("", 0));
    assertEquals("one char", "x", StringUtil.moveLinesUp("x", 0));

    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesUp("Hello World!", 0));
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesUp("Hello World!", 5));
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesUp("Hello World!", 12));

    assertEquals("single linebreak, selection before", "Hello World!\nAnother World!", StringUtil.moveLinesUp("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", "Another World!" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.moveLinesUp("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection after2", "" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.moveLinesUp("Hello World!\n", 13));

    assertEquals("two linebreakes, selection first", "Hello World!\nAnother World!\nA third World!", StringUtil.moveLinesUp("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", "Another World!" + StringUtil.LINE_ENDING + "Hello World!" + StringUtil.LINE_ENDING  + "A third World!", StringUtil.moveLinesUp("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", "Hello World!" + StringUtil.LINE_ENDING + "A third World!" + StringUtil.LINE_ENDING  + "Another World!", StringUtil.moveLinesUp("Hello World!\nAnother World!\nA third World!", 35));
  }

  @Test
  public void moveLinesUp_span_selectionTest() {
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesUp("Hello World!", 5, 12));

    assertEquals("single linebreak, span both", "Hello World!\nAnother World!", StringUtil.moveLinesUp("Hello World!\nAnother World!", 5, 15));

    assertEquals("two linebreakes, selection middle and last", "Another World!" + StringUtil.LINE_ENDING + "A third World!" + StringUtil.LINE_ENDING  + "Hello World!", StringUtil.moveLinesUp("Hello World!\nAnother World!\nA third World!", 20, 35));
  }


  @Test
  public void moveLinesDownTest() {
    assertEquals("empty string", "", StringUtil.moveLinesDown("", 0));
    assertEquals("one char", "x", StringUtil.moveLinesDown("x", 0));

    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesDown("Hello World!", 0));
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesDown("Hello World!", 5));
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesDown("Hello World!", 12));

    assertEquals("single linebreak, selection before", "Another World!" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.moveLinesDown("Hello World!\nAnother World!", 5));
    assertEquals("single linebreak, selection after", "Hello World!\nAnother World!", StringUtil.moveLinesDown("Hello World!\nAnother World!", 20));
    assertEquals("single linebreak, selection before2", "" + StringUtil.LINE_ENDING + "Hello World!", StringUtil.moveLinesDown("Hello World!\n", 12));

    assertEquals("two linebreakes, selection first", "Another World!" + StringUtil.LINE_ENDING + "Hello World!" + StringUtil.LINE_ENDING  + "A third World!", StringUtil.moveLinesDown("Hello World!\nAnother World!\nA third World!", 5));
    assertEquals("two linebreakes, selection middle", "Hello World!" + StringUtil.LINE_ENDING + "A third World!" + StringUtil.LINE_ENDING  + "Another World!", StringUtil.moveLinesDown("Hello World!\nAnother World!\nA third World!", 20));
    assertEquals("two linebreakes, selection last", "Hello World!\nAnother World!\nA third World!", StringUtil.moveLinesDown("Hello World!\nAnother World!\nA third World!", 35));
  }

  @Test
  public void moveLinesDown_span_selectionTest() {
    assertEquals("no linebreak", "Hello World!", StringUtil.moveLinesDown("Hello World!", 5, 12));

    assertEquals("single linebreak, span both", "Hello World!\nAnother World!", StringUtil.moveLinesDown("Hello World!\nAnother World!", 5, 15));

    assertEquals("two linebreakes, selection first and middle", "A third World!" + StringUtil.LINE_ENDING + "Hello World!" + StringUtil.LINE_ENDING  + "Another World!", StringUtil.moveLinesDown("Hello World!\nAnother World!\nA third World!", 5, 20));
  }

  @Test
  public void equalsTest() {
    final String nullStr = null;
    final String emptyStr = "";
    final String blankStr = "      ";
    final String blankStr1 = " ";
    final String filledStr = "abc";
    final String filledStr2 = "ABC";
    final String numStr = "22";

    assertTrue (StringUtil.equals(nullStr   , null));
    assertFalse(StringUtil.equals(nullStr   , emptyStr));
    assertFalse(StringUtil.equals(nullStr   , blankStr));
    assertFalse(StringUtil.equals(nullStr   , filledStr));
    assertFalse(StringUtil.equals(nullStr   , numStr));

    assertTrue (StringUtil.equals(null      , nullStr));
    assertFalse(StringUtil.equals(emptyStr  , nullStr));
    assertFalse(StringUtil.equals(blankStr  , nullStr));
    assertFalse(StringUtil.equals(filledStr , nullStr));
    assertFalse(StringUtil.equals(numStr    , nullStr));

    assertTrue (StringUtil.equals(emptyStr  , ""));
    assertFalse(StringUtil.equals("      "  , emptyStr));
    assertFalse(StringUtil.equals(blankStr  , blankStr1));
    assertFalse(StringUtil.equals(filledStr , filledStr2));

    assertTrue(StringUtil.equals(numStr    , "22"));
  }

  @Test
  public void equalsExceptNullTest() {
    final String nullStr = null;
    final String emptyStr = "";
    final String blankStr = "      ";
    final String blankStr1 = " ";
    final String filledStr = "abc";
    final String filledStr2 = "ABC";
    final String numStr = "22";

    assertFalse(StringUtil.equalsExceptNull(nullStr   , null));
    assertFalse(StringUtil.equalsExceptNull(nullStr   , emptyStr));
    assertFalse(StringUtil.equalsExceptNull(nullStr   , blankStr));
    assertFalse(StringUtil.equalsExceptNull(nullStr   , filledStr));
    assertFalse(StringUtil.equalsExceptNull(nullStr   , numStr));

    assertFalse(StringUtil.equalsExceptNull(null      , nullStr));
    assertFalse(StringUtil.equalsExceptNull(emptyStr  , nullStr));
    assertFalse(StringUtil.equalsExceptNull(blankStr  , nullStr));
    assertFalse(StringUtil.equalsExceptNull(filledStr , nullStr));
    assertFalse(StringUtil.equalsExceptNull(numStr    , nullStr));

    assertTrue (StringUtil.equalsExceptNull(emptyStr  , ""));
    assertFalse(StringUtil.equalsExceptNull("      "  , emptyStr));
    assertFalse(StringUtil.equalsExceptNull(blankStr  , blankStr1));
    assertFalse(StringUtil.equalsExceptNull(filledStr , filledStr2));

    assertTrue (StringUtil.equalsExceptNull(numStr    , "22"));
  }

  @Test
  public void isBlank_Test() {
    final String nullStr = null;
    final String emptyStr = "";
    final String blankStr = "      ";
    final String blankStr1 = " ";
    final String filledStr = "abc";
    final String numStr = "22";

    assertTrue(StringUtil.isBlank(nullStr));
    assertTrue(StringUtil.isBlank(emptyStr));
    assertTrue(StringUtil.isBlank(blankStr));
    assertTrue(StringUtil.isBlank(blankStr1));
    assertFalse(StringUtil.isBlank(filledStr));
    assertFalse(StringUtil.isBlank(numStr));
  }

  @Test
  public void isEmpty_Test() {
    final String nullStr = null;
    final String emptyStr = "";
    final String blankStr = "      ";
    final String blankStr1 = " ";
    final String filledStr = "abc";
    final String numStr = "22";

    assertTrue(StringUtil.isEmpty(nullStr));
    assertTrue(StringUtil.isEmpty(emptyStr));
    assertFalse(StringUtil.isEmpty(blankStr));
    assertFalse(StringUtil.isEmpty(blankStr1));
    assertFalse(StringUtil.isEmpty(filledStr));
    assertFalse(StringUtil.isEmpty(numStr));
  }
}

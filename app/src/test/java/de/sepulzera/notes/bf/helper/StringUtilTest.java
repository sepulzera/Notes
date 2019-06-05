package de.sepulzera.notes.bf.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilTest {

  @Test
  public void abbreviateTest() {
    assertEquals(null, StringUtil.abbreviate(null, 4));
    assertEquals("", StringUtil.abbreviate("", 10));
    assertEquals("abc...", StringUtil.abbreviate("abcdefg", 6));
    assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 7));
    assertEquals("abcdefg", StringUtil.abbreviate("abcdefg", 8));
    assertEquals("a...", StringUtil.abbreviate("abcdefg", 4));
  }

  @Test
  public void defaultIfNull_Test() {
    final String actualString  = "actual";
    final String defaultString = "default";
    assertEquals(defaultString, StringUtil.defaultIfNull(null, defaultString));
    assertEquals(actualString, StringUtil.defaultIfNull(actualString, defaultString));
    assertEquals(null, StringUtil.defaultIfNull(null, null));
    assertEquals(actualString, StringUtil.defaultIfNull(actualString, null));
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

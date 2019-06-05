package de.sepulzera.notes.bf.helper;

import android.support.annotation.Nullable;

public class StringUtil {
  private StringUtil() {
    // Utility class
  }

  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "Now is the time for..."</p>
   *
   * <p>Specifically:
   * <ul>
   *   <li>If <code>str</code> is less than <code>maxWidth</code> characters
   *       long, return it.</li>
   *   <li>Else abbreviate it to <code>(substring(str, 0, max-3) + "...")</code>.</li>
   *   <li>If <code>maxWidth</code> is less than <code>4</code>, throw an
   *       <code>IllegalArgumentException</code>.</li>
   *   <li>In no case will it return a String of length greater than
   *       <code>maxWidth</code>.</li>
   * </ul>
   * </p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *)      = null
   * StringUtils.abbreviate("", 4)        = ""
   * StringUtils.abbreviate("abcdefg", 6) = "abc..."
   * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
   * StringUtils.abbreviate("abcdefg", 4) = "a..."
   * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   */
  public static String abbreviate(String str, int maxWidth) {
    return abbreviate(str, 0, maxWidth);
  }
  /**
   * <p>Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "...is the time for..."</p>
   *
   * <p>Works like <code>abbreviate(String, int)</code>, but allows you to specify
   * a "left edge" offset.  Note that this left edge is not necessarily going to
   * be the leftmost character in the result, or the first character following the
   * ellipses, but it will appear somewhere in the result.
   *
   * <p>In no case will it return a String of length greater than
   * <code>maxWidth</code>.</p>
   *
   * <pre>
   * StringUtils.abbreviate(null, *, *)                = null
   * StringUtils.abbreviate("", 0, 4)                  = ""
   * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
   * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
   * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
   * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
   * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
   * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
   * </pre>
   *
   * @param str  the String to check, may be null
   * @param offset  left edge of source String
   * @param maxWidth  maximum length of result String, must be at least 4
   * @return abbreviated String, <code>null</code> if null String input
   * @throws IllegalArgumentException if the width is too small
   */
  @SuppressWarnings("SameParameterValue")
  private static String abbreviate(String str, int offset, int maxWidth) {
    if (str == null) {
      return null;
    }
    if (maxWidth < 4) {
      throw new IllegalArgumentException("Minimum abbreviation width is 4");
    }
    if (str.length() <= maxWidth) {
      return str;
    }
    if (offset > str.length()) {
      offset = str.length();
    }
    if ((str.length() - offset) < (maxWidth - 3)) {
      offset = str.length() - (maxWidth - 3);
    }
    if (offset <= 4) {
      return str.substring(0, maxWidth - 3) + "...";
    }
    if (maxWidth < 7) {
      throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
    }
    if ((offset + (maxWidth - 3)) < str.length()) {
      return "..." + abbreviate(str.substring(offset), maxWidth - 3);
    }
    return "..." + str.substring(str.length() - (maxWidth - 3));
  }

  /**
   * Gibt den str zurück oder defaultIfNull, falls der str null ist.
   *
   * @param str str
   * @param defaultIfNull Rückgabewert, falls str null.
   *
   * @return str oder defaultIfNull, falls str null.
   */
  public static String defaultIfNull(@Nullable String str, @Nullable String defaultIfNull) {
    return str == null? defaultIfNull : str;
  }

  /**
   * <p>Compares two Strings, returning true if they are equal.</p>
   *
   * <p>{@code null}s are handled without exceptions. Two {@code null}
   * references are considered to be equal. The comparison is case sensitive.</p>
   *
   * <pre>
   * StringUtil.equals(null, null)   = true
   * StringUtil.equals(null, "abc")  = false
   * StringUtil.equals("abc", null)  = false
   * StringUtil.equals("abc", "abc") = true
   * StringUtil.equals("abc", "ABC") = false
   * </pre>
   *
   * @param str1  the first String, may be {@code null}
   * @param str2  the second String, may be {@code null}
   * @return {@code true} if the Strings are equal, case sensitive, or both {@code null}
   */
  public static boolean equals(@Nullable String str1, @Nullable String str2) {
    return str1 == null ? str2 == null : str1.equals(str2);
  }

  /**
   * <p>Compares two Strings, returning true if they are equal.</p>
   *
   * <p>{@code null}s are handled without exceptions. Two {@code null}
   * references are considered to be <strong>different</strong>. The comparison is case sensitive.</p>
   *
   * <pre>
   * StringUtil.equals(null, null)   = false
   * StringUtil.equals(null, "abc")  = false
   * StringUtil.equals("abc", null)  = false
   * StringUtil.equals("abc", "abc") = true
   * StringUtil.equals("abc", "ABC") = false
   * </pre>
   *
   * @param str1  the first String, may be {@code null}
   * @param str2  the second String, may be {@code null}
   * @return {@code true} if the Strings are equal and not {@code null}, case sensitive
   */
  public static boolean equalsExceptNull(@Nullable String str1, @Nullable String str2) {
    return str1 != null && str2 != null && str1.equals(str2);
  }

  /**
   * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
   *
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
   * </pre>
   *
   * @param cs The CharSequence to check, may be null
   *
   * @return {@code true} if the CharSequence is null, empty or whitespace
   *
   * @see <a href="commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html">Apache Lang3 StringUtils</a>
   */
  public static boolean isBlank(@Nullable final CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * <p>Checks if a CharSequence is empty ("") or null.</p>
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * @param cs the CharSequence to check, may be null.
   *
   * @return {@code true} if the CharSequence is empty or null.
   *
   * @see <a href="commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html">Apache Lang3 StringUtils</a>
   */
  public static boolean isEmpty(@Nullable final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }
}

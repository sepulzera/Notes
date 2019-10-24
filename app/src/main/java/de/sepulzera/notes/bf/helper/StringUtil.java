package de.sepulzera.notes.bf.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtil {
  public final static String LINE_ENDING = "\n";

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



  /**
   * <p>Removes the selected line from a string.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to remove the line from.
   * @param pos Index of the line to be removed.
   *
   * @return str without the selected line.
   *
   * @see StringUtil#deleteLines(String str, int selectionStart, int selectionEnd)
   */
  public static String deleteLines(@NonNull String str, int pos) {
    return deleteLines(str, pos, pos);
  }

  /**
   * <p>Removes the selected line(s) from a string.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to remove the line from.
   * @param selectionStart Beginning of the selection, which line(s) should be removed.
   * @param selectionEnd End of the selection.
   *
   * @return str without the selected line(s).
   *
   * @see StringUtil#deleteLines(String str, int pos)
   */
  public static String deleteLines(@NonNull String str, int selectionStart, int selectionEnd) {
    if (isEmpty(str)) return "";

    List<String> lines = getLines(str);
    int[] selLines = getSelectedLines(str, selectionStart, selectionEnd);

    for (int i = selLines.length - 1; i >= 0; --i) {
      lines.remove(selLines[i]);
    }

    return toString(lines);
  }

  public static List<String> getLines(@NonNull String str) {
    List<String> lines = new ArrayList<>(Arrays.asList(str.split(LINE_ENDING)));

    if (str.charAt(str.length() - 1) == '\n') {
      lines.add("");
    }

    return lines;
  }

  private static String toString(@NonNull List<String> lines) {
    StringBuilder blder = new StringBuilder();

    boolean first = true;
    for (String s : lines) {
      if (first) {
        first = false;
      } else {
        blder.append(LINE_ENDING);
      }
      blder.append(s);
    }

    return blder.toString();
  }

  public static int[] getSelectedLines(@NonNull String str, int selectionStart, int selectionEnd) {
    int selStart = selectionStart;
    int selEnd = selectionEnd;

    if (selStart > selEnd) {
      int swap = selStart;
      selStart = selEnd;
      selEnd = swap;
    }

    int strLen = str.length();
    if (selStart > strLen || selEnd > strLen) {
      throw new IndexOutOfBoundsException("selectionStart and selectionEnd may not be larger than the length of the given string");
    }

    List<Integer> selLines = new ArrayList<>();

    int lineCounter = 0;
    for (int i = 0; i < selStart; ++i) {
      if ('\n' == str.charAt(i)) {
        ++lineCounter;
      }
    }
    selLines.add(lineCounter);
    for (int i = selStart; i < selEnd; ++i) {
      if ('\n' == str.charAt(i)) {
        selLines.add(++lineCounter);
      }
    }

    return toIntArray(selLines);
  }

  private static int[] toIntArray(@NonNull List<Integer> intList) {

    // Java8
    // return intList.stream().mapToInt( i -> i).toArray();

    int[] intArray = new int[intList.size()];
    for (int i = 0; i < intList.size(); ++i) {
      intArray[i] = intList.get(i);
    }
    return intArray;
  }

  /**
   * <p>Duplicates the selected line from a string.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to duplicate the line from.
   * @param pos Index of the line to be duplicated.
   *
   * @return str with duplicated selected line.
   *
   * @see StringUtil#duplicateLines(String str, int selectionStart, int selectionEnd)
   */
  public static String duplicateLines(@NonNull String str, int pos) {
    return duplicateLines(str, pos, pos);
  }

  /**
   * <p>Duplicates the selected line(s) from a string.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to duplicate the line from.
   * @param selectionStart Beginning of the selection, which line(s) should be duplicated.
   * @param selectionEnd End of the selection.
   *
   * @return str with duplicated selected line(s).
   *
   * @see StringUtil#duplicateLines(String str, int pos)
   */
  public static String duplicateLines(@NonNull String str, int selectionStart, int selectionEnd) {
    if (isEmpty(str)) return LINE_ENDING;

    List<String> lines = getLines(str);
    int[] selLines = getSelectedLines(str, selectionStart, selectionEnd);

    int lastSelectedLine = selLines[selLines.length - 1];

    for (int i = 0; i < selLines.length; ++i) {
      lines.add(lastSelectedLine + 1 + i, lines.get(selLines[i]));
    }

    return toString(lines);
  }

  /*
  public static int getIndexOfLineStart(@NonNull String str, int pos) {
    int strLen = str.length();
    int selStart;
    if (pos < 0) {
      selStart = 0;
    } else if (strLen > 0 && pos > strLen) {
      selStart = strLen;
    } else {
      selStart = pos;
    }

    int lineStart = str.substring(0, selStart).lastIndexOf(LINE_ENDING);
    return lineStart < 0? 0 : lineStart + 1;
  }

  public static int getIndexOfLineEnd(@NonNull String str, int pos) {
    int strLen = str.length();

    if (strLen == 0) {
      return 0;
    }

    int selEnd;
    if (pos < 0) {
      selEnd = 0;
    } else if (pos > strLen) {
      selEnd = strLen;
    } else {
      selEnd = pos;
    }

    int lineEnd = str.indexOf(LINE_ENDING, selEnd);
    return lineEnd < 0? strLen - 1 : lineEnd - 1;
  } */

  /**
   * <p>Moves the selected lines from a string up by one.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to move the line up from.
   * @param pos Index of the line to be moved up to.
   *
   * @return str with moved up selected line(s).
   *
   * @see StringUtil#moveLinesUp(String str, int selectionStart, int selectionEnd)
   */
  public static String moveLinesUp(@NonNull String str, int pos) {
    return moveLinesUp(str, pos, pos);
  }

  /**
   * <p>Moves the selected line(s) from a string up by one.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to move the line up from.
   * @param selectionStart Beginning of the selection, which line(s) should be moved up.
   * @param selectionEnd End of the selection.
   *
   * @return str with moved up selected line(s).
   *
   * @see StringUtil#moveLinesUp(String str, int pos)
   */
  public static String moveLinesUp(@NonNull String str, int selectionStart, int selectionEnd) {
    if (isEmpty(str)) return "";

    List<String> lines = getLines(str);
    int[] selLines = getSelectedLines(str, selectionStart, selectionEnd);

    int firstSelectedLine = selLines[0];
    if (firstSelectedLine == 0) return str;

    // move the previous line behind the selection
    // is basically the same as moving the whole selection up
    String moveLine = lines.remove(firstSelectedLine - 1);
    lines.add(selLines[selLines.length - 1], moveLine);

    return toString(lines);
  }

  /**
   * <p>Moves the selected lines from a string down by one.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to move the line down from.
   * @param pos Index of the line to be moved down to.
   *
   * @return str with moved down selected line(s).
   *
   * @see StringUtil#moveLinesDown(String str, int selectionStart, int selectionEnd)
   */
  public static String moveLinesDown(@NonNull String str, int pos) {
    return moveLinesDown(str, pos, pos);
  }

  /**
   * <p>Moves the selected line(s) from a string down by one.</p>
   * <p>A line is a substring delimited by {@link StringUtil#LINE_ENDING}s.
   *    Lines do not have to have delimiters. In that case, the whole prior
   *    or post substring is part of that line (e.g. beginning or end).</p>
   *
   * @param str String to move the line down from.
   * @param selectionStart Beginning of the selection, which line(s) should be moved down.
   * @param selectionEnd End of the selection.
   *
   * @return str with moved down selected line(s).
   *
   * @see StringUtil#moveLinesDown(String str, int pos)
   */
  public static String moveLinesDown(@NonNull String str, int selectionStart, int selectionEnd) {
    if (isEmpty(str)) return "";

    List<String> lines = getLines(str);
    int[] selLines = getSelectedLines(str, selectionStart, selectionEnd);

    int lastSelectedLine = selLines[selLines.length - 1];
    if (lastSelectedLine == (lines.size() - 1)) return str;

    // move the next line before the selection
    // is basically the same as moving the whole selection down
    String moveLine = lines.remove(lastSelectedLine + 1);
    lines.add(selLines[0], moveLine);

    return toString(lines);
  }

  private StringUtil() {
    // Utility class
  }
}

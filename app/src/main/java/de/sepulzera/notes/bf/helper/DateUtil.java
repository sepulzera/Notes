package de.sepulzera.notes.bf.helper;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.sepulzera.notes.R;

@SuppressWarnings("WeakerAccess")
public class DateUtil {
  @NonNull public static DateFormat getDefaultDateFormatter() { return df_iso8601; }

  /**
   * Compares two dates.
   * {@code null}-values are treated as "earliest day of our galaxy".
   *
   * @param date1 First date. May be null.
   * @param date2 Second date. May be null.
   *
   * @return {@code 0}, if both dates are equal.
   *         {@code < 0}, if date1 is before date2.
   *         {@code > 0}, if date1 is after date2.
   */
  public static int compare(@Nullable final Date date1, @Nullable final Date date2) {
    if (date1 == null) {
      if (date2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (date2 == null) {
      return 1;
    } else {
      return date1.compareTo(date2);
    }
  }

  /**
   * Compares two dates.
   * {@code null}-values are treated as equal.
   *
   * @param date1 First date. May be null.
   * @param date2 Second date. May be null.
   *
   * @return True: date1 is equal to date2 or both dates are {@code null} - False: else.
   */
  @SuppressWarnings("unused")
  public static boolean equals(@Nullable final Date date1, @Nullable final Date date2) {
    if (date1 == null) {
      return date2 == null;
    }
    return date2 != null && date1.compareTo(date2) == 0;
  }

  /**
   * Formats the given date as ISO-8601.
   *
   * @param date ...
   *
   * @return The date as ISO-8601, or empty string if {@code date} is {@code null}.
   */
  public static String toISO8601(@Nullable final Date date) {
    return (date == null) ? "" : df_iso8601.format(date);
  }

  /**
   * <p>Prints in a very fashioned way, how far the date {@code compareDate} comes before {@code now}.</p>
   * <p>
   * <table>
   *   <caption>Input and output, if compare to Mo 13, 12:00.</caption>
   *   <tr>
   *     <th>compareDate</th>
   *     <th>Return value</th>
   *   </tr>
   *   <tr>
   *     <td>today, 11:59:30</td>
   *     <td>Now</td>
   *   </tr>
   *   <tr>
   *     <td>today, 11:59</td>
   *     <td>1 minute</td>
   *   </tr>
   *   <tr>
   *     <td>today, 10:00</td>
   *     <td>2 hours</td>
   *   </tr>
   *   <tr>
   *     <td>yesterday, 23:00</td>
   *     <td>13 hours</td>
   *   </tr>
   *   <tr>
   *     <td>day before yesterday, 23:00</td>
   *     <td>Sa 11</td>
   *   </tr>
   * </table></p>
   *
   *
   * @param now Date-Instance that is now to compare with.
   * @param compareDate The date to calculated the elapsed time for display.
   *
   * @return Elapsed time from {@code compareDate} to {@code now}.
   */
  public static String formatDatePastShort(@Nullable final Date now, @Nullable final Date compareDate) {
    if (null == compareDate) {
      return "";
    }

    final Date nowNotNull = (null == now)? Calendar.getInstance().getTime() : now;

    if (compare(compareDate, nowNotNull) > 0) {
      // comparedate after nowNotNull
      return "";
    }

    long diff = getDateDiff(compareDate, nowNotNull, TimeUnit.MINUTES);
    if (diff < 5) {
      return POST_NOW;
    } else if (diff < 60) {
      return diff + POST_MINUTE;
    } else if (isWithinDay(compareDate, nowNotNull)) {
      return getDateDiff(compareDate, nowNotNull, TimeUnit.HOURS) + POST_HOUR;
    } else if (isWithinWeek(compareDate, nowNotNull)) {
      // is within a week -> display day only ("Mo")
      return df_day.format(compareDate);
    } else if (isWithinYear(compareDate, nowNotNull)) {
      // is within a year -> display month and day ("Feb 02")
      return df_monthDay.format(compareDate);
    } else {
      // past one year
      return df_yearMonth.format(compareDate);
    }
  }

  /**
   * <p>Get a diff between two dates.</p>
   *
   * @param dateBefore The oldest date.
   * @param dateAfter The newest date.
   * @param timeUnit The unit in which you want the diff.
   *
   * @return The diff value, in the provided unit.
   */
  public static long getDateDiff(Date dateBefore, Date dateAfter, TimeUnit timeUnit) {
    long diffInMillies = dateAfter.getTime() - dateBefore.getTime();
    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
  }

  public static boolean isWithinDay(Date dateBefore, Date dateAfter) {
    return getDateDiff(dateBefore, dateAfter, TimeUnit.HOURS) < 24;
  }

  public static boolean isWithinWeek(Date dateBefore, Date dateAfter) {
    return getDateDiff(dateBefore, dateAfter, TimeUnit.DAYS) < 7;
  }

  public static boolean isWithinYear(final Date dateBefore, final Date dateAfter) {
    return getDateDiff(dateBefore, dateAfter, TimeUnit.DAYS) < 365;
  }

  /**
   * Parse the given date-string to an actual date.
   *
   * The string should be iso8601.
   *
   * @param date Date-string to parse. Should be iso8601. May be null.
   *
   * @return Date or null if the given strnig was null/empty or could not be parsed.
   *
   * @see #toISO8601(Date)
   */
  public static Date parseDate(@Nullable String date) {
    if (StringUtil.isEmpty(date)) {
      return null;
    }

    try {
      return df_iso8601.parse(date);
    } catch (ParseException e) {
      Log.d("Error", e.toString());
      return null;
    }
  }

  /**
   * Needs to be called as initialization to provide proper localization.
   *
   * @param context ...
   */
  public static void localize(@NonNull Context context) {
    POST_NOW     = context.getResources().getString(R.string.post_now);
    POST_MINUTE  = context.getResources().getString(R.string.post_minute);
    POST_HOUR    = context.getResources().getString(R.string.post_hour);

    df_iso8601   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
    df_day       = new SimpleDateFormat("EEE", Locale.getDefault());
    df_monthDay  = new SimpleDateFormat("MMM dd", Locale.getDefault());
    df_yearMonth = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
  }

  private DateUtil() {
    // Utility class
  }

  private static DateFormat df_iso8601   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
  private static DateFormat df_day       = new SimpleDateFormat("EEE", Locale.getDefault());
  private static DateFormat df_monthDay  = new SimpleDateFormat("MMM dd", Locale.getDefault());
  private static DateFormat df_yearMonth = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

  private static       String     POST_NOW;
  private static       String     POST_MINUTE;
  private static       String     POST_HOUR;
}

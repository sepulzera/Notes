package de.sepulzera.notes.bf.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
   * Vergleicht die beiden Daten.
   * {@code null}-Werte werden wie "frühestes Datum der Galaxy" behandelt.
   *
   * @param date1 Erstes Datum. Darf null sein.
   * @param date2 Zweites Datum. Darf null sein.
   *
   * @return {@code 0}, wenn sie gleich sind.
   *         {@code < 0}, wenn date1 vor date2 ist.
   *         {@code > 0}, wenn date1 nach date2 ist.
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
   * Prüft, ob date1 der gleiche DateTime-Stamp wie date2 ist.
   * Prüft nicht, ob es die gleiche Objekt-Referenz ist.
   * {@code null} wird als einheitlicher Wert betrachtet, also beide {@code null} = wahr.
   *
   * @param date1 Date1.
   * @param date2 Date2.
   *
   * @return date1 und date2 gleich {@code null} oder {@code date1.compareTo(date2) == 0}
   */
  @SuppressWarnings("unused")
  public static boolean equals(@Nullable final Date date1, @Nullable final Date date2) {
    if (date1 == null) {
      return date2 == null;
    }
    return date2 != null && date1.compareTo(date2) == 0;
  }

  /**
   * Formatiert das übergebene Datum nach ISO-8601.
   * Eignet sich für die normierte Übergabe als Parameter (z. B. an Web services).
   * Hinweis: Sollte nicht für UI-Ausgabe verwendet werden.
   *
   * @param date Das zu formatierende Datum. Darf null sein.
   *
   * @return Das formatierte Datum oder leer, falls date == null.
   */
  public static String formatDate(@Nullable final Date date) {
    return (date == null)? "" : df_iso8601.format(date);
  }

  /**
   * <p>Gibt fancy und kurz zurück, wie lange das {@code compareDate} hinter {@code now} zurück liegt </p>
   * <p>
   * <table>
   *   <caption>Ein- und Ausgabe, wenn heute Mo 13, 12:00 ist:</caption>
   *   <tr>
   *     <th>Eingabe</th>
   *     <th>Ausgabe</th>
   *   </tr>
   *   <tr>
   *     <td>heute, 11:59:30</td>
   *     <td>Jetzt</td>
   *   </tr>
   *   <tr>
   *     <td>heute, 11:59</td>
   *     <td>1 Minute</td>
   *   </tr>
   *   <tr>
   *     <td>heute, 10:00</td>
   *     <td>2 Stunden</td>
   *   </tr>
   *   <tr>
   *     <td>gestern, 23:00</td>
   *     <td>13 Stunden</td>
   *   </tr>
   *   <tr>
   *     <td>vorgestern, 23:00</td>
   *     <td>Sa 11</td>
   *   </tr>
   * </table></p>
   *
   *
   * @param now Referenz-Jetzt.
   * @param compareDate Zeit in der Vergangenheit, die ausgegeben werden soll.
   *
   * @return Vergangene Zeit von {@code compareDate} seit {@code now}.
   */
  public static String formatDatePastShort(@Nullable final Date now, @Nullable final Date compareDate) {
    if (null == compareDate) {
      return "";
    }

    final Date nowNotNull = (null == now)? Calendar.getInstance().getTime() : now;

    if (compare(compareDate, nowNotNull) > 0) {
      // comparedate nach nowNotNull
      return "";
    }

    long diff = getDateDiff(compareDate, nowNotNull, TimeUnit.MINUTES);
    if (diff < 5) {
      return POST_NOW;
    } else if (diff < 60) {
      return String.valueOf(diff) + POST_MINUTE;
    } else if (isWithinDay(compareDate, nowNotNull)) {
      return String.valueOf(getDateDiff(compareDate, nowNotNull, TimeUnit.HOURS)) + POST_HOUR;
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
   * @param dateBefore the oldest date
   * @param dateAfter the newest date
   * @param timeUnit the unit in which you want the diff
   *
   * @return the diff value, in the provided unit
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
   * Versucht das Datum nach Schema x zu parsen.
   * Hinweis: Sollte nicht für UI-Ausgabe verwendet werden.
   *
   * @param date Das zu parsende Datum. Darf null sein.
   *
   * @return Das geparste Datum oder null, falls date null/leer oder Fehler.
   *
   * @see #formatDate(Date)
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
   * Lokalisiert die Anwendung.
   *
   * @param context Context.
   */
  public static void localize(@NonNull Context context) {
    POST_NOW     = context.getResources().getString(R.string.post_now);
    POST_MINUTE  = context.getResources().getString(R.string.post_minute);
    POST_HOUR    = context.getResources().getString(R.string.post_hour);
  }

  private DateUtil() {
    // Utility class
  }

  private static final DateFormat df_iso8601   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
  private static final DateFormat df_day       = new SimpleDateFormat("EEE", Locale.getDefault());
  private static final DateFormat df_monthDay  = new SimpleDateFormat("MMM dd", Locale.getDefault());
  private static final DateFormat df_yearMonth = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

  private static       String     POST_NOW;
  private static       String     POST_MINUTE;
  private static       String     POST_HOUR;
}

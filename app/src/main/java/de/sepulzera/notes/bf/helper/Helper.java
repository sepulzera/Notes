package de.sepulzera.notes.bf.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ui.activity.note.MainActivity;
import de.sepulzera.notes.ui.activity.note.NoteTabViewerActivity;

@SuppressWarnings("WeakerAccess")
public class Helper {
  /**
   *  Checks if external storage is available for read and write
   */
  public static boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  /**
   *  Checks if external storage is available to at least read
   */
  public static boolean isExternalStorageReadable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state) ||
        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
  }

  /**
   * Lokalisiert die Anwendung.
   *
   * @param context Context.
   */
  public static void localize(@NonNull final Context context) {
    DateUtil.localize(context);

    NoteServiceImpl.createInstance(context);
  }


  public static String makePlaceholders(int len) {
    if (len < 1) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder(len * 2 - 1);
      sb.append("?");
      for (int i = 1; i < len; i++) {
        sb.append(",?");
      }
      return sb.toString();
    }
  }

  public static String readFile(@NonNull final Context context, @NonNull final Uri uri) {
    try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
      if (inputStream == null) {
        return null;
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          inputStream));
      StringBuilder blder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        blder.append(line);
        blder.append('\n');
      }

      return blder.toString();
    } catch (IOException e) {
      Log.d("notes", "Exception while reading a file: " + e.getMessage());
      return null;
    }
  }

  /**
   * <p>Schreibt den angegebenene Inhalt in die Datei.
   * Die Datei wird im {@link Context#MODE_PRIVATE} geöffnet, also ggf. überschrieben.</p>
   * <p>Wirf eine {@link IllegalArgumentException}, falls die Datei nicht zum Schreiben geöffnet werden konnte.</p>
   *
   * @param path File pathname.
   * @param content Der zu schreibende Inhalt.
   * @param doReplace Replace file if exists?
   */
  public static void writeFile(@NonNull final String path, @NonNull String content, boolean doReplace) {
    File file = new File(path);

    if (doReplace && file.exists()) {
      // vorherigen save aufräumen
      if (!file.delete()) {
        throw new IllegalArgumentException("file could not been deleted!");
      }
    }

    FileOutputStream fos = null;
    try {
      // Datei öffnen
      fos =  new FileOutputStream(file);
      fos.write(content.getBytes(Charset.forName(mUtf8)));
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          Log.e("Error", e.toString());
        }
      }
    }
  }

  /**
   * Gibt die Preferenz zum angegebenen key als boolean zurück.
   *
   * @param context Kontext.
   * @param key Key.
   * @param defaultValue Value, falls key nicht gefunden oder kein boolean.
   *
   * @return Preference zum Key oder defaultValue, falls {@code null} oder kein boolean.
   */
  public static boolean getPreferenceAsBool(@NonNull final Context context, @NonNull String key, boolean defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    try {
      return preferences.getBoolean(key, defaultValue);
    } catch (ClassCastException e) {
      return Boolean.parseBoolean(preferences.getString(key, String.valueOf(defaultValue)));
    }
  }

  /**
   * Gibt die Preferenz zum angegebenen key als int zurück.
   *
   * @param context Kontext.
   * @param key Key.
   * @param defaultValue Value, falls key nicht gefunden oder kein int.
   *
   * @return Preference zum Key oder defaultValue, falls {@code null} oder kein int.
   */
  public static int getPreferenceAsInt(@NonNull final Context context, @NonNull String key, int defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    try {
      return preferences.getInt(key, defaultValue);
    } catch (ClassCastException e) {
      return Integer.parseInt(Objects.requireNonNull(preferences.getString(key, String.valueOf(defaultValue))));
    }
  }

  /**
   * Gibt die Preferenz zum angegebenen key als long zurück.
   *
   * @param context Kontext.
   * @param key Key.
   * @param defaultValue Value, falls key nicht gefunden oder kein long.
   *
   * @return Preference zum Key oder defaultValue, falls {@code null} oder kein long.
   */
  public static long getPreferenceAsLong(@NonNull final Context context, @NonNull String key, long defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getLong(key, defaultValue);
  }

  /**
   * Setzt die Preferenz zum angegebene Key.
   * Ggf. alte Werte werden überschrieben.
   *
   * @param context Kontext.
   * @param key Key der Preferenz.
   * @param value Neuer Wert der Preferenz.
   */
  public static void putPreference(@NonNull final Context context, @NonNull String key, final long value) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit().putLong(key, value).apply();
  }

  public static void updatePreferences(@NonNull final Context context) {
    setNightMode(context);

    MainActivity.readPreferences(context);
    NoteTabViewerActivity.readPreferences(context);

    NoteService srv = NoteServiceImpl.getInstance();
    srv.readPreferences(context);
  }

  private static void setNightMode(@NonNull final Context context) {
    setNightMode(Helper.getPreferenceAsInt(context, context.getResources().getString(R.string.PREF_DAY_NIGHT_MODE_KEY)
        , Integer.parseInt(context.getResources().getString(R.string.pref_day_night_mode_default))));
  }

  public static void setNightMode(int mode) {
    switch (mode) {
      case 0:  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
      case 1:  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
      default: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
    }
  }

  public static void dailyTask(@NonNull final Context context) {
    long dailyTaskTick = Helper.getPreferenceAsLong(context, PREF_DAILY_TASK_TICK_KEY, 0L);
    if (0L == dailyTaskTick) {
      Helper.putPreference(context, PREF_DAILY_TASK_TICK_KEY, Calendar.getInstance().getTime().getTime());
    } else if (!DateUtil.isWithinDay(new Date(dailyTaskTick), Calendar.getInstance().getTime())) {
      Helper.putPreference(context, PREF_DAILY_TASK_TICK_KEY, Calendar.getInstance().getTime().getTime());
      NoteService srv = NoteServiceImpl.getInstance();
      srv.wipeTrash();
    }
  }

  public static Intent createShareIntent(@NonNull String title, @NonNull String body) {
    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT , title);
    intent.putExtra(android.content.Intent.EXTRA_TEXT    , body);
    return intent;
  }

  private static final String mUtf8 = "UTF-8";

  private static final String PREF_DAILY_TASK_TICK_KEY = "dailyTaskTick";
}

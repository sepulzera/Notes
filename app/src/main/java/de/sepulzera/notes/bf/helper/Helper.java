package de.sepulzera.notes.bf.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
   * Checks if external storage is available for read and write.
   */
  public static boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  /**
   * Checks if external storage is available for read.
   */
  public static boolean isExternalStorageReadable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state) ||
        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
  }

  /**
   * Needs to be called as initialization to provide proper localization.
   *
   * @param context Context.
   */
  public static void localize(@NonNull final Context context) {
    DateUtil.localize(context);

    NoteServiceImpl.createInstance(context);
  }

  /**
   * Creates {@code amount} number of placeholders for query-selections.
   *
   * @param amount Number of placeholders.
   *
   * @return 0: "(empty string)"; 1: "?"; 2: "?, ?" ...
   */
  public static String makePlaceholders(int amount) {
    if (amount < 1) {
      return "";
    } else {
      StringBuilder sb = new StringBuilder(amount * 2 - 1);
      sb.append("?");
      for (int i = 1; i < amount; i++) {
        sb.append(",?");
      }
      return sb.toString();
    }
  }

  /**
   * Reads the file from disk.
   *
   * @param context ...
   * @param uri ...
   *
   * @return Content of the file or null on error.
   */
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
   * Writes the {@code content} into the given file on disk. Will overwrite existing content.
   *
   * @param path File pathname (URI).
   * @param content ...
   *
   * @throws IllegalArgumentException File could not be opened (access error, permissions error).
   */
  public static void writeFile(@NonNull final String path, @NonNull String content) {
    File file = new File(path);

    if (file.exists()) {
      // clean up the previous file
      if (!file.delete()) {
        throw new IllegalArgumentException("file could not been deleted!");
      }
    }

    FileOutputStream fos = null;
    try {
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

  public static void writeFile(@NonNull final Context context, @NonNull final Uri uri, @NonNull String content) {
    try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "rw")) {
      if (outputStream == null) {
        return;
      }
      try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
        writer.write(content);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    } catch (IOException e) {
      Log.d("notes", "Exception while writing a file: " + e.getMessage());
    }
  }

  /**
   * Returns the preference as boolean.
   *
   * @param context ...
   * @param key Key of the preference.
   * @param defaultValue Returned if the preference was not found or it's value is no boolean.
   *
   * @return Preference's value of {@code key} or {@code defaultValue} if preference is missing oder no boolean.
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
   * Returns the preference as integer.
   *
   * @param context ...
   * @param key Key of the preference.
   * @param defaultValue Returned if the preference was not found or it's value is no integer.
   *
   * @return Preference's value of {@code key} or {@code defaultValue} if preference is missing oder no integer.
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
   * Returns the preference as Long.
   *
   * @param context ...
   * @param key Key of the preference.
   * @param defaultValue Returned if the preference was not found or it's value is no Long.
   *
   * @return Preference's value of {@code key} or {@code defaultValue} if preference is missing oder no Long.
   */
  public static long getPreferenceAsLong(@NonNull final Context context, @NonNull String key, long defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getLong(key, defaultValue);
  }

  /**
   * Stores the {@code value} to the preference with the given {@code key}.
   *
   * @param context ...
   * @param key Key of the preference.
   * @param value New value for the preference.
   */
  public static void putPreference(@NonNull final Context context, @NonNull String key, final long value) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.edit().putLong(key, value).apply();
  }

  /**
   * Manually triggers the activities to update their preference-based values.
   *
   * Should be called after finishing to update the settings, e. g. after closing the SettingsActivity.
   *
   * @param context ...
   */
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

  /**
   * Toggles the displayed night mode.
   *
   * Should be called after the user changed the associated preference.
   *
   * @param mode {@code AppCompatDelegate.MODE_NIGHT_NO}, {@code AppCompatDelegate.MODE_NIGHT_YES} or {@code AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM}
   */
  public static void setNightMode(int mode) {
    int systemMode;
    switch (mode) {
      case 0:  systemMode = AppCompatDelegate.MODE_NIGHT_NO;  break;
      case 1:  systemMode = AppCompatDelegate.MODE_NIGHT_YES; break;
      default: systemMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
    }

    if (systemMode != AppCompatDelegate.getDefaultNightMode()) {
      AppCompatDelegate.setDefaultNightMode(systemMode);
    }
  }

  /**
   * Runs the daily tasks.
   *
   * Can be called more than once per day, but will only be executed once (seriously).
   *
   * @param context ...
   */
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

  /**
   * Creates the share intent and passes {@code title} and {@code body}.
   *
   * The user can then pick their app of choice to share the data with, e. g. Signal Messenger or SMS.
   *
   * @param title Subject of the shared message.
   * @param body Text of the shared message.
   *
   * @return Intent to call startActivity with.
   */
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

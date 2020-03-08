package de.sepulzera.notes.bf.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Hilfsmethoden für Operationen auf JSON-Objekten.
 */
public class JsonUtil {
  /**
   * Gibt den JSON-Wert des Tags als boolean zurück.
   * Gibt defaultIfNotFound zurück, wenn der Tag nicht existiert.
   *
   * @param json JSON
   * @param key Der gesuchte Tag.
   * @param defaultIfNotFound Rückgabewert, falls Tag nicht vorhanden.
   *
   * @return Wert des Tags als boolean oder defaultIfNotFound, falls der Tag nicht vorhanden ist.
   */
  public static boolean getBoolD(@NonNull final JSONObject json, @NonNull String key, final boolean defaultIfNotFound) {
    if (!json.isNull(key)) {
      return json.optBoolean(key, defaultIfNotFound);
    }
    return defaultIfNotFound;
  }

  /**
   * Gibt den JSON-Wert des Tags als Date zurück.
   * Gibt defaultIfNotFound zurück, wenn der Tag nicht existiert, oder nicht geparst werden konnte.
   *
   * @param json JSON
   * @param key Der gesuchte Tag.
   * @param defaultIfNotFound Rückgabewert, falls Tag nicht vorhanden.
   *
   * @return Wert des Tags als Date oder defaultIfNotFound, falls der Tag nicht vorhanden ist.
   *
   * @throws IllegalArgumentException Falls Tag gefunden, aber mit df nicht parsable.
   */
  public static Date getDateD(@NonNull final JSONObject json, @NonNull String key, @Nullable final Date defaultIfNotFound) {
    if (!json.isNull(key)) {
      try {
        return DateUtil.parseDate(json.getString(key));
      } catch (JSONException e) {
        return defaultIfNotFound;
      }
    }
    // unboxing trouble
    return defaultIfNotFound;
  }

  /**
   * Gibt den JSON-Wert des Tags als long zurück.
   * Gibt defaultIfNotFound zurück, wenn der Tag nicht existiert (und nur dann!).
   *
   * @param json JSON
   * @param tag Der gesuchte Tag.
   * @param defaultIfNotFound Rückgabewert, falls Tag nicht vorhanden.
   *
   * @return Wert des Tags oder defaultIfNotFound, falls der Tag nicht vorhanden ist.
   */
  public static Long getLongD(@NonNull final JSONObject json, @NonNull String tag, final Long defaultIfNotFound) {
    if (!json.isNull(tag)) {
      try {
        return json.getLong(tag);
      } catch (JSONException e) {
        return defaultIfNotFound;
      }
    }
    return defaultIfNotFound;
  }

  /**
   * Gibt den JSON-Wert des Tags als String zurück.
   * Gibt defaultIfNotFound zurück, wenn der Tag nicht existiert (und nur dann!).
   *
   * @param json JSON
   * @param tag Der gesuchte Tag.
   * @param defaultIfNotFound Rückgabewert, falls Tag nicht vorhanden.
   *
   * @return Wert des Tags oder defaultIfNotFound, falls der Tag nicht vorhanden ist.
   */
  public static String getStringD(@NonNull final JSONObject json, @NonNull String tag, @Nullable String defaultIfNotFound) {
    if (!json.isNull(tag)) {
      return json.optString(tag, defaultIfNotFound);
    }
    return defaultIfNotFound;
  }

  /**
   * Fügt das Key-Value-Paar in das JSON-Objekt ein, falls value nicht null ist.
   *
   * @param json JSON-Objekt, in das das Key-Value-Paar gespeichert werden soll. Darf nicht null sein.
   * @param key Key. Darf nicht null sein.
   * @param value Wert.
   */
  public static void putBool(@NonNull final JSONObject json, @NonNull String key, final boolean value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Der Bool-Wert \"" + value + "\" konnte nicht zum JSON-Schlüssel \"" + key + "\" gespeichert werden!", e);
    }
  }

  /**
   * Fügt das Key-Value-Paar in das JSON-Objekt ein, falls value nicht null ist.
   *
   * @param json JSON-Objekt, in das das Key-Value-Paar gespeichert werden soll. Darf nicht null sein.
   * @param key Key. Darf null sein.
   * @param value Datum. Darf null sein.
   */
  public static void putDateIfPresent(@NonNull final JSONObject json, @Nullable String key, @Nullable final Date value) {
    if (!StringUtil.isBlank(key) && value != null) {
      try {
        json.put(key, DateUtil.formatDate(value));
      } catch (JSONException e) {
        throw new IllegalArgumentException("Der Wert \"" + value + "\" konnte nicht zum JSON-Schlüssel \"" + key + "\" gespeichert werden!", e);
      }
    }
  }

  /**
   * Fügt das Key-Value-Paar in das JSON-Objekt ein, falls value nicht null ist.
   *
   * @param json JSON-Objekt, in das das Key-Value-Paar gespeichert werden soll. Darf nicht null sein.
   * @param key Key. Darf null sein.
   * @param value Wert.
   */
  public static void putLong(@NonNull final JSONObject json, @Nullable String key, final long value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Der Long-Wert \"" + value + "\" konnte nicht zum JSON-Schlüssel \"" + key + "\" gespeichert werden!", e);
    }
  }

  /**
   * Fügt das Key-Value-Paar in das JSON-Objekt ein, falls value nicht null ist.
   *
   * @param json JSON-Objekt, in das das Key-Value-Paar gespeichert werden soll. Darf nicht null sein.
   * @param key Key. Darf null sein.
   * @param value Wert. Darf null sein.
   */
  public static void putStringIfPresent(@NonNull final JSONObject json, @Nullable String key, @Nullable String value) {
    if (!StringUtil.isBlank(key) && !StringUtil.isEmpty(value)) {
      try {
        json.put(key, value);
      } catch (JSONException e) {
        throw new IllegalArgumentException("Der Wert \"" + value + "\" konnte nicht zum JSON-Schlüssel \"" + key + "\" gespeichert werden!", e);
      }
    }
  }

  private JsonUtil() {
    // Utility class
  }
}

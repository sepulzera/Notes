package de.sepulzera.notes.bf.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Utility functions for programming with JSON.
 */
public class JsonUtil {
  /**
   * Returns the {@code boolean} for the given {@code key}.
   * Returns {@code defaultIfNotFound} if there is no value for the given {@code key}.
   *
   * @param json JSON.
   * @param key Key of value.
   * @param defaultIfNotFound Returned if {@code json} has not any value for {@code key}.
   *
   * @return Value of {@code key} or {@code defaultIfNotFound} if {@code key} is missing.
   */
  public static boolean getBoolD(@NonNull final JSONObject json, @NonNull String key, final boolean defaultIfNotFound) {
    if (!json.isNull(key)) {
      return json.optBoolean(key, defaultIfNotFound);
    }
    return defaultIfNotFound;
  }

  /**
   * Returns the {@code date} for the given {@code key}.
   * Returns {@code defaultIfNotFound} if there is no value for the given {@code key}.
   *
   * @param json JSON.
   * @param key Key of value.
   * @param defaultIfNotFound Returned if {@code json} has not any value for {@code key}.
   *
   * @return Value of {@code key} or {@code defaultIfNotFound} if {@code key} is missing.
   *
   * @throws IllegalArgumentException If the value for {@code key} could not be parsed as date.
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
   * Returns the {@code Long} for the given {@code key}.
   * Returns {@code defaultIfNotFound} if there is no value for the given {@code key}.
   *
   * @param json JSON.
   * @param key Key of value.
   * @param defaultIfNotFound Returned if {@code json} has not any value for {@code key}.
   *
   * @return Value of {@code key} or {@code defaultIfNotFound} if {@code key} is missing.
   */
  public static Long getLongD(@NonNull final JSONObject json, @NonNull String key, final Long defaultIfNotFound) {
    if (!json.isNull(key)) {
      try {
        return json.getLong(key);
      } catch (JSONException e) {
        return defaultIfNotFound;
      }
    }
    return defaultIfNotFound;
  }

  /**
   * Returns the {@code String} for the given {@code key}.
   * Returns {@code defaultIfNotFound} if there is no value for the given {@code key}.
   *
   * @param json ...
   * @param key Key of value.
   * @param defaultIfNotFound Returned if {@code json} has not any value for {@code key}.
   *
   * @return Value of {@code key} or {@code defaultIfNotFound} if {@code key} is missing.
   */
  public static String getStringD(@NonNull final JSONObject json, @NonNull String key, @NonNull String defaultIfNotFound) {
    if (!json.isNull(key)) {
      return json.optString(key, defaultIfNotFound);
    }
    return defaultIfNotFound;
  }

  /**
   * Stores into the {@code json} for the given {@code key} the given {@code value}.
   *
   * @param json JSON.
   * @param key Key for value.
   * @param value Value to store.
   *
   * @throws IllegalArgumentException {@code value} could not be stored into json for some reason.
   */
  public static void putBool(@NonNull final JSONObject json, @NonNull String key, final boolean value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new IllegalArgumentException("The boolean \"" + value + "\" could not be stored to the JSON-key \"" + key + "\".", e);
    }
  }

  /**
   * Stores into the {@code json} for the given {@code key} the given {@code value}.
   * If no value is given (null or empty), then no entry will be created (no-op).
   *
   * @param json JSON.
   * @param key Key for value.
   * @param value Value to store.
   *
   * @throws IllegalArgumentException {@code value} could not be stored into json for some reason.
   */
  public static void putDateIfPresent(@NonNull final JSONObject json, @NonNull String key, @Nullable final Date value) {
    if (!StringUtil.isBlank(key) && value != null) {
      try {
        json.put(key, DateUtil.toISO8601(value));
      } catch (JSONException e) {
        throw new IllegalArgumentException("The date \"" + DateUtil.toISO8601(value) + "\" could not be stored to the JSON-key \"" + key + "\".", e);
      }
    }
  }

  /**
   * Stores into the {@code json} for the given {@code key} the given {@code value}.
   *
   * @param json JSON.
   * @param key Key for value.
   * @param value Value to store.
   *
   * @throws IllegalArgumentException {@code value} could not be stored into json for some reason.
   */
  public static void putLong(@NonNull final JSONObject json, @NonNull String key, final long value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new IllegalArgumentException("The Long \"" + value + "\" could not be stored to the JSON-key \"" + key + "\".", e);
    }
  }

  /**
   * Stores into the {@code json} for the given {@code key} the given {@code value}.
   * If no value is given (null or empty), then no entry will be created (no-op).
   *
   * @param json JSON.
   * @param key Key for value.
   * @param value Value to store.
   *
   * @throws IllegalArgumentException {@code value} could not be stored into json for some reason.
   */
  public static void putStringIfPresent(@NonNull final JSONObject json, @NonNull String key, @Nullable String value) {
    if (!StringUtil.isBlank(key) && !StringUtil.isEmpty(value)) {
      try {
        json.put(key, value);
      } catch (JSONException e) {
        throw new IllegalArgumentException("The String \"" + value + "\" could not be stored to the JSON-key \"" + key + "\".", e);
      }
    }
  }

  private JsonUtil() {
    // Utility class
  }
}

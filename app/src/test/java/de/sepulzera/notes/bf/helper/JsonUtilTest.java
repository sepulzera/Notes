package de.sepulzera.notes.bf.helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("NonAsciiCharacters") // false warning
@RunWith(RobolectricTestRunner.class)
public class JsonUtilTest {
  @Test
  public void putBoolean_Test() {
    final String nonNullKey = "myKey";

    final JSONObject json = new JSONObject();

    JsonUtil.putBool(json, nonNullKey, true);
    assertTrue(json.has(nonNullKey));

    boolean ret = false;
    try {
      ret = json.getBoolean(nonNullKey);
    } catch (JSONException e) {
      // do nothing
    }

    assertTrue(ret);
  }

  @Test
  public void putDate_Test() {
    final Date nuĺlDate = null;
    final Date nonNullDate = GregorianCalendar.getInstance().getTime();

    final String nullKey = null;
    final String nonNullKey = "myKey";

    final JSONObject json = new JSONObject();

    JsonUtil.putDateIfPresent(json, nullKey, nuĺlDate);
    assertTrue(!json.has(nullKey));

    JsonUtil.putDateIfPresent(json, nullKey, nonNullDate);
    assertTrue(!json.has(nullKey));

    JsonUtil.putDateIfPresent(json, nonNullKey, nuĺlDate);
    assertTrue(!json.has(nonNullKey));

    JsonUtil.putDateIfPresent(json, nonNullKey, nonNullDate);
    assertTrue(json.has(nonNullKey));
  }

  @Test
  public void putLongTest() {
    final String nuĺlStr = null;
    final Long lng0 = 0L;
    final long lng999 = 999L;
    final Long longMinus500 = -500L;

    final String nullKey = null;
    final String lng0Key = "lng0Key";
    final String lng999Key = "lng999Key";
    final String longMinus500Key = "longMinus500Key";

    final JSONObject json = new JSONObject();

    JsonUtil.putStringIfPresent(json, nullKey, nuĺlStr);
    assertTrue(!json.has(nullKey));

    JsonUtil.putLong(json, lng0Key, lng0);
    assertTrue(json.has(lng0Key));

    JsonUtil.putLong(json, lng999Key, lng999);
    assertTrue(json.has(lng999Key));

    JsonUtil.putLong(json, longMinus500Key, longMinus500);
    assertTrue(json.has(longMinus500Key));
  }

  @Test
  public void putString_Test() {
    final String nuĺlStr = null;
    final String emptyStr = "";
    final String blankStr = " ";
    final String filledStr = "xx";

    final String nullKey = null;
    final String emptyStrKey = "emptyKey";
    final String blankStrKey = "blankKey";
    final String filledStrKey = "filledKey";

    final JSONObject json = new JSONObject();

    JsonUtil.putStringIfPresent(json, nullKey, nuĺlStr);
    assertTrue(!json.has(nullKey));

    JsonUtil.putStringIfPresent(json, filledStrKey, nuĺlStr);
    assertTrue(!json.has(filledStrKey));

    JsonUtil.putStringIfPresent(json, nullKey, filledStr);
    assertTrue(!json.has(nullKey));

    JsonUtil.putStringIfPresent(json, filledStrKey, filledStr);
    assertTrue(json.has(filledStrKey));

    JsonUtil.putStringIfPresent(json, emptyStrKey, emptyStr);
    assertTrue(!json.has(emptyStrKey));

    JsonUtil.putStringIfPresent(json, blankStrKey, blankStr);
    assertTrue(json.has(blankStrKey));
  }
}

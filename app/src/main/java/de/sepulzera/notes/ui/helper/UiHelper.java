package de.sepulzera.notes.ui.helper;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class UiHelper {
  /**
   * Hides the Soft-Keyboard.
   *
   * @param view ...
   * @param activity ...
   * @param context ...
   */
  public static void hideKeyboard(final View view, final Activity activity, final Context context) {
    if (view != null && activity != null && context != null) {
      InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      if (null != imm) {
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
      }
    }
  }

  private UiHelper() {
    // utility class
  }
}

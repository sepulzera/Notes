package de.sepulzera.notes.ui.activity.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;

public class SettingsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings, new SettingsFragment())
        .commit();
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorPrimaryBar)));
    }
  }

  @Override
  public void onBackPressed() {
    setResult(Activity.RESULT_OK, new Intent());
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);

      Preference pref = findPreference(getString(R.string.PREF_DAY_NIGHT_MODE_KEY));
      if (pref != null) {
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
              if (newValue instanceof String) {
                String newValueS = newValue.toString();
                Helper.setNightMode(Integer.parseInt(newValueS));
                getActivity().recreate();
              }
            }
            return true;
          }
        });
      }
    }
  }
}
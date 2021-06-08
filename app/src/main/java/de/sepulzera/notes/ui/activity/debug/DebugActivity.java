package de.sepulzera.notes.ui.activity.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.helper.vlog.VLog;
import de.sepulzera.notes.bf.helper.vlog.VLogBuilder;
import de.sepulzera.notes.ui.widgets.EditTextSelectable;

import android.text.Editable;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class DebugActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_debug);

    // SETUP APPLICATION

    if (null != savedInstanceState) {
      restoreState(savedInstanceState);
    } else {
      createState();
    }

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();
    if (ab == null) {
      throw new IllegalStateException("ActionBar not found!");
    }
    ab.setDisplayHomeAsUpEnabled(true);

    // SETUP VIEWS

    mMainView = findViewById(R.id.scrollView);

    mDebugMsg = mMainView.findViewById(R.id.debug_log);
    initializeDebugMsg();
  }

  private void createState() {
    // nothing to do
  }

  private void restoreState(@NonNull final Bundle outState) {
    // nothing to do
  }

  @Override
  public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);

    // nothing to do
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_debug, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      case R.id.om_debug_copy:
        copyToClipboard();
        Snackbar.make(mMainView, getResources().getString(R.string.copied_to_clipboard), Snackbar.LENGTH_LONG).show();
        return true;

      case R.id.om_debug_share:
        Intent intent = Helper.createShareIntent("Notes Debug Log", getMsg());
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void copyToClipboard() {
    ClipboardManager cman = ((ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE));
    if (cman != null) {
      cman.setPrimaryClip(
          ClipData.newPlainText("notes_debug_log", getMsg()));
    }
  }

  public void initializeDebugMsg() {
    mDebugMsg.setShowSoftInputOnFocus(false);
    mDebugMsg.setCustomSelectionActionModeCallback(new CustomSelectionActionModeCallback());
    mDebugMsg.setCustomInsertionActionModeCallback(new CustomInsertionActionModeCallback());
    mDebugMsg.setOnDragListener(new DragListener());

    List<VLogBuilder.VLogEntry> log = VLog.getLog();
    StringBuilder blder = new StringBuilder();
    for (VLogBuilder.VLogEntry entry : log) {
      blder.append("[");
      blder.append(entry.tag);
      blder.append("] ");
      blder.append(entry.msg);
      blder.append("\n");
    }
    mDebugMsg.setText(blder.toString());

    mMainView.setBackgroundColor(getResources().getColor(R.color.colorNoteBgReadonly, null));
  }

  public String getMsg() {
    @Nullable Editable text = mDebugMsg.getText();
    return text == null ? "" : text.toString();
  }

  protected static class CustomSelectionActionModeCallback implements ActionMode.Callback {

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      try {
        MenuItem copyItem = menu.findItem(android.R.id.copy);
        CharSequence title = copyItem.getTitle();
        menu.clear();
        menu.add(0, android.R.id.copy, 0, title);
      }
      catch (Exception e) {
        // ignored
      }
      return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
  }

  protected static class CustomInsertionActionModeCallback implements ActionMode.Callback {

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
  }

  protected static class DragListener implements View.OnDragListener {
    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
      return true;
    }
  }

  private NestedScrollView mMainView;
  private EditTextSelectable mDebugMsg;
}
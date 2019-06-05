package de.sepulzera.notes.ui.activity.note;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.sepulzera.notes.R;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.helper.UiHelper;

public class NoteViewDeletedActivity extends AppCompatActivity {
  private Note     mNote;
  private View     mView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_note_view_deleted);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab == null) {
      throw new IllegalStateException("ActionBar not found!");
    }
    ab.setDisplayHomeAsUpEnabled(true);

    mView = findViewById(R.id.main_content);

    // übergebene Notiz einlesen
    final Intent intent = getIntent();
    if (null == intent) {
      throw new IllegalStateException("intent must not be null");
    }
    final Bundle extras = intent.getExtras();
    if (null == extras) {
      throw new IllegalStateException("extras must not be null");
    }
    mNote = (Note) intent.getExtras().getSerializable(Note.TAG_NOTE);
    if (mNote == null) {
      throw new IllegalArgumentException("note must not be null!");
    }

    // set ActionBar title
    setTitle(mNote.getTitle());

    final TextView editMsg = findViewById(R.id.note_msg);
    editMsg.setText(mNote.getMsg());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_note_view_deleted, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      case R.id.om_detail_note_delete:
        deleteNote();
        return true;

      case R.id.om_note_restore:
        restoreNote();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void deleteNote() {
    // yes = delete
    mNote.setCurr(false);
    executeDone();
  }

  private void restoreNote() {
    // yes = delete
    mNote.setCurr(true);
    executeDone();
  }

  private void executeDone() {
    UiHelper.hideKeyboard(mView, this, getApplicationContext());
    setResult(Activity.RESULT_OK, new Intent()
        .putExtra(Note.TAG_NOTE, mNote));
    finish();
  }
}

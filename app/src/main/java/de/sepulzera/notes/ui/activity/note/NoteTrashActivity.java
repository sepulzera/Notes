package de.sepulzera.notes.ui.activity.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.adapter.NoteAdapter;
import de.sepulzera.notes.ui.adapter.impl.NoteTrashAdapterImpl;

public class NoteTrashActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_note_trash);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();
    if (ab == null) {
      throw new IllegalStateException("ActionBar not found!");
    }
    ab.setDisplayHomeAsUpEnabled(true);

    mAdapter = new NoteTrashAdapterImpl(this);

    // Layout-Elemente suchen
    mMainView = findViewById(R.id.mainListView);
    mMainView.setNestedScrollingEnabled(true);
    mMainView.setEmptyView(findViewById(R.id.empty_text));
    mMainView.setAdapter(mAdapter);

    // Handler registrieren
    mMainView.setOnItemClickListener(this); // Single-Click: Notiz bearbeiten
    registerForContextMenu(mMainView); // Kontextmenü registrieren

    // gespeicherten Zustand wiederherstellen
    refreshList();

    mHandler = new Handler();
    mRunRefreshUi = new Runnable() {
      @Override
      public void run() {
        mAdapter.updateView();
        mHandler.postDelayed( this, 60 * MainActivity.mListRefreshInterval * 1000 );
      }
    };
    mHandler.postDelayed(mRunRefreshUi, 60 * MainActivity.mListRefreshInterval * 1000 );
  }

  @Override
  public void onPause() {
    super.onPause();
    mHandler.removeCallbacks(mRunRefreshUi);
  }

  @Override
  public void onResume() {
    super.onResume();
    mAdapter.updateView();
    mHandler.postDelayed(mRunRefreshUi, 60 * MainActivity.mListRefreshInterval * 1000 );
  }

  @Override
  public void onBackPressed() {
    if (mRestoredNotesCount > 0) {
      if (mRestoreNote != null) {
        final NoteService srv = NoteServiceImpl.getInstance();
        srv.restore(mRestoreNote);
        mRestoreNote = null;
      }
      setResult(Activity.RESULT_OK, new Intent());
      finish();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    final Note note = (Note)mAdapter.getItem(position);
    startActivityViewNote(note);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view,
                                  ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);
    getMenuInflater().inflate(R.menu.cm_note_trash, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    final Note note = (Note) mAdapter.getItem(info.position);

    switch (item.getItemId()) {
      case R.id.cm_delete_note:
        deleteNote(note);
        return true;

      case R.id.cm_restore_note:
        doRestore(note);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_note_trash, menu);

    MenuItem item;
    if ((item = menu.findItem(R.id.om_trash_empty)) != null) { item.setVisible(mAdapter.getSize() > 0); }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      case R.id.om_trash_empty:
        empty();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void empty() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getResources().getString(R.string.dialog_empty_trash_title))
        .setMessage(R.string.dialog_empty_trash_msg)
        .setPositiveButton(getResources().getString(R.string.dialog_empty_trash_go), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // yes = delete
            final NoteService srv = NoteServiceImpl.getInstance();
            final List<Note> deletedNotes = srv.getAllDeleted();
            for (final Note note : deletedNotes) {
              // Note+Draft -> evtl. inzwischen mit gelöscht
              if (srv.get(note.getId()) != null) {
                srv.delete(note);
              }
            }

            mAdapter.refresh();
            invalidateOptionsMenu();
            Snackbar.make(mMainView, getResources().getString(R.string.snack_trash_emptied)
                , Snackbar.LENGTH_LONG).show();
          }
        })
        .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // no = abort
          }
        }).show();
  }

  private void deleteNote(@NonNull final Note note) {
    if (note.getDraft()) {
      doDelete(note);
    } else {

      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getResources().getString(R.string.dialog_trash_delete_note_title))
          .setMessage(String.format(getResources().getString(R.string.dialog_trash_delete_note_msg), note.getTitle()))
          .setPositiveButton(getResources().getString(R.string.dialog_trash_delete_note_go_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // yes = delete
              doDelete(note);
            }
          })
          .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // no = abort
            }
          }).show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      switch (requestCode) {

        case RQ_VIEW_NOTE_ACTION:
          if (!data.hasExtra(Note.TAG_NOTE)) {
            break;
          }
          final Note note = (Note)(data.getSerializableExtra(Note.TAG_NOTE));
          if (null == note) {
            throw new IllegalArgumentException("note must not be null");
          }

          if (!note.getCurr()) {
            deleteNote(note);
          } else {
            doRestore(note);
          }
          break;

        default: // unbekannter requestCode -> ignorieren
          break;
      }
    }
  }

  private void doDelete(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();
    srv.delete(note);
    mAdapter.refresh();

    fixAppBarInvisible();
    invalidateOptionsMenu();

    Snackbar.make(mMainView, String.format(getResources().getString(R.string.snack_note_deleted)
        , note.getTitle()), Snackbar.LENGTH_LONG).show();
  }

  private void doRestore(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();

    final Note revision = note.getDraft() ? srv.getCurrRevision(note) : note;
    final Note draft    = note.getDraft() ? note : srv.getDraft(note);

    // restore will restore both, the draft and the note.
    // -> remove note and eventually draft from list.
    if (revision != null) { mAdapter.remove(revision); }
    if (draft != null)    { mAdapter.remove(draft);    }
    ++mRestoredNotesCount;
    mRestoreNote = note;

    fixAppBarInvisible();
    invalidateOptionsMenu();

    final Snackbar snack = Snackbar.make(mMainView, String.format(getResources().getString(R.string.snack_note_restored)
        , note.getTitle()), Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_undo, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        --mRestoredNotesCount;
        mRestoreNote = null;
        // undo -> add note again
        // -> if the draft was restored, add the note again, too
        if (revision != null) { mAdapter.put(revision); }
        if (draft != null)    { mAdapter.put(draft);    }
      }
    });
    snack.addCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        if (event != DISMISS_EVENT_ACTION && mRestoreNote != null) {
          srv.restore(mRestoreNote);
          mRestoreNote = null;
        }
      }
    });
    snack.show();
  }

  private void fixAppBarInvisible() {
    if (mAdapter.isEmpty()) {
      // list empty and appbar faded out -> bring it back
      // (thought it should do it automatically, but it doesn't)
      AppBarLayout appbar = findViewById(R.id.appbar);
      if (null != appbar) {
        appbar.setExpanded(true);
      }
    }
  }

  /**
   * Startet die Activity zum Bearbeiten der übergebenen Notiz.
   * Die Notiz wird mit startActivityForResult aufgerufen.
   *
   * @param note Zu bearbeitende Notiz
   */
  private void startActivityViewNote(Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();
    this.startActivityForResult(new Intent(this, NoteViewDeletedActivity.class)
        .putExtra(Note.TAG_NOTE, srv.clone(note))
        , RQ_VIEW_NOTE_ACTION);
  }

  private void refreshList() {
    mAdapter.clear();
    final NoteService srv = NoteServiceImpl.getInstance();
    for (final Note note : srv.getAllDeleted()) {
      mAdapter.put(note);
    }
  }

  private Handler        mHandler;
  private Runnable       mRunRefreshUi;

  private NoteAdapter    mAdapter; // Adapter zu den Notiz-ListItems
  private ListView       mMainView;

  private int            mRestoredNotesCount = 0;
  private Note           mRestoreNote;

  private static final int RQ_VIEW_NOTE_ACTION = 54011; // Single click (View)
}
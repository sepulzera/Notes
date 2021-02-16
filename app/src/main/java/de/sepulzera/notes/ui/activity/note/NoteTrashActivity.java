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

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
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

    // Layout-Elemente suchen
    mMainView = findViewById(R.id.mainListView);
    mMainView.setNestedScrollingEnabled(true);
    mMainView.setEmptyView(findViewById(R.id.empty_text));
    mMainView.setAdapter(mAdapter);

    /* CONTEXTUAL ACTION BAR */

    mMainView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

    mMainView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
      private int nr = 0;

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
        mAdapter.clearSelection();
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        nr = 0;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cab_trash, menu);
        return true;
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<Note> checkedItems = mAdapter.getCheckedItems();
        if (checkedItems.size() == 0) return true;

        switch (item.getItemId()) {
          case R.id.cm_delete:
            deleteNotes(checkedItems);
            break;

          case R.id.cm_restore:
            doRestore(checkedItems);
            break;

        }

        invalidateOptionsMenu();

        mAdapter.clearSelection();
        nr = 0;

        mode.finish();
        return true;
      }

      @Override
      public void onItemCheckedStateChanged(ActionMode mode, int position,
                                            long id, boolean checked) {
        if (checked) {
          nr++;
          mAdapter.setNewSelection(position);
        } else {
          nr--;
          mAdapter.removeSelection(position);
        }
        mode.setTitle(String.valueOf(nr));

        MenuItem item;
        if ((item = mode.getMenu().findItem(R.id.cm_rename)) != null) { item.setVisible(nr == 1); }
        if ((item = mode.getMenu().findItem(R.id.cm_share))  != null) { item.setVisible(nr == 1); }
      }
    });

    mMainView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                     int position, long arg3) {
        mMainView.setItemChecked(position, !mAdapter.isPositionChecked(position));
        return false;
      }
    });

    /* /CONTEXTUAL ACTION BAR */

    // Handler registrieren
    mMainView.setOnItemClickListener(this); // Single-Click: Notiz bearbeiten

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

  private void createState() {
    mAdapter = new NoteTrashAdapterImpl(this);

    mEmptyTrashRequested = false;
    mRestoredNotesCount = 0;

    // gespeicherten Zustand wiederherstellen
    refreshList();
  }

  private void restoreState(@NonNull final Bundle outState) {
    mAdapter = new NoteTrashAdapterImpl(this);

    // gespeicherten Zustand wiederherstellen
    refreshList();

    mRestoredNotesCount  = outState.getInt(KEY_RESTORED_COUNT);
  }

  @Override
  public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(KEY_RESTORED_COUNT , mRestoredNotesCount);
  }

  @Override
  public void onPause() {
    super.onPause();
    finishPendlingActions();
    mHandler.removeCallbacks(mRunRefreshUi);
  }

  private void finishPendlingActions() {
    final NoteService srv = NoteServiceImpl.getInstance();

    finishDelete(srv);
    finishEmptyTrash(srv);
    finishRestore(srv);
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
      setResult(Activity.RESULT_OK, new Intent()
          .putExtra(MainActivity.RQ_EXTRA_INVALIDATE_LIST, true));
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
            mEmptyTrashRequested = true;

            mAdapter.clear();
            fixAppBarInvisible();
            invalidateOptionsMenu();

            final NoteService srv = NoteServiceImpl.getInstance();

            final Snackbar snack = Snackbar.make(mMainView, getResources().getString(R.string.snack_trash_emptied)
                , Snackbar.LENGTH_LONG);
            snack.setAction(R.string.snack_undo, new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                // restore note again
                mEmptyTrashRequested = false;
                refreshList();
                invalidateOptionsMenu();
              }
            });
            snack.addCallback(new Snackbar.Callback() {
              @Override
              public void onDismissed(Snackbar snackbar, int event) {
                if (event != DISMISS_EVENT_ACTION) {
                  finishEmptyTrash(srv);
                }
              }
            });
            snack.show();
          }
        })
        .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // no = aborts
          }
        }).show();
  }

  private void deleteNotes(@NonNull final List<Note> notes) {
    if (notes.size() < 1) throw new IllegalArgumentException("notes may not be empty");

    int numOfNotes = 0;
    for (Note note : notes) {
      if (!note.getDraft()) {
        ++numOfNotes;
      }
    }

    if (numOfNotes == 0) {
      doDelete(notes);
    } else {
      String msg = notes.size() == 1
          ? String.format(getResources().getString(R.string.dialog_trash_delete_note_msg), notes.get(0).getTitle())
          : String.format(getResources().getString(R.string.dialog_trash_delete_notes_msg), notes.size());

      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getResources().getString(R.string.dialog_trash_delete_note_title))
          .setMessage(msg)
          .setPositiveButton(getResources().getString(R.string.dialog_trash_delete_note_go_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // yes = delete
              doDelete(notes);
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
          if (data.hasExtra(Note.TAG_NOTE)) {
            final Note note = (Note)(data.getSerializableExtra(Note.TAG_NOTE));
            if (null == note) {
              throw new IllegalArgumentException("note must not be null");
            }

            List<Note> notesList = new ArrayList<>(1);
            notesList.add(note);

            if (!note.getCurr()) {
              deleteNotes(notesList);
            } else {
              doRestore(notesList);
            }
          }
          if (data.hasExtra(MainActivity.RQ_EXTRA_INVALIDATE_LIST)) {
            ++mRestoredNotesCount;
          }
          break;

        default: // unbekannter requestCode -> ignorieren
          break;
      }
    }
  }

  private void doDelete(@NonNull final List<Note> notes) {
    if (notes.size() < 1) throw new IllegalArgumentException("notes may not be empty");
    final NoteService srv = NoteServiceImpl.getInstance();

    // Execute previous delete, if any
    finishDelete(srv);

    // Remove note and eventually draft from list.
    for (Note nextNote : notes) {
      mAdapter.remove(nextNote);
      if (!nextNote.getDraft()) {
        final Note draft = srv.getDraft(nextNote);
        if (draft != null) {
          mAdapter.remove(draft);
        }
      }
    }
    mDeleteNotes = notes;
    fixAppBarInvisible();
    invalidateOptionsMenu();

    String msg = notes.size() == 1
        ? String.format(getResources().getString(R.string.snack_note_deleted), notes.get(0).getTitle())
        : String.format(getResources().getString(R.string.snack_notes_deleted), notes.size());
    final Snackbar snack = Snackbar.make(mMainView, msg, Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_undo, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // restore note again
        mDeleteNotes = null;
        for (Note nextNote : notes) {
          mAdapter.put(nextNote);
          if (!nextNote.getDraft()) {
            final Note draft = srv.getDraft(nextNote);
            if (draft != null) {
              mAdapter.put(draft);
            }
          }
        }
        invalidateOptionsMenu();
      }
    });
    snack.addCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        if (event != DISMISS_EVENT_ACTION) {
          finishDelete(srv);
        }
      }
    });
    snack.show();
  }

  private void doRestore(@NonNull final List<Note> notes) {
    if (notes.size() < 1) throw new IllegalArgumentException("notes may not be empty");
    final NoteService srv = NoteServiceImpl.getInstance();

    // Execute previous restore, if any
    finishRestore(srv);

    final List<Note> deletedDraftsAndRevisions = new ArrayList<>();
    for (Note nextNote : notes) {
      final Note revision = nextNote.getDraft() ? srv.getCurrRevision(nextNote) : nextNote;
      final Note draft    = nextNote.getDraft() ? nextNote : srv.getDraft(nextNote);

      // restore will restore both, the draft and the note.
      // -> remove note and eventually draft from list.
      if (revision != null) {
        mAdapter.remove(revision);
        deletedDraftsAndRevisions.add(revision);
      }
      if (draft != null) {
        mAdapter.remove(draft);
        deletedDraftsAndRevisions.add(draft);
      }
      ++mRestoredNotesCount;
    }
    mRestoreNotes = notes;

    fixAppBarInvisible();
    invalidateOptionsMenu();

    String msg = notes.size() == 1
        ? String.format(getResources().getString(R.string.snack_note_restored), notes.get(0).getTitle())
        : String.format(getResources().getString(R.string.snack_notes_restored), notes.size());
    final Snackbar snack = Snackbar.make(mMainView, msg, Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_undo, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        --mRestoredNotesCount;
        mRestoreNotes = null;
        for (Note nextNote : notes) {
          nextNote.setCurr(false);
        }

        for (Note nextDraft : deletedDraftsAndRevisions) {
          // undo -> add note again
          // -> if the draft was restored, add the note again, too
          mAdapter.put(nextDraft);
        }
        invalidateOptionsMenu();
      }
    });
    snack.addCallback(new Snackbar.Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        if (event != DISMISS_EVENT_ACTION) {
          finishRestore(srv);
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
    this.startActivityForResult(new Intent(this, NoteTabViewerActivity.class)
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

  private void finishDelete(@NonNull NoteService srv) {
    if (mDeleteNotes != null) {
      for (Note note : mDeleteNotes) {
        srv.delete(note);
        mDeleteNotes = null;
      }
    }
  }

  private void finishEmptyTrash(@NonNull NoteService srv) {
    if (mEmptyTrashRequested) {
      final List<Note> deletedNotes = srv.getAllDeleted();
      for (final Note note : deletedNotes) {
        // Note+Draft -> evtl. inzwischen mit gelöscht
        if (srv.get(note.getId()) != null) {
          srv.delete(note);
        }
      }
      mEmptyTrashRequested = false;
    }
  }

  private void finishRestore(@NonNull NoteService srv) {
    if (mRestoreNotes != null) {
      for (Note note : mRestoreNotes) {
        srv.restore(note);
      }
      mRestoreNotes = null;
    }
  }

  private Handler        mHandler;
  private Runnable       mRunRefreshUi;

  private NoteAdapter    mAdapter; // Adapter zu den Notiz-ListItems
  private ListView       mMainView;

  private List<Note>     mDeleteNotes;
  private boolean        mEmptyTrashRequested;
  private List<Note>     mRestoreNotes;
  private int            mRestoredNotesCount;

  private static final String KEY_RESTORED_COUNT          = "notetrashactivity_restoredcount";

  private static final int RQ_VIEW_NOTE_ACTION = 54011; // Single click (View)
}
package de.sepulzera.notes.ui.activity.note;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.activity.settings.SettingsActivity;
import de.sepulzera.notes.ui.adapter.NoteAdapter;
import de.sepulzera.notes.ui.adapter.impl.NoteAdapterImpl;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;

/**
 * NotizListActivity der NotizApp.
 * <p>
 * <p>Schnittstelle zum User.</p>
 * <p>
 * <ul>
 * <li>Kontextmenü für Operationen auf Notizebene.</li>
 * <li>Optionsmneu für Operationen auf Appebene.</li>
 * <li>Create-Button zum Anlegen neuer Notizen.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity
    implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {
  public static int mListRefreshInterval = 5;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_main);

    Helper.localize(getApplicationContext());
    Helper.updatePreferences(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab == null) {
      throw new IllegalStateException("ActionBar not found!");
    }
    ab.setHomeAsUpIndicator(R.drawable.ic_menu);
    ab.setDisplayHomeAsUpEnabled(true);

    mDrawerLayout = findViewById(R.id.drawer_layout);

    setupDrawerContent((NavigationView)findViewById(R.id.nav_view));

    mAdapter = new NoteAdapterImpl(this);

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
        inflater.inflate(R.menu.cab_main, menu);
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

          case R.id.cm_rename:
            if (checkedItems.size() != 1) {
              throw new IllegalArgumentException("Rename can only be used with a single note.");
            }
            renameNote(checkedItems.iterator().next());
            break;

          case R.id.cm_copy:
            copyNotes(checkedItems);
            break;

          case R.id.cm_share:
            if (checkedItems.size() != 1) {
              throw new IllegalArgumentException("Share can only be used with a single note.");
            }
            shareNote(checkedItems.iterator().next());
            break;
        }

        mSearchView.setIconified(true);
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

    final FloatingActionButton mFab = findViewById(R.id.fab);

    // Handler registrieren
    mMainView.setOnItemClickListener(this); // Single-Click: Notiz bearbeiten

    mFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onAddNote();
      }
    });

    // gespeicherten Zustand wiederherstellen
    restoreState();

    NoteService srv = NoteServiceImpl.getInstance();
    srv.wipeTrash();

    readPreferences(this);

    mHandler = new Handler();
    mRunRefreshUi = new Runnable() {
      @Override
      public void run() {
        mAdapter.updateView();
        mHandler.postDelayed( this, 60 * mListRefreshInterval * 1000 );
      }
    };
    mHandler.postDelayed(mRunRefreshUi, 60 * mListRefreshInterval * 1000 );
  }

  /**
   * Verarbeitet Input des Add-Buttons.
   * Legt eine neue Notiz mit dem im EditText angegebenen Namen an,
   * und öffnet die neue Notiz zum Bearbeiten.
   */
  private void onAddNote() {
    this.startActivityForResult(new Intent(this, NoteTabViewerActivity.class), RQ_CREATE_NOTE_ACTION);
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
  }

  @Override
  public void onResume() {
    super.onResume();
    mAdapter.updateView();
    mHandler.postDelayed(mRunRefreshUi, 60 * mListRefreshInterval * 1000 );

    Helper.dailyTask(this);
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  public static void readPreferences(@NonNull final Context context) {
    mListRefreshInterval = Helper.getPreferenceAsInt(context
        , context.getResources().getString(R.string.PREF_LIST_REFRESH_INTERVAL_KEY), Integer.parseInt(context.getResources().getString(R.string.pref_list_refresh_interval_default)));
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    final Note note = (Note)mAdapter.getItem(position);
    openNote(note);
  }

  private void openNote(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();

    this.startActivityForResult(new Intent(this, NoteTabViewerActivity.class)
            .putExtra(Note.TAG_NOTE, srv.clone(note))
        , RQ_EDIT_NOTE_ACTION);
  }

  private void renameNote(@NonNull final Note note) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getResources().getString(R.string.dialog_rename_note_rename_title));

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    input.setText(note.getTitle());
    input.setSelectAllOnFocus(true);
    builder.setView(input);

    builder.setPositiveButton(getResources().getString(R.string.dialog_rename_note_rename_btn), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        final NoteService srv = NoteServiceImpl.getInstance();
        note.setTitle(input.getText().toString());
        srv.save(note);
        mAdapter.refresh(); // old revisions, draft etc.
      }
    }).setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });
    AlertDialog dialog = builder.create();
    Window dialogWindow = dialog.getWindow();
    if (null != dialogWindow) {
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
    dialog.show();
  }

  private void copyNotes(@NonNull final List<Note> notes) {
    if (notes.size() < 1) throw new IllegalArgumentException("notes may not be empty");
    final NoteService srv = NoteServiceImpl.getInstance();

    for (int i = notes.size() - 1; i >= 0; i--) {
      final Note copyNote = srv.copy(this, notes.get(i));
      final Note savedNote = srv.save(copyNote);
      mAdapter.put(savedNote);
    }
  }

  private void shareNote(@NonNull final Note note) {
    Intent intent = Helper.createShareIntent(note.getTitle(), note.getTitle() + ": " + note.getMsg());
    startActivity(Intent.createChooser(intent, getString(R.string.share)));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_main, menu);

    SearchManager searchManager = (SearchManager)
        getSystemService(Context.SEARCH_SERVICE);
    if (null == searchManager) {
      throw new IllegalStateException("SearchManager not found");
    }
    MenuItem searchMenuItem = menu.findItem(R.id.search);
    searchMenuItem.setVisible(mAdapter.getSize() > 0);
    mSearchView = (SearchView) searchMenuItem.getActionView();
    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    mSearchView.setOnQueryTextListener(this);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    mAdapter.filter(newText);
    return true;
  }

  private void setupDrawerContent(NavigationView navigationView) {
    if (null == navigationView) { return; }

    navigationView.setCheckedItem(R.id.nav_home);

    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            //menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            return onNavigationItemSelectedHandle(menuItem);
          }
        });
  }

  private boolean onNavigationItemSelectedHandle(@NonNull final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.nav_home:
        // nothing to do
        return true;

      case R.id.nav_trash:
        startActivityForResult(new Intent(MainActivity.this, NoteTrashActivity.class), RQ_TRASH);
        // do not check nav button
        return false;

      case R.id.nav_preferences:
        startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), RQ_SETTINGS);
        // do not check nav button
        return false;

      case R.id.nav_backup:
        doNavBackup();
        // do not check nav button
        return false;

      case R.id.nav_restore:
        doNavRestore(null);
        // do not check nav button
        return false;

      case R.id.nav_about:
        doNavAbout();
        // do not check nav button
        return false;

      default:
        return false;
    }
  }

  private void doNavAbout() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    final LayoutInflater inflater = this.getLayoutInflater();
    // the root=null warning is false positive
    // see https://possiblemobile.com/2013/05/layout-inflation-as-intended/
    @SuppressLint("InflateParams")
    final View dialogView = inflater.inflate(R.layout.alert_about, null);
    builder.setView(dialogView);
    builder.show();
  }

  private void doNavBackup() {
    if (!Helper.isExternalStorageWritable()) {
      Snackbar.make(mMainView, getResources().getString(R.string.snack_backup_storage_not_writeable)
          , Snackbar.LENGTH_LONG).show();
      return;
    }
    if (!requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_BACKUP_STORAGE)) {
      return;
    }

    final NoteService srv = NoteServiceImpl.getInstance();
    final File backupFile = srv.saveBackup(getResources().getString(R.string.backup_file_name) + ".json");
    final File parentFile = backupFile.getParentFile();

    Snackbar.make(mMainView, String.format(getResources().getString(R.string.snack_backup_created)
        , (parentFile != null ? (parentFile.getName() + "/") : "") + backupFile.getName()), Snackbar.LENGTH_LONG).show();
  }

  private void doNavRestore(final Uri backupFile) {
    if (!Helper.isExternalStorageReadable()) {
      Snackbar.make(mMainView, getResources().getString(R.string.snack_restore_storage_not_readable)
          , Snackbar.LENGTH_LONG).show();
      return;
    }
    if (!requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_RESTORE_STORAGE)) {
      return;
    }

    if (null == backupFile) {
      // android versions up to 9 do not support json as mimetype
      String jsonMimeType = Build.VERSION.SDK_INT > Build.VERSION_CODES.P ? "application/json" : "application/octet-stream";
      Intent intent = new Intent()
          .setType(jsonMimeType)
          .setAction(Intent.ACTION_GET_CONTENT);

      startActivityForResult(Intent.createChooser(intent
          , getResources().getString(R.string.dialog_restore_select_file_title)), RQ_RESTORE_FILECHOOSE);
      return;
    }

    try {
      NoteService srv = NoteServiceImpl.getInstance();
      srv.restoreBackup(this, backupFile);
    } catch (IllegalArgumentException e) {
      Log.e("nav restore", "Error parsing a note: " + e.getMessage(), e);
      Snackbar.make(mMainView, "Error parsing a note: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
      return;
    }
    invalidateList();

    Snackbar.make(mMainView, getResources().getString(R.string.snack_restore_completed), Snackbar.LENGTH_LONG).show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {

      switch (requestCode) {

        case RQ_CREATE_NOTE_ACTION:
        case RQ_EDIT_NOTE_ACTION:
          if (data.hasExtra(Note.TAG_NOTE)) {
            saveNote((Note)(Objects.requireNonNull(data.getSerializableExtra(Note.TAG_NOTE))));
          }
          if (data.hasExtra(RQ_EXTRA_INVALIDATE_LIST)) {
            invalidateList();
          }
          if (data.hasExtra(RQ_EXTRA_FOLLOWUP_ID)) {
            final long noteId = data.getLongExtra(RQ_EXTRA_FOLLOWUP_ID, 0L);
            final NoteService srv = NoteServiceImpl.getInstance();
            final Note note = srv.get(noteId);
            openNote(note);
          }
          break;

        case RQ_RESTORE_FILECHOOSE:
          doNavRestore(data.getData());
          break;

        case RQ_SETTINGS:
          Helper.updatePreferences(this);
          if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
          }
          break;

        case RQ_TRASH:
          if (data.hasExtra(RQ_EXTRA_INVALIDATE_LIST)) {
            invalidateList();
          }
          break;

        default: // unknown request code -> ignore
          break;
      }

      invalidateOptionsMenu();
    }
  }

  private void saveNote(@NonNull final Note note) {
    boolean deleted = !note.getCurr();
    long oldId = note.getId();

    Note savedNote = doSaveNote(note);

    if (deleted) {
      List<Note> deleteNotesList = new ArrayList<>();
      deleteNotesList.add(oldId != 0L? note : savedNote);
      deleteNotes(deleteNotesList);
    }
  }

  private Note doSaveNote(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();

    final Note savedNote = srv.save(note);
    mAdapter.put(savedNote); // add new revision
    mAdapter.refresh(); // old revisions, draft etc.
    return savedNote;
  }

  private void deleteNotes(@NonNull final List<Note> notes) {
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

    fixAppbarPosition();
    invalidateOptionsMenu();

    String msg = notes.size() == 1
        ? String.format(getResources().getString(R.string.snack_note_moved_to_trash), notes.iterator().next().getTitle())
        : String.format(getResources().getString(R.string.snack_notes_moved_to_trash), notes.size());
    final Snackbar snack = Snackbar.make(mMainView, msg, Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_undo, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // restore note again
        mDeleteNotes = null;
        for (Note note : notes) {
          mAdapter.put(note);
          if (!note.getDraft()) {
            final Note draft = srv.getDraft(note);
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

  private void fixAppbarPosition() {
    if (mAdapter.isEmpty()) {
      // list empty and appbar faded out -> bring it back
      // (thought it should do it automatically, but it doesn't)
      AppBarLayout appbar = findViewById(R.id.appbar);
      if (null != appbar) {
        appbar.setExpanded(true);
      }
    }
  }

  private void restoreState() {
    invalidateList();
  }

  private void invalidateList() {
    mAdapter.clear();
    final NoteService srv = NoteServiceImpl.getInstance();
    for (final Note note : srv.getAllCurrent()) {
      mAdapter.put(note);
    }
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean requestPermission(@NonNull String permission, int permissionRequestId) {
    if (ContextCompat.checkSelfPermission(this,
        permission)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{permission},
          permissionRequestId);
    } else {
      // Permission has already been granted
      return true;
    }

    return false;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_CODE_BACKUP_STORAGE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          doNavBackup();
        }
        break;

      case PERMISSION_REQUEST_CODE_RESTORE_STORAGE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          doNavRestore(null);
        }
        break;

      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  private void finishDelete(@NonNull NoteService srv) {
    if (mDeleteNotes != null) {
      for (int i = mDeleteNotes.size() - 1; i >= 0; i--) {
        Note nextNote = mDeleteNotes.get(i);
        nextNote.setCurr(true); // workaround: service move to trash
        srv.delete(nextNote);
      }
      mDeleteNotes = null;
    }
  }

  private DrawerLayout   mDrawerLayout;
  private NoteAdapter    mAdapter; // Adapter zu den Notiz-ListItems
  private ListView       mMainView;
  private SearchView     mSearchView;

  private Handler        mHandler;
  private Runnable       mRunRefreshUi;

  private List<Note>     mDeleteNotes;

  private static final int RQ_EDIT_NOTE_ACTION   = 40712; // Single click (Bearbeiten)
  private static final int RQ_CREATE_NOTE_ACTION = 40713;
  private static final int RQ_SETTINGS           = 53771;
  private static final int RQ_TRASH              = 16454; // 7xxxx > 16 bit = error
  private static final int RQ_RESTORE_FILECHOOSE = 1123;

  private static final int PERMISSION_REQUEST_CODE_BACKUP_STORAGE = 123;
  private static final int PERMISSION_REQUEST_CODE_RESTORE_STORAGE = 223;

  public  static final String RQ_EXTRA_INVALIDATE_LIST     = "invalide_list";
  public  static final String RQ_EXTRA_FOLLOWUP_ID         = "followup_note_id";
}
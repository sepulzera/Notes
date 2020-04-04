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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.activity.settings.SettingsActivity;
import de.sepulzera.notes.ui.adapter.NoteAdapter;
import de.sepulzera.notes.ui.adapter.impl.NoteAdapterImpl;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ui.helper.UiHelper;

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

    final FloatingActionButton mFab = findViewById(R.id.fab);

    // Handler registrieren
    mMainView.setOnItemClickListener(this); // Single-Click: Notiz bearbeiten
    registerForContextMenu(mMainView); // Kontextmenü registrieren

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

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view,
                                  ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);
    getMenuInflater().inflate(R.menu.cm_main, menu);

    mMainView.requestFocus();
    UiHelper.hideKeyboard(mMainView, this, this);

    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    final Note note = (Note)mAdapter.getItem(info.position);
    boolean isDraft = note.getDraft();

    MenuItem item;
    if ((item = menu.findItem(R.id.cm_discard_draft)) != null) { item.setVisible(isDraft); }
    if ((item = menu.findItem(R.id.cm_delete_note)) != null)   { item.setVisible(!isDraft); }

    if ((item = menu.findItem(R.id.cm_rename_draft)) != null)  { item.setVisible(isDraft); }
    if ((item = menu.findItem(R.id.cm_rename_note)) != null)   { item.setVisible(!isDraft); }

    if ((item = menu.findItem(R.id.cm_copy_draft)) != null)    { item.setVisible(isDraft); }
    if ((item = menu.findItem(R.id.cm_copy_note)) != null)     { item.setVisible(!isDraft); }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    final Note note = (Note) mAdapter.getItem(info.position);

    switch (item.getItemId()) {
      case R.id.cm_discard_draft:
      case R.id.cm_delete_note:
        deleteNote(note);
        invalidateOptionsMenu();
        mSearchView.setIconified(true);
        return true;

      case R.id.cm_rename_draft:
      case R.id.cm_rename_note:
        renameNote(note);
        return true;

      case R.id.cm_copy_draft:
      case R.id.cm_copy_note:
        copyNote(note);
        return true;

      case R.id.cm_send_as_msg:
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            .putExtra("sms_body", note.getTitle() + ": " + note.getMsg()));
        return true;

      default:
        return super.onContextItemSelected(item);
    }
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

  private void copyNote(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();
    final Note copyNote = srv.copy(this, note);
    final Note savedNote = srv.save(copyNote);
    mAdapter.put(savedNote);
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

    Snackbar.make(mMainView, String.format(getResources().getString(R.string.snack_backup_created)
        , backupFile.getParentFile().getName() + "/" + backupFile.getName()), Snackbar.LENGTH_LONG).show();
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
            saveNote((Note)(data.getSerializableExtra(Note.TAG_NOTE)));
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
      deleteNote(oldId != 0L? note : savedNote);
    }
  }

  private Note doSaveNote(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();

    final Note savedNote = srv.save(note);
    mAdapter.put(savedNote); // add new revision
    mAdapter.refresh(); // old revisions, draft etc.
    return savedNote;
  }

  private void deleteNote(@NonNull final Note note) {
    final NoteService srv = NoteServiceImpl.getInstance();

    // Remove note and eventually draft from list.
    mAdapter.remove(note);
    if (!note.getDraft()) {
      final Note draft = srv.getDraft(note);
      if (draft != null) {
        mAdapter.remove(draft);
      }
    }
    mDeleteNote = note;

    fixAppbarPosition();
    invalidateOptionsMenu();

    final Snackbar snack = Snackbar.make(mMainView, String.format(getResources().getString(R.string.snack_note_moved_to_trash)
        , note.getTitle()), Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_undo, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // restore note again
        mDeleteNote = null;
        mAdapter.put(note);
        if (!note.getDraft()) {
          final Note draft = srv.getDraft(note);
          if (draft != null) {
            mAdapter.put(draft);
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
    if (mDeleteNote != null) {
      mDeleteNote.setCurr(true); // workaround: service move to trash
      srv.delete(mDeleteNote);
      mDeleteNote = null;
    }
  }

  private DrawerLayout   mDrawerLayout;
  private NoteAdapter    mAdapter; // Adapter zu den Notiz-ListItems
  private ListView       mMainView;
  private SearchView     mSearchView;

  private Handler        mHandler;
  private Runnable       mRunRefreshUi;

  private Note           mDeleteNote;

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
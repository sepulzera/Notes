package de.sepulzera.notes.ui.activity.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.helper.StringUtil;
import de.sepulzera.notes.bf.helper.vlog.VLog;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.helper.UiHelper;
import de.sepulzera.notes.ui.widgets.rundo.RunDo;

public class NoteTabViewerActivity extends AppCompatActivity implements NoteEditFragment.NoteEditFragmentListener, RunDo.Callbacks, RunDo.TextLink {
  public static boolean mOpenNotesReadonly = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_note_tab_viewer);

    VLog.d(ACTIVITY_IDENT, "Creating activity.");

    Helper.localize(getApplicationContext());
    Helper.updatePreferences(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab == null) {
      throw new IllegalStateException("ActionBar not found!");
    }
    ab.setDisplayHomeAsUpEnabled(true);

    mView = findViewById(R.id.main_content);

    if (null != savedInstanceState) {
      restoreState(savedInstanceState);
    } else {
      createState(getIntent());
    }

    mPager = findViewById(R.id.viewpager);
    setupViewPager(mPager);

    mTabLayout = findViewById(R.id.tabs);
    setupTabLayout(mTabLayout, mNoteFrags, mPager);

    if (mShowsRevisions) {
      showRevisions();
    }

    // button save
    mFabSave = findViewById(R.id.fab_save);
    mFabSave.setOnClickListener(v -> {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        final Note note = noteFrag.getNote();
        if (!note.getCurrRev()) {
          Snackbar.make(mView, getResources().getString(R.string.note_save_ignore_old_rev), Snackbar.LENGTH_LONG).show();
          return;
        }
        saveNote(noteFrag);
      }
    });

    // button edit
    mFabEdit = findViewById(R.id.fab_edit);
    mFabEdit.setOnClickListener(v -> {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        final Note note = noteFrag.getNote();
        if (!note.getCurrRev()) {
          return;
        }
        noteFrag.setEditable(true);
        if (note.getDraft()) {
          mIsDraftFragEditable = true;
        } else {
          mIsNoteFragEditable = true;
        }
        invalidateFloatingActionButton(true, true);
        invalidateOptionsMenu();
      }
    });

    mFabShowTbEdit = findViewById(R.id.fab_show_toolbar_edit);
    mFabShowTbEdit.setOnClickListener(v -> {
      mShowToolbarEdit = true;
      hideFloatingActionButton(mFabShowTbEdit);
      showFloatingActionButton(mFabHideTbEdit);
      invalidateOptionsMenu();
    });

    mFabHideTbEdit = findViewById(R.id.fab_hide_toolbar_edit);
    mFabHideTbEdit.setOnClickListener(v -> {
      mShowToolbarEdit = false;
      hideFloatingActionButton(mFabHideTbEdit);
      showFloatingActionButton(mFabShowTbEdit);
      invalidateOptionsMenu();
    });

    invalidateFloatingActionButton();
    invalidateOptionsMenu();
  }

  private void createState(final Intent intent) {
    if (null == intent) {
      throw new IllegalStateException("intent must not be null");
    }

    final Bundle extras = intent.getExtras();
    if (null == extras) {
      mNote = new Note();
    } else {
      mNote = (Note) intent.getExtras().getSerializable(Note.TAG_NOTE);
      if (mNote == null) {
        throw new IllegalArgumentException("note must not be null!");
      }
      mIsDraftFragEditable = !mOpenNotesReadonly;
      mIsNoteFragEditable  = !mOpenNotesReadonly;
    }

    final NoteService srv = NoteServiceImpl.getInstance();

    if (mNote.getDraft()) {
      mDraft = mNote;
      mDisplayedNote = mDraft;
      mNote = srv.getCurrRevision(mNote);
    } else {
      mDraft = srv.getDraft(mNote);
      if (null != mDraft) {
        if (mNote.getCurr() && !mDraft.getCurr()) {
          mDraft = null;
        } else {
          mDisplayedNote = mDraft;
        }
      }
      if (mDisplayedNote == null) {
        mDisplayedNote = mNote;
      }
    }

    if (mDisplayedNote == null) {
      throw new IllegalArgumentException("DisplayedNote must not be null.");
    }
    if (mDraft != null) {
      VLog.d(ACTIVITY_IDENT, "Draft=\"" + mDraft + "\"");
      mNoteFrags.add(mDraft, getResources().getString(R.string.tab_viewer_draft_title));
    }
    if (mNote != null) {
      VLog.d(ACTIVITY_IDENT, "Note=\"" + mNote + "\"");
      mNoteFrags.add(mNote, getResources().getString(R.string.tab_viewer_note_title));
    }

    // set ActionBar title
    final String title = mDisplayedNote.getTitle();
    setTitle(StringUtil.isBlank(title)? getResources().getString(R.string.new_note_title) : title);
  }

  private void restoreState(@NonNull final Bundle outState) {
    mNote  = (Note)outState.getSerializable(Note.TAG_NOTE);
    mDraft = (Note)outState.getSerializable(KEY_DRAFT);
    mDisplayedNote = (Note)outState.getSerializable(KEY_DISPLAYED_NOTE);
    mIsDraftFragEditable = outState.getBoolean(KEY_DRAFT_EDITABLE);
    mIsNoteFragEditable = outState.getBoolean(KEY_NOTE_EDITABLE);
    mShowsRevisions = outState.getBoolean(KEY_SHOW_REVS);
    mShowToolbarEdit = outState.getBoolean(KEY_SHOW_TB_EDIT);
    mInvalidateList = outState.getBoolean(KEY_INVALID_LIST);

    mCanUndo = outState.getBoolean(KEY_CAN_UNDO);
    mCanRedo = outState.getBoolean(KEY_CAN_REDO);

    setTitle(outState.getString(KEY_TITLE));

    final List<Fragment> frags = getSupportFragmentManager().getFragments();
    int numberOfFrags = frags.size();
    if (numberOfFrags == 0) {
      throw new IllegalStateException("Frags are lost");
    }
    final List<NoteEditFragment> noteFrags = new ArrayList<>();
    for (final Fragment frag : frags) {
      if (frag instanceof NoteEditFragment) {
        noteFrags.add((NoteEditFragment) frag);
      }
    }

    noteFrags.sort(Comparator.comparingInt(NoteEditFragment::getIndex));

    NoteEditFragment noteFrag = noteFrags.get(0);
    if (mDraft != null) {
      mNoteFrags.add(mDraft, getResources().getString(R.string.tab_viewer_draft_title), noteFrag);
    }

    if (mNote != null) {
      if (mDraft != null) {
        noteFrag = noteFrags.get(1);
      }
      mNoteFrags.add(mNote, getResources().getString(R.string.tab_viewer_note_title), noteFrag);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mNote != null) {
      outState.putSerializable(Note.TAG_NOTE, mNote);
    }
    if (mDraft != null) {
      outState.putSerializable(KEY_DRAFT, mDraft);
    }
    if (mDisplayedNote != null) {
      outState.putSerializable(KEY_DISPLAYED_NOTE, mDisplayedNote);
    }
    outState.putBoolean(KEY_DRAFT_EDITABLE, mIsDraftFragEditable);
    outState.putBoolean(KEY_NOTE_EDITABLE, mIsNoteFragEditable);
    outState.putString(KEY_TITLE, getTitle().toString());
    outState.putBoolean(KEY_SHOW_REVS , mShowsRevisions);
    outState.putBoolean(KEY_SHOW_TB_EDIT, mShowToolbarEdit);
    outState.putBoolean(KEY_INVALID_LIST, mInvalidateList);

    outState.putBoolean(KEY_CAN_UNDO, mCanUndo);
    outState.putBoolean(KEY_CAN_REDO, mCanRedo);
  }

  public static void readPreferences(@NonNull final Context context) {
    mOpenNotesReadonly = Helper.getPreferenceAsBool(context
        , context.getResources().getString(R.string.PREF_NOTE_OPEN_READONLY_KEY), Boolean.parseBoolean(context.getResources().getString(R.string.pref_note_open_readonly_default)));
  }

  private void invalidateFloatingActionButton() {
    final boolean isCurrRev = mDisplayedNote != null && mDisplayedNote.getCurrRev() && mDisplayedNote.getCurr();
    final boolean isEditable = mDisplayedNote != null && mDisplayedNote.getDraft() ? mIsDraftFragEditable : mIsNoteFragEditable;
    invalidateFloatingActionButton(isCurrRev, isEditable);
  }

  private void invalidateFloatingActionButton(boolean isCurrRev, boolean isEditable) {
    if (isCurrRev) {
      if (isEditable) {
        mFabSave.show();
        mFabEdit.hide();
        showFloatingActionButton(mShowToolbarEdit? mFabHideTbEdit : mFabShowTbEdit);
        hideFloatingActionButton(mShowToolbarEdit? mFabShowTbEdit : mFabHideTbEdit);
      } else {
        mFabSave.hide();
        mFabEdit.show();
        hideFloatingActionButton(mFabShowTbEdit);
        hideFloatingActionButton(mFabHideTbEdit);
      }
    } else {
      mFabSave.hide();
      mFabEdit.hide();
      hideFloatingActionButton(mFabShowTbEdit);
      hideFloatingActionButton(mFabHideTbEdit);
    }
  }

  /**
   * https://stackoverflow.com/a/43077760
   * @param fab Fab
   */
  private void hideFloatingActionButton(FloatingActionButton fab) {
    CoordinatorLayout.LayoutParams params =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    FloatingActionButton.Behavior behavior =
        (FloatingActionButton.Behavior) params.getBehavior();

    if (behavior != null) {
      behavior.setAutoHideEnabled(false);
    }

    fab.hide();
  }

  /**
   * https://stackoverflow.com/a/43077760
   * @param fab Fab
   */
  private void showFloatingActionButton(FloatingActionButton fab) {
    fab.show();
    CoordinatorLayout.LayoutParams params =
        (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
    FloatingActionButton.Behavior behavior =
        (FloatingActionButton.Behavior) params.getBehavior();

    if (behavior != null) {
      behavior.setAutoHideEnabled(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_note_view, menu);

    final boolean isCurr     = mDisplayedNote != null && mDisplayedNote.getCurr();
    final boolean isCurrRev  = mDisplayedNote != null && mDisplayedNote.getCurrRev();
    final boolean isNewNote  = mDisplayedNote != null && mDisplayedNote.getId() == 0L;
    final NoteEditFragment frag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
    final boolean isEditable = frag != null && frag.isEditable();

    MenuItem item;
    if ((item = menu.findItem(R.id.om_detail_note_show_revisions)) != null) { item.setVisible(!mShowsRevisions && mDisplayedNote != null && !isNewNote && (mDisplayedNote.getRevision() > 2 || (mDisplayedNote.getRevision() == 2 && !mDisplayedNote.getDraft()))); }
    if ((item = menu.findItem(R.id.om_detail_note_rename))         != null) { item.setVisible(isCurrRev && !isNewNote && isEditable); }
    if ((item = menu.findItem(R.id.om_detail_note_checkout))       != null) { item.setVisible(!isCurrRev && !isEditable); }
    if ((item = menu.findItem(R.id.om_detail_note_clear))          != null) { item.setVisible(isCurrRev && isEditable && !frag.getMsg().isEmpty()); }
    if ((item = menu.findItem(R.id.om_detail_note_delete))         != null) { item.setVisible(isCurr && isCurrRev && isEditable && !mDisplayedNote.getDraft()); }
    if ((item = menu.findItem(R.id.om_detail_draft_discard))       != null) { item.setVisible(isCurr && isCurrRev && isEditable && mDisplayedNote.getDraft()); }
    if ((item = menu.findItem(R.id.om_detail_note_revert))         != null) { item.setVisible(isCurrRev && !isNewNote && isEditable && frag.isChanged()); }

    if ((item = menu.findItem(R.id.om_detail_note_delete_perma))   != null) { item.setVisible(!isCurr); }
    if ((item = menu.findItem(R.id.om_detail_note_restore))        != null) { item.setVisible(!isCurr); }

    onCreateToolbarMenu(frag);

    return super.onCreateOptionsMenu(menu);
  }

  private void onCreateToolbarMenu(NoteEditFragment frag) {
    // Inflate and initialize the bottom menu
    mEditToolbar = findViewById(R.id.toolbar_edit);
    Menu bottomMenu = mEditToolbar.getMenu();
    if (bottomMenu.size() == 0) {
      getMenuInflater().inflate(R.menu.om_note_edit, bottomMenu);
      MenuItem item;
      for (int i = 0; i < bottomMenu.size(); i++) {
        item = bottomMenu.getItem(i);
        item.setOnMenuItemClickListener(this::onOptionsItemSelected);

        if ((item = bottomMenu.findItem(R.id.om_detail_note_undo))           != null) { mItemUndo = item; }
        if ((item = bottomMenu.findItem(R.id.om_detail_note_redo))           != null) { mItemRedo = item; }
        if ((item = bottomMenu.findItem(R.id.om_detail_note_line_delete))    != null) { mItemDeleteLine = item; }
        if ((item = bottomMenu.findItem(R.id.om_detail_note_line_duplicate)) != null) { mItemDuplicateLine = item; }
        if ((item = bottomMenu.findItem(R.id.om_detail_note_line_up))        != null) { mItemLineUp = item; }
        if ((item = bottomMenu.findItem(R.id.om_detail_note_line_down))      != null) { mItemLineDown = item; }
      }
    }
    int priorEditToolbarVisibility = mEditToolbar.getVisibility();
    int newEditToolbarVisibility   = mShowToolbarEdit && frag.isEditable()? View.VISIBLE : View.GONE;
    setEditToolbarVisibility(newEditToolbarVisibility);

    if (mShowToolbarEdit && frag.isEditable()) {
      mCanUndo = frag.canUndo();
      mCanRedo = frag.canRedo();
      setEditToolbarEnabled(priorEditToolbarVisibility != newEditToolbarVisibility, frag.getMsg(), frag.hasFocus(), frag.getSelectionStart(), frag.getSelectionEnd());
    }
  }

  private void setEditToolbarVisibility(int visibility) {
    mEditToolbar.setVisibility(visibility);
    setMargins(mFabSave, 0, 0, 12, visibility == View.VISIBLE ? 50 : 12);
  }

  private void setEditToolbarEnabled(boolean force, String msg, boolean hasFocus, int selStart, int selEnd) {
    if (!hasFocus || msg.isEmpty()) {
      if (mItemDeleteLine != null)    { setItemEnabled(mItemDeleteLine    , false, force); }
      if (mItemDuplicateLine != null) { setItemEnabled(mItemDuplicateLine , false, force); }

      if (mItemLineUp != null)        { setItemEnabled(mItemLineUp        , false, force); }
      if (mItemLineDown != null)      { setItemEnabled(mItemLineDown      , false, force); }
    } else {
      if (mItemDeleteLine != null)    { setItemEnabled(mItemDeleteLine    , true, force); }
      if (mItemDuplicateLine != null) { setItemEnabled(mItemDuplicateLine , true, force); }

      List<String> lines = StringUtil.getLines(msg);
      int[] selLines = StringUtil.getSelectedLines(msg, selStart, selEnd);
      if (mItemLineUp != null)        { setItemEnabled(mItemLineUp   , selLines.length != 0 && selLines[0] != 0, force); }
      if (mItemLineDown != null)      { setItemEnabled(mItemLineDown , selLines.length != 0 && selLines[selLines.length - 1] != lines.size() - 1, force); }
    }

    if (mItemUndo != null) { setItemEnabled(mItemUndo , mCanUndo, force); }
    if (mItemRedo != null) { setItemEnabled(mItemRedo , mCanRedo, force); }
  }

  private static void setItemEnabled(MenuItem item, boolean enabled, boolean force) {
    if (force || item.isEnabled() != enabled) {
      item.setEnabled(enabled);
      item.getIcon().setAlpha(enabled ? 255 : 130);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void setMargins(View view, int left, int top, int right, int bottom) {
    if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
      p.setMargins(toPx(left), toPx(top), toPx(right), toPx(bottom));
      view.requestLayout();
    }
  }

  private int toPx(int dp) {
    Resources r = getApplicationContext().getResources();
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        r.getDisplayMetrics()
    );
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (android.R.id.home == itemId) {
      onBackPressed();
      return true;
    } else if (R.id.om_detail_note_checkout == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        checkoutNote(noteFrag);
      }
      return true;
    } else if (R.id.om_detail_note_copy_to_clipboard == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        noteFrag.copyToClipboard();
        Snackbar.make(mView, getResources().getString(R.string.copied_to_clipboard), Snackbar.LENGTH_LONG).show();
      }
      return true;
    } else if (R.id.om_detail_note_share == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        Note note = noteFrag.getNote();
        Intent intent = Helper.createShareIntent(note.getTitle(), note.getTitle() + ": " + note.getMsg());
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
      }
      return true;
    } else if (R.id.om_detail_note_show_revisions == itemId) {
      showRevisions();
      return true;
    } else if (R.id.om_detail_note_rename == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        renameNote(noteFrag);
      }
      return true;
    } else if (R.id.om_detail_note_delete == itemId
            || R.id.om_detail_draft_discard == itemId
            || R.id.om_detail_note_delete_perma == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        deleteNote(noteFrag);
      }
      return true;
    } else if (R.id.om_detail_note_restore == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        restoreNote(noteFrag);
      }
      return true;
    } else if (R.id.om_detail_note_clear == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).clearNote();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_revert == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).revert();
        invalidateOptionsMenu();
      }
      return true;
    }


      /* Note Edit Options */

    else if (R.id.om_detail_note_undo == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).undo();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_redo == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).redo();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_line_delete == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).deleteSelectedLines();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_line_duplicate == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).duplicateSelectedLines();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_line_up == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).moveSelectedLinesUp();
        invalidateOptionsMenu();
      }
      return true;
    } else if (R.id.om_detail_note_line_down == itemId) {
      NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
      if (noteFrag != null) {
        (noteFrag).moveSelectedLinesDown();
        invalidateOptionsMenu();
      }
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    final String newTitle = getTitle().toString();

    if (mDraft == null) {
      // Note only
        // unchanged -> back
        //   changed -> save Draft
      final NoteEditFragment frag = mNoteFrags.get(0).getFragment();
      if (frag.isChanged() || !StringUtil.equals(frag.getNote().getTitle(), newTitle)) {
        draft(frag);
      }
    } else if (mNote == null) {
      // Draft only
      // unchanged -> back
      //   changed -> update Draft
      final NoteEditFragment frag = mNoteFrags.get(0).getFragment();
      if (frag.isChanged() || (!StringUtil.isEmpty(frag.getNote().getTitle()) && !StringUtil.equals(frag.getNote().getTitle(), newTitle))) {
        draft(frag);
      }
    } else {
      // Note and Draft
        // Note unchanged , Draft unchanged -> back
        // Note unchanged , Draft   changed -> update Draft
        // Note   changed , Draft unchanged -> override Draft
        // Note   changed , Draft   changed -> CONFLICT -> open dialog

      final NoteEditFragment draftFrag = mNoteFrags.get(0).getFragment();
      // noteFrag is always right of draftFrag
      final NoteEditFragment noteFrag  = mNoteFrags.get(1).getFragment();

      final boolean draftChanged = draftFrag.isChanged();
      final boolean noteChanged  = noteFrag.isChanged();

      if (!noteChanged && !draftChanged) {
        if (!StringUtil.equals(draftFrag.getNote().getTitle(), newTitle)) {
          // just rename it
          draft(draftFrag);
        }
        // back
        executeDone();
      } else if (!noteChanged) { // && draftChanged
        // Update Draft
        draft(draftFrag);
      } else if (!draftChanged) { // && noteChanged
        // Override Draft
        draft(noteFrag);
      } else { // noteChanged && draftChanged
        // CONFLICT -> open dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialog_back_unsaved_conflict_title))
            .setMessage(getResources().getString(R.string.dialog_back_unsaved_conflict_msg))
            .setPositiveButton(getResources().getString(R.string.dialog_back_unsaved_conflict_go), (dialog, which) -> {
              // yes = ignore conflict and go back
              executeDone();
            })
            .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), (dialog, which) -> {
              // no = abort
            }).show();
        return;
      }
    }

    executeDone();
  }

  private void checkoutNote(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();

    final NoteService srv = NoteServiceImpl.getInstance();
    final Note copyNote = srv.copy(this, note);
    copyNote.setTitle(String.format(getResources().getString(R.string.checkout_name)
        , note.getTitle(), note.getRevision()));
    final Note savedNote = srv.save(copyNote);
    mInvalidateList = true;

    final Snackbar snack = Snackbar.make(mView, getResources().getString(R.string.snack_note_checked_out), Snackbar.LENGTH_LONG);
    snack.setAction(R.string.snack_goto, v -> {
      // goto -> close current note and open the other one
      mFollowupNoteId = savedNote.getId();
      onBackPressed();
    });
    snack.show();
  }

  private void renameNote(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getResources().getString(R.string.dialog_rename_note_rename_title));

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    input.setText(note.getTitle());
    input.setSelectAllOnFocus(true);
    builder.setView(input);

    builder
        .setPositiveButton(getResources().getString(R.string.dialog_rename_note_rename_btn),
            (dialog, which) -> setTitle(input.getText().toString()))
        .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), (dialog, which) -> dialog.cancel());

    final AlertDialog dialog = builder.create();
    final Window dialogWindow = dialog.getWindow();
    if (null != dialogWindow) {
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    input.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {}
      @Override public void onTextChanged(CharSequence c, int i, int i2, int i3) {}

      @Override
      public void afterTextChanged(Editable editable) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!StringUtil.isBlank(editable.toString()));
      }
    });

    dialog.show();
  }

  private void deleteNote(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();
    if (frag.isChanged()) {
      note.setDraft(true);
      note.setMsg(frag.getMsg());
    }
    note.setCurr(false);
    executeDone(note);
  }

  private void restoreNote(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();
    note.setCurr(true);
    executeDone(note);
  }

  private void draft(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();
    note.setMsg(frag.getMsg());
    note.setTitle(getTitle().toString());

    // save draft
    note.setDraft(true);
    if (0L == note.getId()) {
      final NoteService srv = NoteServiceImpl.getInstance();
      note.setTitle(srv.toNoteTitle(""));
    }
    executeDone(note);
  }

  private void saveNote(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();
    final String newMsg = frag.getMsg();
    final String newTitle = getTitle().toString();
    if (!note.getDraft()
        && StringUtil.equals(newMsg, note.getMsg()) && StringUtil.equals(newTitle, note.getTitle())) {
      // no changes made -> just go back
      executeDone();
      return;
    }

    final Note saveNote = NoteServiceImpl.getInstance().clone(note);
    saveNote.setMsg(newMsg);
    saveNote.setTitle(StringUtil.equals(getResources().getString(R.string.new_note_title), newTitle) ? "" : newTitle);

    final boolean wasDraft = note.getDraft();
    if (wasDraft) {
      saveNote.setDraft(false);
    }

    if (0L != saveNote.getId() && !wasDraft
        || 1L < saveNote.getRevision() && wasDraft) {
      // Note was already saved and thus got a title
      saveNote.setRevision(saveNote.getRevision() + 1);
      executeDone(saveNote);
      return;
    }

    final NoteService srv = NoteServiceImpl.getInstance();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getResources().getString(R.string.dialog_create_note_title));

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    input.setText(srv.toNoteTitle(saveNote.getTitle()));
    input.setSelectAllOnFocus(true);

    builder.setView(input);

    builder.setPositiveButton(getResources().getString(R.string.dialog_create_note_save_btn), (dialog, which) -> {
      saveNote.setTitle(srv.toNoteTitle(input.getText().toString()));
      saveNote.setRevision(1);
      executeDone(saveNote);
    }).setNegativeButton(getResources().getString(R.string.dialog_btn_abort), (dialog, which) -> {
      // no = abort
    });
    AlertDialog dialog = builder.create();
    Window dialogWindow = dialog.getWindow();
    if (null != dialogWindow) {
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
    dialog.show();
  }

  private void executeDone() {
    executeDone(null);
  }

  private void executeDone(final Note note) {
    UiHelper.hideKeyboard(mView, this, getApplicationContext());

    Intent result = new Intent();
    if (note != null) {
      VLog.d(ACTIVITY_IDENT,  "ExecuteDone note=\"" + note + "\"");
      result.putExtra(Note.TAG_NOTE, note);
    }
    if (mInvalidateList) {
      result.putExtra(MainActivity.RQ_EXTRA_INVALIDATE_LIST, true);
    }
    if (mFollowupNoteId != null) {
      result.putExtra(MainActivity.RQ_EXTRA_FOLLOWUP_ID, mFollowupNoteId);
    }
    setResult(Activity.RESULT_OK, result);

    finish();
  }

  private void setupViewPager(@NonNull final ViewPager viewPager) {
    mAdapter = new NoteFragmentPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(mAdapter);
    loadTabsIntoPager(mAdapter, mNoteFrags);

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
      @Override
      public void onPageSelected(int position) { updatePage(); }
      @Override
      public void onPageScrollStateChanged(int state) { }
    });
  }

  private void setupTabLayout(@NonNull final TabLayout tabLayout
      , @NonNull final NoteFragmentAdapter noteFrags, @NonNull final ViewPager pager) {
    tabLayout.setTabMode(noteFrags.size() >= mNumScrollTabs? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);

    if (noteFrags.size() == 1) { // only 1 item displayed
      tabLayout.setVisibility(View.GONE);
    } else { // at least two items displayed -> show tabs
      tabLayout.setVisibility(View.VISIBLE);
      tabLayout.setupWithViewPager(pager);
    }
  }

  private void updatePage() {
    NoteEditFragment noteFrag = getActiveNoteFragment(getSupportFragmentManager(), mPager);
    if (noteFrag != null) {
      mDisplayedNote = noteFrag.getNote();
      invalidateFloatingActionButton();
      invalidateOptionsMenu();
    }
  }

  private static void loadTabsIntoPager(@NonNull final NoteFragmentPagerAdapter adapter
      , @NonNull final NoteFragmentAdapter noteFrags) {
    adapter.clear();
    for (int i = 0; i < noteFrags.size(); ++ i) {
      adapter.addFragment(noteFrags.get(i));
    }
    adapter.notifyDataSetChanged();
  }

  private void showRevisions() {
    if (null == mAdapter || null == mTabLayout) {
      throw new IllegalStateException("Layout incomplete");
    }

    final NoteService srv = NoteServiceImpl.getInstance();
    final List<Note> revisions = srv.getAllNoteRevisions(mNote);

    for (final Note rev : revisions) {
      if (!rev.getCurrRev()) {
        mNoteFrags.add(rev, String.valueOf(rev.getRevision()));
      }
    }

    loadTabsIntoPager(mAdapter, mNoteFrags);
    setupTabLayout(mTabLayout, mNoteFrags, mPager);

    mShowsRevisions = true;
    invalidateOptionsMenu();
  }

  @Override
  public void onTextChanged(String msg, boolean hasFocus, int selectionStart, int selectionEnd) {
    if (mShowToolbarEdit) {
      setEditToolbarEnabled(false, msg, hasFocus, selectionStart, selectionEnd);
    }
  }

  @Override
  public EditText getEditTextForRunDo(String ident) {
    final List<Fragment> frags = getSupportFragmentManager().getFragments();
    int numberOfFrags = frags.size();
    if (numberOfFrags == 0) {
      throw new IllegalStateException("Frags are lost");
    }
    for (final Fragment frag : frags) {
      if (frag instanceof NoteEditFragment && (ident == null || ident.equals(String.valueOf(((NoteEditFragment)frag).getIndex())))) {
        return ((NoteEditFragment) frag).getRef();
      }
    }
    return null;
  }

  @Override
  public void undoCalled() {
    // not needed
  }

  @Override
  public void redoCalled() {
    // not needed
  }

  @Override
  public void undoEmpty() {
    mCanUndo = false;
    if (mItemUndo != null) { setItemEnabled(mItemUndo , false, false); }
  }

  @Override
  public void redoEmpty() {
    mCanRedo = false;
    if (mItemUndo != null) { setItemEnabled(mItemUndo , false, false); }
  }

  @Override
  public void undoAvailable() {
    mCanUndo = true;
    if (mItemUndo != null) { setItemEnabled(mItemUndo , true, false); }
  }

  @Override
  public void redoAvailable() {
    mCanRedo = true;
    if (mItemUndo != null) { setItemEnabled(mItemUndo , true, false); }
  }

  static class NoteFragmentPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    NoteFragmentPagerAdapter(FragmentManager fm) {
      super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    void addFragment(@NonNull final NoteFrag noteFrag) {
      mFragments.add(noteFrag.getFragment());
      mFragmentTitles.add(noteFrag.getTitle());
    }

    @Override
    public @NonNull Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitles.get(position);
    }

    private void clear() {
      mFragments.clear();
      mFragmentTitles.clear();
    }
  }

  private static class NoteFragmentAdapter {
    private final List<NoteFrag> mNoteFrags;

    NoteFragmentAdapter() {
      mNoteFrags = new ArrayList<>();
    }

    void add(@NonNull final Note note, @NonNull final String fragmentTitle) {
      final NoteEditFragment frag = new NoteEditFragment();
      frag.initialize(mNoteFrags.size(), note, mOpenNotesReadonly || !note.getCurrRev());
      // The fragment's instances should not be destroyed to not invalidate the references to them
      frag.setRetainInstance(true);

      mNoteFrags.add(new NoteFrag(note, frag, fragmentTitle));
    }

    void add(@NonNull final Note note, @NonNull final String fragmentTitle
        , @NonNull final NoteEditFragment frag) {
      mNoteFrags.add(new NoteFrag(note, frag, fragmentTitle));
    }

    NoteFrag get(int index) {
      return mNoteFrags.get(index);
    }

    int size() {
      return mNoteFrags.size();
    }
  }

  private static class NoteFrag {
    private final Note mNote;
    private final NoteEditFragment mFrag;
    private final String mTitle;

    NoteFrag(@NonNull final Note note, @NonNull final NoteEditFragment frag, @NonNull final String title) {
      this.mNote  = note;
      this.mFrag  = frag;
      this.mTitle = title;
    }

    NoteEditFragment getFragment() { return mFrag; }
    String getTitle() { return mTitle; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      NoteFrag that = (NoteFrag) o;

      return mNote.equals(that.mNote) && mFrag.equals(that.mFrag);
    }

    @Override
    public int hashCode() {
      int result = mNote.hashCode();
      result = 31 * result + mFrag.hashCode();
      return result;
    }

    @NonNull
    @Override
    public String toString() {
      return "NoteFragPair{" +
          "mNote=" + mNote +
          ", mFrag=" + mFrag +
          ", mTitle='" + mTitle + '\'' +
          '}';
    }
  }

  private static NoteEditFragment getActiveNoteFragment(@NonNull final FragmentManager fragmentManager, @NonNull final ViewPager pager) {
    Fragment frag = getActiveFragment(fragmentManager, pager);
    if (frag instanceof NoteEditFragment) {
      return (NoteEditFragment) frag;
    } else {
      return null;
    }
  }

  private static Fragment getActiveFragment(@NonNull final FragmentManager fragmentManager, @NonNull final ViewPager pager) {
    return fragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + pager.getCurrentItem());
  }

  private View     mView;
  private ActionMenuView mEditToolbar;
  private FloatingActionButton mFabSave;
  private FloatingActionButton mFabEdit;
  private FloatingActionButton mFabShowTbEdit;
  private FloatingActionButton mFabHideTbEdit;

  private Note    mNote;
  private Note    mDraft;
  private Note    mDisplayedNote;
  private Long    mFollowupNoteId = null;

  private boolean mShowsRevisions = false;
  private boolean mInvalidateList = false;

  private final NoteFragmentAdapter mNoteFrags = new NoteFragmentAdapter();
  private boolean mIsNoteFragEditable = false;
  private boolean mIsDraftFragEditable = false;

  private NoteFragmentPagerAdapter mAdapter;
  private ViewPager mPager;
  private TabLayout mTabLayout;
  private boolean mShowToolbarEdit = false;

  private MenuItem mItemUndo;
  private MenuItem mItemRedo;
  private boolean  mCanUndo;
  private boolean  mCanRedo;

  private MenuItem mItemDeleteLine;
  private MenuItem mItemDuplicateLine;
  private MenuItem mItemLineUp;
  private MenuItem mItemLineDown;

  private static final int mNumScrollTabs = 5;

  private static final String KEY_DRAFT          = "notetabvieweract_draft";
  private static final String KEY_TITLE          = "notetabvieweract_title";
  private static final String KEY_DISPLAYED_NOTE = "notetabvieweract_displayednote";
  private static final String KEY_DRAFT_EDITABLE = "notetabvieweract_drafteditable";
  private static final String KEY_NOTE_EDITABLE  = "notetabvieweract_noteeditable";
  private static final String KEY_SHOW_REVS      = "notetabvieweract_showrevs";
  private static final String KEY_SHOW_TB_EDIT   = "notetabvieweract_showtoolbaredit";
  private static final String KEY_INVALID_LIST   = "notetabvieweract_invalidatelist";

  private static final String KEY_CAN_UNDO = "notetabvieweract_canundo";
  private static final String KEY_CAN_REDO = "notetabvieweract_canredo";

  private static final String ACTIVITY_IDENT = "NoteTabViewerActivity";
}

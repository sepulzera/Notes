package de.sepulzera.notes.ui.activity.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.helper.StringUtil;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.helper.UiHelper;

public class NoteTabViewerActivity extends AppCompatActivity {
  public static boolean mOpenNotesReadonly = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_note_tab_viewer);

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

    // set ActionBar title
    final String title = mNote != null? mNote.getTitle() : mDraft.getTitle();
    setTitle(StringUtil.isBlank(title)? getResources().getString(R.string.new_note_title) : title);

    mPager = findViewById(R.id.viewpager);
    setupViewPager(mPager);

    mTabLayout = findViewById(R.id.tabs);
    setupTabLayout(mTabLayout, mNoteFrags, mPager);

    if (mShowsRevisions) {
      showRevisions();
    }

    // button save
    mFabSave = findViewById(R.id.fab_save);
    mFabSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Fragment page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          final Note note = ((NoteEditFragment)page).getNote();
          if (!note.getCurrRev()) {
            Snackbar.make(mView, getResources().getString(R.string.note_save_ignore_old_rev), Snackbar.LENGTH_LONG).show();
            return;
          }
          saveNote((NoteEditFragment)page);
        }
      }
    });

    // button edit
    mFabEdit = findViewById(R.id.fab_edit);
    mFabEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Fragment page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          final Note note = ((NoteEditFragment)page).getNote();
          if (!note.getCurrRev()) {
            return;
          }
          ((NoteEditFragment)page).setEditable(true);
          invalidateFloatingActionButton(true, true);
        }
      }
    });

    invalidateFloatingActionButton(true, mNoteFrags.get(0).getFragment().isEditable());
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
    }

    if (mNote.getDraft()) {
      mDraft = mNote;
      mNoteFrags.add(mNote, getResources().getString(R.string.tab_viewer_draft_title));
      mDisplayedNote = mNote;

      final NoteService srv = NoteServiceImpl.getInstance();
      mNote = srv.getCurrRevision(mNote);
      if (null != mNote) {
        mNoteFrags.add(mNote, getResources().getString(R.string.tab_viewer_note_title));
      }
    } else {
      final NoteService srv = NoteServiceImpl.getInstance();
      mDraft = srv.getDraft(mNote);
      if (null != mDraft) {
        if (!mDraft.getCurr()) {
          mDraft = null;
        } else {
          mNoteFrags.add(mDraft, getResources().getString(R.string.tab_viewer_draft_title));
          mDisplayedNote = mDraft;
        }
      }
      mNoteFrags.add(mNote, getResources().getString(R.string.tab_viewer_note_title));
      if (mDisplayedNote == null) {
        mDisplayedNote = mNote;
      }
    }
  }

  private void restoreState(@NonNull final Bundle outState) {
    mNote  = (Note)outState.getSerializable(Note.TAG_NOTE);
    mDraft = (Note)outState.getSerializable(KEY_DRAFT);
    mShowsRevisions = outState.getBoolean(KEY_SHOW_REVS);

    final List<Fragment> frags = getSupportFragmentManager().getFragments();
    int numberOfFrags = frags.size();
    if (numberOfFrags == 0) {
      throw new IllegalStateException("Frags are lost");
    }
    final List<NoteEditFragment> noteFrags = new ArrayList<>(numberOfFrags);
    for (final Fragment frag : frags) {

      noteFrags.add((NoteEditFragment) frag);
    }

    Collections.sort(noteFrags, new Comparator<NoteEditFragment>() {
      @Override
      public int compare(NoteEditFragment frag1, NoteEditFragment frag2) {
        return Integer.compare(frag1.getIndex(), frag2.getIndex());
      }
    });

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
    mPager.setCurrentItem(0);

    super.onSaveInstanceState(outState);

    if (mNote != null) {
      outState.putSerializable(Note.TAG_NOTE, mNote);
    }
    if (mDraft != null) {
      outState.putSerializable(KEY_DRAFT, mDraft);
    }
    outState.putBoolean(KEY_SHOW_REVS , mShowsRevisions);
  }

  public static void readPreferences(@NonNull final Context context) {
    mOpenNotesReadonly = Helper.getPreferenceAsBool(context
        , context.getResources().getString(R.string.PREF_NOTE_OPEN_READONLY_KEY), Boolean.valueOf(context.getResources().getString(R.string.pref_note_open_readonly_default)));
  }

  private void invalidateFloatingActionButton(@NonNull final NoteEditFragment frag) {
    final boolean isCurrRev = mDisplayedNote != null && mDisplayedNote.getCurrRev();
    invalidateFloatingActionButton(isCurrRev, frag.isEditable());
  }

  private void invalidateFloatingActionButton(boolean isCurrRev, boolean isEditable) {
    if (isCurrRev) {
      if (isEditable) {
        mFabSave.show();
        mFabEdit.hide();
      } else {
        mFabSave.hide();
        mFabEdit.show();
      }
    } else {
      mFabSave.hide();
      mFabEdit.hide();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.om_note_view, menu);

    final boolean isCurrRev = mDisplayedNote != null && mDisplayedNote.getCurrRev();
    final boolean isNewNote = mDisplayedNote != null && mDisplayedNote.getId() == 0L;

    MenuItem item;
    if ((item = menu.findItem(R.id.om_detail_note_show_revisions)) != null) { item.setVisible(!mShowsRevisions && mNote != null && !isNewNote && mDisplayedNote.getRevision() > 1); }
    if ((item = menu.findItem(R.id.om_detail_note_clear)) != null) { item.setVisible(isCurrRev); }
    if ((item = menu.findItem(R.id.om_detail_note_revert)) != null) { item.setVisible(isCurrRev && !isNewNote); }
    if ((item = menu.findItem(R.id.om_detail_note_delete)) != null) { item.setVisible(isCurrRev && !mDisplayedNote.getDraft()); }
    if ((item = menu.findItem(R.id.om_detail_draft_discard)) != null) { item.setVisible(isCurrRev && mDisplayedNote.getDraft()); }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      case R.id.om_detail_note_copy_to_clipboard:
        Fragment page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          ((NoteEditFragment)page).copyToClipboard();
          Snackbar.make(mView, getResources().getString(R.string.copied_to_clipboard), Snackbar.LENGTH_LONG).show();
        }
        return true;

      case R.id.om_detail_note_send_as_msg:
        page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          final NoteEditFragment noteEditFragment = (NoteEditFragment)page;
          startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
              .putExtra("sms_body", NoteServiceImpl.getInstance().toNoteTitle(noteEditFragment.getNote().getTitle()) + ": " + noteEditFragment.getMsg()));
        }
        return true;

      case R.id.om_detail_note_show_revisions:
        showRevisions();
        return true;

      case R.id.om_detail_note_delete:
      case R.id.om_detail_draft_discard:
        page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          deleteNote((NoteEditFragment)page);
        }
        return true;

      case R.id.om_detail_note_clear:
        page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          final Note note = ((NoteEditFragment)page).getNote();
          if (!note.getCurrRev()) {
            Snackbar.make(mView, getResources().getString(R.string.note_clear_ignore_old_rev), Snackbar.LENGTH_LONG).show();
            return true;
          }
          ((NoteEditFragment)page).clearNote();
        }
        return true;

      case R.id.om_detail_note_revert:
        page = getActiveFragment(getSupportFragmentManager(), mPager);
        if (page != null) {
          final Note note = ((NoteEditFragment)page).getNote();
          if (!note.getCurrRev()) {
            Snackbar.make(mView, getResources().getString(R.string.note_revert_ignore_old_rev), Snackbar.LENGTH_LONG).show();
            return true;
          }
          ((NoteEditFragment)page).revert();
        }
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (mDraft == null) {
      // Nur Note
        // unverändert -> Zurück
        //   verändert -> Save Draft
      final NoteEditFragment frag = mNoteFrags.get(0).getFragment();
      if (frag.isChanged()) {
        draft(frag);
      }
    } else if (mNote == null) {
      // Nur Draft
      // unverändert -> Zurück
      //   verändert -> Update Draft
      final NoteEditFragment frag = mNoteFrags.get(0).getFragment();
      if (frag.isChanged()) {
        draft(frag);
      }
    } else {
      // Note und Draft
        // Note unverändert , Draft unverändert -> Zurück
        // Note unverändert , Draft   verändert -> Update Draft
        // Note   verändert , Draft unverändert -> Override Draft
        // Note   verändert , Draft   verändert -> KONFLIKT -> Dialog

      final NoteEditFragment draftFrag = mNoteFrags.get(0).getFragment();
      // noteFrag is always right of draftFrag
      final NoteEditFragment noteFrag  = mNoteFrags.get(1).getFragment();

      final boolean draftChanged = draftFrag.isChanged();
      final boolean noteChanged  = noteFrag.isChanged();

      if (!noteChanged && !draftChanged) {
        // Zurück
        super.onBackPressed();
      } else if (!noteChanged) { // && draftChanged
        // Update Draft
        draft(draftFrag);
      } else if (!draftChanged) { // && noteChanged
        // Override Draft
        draft(noteFrag);
      } else { // noteChanged && draftChanged
        // KONFLIKT -> Dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialog_back_unsaved_conflict_title))
            .setMessage(getResources().getString(R.string.dialog_back_unsaved_conflict_msg))
            .setPositiveButton(getResources().getString(R.string.dialog_back_unsaved_conflict_go), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // yes = ignore conflict and go back
                finish();
              }
            })
            .setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // no = abort
              }
            }).show();
        return;
      }
    }

    super.onBackPressed();
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

  private void draft(@NonNull final NoteEditFragment frag) {
    final Note note = frag.getNote();
    note.setMsg(frag.getMsg());

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
    if (!note.getDraft() && StringUtil.equals(newMsg, note.getMsg())) {
      // no changes made -> just go back
      finish();
      return;
    }

    final Note saveNote = NoteServiceImpl.getInstance().clone(note);
    saveNote.setMsg(newMsg);

    final boolean wasDraft = note.getDraft();
    if (wasDraft) {
      saveNote.setDraft(false);
    }

    if (0L != saveNote.getId() && !wasDraft
        || 1L < saveNote.getRevision() && wasDraft) {
      // Note was already saved and thus got a title
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

    builder.setPositiveButton(getResources().getString(R.string.dialog_create_note_save_btn), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        saveNote.setTitle(srv.toNoteTitle(input.getText().toString()));
        executeDone(saveNote);
      }
    }).setNegativeButton(getResources().getString(R.string.dialog_btn_abort), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // no = abort
      }
    });
    AlertDialog dialog = builder.create();
    Window dialogWindow = dialog.getWindow();
    if (null != dialogWindow) {
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
    dialog.show();
  }

  private void executeDone(@NonNull final Note note) {
    UiHelper.hideKeyboard(mView, this, getApplicationContext());
    setResult(Activity.RESULT_OK, new Intent()
        .putExtra(Note.TAG_NOTE, note));
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
    Fragment page = getActiveFragment(getSupportFragmentManager(), mPager);
    if (page != null) {
      mDisplayedNote = ((NoteEditFragment)page).getNote();
      invalidateOptionsMenu();
      invalidateFloatingActionButton(((NoteEditFragment)page));
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

  static class NoteFragmentPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    NoteFragmentPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    void addFragment(@NonNull final NoteFrag noteFrag) {
      mFragments.add(noteFrag.getFragment());
      mFragmentTitles.add(noteFrag.getTitle());
    }

    @Override
    public Fragment getItem(int position) {
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

  private class NoteFragmentAdapter {
    private final List<NoteFrag> mNoteFrags;

    NoteFragmentAdapter() {
      mNoteFrags = new ArrayList<>();
    }

    void add(@NonNull final Note note, @NonNull final String fragmentTitle) {
      final NoteEditFragment frag = new NoteEditFragment();
      frag.initialize(mNoteFrags.size(), note);
      // Fragmente anweisen, sich nicht vollständig zu zerstören (wegen Referenzen)
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

  private class NoteFrag {
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

    @Override
    public String toString() {
      return "NoteFragPair{" +
          "mNote=" + mNote +
          ", mFrag=" + mFrag +
          ", mTitle='" + mTitle + '\'' +
          '}';
    }
  }

  private static Fragment getActiveFragment(@NonNull final FragmentManager fragmentManager, @NonNull final ViewPager pager) {
    return fragmentManager.findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + pager.getCurrentItem());
  }

  private View     mView;
  private FloatingActionButton mFabSave;
  private FloatingActionButton mFabEdit;

  private Note    mNote;
  private Note    mDraft;
  private Note    mDisplayedNote;

  private boolean mShowsRevisions = false;

  private final NoteFragmentAdapter mNoteFrags = new NoteFragmentAdapter();

  private NoteFragmentPagerAdapter mAdapter;
  private ViewPager mPager;
  private TabLayout mTabLayout;

  private static final int mNumScrollTabs = 5;

  private static final String KEY_DRAFT     = "notetabvieweract_draft";
  private static final String KEY_SHOW_REVS = "notetabvieweract_showrevs";
}

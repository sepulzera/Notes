package de.sepulzera.notes.bf.service.impl;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.Helper;
import de.sepulzera.notes.bf.helper.StringUtil;
import de.sepulzera.notes.bf.helper.vlog.VLog;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.ds.db.impl.NoteDbImpl;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ds.db.NoteDatabase;
import de.sepulzera.notes.ds.model.NoteContract.NoteEntry;

public final class NoteServiceImpl implements NoteService {

  public static void createInstance(@NonNull final Context context) {
    if (mInstance == null) {
      mInstance = new NoteServiceImpl(context);
    }
  }

  public static NoteServiceImpl getInstance() {
    if (mInstance == null) {
      throw new IllegalStateException("createInstance not called");
    }
    return mInstance;
  }

  @Override
  public Note copy(@NonNull final Context context, @NonNull final Note from) {
    final Note newNote = new Note();

    newNote.setDraft(true);

    newNote.setTitle(from.getTitle() + " - " + context.getResources().getString(R.string.copy_name_add));
    newNote.setMsg(from.getMsg());

    final Date now = Calendar.getInstance().getTime();
    newNote.setCreadt(now);
    newNote.setLchadt(now);

    return newNote;
  }

  @Override
  public Note clone(@NonNull final Note from) {
    final Note newNote = new Note();

    newNote.setId       (from.getId());

    newNote.setIdent    (from.getIdent());
    newNote.setRevision (from.getRevision());
    newNote.setCurrRev  (from.getCurrRev());
    newNote.setDraft    (from.getDraft());

    newNote.setTitle    (from.getTitle());
    newNote.setMsg      (from.getMsg());

    newNote.setDeldt    (from.getDeldt());

    newNote.setCurr     (from.getCurr());
    newNote.setCreadt   (from.getCreadt());
    newNote.setLchadt   (from.getLchadt());

    return newNote;
  }

  @Override
  public void delete(@NonNull final Note note) {
    final Note oldNote = get(note.getId());
    if (oldNote == null) {
      throw new IllegalArgumentException("note does not exist! (" + note.toString() + ")");
    }

    if (oldNote.getCurr() && mPrefDeleteTrashedNotesDays != 0) {
      moveToTrash(oldNote);
    } else {
      doDelete(oldNote);
    }
  }

  private static void moveToTrash(@NonNull final Note note) {
    final ContentValues values = new ContentValues(2);
    values.put(NoteEntry.COL_CURR   , 0); // 0 = false
    values.put(NoteEntry.COL_DELDT  , Calendar.getInstance().getTime().getTime());

    if (note.getDraft()) {
      mDb.update(values, NoteEntry._ID + " = ?"
          , new String[]{String.valueOf(note.getId())});
    } else {
      mDb.update(values, NoteEntry.COL_IDENT + " = ?"
          , new String[]{String.valueOf(note.getIdent())});
    }
  }

  private static void doDelete(@NonNull final Note note) {
    if (note.getDraft()) {
      // delete this draft only
      mDb.delete(NoteEntry._ID + " = ?", new String[]{String.valueOf(note.getId())});
    } else {
      // delete note and all revisions
      mDb.delete(NoteEntry.COL_IDENT + " = ?", new String[]{String.valueOf(note.getIdent())});
    }
  }

  @Override
  public Note get(long id) {
    return mDb.get(id);
  }

  @Override
  public Note getDraft(@NonNull final Note note) {
    final String[] selectionArgs = { String.valueOf(note.getIdent()), "1", "1"};

    final List<Note> drafts = mDb.find(NoteEntry.COL_IDENT + "=? AND "
        + NoteEntry.COL_CURR_REV + "=? AND " + NoteEntry.COL_DRAFT + "=?", selectionArgs);
    if (null == drafts || drafts.isEmpty()) {
      return null; // no draft
    }

    return drafts.get(0); // there can be only 1 draft
  }

  @Override
  public Note getCurrRevision(@NonNull final Note note) {
    final String[] selectionArgs = { String.valueOf(note.getIdent()), "1", "0"};

    final List<Note> result = mDb.find(NoteEntry.COL_IDENT + "=? AND "
        + NoteEntry.COL_CURR_REV + "=? AND " + NoteEntry.COL_DRAFT + "=?", selectionArgs);
    if (null == result || result.isEmpty()) {
      return null; // no rev
    }

    return result.get(0); // there can be only 1 revision
  }

  @Override
  public Note getRevision(@NonNull final Note note, long revision) {
    final String[] selectionArgs = { String.valueOf(note.getIdent()), String.valueOf(revision) };

    final List<Note> result = mDb.find(NoteEntry.COL_IDENT + "=? AND "
        + NoteEntry.COL_REVISION + "=?", selectionArgs);
    if (null == result || result.isEmpty()) {
      return null; // no note found
    }

    return result.get(0); // there can be only 1 revision
  }

  @Override
  public List<Note> getAll(@NonNull long[] ids) {
    final int numIds = ids.length;

    final String[] selectionArgs = new String[numIds];
    for (int i = 0; i < numIds; ++i) {
      selectionArgs[i] = String.valueOf(ids[i]);
    }

    return mDb.find(NoteEntry._ID + " IN ("
        + Helper.makePlaceholders(numIds) +  ")", selectionArgs);
  }

  @Override
  public List<Note> getAllCurrent() {
    final String[] selectionArgs = {"1", "1"};

    return mDb.find(NoteEntry.COL_CURR + "=? AND " + NoteEntry.COL_CURR_REV + "=?", selectionArgs);
  }

  @Override
  public List<Note> getAllDeleted() {
    final String[] selectionArgs = {"0", "1"};

    return mDb.find(NoteEntry.COL_CURR + "=? AND " + NoteEntry.COL_CURR_REV + "=?", selectionArgs);
  }

  @Override
  public List<Note> getAllNoteRevisions(@NonNull final Note note) {
    final String[] selectionArgs = { String.valueOf(note.getIdent())};

    return mDb.find(NoteEntry.COL_IDENT + "=?", selectionArgs
        , null, null, NoteEntry.COL_REVISION + " DESC", null);
  }

  @Override
  public void restore(@NonNull final Note note) {
    // restore note and all revisions
    final ContentValues values = new ContentValues(1);
    values.put(NoteEntry.COL_CURR   , 1); // 1 = true

    mDb.update(values, NoteEntry.COL_IDENT + " = ?", new String[] { String.valueOf(note.getIdent()) });
  }

  @Override
  public Note save(@NonNull final Note note) {
    long id = note.getId();
    if (StringUtil.isBlank(note.getTitle())) {
      note.setTitle(toNoteTitle(""));
    }

    if (id == 0L) {
      return insert(note);
    } else {
      return update(note);
    }
  }

  private Note insert(@NonNull final Note note) {
    note.setIdent(++mLastIdent);
    note.setCurrRev(true);
    note.setLchadt(Calendar.getInstance().getTime());

    Note savedNote;
    try {
      VLog.d(LOGGER_ID, "[Insert] note=\"" + note.toString() + "\"");
      savedNote = mDb.add(note);
    } catch (SQLIntegrityConstraintViolationException e) {
      VLog.d(LOGGER_ID, "[Insert] Constraint collision: " + e.getMessage());
      throw new IllegalArgumentException("Constraint collision!", e);
    }
    VLog.d(LOGGER_ID, "[Insert] Saved note=\"" + savedNote.toString() + "\"");
    return savedNote;
  }

  private Note update(@NonNull final Note note) {
    if (!note.getCurrRev()) {
      throw new IllegalArgumentException("Old revisions can not be updated!");
    }
    VLog.d(LOGGER_ID, "[Update] note=\"" + note.toString() + "\"");

    final Note oldNote = mDb.get(note.getId());
    note.setLchadt(Calendar.getInstance().getTime());
    note.setCurr(true);

    // oldNote was draft or msg not changed -> no new revision
    // else -> new revision

    final boolean oldNoteIsDraft = oldNote.getDraft();
    final boolean noteIsDraft = note.getDraft();
    final boolean revIncreased = oldNote.getRevision() < note.getRevision();
    note.setRevision(oldNote.getRevision());

    if (oldNoteIsDraft && !noteIsDraft) {
      // oldNote was Draft and is now the new revision
      return upgradeDraft(note);
    }

    if (StringUtil.equals(oldNote.getMsg(), note.getMsg())) {
      if (StringUtil.equals(oldNote.getTitle(), note.getTitle())) {
        return note;
      }
      // msg not changed but title changed -> just update
      if (noteIsDraft) {
        return updateDraft(note);
      } else {
        if (revIncreased) {
          discardOldDraft(note);
        }
        return updateNote(note);
      }
    }

    // msg changed

    if (noteIsDraft) {
      if (!oldNoteIsDraft) {
        return createDraft(note);
      } else {
        // draft: just update the draft (no new revision)
        return updateDraft(note);
      }
    }

    // note: upgrade
    return upgradeNote(note);
  }

  private Note createDraft(@NonNull final Note note) {
    VLog.d(LOGGER_ID, "[CreateDraft] draft=\"" + note.toString() + "\"");
    discardOldDraft(note);

    final Note draft = clone(note);
    draft.setRevision(note.getRevision() + 1);
    draft.setCurrRev(true);
    try {
      return mDb.add(draft);
    } catch (SQLIntegrityConstraintViolationException e) {
      throw new IllegalArgumentException("Constraint collision!", e);
    }
  }

  private void discardOldDraft(@NonNull final Note note) {
    final Note draft = getDraft(note);
    if (draft != null) {
      VLog.d(LOGGER_ID, "[DiscardOldDraft] draft=\"" + draft.toString() + "\"");
      draft.setCurr(false);
      doDelete(draft);
    }
  }

  private Note upgradeDraft(@NonNull final Note draft) {
    VLog.d(LOGGER_ID, "[UpgradeDraft] draft=\"" + draft.toString() + "\"");
    final Note savedNote = mDb.update(draft);
    if (null != savedNote) {
      final Note oldRevision = getRevision(draft, draft.getRevision() - 1);
      if (null != oldRevision) {
        invalidateRevision(oldRevision);
      }
    }

    return savedNote;
  }

  private Note upgradeNote(@NonNull final Note note) {
    discardOldDraft(note);

    if (mPrefDeleteOldRevs == -1) {
      VLog.d(LOGGER_ID, "[UpgradeNote] overwrite note=\"" + note.toString() + "\"");
      return updateNote(note);
    }

    // make new revision
    Note newRevision;
    final long newRev = note.getRevision() + 1;
    note.setRevision(newRev);
    note.setCurrRev(true);
    try {
      VLog.d(LOGGER_ID, "[UpgradeNote] new revision=\"" + note.toString() + "\"");
      newRevision = mDb.add(note);
    } catch (SQLIntegrityConstraintViolationException e) {
      throw new IllegalArgumentException("Constraint collision!", e);
    }

    // invalidate old revision
    final Note oldNote = mDb.get(note.getId());
    invalidateRevision(oldNote);

    if (mPrefDeleteOldRevs > 0 && newRev > mPrefDeleteOldRevs) {
      // delete old revisions
      final String[] selectionArgs = { String.valueOf(note.getIdent()), String.valueOf(newRev - mPrefDeleteOldRevs) };
      VLog.d(LOGGER_ID, "[UpgradeNote] remove old revisions=\"" + Arrays.toString(selectionArgs) + "\"");
      mDb.delete(NoteEntry.COL_IDENT + " = ? AND " + NoteEntry.COL_REVISION + " <= ?", selectionArgs);
    }

    return newRevision;
  }

  private Note updateDraft(@NonNull final Note draft) {
    VLog.d(LOGGER_ID, "[UpdateDraft] draft=\"" + draft.toString() + "\"");
    return mDb.update(draft);
  }

  private Note updateNote(@NonNull final Note note) {
    VLog.d(LOGGER_ID, "[UpdateNote] update note=\"" + note.toString() + "\"");
    final Note savedNote = mDb.update(note);
    if (savedNote != null) {
      final Note draft = getDraft(savedNote);
      if (draft != null && !StringUtil.equals(savedNote.getTitle(), draft.getTitle())) {
        draft.setTitle(savedNote.getTitle());
        VLog.d(LOGGER_ID, "[UpdateNote] update draft=\"" + draft.toString() + "\"");
        mDb.update(draft);
      }
    }

    return savedNote;
  }

  private void invalidateRevision(@NonNull final Note note) {
    // invalidate oldNote
    note.setCurrRev(false);
    VLog.d(LOGGER_ID, "[InvalidateRevision] note=\"" + note.toString() + "\"");
    mDb.update(note);
  }

  @Override
  public String toNoteTitle(@NonNull String title) {
    if (!StringUtil.isBlank(title)) {
      return title.trim();
    }

    return mDf.format(GregorianCalendar.getInstance().getTime());
  }

  @Override
  public void readPreferences(@NonNull final Context context) {
      mPrefDeleteOldRevs = Helper.getPreferenceAsInt(context
          , context.getResources().getString(R.string.PREF_REV_DELETE_OLD_NUM_KEY), Integer.parseInt(context.getResources().getString(R.string.pref_rev_delete_old_num_default)));
    mPrefDeleteTrashedNotesDays = Helper.getPreferenceAsInt(context
        , context.getResources().getString(R.string.PREF_NOTE_REMOVE_DELETED_DAYS_NUM_KEY), Integer.parseInt(context.getResources().getString(R.string.pref_note_remove_deleted_days_num_default)));
  }

  @Override
  public void restoreBackup(@NonNull final Context context, @NonNull final Uri backupFile) {
    final List<Note> allNotes = new ArrayList<>();

    try {
      String backupString = Helper.readFile(context, backupFile);

      if (StringUtil.isEmpty(backupString)) {
        throw new IllegalArgumentException("Error: Could not read the backup file \"" + backupFile + "\".");
      }

      final JSONArray arr = new JSONArray(backupString);
      for (int i = 0; i < arr.length(); i++) {
        allNotes.add(new Note(arr.getJSONObject(i)));
      }

    } catch (JSONException e) {
      throw new IllegalArgumentException("Error parsing a note!", e);
    }

    doRestoreBackup(allNotes);
  }

  private void doRestoreBackup(@NonNull final List<Note> backup) {
    final List<IdentPair> identMapper = new ArrayList<>();
    long nextIdent = 1L;
    int nextPairIndex;

    for (final Note note : backup) {
      IdentPair nextPair = new IdentPair();
      nextPair.setKey(note.getIdent());

      nextPairIndex = identMapper.indexOf(nextPair); // returns > -1 if key already mapped
      if (nextPairIndex > -1) {
        // key was already mapped, get the value
        note.setIdent(identMapper.get(nextPairIndex).getVal());
      } else {
        // key was not mapped, yet
        while (hasIdent(nextIdent)) {
          // ident does already exist, get the next one
          ++nextIdent;
        }
        // update the mapper
        nextPair.setVal(nextIdent);
        note.setIdent(nextIdent);
        ++nextIdent;
        identMapper.add(nextPair);
      }

      // store it
      note.setId(0L);
      try {
        mDb.add(note);
      } catch (SQLIntegrityConstraintViolationException e) {
        throw new IllegalArgumentException("Constraint collision!", e);
      }
    }
  }

  private boolean hasIdent(long ident) {
    final String[] selectionArgs = { String.valueOf(ident) };
    return !mDb.find(NoteEntry.COL_IDENT + "=?", selectionArgs).isEmpty();
  }

  private static final class IdentPair {
    long key;
    long val;

    IdentPair () {
      this.key = 0L;
      this.val = 0L;
    }

    // public long getKey() { return key; }
    long getVal() { return val; }

    void setKey(long key) { this.key = key; }
    void setVal(long val) { this.val = val; }

    @Override
    public boolean equals(Object obj) {
      // only compare the key

      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      IdentPair other = (IdentPair) obj;
      return key == other.key;
    }

    @Override
    public int hashCode() {
      // only compare the key
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (key ^ (key >>> 32));
      return result;
    }

    @Override
    public String toString() {
      return "IdentPair [key=" + key + ", val=" + val + "]";
    }
  }

  @Override
  public File saveBackup(@NonNull final String fileName) {
    final List<Note> allNotes = mDb.find(null, null);

    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toURI());

    // Convert SubjectList into JSONArray.
    // The JSONArray will be serialized.
    final JSONArray arr = new JSONArray();
    for (final Note note : allNotes) {
      arr.put(note.toJson());
    }

    try {
      file = new File(file, fileName);
      Helper.writeFile(file.getPath(), arr.toString(2), true);
    } catch (JSONException e) {
      throw new IllegalArgumentException("JSONException!", e);
    }

    return file;
  }

  @Override
  public void wipeTrash() {
    if (mPrefDeleteTrashedNotesDays == -1) {
      // auto wipe trash disabled
      return;
    }

    if (mPrefDeleteTrashedNotesDays == 0) {
      // delete instantly
      mDb.delete(NoteEntry.COL_CURR + " = ?", new String[]{"0"} );
      return;
    }

    long now = Calendar.getInstance().getTime().getTime();
    mDb.delete(NoteEntry.COL_CURR + " = ? AND " + NoteEntry.COL_DELDT + " < ?"
        , new String[]{"0", String.valueOf(now - mPrefDeleteTrashedNotesDays * 24 * 60 * 60 * 1000) } );
  }

  private NoteServiceImpl(@NonNull final Context context) {
    // Singleton
    NoteDbImpl.createInstance(context);

    mDb = NoteDbImpl.getInstance();
    final List<Note> lastNote = mDb.find(null, null, null, null
        , NoteEntry.COL_IDENT + " DESC", "1");

    if (null != lastNote && !lastNote.isEmpty()) {
      mLastIdent = lastNote.get(0).getIdent();
    }

    readPreferences(context);
  }

  private static NoteServiceImpl mInstance = null;
  private static NoteDatabase mDb = null;

  private final static DateFormat mDf = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());

  private static long mLastIdent;

  private static int mPrefDeleteOldRevs;
  private static int mPrefDeleteTrashedNotesDays;

  private final static String LOGGER_ID = "NoteService";
}

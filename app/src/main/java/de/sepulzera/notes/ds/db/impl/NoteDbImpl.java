package de.sepulzera.notes.ds.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.sepulzera.notes.ds.db.NoteDatabase;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ds.model.NoteContract.NoteEntry;

public class NoteDbImpl implements NoteDatabase {

  public static void createInstance(@NonNull final Context context) {
    if (mInstance == null) {
      mInstance = new NoteDbImpl(context);
    }
  }

  public static NoteDbImpl getInstance() {
    if (mInstance == null) {
      throw new IllegalStateException("createInstance not called");
    }
    return mInstance;
  }

  @Override
  public Note add(@NonNull final Note note) throws SQLIntegrityConstraintViolationException {
    final String[] selectionArgs = { String.valueOf(note.getIdent()), String.valueOf(note.getRevision()) };

    final List<Note> secondaryKeyCollision =
        find(NoteEntry.COL_IDENT + "=? AND " + NoteEntry.COL_REVISION + "=?", selectionArgs);
    if (!secondaryKeyCollision.isEmpty()) {
      throw new SQLIntegrityConstraintViolationException("Secondary key "
          + "[ident=" + note.getIdent() + ",revision=" + note.getRevision() + "]"
          + " does already exist");
    }

    ContentValues values = new ContentValues();
    values.put(NoteEntry.COL_IDENT     , note.getIdent());
    values.put(NoteEntry.COL_REVISION  , note.getRevision());
    values.put(NoteEntry.COL_CURR_REV  , note.getCurrRev()? 1 : 0);
    values.put(NoteEntry.COL_DRAFT     , note.getDraft()? 1 : 0);
    values.put(NoteEntry.COL_TITLE     , note.getTitle());
    values.put(NoteEntry.COL_MSG       , note.getMsg());
    Date date = note.getDeldt();
    if (date != null) {
      values.put(NoteEntry.COL_DELDT   , date.getTime());
    }
    values.put(NoteEntry.COL_CURR      , note.getCurr()? 1 : 0);
    date = note.getCreadt();
    values.put(NoteEntry.COL_CREADT    , (null == date)? Calendar.getInstance().getTime().getTime() : date.getTime());
    date = note.getLchadt();
    values.put(NoteEntry.COL_LCHADT    , (null == date)? Calendar.getInstance().getTime().getTime() : date.getTime());

    return get(mDbHelper.getWritableDatabase().insert(NoteEntry.TABLE_NAME, null, values));
  }

  @Override
  public int delete(String where, String[] selectionArgs) {
    return mDbHelper.getWritableDatabase().delete(NoteEntry.TABLE_NAME, where, selectionArgs);
  }

  @Override
  public Note get(long id) {
    final String[] selectionArgs = { String.valueOf(id) };

    final Cursor cursor = mDbHelper.getReadableDatabase().query(NoteEntry.TABLE_NAME, NoteEntry.PROJECTION
        , NoteEntry._ID + "=?", selectionArgs, null, null, null);
    Note note = null;
    if (cursor.moveToFirst()) {
      if (!cursor.isAfterLast()) {
        note = readNote(cursor);
      }
    }
    cursor.close();
    return note;
  }

  private static Note readNote(@NonNull Cursor cursor) {
    final Note note = new Note();

    note.setId(readLong(cursor, BaseColumns._ID));
    note.setIdent(readLong(cursor, NoteEntry.COL_IDENT));
    note.setRevision(readLong(cursor, NoteEntry.COL_REVISION));
    note.setCurrRev(readBool(cursor, NoteEntry.COL_CURR_REV));
    note.setDraft(readBool(cursor, NoteEntry.COL_DRAFT));
    note.setTitle(readString(cursor, NoteEntry.COL_TITLE));
    note.setMsg(readString(cursor, NoteEntry.COL_MSG));
    note.setDeldt(new Date(readLong(cursor, NoteEntry.COL_DELDT)));
    note.setCurr(readBool(cursor, NoteEntry.COL_CURR));
    note.setCreadt(new Date(readLong(cursor, NoteEntry.COL_CREADT)));
    note.setLchadt(new Date(readLong(cursor, NoteEntry.COL_LCHADT)));

    return note;
  }

  @Override
  public List<Note> find(String where, String[] selectionArgs) {
    return find(where, selectionArgs, null, null, null, null);
  }

  @Override
  public List<Note> find(String selection, String[] selectionArgs
      , String groupBy, String having, String orderBy, String limit) {
    final List<Note> notes = new ArrayList<>();
    final Cursor cursor = mDbHelper.getReadableDatabase().query(NoteEntry.TABLE_NAME, NoteEntry.PROJECTION
        , selection, selectionArgs, groupBy, having, orderBy, limit);

    if (cursor.moveToFirst()) {
      while(!cursor.isAfterLast()) {
        notes.add(readNote(cursor));
        cursor.moveToNext();
      }
    }
    cursor.close();

    return notes;
  }

  @Override
  public Note update(@NonNull final Note note) {
    ContentValues values = new ContentValues();
    values.put(NoteEntry.COL_CURR_REV  , note.getCurrRev()? 1 : 0);
    values.put(NoteEntry.COL_DRAFT     , note.getDraft()? 1 : 0);
    values.put(NoteEntry.COL_TITLE     , note.getTitle());
    values.put(NoteEntry.COL_MSG       , note.getMsg());
    values.put(NoteEntry.COL_DELDT     , note.getDeldt().getTime());
    values.put(NoteEntry.COL_CURR      , note.getCurr()? 1 : 0);
    Date date = note.getCreadt();
    values.put(NoteEntry.COL_CREADT    , (null == date)? Calendar.getInstance().getTime().getTime() : date.getTime());
    date = note.getLchadt();
    values.put(NoteEntry.COL_LCHADT    , (null == date)? Calendar.getInstance().getTime().getTime() : date.getTime());

    final String[] selectionArgs = { String.valueOf(note.getId()) };

    mDbHelper.getWritableDatabase().update(NoteEntry.TABLE_NAME, values
        , NoteEntry._ID + " = ?", selectionArgs);

    return get(note.getId());
  }

  @Override
  public int update(@NonNull ContentValues values, String where, String[] selectionArgs) {
    return mDbHelper.getWritableDatabase().update(NoteEntry.TABLE_NAME, values, where, selectionArgs);
  }

  private static boolean readBool(@NonNull Cursor cursor, @NonNull String column) {
    int colId = cursor.getColumnIndexOrThrow(column);
    return !cursor.isNull(colId) && cursor.getInt(colId) == 1;
  }

  private static long readLong(@NonNull Cursor cursor, @NonNull String column) {
    int colId = cursor.getColumnIndexOrThrow(column);
    if (cursor.isNull(colId)) {
      return 0L;
    }
    return cursor.getLong(colId);
  }

  private static String readString(@NonNull Cursor cursor, @NonNull String column) {
    return cursor.getString(cursor.getColumnIndexOrThrow(column));
  }

  private NoteDbImpl(@NonNull final Context context) {
    // Singleton
    mDbHelper = new NoteDbHelperImpl(context);
  }

  private static NoteDbImpl mInstance = null;
  private static NoteDbHelperImpl mDbHelper = null;
}

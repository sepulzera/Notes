package de.sepulzera.notes.ds.db;

import android.content.ContentValues;
import androidx.annotation.NonNull;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import de.sepulzera.notes.ds.model.Note;

public interface NoteDatabase {
  Note add(@NonNull final Note note) throws SQLIntegrityConstraintViolationException;
  Note get(long id);
  Note update(@NonNull final Note note);
  @SuppressWarnings("UnusedReturnValue")
  int update(@NonNull ContentValues values, String where, String[] selectionArgs);
  @SuppressWarnings("UnusedReturnValue")
  int delete(String where, String[] selectionArgs);

  List<Note> find(String where, String[] selectionArgs);
  List<Note> find(String selection, String[] selectionArgs
      , String groupBy, String having, String orderBy, String limit);
}

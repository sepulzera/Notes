package de.sepulzera.notes.ds.db;

import android.content.ContentValues;
import androidx.annotation.NonNull;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import de.sepulzera.notes.ds.model.Note;

public interface NoteDatabase {
  /**
   * <p>Fügt die übergebene Notiz an die Liste an.</p>
   *
   * @param note Notiz
   */
  Note add(@NonNull final Note note) throws SQLIntegrityConstraintViolationException;

  /**
   * <p>Löscht die Notiz mit der angegebenen ID.</p>
   *
   * @param where Where clause.
   * @param selectionArgs args.
   */
  @SuppressWarnings("UnusedReturnValue")
  int delete(String where, String[] selectionArgs);

  /**
   * <p>Gibt die Notiz mit der angegebenen ID zurück.</p>
   *
   * @param id ID der gesuchten Notiz.
   * @return Notiz mit der angegebenen ID oder null wenn nicht gefunden.
   */
  Note get(long id);

  /**
   * Gibt alle Notizen zurück.
   *
   * @param where Where clause.
   * @param selectionArgs args.
   *
   * @return Alle gefunden Notizen .
   */
  List<Note> find(String where, String[] selectionArgs);

  List<Note> find(String selection, String[] selectionArgs
      , String groupBy, String having, String orderBy, String limit);

  /**
   * <p>Aktualisiert die Notiz.</p>
   *
   * @param note Note
   */
  Note update(@NonNull final Note note);

  @SuppressWarnings("UnusedReturnValue")
  int update(@NonNull ContentValues values, String where, String[] selectionArgs);
}

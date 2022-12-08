package de.sepulzera.notes.bf.service;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import de.sepulzera.notes.ds.model.Note;

public interface NoteService {
  Note copy(@NonNull final Context context, @NonNull final Note from);

  Note clone(@NonNull final Note from);

  void delete(@NonNull final Note note);

  Note get(long id);

  Note getDraft(@NonNull final Note note);

  Note getCurrRevision(@NonNull final Note note);

  @SuppressWarnings("unused")
  Note getRevision(@NonNull final Note note, long revision);

  List<Note> getAll(@NonNull long[] ids);

  List<Note> getAllCurrent();

  List<Note> getAllDeleted();

  List<Note> getAllNoteRevisions(@NonNull final Note note);

  void restore(@NonNull final Note note);

  Note save(@NonNull final Note note);

  String toNoteTitle(@NonNull String title);

  void readPreferences(@NonNull final Context context);

  void restoreBackup(@NonNull final Context context, @NonNull final Uri backupFile);

  String getSaveBackup();

  void wipeTrash();
}

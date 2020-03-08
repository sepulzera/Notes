package de.sepulzera.notes.ui.adapter.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ds.model.Note;

public class NoteTrashAdapterImpl extends NoteAdapterImpl {
  public NoteTrashAdapterImpl(@NonNull final Context context) {
    super(context);
  }

  @Override
  public void refresh() {
    int numNotes = getSize();
    if (numNotes > 0) {
      final long[] ids = new long[numNotes];
      for (int i = 0; i < numNotes; ++i) {
        ids[i] = ((Note)getItem(i)).getId();
      }

      final NoteService srv = NoteServiceImpl.getInstance();
      final List<Note> notes = srv.getAll(ids);

      clear();
      for (final Note note : notes) {
        if (!note.getCurr() && note.getCurrRev()) {
          doPut(note);
        }
      }
    }

    sort();
    updateView();
  }

  @Override
  protected Date getTimestamp(@NonNull final Note note) {
    return note.getDeldt();
  }
}
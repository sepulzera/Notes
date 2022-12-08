package de.sepulzera.notes.ui.adapter.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

import de.sepulzera.notes.bf.helper.DateUtil;
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

  /**
   * <p>Sorts the notes descending by DELDT.</p>
   * <p>Drafts are always displayed above their corresponding revision.
   * (The DELDT for drafts and their revisions are equal.)</p>
   */
  @Override
  protected void sort() {
    getFilteredNotes().sort((note1, note2) -> {
      if (note1.getIdent() == note2.getIdent()) {
        if (note1.getDraft() && note2.getDraft()) return 0;
        return note1.getDraft() ? -1 : 1;
      }
      return DateUtil.compare(getTimestamp(note2), getTimestamp(note1));
    });
  }
}
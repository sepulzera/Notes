package de.sepulzera.notes.ui.adapter;

import android.support.annotation.NonNull;
import android.widget.ListAdapter;

import de.sepulzera.notes.ds.model.Note;

public interface NoteAdapter extends ListAdapter {

  /**
   * <p>Entfernt alle Items.</p>
   *
   * <p>Ruft {@link NoteAdapter#updateView} auf.</p>
   */
  void clear();

  /**
   * <p>Gibt die Größe der Liste insgesamt zurück.</p>
   * <p>Die Size ist nicht gleicht die Anzahl der sichtbaren Elemente.
   * Dafür ist die Methode {@linkplain ListAdapter#getCount()} zu verwenden.</p>
   *
   * @return Größe der Liste (ungefiltert).
   */
  int getSize();

  void filter(@NonNull String filter);

  /**
   * <p>Fügt die übergebene Notiz an die Liste an, oder aktualisiert falls vorhanden.</p>
   *
   * <p>Ruft {@link NoteAdapter#updateView} auf.</p>
   *
   * @param note Notiz
   */
  void put(@NonNull Note note);

  /**
   * <p>Löscht die Notiz mit der angegebenen ID.</p>
   *
   * <p>Ruft {@link NoteAdapter()#updateView} auf.</p>
   *
   * @param note Note.
   */
  void remove(@NonNull Note note);

  /**
   * <p>Aktualisiert den Adapter.
   * Alle Items werden neu gelesen.</p>
   * <p>Der Aufruf ist sehr teuer und sollte mit bedacht getriggert werden!</p>
   * <p>Soll nur die View aktualisiert werden, sollte {@linkplain NoteAdapter#updateView()}
   * verwendet werden.</p>
   *
   * @see NoteAdapter#updateView()
   */
  void refresh();

  /**
   * <p>Aktualisiert den View.</p>
   * <p>Die Items werden nicht neu gelesen. In dem Fall ist {@link NoteAdapter#refresh()}
   * zu verwenden.</p>
   *
   * @see NoteAdapter#refresh()
   */
  void updateView();
}

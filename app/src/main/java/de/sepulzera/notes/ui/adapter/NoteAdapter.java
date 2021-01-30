package de.sepulzera.notes.ui.adapter;

import androidx.annotation.NonNull;
import android.widget.ListAdapter;

import java.util.List;

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
   * <p>Entfernt die übergebene Notiz aus der Liste.</p>
   *
   * @param note Notiz
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



  /* SELECTION */

  /**
   * Selects the item at the given position.
   *
   * @param position Item's position, that should be selected.
   */
  void setNewSelection(int position);

  /**
   * Returns if the item at the given position is selected.
   *
   * @param position Item's position to check.
   *
   * @return {@code true}: Item is selected - {@code false}: not.
   */
  boolean isPositionChecked(int position);

  /**
   * Get all currently selected items.
   *
   * @return List with all selected items, asc sorted.
   */
  List<Note> getCheckedItems();

  /**
   * Removes the selection for the given position.
   *
   * @param position Item's position, that should be unselected.
   */
  void removeSelection(int position);

  /**
   * Removes all selections.
   */
  void clearSelection();
}

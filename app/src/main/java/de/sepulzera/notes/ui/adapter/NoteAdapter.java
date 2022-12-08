package de.sepulzera.notes.ui.adapter;

import androidx.annotation.NonNull;
import android.widget.ListAdapter;

import java.util.List;

import de.sepulzera.notes.ds.model.Note;

public interface NoteAdapter extends ListAdapter {

  /**
   * <p>Removes all items.</p>
   *
   * <p>Calls {@link NoteAdapter#updateView}.</p>
   */
  void clear();

  /**
   * <p>Returns the number of ALL items.</p>
   * <p>The size is not equals the number of visible items.
   * Use {@linkplain ListAdapter#getCount()} to get the number of visible items only.</p>
   *
   * @return Number of all items.
   *
   * @see ListAdapter#getCount()
   */
  int getSize();

  /**
   * Sets the filter. Only notes that match the filter will be displayed.
   *
   * @param filter ...
   */
  void filter(@NonNull String filter);

  /**
   * <p>Add or update.</p>
   *
   * <p>Calls {@link NoteAdapter#updateView}.</p>
   *
   * @param note ...
   */
  void put(@NonNull Note note);

  /**
   * <p>Removes the note.</p>
   *
   * @param note ..
   */
  void remove(@NonNull Note note);

  /**
   * <p>Re-reads all items.</p>
   * <p>! Potentially very resource-heavy for large lists. Use with caution !</p>
   * <p>If only the view should be refreshed, use {@linkplain NoteAdapter#updateView()} instead.</p>
   *
   * @see NoteAdapter#updateView()
   */
  void refresh();

  /**
   * <p>Updates the view, e. g. to remove deleted notes from display.</p>
   * <p>The items will not be re-read. Only the view will be updated to display the current state.
   * <p>If the items should be re-read, use {@link NoteAdapter#refresh()} instead.</p>
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

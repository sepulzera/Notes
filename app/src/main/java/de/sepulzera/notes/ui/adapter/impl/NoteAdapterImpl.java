package de.sepulzera.notes.ui.adapter.impl;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.DateUtil;
import de.sepulzera.notes.bf.helper.StringUtil;
import de.sepulzera.notes.bf.service.NoteService;
import de.sepulzera.notes.bf.service.impl.NoteServiceImpl;
import de.sepulzera.notes.ds.model.Note;
import de.sepulzera.notes.ui.adapter.NoteAdapter;

/**
 * Adapter Liste von Notizen.
 */
@SuppressWarnings("WeakerAccess")
public class NoteAdapterImpl extends BaseAdapter
    implements NoteAdapter, Filterable {

  public NoteAdapterImpl(@NonNull final Context context) {
    mInflater = LayoutInflater.from(context);

    mNotes         = new ArrayList<>();
    mFilteredNotes = new ArrayList<>();
    mEmptyContent = context.getResources().getString(R.string.note_item_empty_content);
    mDraftTitle   = context.getResources().getString(R.string.note_item_draft) + " ";
  }

  @Override
  public void clear() {
    mNotes.clear();
    mFilteredNotes.clear();
    updateView();
  }

  @Override
  public void put(@NonNull final Note note) {
    doPut(note);

    filter(mSearch);
  }

  protected void doPut(@NonNull final Note note) {
    if (null != get(note.getId())) {
      doRemove(note);
    }

    notifyDataSetChanged();
    mNotes.add(note);
  }

  @Override
  public void remove(@NonNull final Note note) {
    doRemove(note);

    notifyDataSetChanged();
  }

  private void doRemove(@NonNull final Note note) {
    long id = note.getId();
    for (final Note nextNote : mNotes) {
      if (id == nextNote.getId()) {
        mNotes.remove(nextNote);
        break;
      }
    }
  }

  @Override
  public void refresh() {
    if (!mNotes.isEmpty()) {
      int numNotes = mNotes.size();

      final long[] ids = new long[numNotes];
      for (int i = 0; i < numNotes; ++i) {
        ids[i] = mNotes.get(i).getId();
      }

      final NoteService srv = NoteServiceImpl.getInstance();
      final List<Note> notes = srv.getAll(ids);

      clear();
      for (final Note note : notes) {
        if (note.getCurr() && note.getCurrRev()) {
          doPut(note);
        }
      }
    }

    filter(mSearch);
  }

  @Override
  public void updateView() {
    mNow = Calendar.getInstance().getTime();
    this.notifyDataSetChanged();
    sort();
  }

  /**
   * <p>Sortiert die Notizen absteigend nach dem LCHADT.</p>
   */
  protected void sort() {
    Collections.sort(mFilteredNotes, new Comparator<Note>() {
      @Override
      public int compare(Note note1, Note note2) {
        return DateUtil.compare(getTimestamp(note2), getTimestamp(note1));
      }
    });
  }

  protected List<Note> getFilteredNotes() {
    return mFilteredNotes;
  }

  /**
   * <p>Gibt die Notiz mit der angegebenen ID zurück.</p>
   *
   * @param id ID der gesuchten Notiz.
   * @return Notiz mit der angegebenen ID oder null wenn nicht gefunden.
   */
  protected Note get(long id) {
    for (Note note : mNotes) {
      if (note.getId() == id) {
        return note;
      }
    }
    return null; // not found
  }

  @Override
  public int getCount() {
    return mFilteredNotes.size();
  }

  @Override
  public int getSize() { return mNotes.size(); }

  @Override
  public Object getItem(int position) {
    return mFilteredNotes.get(position);
  }

  @Override
  public long getItemId(int position) {
    return mFilteredNotes.get(position).getId();
  }

  protected Date getTimestamp(@NonNull final Note note) {
    return note.getLchadt();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    // falls nötig, convertView bauen
    if (convertView == null) {
      // Layoutdatei entfalten
      convertView = mInflater.inflate(R.layout.item_note, parent, false);
      // Holder erzeugen
      holder = new ViewHolder();
      holder.title = convertView.findViewById(R.id.title);
      holder.msg   = convertView.findViewById(R.id.msg);
      holder.timestamp = convertView.findViewById(R.id.timestamp);
      convertView.setTag(holder);
    } else {
      // Holder bereits vorhanden
      holder = (ViewHolder) convertView.getTag();
    }

    Note note = (Note) getItem(position);
    holder.timestamp.setText(DateUtil.formatDatePastShort(mNow, getTimestamp(note)));

    final String displayedTitle = note.getDraft()? mDraftTitle + note.getTitle() : note.getTitle();

    String content = StringUtil.defaultIfNull(note.getMsg(), "").replace(StringUtil.LINE_ENDING, "   ");

    if (!StringUtil.isEmpty(mSearch)) {
      int ixBegin = displayedTitle.toLowerCase().indexOf(mSearch);
      if (-1 != ixBegin) {
        Spannable spanText = Spannable.Factory.getInstance().newSpannable(displayedTitle);
        spanText.setSpan(new ForegroundColorSpan(Color.RED), ixBegin, ixBegin + mSearchLen
            , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.title.setText(spanText, TextView.BufferType.SPANNABLE);
      }

      ixBegin = content.toLowerCase().indexOf(mSearch);
      if (-1 != ixBegin) {
        int ixFrom = getProperBegin(content, ixBegin);
        if (ixFrom > 0) { ixBegin = ixBegin - ixFrom + 4; } // displayed content starts on ixFrom + "... "
        Spannable spanText = Spannable.Factory.getInstance().newSpannable(
            ixFrom > 0? "... " + content.substring(ixFrom) : content.substring(ixFrom));
        spanText.setSpan(new ForegroundColorSpan(Color.RED)
            , ixBegin, ixBegin + mSearchLen
            , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.msg.setText(spanText, TextView.BufferType.SPANNABLE);
      }
    } else {
      holder.title.setText(displayedTitle);
      holder.msg.setText(StringUtil.isBlank(content)? mEmptyContent : content);
    }

    return convertView;
  }

  private int getProperBegin(@NonNull String content, int ixBegin) {
    if (ixBegin < 10) {
      return ixBegin;
    }

    int ixFrom = content.substring(0, ixBegin - 5).lastIndexOf(" ");
    if (ixFrom == -1) { ixFrom = 0; }

    return ixFrom;
  }

  @Override
  public void filter(@NonNull String filter) {
    mSearch = filter.toLowerCase();
    mSearchLen = mSearch.length();

    getFilter().filter(mSearch);
  }

  @Override
  public Filter getFilter() {
    if (null == mFiler) {
      mFiler = new NoteFilter();
    }
    return mFiler;
  }

  class NoteFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence filter) {
      FilterResults results = new FilterResults();

      if (StringUtil.isEmpty(filter)) {
        results.count  = mNotes.size();
        results.values = mNotes;
      } else {
        final ArrayList<Note> tmpList  = new ArrayList<>();

        for (final Note note : mNotes) {
          if (note.getTitle().toLowerCase().contains(filter)
              || note.getMsg().toLowerCase().contains(filter)) {
            tmpList.add(note);
          }
        }

        results.count  = tmpList.size();
        results.values = tmpList;
      }

      return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
      mFilteredNotes = (ArrayList<Note>) filterResults.values;
      updateView();
    }
  }

  static class ViewHolder {
    TextView title, msg, timestamp;
  }

  private   final List<Note>     mNotes;
  private         List<Note>     mFilteredNotes;

  private   final LayoutInflater mInflater;

  private   final String         mEmptyContent;
  private   final String         mDraftTitle;

  private         Date           mNow;

  private         Filter         mFiler = null;
  private         String         mSearch = "";
  private         int            mSearchLen = 0;
}

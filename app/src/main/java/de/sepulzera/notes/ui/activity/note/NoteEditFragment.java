package de.sepulzera.notes.ui.activity.note;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

import de.sepulzera.notes.R;
import de.sepulzera.notes.bf.helper.StringUtil;
import de.sepulzera.notes.ds.model.Note;

public class NoteEditFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    final View view = (container == null)? getView() : inflater.inflate(R.layout.frag_note, container, false);
    if (view == null) {
      throw new IllegalStateException("Could not find view!");
    }

    String msg = null;
    if (null != savedInstanceState) {
      mIndex      = savedInstanceState.getInt(KEY_INDEX, -1);

      mNote       = (Note) savedInstanceState.getSerializable(Note.TAG_NOTE);
      msg         = savedInstanceState.getString(KEY_MSG);
      mIsEditable = savedInstanceState.getBoolean(KEY_EDITABLE, true);
    }

    if (null == mNote || mIndex == -1) {
      throw new IllegalStateException("Initialization was not called!");
    }

    final View mView = view.findViewById(R.id.main_content);
    mEditMsg = mView.findViewById(R.id.note_msg);
    mTvMsg = mView.findViewById(R.id.tv_note_msg);

    setEditable(mIsEditable);
    setMsg(msg != null? msg : mNote.getMsg());

    // only show keyboard on new notes
    if (0L == mNote.getId()) {
      mEditMsg.requestFocus();
    } else {
      mView.requestFocus();
    }

    return mView;
  }

  @Override
  public void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(KEY_INDEX, mIndex);
    outState.putSerializable(Note.TAG_NOTE, mNote);
    outState.putBoolean(KEY_EDITABLE, mIsEditable);
  }

  public void initialize(int index, @NonNull final Note note) {
    mIndex = index;
    mNote = note;
    mIsEditable = note.getId() == 0L || !NoteTabViewerActivity.mOpenNotesReadonly;
  }
  public Note getNote() { return mNote; }
  public int getIndex() { return mIndex; }

  public void copyToClipboard() {
    ClipboardManager cman = ((ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE));
    if (cman != null) {
      cman.setPrimaryClip(
          ClipData.newPlainText("notes_" + mNote.getTitle(), getMsg()));
      // Android Design Support Library: Snack shown below the screen
      // Snackbar.make(mView, getResources().getString(R.string.copied_to_clipboard), Snackbar.LENGTH_LONG).show();
    }
  }

  public void clearNote() {
    setMsg("");
  }

  public void revert() {
    setMsg(mNote.getMsg());
  }

  public boolean isEditable() { return mIsEditable; }

  public void setEditable(boolean editable) {
    mIsEditable = editable;
    if (editable) {
      mEditMsg.setVisibility(View.VISIBLE);
      mTvMsg.setVisibility(View.GONE);
    } else {
      mEditMsg.setVisibility(View.GONE);
      mTvMsg.setVisibility(View.VISIBLE);
    }
  }

  public boolean isChanged() {
    return !StringUtil.equals(getMsg(), mNote.getMsg());
  }

  public String getMsg() {
    return mEditMsg.getText().toString();
  }

  private void setMsg(@NonNull String msg) {
    mEditMsg.setText(msg);
    mTvMsg.setText(msg);
  }

  private EditText mEditMsg;
  private TextView mTvMsg;

  private int  mIndex = -1;
  private Note mNote;
  private boolean mIsEditable;

  private static final String KEY_INDEX    = "noteEditFrag_index";
  private static final String KEY_MSG      = "noteEditFrag_msg";
  private static final String KEY_EDITABLE = "noteEditFrag_editable";
}
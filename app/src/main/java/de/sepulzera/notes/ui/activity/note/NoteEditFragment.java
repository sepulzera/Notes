package de.sepulzera.notes.ui.activity.note;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;
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

    mView = view.findViewById(R.id.main_content);
    mEditMsg = mView.findViewById(R.id.note_msg);

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
    }
  }

  public void clearNote() {
    setMsg("");
  }

  public void revert() {
    setMsg(mNote.getMsg());
  }

  public void deleteSelectedLines() {
    String msgDeletedLine = StringUtil.deleteLines(getMsg(), mEditMsg.getSelectionStart(), mEditMsg.getSelectionEnd());
    setMsgAndFixSelection(msgDeletedLine);
  }

  public void duplicateSelectedLines() {
    String msgCopiedLine = StringUtil.duplicateLines(getMsg(), mEditMsg.getSelectionStart(), mEditMsg.getSelectionEnd());
    setMsgAndFixSelection(msgCopiedLine);
  }

  private void setMsgAndFixSelection(@NonNull String msg) {
    int selStart = mEditMsg.getSelectionStart();
    int selEnd   = mEditMsg.getSelectionEnd();

    setMsg(msg);
    int len = msg.length();

    selStart = (selStart > len? len : selStart);
    selEnd = (selEnd > len? len : selEnd);
    mEditMsg.setSelection(selStart, selEnd);
  }

  public void moveSelectedLinesUp() {
    String msgMovedLine = StringUtil.moveLinesUp(getMsg(), mEditMsg.getSelectionStart(), mEditMsg.getSelectionEnd());

    String oldMsg = getMsg();

    int selStart = mEditMsg.getSelectionStart();
    int selEnd   = mEditMsg.getSelectionEnd();

    List<String> lines = StringUtil.getLines(oldMsg);
    int[] selectedLines = StringUtil.getSelectedLines(oldMsg, selStart, selEnd);
    if (selectedLines[0] == 0) return;
    String movedLine = lines.get(selectedLines[0] - 1);

    setMsg(msgMovedLine);
    mEditMsg.setSelection(selStart - movedLine.length() - 1, selEnd - movedLine.length() - 1);
  }

  public void moveSelectedLinesDown() {
    String msgMovedLine = StringUtil.moveLinesDown(getMsg(), mEditMsg.getSelectionStart(), mEditMsg.getSelectionEnd());

    String oldMsg = getMsg();

    int selStart = mEditMsg.getSelectionStart();
    int selEnd   = mEditMsg.getSelectionEnd();

    List<String> lines = StringUtil.getLines(oldMsg);
    int[] selectedLines = StringUtil.getSelectedLines(oldMsg, selStart, selEnd);
    if (selectedLines[selectedLines.length - 1] == (lines.size() - 1)) return;
    String movedLine = lines.get(selectedLines[selectedLines.length - 1] + 1);

    setMsg(msgMovedLine);
    mEditMsg.setSelection(selStart + movedLine.length() + 1, selEnd + movedLine.length() + 1);
  }

  public boolean isEditable() { return mIsEditable; }

  public void setEditable(boolean editable) {
    mIsEditable = editable;
    if (editable) {
      mEditMsg.setEnabled(true);
    } else {
      mEditMsg.setEnabled(false);
    }

    mView.setBackgroundColor(getResources().getColor(editable? R.color.colorNoteBg : R.color.colorNoteBgReadonly, null));
  }

  public boolean isChanged() {
    return !StringUtil.equals(getMsg(), mNote.getMsg());
  }

  public String getMsg() {
    return mEditMsg.getText().toString();
  }

  private void setMsg(@NonNull String msg) {
    mEditMsg.setText(msg);
  }

  private EditText mEditMsg;

  private int  mIndex = -1;
  private CoordinatorLayout mView;
  private Note mNote;
  private boolean mIsEditable;

  private static final String KEY_INDEX    = "noteEditFrag_index";
  private static final String KEY_MSG      = "noteEditFrag_msg";
  private static final String KEY_EDITABLE = "noteEditFrag_editable";
}
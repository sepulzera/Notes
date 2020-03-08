package de.sepulzera.notes.ui.widgets;

import android.content.Context;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class EditTextSelectable extends AppCompatEditText {
  public interface SelectionChangedListener {
    void onSelectionChanged(int selStart, int selEnd);
  }

  private List<SelectionChangedListener> mListeners;

  public EditTextSelectable(Context context) {
    super(context);
    init();
  }

  public EditTextSelectable(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EditTextSelectable(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public void addSelectionChangedListener(SelectionChangedListener o) {
    mListeners.add(o);
  }

  private void init() {
    mListeners = new ArrayList<>();
  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    super.onSelectionChanged(selStart, selEnd);
    
    if (mListeners == null) {
      init();
    }

    for (SelectionChangedListener l : mListeners) {
      l.onSelectionChanged(selStart, selEnd);
    }
  }
}

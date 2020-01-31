package de.sepulzera.notes.ui.widgets.rundo;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

/**
 * Implementation of {@link RunDo} which extends {@link Fragment}. It is best to create an
 * instance of this class with {@link de.sepulzera.notes.ui.widgets.rundo.RunDo.Factory}, rather than
 * with {@link #newInstance()} or {@link #RunDoNative()} directly.
 *
 * @author Tom Calver
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RunDoNative extends Fragment implements RunDo {

    private RunDo.TextLink mTextLink;
    private EditText mTextRef;
    private RunDo.Callbacks mCallbacks;

    private String mIdent;

    private Handler mHandler;
    private WriteToArrayDequeRunnable mRunnable;
    private boolean isRunning;
    private boolean undoRequested;

    private long countdownTimerLength;
    private int queueSize;

    private FixedSizeArrayDeque<SubtractStrings.Item> mUndoQueue, mRedoQueue;

    private String mOldText, mNewText;
    private int mOldSelectionStart, mOldSelectionEnd;
    private int trackingState;

    public RunDoNative() {
        mHandler = new Handler();
        mRunnable = new WriteToArrayDequeRunnable(this);

        countdownTimerLength = DEFAULT_TIMER_LENGTH;
        queueSize = DEFAULT_QUEUE_SIZE;

        trackingState = TRACKING_ENDED;
    }

    public static RunDoNative newInstance() {
        return new RunDoNative();
    }

    public static RunDoNative newInstance(String ident) {
        RunDoNative runDo = new RunDoNative();

        if (ident != null) {
            Bundle args = new Bundle();
            args.putString(IDENT_TAG, ident);
            runDo.setArguments(args);
        }

        return runDo;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mTextLink = (RunDo.TextLink) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RunDo.TextLink");
        }

        if (context instanceof RunDo.Callbacks) mCallbacks = (RunDo.Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mIdent = args != null? args.getString(IDENT_TAG) : null;

        if (mUndoQueue == null) mUndoQueue = new FixedSizeArrayDeque<>(queueSize);
        if (mRedoQueue == null) mRedoQueue = new FixedSizeArrayDeque<>(queueSize);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mUndoQueue = savedInstanceState.getParcelable(UNDO_TAG);
            mRedoQueue = savedInstanceState.getParcelable(REDO_TAG);

            mOldText = savedInstanceState.getString(OLD_TEXT_TAG);

            isRunning = savedInstanceState.getBoolean(CONFIG_CHANGE_TAG);

            if(isRunning) startCountdownRunnable();

            trackingState = savedInstanceState.getInt(TRACKING_TAG);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mTextRef = mTextLink.getEditTextForRunDo(mIdent);
        mTextRef.addTextChangedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(UNDO_TAG, mUndoQueue);
        outState.putParcelable(REDO_TAG, mRedoQueue);

        outState.putString(OLD_TEXT_TAG, mOldText);

        outState.putBoolean(CONFIG_CHANGE_TAG, isRunning);

        if (isRunning) stopCountdownRunnable();

        outState.putInt(TRACKING_TAG, trackingState);
    }

    @Override
    public void onDetach() {
        mTextRef = null;
        mTextLink = null;
        super.onDetach();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        if (mOldText == null) mOldText = mTextRef.getText().toString();

        if (trackingState == TRACKING_ENDED) {

            //Redo Queue should only be required as response to Undo calls. Otherwise clear.
            clearRedoQueue();

            startCountdownRunnable();
            trackingState = TRACKING_CURRENT;

            int currentSelStart = mTextRef.getSelectionStart();
            int currentSelEnd = mTextRef.getSelectionEnd();
            // Store selection to properly set the cursor on undo/redo
            // this is bit awkward, but not my bugs
            boolean isStartGiven = count > 1 && start + count == currentSelStart;
            mOldSelectionStart = isStartGiven ? start : currentSelStart;
            mOldSelectionEnd = isStartGiven ? start + count : currentSelEnd;

            if (mCallbacks != null && isQueueEmpty(mUndoQueue)) mCallbacks.undoAvailable();
        }

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        switch (trackingState) {
            case TRACKING_STARTED:
                trackingState = TRACKING_ENDED;
                break;
            case TRACKING_CURRENT:
                restartCountdownRunnable();
                break;
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
        //Unused
    }

    /**
     *
     * @see {@link WriteToArrayDeque#getNewString()}
     */
    @Override
    public String getNewString() {
        mNewText = mTextRef.getText().toString();
        return mNewText;
    }

    /**
     *
     * @see {@link WriteToArrayDeque#getOldString()}
     */
    @Override
    public String getOldString() {
        return mOldText;
    }

    /**
     *
     * @see {@link WriteToArrayDeque#notifyArrayDequeDataReady(de.sepulzera.notes.ui.widgets.rundo.SubtractStrings.Item)}
     */
    @Override
    public void notifyArrayDequeDataReady(SubtractStrings.Item item) {

        trackingState = TRACKING_ENDED;

        if (item.getDeviationType() == SubtractStrings.UNCHANGED) {
            if (mCallbacks != null && isQueueEmpty(mUndoQueue)) mCallbacks.undoEmpty();
            return;
        }

        item.setOldSelection(mOldSelectionStart, mOldSelectionEnd);
        item.setNewSelection(mTextRef.getSelectionStart(), mTextRef.getSelectionEnd());
        fillUndoQueue(item);

        mOldText = mTextRef.getText().toString();

        if (undoRequested) {
            undo();
        }
    }

    /**
     *
     * @see {@link WriteToArrayDeque#setIsRunning(boolean)}
     */
    @Override
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     *
     * @see {@link RunDo#getIdent()}
     */
    @Override
    public String getIdent() {
        return mIdent;
    }

    /**
     *
     * @see {@link RunDo#setQueueSize(int)}
     */
    @Override
    public void setQueueSize(int size) {
        queueSize = size;
        mUndoQueue = new FixedSizeArrayDeque<>(queueSize);
        mRedoQueue = new FixedSizeArrayDeque<>(queueSize);
    }

    /**
     *
     * @see {@link RunDo#setTimerLength(long)}
     */
    @Override
    public void setTimerLength(long lengthInMillis) {
        countdownTimerLength = lengthInMillis;
    }

    /**
     *
     * @see {@link RunDo#canUndo()}
     */
    @Override
    public boolean canUndo() {
        return !isQueueEmpty(mUndoQueue) || trackingState != TRACKING_ENDED;
    }

    /**
     *
     * @see {@link RunDo#canRedo()}
     */
    @Override
    public boolean canRedo() {
        return !isQueueEmpty(mRedoQueue);
    }

    /**
     *
     * @see {@link RunDo#undo()}
     */
    @Override
    public void undo() {

        if (isRunning && !undoRequested) {
            undoRequested = true;
            restartCountdownRunnableImmediately();
            return;
        }

        trackingState = TRACKING_STARTED;
        undoRequested = false;

        if (isQueueEmpty(mUndoQueue)) {
            //Log.e(TAG, "Undo Queue Empty");
            return;
        }

        try {

            SubtractStrings.Item temp = pollUndoQueue();

            switch (temp.getDeviationType()) {
                case SubtractStrings.ADDITION:
                    mTextRef.getText().delete(
                        temp.getFirstDeviation(),
                        temp.getLastDeviationNewText()
                    );
                    break;
                case SubtractStrings.DELETION:
                    mTextRef.getText().insert(
                        temp.getFirstDeviation(),
                        temp.getReplacedText());
                    break;
                case SubtractStrings.REPLACEMENT:
                    mTextRef.getText().replace(
                        temp.getFirstDeviation(),
                        temp.getLastDeviationNewText(),
                        temp.getReplacedText());
                    break;
                case SubtractStrings.UNCHANGED:
                    break;
                default:
                    break;
            }

            mTextRef.setSelection(temp.getOldSelectionStart(), temp.getOldSelectionEnd());

            fillRedoQueue(temp);

            if (mCallbacks != null) mCallbacks.undoCalled();

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            mOldText = mTextRef.getText().toString();
        }

    }

    /**
     *
     * @see {@link RunDo#redo()}
     */
    @Override
    public void redo() {

        trackingState = TRACKING_STARTED;

        if (isQueueEmpty(mRedoQueue)) {
            //Log.e(TAG, "Redo Queue Empty");
            return;
        }

        try {

            SubtractStrings.Item temp = pollRedoQueue();

            switch (temp.getDeviationType()) {
                case SubtractStrings.ADDITION:
                    mTextRef.getText().insert(
                        temp.getFirstDeviation(),
                        temp.getAlteredText());
                    break;
                case SubtractStrings.DELETION:
                    mTextRef.getText().delete(
                        temp.getFirstDeviation(),
                        temp.getLastDeviationOldText()
                    );
                    break;
                case SubtractStrings.REPLACEMENT:
                    mTextRef.getText().replace(
                        temp.getFirstDeviation(),
                        temp.getLastDeviationOldText(),
                        temp.getAlteredText());
                    break;
                case SubtractStrings.UNCHANGED:
                    break;
                default:
                    break;

            }

            mTextRef.setSelection(temp.getNewSelectionStart(), temp.getNewSelectionEnd());

            fillUndoQueue(temp);

            if (mCallbacks != null) mCallbacks.redoCalled();

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            mOldText = mTextRef.getText().toString();
        }

    }

    /**
     *
     * @see {@link RunDo#clearAllQueues()}
     */
    @Override
    public void clearAllQueues() {
        clearUndoQueue();
        clearRedoQueue();
    }

    private void clearUndoQueue() {
        mUndoQueue.clear();
        if (mCallbacks != null) mCallbacks.undoEmpty();
    }

    private void clearRedoQueue() {
        mRedoQueue.clear();
        if (mCallbacks != null) mCallbacks.redoEmpty();
    }

    private SubtractStrings.Item pollUndoQueue() {
        SubtractStrings.Item item = mUndoQueue.poll();
        if (mCallbacks != null && isQueueEmpty(mUndoQueue)) mCallbacks.undoEmpty();
        return item;
    }

    private SubtractStrings.Item pollRedoQueue() {
        SubtractStrings.Item item = mRedoQueue.poll();
        if (mCallbacks != null && isQueueEmpty(mRedoQueue)) mCallbacks.redoEmpty();
        return item;
    }

    private void fillUndoQueue(SubtractStrings.Item item) {
        if (mCallbacks != null && isQueueEmpty(mUndoQueue)) mCallbacks.undoAvailable();
        mUndoQueue.addFirst(item);
    }

    private void fillRedoQueue(SubtractStrings.Item item) {
        if (mCallbacks != null && isQueueEmpty(mRedoQueue)) mCallbacks.redoAvailable();
        mRedoQueue.addFirst(item);
    }

    private static boolean isQueueEmpty(FixedSizeArrayDeque<SubtractStrings.Item> queue) {
        return queue == null || queue.peek() == null;
    }

    private void restartCountdownRunnableImmediately() {
        stopCountdownRunnable();
        mHandler.post(mRunnable);
    }

    private void startCountdownRunnable() {
        isRunning = true;
        mHandler.postDelayed(mRunnable, countdownTimerLength);
    }

    private void stopCountdownRunnable() {
        mHandler.removeCallbacks(mRunnable);
        isRunning = false;
    }

    private void restartCountdownRunnable() {
        stopCountdownRunnable();
        startCountdownRunnable();
    }

}
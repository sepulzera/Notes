package de.sepulzera.notes.ui.widgets.rundo;

import java.lang.ref.WeakReference;

final class WriteToArrayDequeRunnable implements Runnable {

    private final WeakReference<WriteToArrayDeque> mWriteToArrayDeque;

    WriteToArrayDequeRunnable(WriteToArrayDeque writeToArrayDeque) {
        mWriteToArrayDeque = new WeakReference<>(writeToArrayDeque);
    }

    @Override
    public void run() {

        try {
            WriteToArrayDeque writeToArrayDeque = mWriteToArrayDeque.get();

            writeToArrayDeque.setIsRunning(true);

            String mNewString = writeToArrayDeque.getNewString();
            String mOldString = writeToArrayDeque.getOldString();

            SubtractStrings.Item mItem = new SubtractStrings(mOldString, mNewString).getItem();

            writeToArrayDeque.notifyArrayDequeDataReady(mItem);

            writeToArrayDeque.setIsRunning(false);

        } catch (NullPointerException e) {
            //Occurs on config change
        }

    }

}

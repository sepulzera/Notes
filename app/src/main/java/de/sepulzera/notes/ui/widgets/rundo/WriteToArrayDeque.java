package de.sepulzera.notes.ui.widgets.rundo;

interface WriteToArrayDeque {

    String getNewString();
    String getOldString();

    void notifyArrayDequeDataReady(SubtractStrings.Item item);

    void setIsRunning(boolean isRunning);

}

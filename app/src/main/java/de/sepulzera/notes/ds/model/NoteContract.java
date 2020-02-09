package de.sepulzera.notes.ds.model;

import android.provider.BaseColumns;

public final class NoteContract {
  public static class NoteEntry implements BaseColumns {
    public static final String TABLE_NAME    = "note";

    public static final String COL_IDENT     = "ident";
    public static final String COL_REVISION  = "revision";
    public static final String COL_CURR_REV  = "currrev";
    public static final String COL_DRAFT     = "draft";

    public static final String COL_TITLE     = "title";
    public static final String COL_MSG       = "message";

    public static final String COL_DELDT     = "deldt";

    public static final String COL_CURR      = "curr";
    public static final String COL_CREADT    = "creadt";
    public static final String COL_LCHADT    = "lchadt";

    public static final String[] PROJECTION = {
        BaseColumns._ID
        , NoteEntry.COL_IDENT
        , NoteEntry.COL_REVISION
        , NoteEntry.COL_CURR_REV
        , NoteEntry.COL_DRAFT
        , NoteEntry.COL_TITLE
        , NoteEntry.COL_MSG
        , NoteEntry.COL_DELDT
        , NoteEntry.COL_CURR
        , NoteEntry.COL_CREADT
        , NoteEntry.COL_LCHADT
    };
  }

  private NoteContract() {
    // contract class
  }
}

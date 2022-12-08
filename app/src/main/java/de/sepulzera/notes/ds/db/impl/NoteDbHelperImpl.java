package de.sepulzera.notes.ds.db.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import de.sepulzera.notes.ds.model.NoteContract.NoteEntry;

class NoteDbHelperImpl extends SQLiteOpenHelper {

  private static final int    DATABASE_VERSION = 4;
  private static final String DATABASE_NAME = "Note.db";

  NoteDbHelperImpl(@NonNull final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_NOTE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 4) {
      doUpgradeToVersion4(db);
      return;
    }

    // no action needed at this point
    db.execSQL(SQL_DROP_NOTE_TABLE);
    onCreate(db);
  }

  private void doUpgradeToVersion4(SQLiteDatabase db) {
    db.execSQL(SQL_ALTER_TABLE_UPGRADE_4);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // no action needed at this point
    db.execSQL(SQL_DROP_NOTE_TABLE);
    onCreate(db);
  }

  private static final String SQL_CREATE_NOTE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + NoteEntry.TABLE_NAME + "("
      + NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + NoteEntry.COL_IDENT + " INTEGER NOT NULL,"
      + NoteEntry.COL_REVISION + " INTEGER NOT NULL DEFAULT 1,"
      + NoteEntry.COL_CURR_REV + " INTEGER NOT NULL DEFAULT 1,"
      + NoteEntry.COL_DRAFT + " INTEGER NOT NULL DEFAULT 0,"
      + NoteEntry.COL_TITLE + " TEXT,"
      + NoteEntry.COL_MSG + " TEXT,"
      + NoteEntry.COL_DELDT + " INTEGER,"
      + NoteEntry.COL_CURR + " INTEGER NOT NULL DEFAULT 1,"
      + NoteEntry.COL_CREADT + " INTEGER NOT NULL,"
      + NoteEntry.COL_LCHADT + " INTEGER NOT NULL)";

  private static final String SQL_DROP_NOTE_TABLE =
      "DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME;

  private static final String SQL_ALTER_TABLE_UPGRADE_4 =
      "ALTER TABLE " + NoteEntry.TABLE_NAME
      + " ADD COLUMN " + NoteEntry.COL_DELDT + " INTEGER";
}

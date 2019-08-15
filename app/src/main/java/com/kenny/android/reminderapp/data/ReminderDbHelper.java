package com.kenny.android.reminderapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.kenny.android.reminderapp.data.ReminderContract.*;

/**
 * Created by TAYE on 10/03/2018.
 */

public class ReminderDbHelper extends SQLiteOpenHelper {

    //Name of database
    private static final String DATABASE_NAME = "activity.db";

    //Database version, if you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // String to create database table
    String SQL_CREATE_REMINDER_TABLE = "CREATE TABLE " + ReminderEntry.TABLE_NAME + "("
            + ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ReminderEntry.COLUMN_REMINDER_TITLE + " TEXT NOT NULL, "
            + ReminderEntry.COLUMN_REMINDER_DETAILS + " TEXT NOT NULL, "
            + ReminderEntry.COLUMN_REMINDER_DATETIME + " INTEGER NOT NULL);";

    public ReminderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_REMINDER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        onCreate(db);
    }
}

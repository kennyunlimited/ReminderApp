package com.kenny.android.reminderapp.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TAYE on 10/03/2018.
 */

public class ReminderContract {

    private ReminderContract() {

    }

    /**
     * +     * The "Content authority" is a name for the entire content provider, similar to the
     * +     * relationship between a domain name and its website.  A convenient string to use for the
     * +     * content authority is the package name for the app, which is guaranteed to be unique on the
     * +     * device.
     * +
     */
    public static final String CONTENT_AUTHORITY = "com.kenny.android.reminderapp";

    /**
     * +     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * +     * the content provider.
     * +
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    // Name of the database table for reminders
    public static final String PATH_REMINDERS = "reminders";


    public static final class ReminderEntry implements BaseColumns {

        /** The content URI to access the reminder data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REMINDERS);

        //Database Table name
        public static final String TABLE_NAME = "reminders";

        //The MIME type for a list of reminders.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_REMINDERS;

        //The MIME type for a single reminder.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.ANY_CURSOR_ITEM_TYPE + "/" + CONTENT_AUTHORITY + PATH_REMINDERS;

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_REMINDER_TITLE = "title";
        public static final String COLUMN_REMINDER_DETAILS = "details";
        public static final String COLUMN_REMINDER_DATETIME = "datetime";

    }

//    public static String getColumnString(Cursor cursor, String columnName) {
//        return cursor.getString(cursor.getColumnIndex(columnName));
//    }
}

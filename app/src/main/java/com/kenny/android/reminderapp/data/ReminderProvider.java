package com.kenny.android.reminderapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.kenny.android.reminderapp.data.ReminderContract.ReminderEntry;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by TAYE on 12/03/2018.
 */

public class ReminderProvider extends ContentProvider {


    //Database helper object
    ReminderDbHelper dbHelper;

    // URI matcher code for the content URI for the reminders table
    private static final int REMINDERS = 100;

    // URI matcher code for the content URI for a single reminder in the reminders table
    private static final int REMINDER_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        mUriMatcher.addURI(ReminderContract.CONTENT_AUTHORITY, ReminderContract.PATH_REMINDERS, REMINDERS);
        mUriMatcher.addURI(ReminderContract.CONTENT_AUTHORITY, ReminderContract.PATH_REMINDERS + "/#", REMINDER_ID);
    }



    @Override
    public boolean onCreate() {
        dbHelper = new ReminderDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

       // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = mUriMatcher.match(uri);
        switch (match) {
            case (REMINDERS):
                // For the REMINDERS code, query the reminders table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the reminders table.
                cursor = database.query(ReminderEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case (REMINDER_ID):
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(ReminderEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        // Set notification URI on the cursor, so we know what content URI the cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return ReminderEntry.CONTENT_LIST_TYPE;
            case REMINDER_ID:
                return ReminderEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */

    @Override
    public Uri insert( Uri uri, ContentValues values) {

        int match = mUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                 return insertReminder(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertReminder(Uri uri, ContentValues values) {
        // Check that title is not null
        String title = values.getAsString(ReminderEntry.COLUMN_REMINDER_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Reminder title required");
        }

        // Check that detail is not null
        String details = values.getAsString(ReminderEntry.COLUMN_REMINDER_DETAILS);
        if (details == null) {
            throw new IllegalArgumentException("Reminder requires detail");
        }

        //Check that date and time is valid
        Long dateTime = values.getAsLong(ReminderEntry.COLUMN_REMINDER_DATETIME);
        if (dateTime == null) {
            throw new IllegalArgumentException("Date and time required");
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        final long id = database.insert(ReminderEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(ReminderProvider.class.getSimpleName(),
                    "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        Uri uri1 = ContentUris.withAppendedId(uri, id);
        return uri1;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //Track the number of rows that were deleted
        int rowsDeleted;

        int match = mUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REMINDER_ID:
                // Delete a single row given by the ID in the URI
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))
                };
                rowsDeleted = database.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        //If one or more rows are deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = mUriMatcher.match(uri);
        switch (match) {
            case REMINDERS:
                return updateReminder(uri, values, selection, selectionArgs);
            case REMINDER_ID:
                selection = ReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updateReminder(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    private int updateReminder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ReminderEntry.COLUMN_REMINDER_TITLE)) {
            String title = values.getAsString(ReminderEntry.COLUMN_REMINDER_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Reminder title required...");
            }
        }

        if (values.containsKey(ReminderEntry.COLUMN_REMINDER_DETAILS)) {
            String details = values.getAsString(ReminderEntry.COLUMN_REMINDER_DETAILS);
            if (details == null) {
                throw new IllegalArgumentException("Reminder details required...");
            }
        }

        if (values.containsKey(ReminderEntry.COLUMN_REMINDER_DATETIME)) {
            Long dateTime = values.getAsLong(ReminderEntry.COLUMN_REMINDER_DATETIME);
            if (dateTime == null) {
                throw new IllegalArgumentException("Date and time required..");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int updatedRows = database.update(ReminderEntry.TABLE_NAME, values, selection, selectionArgs);

        if (updatedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }
}

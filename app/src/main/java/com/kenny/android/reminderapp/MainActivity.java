package com.kenny.android.reminderapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kd.dynamic.calendar.generator.ImageGenerator;
import com.kenny.android.reminderapp.data.ReminderContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.kenny.android.reminderapp.data.ReminderContract.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the pet data loader
    private static final int REMINDER_LOADER = 0;

    ListView listView;
    FloatingActionButton fab;
    ImageView emptyImageView;
    ReminderCursorAdapter reminderCursorAdapter;
    Calendar mCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        fab = (FloatingActionButton) findViewById(R.id.floatingButton);
        emptyImageView = (ImageView) findViewById(R.id.emptyImageView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });

        listView = (ListView) findViewById(R.id.listView);
        View emptyView = findViewById(R.id.emptyView);

        listView.setEmptyView(emptyView);

        reminderCursorAdapter = new ReminderCursorAdapter(this, null);
        listView.setAdapter(reminderCursorAdapter);

        // Setup item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentReminderUri = ContentUris.withAppendedId(ReminderEntry.CONTENT_URI, id);

                intent.setData(currentReminderUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(REMINDER_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insertDummyData:
                insertDummyReminder();
                return true;
            case R.id.deleteAllReminders:
                showDeleteConfirmationDialog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete all reminders?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllReminders();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllReminders() {
        int delete = getContentResolver().delete(ReminderEntry.CONTENT_URI, null, null);

        if (delete == 0) {
            Toast.makeText(this, "Reminder Delete Failed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, delete +" Reminder Rows Deleted Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertDummyReminder() {
        mCurrentDate = Calendar.getInstance();
        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        Date d = new Date(year-1900, month, day);
        long time = d.getTime();

        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_REMINDER_TITLE,"Taye's Birthday");
        values.put(ReminderEntry.COLUMN_REMINDER_DETAILS, "I will be going to Lagos tomorrow for Taye's birthday");
        values.put(ReminderEntry.COLUMN_REMINDER_DATETIME, time);

        Uri newRowId = getContentResolver().insert(ReminderEntry.CONTENT_URI, values);
        Toast.makeText(this, "Reminder Successfully added", Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", "New Row ID: " + newRowId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_REMINDER_TITLE,
                ReminderEntry.COLUMN_REMINDER_DATETIME
        };

        return new CursorLoader(this, ReminderEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        reminderCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        reminderCursorAdapter.swapCursor(null);
    }
}

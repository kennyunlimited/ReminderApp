package com.kenny.android.reminderapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kd.dynamic.calendar.generator.ImageGenerator;
import com.kenny.android.reminderapp.data.ReminderContract;
import com.kenny.android.reminderapp.data.ReminderContract.ReminderEntry;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int EXISTING_REMINDER_LOADER = 0;

    EditText mDateEditText;
    EditText mTimeEditText;
    EditText mTitleEditText;
    EditText mReminderEditText;
    Calendar mCurrentDate;
    Bitmap mGeneratedDateIcon;
    ImageGenerator mImageGenerator;
    ImageView mDisplayGeneratedImage;
    long time;

    int year;
    int month;
    int day;

    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri currentReminderUri;

    //Boolean flag that keeps track of whether the reminder has been edited (true) or not (false)
    private boolean mReminderHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mReminderHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mReminderHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentReminderUri = intent.getData();

        // If the intent DOES NOT contain a reminder content URI, then we know that we are creating a new pet.
        if (currentReminderUri == null) {
            setTitle("Create Reminder");

            // Invaliate the option menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a reminder that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        else {
            setTitle("Edit Reminder");

            // initialize a loader to read the reminder data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_REMINDER_LOADER, null, this);
        }

        // Initialize Views
        mTitleEditText = (EditText) findViewById(R.id.lTitleEditText);
        mReminderEditText = (EditText) findViewById(R.id.lWishEditText);
        mDateEditText = (EditText) findViewById(R.id.ltxtDateEntered);
        mTimeEditText = (EditText) findViewById(R.id.ltxtTimeEntered);
        mDisplayGeneratedImage = (ImageView) findViewById(R.id.imageGen);

        /**
         *  Set up OnTouchListeners on all the input fields, so we can determine if the user
         *  has touched or modified them. This will let us know if there are unsaved changes
         *  or not, if the user tries to leave the editor without saving.
         */
        mTitleEditText.setOnTouchListener(mTouchListener);
        mReminderEditText.setOnTouchListener(mTouchListener);
        mDateEditText.setOnTouchListener(mTouchListener);
        mTimeEditText.setOnTouchListener(mTouchListener);

        startTyping();

        // Create an object of ImageGenerator class in your activity
// and pass the context as the parameter
        mImageGenerator = new ImageGenerator(this);

// Set the icon size to the generated in dip.
        mImageGenerator.setIconSize(50, 50);

// Set the size of the date and month font in dip.
        mImageGenerator.setDateSize(30);
        mImageGenerator.setMonthSize(10);

// Set the position of the date and month in dip.
        mImageGenerator.setDatePosition(42);
        mImageGenerator.setMonthPosition(14);

// Set the color of the font to be generated
        mImageGenerator.setDateColor(Color.parseColor("#3c6eaf"));
        mImageGenerator.setMonthColor(Color.WHITE);

        // The images are stored in ../sdcard/CalendarImageGenerated/
        mImageGenerator.setStorageToSDCard(true);

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });

        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTime();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);

        // If this is a new reminder, hide the Delete menu item.
        if (currentReminderUri == null) {
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveReminder();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mReminderHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Discard your changes and quit editing?");
                builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                });
                builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when the back button is pressed
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mReminderHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that
        // changes should be discarded.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        });
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this reminder?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteSelectedReminder();
            }
        });
        builder.setNegativeButton("Don't Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteSelectedReminder() {

        int deleteRow = getContentResolver().delete(currentReminderUri, null, null);

        if (deleteRow == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, "Unable to delete reminder" , Toast.LENGTH_SHORT).show();
        }
        else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, "Reminder deleted successfully" , Toast.LENGTH_SHORT).show();
        }

        // Close the activity
        finish();
    }

    private void saveReminder() {
        // Read from input fields
        String titleString = mTitleEditText.getText().toString().trim();
        String wishString = mReminderEditText.getText().toString().trim();
        String timeString = mTimeEditText.getText().toString().trim();

        if (currentReminderUri == null && TextUtils.isEmpty(titleString) && TextUtils.isEmpty(wishString)
                && TextUtils.isEmpty(timeString)) {
            return;
        }

//        if (titleString.length() == 0) {
//            titleString.setError
//        }

        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_REMINDER_TITLE, titleString);
        values.put(ReminderEntry.COLUMN_REMINDER_DETAILS, wishString);
        values.put(ReminderEntry.COLUMN_REMINDER_DATETIME, time);

        // Determine if this is a new or existing reminder by checking if currentReminderUri is null or not
        if (currentReminderUri == null) {
            Uri newUri = getContentResolver().insert(ReminderEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getText(R.string.editor_insert_reminder_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getText(R.string.editor_insert_reminder_successful), Toast.LENGTH_SHORT).show();

            }
        }
        else {

            if (!mReminderHasChanged) {
                return;
            }

            else {

                int rowsAffected = getContentResolver().update(currentReminderUri, values, null, null);


                //Show a toast message depending on whether or not the update was successful
                if (rowsAffected == 0) {
                    Toast.makeText(this, "Reminder Update Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Reminder Update Successful", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    private void getDate() {
        mCurrentDate = Calendar.getInstance();
        year = mCurrentDate.get(Calendar.YEAR);
        month = mCurrentDate.get(Calendar.MONTH);
        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(EditorActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selesctedDay) {
              year = selectedYear;
              month = selectedMonth;
              day = selesctedDay;

                if (selectedMonth < 10 && selesctedDay < 10) {
                    mDateEditText.setText("0" + selesctedDay + "-0" + (selectedMonth + 1) + "-" + selectedYear);
                } else if (selectedMonth < 10 && selesctedDay >= 10) {
                    mDateEditText.setText(selesctedDay + "-0" + (selectedMonth + 1) + "-" + selectedYear);
                } else if (selectedMonth > 10 && selesctedDay < 10) {
                    mDateEditText.setText("0" + selesctedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                } else if (selectedMonth > 10 && selesctedDay < 10) {
                    mDateEditText.setText("0" + selesctedDay + (selectedMonth + 1) + "-" + selectedYear);
                } else {
                    mDateEditText.setText(selesctedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
                }

                mCurrentDate.set(selectedYear, selectedMonth, selesctedDay);
                mGeneratedDateIcon = mImageGenerator.generateDateImage(mCurrentDate, R.drawable.empty_calendar);
                mDisplayGeneratedImage.setImageBitmap(mGeneratedDateIcon);

//                year = selectedYear;
//                month = selectedMonth;
//                day = selesctedDay;


                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
                Date d = new Date(selectedYear-1900, selectedMonth, selesctedDay);
                long time = d.getTime();
                String strDate = dateFormatter.format(time);
                Toast.makeText(EditorActivity.this, strDate, Toast.LENGTH_LONG).show();
            }
        }, year, month, day);
//                SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
//                Date d = new Date(year-1900, month, day);
//                String strDate = dateFormatter.format(d);
//                Toast.makeText(MainActivity.this, strDate, Toast.LENGTH_LONG).show();
        mDatePicker.show();
    }

    private void getTime() {
        mCurrentDate = Calendar.getInstance();
        int hour = mCurrentDate.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentDate.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(EditorActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

                String AM_PM;

                if (hourOfDay < 12) {
                    AM_PM = "AM";
                    if (minute == 0) {
                        mTimeEditText.setText(hourOfDay + ":00 " + AM_PM);
                    } else if (minute < 10) {
                        mTimeEditText.setText(hourOfDay + ":0" + minute + " " + AM_PM);
                    } else {
                        mTimeEditText.setText(hourOfDay + ":" + minute + AM_PM);
                    }
                } else {
                    AM_PM = "PM";
                    if (minute == 0) {
                        mTimeEditText.setText(hourOfDay - 12 + ":00 " + AM_PM);
                    } else if (minute < 10) {
                        mTimeEditText.setText(hourOfDay - 12 + ":0" + minute + " " + AM_PM);
                    } else {
                        mTimeEditText.setText(hourOfDay - 12 + ":" + minute + AM_PM);
                    }
                }


                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                Date d = new Date(year-1900, month, day, hourOfDay, minute);
                time = d.getTime();
                String strDate = dateFormatter.format(time);
                Toast.makeText(EditorActivity.this, strDate, Toast.LENGTH_LONG).show();

            }

        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void startTyping() {
        // To display the hint at the center of the EditText and start typing at the top left of the EditText
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // position the text type in the left top corner
                    mTitleEditText.setGravity(Gravity.LEFT | Gravity.TOP);
                } else {
                    // no text entered. Center the hint text.
                    mTitleEditText.setGravity(Gravity.CENTER);
                }
            }
        });

        mReminderEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // position the text type in the left top corner
                    mReminderEditText.setGravity(Gravity.LEFT | Gravity.TOP);
                } else {
                    // no text entered. Center the hint text.
                    mReminderEditText.setGravity(Gravity.CENTER);
                }
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_REMINDER_TITLE,
                ReminderEntry.COLUMN_REMINDER_DETAILS,
                ReminderEntry.COLUMN_REMINDER_DATETIME
        };
        return new CursorLoader(this, currentReminderUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // This should be the only row in the cursor
        if (cursor.moveToFirst()) {
            // Find the columns of reminder attributes that we are interested in
            int titleColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_TITLE);
            int detailsColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_DETAILS);
            int dateColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_DATETIME);

            //Extract out the value from the cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String details = cursor.getString(detailsColumnIndex);
            Long columnDate = cursor.getLong(dateColumnIndex);

            Date date = new Date(columnDate);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");
            String date1 = format.format(date);
            String time = format2.format(date);

            //Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mReminderEditText.setText(details);
            mDateEditText.setText(date1);
            mTimeEditText.setText(time);
        }

    }


    @Override
    public void onLoaderReset(Loader loader) {
        mTitleEditText.setText("");
        mReminderEditText.setText("");
        mDateEditText.setText("");
        mTimeEditText.setText("");

    }
}

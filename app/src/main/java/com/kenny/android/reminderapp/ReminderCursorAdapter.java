package com.kenny.android.reminderapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.kenny.android.reminderapp.data.ReminderContract;
import com.kenny.android.reminderapp.data.ReminderContract.ReminderEntry;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TAYE on 13/03/2018.
 */

public class ReminderCursorAdapter extends CursorAdapter {

    public ReminderCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.customised_row, parent, false);
        return inflate;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Find individual views that we want to modify in the list item layout
        TextView tvTitle = (TextView) view.findViewById(R.id.lTitleTextView);
        TextView tvDate = (TextView) view.findViewById(R.id.lDateTextView);

        //Find the column of the reminder attribute that we are interested in
        int titleColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_TITLE);
        int dateColumnIndex = cursor.getColumnIndex(ReminderEntry.COLUMN_REMINDER_DATETIME);

        //Read the reminder attributes from the Cursor for the current reminder
        String columnTitle = cursor.getString(titleColumnIndex);
        long columnDate = cursor.getLong(dateColumnIndex);

        Date date = new Date(columnDate);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String date1 = format.format(date);
        // Update the TextViews with the attributes for the current reminder
        tvTitle.setText(columnTitle);
        tvDate.setText(date1);

    }
}

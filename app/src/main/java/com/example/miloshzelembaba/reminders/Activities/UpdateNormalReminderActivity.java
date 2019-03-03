package com.example.miloshzelembaba.reminders.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateNormalReminderActivity extends BaseUpdateReminderActivity {
    protected TextView date;
    protected TextView time;
    private ImageView timeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        childContainer.addView(layoutInflater.inflate(R.layout.activity_update_normal_reminder, null, false));
        reminderType = "normal";

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        timeIcon = findViewById(R.id.time_icon);

        if (reminder != null) {
            date.setText(DateFormat.getDateInstance().format(new Date(reminder.getTimestamp())));
            time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(reminder.getTimestamp())));
            calendar.setTime(new Date(reminder.getTimestamp()));
            setupDateAndTimePickerClicker();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            date.setText(DateFormat.getDateInstance().format(new Date()));

            Date rightNow = new Date();
            calendar.setTime(rightNow);
            int minutes = calendar.get(Calendar.MINUTE);
            calendar.set(Calendar.MINUTE, minutes < 30 ? 30 : 60);
            time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime()));
            setupDateAndTimePickerClicker();
        }

        Drawable timeIconDrawable = getResources().getDrawable(R.mipmap.baseline_access_time_white_24dp);
        timeIconDrawable.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        timeIcon.setImageDrawable(timeIconDrawable);
    }

    private void setupDateAndTimePickerClicker() {
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(year, month, dayOfMonth);
                                date.setText(DateFormat.getDateInstance().format(calendar.getTime()));
                            }
                        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(v.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime()));
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false);
                timePickerDialog.show();
            }
        });
    }

    @Override
    protected void save() {
        String titleText = title.getText().toString();
        String descText = description.getText().toString();

        Reminder newReminder = new Reminder(titleText, descText, reminderType, calendar.getTimeInMillis());
        if (reminder != null) {
            newReminder.setId(reminder.getId());
        }
        dbService.updateReminder(newReminder);
        finish();
    }
}

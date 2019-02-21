package com.example.miloshzelembaba.reminders.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.R;
import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;

import org.json.JSONObject;

import java.util.Calendar;

abstract public class BaseUpdateReminderActivity extends AppCompatActivity {
    protected EditText title;
    protected EditText description;
    private Button save;
    private Button delete;
    protected RelativeLayout childContainer;

    protected DatePickerDialog datePickerDialog;
    protected TimePickerDialog timePickerDialog;
    protected Reminder reminder;
    protected String reminderType;
    protected Calendar calendar = Calendar.getInstance();
    protected DBService dbService = DBService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_update_reminder);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        save = findViewById(R.id.save);
        delete = findViewById(R.id.delete);
        childContainer = findViewById(R.id.child_container);

        reminder = getReminder();

        save.setOnClickListener(v -> save());
        delete.setOnClickListener(v -> delete());

        if (reminder != null) {
            delete.setVisibility(View.VISIBLE);
            title.setText(reminder.getTitle());
            description.setText(reminder.getDescription());
        }
    }

    private Reminder getReminder() {
        JSONObject jsonObject;
        try {
            if (getIntent().hasExtra("reminder")) {
                jsonObject = new JSONObject(getIntent().getStringExtra("reminder"));
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("well fuuuuuck", "this should never happen");
            return null;
        }

        reminderType = jsonObject.optString("reminder_type");
        if (reminderType.equals("normal")) {
            return new Reminder(jsonObject);
        } else {
            return new LocationReminder(jsonObject);
        }
    }

    abstract protected void save();
    protected void delete() {
        if (reminder != null) {
            dbService.deleteReminder(reminder);
        }
        finish();
    }
}

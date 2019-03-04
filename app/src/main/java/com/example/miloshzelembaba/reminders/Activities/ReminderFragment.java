package com.example.miloshzelembaba.reminders.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.R;
import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReminderFragment extends Fragment implements ReminderService.RemindersListener {
    private ReminderAdapter reminderAdapter;
    private DBService dbService = DBService.getInstance();
    private ArrayList<Reminder> currentReminders;
    ListView listview;
    TextView descriptiveText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.normal_reminder_layout, container, false);
        descriptiveText = parent.findViewById(R.id.descriptive_text);
        listview = parent.findViewById(R.id.list_view);
        onReminderRefresh(currentReminders, false);
        return parent;
    }

    @Override
    public void onReminderRefresh(ArrayList<Reminder> reminders, boolean error) {
        if (reminders != null && listview != null && getContext() != null) {
            reminderAdapter = new ReminderAdapter(getContext(), 0, reminders, listview);
            listview.setAdapter(reminderAdapter);
        }

        if (reminders != null) {
            if (reminders.size() == 0) {
                listview.setVisibility(View.GONE);
                descriptiveText.setVisibility(View.VISIBLE);
                descriptiveText.setText(getText(R.string.no_reminders));
            } else {
                listview.setVisibility(View.VISIBLE);
                descriptiveText.setVisibility(View.GONE);
            }
            currentReminders = reminders;
        }
    }

    class ReminderAdapter extends ArrayAdapter<Reminder> {

        ReminderAdapter(Context context, int textViewResourceId, ArrayList<Reminder> listItems, ListView l) {
            super(context, textViewResourceId, listItems);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Reminder reminder = getItem(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.reminder_layout, parent, false);
            }

            ImageView reminderType = convertView.findViewById(R.id.reminder_type_icon);
            TextView title = convertView.findViewById(R.id.title);
            TextView date = convertView.findViewById(R.id.date);
            TextView time = convertView.findViewById(R.id.time);

            Drawable icon;
            title.setText(reminder.getTitle());
            if (reminder.getReminderType().equals("location")) {
                icon = getResources().getDrawable(R.mipmap.baseline_location_on_white_24dp);
                time.setText("");
                date.setText("");
            } else {
                icon = getResources().getDrawable(R.mipmap.baseline_access_time_white_24dp);
                Date dateTime = new Date(reminder.getTimestamp());
                date.setText(DateFormat.getDateInstance().format(dateTime));
                time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(dateTime));
            }
            icon.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
            reminderType.setImageDrawable(icon);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateReminder(reminder);
                }
            });

            return convertView;
        }
    }

    private void updateReminder(Reminder reminder) {
        Intent intent;
        if (reminder.getReminderType().equals("location")) {
            intent = new Intent(getContext(), UpdateLocationReminderActivity.class);
        } else {
            intent = new Intent(getContext(), UpdateNormalReminderActivity.class);
        }
        intent.putExtra("reminder", reminder.toJson().toString());
        startActivity(intent);
    }

}

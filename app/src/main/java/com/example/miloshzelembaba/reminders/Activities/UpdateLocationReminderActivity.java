package com.example.miloshzelembaba.reminders.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.miloshzelembaba.reminders.Activities.BaseUpdateReminderActivity;
import com.example.miloshzelembaba.reminders.CurrentLocationManager;
import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateLocationReminderActivity extends BaseUpdateReminderActivity implements OnMapReadyCallback {
    protected double radius;
    protected LatLng position;
    protected MapView mapView;
    protected SeekBar radiusSlider;
    protected CheckBox datetimeCheckbox;
    protected TextView date;
    protected TextView time;
    protected RelativeLayout datetimePickerContainer;

    protected Long datetime;
    protected LocationReminder locationReminder;
    protected GoogleMap googleMap;
    private final int MIN_RADIUS = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        childContainer.addView(layoutInflater.inflate(R.layout.activity_update_location_reminder, null, false));

        reminderType = "location";
        locationReminder = (LocationReminder) reminder;

        mapView = findViewById(R.id.map_view);
        radiusSlider = findViewById(R.id.radius_slider);
        datetimeCheckbox = findViewById(R.id.datetime_checkbox);
        date = findViewById(R.id.location_date);
        time = findViewById(R.id.location_time);
        datetimePickerContainer = findViewById(R.id.date_time_picker_container);
        datetimePickerContainer.setVisibility(View.GONE); // the datetime feature is currenctly broken and it's not MVP

        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (reminder != null) {
                    position = ((LocationReminder)reminder).getLatLng();
                }
                radius = progress + MIN_RADIUS;
                createReminderCircle(position);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        if (locationReminder != null) {
            position = locationReminder.getLatLng();
            radius = locationReminder.getRadius();
            if (reminder.getTimestamp() > 0) {
                datetimeCheckbox.setChecked(true);
                date.setText(DateFormat.getDateInstance().format(new Date(reminder.getTimestamp())));
                time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(reminder.getTimestamp())));
                calendar.setTime(new Date(reminder.getTimestamp()));
            } else {
                datetimeCheckbox.setChecked(false);
                setNewDateTime();
            }
        } else {
            radius = getIntent().getDoubleExtra("radius", 50);
            position = new LatLng(getIntent().getDoubleExtra("lat", 0), getIntent().getDoubleExtra("lon", 0));

            setNewDateTime();
        }

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);
        radiusSlider.setProgress((int)radius - MIN_RADIUS);
        radiusSlider.setOnSeekBarChangeListener(seekListener);
        setupDateAndTimePickerClicker();
    }

    private void setNewDateTime() {
        date.setText(DateFormat.getDateInstance().format(new Date()));
        Date rightNow = new Date();
        calendar.setTime(rightNow);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes < 30 ? 30 : 60);
        time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime()));
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
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng l) {
                position = l;
                createReminderCircle(position);
            }
        });
        createReminderCircle(position);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void createReminderCircle(LatLng latlng) {
        googleMap.clear();
        CircleOptions circleOptions = new CircleOptions().fillColor(Color.argb(97, 93, 0, 139)).strokeColor(Color.argb(200, 93, 0, 139));
        CircleOptions userLocation = new CircleOptions().fillColor(Color.argb(97, 93, 185, 139)).strokeColor(Color.argb(200, 93, 185, 139));
        googleMap.addCircle(circleOptions.center(latlng).radius(radius));
        googleMap.addCircle(userLocation.center(CurrentLocationManager.getInstance().getCurrentLocation()).radius(5)); // yes i know this location won't really update
    }

    @Override
    protected void save(){
        String titleText = title.getText().toString();
        String descText = description.getText().toString();

        LocationReminder newReminder = new LocationReminder(titleText, descText, reminderType, position, radius);
        if (reminder != null) {
            newReminder.setId(reminder.getId());
        }
        if (datetimeCheckbox.isChecked()) {
            newReminder.setTimestamp(calendar.getTimeInMillis());
        } else {
            newReminder.setTimestamp(0);
        }

        dbService.updateLocationReminder(newReminder);
        finish();
    }
}

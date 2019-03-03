package com.example.miloshzelembaba.reminders.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miloshzelembaba.reminders.CurrentLocationManager;
import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Place;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.R;
import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.PlaceAutocompleteService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;
import com.example.miloshzelembaba.reminders.ReminderManager;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.LocationUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class GoogleMapFragment extends Fragment implements OnMapReadyCallback, CurrentLocationManager.LocationUpdateListener, ReminderService.RemindersListener, PlaceAutocompleteService.PlaceAutocompleteListener {
    DBService dbService = DBService.getInstance();
    ReminderManager reminderManager = ReminderManager.getInstance();
    MapView mapView;
    GoogleMap googleMap;
    EditText searchBar;
    CurrentLocationManager currentLocationManager;
    Circle previousLocation;
    ListView searchResultListView;
    FloatingActionButton fabHome;
    final double REMINDER_RADIUS = 50;
    boolean initialMove = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_reminder_layout, container, false);
        mapView = view.findViewById(R.id.map_view);
        searchBar = view.findViewById(R.id.search_bar);
        searchResultListView = view.findViewById(R.id.search_results_listview);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        currentLocationManager = CurrentLocationManager.getInstance();
        currentLocationManager.addListener(this);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                focusOnLocation(currentLocationManager.getCurrentLocation());
            }
        });

        fabHome = view.findViewById(R.id.fab_home);
        fabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationUtil.getUser().getHome() != null) {
                    focusOnLocation(ApplicationUtil.getUser().getHome());
                } else {
                    Toast.makeText(getContext(), "Long press search result to add home", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchResultListView.setVisibility(View.VISIBLE);
                dbService.getOptions(s.toString(), 500, false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    public void showFabAnitmation() {
        fabHome.show();
        TranslateAnimation animation = new TranslateAnimation(0,0,fabHome.getHeight(),0);
        animation.setDuration(200);
        fabHome.startAnimation(animation);
    }

    public void hideFabAnitmation() {
        ScaleAnimation animation = new ScaleAnimation(1,0,1,0, fabHome.getWidth()/2, fabHome.getHeight()/2);
        animation.setDuration(200);
        fabHome.startAnimation(animation);
        fabHome.hide();
    }

    private void focusOnLocation(LatLng location) {
        if (googleMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // todo: this method to adding reminders will be updated
                if (searchResultListView.getVisibility() == View.VISIBLE) {
                    searchResultListView.setVisibility(View.GONE);
                } else {
                    createLocationReminder(latLng);
                }
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                openReminderView(latLng);
            }
        });
        if (CurrentLocationManager.getInstance().getCurrentLocation() != null) {
            focusOnLocation(CurrentLocationManager.getInstance().getCurrentLocation());
        }
        drawReminders(reminderManager.getReminders());
    }

    private void openReminderView(LatLng latLng) {
        for (Reminder reminder : reminderManager.getReminders()) {
            if (reminder.getReminderType().equals("location")) {
                LocationReminder locationReminder = (LocationReminder)reminder;
                LatLng reminderPos = locationReminder.getLatLng();
                double dist = LocationUtil.getDistanceFromLatLonInMeters(latLng, reminderPos);
                if (dist < locationReminder.getRadius()) {
                    Intent intent = new Intent(getContext(), UpdateLocationReminderActivity.class);
                    intent.putExtra("reminder", reminder.toJson().toString());
                    startActivity(intent);
                }
            }
        }
    }

    // this is for if the reminders come in before the map is ready. Theres also the other case of the
    // map being ready first and then the reminders coming in after, which gets handled by onReminderRefresh()
    private void drawReminders(ArrayList<Reminder> reminders) {
        if (reminders != null) {
            for (Reminder r : reminders) {
                if (r.getReminderType().equals("location")) {
                    LocationReminder reminder = (LocationReminder)r;
                    createReminderCircle(reminder.getLatLng(), (int)reminder.getRadius());
                }
            }
        }
    }

    private Circle createReminderCircle(LatLng latlng, int radius) {
        CircleOptions circleOptions = new CircleOptions().fillColor(Color.argb(97, 93, 0, 139)).strokeColor(Color.argb(200, 93, 0, 139));
        return googleMap.addCircle(circleOptions.center(latlng).radius(radius));
    }

    private void createLocationReminder(LatLng latLng) {
        Intent intent = new Intent(getContext(), UpdateLocationReminderActivity.class);
        intent.putExtra("type", "location");
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lon", latLng.longitude);
        intent.putExtra("radius", REMINDER_RADIUS);

        startActivity(intent);
    }

    @Override
    public void onAutocompleteUpdate(ArrayList<Place> places, boolean error) {
        SearchResultAdapter adapter = new SearchResultAdapter(getContext(), 0, places, searchResultListView);
        searchResultListView.setAdapter(adapter);
//        placePins(places);
    }

    @Override
    public void onReminderRefresh(ArrayList<Reminder> reminders, boolean error) {
        if (googleMap != null && reminders != null) {
            googleMap.clear();
            drawReminders(reminders);
            drawLocation(currentLocationManager.getCurrentLocation());
        }
    }

    private void placePins(ArrayList<Place> places) {
        googleMap.clear();
        drawReminders(reminderManager.getReminders());
        drawLocation(currentLocationManager.getCurrentLocation());
        drawPins(places);
    }

    private void drawPins(ArrayList<Place> places) {
        for (Place place : places) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(place.location);
            markerOptions.title(place.name);
            googleMap.addMarker(markerOptions);
        }
    }

    @Override
    public void locationUpdate(Location location) {
        drawLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        moveMapIfHaventAlready(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void moveMapIfHaventAlready(LatLng latLng) {
        if (!initialMove && googleMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            initialMove = true;
        }
    }

    private void drawLocation(LatLng latLng) {
        if (previousLocation != null) {
            previousLocation.remove();
        }

        if (googleMap != null && latLng != null) {
            CircleOptions circleOptions = new CircleOptions().fillColor(Color.argb(97, 93, 185, 139)).strokeColor(Color.argb(200, 93, 185, 139));
            previousLocation = googleMap.addCircle(circleOptions.center(latLng).radius(5));
        }
    }

    class SearchResultAdapter extends ArrayAdapter<Place> {

        SearchResultAdapter(Context context, int textViewResourceId, ArrayList<Place> listItems, ListView l) {
            super(context, textViewResourceId, listItems);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Place place = getItem(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.search_result_layout, parent, false);
            }

            TextView name = convertView.findViewById(R.id.name);
            TextView address = convertView.findViewById(R.id.address);

            name.setText(place.name);
            address.setText(place.address);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchResultListView.setVisibility(View.GONE);
                    focusOnLocation(place.location);
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dbService.setHome(place.location);
                    Toast.makeText(getContext(), "Home address updated", Toast.LENGTH_SHORT).show();
                    return true;
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

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mapView != null)
            mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mapView != null)
            mapView.onStop();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapView != null)
            mapView.onSaveInstanceState(outState);
    }
}


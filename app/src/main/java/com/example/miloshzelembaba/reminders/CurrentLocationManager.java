package com.example.miloshzelembaba.reminders;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

// todo(BUG): the first time a user uses the app, it won't be able to get their location until they allow the permission

public class CurrentLocationManager {
    private static CurrentLocationManager instance;
    private ArrayList<LocationUpdateListener> locationUpdateListeners = new ArrayList<>();
    private Location currentLocation;
    private Intent locationServiceIntent;

    private CurrentLocationManager(){}

    public static CurrentLocationManager getInstance() {
        if (instance == null) {
            instance = new CurrentLocationManager();
        }

        return instance;
    }

    public void addListener(LocationUpdateListener l) {
        if (!locationUpdateListeners.contains(l)) {
            locationUpdateListeners.add(l);
        }
    }

    @Nullable
    public LatLng getCurrentLocation() {
        if (currentLocation != null) {
            return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {
            return null;
        }
    }

    public void onLocationUpdate(Location loc) {
        currentLocation = loc;
        for (CurrentLocationManager.LocationUpdateListener l : locationUpdateListeners) {
            l.locationUpdate(currentLocation);
        }
    }

    public interface LocationUpdateListener{
        void locationUpdate(Location location);
    }
}

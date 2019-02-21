package com.example.miloshzelembaba.reminders.Models;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class LocationReminder extends Reminder {
    protected LatLng latLng;
    protected double radius;

    public LocationReminder(@NonNull String title, @NonNull String description, @NonNull String reminderType, @NonNull LatLng latLng, double radius) {
        super(title, description, reminderType);
        this.latLng = latLng;
        this.radius = radius;
    }

    public LocationReminder(@NonNull JSONObject jsonObject) {
        super(jsonObject);
        this.radius = jsonObject.optDouble("radius");
        double lat = jsonObject.optDouble("lat");
        double lon = jsonObject.optDouble("lon");
        this.latLng = new LatLng(lat, lon);
    }

    public JSONObject toJson() {
        if (jsonObject != null) {
            return jsonObject;
        }

        try {
            jsonObject = super.toJson();
            jsonObject.put("radius", radius);
            jsonObject.put("lat", latLng.latitude);
            jsonObject.put("lon", latLng.longitude);
        } catch (Exception e) {
            return null;
        }

        return jsonObject;
    }


    public LatLng getLatLng() {
        return latLng;
    }

    public double getRadius() {
        return radius;
}

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

}

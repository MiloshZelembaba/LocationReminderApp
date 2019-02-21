package com.example.miloshzelembaba.reminders.Models;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class User {
    private JSONObject jsonObject;
    private String id;
    private String deviceId;
    private LatLng home;

    public User(String id, String deviceId, double lat, double lon, JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        this.id = id;
        this.deviceId = deviceId;

        if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
            this.home = new LatLng(lat, lon);
        }
    }

    @Nullable
    public LatLng getHome() {
        return home;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }


    public JSONObject getJson() {
        return jsonObject;
    }
}

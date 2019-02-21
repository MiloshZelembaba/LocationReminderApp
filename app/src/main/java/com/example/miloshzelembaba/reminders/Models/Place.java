package com.example.miloshzelembaba.reminders.Models;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class Place {
    public final String name;
    public final LatLng location;
    public final String address;

    public Place(JSONObject obj) {
        this.name = obj.optString("name");
        this.address= obj.optString("address");
        this.location = new LatLng(obj.optDouble("lat"), obj.optDouble("lng"));
    }


}

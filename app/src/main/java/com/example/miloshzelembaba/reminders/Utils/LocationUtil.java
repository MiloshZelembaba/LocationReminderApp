package com.example.miloshzelembaba.reminders.Utils;

import com.google.android.gms.maps.model.LatLng;

public class LocationUtil {

    public static double getDistanceFromLatLonInMeters(LatLng l1, LatLng l2) {
        double lat1 = l1.latitude;
        double lon1 = l1.longitude;
        double lat2 = l2.latitude;
        double lon2 = l2.longitude;

        int R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2-lat1);  // deg2rad below
        double dLon = Math.toRadians(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d*1000;
    }
}

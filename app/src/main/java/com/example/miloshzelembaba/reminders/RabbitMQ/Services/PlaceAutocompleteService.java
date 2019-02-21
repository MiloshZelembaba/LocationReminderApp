package com.example.miloshzelembaba.reminders.RabbitMQ.Services;


import com.example.miloshzelembaba.reminders.CurrentLocationManager;
import com.example.miloshzelembaba.reminders.Models.Place;
import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceAutocompleteService extends BaseService{
    private static PlaceAutocompleteService instance = new PlaceAutocompleteService();
    private ArrayList<PlaceAutocompleteListener> listeners = new ArrayList<>();
    private final String endPoint = NetworkUtil.getServerAddress() + "placeAutoComplete/";

    private PlaceAutocompleteService(){}

    public static PlaceAutocompleteService getInstance() {
        return instance;
    }

    public void addListener(PlaceAutocompleteListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void getOptions(String text, int radius, boolean strictBounds) {
        if (text == null || text.length() == 0) {
            return;
        }

        CurrentLocationManager locationManager = CurrentLocationManager.getInstance();
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("input_text", text);
            obj.put("lat", locationManager.getCurrentLocation().latitude);
            obj.put("lon", locationManager.getCurrentLocation().longitude);
            obj.put("strict_bounds", strictBounds);
            obj.put("radius", radius);

            sendPost(endPoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    private void notifyListeners(ArrayList<Place> places, boolean error) {
        for (PlaceAutocompleteListener listener : listeners) {
            listener.onAutocompleteUpdate(places, error);
        }
    }

    @Override
    public void onServerObjectReceived(ServerObject serverObject) {
        notifyListeners(serverObject.getPlaces(), false);
    }

    public interface PlaceAutocompleteListener {
        void onAutocompleteUpdate(ArrayList<Place> places, boolean error); // todo: update to pass in all of the places
    }
}

package com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects;

import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Place;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerObject {
    User user;
    ArrayList<Reminder> reminders = new ArrayList<>();
    ArrayList<Place> places = new ArrayList<>();
    public final String eventType;

    public ServerObject(String str) throws JSONException {
        JSONObject response = new JSONObject(str);
        eventType = response.getString("event_type");

        if (response.has("user")) {
            JSONObject jsonUser = response.getJSONObject("user");
            user = new User(jsonUser.getString("id"), jsonUser.getString("device_id"), jsonUser.optDouble("home_lat"), jsonUser.optDouble("home_lon"),  jsonUser);
        }

        if (response.has("reminders")) {
            JSONArray jsonArray = response.getJSONArray("reminders");
            for (int i=0; i<jsonArray.length(); ++i){
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("reminder_type").equals("location")) {
                    reminders.add(new LocationReminder(jsonArray.getJSONObject(i)));
                } else {
                    reminders.add(new Reminder(jsonArray.getJSONObject(i)));
                }
            }
        }

        if (response.has("results")) { // this is auto complete results
            JSONArray jsonArray = response.optJSONArray("results");
            for (int i=0; i<jsonArray.length(); i++) {
                places.add(new Place(jsonArray.getJSONObject(i)));
            }
        }
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<Reminder> getReminders() {
        return reminders;
    }

}

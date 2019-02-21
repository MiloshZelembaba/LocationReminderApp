package com.example.miloshzelembaba.reminders;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SharedPreferencesManager {
    private static SharedPreferencesManager instance = new SharedPreferencesManager();

    public static SharedPreferencesManager getInstance() {
        return instance;
    }

    private SharedPreferencesManager() {}

    public void saveReminders(Context c, @NonNull ArrayList<Reminder> reminders) {
        SharedPreferences prefs = c.getSharedPreferences("locationAppPreferences", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            for (Reminder r : reminders) {
                jsonArray.put(r.toJson());
            }
            prefs.edit().putString("reminders", jsonArray.toString()).apply();
        } catch (Exception e) {
            prefs.edit().putString("reminders", null).apply();
        }
    }

    public void saveRemindersToDelete(Context c, @NonNull ArrayList<Reminder> reminders) {
        SharedPreferences prefs = c.getSharedPreferences("locationAppPreferences", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        try {
            for (Reminder r : reminders) {
                jsonArray.put(r.toJson());
            }
            prefs.edit().putString("delete_reminders", jsonArray.toString()).apply();
        } catch (Exception e) {
            prefs.edit().putString("delete_reminders", null).apply();
        }
    }

    @Nullable
    public ArrayList<Reminder> getReminders(Context c) {
        SharedPreferences prefs = c.getSharedPreferences("locationAppPreferences", Context.MODE_PRIVATE);
        String remindersStr = prefs.getString("reminders", "");
        ArrayList<Reminder> result = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(remindersStr);
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("reminder_type").equals("location")) {
                    result.add(new LocationReminder(jsonArray.getJSONObject(i)));
                } else {
                    result.add(new Reminder(jsonArray.getJSONObject(i)));
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

}

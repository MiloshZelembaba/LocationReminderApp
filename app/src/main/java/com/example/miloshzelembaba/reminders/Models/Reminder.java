package com.example.miloshzelembaba.reminders.Models;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Reminder {
    protected JSONObject jsonObject;
    protected String id;
    protected String title;
    protected String description;
    private long unixTime;
    protected String reminderType;

    public Reminder(@NonNull String title, @NonNull String description, @NonNull String reminderType) {
        this.title = title;
        this.description = description;
        this.reminderType = reminderType;
    }

    public Reminder(@NonNull String title, @NonNull String description, @NonNull String reminderType, long unixTime) {
        this.title = title;
        this.description = description;
        this.reminderType = reminderType;
        this.unixTime = unixTime;
    }

    public Reminder(@NonNull JSONObject jsonObject) {
        this.id = jsonObject.optString("id");
        this.title = jsonObject.optString("title");
        this.description = jsonObject.optString("description");
        this.reminderType = jsonObject.optString("reminder_type");
        this.unixTime = jsonObject.optLong("reminder_trigger_timestamp");
        this.jsonObject = jsonObject;
    }

    public JSONObject toJson() {
        if (jsonObject != null) {
            return jsonObject;
        }

        try {
            jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("title", title);
            jsonObject.put("description", description);
            jsonObject.put("reminder_trigger_timestamp", unixTime);
            jsonObject.put("reminder_type", reminderType);
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getReminderType() {
        return reminderType;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return unixTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        unixTime = timestamp;
    }

}

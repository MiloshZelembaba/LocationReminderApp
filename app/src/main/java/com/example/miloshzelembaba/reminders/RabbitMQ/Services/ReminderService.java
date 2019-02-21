package com.example.miloshzelembaba.reminders.RabbitMQ.Services;

import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReminderService extends BaseService {
    private static ReminderService instance = new ReminderService();
    private ArrayList<RemindersListener> listeners = new ArrayList<>();
    private final String getEndpoint = NetworkUtil.getServerAddress() + "getReminders/";
    private final String updateEndpoint = NetworkUtil.getServerAddress() + "updateReminder/";
    private final String deleteEndpoint = NetworkUtil.getServerAddress() + "deleteReminder/";

    private ReminderService() {}

    public static ReminderService getInstance() {
        return instance;
    }

    public void addListener(RemindersListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void deleteReminders(ArrayList<Reminder> reminders) {
        deleteReminders(reminders, true);
    }

    public void deleteReminders(ArrayList<Reminder> reminders, boolean doCallback) {
        if (reminders.size() == 0) {
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("reminder", arrayToJsonArray(reminders));
            obj.put("notify", doCallback);
            sendPost(deleteEndpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    private JSONArray arrayToJsonArray(ArrayList<Reminder> reminders) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Reminder r : reminders) {
                jsonArray.put(r.toJson());
            }
        } catch (Exception e) {
            return null;
        }

        return jsonArray;
    }

    public void deleteReminder(Reminder reminder) {
        deleteReminder(reminder, true);
    }

    public void deleteReminder(Reminder reminder, boolean doCallback) {
        ArrayList<Reminder> reminders = new ArrayList<>();
        reminders.add(reminder);
        deleteReminders(reminders, doCallback);
    }

    public void updateReminder(Reminder reminder) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("reminder", reminder.toJson());
            sendPost(updateEndpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    public void updateLocationReminder(LocationReminder reminder) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("reminder", reminder.toJson());
            sendPost(updateEndpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    public void getReminders() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            sendPost(getEndpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    public void getServiceReminders() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("event_type", "service");
            sendPost(getEndpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    private void notifyListeners(ArrayList<Reminder> reminders, boolean error) {
        for (RemindersListener listener : listeners) {
            listener.onReminderRefresh(reminders, error);
        }
    }

    @Override
    public void onServerObjectReceived(ServerObject serverObject) {
        // do something with server object and then pass it along
        notifyListeners(serverObject.getReminders(), false);
    }

    public interface RemindersListener {
        void onReminderRefresh(ArrayList<Reminder> reminders, boolean error);
    }
}

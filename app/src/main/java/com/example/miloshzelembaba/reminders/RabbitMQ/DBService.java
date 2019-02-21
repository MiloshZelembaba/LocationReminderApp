package com.example.miloshzelembaba.reminders.RabbitMQ;

import android.support.annotation.NonNull;

import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.FCMService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.PlaceAutocompleteService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.UpdateHomeAddressService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.UserService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class DBService {
    private ReminderService reminderService = ReminderService.getInstance();
    private FCMService fcmService = FCMService.getInstance();
    private UserService userService = UserService.getInstance();
    private PlaceAutocompleteService placeAutocompleteService = PlaceAutocompleteService.getInstance();
    private UpdateHomeAddressService updateHomeAddressService = UpdateHomeAddressService.getInstance();
    private static DBService instance = new DBService();

    public static DBService getInstance() {
        return instance;
    }

    private DBService(){}

    public void addReminderListener(ReminderService.RemindersListener listener) {
        reminderService.addListener(listener);
    }

    public void addAutocompleteListener(PlaceAutocompleteService.PlaceAutocompleteListener listener) {
        placeAutocompleteService.addListener(listener);
    }

    public void getOptions(String text, int radius, boolean strictBounds) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                placeAutocompleteService.getOptions(text, radius, strictBounds);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void setHome(LatLng location) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateHomeAddressService.setHome(location.latitude, location.longitude);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void deleteReminder(Reminder remidner) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                reminderService.deleteReminder(remidner);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void updateReminder(Reminder remidner) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                reminderService.updateReminder(remidner);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    // todo: locations shouldn't have to be their own method
    public void updateLocationReminder(LocationReminder remidner) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                reminderService.updateLocationReminder(remidner);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void getReminders() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                reminderService.getReminders();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void updateFCMToken(String token) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fcmService.updateFCMToken(token);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void updateFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        updateFCMToken(token);
                    }
                });
    }

    public void addUserInfoListener(UserService.UserInfoListener listener) {
        userService.addListener(listener);
    }

    public void getUserInfo(final String deviceId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                userService.getUserInfo(deviceId);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}

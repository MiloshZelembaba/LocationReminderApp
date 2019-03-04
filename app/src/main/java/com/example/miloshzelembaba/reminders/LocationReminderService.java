package com.example.miloshzelembaba.reminders;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.miloshzelembaba.reminders.Activities.MainActivity;
import com.example.miloshzelembaba.reminders.Models.LocationReminder;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.app.NotificationManager.IMPORTANCE_MIN;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class LocationReminderService extends Service implements ReminderService.RemindersListener, LifecycleObserver{
    CurrentLocationManager currentLocationManager = CurrentLocationManager.getInstance();
    ArrayList<Reminder> currentReminders = new ArrayList<>();
    private boolean background = false;
    public static boolean manualDelete = false;

    public LocationReminderService() {
        super();
    }

    LocationResult locationResult = new LocationResult(){
        @Override
        public void gotLocation(Location location){
            if (currentLocationManager != null) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        currentLocationManager.onLocationUpdate(location);
                    }
                };
                mainHandler.post(runnable);
            }

            checkIfUserInReminder(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    };

//    void onLocationChanged(Location var1) {
//
//    }

    class MyLocation {
        LocationManager lm;
        LocationResult locationResult;
        boolean gps_enabled=false;
        boolean network_enabled=false;

        public boolean getLocation(Context context, LocationResult result)
        {
            //I use LocationResult callback class to pass location value from MyLocation to user code.
            locationResult=result;
            if(lm==null)
                lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            //exceptions will be thrown if provider is not permitted.
            try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
            try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

            //don't start listeners if no provider is enabled
            if(!gps_enabled && !network_enabled) {
                return false;
            }

            try {
                if (gps_enabled)
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 7, locationListenerGps);
                if (network_enabled)
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 7, locationListenerNetwork);
            } catch (SecurityException e) {
                // todo: need to handle this by prompting the user
                Log.e("location", "proper permissions not granted");
                System.out.println("------ " + e.getMessage());
                return false;
            }
            return true;
        }

        LocationListener locationListenerGps = new LocationListener() {
            public void onLocationChanged(Location location) {
                locationResult.gotLocation(location);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        LocationListener locationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {
                locationResult.gotLocation(location);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };
    }

    @Override
    public void onReminderRefresh(ArrayList<Reminder> reminders, boolean error) {
        currentReminders = reminders;
        SharedPreferencesManager.getInstance().saveReminders(this, reminders);
        checkIfUserInReminder(currentLocationManager.getCurrentLocation());
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This won't be a bound service, so simply return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("MyIntentService", "onStartCommand callback called");
        manualDelete = false;
        if (intent != null) {
            background = intent.getBooleanExtra("background", background);
        }
        startService();
        return START_STICKY;
    }

    public void setBackground(boolean b) {
        background = b;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.w("MyIntentService", "onStart callback called");
        super.onStart(intent, startId);
    }

    private void startService() {
        Log.i("~~~~~~", "~~~~~~ starting service");
        startForeground(1338, buildForegroundNotification());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        DBService.getInstance().addReminderListener(this);
        ArrayList<Reminder> tmp = SharedPreferencesManager.getInstance().getReminders(this);
        if (tmp != null) {
            currentReminders = tmp;
            Log.i("~~~~~~", "~~~~~~ reminder size = " + currentReminders.size());
        }

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getBaseContext(), locationResult);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        //App in background
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        background = false;
    }

    private void checkIfUserInReminder(LatLng location) {
        ArrayList<Reminder> remindersToDelete = new ArrayList<>();
        if (currentReminders != null && location != null) {
            for (Reminder r : currentReminders) {
                if (r.getReminderType().equals("location")) {
                    LocationReminder reminder = (LocationReminder) r;
                    LatLng reminderLocation = ((LocationReminder) r).getLatLng();
                    double dist = LocationUtil.getDistanceFromLatLonInMeters(reminderLocation, location);
                    if (dist < reminder.getRadius()) {
                        ApplicationUtil.sendNotification(this, reminder);
                        DBService.getInstance().deleteReminder(reminder, !background);
                        remindersToDelete.add(r);
                    }
                }
            }
            currentReminders.removeAll(remindersToDelete);
            SharedPreferencesManager.getInstance().saveReminders(this, currentReminders);
        }
    }

    private Notification buildForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "location_reminder_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Location Reminder";
            String Description = "Location Reminder Channel";
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setImportance(IMPORTANCE_MIN);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            mChannel.setSound(null, null);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent snoozeIntent = new Intent(this, StopLocationServiceReceiver.class);
        snoozeIntent.putExtra("kill", true);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher).addAction(new NotificationCompat.Action(R.mipmap.baseline_close_black_18dp, "Stop", snoozePendingIntent))
                .setPriority(IMPORTANCE_MIN);

        Intent resultIntent = new Intent(this, LocationReminderService.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        Log.w("MyIntentService", "onDestroy callback called");
        if (!manualDelete) {
            Intent broadcastIntent = new Intent(this, LocationServiceReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        manualDelete = false;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
    }
}

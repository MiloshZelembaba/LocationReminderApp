package com.example.miloshzelembaba.reminders;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.app.NotificationManager.IMPORTANCE_MAX;
import static android.app.NotificationManager.IMPORTANCE_MIN;
import static android.app.NotificationManager.IMPORTANCE_NONE;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class LocationReminderService extends Service implements ReminderService.RemindersListener {
    CurrentLocationManager currentLocationManager = CurrentLocationManager.getInstance();
    ArrayList<Reminder> currentReminders = new ArrayList<>();

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

            Log.i("~~~~~~", "~~~~~~ lat=" + location.getLatitude() + "   lon="+location.getLongitude());
            checkIfUserInReminder(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    };

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
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListenerGps);
                if (network_enabled)
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListenerNetwork);
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
        startService();

        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.w("MyIntentService", "onStart callback called");
        super.onStart(intent, startId);
    }

    private void startService() {
        Log.i("~~~~~~", "~~~~~~ starting service");
        startForeground(1338, buildForegroundNotification());

        DBService.getInstance().addReminderListener(this);
//        ArrayList<Reminder> tmp = SharedPreferencesManager.getInstance().getReminders(this);
//        if (tmp != null) {
//            currentReminders = tmp;
//            Log.i("~~~~~~", "~~~~~~ reminder size = " + currentReminders.size());
//        }
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getBaseContext(), locationResult);
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
                        // todo: this should eventually get updated to be offline i think... Will crash when just the service is running and we need to delte a reminder
                        DBService.getInstance().deleteReminder(reminder);
                        remindersToDelete.add(r);
                    }
                }
            }
            currentReminders.removeAll(remindersToDelete);
        }
    }

    private Notification buildForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
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
//        Intent broadcastIntent = new Intent(this, LocationServiceReceiver.class);
//
//        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
//        Intent restartServiceIntent = new Intent(getBaseContext(), this.getClass());
//        restartServiceIntent.setPackage(getPackageName());
//
//        PendingIntent restartServicePendingIntent = PendingIntent.getService(getBaseContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(
//                AlarmManager.ELAPSED_REALTIME,
//                SystemClock.elapsedRealtime() + 1000,
//                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}

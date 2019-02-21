package com.example.miloshzelembaba.reminders.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.example.miloshzelembaba.reminders.Activities.MainActivity;
import com.example.miloshzelembaba.reminders.LocationReminderService;
import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.Models.User;
import com.example.miloshzelembaba.reminders.R;

public class ApplicationUtil {
    private static User user;

    public static void setUser(User u) {
        user = u;
    }

    public static User getUser() {
        return user;
    }

    public static void sendNotification(Context context, String title, String description) {
        if (title == null) {
            title = "";
        }
        int NOTIFICATION_ID = title.hashCode();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description);

        Intent resultIntent = new Intent(context, LocationReminderService.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    public static void sendNotification(Context context, Reminder reminder) {
        sendNotification(context, reminder.getTitle(), reminder.getDescription());
    }
}

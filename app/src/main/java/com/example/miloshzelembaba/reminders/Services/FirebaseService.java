package com.example.miloshzelembaba.reminders.Services;

import com.example.miloshzelembaba.reminders.RabbitMQ.DBService;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService {
    DBService dbService = DBService.getInstance();

    @Override
    public void onNewToken(String token) {
        if (ApplicationUtil.getUser() != null) {
            dbService.updateFCMToken(token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            ApplicationUtil.sendNotification(this, notification.getTitle(), notification.getBody());
        }
    }
}

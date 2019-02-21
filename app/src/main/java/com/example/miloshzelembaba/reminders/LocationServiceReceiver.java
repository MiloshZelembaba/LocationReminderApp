package com.example.miloshzelembaba.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LocationReminderService.class));
    }
}

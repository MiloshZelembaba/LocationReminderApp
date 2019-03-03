package com.example.miloshzelembaba.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationReminderService.class);
        i.putExtra("background", true);
        context.startService(i);
    }
}

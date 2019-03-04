package com.example.miloshzelembaba.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopLocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationReminderService.class);
        LocationReminderService.manualDelete = true;
        context.stopService(i);
    }
}

package com.example.miloshzelembaba.reminders;

import com.example.miloshzelembaba.reminders.Models.Reminder;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReminderManager {
    private static ReminderManager instance = new ReminderManager();
    private ArrayList<Reminder> reminders = new ArrayList();

    private ReminderManager(){}

    public static ReminderManager getInstance() {
        return instance;
    }

    public void updateReminders(ArrayList<Reminder> reminders) {
        // todo: should this be nicer?
        this.reminders = reminders;
    }

    public ArrayList<Reminder> getReminders() {
        return reminders;
    }

    public boolean performReminderCleanup(ArrayList<Reminder> upToDateReminders, ArrayList<Reminder> staleReminders) {
        if (upToDateReminders == null || staleReminders == null) {
            return false;
        }

        ArrayList<Reminder> remindersToDelete = new ArrayList<>();
        for (Reminder staleReminder : staleReminders) {
            boolean stillAlive = false;
            for (Reminder uptodateReminder : upToDateReminders) {
                stillAlive |= uptodateReminder.getId().equals(staleReminder.getId());
            }

            if (!stillAlive) {
                remindersToDelete.add(staleReminder);
            }
        }

        ReminderService.getInstance().deleteReminders(remindersToDelete);
        return true;
    }


}

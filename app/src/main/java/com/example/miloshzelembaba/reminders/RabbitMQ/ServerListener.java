package com.example.miloshzelembaba.reminders.RabbitMQ;

import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;

public interface ServerListener {
    void onServerObjectReceived(ServerObject serverObject);
}

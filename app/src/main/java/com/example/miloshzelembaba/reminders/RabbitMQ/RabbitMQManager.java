package com.example.miloshzelembaba.reminders.RabbitMQ;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.BaseService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.PlaceAutocompleteService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.ReminderService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.UpdateHomeAddressService;
import com.example.miloshzelembaba.reminders.RabbitMQ.Services.UserService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

// todo: maybe this should be a service instead.

public class RabbitMQManager {
    private ConnectionFactory factory = new ConnectionFactory();
    private static RabbitMQManager instance;
    private static AMQP.Queue.DeclareOk queue;

    private HashMap<String, BaseService> eventToCallbackMap = new HashMap<>();
    private final String REMINDER_SERVICE_EVENT = "get_reminder_service_event";
    private final String USER_SERVICE_EVENT = "user_service_event";
    private final String PLACE_AUTOCOMPLETE = "place_auto_complete";
    private final String UPDATE_HOME_ADDRESS_EVENT = "update_home_address_event";

    @Nullable
    public static RabbitMQManager getInstance() {
        if (instance == null) {
            try {
                instance = new RabbitMQManager();
            } catch (Exception e) {
                return null;
            }
        }
        return instance;
    }

    public boolean isQ() {
        return queue != null;
    }

    private RabbitMQManager() throws Exception{
        setupConnectionFactory();
        eventToCallbackMap.put(REMINDER_SERVICE_EVENT, ReminderService.getInstance());
        eventToCallbackMap.put(USER_SERVICE_EVENT, UserService.getInstance());
        eventToCallbackMap.put(PLACE_AUTOCOMPLETE, PlaceAutocompleteService.getInstance());
        eventToCallbackMap.put(UPDATE_HOME_ADDRESS_EVENT, UpdateHomeAddressService.getInstance());
    }

    private void setupConnectionFactory() throws Exception{
        String uri = "amqp://ptqfmgez:CgSp62yRLQrGGbT3RNUEYElwzX4fO8vj@wombat.rmq.cloudamqp.com/ptqfmgez"; // todo: put this on the server
        factory.setAutomaticRecoveryEnabled(false); // todo: ??
        factory.setUri(uri);
    }

    public void connectToQueue(String queueName) { // todo: add the exchange and what not
        if (queue == null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1); // todo: take this out?
                        queue = channel.queueDeclare(queueName, false, false, false, null);
                        channel.basicConsume(queueName, true,
                                new DefaultConsumer(channel) {
                                    @Override
                                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                                        try {
                                            ServerObject serverObject = new ServerObject(new String(body));
                                            if (eventToCallbackMap.containsKey(serverObject.eventType)) {
                                                BaseService service = eventToCallbackMap.get(serverObject.eventType);
                                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                                Runnable runnable1 = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        service.onServerObjectReceived(serverObject);
                                                    }
                                                };
                                                mainHandler.post(runnable1);
                                            } else {
                                                Log.e("ServerObject", "Unknown event type received: " + serverObject.eventType);
                                            }
                                        } catch (JSONException e) {
                                            queue = null;
                                            connectToQueue(queueName);
                                            Log.e("ServerObject", e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                                        queue = null;
                                        connectToQueue(queueName);
                                        Log.e("RabbitMQ", "Couldn't connect to queue");
                                    }
                                });
                    } catch (Exception e) {
                        queue = null;
                        connectToQueue(queueName);
                        Log.e("RabbitMQ", "Couldn't connect to queue. e=" + e.getMessage());
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}

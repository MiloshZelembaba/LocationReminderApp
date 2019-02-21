package com.example.miloshzelembaba.reminders.RabbitMQ.Services;

import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.NetworkUtil;

import org.json.JSONObject;

public class FCMService extends BaseService {
    private static FCMService instance = new FCMService();
    private final String updateFCMTokenEndpoint = NetworkUtil.getServerAddress() + "updateFCMToken/";

    public static FCMService getInstance() {
        return instance;
    }

    private FCMService(){}

    public void updateFCMToken(String token) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("fcm_token", token);
            sendPost(updateFCMTokenEndpoint, obj);
        } catch (Exception e) {}
    }

    @Override
    public void onServerObjectReceived(ServerObject serverObject) {
        // no op
    }
}

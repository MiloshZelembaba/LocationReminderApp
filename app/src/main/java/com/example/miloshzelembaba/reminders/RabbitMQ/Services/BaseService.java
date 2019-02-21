package com.example.miloshzelembaba.reminders.RabbitMQ.Services;

import android.util.Log;

import com.example.miloshzelembaba.reminders.RabbitMQ.ServerListener;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

abstract public class BaseService implements ServerListener {

    void sendPost(String endpoint, JSONObject object) {
        Timer t = new Timer();
        sendPost(endpoint, object, 0, t);
    }

    private void sendPost(String endpoint, JSONObject object, int attempt, Timer timer) {
        if (attempt > 10) {
            timer.cancel();
            return;
        }

        try {
            byte[] postDataBytes = getBytesFromJson(object);
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            conn.getInputStream(); // i have to call this in order for the request to be sent
            conn.disconnect();
            timer.cancel();
        } catch (Exception e) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendPost(endpoint, object, attempt + 1, timer);
                }
            }, 2000);
        }
    }

    private byte[] getBytesFromJson(JSONObject obj) {
        StringBuilder postData = new StringBuilder();
        postData.append(obj.toString());

        try {
            return postData.toString().getBytes("UTF-8");
        } catch (Exception e){
            Log.e("getBytesFromJson", "couldn't do it");
            return null;
        }
    }
}

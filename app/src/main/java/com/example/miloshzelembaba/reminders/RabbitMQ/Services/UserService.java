package com.example.miloshzelembaba.reminders.RabbitMQ.Services;

import com.example.miloshzelembaba.reminders.Models.User;
import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.Utils.NetworkUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class UserService extends BaseService {
    private static UserService instance = new UserService();
    private ArrayList<UserInfoListener> listeners = new ArrayList<>();
    private final String endpoint = NetworkUtil.getServerAddress() + "getUserInfo/";

    private UserService() {}

    public static UserService getInstance() {
        return instance;
    }

    public void addListener(UserInfoListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void getUserInfo(String deviceId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("device_id", deviceId);
            System.out.println("~~~~~~ sent out user request");
            sendPost(endpoint, obj);
        } catch (Exception e) {
            notifyListeners(null, true);
        }
    }

    private void notifyListeners(User user, boolean error) {
        for (UserInfoListener listener : listeners) {
            listener.onUserInfoReceived(user, error);
        }
    }

    @Override
    public void onServerObjectReceived(ServerObject serverObject) {
        notifyListeners(serverObject.getUser(), false);
    }

    public interface UserInfoListener {
        void onUserInfoReceived(User user, boolean error);
    }
}

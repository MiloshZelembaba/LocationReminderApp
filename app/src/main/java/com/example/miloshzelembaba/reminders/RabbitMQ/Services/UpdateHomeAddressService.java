package com.example.miloshzelembaba.reminders.RabbitMQ.Services;

import com.example.miloshzelembaba.reminders.CurrentLocationManager;
import com.example.miloshzelembaba.reminders.RabbitMQ.ServerObjects.ServerObject;
import com.example.miloshzelembaba.reminders.Utils.ApplicationUtil;
import com.example.miloshzelembaba.reminders.Utils.NetworkUtil;

import org.json.JSONObject;

public class UpdateHomeAddressService extends BaseService {
    private static UpdateHomeAddressService instance = new UpdateHomeAddressService();
    private final String endPoint = NetworkUtil.getServerAddress() + "updateHomeAddress/";

    private UpdateHomeAddressService(){}

    public static UpdateHomeAddressService getInstance() {
        return instance;
    }

    public void setHome(double lat, double lon) {
        CurrentLocationManager locationManager = CurrentLocationManager.getInstance();
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", ApplicationUtil.getUser().getJson());
            obj.put("lat", lat);
            obj.put("lon", lon);

            sendPost(endPoint, obj);
        } catch (Exception e) {}
    }

    @Override
    public void onServerObjectReceived(ServerObject serverObject) {
        ApplicationUtil.setUser(serverObject.getUser());
    }
}

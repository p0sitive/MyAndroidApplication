package com.lesports.geneliveman.ble;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by chenshang on 16/4/12.
 */
public class LMBleApi {
    private static final String TAG = "LMBleApi";

    public static final int WIFI_MODE_AP = 0;
    public static final int WIFI_MODE_STA = 1;

    public static final String STA = "sta";
    public static final String AP = "ap";


    private LMBleConnection lmBleConnection;


    public LMBleApi(LMBleConnection lmBleConnection) {
        this.lmBleConnection = lmBleConnection;
    }

    /**
     * @param mode WIFI_MODE_APor WIFI_MODE_STA
     */
    public void setWifiMode(@NonNull final int mode, final Callback callback) {

        send(Command.CMD_SET_MODE, String.valueOf(mode), new Callback() {
            @Override
            public void onPrepareSend() {
                callback.onPrepareSend();
            }

            @Override
            public void onSended() {
                callback.onSended();
            }

            @Override
            public void onResponse(String response) {
                callback.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // 设置相机wifi模式 ap
    public void setModeAp(Callback callback) {
        log("setModeAp");
        setWifiMode(WIFI_MODE_AP, callback);
    }

    // 设置相机wifi模式 sta
    public void setModeSta(Callback callback) {
        log("setModeSta");
        setWifiMode(WIFI_MODE_STA, callback);
    }


    public void setSSID(String ssid, Callback callback) {
        log("setSSID:" + ssid);
        send(Command.CMD_SET_STA_SSID, ssid, callback);
    }

    public void setPassword(String password, Callback callback) {
        log("setPassword:" + password);
        send(Command.CMD_SET_STA_PASSWORD, password, callback);
    }

    // 重启相机Wifi
    public void reboot(Callback callback) {
        log("reboot");
        send(Command.CMD_REBOOT, null, callback);
    }

    // 重启相机Wifi
    public void restartWifi(Callback callback) {
        log("restartWifi");
        reboot(callback);
    }

    // 获取IP地址
    public void fetchIpAddress(final Callback callback) {
        log("fetchIpAddress");
        send(Command.CMD_GET_IP, null, true, new Callback() {
            @Override
            public void onPrepareSend() {
                callback.onPrepareSend();
            }

            @Override
            public void onSended() {
                callback.onSended();
            }

            @Override
            public void onResponse(String response) {
                lmBleConnection.setIp(response);
                callback.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    public void fetchSSID(final Callback callback) {
        log("fetchSSID");
        send(Command.CMD_GET_STA_SSID, null, true, new Callback() {
            @Override
            public void onPrepareSend() {
                callback.onPrepareSend();
            }

            @Override
            public void onSended() {
                callback.onSended();
            }

            @Override
            public void onResponse(String response) {
                callback.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchApSSID(final Callback callback) {
        log("fetchApSSID");
        send(Command.CMD_GET_AP_SSID, null, true, new Callback() {
            @Override
            public void onPrepareSend() {
                callback.onPrepareSend();
            }

            @Override
            public void onSended() {
                callback.onSended();
            }

            @Override
            public void onResponse(String response) {
                callback.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void fetchMode(final Callback callback) {
        log("fetchMode");
        send(Command.CMD_AP_OR_STA, null, true, new Callback() {
            @Override
            public void onPrepareSend() {
                callback.onPrepareSend();
            }

            @Override
            public void onSended() {
                callback.onSended();
            }

            @Override
            public void onResponse(String response) {
                lmBleConnection.setWifiMode(response);
                callback.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public String send(@NonNull String cmd, @Nullable String param, Callback callback) {
        return lmBleConnection.send(cmd, param, false, callback);
    }

    public String send(String cmd, String param, boolean isWaitResponse, Callback callback) {
        return lmBleConnection.send(cmd, param, isWaitResponse, callback);
    }

    public static void log(String log) {
        Log.w(TAG, log);
    }

    public boolean hasAvailaibleIp(){
        return lmBleConnection.hasAvailaibleIp();
    }

    public String getIp(){
        return lmBleConnection.getIp();
    }

    public String getWifiMode(){
        return lmBleConnection.getWifiMode();
    }


}

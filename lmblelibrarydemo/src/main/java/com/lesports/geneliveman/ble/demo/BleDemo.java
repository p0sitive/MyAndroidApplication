package com.lesports.geneliveman.ble.demo;

import android.app.Application;

import com.lesports.geneliveman.ble.LivemanBleManager;

/**
 * Created by chenshang on 16/4/5.
 */
public class BleDemo extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LivemanBleManager.init(this);
    }
}

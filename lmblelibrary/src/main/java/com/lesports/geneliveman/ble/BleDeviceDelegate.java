package com.lesports.geneliveman.ble;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

/**
 * Created by raysun on 15/11/11.
 */
public class BleDeviceDelegate {
    BluetoothDevice mDevice;

    public BleDeviceDelegate(@NonNull BluetoothDevice device) {
        mDevice = device;
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    @Override
    public String toString() {
        return "name: " + mDevice.getName() + "\nmac/sn: " + mDevice.getAddress();
    }

    public String getName() {
        return mDevice.getName();
    }

    public int getBondState() {
        return mDevice.getBondState();
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }
}

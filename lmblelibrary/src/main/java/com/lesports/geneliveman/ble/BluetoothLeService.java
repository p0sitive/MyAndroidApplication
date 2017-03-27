/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lesports.geneliveman.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String, LMBleConnection> bleConnectionMap = new HashMap<>();
    private Map<String, LMBleConnection> bleConnectionConnectingMap = new HashMap<>();

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public LMBleConnection connect(final String address) {
        Log.d(TAG, "connect: " + address);
        if (bleConnectionConnectingMap.containsKey(address)) {
            return bleConnectionConnectingMap.get(address);
        }

        if (bleConnectionMap.containsKey(address)) {
            Log.d(TAG, "connect: has connected return");
            return bleConnectionMap.get(address);
        }
        LMBleConnection connection = new LMBleConnection(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getRemoteDevice(address).connectGatt(this, false, connection, BluetoothDevice.TRANSPORT_LE);
        } else {
            getRemoteDevice(address).connectGatt(this, false, connection);
        }
        connection.registerListener(new LMBleConnection.Listener() {
            @Override
            public boolean onConnected(LMBleConnection connection) {
                bleConnectionMap.put(address, connection);
                bleConnectionConnectingMap.remove(address);
                return false;
            }

            @Override
            public boolean onDisconnected(LMBleConnection connection) {
                bleConnectionMap.remove(address);
                bleConnectionConnectingMap.remove(address);
                return true;
            }

            @Override
            public boolean onServicesDiscovered(LMBleConnection connection) {
                return false;
            }
        });
        bleConnectionConnectingMap.put(address, connection);
        return connection;
    }

    private BluetoothDevice getRemoteDevice(String address) {
        return mBluetoothAdapter.getRemoteDevice(address);
    }

//    public boolean isConnected(String address){
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            return mConnectionState == STATE_CONNECTED;
//        }
//        return false;
//    }

    public boolean isConnected(String address) {
        if (bleConnectionMap.containsKey(address)) {
            return bleConnectionMap.get(address).isConnected();
        } else {
            Log.d(TAG, "isConnected: do not contains address");
        }
        return false;
    }

    public boolean isConnecting(String address) {
        return bleConnectionConnectingMap.containsKey(address);
    }

    public LMBleConnection getConnection(String address) {
        return bleConnectionMap.get(address);
    }

    public Collection<LMBleConnection> getConnections() {
        return bleConnectionMap.values();
    }


}

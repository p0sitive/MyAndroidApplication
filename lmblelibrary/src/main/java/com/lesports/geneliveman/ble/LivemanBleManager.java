package com.lesports.geneliveman.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Collection;


/**
 * Created by chenshang on 16/3/30.
 */
public final class LivemanBleManager implements ServiceConnection {

    public static final String ACTION_GATT_CONNECTED = LMBleConnection.ACTION_GATT_CONNECTED;
    public static final String ACTION_GATT_SERVICES_DISCOVERED = LMBleConnection.ACTION_GATT_SERVICES_DISCOVERED;
    public static final String ACTION_GATT_SERVICES_DISCOVERED_FAILURE = LMBleConnection.ACTION_GATT_SERVICES_DISCOVERED_FAILURE;
    public static final String ACTION_GATT_DISCONNECTED = LMBleConnection.ACTION_GATT_DISCONNECTED;

    private static final String TAG = "LivemanBleManager";

    private static LivemanBleManager bleManager;

    private BluetoothLeService bluetoothLeService;


    private LivemanBleManager(Context context) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        context.startService(intent);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public synchronized static void init(Context context) {
        bleManager = new LivemanBleManager(context);
    }

    public static LivemanBleManager getInstance() {
        return bleManager;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BluetoothLeService.LocalBinder localBinder = (BluetoothLeService.LocalBinder) service;
        bluetoothLeService = localBinder.getService();
        bluetoothLeService.initialize();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bluetoothLeService = null;
    }

    public LMBleConnection connect(String address) {
        Log.d(TAG, "connect: "+address);
        if (bluetoothLeService != null) {
            LMBleConnection connection = bluetoothLeService.connect(address);
            return connection;
        } else {
            Log.w(TAG, "bluetoothLeService not init");
        }
        return null;
    }

    public boolean isConnected(String address) {
        if (bluetoothLeService != null) {
            return bluetoothLeService.isConnected(address);
        } else {
            Log.w(TAG, "bluetoothLeService not init");
        }
        return false;
    }

    public boolean isConnecting(String address){
        if (bluetoothLeService != null) {
            return bluetoothLeService.isConnecting(address);
        } else {
            Log.w(TAG, "bluetoothLeService not init");
        }
        return false;
    }

    public LMBleConnection getConnection(String address){
        if(bluetoothLeService !=null){
            return bluetoothLeService.getConnection(address);
        }
        return null;
    }

    public Collection<LMBleConnection> getConnections(){
        if(bluetoothLeService !=null){
            return bluetoothLeService.getConnections();
        }
        return null;
    }

}

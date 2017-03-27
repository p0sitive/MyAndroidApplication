package com.lesports.geneliveman.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenshang on 16/3/30.
 */
public class BluetoothScanner implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BluetoothScanner";

    private static final int REQUEST_CODE_FIND_LOCATION = 10;

    public static final String ACTION_FOUND_NEW_DEVICE = "com.lesports.geneliveman.ble.ACTION_FOUND_NEW_DEVICE";

    public static final String[] CAMERA_NAMES = {"amba", "liveman"};

    private static final Handler hanlder = new Handler(Looper.getMainLooper());

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Listener listener;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private boolean isScanning;
    private boolean isRequestScanning;
    private Context context;


    public BluetoothScanner(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {

                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FIND_LOCATION);
                }
                return;
            }
        }
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScan() {

        if (isScanning) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_FIND_LOCATION);
                    isRequestScanning = true;
                }
                return;
            } else {
                bluetoothAdapter.startLeScan(this);
                isScanning = true;
            }
        } else {

            bluetoothAdapter.startLeScan(this);
            isScanning = true;
        }
    }


    public void startScan(long period) {
        if (isScanning) {
            return;
        }
        bluetoothAdapter.startLeScan(this);
        isScanning = true;
        CountDownTimer timer = new CountDownTimer(Long.MAX_VALUE, period) {
            @Override
            public void onTick(long millisUntilFinished) {
                stopScan();
                startScan();
            }

            @Override
            public void onFinish() {

            }
        };
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_FIND_LOCATION:
                if (grantResults.length > 0&&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    isRequestScanning = false;
                    startScan();
                } else {

                }
                break;
        }
    }


    public void stopScan() {
        isRequestScanning = false;
        if (isScanning) {
            bluetoothAdapter.stopLeScan(this);
            isScanning = false;
            notifyOnScanStoped();
        }
    }

    public void clear() {
        devices.clear();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.w(TAG, "onLeScan:" + device.getName() + " " + device.getAddress());
//        Log.w(TAG, "devices:" + devices);
        if (filter(device.getName())) {
            if (!devices.contains(device)) {
                Log.w(TAG, "add :" + device.getName() + " " + device.getAddress());
                devices.add(device);
            }
            sendFoundDeviceBroadcast(device);
            if (!devices.isEmpty()) {
                notifyOnDiscoveryDevices();
            }
        }
    }

    private void sendFoundDeviceBroadcast(BluetoothDevice device) {
        Intent intent = new Intent(ACTION_FOUND_NEW_DEVICE);
        intent.putExtra("device", device);
        context.sendBroadcast(intent);
    }

    private void notifyOnDiscoveryDevices() {

        hanlder.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDiscoveryDevices(devices);
                }
            }
        });
    }

    private void notifyOnScanStoped() {
        hanlder.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onScanStoped();
                }
            }
        });
    }

    private boolean filter(String deviceName) {
        if (!TextUtils.isEmpty(deviceName)) {
            for (String name : CAMERA_NAMES) {
                if (deviceName.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static interface Listener {
        public void onDiscoveryDevices(List<BluetoothDevice> devices);

        public void onScanStoped();
    }
}

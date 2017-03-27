package com.lesports.geneliveman.ble.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import com.lesports.geneliveman.ble.LMBleConnection;
import com.lesports.geneliveman.ble.LivemanBleManager;

import java.util.Calendar;

/**
 * Created by chenshang on 16/4/5.
 */
public class ConnectedBleActivity extends Activity {

    private TextView textView;
    private String address;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();

        setContentView(R.layout.activity_connected);

        textView = (TextView) findViewById(R.id.textView);
        textView.setText("");
        address = getIntent().getStringExtra("address");
        LivemanBleManager.getInstance().connect(address);
        logText("正在连接 " + address);

//        BluetoothDevice remoteDevice = LivemanBleManager.getInstance().getRemoteDevice(address);
//        if (remoteDevice != null) {
//            remoteDevice.connectGatt(this, false, new BluetoothGattCallback() {
//                @Override
//                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//
//                    if(newState == BluetoothProfile.STATE_CONNECTED){
//                        logText("已连接");
//                        boolean discoverServices = gatt.discoverServices();
//                        logText("搜索服务："+discoverServices);
//                    }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
//                        logText("已断开连接");
//                    }
//                }
//
//                @Override
//                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                    List<BluetoothGattService> services = gatt.getServices();
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append("已发现服务\n");
//                    for (BluetoothGattService service:services
//                         ) {
//                        buffer.append(service.getUuid().toString()+" "+service.getType() + "\n");
//                    }
//                    logText(buffer.toString());
//                }
//            });
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectedReceiver, new IntentFilter(LivemanBleManager.ACTION_GATT_CONNECTED));
        registerReceiver(discoveredReceiver, new IntentFilter(LivemanBleManager.ACTION_GATT_SERVICES_DISCOVERED));
        registerReceiver(disconnectedReceiver, new IntentFilter(LivemanBleManager.ACTION_GATT_DISCONNECTED));
        registerReceiver(discoveredFailure, new IntentFilter(LivemanBleManager.ACTION_GATT_SERVICES_DISCOVERED_FAILURE));
    }

    private BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logText("蓝牙已连接");
        }
    };


    private BroadcastReceiver discoveredFailure = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logText("搜索ATT服务失败");
        }
    };

    private BroadcastReceiver discoveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logText("已搜索到ATT服务");
        }
    };

    private BroadcastReceiver disconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logText("蓝牙已断开连接");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LMBleConnection connection = LivemanBleManager.getInstance().getConnection(address);
        if(connection != null){
            connection.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(connectedReceiver);
        unregisterReceiver(discoveredReceiver);
        unregisterReceiver(disconnectedReceiver);
        unregisterReceiver(discoveredFailure);
    }

    private void logText(final String log) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.append(calendar.getTime().toLocaleString());
                textView.append(log);
                textView.append("\n");
            }
        });
    }

    public static void launch(Context context, String address) {
        Intent intent = new Intent(context, ConnectedBleActivity.class);
        intent.putExtra("address", address);
        context.startActivity(intent);
    }
}

package com.p0sitive.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothDemoActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    TextView tvDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_demo);

        tvDevice = (TextView) findViewById(R.id.bluetooth_device);
    }


    public void startBluetooth(View view) {
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        Toast.makeText(this, "bluetoothAdapter.enable():" + bluetoothAdapter.enable(), Toast.LENGTH_SHORT).show();

        tvDevice.setText("");
        getSeachedDevices();

    }


    private void getSeachedDevices(){
        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                tvDevice.append(device.getName() + ":" + device.getAddress()+"\n\r");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置广播信息过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
        // 注册蓝牙搜索广播接收者，接收并处理搜索结果
        this.registerReceiver(receiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    tvDevice.append(device.getName()+":"+device.getAddress()+"\n\r");
                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Toast.makeText(context, "搜索完成", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    public void search(View view) {
        if(bluetoothAdapter==null){
            Toast.makeText(this, "请先点击bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "开始搜索蓝牙设备", Toast.LENGTH_SHORT).show();
    }

    public void Fastble(View view) {
        startActivity(new Intent(this,FastbleDemoActivity.class));
    }
}

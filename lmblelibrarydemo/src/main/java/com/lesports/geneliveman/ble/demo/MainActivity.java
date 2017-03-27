package com.lesports.geneliveman.ble.demo;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lesports.geneliveman.ble.BluetoothScanner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener ,BluetoothScanner.Listener {

    private ListView listView;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BleAdapter adapter;
    private BluetoothScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(this);
        adapter = new BleAdapter();
        listView.setAdapter(adapter);



        scanner = new BluetoothScanner(this,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        scanner.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanner.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanner.stopScan();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        scanner.stopScan();
        BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
        ConnectedBleActivity.launch(this,device.getAddress());
    }

    @Override
    public void onDiscoveryDevices(List<BluetoothDevice> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onScanStoped() {

    }

    private class BleAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_ble,parent,false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView mac = (TextView) convertView.findViewById(R.id.mac);
            BluetoothDevice device = (BluetoothDevice) getItem(position);
            name.setText(TextUtils.isEmpty(device.getName())?"UNKNOWN":device.getName());
            mac.setText(device.getAddress());

            return convertView;
        }
    }
}

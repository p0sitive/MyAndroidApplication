package com.p0sitive.aidlclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.p0sitive.aidldemo.IMyAidlInterface;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    IMyAidlInterface iMyAidlInterface = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            Log.i(TAG, "onServiceConnected: ");
            try {
                iMyAidlInterface.setMessage("test!!!!!!!!");
                Toast.makeText(MainActivity.this, iMyAidlInterface.getMessage(), Toast.LENGTH_SHORT).show();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected: ");

        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent("com.p0sitive.aidldemo.IMyAidlInterface");
        intent.setPackage("com.p0sitive.aidldemo");
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
//        try {
//            if(iMyAidlInterface!=null) {
//                Toast.makeText(MainActivity.this, iMyAidlInterface.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(iMyAidlInterface!=null){
            unbindService(mServiceConnection);
//        }
    }
}

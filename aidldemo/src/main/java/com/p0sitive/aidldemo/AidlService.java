package com.p0sitive.aidldemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by lihe6 on 2017/2/14.
 */

public class AidlService extends Service {
    private static final String TAG = "AidlService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AidlDemoBinder();
    }

    class AidlDemoBinder extends IMyAidlInterface.Stub{

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            Log.i(TAG, "basicTypes: ");
        }

        @Override
        public void setMessage(String msg) throws RemoteException {
            Log.i(TAG, "setMessage: --->"+msg);
            Toast.makeText(AidlService.this, "AidlService get Msg-->"+msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public String getMessage() throws RemoteException {
            return "This message from Demo Server";
        }

    }
}

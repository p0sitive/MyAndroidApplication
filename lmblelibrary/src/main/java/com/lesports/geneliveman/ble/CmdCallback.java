package com.lesports.geneliveman.ble;

/**
 * Created by chenshang on 16/3/18.
 */
public class CmdCallback extends Callback{

    public final String cmd;
    public final boolean isWaitResponse;
    private final Callback callback;


    public CmdCallback(String cmd,Callback callback) {
       this(cmd,callback,false);
    }

    public CmdCallback(String cmd,Callback callback,boolean isWaitResponse){
        this.cmd = cmd;
        this.callback = callback;
        this.isWaitResponse = isWaitResponse;
    }

    @Override
    public void onPrepareSend() {
        callback.onPrepareSend();
    }

    @Override
    public void onSended() {
        this.callback.onSended();
    }

    @Override
    public void onResponse(String response) {
        this.callback.onResponse(response);
    }

    @Override
    public void onError(Exception e) {
        this.callback.onError(e);
    }


}

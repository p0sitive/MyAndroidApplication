package com.lesports.geneliveman.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenshang on 16/4/12.
 */
public class LMBleConnection extends BluetoothGattCallback {

    public static final String ACTION_GATT_CONNECTED = "com.lesports.camera.ble.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.lesports.camera.ble.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.lesports.camera.ble.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED_FAILURE = "com.lesports.camera.ble.ACTION_GATT_SERVICES_DISCOVERED_FAILURE";
    public static final String ACTION_AVAIABLE_IP = "com.lesports.geneliveman.ble.ACTION_AVAIABLE_IP";

    // 相机服务UUID
    private static final String UUID_SERVICE = "00000000-0000-1000-8000-00805f9b34fb";
    // 相机写characteristic
    private static final String UUID_CHARACTERISTIC_WRITE = "00001111-0000-1000-8000-00805f9b34fb";
    // 相机读characteristic
    private static final String UUID_CHARACTERISTIC_READ = "00003333-0000-1000-8000-00805f9b34fb";

    private static final String TAG = "LMBleConnection";

    private static final long WRITE_INTERVAL = 500L;

    private BluetoothGatt mBluetoothGatt;
    private boolean isServiceDiscovered;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private BluetoothGattCharacteristic writeCharacteristic;

    private Queue<CmdCallback> cmdQueue = new LinkedList<>();
    private long lastWriteTime;
    private CmdCallback currentCmdCallback;

    private String wifiMode;
    private String ip = "192.168.42.1";
    private Context context;

    private List<Listener> listeners = new ArrayList<>();

    public LMBleConnection(Context context) {
        this.context = context;
    }

    public void setBluetoothGatt(BluetoothGatt gatt) {
        this.mBluetoothGatt = gatt;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }


    public boolean isConnected() {
        Log.d(TAG, "isConnected: " + (mBluetoothGatt != null));
        return mBluetoothGatt != null&&isServiceDiscovered;
    }


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt != null) {

            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                 boolean enabled) {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        }
        return false;
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public String send(String cmd, String param, boolean isWaitResponse, Callback callback) {
        log("send  cmd: " + cmd + ", param: " + param);
        String cmdStr = BleCmdFactory.getCommand(cmd, param);
        log("send command :" + cmdStr + ", bytes: " + ByteUtils.byteArrayToString(ByteUtils.hexStringToBytes(cmdStr)));
        offerCmd(new CmdCallback(cmdStr, callback, isWaitResponse));
        return cmdStr;
    }

    public static void log(String log) {
        Log.w(TAG, log);
    }

    void onWrote() {
        lastWriteTime = System.currentTimeMillis();
        CmdCallback cmdCallback = currentCmdCallback;
        notifyCmdSended(cmdCallback);
        if (cmdCallback != null && !cmdCallback.isWaitResponse) {
            writeNextCmd();
        }
    }

    private void notifyCmdSended(final CmdCallback cmdCallback) {
        if (cmdCallback == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                cmdCallback.onSended();

            }
        });
    }

    private void notifyCmdOnError(final CmdCallback cmdCallback, final Exception e) {
        if (cmdCallback == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                cmdCallback.onError(e);
            }
        });
    }

    private void notifyCmdResponse(final CmdCallback cmdCallback, final String result) {
        if (cmdCallback == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                cmdCallback.onResponse(result);
            }
        });
    }


    private final Runnable writeRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            if (cmdQueue.isEmpty()) {
                log("cmdQueue isEmpty");
                return;
            }

            long now = System.currentTimeMillis();
            long delay = now - lastWriteTime;
            if (delay < WRITE_INTERVAL) {
                handler.postDelayed(writeRunnable, WRITE_INTERVAL - delay);
                log("delay < WRITE_INTERVAL postDelayed after " + (WRITE_INTERVAL - delay) + "ms");
                return;
            }
            ;

            CmdCallback cmdCallback = cmdQueue.poll();
            log("write cmd: " + cmdCallback.cmd);
            if (writeCharacteristic == null) {
                return;
            }
            if (writeCharacteristic != null) {
                writeCharacteristic.setValue(ByteUtils.hexStringToBytes(cmdCallback.cmd));
                currentCmdCallback = cmdCallback;
                notifyOnPrepareSend(cmdCallback);
                writeCharacteristic(writeCharacteristic);
            }
        }
    };

    private void notifyOnPrepareSend(final CmdCallback cmdCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                cmdCallback.onPrepareSend();
            }
        });
    }

    private void writeNextCmd() {
        log("delay write next cmd");
        currentCmdCallback = null;
        handler.postDelayed(writeRunnable, WRITE_INTERVAL);
    }

    private void offerCmd(CmdCallback cmdCallback) {
        boolean isEmpty = cmdQueue.isEmpty();
        log("offerCmd isEmpty=" + isEmpty);
        cmdQueue.offer(cmdCallback);
        if (isEmpty && currentCmdCallback == null) {
            writeRunnable.run();
        }
    }

    private void getCharacteristics() {
        List<BluetoothGattService> supportedGattServices = getSupportedGattServices();
        for (BluetoothGattService gattService : supportedGattServices) {
            Log.d(TAG, "getCharacteristics: " + gattService.getUuid());
            if (gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
                List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic c :
                        characteristics) {
                    Log.d(TAG, "getCharacteristics: " + c.getUuid());
                }
                writeCharacteristic = gattService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_WRITE));
                BluetoothGattCharacteristic readCharacteristic = gattService.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_READ));
                boolean notification = setCharacteristicNotification(readCharacteristic, true);
                Log.w(TAG, "setCharacteristicNotification " + notification);
            }
        }
    }

    public String getIp() {
        return ip;
    }

    void setIp(String ip) {
        this.ip = ip;
        if (hasAvailaibleIp()) {
            broadcastAvaiableIp();
        }
    }

    private void broadcastAvaiableIp() {
        Intent intent = new Intent(ACTION_AVAIABLE_IP);
        intent.putExtra("ip", ip);
        context.sendBroadcast(intent);
    }

    public BluetoothDevice getDevice() {
        if (mBluetoothGatt != null) {

            return mBluetoothGatt.getDevice();
        }
        return null;
    }

    public String getWifiMode() {
        return wifiMode;
    }

    void setWifiMode(String wifiMode) {
        this.wifiMode = wifiMode;
    }

    public boolean hasAvailaibleIp() {
        if (TextUtils.isEmpty(ip)) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches() && (!"0.0.0.0".equals(ip));
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.w(TAG, "onConnectionStateChange");
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            this.mBluetoothGatt = gatt;
            Log.i(TAG, "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            Log.i(TAG, "Attempting to start service discovery:" +
                    gatt.discoverServices());
            onConnected();
            sendConnectedBroadcast(gatt.getDevice());

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            onDisconnected();
            sendDisconnectedBroadcast(gatt.getDevice());
            this.mBluetoothGatt = null;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.w(TAG, "onServicesDiscovered");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            isServiceDiscovered = true;
            onServicesDiscovered();
            sendDisconveredBroadcast(gatt.getDevice());
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            sendDisconveredFailureBroadcast(gatt.getDevice());
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        Log.w(TAG, "onCharacteristicRead");
        if (status == BluetoothGatt.GATT_SUCCESS) {
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.w(TAG, "onCharacteristicChanged");
        byte[] data = characteristic.getValue();
        UUID uuid = characteristic.getUuid();
        onDataAvailable(data, uuid);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.w(TAG, "onCharacteristicWrite");
        onWrote();
    }

    private void sendConnectedBroadcast(BluetoothDevice device) {
        if (device != null) {
            Intent intent = new Intent(ACTION_GATT_CONNECTED);
            intent.putExtra("device", device);
            context.sendBroadcast(intent);
        }
    }

    private void sendDisconnectedBroadcast(BluetoothDevice device){
        if (device != null) {
            Intent intent = new Intent(ACTION_GATT_DISCONNECTED);
            intent.putExtra("device", device);
            context.sendBroadcast(intent);
        }
    }

    private void sendDisconveredBroadcast(BluetoothDevice device) {
        if (device != null) {
            Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);
            intent.putExtra("device", device);
            context.sendBroadcast(intent);
        }
    }

    private void sendDisconveredFailureBroadcast(BluetoothDevice device){
        if (device != null) {
            Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED_FAILURE);
            intent.putExtra("device", device);
            context.sendBroadcast(intent);
        }
    }

    void onDataAvailable(byte[] data, UUID uuid) {
        if (UUID_CHARACTERISTIC_READ.equalsIgnoreCase(uuid.toString())) {
            String result = "";
            try {
                result = new String(Arrays.copyOfRange(data, 4, data.length - 1));
            } catch (Exception e) {

            }
            log("onDataAvailable:" + result);
            CmdCallback cmdCallback = currentCmdCallback;
            if (cmdCallback != null && cmdCallback.isWaitResponse) {
                notifyCmdResponse(cmdCallback, result);
                writeNextCmd();
            }
        }
    }

    void onConnected() {
        Log.d(TAG, "onConnected: ");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Iterator<Listener> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    boolean delete = listener.onConnected(LMBleConnection.this);
                    if (delete) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    void onDisconnected() {
        Log.d(TAG, "onDisconnected: ");
        isServiceDiscovered = false;
        close();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Iterator<Listener> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    boolean delete = listener.onDisconnected(LMBleConnection.this);
                    if (delete) {
                        iterator.remove();
                    }
                }

            }
        });
    }

    void onServicesDiscovered() {
        Log.d(TAG, "onServicesDiscovered: ");
        getCharacteristics();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Iterator<Listener> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    Listener listener = iterator.next();
                    boolean delete = listener.onServicesDiscovered(LMBleConnection.this);
                    if (delete) {
                        iterator.remove();
                    }
                }

            }
        });
    }

    public void registerListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        /**
         * @param connection
         * @return if true the listener will be unregister
         */
        boolean onConnected(LMBleConnection connection);

        /**
         * @param connection
         * @return if true the listener will be unregister
         */
        boolean onDisconnected(LMBleConnection connection);

        /**
         * @param connection
         * @return if true the listener will be unregister
         */
        boolean onServicesDiscovered(LMBleConnection connection);
    }

}

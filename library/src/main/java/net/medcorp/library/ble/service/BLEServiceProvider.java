package net.medcorp.library.ble.service;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by karl-john on 6/6/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEServiceProvider {

    private Handler handler = new Handler();

    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer gattServer;

    private List<BluetoothDevice> connectedDevices = new ArrayList<>();

    private final UUID serviceUUID = UUID.fromString("F0BA3124-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID controlPointCharacteristics = UUID.fromString("F0BA3126-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID dataSourceCharacteristicsUUID = UUID.fromString("F0BA3127-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID alertSourceUUID = UUID.fromString("F0BA3125-6CAC-4C99-9089-4B0A1DF45002");

    public BLEServiceProvider(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
                bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                gattServer = bluetoothManager.openGattServer(context, mGattServerCallback);
                initServer();
                return;
            }
        }
        Toast.makeText(context, "Device does not support notifications.", Toast.LENGTH_LONG).show();
    }

    private void initServer() {
        BluetoothGattService service =new BluetoothGattService(serviceUUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic alertSourceCharacteristics= new BluetoothGattCharacteristic(alertSourceUUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic dataSourceCharacteristics = new BluetoothGattCharacteristic(dataSourceCharacteristicsUUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic offsetCharacteristic = new BluetoothGattCharacteristic(controlPointCharacteristics,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(dataSourceCharacteristics);
        service.addCharacteristic(alertSourceCharacteristics);
        service.addCharacteristic(offsetCharacteristic);
        gattServer.addService(service);
    }

    public void closeServer(){
        handler.removeCallbacks(notifyRunnable);
        if (gattServer == null) {
            return;
        }
        gattServer.close();
    }

    public void startAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            return;
        }
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(serviceUUID))
                .build();
        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    public void stopAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            return;
        }
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    private void notifyConnectedDevices() {
        for (BluetoothDevice device : connectedDevices) {
            BluetoothGattCharacteristic readCharacteristic = gattServer.getService(serviceUUID)
                    .getCharacteristic(controlPointCharacteristics);
            // TODO Send something?
            readCharacteristic.setValue(serviceUUID.toString());
            gattServer.notifyCharacteristicChanged(device, readCharacteristic, false);
        }
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i("Karl", "Peripheral Advertise Started.");
            Log.i("Karl", "GATT Server Ready to go bro!.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w("Karl", "Peripheral Advertise Failed: "+errorCode);
        }
    };

    private void postDeviceChange(final BluetoothDevice device, final boolean toAdd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Trigger our periodic notification once devices are connected
                handler.removeCallbacks(notifyRunnable);
                connectedDevices.add(device);
                if (!connectedDevices.isEmpty()) {
                    handler.post(notifyRunnable);
                }
            }
        });
    }

    private Runnable notifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectedDevices();
            handler.postDelayed(this, 2000);
        }
    };

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                postDeviceChange(device, false);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i("Karl", "onCharacteristicWriteRequest " + characteristic.getUuid().toString());
            if (controlPointCharacteristics.equals(characteristic.getUuid())) {
                notifyConnectedDevices();
            }
        }
    };


}

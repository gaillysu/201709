package net.medcorp.library.ble.service;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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

    private Handler mHandler = new Handler();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private List<BluetoothDevice> mConnectedDevices = new ArrayList<>();

    private final UUID serviceUUID = UUID.fromString("F0BA3124-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID controlPointCharacteristics = UUID.fromString("F0BA3126-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID dataSourceCharacteristicsUUID = UUID.fromString("F0BA3127-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID alertSourceUUID = UUID.fromString("F0BA3125-6CAC-4C99-9089-4B0A1DF45002");


    public BLEServiceProvider(Context context) {
        mBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                mGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);
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
        mGattServer.addService(service);
    }

    public void closeServer(){
        mHandler.removeCallbacks(mNotifyRunnable);

        if (mGattServer == null) return;

        mGattServer.close();
    }

    public void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

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
        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    private void notifyConnectedDevices() {
        for (BluetoothDevice device : mConnectedDevices) {

            BluetoothGattCharacteristic readCharacteristic = mGattServer.getService(serviceUUID)
                    .getCharacteristic(controlPointCharacteristics);
            // TODO Send something?
//            readCharacteristic.setValue(getStoredValue());
            mGattServer.notifyCharacteristicChanged(device, readCharacteristic, false);
        }
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Trigger our periodic notification once devices are connected
                mHandler.removeCallbacks(mNotifyRunnable);
                if (!mConnectedDevices.isEmpty()) {
                    mHandler.post(mNotifyRunnable);
                }
            }
        });
    }

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectedDevices();
            mHandler.postDelayed(this, 2000);
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
            if (dataSourceCharacteristicsUUID.equals(characteristic.getUuid())) {
                // todo Read response.
//                mGattServer.sendResponse(device,
//                        requestId,
//                        BluetoothGatt.GATT_SUCCESS,
//                        0,
//                        DeviceProfile.bytesFromInt(mTimeOffset));
            }

            if (alertSourceUUID.equals(characteristic.getUuid())) {
                // todo Read response.
            }

            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i("Karl", "onCharacteristicWriteRequest " + characteristic.getUuid().toString());
            if (controlPointCharacteristics.equals(characteristic.getUuid())) {
                if (responseNeeded) {
                    // todo Read response.
                    mGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                notifyConnectedDevices();
            }
        }
    };
}
